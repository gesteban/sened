package es.unizar.sened.store;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.rdf.model.Resource;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import es.unizar.sened.model.DomainOntology;
import es.unizar.sened.query.SQuery;
import es.unizar.sened.query.SQueryFactory;
import es.unizar.sened.query.SQueryResult;
import es.unizar.sened.utils.Log;

/**
 * @author gesteban@unizar.es
 */
public class LuceneIndex {

  public static LuceneIndex load(SQueryFactory mainQuery) {
    try {
      LuceneIndex index = new LuceneIndex(mainQuery, true);
      return index;
    } catch (IOException ex) {
      Log.e(TAG, ex.getMessage());
      return create(mainQuery);
    }
  }

  public static LuceneIndex create(SQueryFactory mainQuery) {
    try {
      LuceneIndex index = new LuceneIndex(mainQuery, false);
      return index;
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public Set<String> get(Set<String> keywordSet, Resource category, int depth) {
    for (String keyword : keywordSet) {
      // looking for already stored results
      if (!existDocument(keyword, category.getLocalName(), depth)) {
        // if it does not exist, perform a remote query
        SQueryResult results;
        try {
          results = _queryCache.get(new QueryParameters(keyword, category.getURI(), depth));
        } catch (ExecutionException e) {
          Log.d(TAG + "/get", "ExecutionException");
          return null;
        }
        // a resource document for each row
        Set<String> uriSet = new HashSet<>();
        for (int i = 0; i < results.getResultSize(); i++) {
          String uri = null;
          for (Iterator<String> iter = results.getRow(i).varNames(); iter.hasNext();) {
            String varName = iter.next();
            if (varName.toUpperCase().equals(Fields.URI)) // ignore other variables
              uri = results.getRow(i).getResource(varName).getURI();
          }
          if (uri == null)
            Log.e(TAG, "unexpected null result from last query");
          else
            uriSet.add(uri);
        }
        // storing the keyword document
        addDocument(createDocument(keyword, category.getLocalName(), depth, uriSet));
      }
    }
    printAllDocuments();
    return getUris(keywordSet, category.getLocalName(), depth);
  }

  public void printAllDocuments() {
    try {
      IndexReader reader = DirectoryReader.open(_directory);
      for (int i = 0; i < reader.maxDoc(); i++) {
        Document doc = reader.document(i);
        printDocument(doc);
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static final String TAG = LuceneIndex.class.getSimpleName();
  private static final String LUCENE_INDEX_PATH = "file:/" + System.getProperty("user.dir").replace("\\", "/")
      + "/.directory";
  private static final int MAX_SEARCHER_RESULTS = 50;

  private FSDirectory _directory;
  private Analyzer _analyzer;
  private SQueryFactory _query;

  private LuceneIndex(SQueryFactory mainQuery, boolean load) throws IOException {
    _query = mainQuery;
    _analyzer = new StandardAnalyzer();
    if (load)
      load();
    else
      create();
  }

  private void create() throws IOException {
    File file = new File(URI.create(LUCENE_INDEX_PATH));
    if (!file.exists()) {
      file.mkdir();
    }
    try {
      _directory = FSDirectory.open(Paths.get(URI.create(LUCENE_INDEX_PATH)));
      IndexWriter writer = new IndexWriter(_directory, newConfig());
      writer.commit();
      writer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void save() throws IOException {
    // FSDirectory memDirectory = new SimpleFSDirectory(Paths.get(URI.create(LUCENE_INDEX_PATH)));
    // for (String str : _directory.listAll()) {
    // memDirectory.copyFrom(_directory, str, str, IOContext.DEFAULT);
    // }
    // memDirectory.close();
  }

  private void load() throws IOException {
    _directory = FSDirectory.open(Paths.get(URI.create(LUCENE_INDEX_PATH)));
    if (!DirectoryReader.indexExists(_directory))
      throw new IOException("error loading index from " + LUCENE_INDEX_PATH);
    else {
      IndexReader reader = DirectoryReader.open(_directory);
      Log.d(TAG + "/load", "index loaded from " + LUCENE_INDEX_PATH + " with " + reader.numDocs() + " documents");
    }
  }

  private IndexWriterConfig newConfig() {
    return new IndexWriterConfig(_analyzer).setMaxBufferedDeleteTerms(1).setMergePolicy(new TieredMergePolicy());
  }

  private void addDocument(Document doc) {
    try {
      IndexWriter writer = new IndexWriter(_directory, newConfig());
      writer.addDocument(doc);
      writer.commit();
      writer.close();
      save();
      Log.d(TAG + "/addDocument", "added a document to the index");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean existDocument(String keyword, String categoryName, int depth) {
    BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
    PhraseQuery.Builder phraseQueryBuilder;
    PhraseQuery phraseQuery;
    phraseQueryBuilder = new PhraseQuery.Builder();
    phraseQueryBuilder.add(new Term(Fields.KEYWORD, keyword));
    phraseQuery = phraseQueryBuilder.build();
    booleanBuilder.add(phraseQuery, BooleanClause.Occur.MUST);
    phraseQueryBuilder = new PhraseQuery.Builder();
    phraseQueryBuilder.add(new Term(Fields.CATEGORY, categoryName));
    phraseQuery = phraseQueryBuilder.build();
    booleanBuilder.add(phraseQuery, BooleanClause.Occur.MUST);
    phraseQueryBuilder = new PhraseQuery.Builder();
    phraseQueryBuilder.add(new Term(Fields.DEPTH, depth + ""));
    phraseQuery = phraseQueryBuilder.build();
    booleanBuilder.add(phraseQuery, BooleanClause.Occur.MUST);
    BooleanQuery booleanQuery = booleanBuilder.build();
    IndexReader reader;
    try {
      reader = DirectoryReader.open(_directory);
      IndexSearcher searcher = new IndexSearcher(reader);
      TopDocs hits = searcher.search(booleanQuery, MAX_SEARCHER_RESULTS);
      reader.close();
      Log.d(TAG + "/existsDocument", booleanQuery.toString() + " ? " + (hits.totalHits > 0 ? "TRUE" : "FALSE"));
      return hits.totalHits > 0;
    } catch (IOException ex) {
      ex.printStackTrace();
      return false;
    }
  }

  private Set<String> getUris(Set<String> keywords, String categoryName, int depth) {
    Set<String> uriSet = new HashSet<>();
    for (String keyword : keywords) {
      uriSet.addAll(getUris(keyword, categoryName, depth));
    }
    return uriSet;
  }

  private Set<String> getUris(String keyword, String categoryName, int depth) {
    BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
    PhraseQuery.Builder phraseQueryBuilder;
    PhraseQuery phraseQuery;
    phraseQueryBuilder = new PhraseQuery.Builder();
    phraseQueryBuilder.add(new Term(Fields.KEYWORD, keyword));
    phraseQuery = phraseQueryBuilder.build();
    booleanBuilder.add(phraseQuery, BooleanClause.Occur.MUST);
    phraseQueryBuilder = new PhraseQuery.Builder();
    phraseQueryBuilder.add(new Term(Fields.CATEGORY, categoryName));
    phraseQuery = phraseQueryBuilder.build();
    booleanBuilder.add(phraseQuery, BooleanClause.Occur.MUST);
    phraseQueryBuilder = new PhraseQuery.Builder();
    phraseQueryBuilder.add(new Term(Fields.DEPTH, depth + ""));
    phraseQuery = phraseQueryBuilder.build();
    booleanBuilder.add(phraseQuery, BooleanClause.Occur.MUST);
    BooleanQuery booleanQuery = booleanBuilder.build();
    IndexReader reader;
    Set<String> uriSet = null;
    try {
      reader = DirectoryReader.open(_directory);
      IndexSearcher searcher = new IndexSearcher(reader);
      TopDocs hits = searcher.search(booleanQuery, MAX_SEARCHER_RESULTS);
      Document doc = null;
      if (hits.totalHits == 1)
        doc = searcher.doc(hits.scoreDocs[0].doc);
      else
        Log.e(TAG + "/getUris", booleanQuery.toString() + " ? " + hits.totalHits + " hits");
      reader.close();
      if (doc != null) {
        uriSet = new HashSet<>();
        for (IndexableField field : doc.getFields(Fields.URI))
          uriSet.add(field.stringValue());
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return uriSet;
  }

  private final LoadingCache<QueryParameters, SQueryResult> _queryCache = CacheBuilder.newBuilder().maximumSize(100)
      .build(new CacheLoader<QueryParameters, SQueryResult>() {
        @Override
        public SQueryResult load(QueryParameters params) {
          SQuery query = null;
          if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CLASSES))
            throw new NotImplementedException("class taxonomy not implemented yet");
          else if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CATEGORIES))
            query = _query.getKeywordQuery_CategoryTaxonomy_v2(params.getKeyword(), params.getResource(),
                params.getDepth());
          Log.d(TAG + "/_queryCache_v2", "\n" + query.toString());
          Log.i(TAG + "/_queryCache_v2", "querying keyword [" + params._keyword + "] remotelly");
          return query == null ? null : query.doSelect();
        }
      });

  private class QueryParameters {

    private final String _keyword;
    private final String _resourceUri;
    private final int _depth;

    public String getResource() {
      return _resourceUri;
    }

    public String getKeyword() {
      return _keyword;
    }

    public int getDepth() {
      return _depth;
    }

    public QueryParameters(String keyword, String resource, int depth) {
      _keyword = keyword;
      _resourceUri = resource;
      _depth = depth;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final QueryParameters other = (QueryParameters) obj;
      if (!this._keyword.equals(other._keyword)) {
        return false;
      }
      if (!this._resourceUri.equals(other._resourceUri)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int hash = 5;
      hash = 89 * hash + this._keyword.hashCode();
      hash = 89 * hash + this._resourceUri.hashCode();
      return hash;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(QueryParameters.class).add("keyword", _keyword).add("resource", _resourceUri)
          .toString();
    }
  }

  private class Fields {

    public static final String URI = "URI";
    public static final String KEYWORD = "KEYWORD";
    public static final String CATEGORY = "CATEGORY";
    public static final String DEPTH = "DEPTH";
    public static final String LAST_USE = "LAST_USE";

  }

  private static Document createDocument(String keyword, String category, int depth, Set<String> uriSet) {
    Document doc = new Document();
    doc.add(new StringField(Fields.KEYWORD, keyword, Field.Store.YES));
    doc.add(new StringField(Fields.CATEGORY, category, Field.Store.YES));
    doc.add(new StringField(Fields.DEPTH, depth + "", Field.Store.YES));
    doc.add(new StringField(Fields.LAST_USE, String.valueOf(new Date().getTime()), Field.Store.YES));
    if (uriSet != null)
      for (String uri : uriSet)
        doc.add(new StringField(Fields.URI, uri, Field.Store.YES));
    return doc;
  }

  private static void printDocument(Document document) {
    MoreObjects.ToStringHelper stringHelper = MoreObjects.toStringHelper(Document.class);
    for (IndexableField field : document.getFields())
      stringHelper.add(field.name(), field.stringValue().substring(0, Math.min(50, field.stringValue().length())));
    Log.d(TAG + "/printDocument", stringHelper.toString());
  }

}