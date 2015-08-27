package eina.lucene;

import eina.ontology.OntologyManager;
import eina.ontology.beans.Article;
import eina.utils.Log;
import eina.utils.PropAndDir;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Clase que se encarga de la creación y mantenimiento de un índice usando
 * Lucene que almacena e indexa los resultados de las búsquedas a la DBPedia de
 * manera que para encontrar resultados previamente consultados no se tenga que
 * volver a recurrir a una llamada a la DBPedia. <p> Desde nuestro punto de
 * vista se almacenarán dos tipos de documentos: <ul> <li>Documento "resource":
 * cada documento de éste tipo contiene la siguiente información de un recurso
 * extraído de la DBPedia:<br> [URI, label, abstract, type, category... ]
 * <li>Documento "keyword": cada documento de éste tipo almacenará una palabra
 * cuyos resultados están en los documentos tipo resource ya presentes en el
 * índice Lucene; contiene la siguiente información:<br> [keyword, category... ]
 * </ul>
 * 
 * @author Guillermo Esteban
 */
public class LuceneIndex {

    private OntologyManager om = OntologyManager.getInstance();
    private final long MAX_DIRECTORY_BYTES = 200000000; // 200 MB
    private final int maxSearcherResults = 50;
    private RAMDirectory directory;
    private CustomAnalyzer analyzer = new CustomAnalyzer(Version.LUCENE_36, om.getExternalDataRetriavablePropertyFragment());
//    private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
    private DocumentFactory df = new DocumentFactory();
    

    public LuceneIndex() {
        try {
            directory = new RAMDirectory();
            IndexWriter writer = new IndexWriter(directory, newConfig());
            writer.addDocument(df.createResourceDocument(new Article("no"), "no"));
            writer.close();
        } catch (Exception ex) {
            Log.getLog().error("<LuceneIndex:LuceneIndex> Error en el constructor");
            ex.printStackTrace();
        }
    }

    public void save() throws IOException {
        String stringDir = "file:/" + System.getProperty("user.dir").replace("\\", "/") + "/_lucene_directory";
        FSDirectory newDirectory = new SimpleFSDirectory(new File(URI.create(stringDir)), null);
        for (String str : directory.listAll()) {
            directory.copy(newDirectory, str, str);
        }
        newDirectory.close();
    }

    public void load() throws IOException {
        String stringDir = "file:/" + System.getProperty("user.dir").replace("\\", "/") + "/_lucene_directory";
        File file = new File(URI.create(stringDir));
        if (!file.exists()) {
            file.mkdir();
        }
        FSDirectory newDirectory = new SimpleFSDirectory(file, null);
        for (String str : newDirectory.listAll()) {
            newDirectory.copy(directory, str, str);
        }
        newDirectory.close();
        Log.getLog().debug("<LuceneIndex:load> Cargado índice desde "+stringDir);
    }

    /**
     * Devuelve una nueva IndexWriterConfig, metodo existente para asuntos
     * internos, ya que es necesario usar una configuracion nueva cada vez que
     * se crea un IndexWriter
     *
     * @return IndexWriterConfig Nueva configuracion para un IndexWriter
     */
    private IndexWriterConfig newConfig() {
        return new IndexWriterConfig(Version.LUCENE_36, analyzer).setMaxBufferedDeleteTerms(1).setMergePolicy(new TieredMergePolicy());
    }

    /**
     * Añade una lista de documentos al índice. <p> Cada documento se inserta a
     * través del método
     * {@link #updateResourceDocument(Document)}, que verifica la existencia del
     * documento antes de su inserción. <p> See {@link #updateResourceDocument(Document)}
     *
     * @param lista
     */
    private void updateResourceDocuments(Set<Document> lista, boolean suma) throws IOException {
        int cuenta = 0;
        for (Document doc : lista) {
            switch (updateResourceDocument(doc, suma)) {
                case 0:
                case 1:
                    cuenta++;
            }
        }
        Log.getLog().info("<LuceneIndex:addDocuments> Añadidos/Actualizados "
                + cuenta + " documentos del indice");
    }

    /**
     * Añade un documento al índice. <p> <b>NO</b> verifica si el documento
     * existe ya o no, simplemente lo añade, con lo que debe verificarse antes
     * manualmente que no existe el documento a añadir.
     *
     * @param doc Documento a añadir al índice
     */
    private void addDocument(Document doc) {
        try {
            IndexWriter writer = new IndexWriter(directory, newConfig());
            writer.addDocument(doc);
            writer.close();
        } catch (Exception ex) {
            Log.getLog().error("<LuceneIndex:addDocument> Exception");
            ex.printStackTrace();
        }
    }

    /**
     * Devuelve cierto siempre y cuando exista un documento con field KEYWORD
     * igual al dado y una KW_CATEGORY igual al currentCategory de éste objeto.
     *
     * @param keyword Palabra clave a buscar
     * @return Cierto si existe documento con
     * [KEYWORD=keyword,KW_CATEGORY=currentCategory] en Lucene, falso en caso
     * contrario
     * @throws IOException
     * @throws ParseException
     */
    public boolean existsKeywordDocument(String keyword, String categoryName) throws IOException {
        BooleanQuery bq = new BooleanQuery();
        PhraseQuery query2 = new PhraseQuery();
        query2.add(new Term(FieldID.KEYWORD.toString(), keyword));
        PhraseQuery query = new PhraseQuery();
        query.add(new Term(FieldID.KW_CATEGORY.toString(), categoryName));
        bq.add(query2, BooleanClause.Occur.MUST);
        bq.add(query, BooleanClause.Occur.MUST);
        Log.getLog().debug("<LuceneIndex:existsKeywordDocument> Query: " + bq.toString());
        IndexSearcher searcher = new IndexSearcher(directory);
        TopDocs hits = searcher.search(bq, maxSearcherResults);
        return hits.totalHits > 0;
    }

    /**
     * Añade o actualiza un documento a Lucene tomando el field KEYWORD {@link utils.Constants}
     * como identificador único, si éste no existe aún en el índice Lucene lo
     * añade, si este KEYWORD ya existia, actualiza la entrada del documento
     * añadiendole un field KW_CATEGORY extra con la nueva categoria, si esta
     * categoria ya existia para el documento no hace nada.
     *
     * @param keyword Identificador del documento a añadir/actualizar
     * @param newCategory Categoría con la que añadimos/actualizamos la clave
     * @return El entero que devuelve significa: <ul> <li> 1 -> new keyword
     * document created and indexed <li> 0 -> keyword document updated OR
     * nothing to do <li> -1 -> error </ul>
     * @throws IOException
     * @throws ParseException
     */
    private int updateKeywordDocument(Document doc) throws IOException {

        // Primero buscamos los documentos que tengan un field con el keyword buscado
        IndexSearcher searcher = new IndexSearcher(directory);

        BooleanQuery query = new BooleanQuery();
        PhraseQuery pq = new PhraseQuery();
        pq.add(new Term(FieldID.KEYWORD.toString(), doc.get(FieldID.KEYWORD.toString())));
        query.add(pq, BooleanClause.Occur.MUST);
        //System.out.println("<LuceneIndex:updateDocument> Query: "+bq.toString());
        TopDocs hits = searcher.search(query, maxSearcherResults);
        // Comprobamos el total de hits
        if (hits.totalHits == 0) {
            // Sin hits, se crea una nueva entrada
            addDocument(doc);
            Log.getLog().debug("<LuceneIndex:updateKeywordDocument> Documento KEYWORD añadido ("
                    + doc.get(FieldID.KEYWORD.toString()) + ")");
            searcher.close();
            return 1;
        } else if (hits.totalHits > 1) {
            // Mas de un hit, problema
            Log.getLog().error("<LuceneIndex:updateKeywordDocument> Encontrados mas de un hit buscando por un keyword");
            searcher.close();
            return -1;
        } else { // totalHits == 1
            // Un solo hit, extraemos el documento que ha hecho hit
            Document auxDoc = searcher.doc(hits.scoreDocs[0].doc);
            // Ahora buscamos en el documento por si la categoria que queremos añadir
            // ya se encuentra presente en alguno de sus fields
            boolean yaExiste = false;
            Fieldable[] categorias = auxDoc.getFieldables(FieldID.KW_CATEGORY.toString());
            for (int i = 0; i < categorias.length && !yaExiste; i++) {
                yaExiste = categorias[i].stringValue().equalsIgnoreCase(doc.get(FieldID.KEYWORD.toString()));
            }

            if (!yaExiste) {
                // En caso de que no exista se actualiza el documento, se elimina el viejo
                // y se añade el nuevo actualizado
                IndexWriter writer = new IndexWriter(directory, newConfig());
                auxDoc.add(new Field(FieldID.KW_CATEGORY.toString(), doc.get(FieldID.KW_CATEGORY.toString()),
                        Field.Store.YES, Field.Index.ANALYZED));
                auxDoc.removeField(FieldID.LAST_USE.toString());
                auxDoc.add(new Field(FieldID.LAST_USE.toString(), String.valueOf(new Date().getTime()),
                        Field.Store.YES, Field.Index.NO));
                writer.deleteDocuments(query);
                writer.addDocument(auxDoc);
                writer.close();
                Log.getLog().debug("<LuceneIndex:updateKeywordDocument> Documento KEYWORD actualizado ("
                        + doc.get(FieldID.KEYWORD.toString()) + ")");
                searcher.close();
                return 0;
            } else {
                // El hit encontrado ya tiene la categoria que le ibamos a incluir
                // lo que significa que hemos hecho algo raro
                Log.getLog().error("<LuceneIndex:updateKeywordDocument> Ya existe la categoria que ibamos"
                        + " a incluir en el keyword " + auxDoc.get(FieldID.KEYWORD.toString()));
                searcher.close();
                return -1;
            }
        }
    }

    /**
     * Añade o actualiza un documento a Lucene tomando el field URI {@link utils.Constants}
     * como identificador único, si éste no existe aún en el índice Lucene lo
     * añade, si esa URI ya existia, actualiza la entrada del documento
     * añadiendole un field CATEGORY extra con la nueva categoria, si esta
     * categoria ya existia para el documento no hace nada.
     *
     * @param newDoc Documento a añadir/actualizar en el índice Lucene
     * @return El entero que devuelve significa: <ul> <li> 1 -> new keyword
     * document created and indexed <li> 0 -> keyword document updated OR
     * nothing to do <li> -1 -> error </ul>
     * @throws IOException
     */
    private int updateResourceDocument(Document newDoc, boolean suma) throws IOException {
        // Primero buscamos los documentos que tengan un field con el URI buscado
        IndexSearcher searcher = new IndexSearcher(directory);
        BooleanQuery bq = new BooleanQuery();
        PhraseQuery pq = new PhraseQuery();
        pq.add(new Term(FieldID.URI.toString(), newDoc.get(FieldID.URI.toString())));
        bq.add(pq, BooleanClause.Occur.MUST);
//        System.out.println("<LuceneIndex:updateDocument> Query: " + bq.toString());
        TopDocs hits = searcher.search(bq, maxSearcherResults);
        // Comprobamos el total de hits
        if (hits.totalHits == 0) {
            // Sin hits, se crea una nueva entrada
            addDocument(newDoc);
            Log.getLog().debug("<LuceneIndex:updateResourceDocument> Documento añadido ("
                    + newDoc.get(FieldID.URI.toString()) + " - " + newDoc.get(FieldID.CATEGORY.toString()) + ")");
            searcher.close();
            return 1;
        } else if (hits.totalHits > 1) {
            // Mas de un hit, problema
            Log.getLog().error("<LuceneIndex:updateDocument> Encontrados más de un hit buscando por URI");
            searcher.close();
            return -1;
        } else { // totalHits == 1
            // Un solo hit, extraemos el documento que ha hecho hit
            Document doc = searcher.doc(hits.scoreDocs[0].doc);
            // Ahora si el valor del booleano de entrada es cierto significará que el documento que
            // ahora tenemos que actualizar tendrá que sumar uno en el campo FieldID.COUNT
            if (suma) {
                try {
                    int oldValue = Integer.parseInt(doc.get(FieldID.COUNT.toString()));
                    doc.removeField(FieldID.COUNT.toString());
                    doc.add(new Field(FieldID.COUNT.toString(), String.valueOf(oldValue + 1),
                            Field.Store.YES, Field.Index.NO));
                } catch (Exception ex) {
                    Log.getLog().error("<LuceneIndex:updateResourceDocument> Error parseando el entero de FieldID.COUNT");
                }
            }
            // Ahora buscamos en el documento por si la categoria que queremos añadir
            // ya se encuentra presente en alguno de sus fields
            boolean yaExiste = false;
            Fieldable[] categorias = doc.getFieldables(FieldID.CATEGORY.toString());
            for (int i = 0; i < categorias.length && !yaExiste; i++) {
                yaExiste = categorias[i].stringValue().equalsIgnoreCase(newDoc.get(FieldID.CATEGORY.toString()));
            }
            // En caso de que no exista se actualiza el documento, se elimina el viejo
            // y se añade el nuevo actualizado
            if (!yaExiste) {
                IndexWriter writer = new IndexWriter(directory, newConfig());
                doc.add(new Field(FieldID.CATEGORY.toString(), newDoc.get(FieldID.CATEGORY.toString()),
                        Field.Store.YES, Field.Index.ANALYZED));
                writer.deleteDocuments(bq);
                writer.addDocument(doc);
                writer.close();
                Log.getLog().debug("<LuceneIndex:updateDocument> Documento actualizado ("
                        + doc.get(FieldID.URI.toString()) + ")");
                searcher.close();
                return 0;
            } else {
                // El hit encontrado ya tiene la categoria que le ibamos a incluir
                // lo que significa que o bien ese resultado ya existía desde otra búsqueda
                // en la misma categoría, o bien hay label duplicada en la DBPedia.
                // De todas maneras hay que actualizar si COUNT ha variado
                if (suma) {
                    IndexWriter writer = new IndexWriter(directory, newConfig());
                    writer.deleteDocuments(bq);
                    writer.addDocument(doc);
                    writer.close();
                    Log.getLog().debug("<LuceneIndex:updateDocument> Documento actualizado ("
                            + doc.get(FieldID.URI.toString()) + ")");
                    searcher.close();
                    return 0;
                } else {
                    Log.getLog().debug("<LuceneIndex:updateDocument> Documento ya existente ("
                            + doc.get(FieldID.URI.toString()) + ")");
                    searcher.close();
                    return -1;
                }

            }
        }
    }

    public void add(String keyword, Set<Article> articleSet, String categoryName) throws Exception {
        if (updateKeywordDocument(df.createKeywordDocument(keyword, categoryName)) == 1) {
            // Se ha creado un keywordDocument nuevo, lo que significa que los resourceDocument
            // que se vayan a actualizar a continuación tendrán que sumar uno a su FieldID.COUNT
            updateResourceDocuments(df.createResourceDocuments(articleSet, categoryName), true);
        } else {
            updateResourceDocuments(df.createResourceDocuments(articleSet, categoryName), false);
        }
        save();
        while(directory.sizeInBytes()>MAX_DIRECTORY_BYTES) {
            deleteLastUsedKeywordSearch();
        }
    }

    public List<Article> searchKeywords(Set<String> lista, String className) throws IOException {
        try {
            // Creamos la consulta para Lucene
            BooleanQuery boolQ = new BooleanQuery();
            OWLClass tipo;
            
            
            // A partir de este punto averiguaremos la propiedad marcada como keywordSearchable para
            // buscar en lucene ese campo
            String claveBuscada = null;
            if(om.getTaxonomyType().equals(om.TAXONOMY_CLASSES)) {
//                tipo = om.getOWLDataFactory().getOWLClass(IRI.create(om.getIRI().toString()+"#"+className));
                tipo = om.getOWLDataFactory().getOWLClass(IRI.create(className));
                System.out.println("************ "+tipo);
            } else if(om.getTaxonomyType().equals(om.TAXONOMY_CATEGORIES)) {
                tipo = om.getOWLDataFactory().getOWLClass(IRI.create(om.getIRI().toString()+"#Articulo"));
            } else {
                return null;
            }
            Set<PropAndDir> queryAbout = PropAndDir.getPropertiesAndDirectionFrom(tipo);
            for (PropAndDir propAndDir : queryAbout) {
                if (om.isKeywordSearchable(propAndDir.prop)) {
                    claveBuscada = propAndDir.prop.getIRI().getFragment().toString();
                    break;
                }
            }
            QueryParser qp = new QueryParser(Version.LUCENE_34, claveBuscada, analyzer);
            
            String qs = "";
            for (String kw : lista) {
                qs += kw + " ";
            }
            Query query = qp.parse(qs);
            PhraseQuery query2 = new PhraseQuery();
            query2.add(new Term(FieldID.CATEGORY.toString(), className));
            boolQ.add(query, BooleanClause.Occur.MUST);
            boolQ.add(query2, BooleanClause.Occur.MUST);
            Log.getLog().debug("<LuceneIndex:searchKeywords> Query: " + boolQ.toString());

            // Consultamos
            IndexSearcher searcher = new IndexSearcher(directory);
            TopDocs hits = searcher.search(boolQ, maxSearcherResults);
            Log.getLog().info("<LuceneIndex:searchKeywords> "
                    + hits.totalHits + " resultados encontrados en LuceneIndex");

            // Creo una lista con los documentos encontrados
            List<Document> hitDocs = new ArrayList<Document>();
            for (int i = 0; i < hits.totalHits && i < maxSearcherResults; i++) {
                hitDocs.add(searcher.doc(hits.scoreDocs[i].doc));
            }
            
            // Mejor resultado
            if (hits.totalHits != 0) {
                Document topDoc = searcher.doc(hits.scoreDocs[0].doc);
                Log.getLog().info("<LuceneIndex:searchKeywords> Mejor resultado: "
                        + hits.getMaxScore() + " -- " + topDoc.get(FieldID.URI.toString()));
            }

            return df.documentsToArticles(hitDocs); // tiene que devolver un List porque tiene que ir ordenado!!

        } catch (ParseException ex) {
            Log.getLog().error("<LuceneIndex:searchKeywords> ParseException");
            return null;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(directory.sizeInBytes());
    }

    /**
     * Este método elimina el keywordDocument más viejo presente en el índice,
     * ésto se realiza comprobando el campo FieldID.LAST_USE de todos los
     * keywordDocument. <p> Una vez se ha encontrado el más viejo, se buscan
     * todos los resourceDocument asociados a éste, que vienen a ser aquellos
     * que realizando una búsqueda de FieldID.KEYWORD y FieldID.KW_CATEGORY de
     * nuestro keywordDocument obtenemos como resultado. Una vez tenemos todos
     * los resourceDocument asociacos a éste bajamos en uno el campo
     * FieldID.COUNT de éstos documentos, indicando así que ya no están
     * "vinculados" a ningún keywordDocument. Al finalizar éste proceso, todos
     * los resourceDocument que tengan el FieldID.COUNT a cero son eliminados
     * del índice. <p> De ésta manera garantizamos que ningún borrado deje
     * documentos en nuestro índice que no estén correctamente "vinculados".
     */
    public void deleteLastUsedKeywordSearch() {
        try {
            IndexReader reader = IndexReader.open(directory, false);
            IndexSearcher searcher = new IndexSearcher(reader);

            // Primero realizaremos la búsqueda del keywordDocument más viejo
            QueryParser parser = new QueryParser(Version.LUCENE_36, "keyword", analyzer);
            Query q = null;
            try {
                q = parser.parse("[* TO * ]");
            } catch (ParseException ex) {
                Logger.getLogger(LuceneIndex.class.getName()).log(Level.SEVERE, null, ex);
            }
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            long valorMasViejo = Long.MAX_VALUE;
            int posicionMasViejo = -1;
            for (int i = 0; i < hits.totalHits; i++) {
                Document topDoc = searcher.doc(hits.scoreDocs[i].doc);
                Long actual = new Long(topDoc.get(FieldID.LAST_USE.toString()));
                if (valorMasViejo > actual) {
                    valorMasViejo = actual;
                    posicionMasViejo = i;
                }
            }
            Document kwDoc = searcher.doc(hits.scoreDocs[posicionMasViejo].doc);
            reader.deleteDocument(hits.scoreDocs[posicionMasViejo].doc);

            // Ahora que tenemos el keywordDocument más viejo crearemos una query
            // con los parámetros de este en los resourceDocument para obtener todos
            // los documentos que deben ser borrados
            BooleanQuery boolQ = new BooleanQuery();
            QueryParser qp = new QueryParser(Version.LUCENE_34, om.getExternalDataRetriavablePropertyFragment(), analyzer);
            Query query = qp.parse(kwDoc.get(FieldID.KEYWORD.toString()));
            PhraseQuery query2 = new PhraseQuery();
            ///////////////////////////////////////////////////
            // FALLO AQUI
            // SOLO TENEMOS EN CUENTA UNA DE LAS CATEGORIAS PARA ELIMINAR LOS RESOURCE_DOCUMENTS!?
            //
            // BASTARIA CON ELIMINAR TODOS LOS RESOURCE_DOCUMENTS QUE TENGAN CUALQUIERA DE LAS CATEGORIAS
            // QUE TIENE EL KW_DOC (Y EL KEYWORD POR SUPUESTO)
            ///////////////////////////////////////////////////
            query2.add(new Term(FieldID.CATEGORY.toString(), kwDoc.get(FieldID.KW_CATEGORY.toString())));
            boolQ.add(query, BooleanClause.Occur.MUST);
            boolQ.add(query2, BooleanClause.Occur.MUST);
            Log.getLog().debug("<LuceneIndex:deleteLastUsedKeywordSearch> Query de resourceDocuments: " + boolQ.toString());

            // Ahora buscaremos y recorreremos los documentos enlazados a este keywordDocument
            // Restando 1 a todos los campos FieldID.COUNT de los documentos, y en caso
            // de quedarse con valor 0, eliminarlos del índice
            TopDocs hits2 = searcher.search(boolQ, maxSearcherResults);
            Log.getLog().debug("<LuceneIndex:deleteLastUsedKeywordSearch> "
                    + hits2.totalHits + " resultados encontrados en LuceneIndex para borrar/actualizar");

            // Creo una lista con los documentos encontrados que vayan a ser actualizados y no eliminamos
            List<Document> listToAdd = new ArrayList<Document>();
            for (int i = 0; i < hits2.totalHits && i < maxSearcherResults; i++) {
                Document doc = searcher.doc(hits2.scoreDocs[i].doc);
                reader.deleteDocument(hits2.scoreDocs[i].doc);
                if(!doc.get(FieldID.COUNT.toString()).equals("1")) {
                    // Tiene más de un keywordDocument enlazado, lo añadiremos
                    // restando 1 de campo FieldID.COUNT
                    int newCount = Integer.parseInt(doc.get(FieldID.COUNT.toString())) + 1;
                    doc.removeField(FieldID.COUNT.toString());
                    doc.add(new Field(FieldID.COUNT.toString(), String.valueOf(newCount), 
                            Field.Store.YES, Field.Index.NO));
                    listToAdd.add(doc);
                }
                
            }
            searcher.close();
            reader.close();
            
            // Ahora añadiremos todos los documentos actualizados y llamaremos a IndexWriter.optimize,
            // que por alguna razón que desconozco es la única manera de que se borren correctamente los
            // documentos borrados en las anteriores líneas y quede el índice bien
            IndexWriter writer = new IndexWriter(directory, newConfig());
            for(Document docToAdd : listToAdd) {
                writer.addDocument(docToAdd);
            }
            writer.optimize();
            writer.close();



        } catch (Exception ex) {
            Log.getLog().error("<LuceneIndex:deleteLastUsedKeywordSearch> Excepcion");
            ex.printStackTrace();
        }
    }
}