package es.unizar.sened.cache;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import com.google.common.base.MoreObjects;

import es.unizar.sened.model.DomainOntology;
import es.unizar.sened.model.PropAndDir;
import es.unizar.sened.model.SResource;
import es.unizar.sened.utils.Log;

/**
 * Cache class for sened system. It stores two kind of document:
 * <ul>
 * <li><b>keyword</b> document: represents an already performed query; contains the keyword and the taxonomy root uri
 * used
 * <li><b>resource</b> document: represents a resource, contains all the relevant information retrieved from the
 * endpoint and the taxonomy root uris used to retrieve this resource
 * </ul>
 * 
 * @author gesteban@unizar.es
 */
public class LuceneIndex {

  public static final String TAG = LuceneIndex.class.getSimpleName();

  private static final String LUCENE_INDEX_PATH = "file:/" + System.getProperty("user.dir").replace("\\", "/")
      + "/.directory";
  private final long MAX_DIRECTORY_BYTES = 200000000; // 200 MB
  private final int MAX_SEARCHER_RESULTS = 50;

  private RAMDirectory _directory;
  private Analyzer _analyzer;

  public LuceneIndex() {
    _analyzer = new SimpleAnalyzer();
    // _analyzer = new
    // CustomAnalyzer(_om.getDataRetriavablePropertyFragments());
    _directory = new RAMDirectory();
    try {
      IndexWriter writer = new IndexWriter(_directory, newConfig());
      writer.commit();
      writer.close();
    } catch (Exception ex) {
      Log.e(TAG, "<LuceneIndex> Error al construir");
      ex.printStackTrace();
    }
  }

  public void save() throws IOException {
    FSDirectory newDirectory = new SimpleFSDirectory(Paths.get(URI.create(LUCENE_INDEX_PATH)));
    for (String str : _directory.listAll()) {
      newDirectory.copyFrom(_directory, str, str, IOContext.DEFAULT);
    }
    newDirectory.close();
  }

  public void load() throws IOException {
    File file = new File(URI.create(LUCENE_INDEX_PATH));
    if (!file.exists()) {
      file.mkdir();
    }
    FSDirectory newDirectory = new SimpleFSDirectory(Paths.get(URI.create(LUCENE_INDEX_PATH)));
    for (String str : newDirectory.listAll()) {
      _directory.copyFrom(newDirectory, str, str, IOContext.DEFAULT);
    }
    newDirectory.close();
    Log.d(TAG, "<load> Index loaded from [" + LUCENE_INDEX_PATH + "]");
  }

  private IndexWriterConfig newConfig() {
    return new IndexWriterConfig(_analyzer).setMaxBufferedDeleteTerms(1).setMergePolicy(new TieredMergePolicy());
  }

  /**
   * Añade una lista de documentos al índice.
   * <p>
   * Cada documento se inserta a través del método {@link #updateResourceDocument(Document)}, que verifica la existencia
   * del documento antes de su inserción.
   * <p>
   * See {@link #updateResourceDocument(Document)}
   *
   * @param documentList
   */
  private void updateResourceDocuments(Set<Document> documentList, boolean increaseCount) throws IOException {
    int cuenta = 0;
    for (Document doc : documentList) {
      switch (updateResourceDocument(doc, increaseCount)) {
      case 0:
      case 1:
        cuenta++;
      }
    }
    Log.i(TAG, "<updateResourceDocuments> Añadidos/Actualizados " + cuenta + " documentos del índice");
  }

  /**
   * Añade un documento al índice.
   * <p>
   * <b>NO</b> verifica si el documento existe ya o no, simplemente lo añade, con lo que debe verificarse antes
   * manualmente que no existe el documento a añadir.
   *
   * @param doc
   *          Documento a añadir al índice
   */
  private void addDocument(Document doc) {
    try {
      IndexWriter writer = new IndexWriter(_directory, newConfig());
      writer.addDocument(doc);
      writer.commit();
      writer.close();
    } catch (Exception ex) {
      Log.e(TAG, "<addDocument> Exception");
      ex.printStackTrace();
    }
  }

  /**
   * Devuelve cierto siempre y cuando exista un documento con field KEYWORD igual al dado y una KW_CATEGORY igual al
   * currentCategory de este objeto.
   *
   * @param keyword
   *          Palabra clave a buscar
   * @return Cierto si existe documento con [KEYWORD=keyword,KW_CATEGORY=currentCategory] en Lucene, falso en caso
   *         contrario
   * @throws IOException
   * @throws ParseException
   */
  public boolean existsKeywordDocument(String keyword, String categoryName) throws IOException {
    BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
    PhraseQuery.Builder phraseQueryBuilder1 = new PhraseQuery.Builder();
    phraseQueryBuilder1.add(new Term(Fields.KEYWORD, keyword));
    PhraseQuery phraseQuery1 = phraseQueryBuilder1.build();
    booleanBuilder.add(phraseQuery1, BooleanClause.Occur.MUST);
    PhraseQuery.Builder phraseQueryBuilder2 = new PhraseQuery.Builder();
    phraseQueryBuilder2.add(new Term(Fields.KW_CATEGORY, categoryName));
    PhraseQuery phraseQuery2 = phraseQueryBuilder2.build();
    booleanBuilder.add(phraseQuery2, BooleanClause.Occur.MUST);
    BooleanQuery booleanQuery = booleanBuilder.build();
    Log.d(TAG, "<existsKeywordDocument> Query: " + booleanQuery.toString());
    IndexReader reader = DirectoryReader.open(_directory);
    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs hits = searcher.search(booleanQuery, MAX_SEARCHER_RESULTS);
    reader.close();
    return hits.totalHits > 0;
  }

  /**
   * Añade o actualiza un documento a Lucene tomando el field KEYWORD {@link utils.Constants} como identificador único,
   * si este no existe aún en el índice Lucene lo añade, si este KEYWORD ya existia, actualiza la entrada del documento
   * añadiendole un field KW_CATEGORY extra con la nueva categoria, si esta categoria ya existia para el documento no
   * hace nada.
   *
   * @param keyword
   *          Identificador del documento a añadir/actualizar
   * @param newCategory
   *          Categoría con la que añadimos/actualizamos la clave
   * @return El entero que devuelve significa:
   *         <ul>
   *         <li>1 -> new keyword document created and indexed
   *         <li>0 -> keyword document updated OR nothing to do
   *         <li>-1 -> error
   *         </ul>
   * @throws IOException
   * @throws ParseException
   */
  private int updateKeywordDocument(Document doc) throws IOException {

    // Primero buscamos los documentos que tengan un field con el keyword
    // buscado.
    BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
    PhraseQuery.Builder phraseQueryBuilder = new PhraseQuery.Builder();
    phraseQueryBuilder.add(new Term(Fields.KEYWORD.toString(), doc.get(Fields.KEYWORD.toString())));
    PhraseQuery phraseQuery = phraseQueryBuilder.build();
    booleanBuilder.add(phraseQuery, BooleanClause.Occur.MUST);
    BooleanQuery booleanQuery = booleanBuilder.build();
    IndexReader reader = DirectoryReader.open(_directory);
    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs hits = searcher.search(booleanQuery, MAX_SEARCHER_RESULTS);

    // Comprobamos el total de hits.
    if (hits.totalHits == 0) {
      // Sin hits, se crea una nueva entrada.
      addDocument(doc);
      Log.d(TAG, "<updateKeywordDocument> Documento KEYWORD añadido (" + doc.get(Fields.KEYWORD.toString()) + ")");
      reader.close();
      return 1;
    } else if (hits.totalHits > 1) {
      // Mas de un hit, problema.
      Log.e(TAG, "<updateKeywordDocument> Encontrados más de un hit buscando por un keyword");
      reader.close();
      return -1;
    } else { // totalHits == 1
      // Un solo hit, extraemos el documento que ha hecho hit.
      Document auxDoc = searcher.doc(hits.scoreDocs[0].doc);
      // Buscamos en el doc por si la categoria que queremos añadir ya se
      // encuentra presente en algun field.
      boolean yaExiste = false;
      IndexableField[] categorias = auxDoc.getFields(Fields.KW_CATEGORY.toString());
      for (int i = 0; i < categorias.length && !yaExiste; i++) {
        yaExiste = categorias[i].stringValue().equalsIgnoreCase(doc.get(Fields.KEYWORD.toString()));
      }

      if (!yaExiste) {
        // En caso de que no exista se actualiza el documento.
        // Se elimina el viejo y se añade el nuevo actualizado.
        IndexWriter writer = new IndexWriter(_directory, newConfig());
        auxDoc.add(new StringField(Fields.KW_CATEGORY.toString(), doc.get(Fields.KW_CATEGORY.toString()),
            Field.Store.YES));
        auxDoc.removeField(Fields.LAST_USE.toString());
        auxDoc.add(new StringField(Fields.LAST_USE.toString(), String.valueOf(new Date().getTime()), Field.Store.YES));
        writer.deleteDocuments(booleanQuery);
        writer.addDocument(auxDoc);
        writer.close();
        Log.d(TAG, "<updateKeywordDocument> Documento KEYWORD actualizado (" + doc.get(Fields.KEYWORD.toString()) + ")");
        reader.close();
        return 0;
      } else {
        // El hit encontrado ya tiene la categoria que le ibamos a
        // incluir.
        // Significa que hemos hecho algo mal en el código.
        Log.e(
            TAG,
            "<updateKeywordDocument> Ya existe la categoria que ibamos" + " a incluir en el keyword "
                + auxDoc.get(Fields.KEYWORD.toString()));
        reader.close();
        return -1;
      }
    }
  }

  /**
   * Añade o actualiza un documento a Lucene tomando el field URI {@link utils.Constants} como identificador único, si
   * este no existe aún en el índice Lucene lo añade, si esa URI ya existia, actualiza la entrada del documento
   * añadiendole un field CATEGORY extra con la nueva categoria, si esta categoria ya existia para el documento no hace
   * nada.
   *
   * @param newDoc
   *          Documento a añadir/actualizar en el índice Lucene
   * @return El entero que devuelve significa:
   *         <ul>
   *         <li>1 -> new keyword document created and indexed
   *         <li>0 -> keyword document updated OR nothing to do
   *         <li>-1 -> error
   *         </ul>
   * @throws IOException
   */
  private int updateResourceDocument(Document newDoc, boolean increaseCount) throws IOException {

    // Primero buscamos los documentos que tengan un field con el URI
    // buscado.
    IndexReader reader = DirectoryReader.open(_directory);
    IndexSearcher searcher = new IndexSearcher(reader);
    BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
    PhraseQuery.Builder phraseQueryBuilder = new PhraseQuery.Builder();
    phraseQueryBuilder.add(new Term(Fields.URI, newDoc.get(Fields.URI)));
    PhraseQuery phraseQuery = phraseQueryBuilder.build();
    booleanBuilder.add(phraseQuery, BooleanClause.Occur.MUST);
    BooleanQuery booleanQuery = booleanBuilder.build();
    TopDocs hits = searcher.search(booleanQuery, MAX_SEARCHER_RESULTS);

    // Comprobamos el total de hits.
    if (hits.totalHits == 0) {
      // Sin hits, se crea una nueva entrada.
      addDocument(newDoc);
      Log.d(
          TAG,
          "<updateResourceDocument> Documento RESOURCE añadido (" + newDoc.get(Fields.URI) + " - "
              + newDoc.get(Fields.CATEGORY) + ")");
      reader.close();
      return 1;
    } else if (hits.totalHits > 1) {
      // Mas de un hit, problema.
      Log.e(TAG, "<updateDocument> Encontrados más de un hit buscando por URI");
      reader.close();
      return -1;
    } else { // totalHits == 1
      // Un solo hit, extraemos el documento que ha hecho hit.
      Document doc = searcher.doc(hits.scoreDocs[0].doc);
      // Ahora si el valor del booleano de entrada es cierto significará
      // que el documento que
      // ahora tenemos que actualizar tendrá que sumar uno en el campo
      // FieldID.COUNT.
      if (increaseCount) {
        try {
          int oldValue = Integer.parseInt(doc.get(Fields.COUNT));
          doc.removeField(Fields.COUNT);
          doc.add(new StringField(Fields.COUNT, String.valueOf(oldValue + 1), Field.Store.YES));
        } catch (Exception ex) {
          Log.e(TAG, "<updateResourceDocument> Error parseando el entero de FieldID.COUNT");
        }
      }
      // Ahora buscamos en el documento por si la categoria que queremos
      // añadir
      // ya se encuentra presente en alguno de sus fields.
      boolean yaExiste = false;
      IndexableField[] categorias = doc.getFields(Fields.CATEGORY);
      for (int i = 0; i < categorias.length && !yaExiste; i++) {
        yaExiste = categorias[i].stringValue().equalsIgnoreCase(newDoc.get(Fields.CATEGORY));
      }
      // En caso de que no exista se actualiza el documento, se elimina el
      // viejo
      // y se añade el nuevo actualizado.
      if (!yaExiste) {
        IndexWriter writer = new IndexWriter(_directory, newConfig());
        doc.add(new StringField(Fields.CATEGORY, newDoc.get(Fields.CATEGORY), Field.Store.YES));
        writer.deleteDocuments(booleanQuery);
        writer.addDocument(doc);
        writer.close();
        Log.d(TAG, "<updateDocument> Documento actualizado (" + doc.get(Fields.URI) + ")");
        reader.close();
        return 0;
      } else {
        // El hit encontrado ya tiene la categoria que le ibamos a
        // incluir
        // lo que significa que o bien ese resultado ya existía desde
        // otra búsqueda
        // en la misma categoría, o bien hay label duplicada en la
        // DBPedia.
        // De todas maneras hay que actualizar si COUNT ha variado.
        if (increaseCount) {
          IndexWriter writer = new IndexWriter(_directory, newConfig());
          writer.deleteDocuments(booleanQuery);
          writer.addDocument(doc);
          writer.close();
          Log.d(TAG, "<updateDocument> Documento actualizado (" + doc.get(Fields.URI) + ")");
          reader.close();
          return 0;
        } else {
          Log.d(TAG, "<updateDocument> Documento ya existente (" + doc.get(Fields.URI) + ")");
          reader.close();
          return -1;
        }

      }
    }
  }

  public void add(String keyword, Set<SResource> articleSet, String categoryName) throws Exception {
    if (updateKeywordDocument(DocumentUtils.createKeywordDocument(keyword, categoryName)) == 1) {
      // Se ha creado un keywordDocument nuevo, lo que significa que los
      // resourceDocument
      // que se vayan a actualizar a continuación tendrán que sumar uno a
      // su FieldID.COUNT.
      updateResourceDocuments(DocumentUtils.createResourceDocuments(articleSet, categoryName), true);
    } else {
      updateResourceDocuments(DocumentUtils.createResourceDocuments(articleSet, categoryName), false);
    }

    save();
    while (_directory.ramBytesUsed() > MAX_DIRECTORY_BYTES) {
      deleteLastUsedKeywordSearch();
    }
  }

  public List<SResource> searchKeywords(Set<String> keywordSet, String taxonomyRoot) throws Exception {
    try {
      OntClass type;
      String keyToLookFor = null;
      if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CLASSES)) {
        throw new NotImplementedException("not yet implemented taxonomy class");
      } else if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CATEGORIES)) {
        type = DomainOntology.Void;
      } else {
        throw new Exception("non valid taxonomy type");
      }
      Set<PropAndDir> queryAbout = DomainOntology.getProperties(type);
      for (PropAndDir propAndDir : queryAbout) {
        if (DomainOntology.isKeywordSearchable(propAndDir.prop)) {
          keyToLookFor = propAndDir.prop.getLocalName();
          break;
        }
      }

      // Querying index.
      QueryParser qp = new QueryParser(keyToLookFor, _analyzer);
      String qs = "";
      for (String kw : keywordSet)
        qs += kw + " ";
      Query parsedQuery = qp.parse(qs);
      BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
      PhraseQuery.Builder phraseQueryBuilder = new PhraseQuery.Builder();
      phraseQueryBuilder.add(new Term(Fields.CATEGORY, taxonomyRoot));
      PhraseQuery phraseQuery = phraseQueryBuilder.build();
      booleanBuilder.add(parsedQuery, BooleanClause.Occur.MUST);
      booleanBuilder.add(phraseQuery, BooleanClause.Occur.MUST);
      BooleanQuery booleanQuery = booleanBuilder.build();
      Log.d(TAG, "<searchKeywords> Query: " + booleanQuery.toString());
      IndexReader reader = DirectoryReader.open(_directory);
      IndexSearcher searcher = new IndexSearcher(reader);
      TopDocs hits = searcher.search(booleanQuery, MAX_SEARCHER_RESULTS);
      Log.i(TAG, "<searchKeywords> " + hits.totalHits + " resultados encontrados en LuceneIndex");

      // Listing all hits.
      List<Document> hitDocs = new ArrayList<Document>();
      for (int i = 0; i < hits.totalHits && i < MAX_SEARCHER_RESULTS; i++) {
        hitDocs.add(searcher.doc(hits.scoreDocs[i].doc));
      }

      // Print best result.
      if (hits.totalHits != 0) {
        Document topDoc = searcher.doc(hits.scoreDocs[0].doc);
        Log.i(TAG, "<searchKeywords> Mejor resultado: " + hits.getMaxScore() + " -- " + topDoc.get(Fields.URI));
      }
      reader.close();
      return DocumentUtils.documentsToResources(hitDocs);

    } catch (ParseException ex) {
      Log.e(TAG, "<searchKeywords> ParseException");
      return null;
    }
  }

  /**
   * Este método elimina el keywordDocument más viejo presente en el índice, esto se realiza comprobando el campo
   * FieldID.LAST_USE de todos los keywordDocument.
   * <p>
   * Una vez se ha encontrado el más viejo, se buscan todos los resourceDocument asociados a este, que vienen a ser
   * aquellos que realizando una búsqueda de FieldID.KEYWORD y FieldID.KW_CATEGORY de nuestro keywordDocument obtenemos
   * como resultado. Una vez tenemos todos los resourceDocument asociacos a este bajamos en uno el campo FieldID.COUNT
   * de estos documentos, indicando así que ya no están "vinculados" a ningún keywordDocument. Al finalizar este
   * proceso, todos los resourceDocument que tengan el FieldID.COUNT a cero son eliminados del índice.
   * <p>
   * De esta manera garantizamos que ningún borrado deje documentos en nuestro índice que no estén correctamente
   * "vinculados".
   */
  public void deleteLastUsedKeywordSearch() {
    try {
      IndexReader reader = DirectoryReader.open(_directory);
      IndexSearcher searcher = new IndexSearcher(reader);

      // Primero realizaremos la búsqueda del keywordDocument más viejo.
      QueryParser parser = new QueryParser(Fields.KEYWORD, _analyzer);
      Query oldestDocQuery = null;
      oldestDocQuery = parser.parse("[* TO * ]");
      // TODO test new search with sorting to retrieve and delete oldest
      // document
      TopDocs hits = searcher.search(oldestDocQuery, Integer.MAX_VALUE, new Sort(new SortField(Fields.LAST_USE,
          Type.STRING, true)));
      Document oldestDoc = searcher.doc(hits.scoreDocs[0].doc);

      // Form the query to delete the oldest document.
      BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
      PhraseQuery.Builder phraseQueryBuilder = new PhraseQuery.Builder();
      phraseQueryBuilder.add(new Term(Fields.LAST_USE, oldestDoc.get(Fields.LAST_USE)));
      PhraseQuery phraseQuery = phraseQueryBuilder.build();
      booleanBuilder.add(phraseQuery, BooleanClause.Occur.MUST);
      IndexWriter writer = new IndexWriter(_directory, newConfig());
      writer.deleteDocuments(booleanBuilder.build());

      // Ahora que tenemos el keywordDocument más viejo crearemos una
      // query
      // con los parámetros de este en los resourceDocument para obtener
      // todos
      // los documentos que deben ser borrados.
      BooleanQuery.Builder booleanBuilder2 = new BooleanQuery.Builder();
      PhraseQuery.Builder phraseQueryBuilder2 = new PhraseQuery.Builder();
      phraseQueryBuilder2.add(new Term(Fields.CATEGORY, oldestDoc.get(Fields.KW_CATEGORY)));
      PhraseQuery phraseQuery2 = phraseQueryBuilder2.build();
      booleanBuilder2.add(phraseQuery2, BooleanClause.Occur.MUST);
      String kwdSearchableProp = null;
      for (OntProperty prop : DomainOntology.getProperties()) {
        if (DomainOntology.isKeywordSearchable(prop)) {
          kwdSearchableProp = prop.getLocalName();
        }
      }
      // TODO check all this shitty method AND CLASS
      QueryParser queryParser = new QueryParser(kwdSearchableProp, _analyzer);
      Query parsedQuery = queryParser.parse(oldestDoc.get(Fields.KEYWORD));
      booleanBuilder2.add(parsedQuery, BooleanClause.Occur.MUST);
      BooleanQuery booleanQuery2 = booleanBuilder2.build();
      // TODO check this comment
      // /////////////////////////////////////////////////
      // FALLO AQUI
      // SOLO TENEMOS EN CUENTA UNA DE LAS CATEGORIAS PARA ELIMINAR LOS
      // RESOURCE_DOCUMENTS!?
      //
      // BASTARIA CON ELIMINAR TODOS LOS RESOURCE_DOCUMENTS QUE TENGAN
      // CUALQUIERA DE LAS CATEGORIAS
      // QUE TIENE EL KW_DOC (Y EL KEYWORD POR SUPUESTO)
      // /////////////////////////////////////////////////
      Log.d(TAG, "<deleteLastUsedKeywordSearch> Query de resourceDocuments: " + booleanQuery2.toString());

      // Ahora buscaremos y recorreremos los documentos enlazados a este
      // keywordDocument
      // Restando 1 a todos los campos FieldID.COUNT de los documentos, y,
      // en caso
      // de quedarse con valor 0, eliminarlos del índice.
      TopDocs hits2 = searcher.search(booleanQuery2, MAX_SEARCHER_RESULTS);
      Log.d(TAG, "<deleteLastUsedKeywordSearch> " + hits2.totalHits
          + " resultados encontrados en LuceneIndex para borrar/actualizar");

      // Creo una lista con los documentos encontrados que vayan a ser
      // actualizados (no eliminados).
      List<Document> listToAdd = new ArrayList<Document>();
      for (int i = 0; i < hits2.totalHits && i < MAX_SEARCHER_RESULTS; i++) {
        Document doc = searcher.doc(hits2.scoreDocs[i].doc);
        if (!doc.get(Fields.COUNT).equals("1")) {
          // Tiene más de un keywordDocument enlazado, lo añadiremos
          // restando 1 de campo FieldID.COUNT
          int newCount = Integer.parseInt(doc.get(Fields.COUNT)) + 1;
          doc.removeField(Fields.COUNT);
          doc.add(new StringField(Fields.COUNT, String.valueOf(newCount), Field.Store.YES));
          listToAdd.add(doc);
        }

      }
      reader.close();
      // Deleting already in desuse documents (FieldID.COUNT = 0)
      // TODO test new way to delete documents
      writer.deleteDocuments(booleanQuery2);
      writer.commit();

      // Ahora añadiremos todos los documentos actualizados.
      for (Document docToAdd : listToAdd) {
        writer.addDocument(docToAdd);
      }
      writer.commit();
      writer.close();

    } catch (Exception ex) {
      Log.e(TAG, "<deleteLastUsedKeywordSearch> Exception (superb info, good luck)");
      ex.printStackTrace();
    }
  }

  public void printAllDocuments() throws IOException {

    IndexReader reader = DirectoryReader.open(_directory);
    Log.i(TAG, "~ KEYWORD documents ~");
    Log.i(TAG, "with KEYWORD  = " + reader.getDocCount(Fields.KEYWORD));
    Log.i(TAG, "with KW_CATEGORY = " + reader.getDocCount(Fields.KW_CATEGORY));
    Log.i(TAG, "with LAST_USE= " + reader.getDocCount(Fields.LAST_USE));
    Log.i(TAG, "~ RESOURCE documents ~");
    Log.i(TAG, "with URI = " + reader.getDocCount(Fields.URI));
    Log.i(TAG, "with CATEGORY = " + reader.getDocCount(Fields.CATEGORY));
    Log.i(TAG, "with COUNT = " + reader.getDocCount(Fields.COUNT));
    Log.i(TAG, "with abstract = " + reader.getDocCount("abstract"));
    reader.close();

  }

  public static void printDocument(Document document) {
    MoreObjects.ToStringHelper stringHelper = MoreObjects.toStringHelper(Document.class);
    for (IndexableField field : document.getFields()) {
      String key = field.name();
      String value = document.get(field.name());
      stringHelper.add(key, value.substring(0, Math.min(50, value.length())));
    }
    Log.i(TAG + "_PRINT_DOCUMENT", stringHelper.toString());
  }

}