package es.unizar.sened;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.tdb.TDBFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import es.unizar.sened.model.DomainOntology;
import es.unizar.sened.query.SQuery;
import es.unizar.sened.query.SQueryFactory;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.Utils;

/**
 * Proxy triple database.
 * 
 * This class works as a proxy triple database, serving as a local cache and querying the main tdb as needed.
 * 
 * Its intented usage is to alleviate the number of queries performed remotelly and speed up any rdf query related
 * service.
 * 
 * @author gesteban@unizar.es
 */
public class TdbProxy {

  /**
   * Default constructor.
   * 
   * @param mainQuery
   *          query factory of the main endpoint
   */
  public TdbProxy(SQueryFactory mainQuery) {

    // load _tdb
    _mainQuery = mainQuery;
    File tdbFile = new File(TDB_DIRECTORY + System.getProperty("file.separator") + TDB_NAME);
    tdbFile.mkdirs();
    _tdb = TDBFactory.createDataset(tdbFile.getAbsolutePath());

    // load _uriSet
    _uriSet = new HashSet<>();
    _uriSetFile = new File(TDB_DIRECTORY + System.getProperty("file.separator") + TDB_NAME + ".txt");
    if (!_uriSetFile.exists()) {
      try {
        _uriSetFile.createNewFile();
      } catch (IOException e) {
        Log.e(TAG, "<SenedTdb> error creating file " + _uriSetFile);
      }
    } else {
      loadUriSet();
    }

    // create local query factory
    _proxyQuery = new SQueryFactory(_tdb);

  }

  public Model get(String resourceUri, int depth) throws ExecutionException {
    if (depth < 1)
      return null;
    else if (depth == 1)
      return _modelCache.get(resourceUri);
    else {
      Resource resource = Utils.createResource(resourceUri);
      Model resultModel = _modelCache.get(resourceUri);
      StmtIterator iter = resultModel.listStatements(resource, null, (RDFNode) null);
      for (; iter.hasNext();) {
        Statement stmt = iter.next();
        if (stmt.getPredicate().isURIResource()
            && DomainOntology.getObjectProperties().contains(Utils.createProperty(stmt.getPredicate().getURI()))) {
          if (stmt.getSubject().isURIResource() && stmt.getSubject().getURI().equals(resourceUri)) {
            if (stmt.getObject().isURIResource())
              resultModel.add(get(stmt.getObject().asResource().getURI()));
          } else if (stmt.getObject().isURIResource() && stmt.getObject().asResource().getURI().equals(resourceUri)) {
            resultModel.add(get(stmt.getSubject().asResource().getURI()));
          } else {
            Log.e(TAG + "/get", "Unexpected error in statement");
          }
        }
      }
      return resultModel;
    }
  }
  
  public Model getDepth2(String resourceUri) throws ExecutionException {
    Resource resource = Utils.createResource(resourceUri);
    Model resultModel = _modelCache.get(resourceUri);
    StmtIterator iter = resultModel.listStatements(resource, null, (RDFNode) null);
    for (; iter.hasNext();) {
      Statement stmt = iter.next();
      if (stmt.getPredicate().isURIResource()
          && DomainOntology.getObjectProperties().contains(Utils.createProperty(stmt.getPredicate().getURI()))) {
        if (stmt.getSubject().isURIResource() && stmt.getSubject().getURI().equals(resourceUri)) {
          if (stmt.getObject().isURIResource())
            resultModel.add(_modelCache.get(stmt.getObject().asResource().getURI()));
        } else if (stmt.getObject().isURIResource() && stmt.getObject().asResource().getURI().equals(resourceUri)) {
          resultModel.add(_modelCache.get(stmt.getSubject().asResource().getURI()));
        } else {
          Log.e(TAG + "/getDepth2", "Unexpected error in statement");
        }
      }
    }
    return resultModel;
  }

  public long size() {
    _tdb.begin(ReadWrite.READ);
    long size = _tdb.getDefaultModel().size();
    _tdb.end();
    return size;
  }

  public void print() {
    _tdb.begin(ReadWrite.READ);
    RDFDataMgr.write(System.out, _tdb.getDefaultModel(), RDFFormat.TURTLE);
    _tdb.end();
  }

  public void close() {
    _tdb.close();
  }

  private static final String TAG = TdbProxy.class.getSimpleName();

  private final String TDB_DIRECTORY = ".sened_tdb";
  private final String TDB_NAME = "dataset_2";

  private final long REMOTE_QUERY_DELAY = 1000;
  private long REMOTE_QUERY_LAST_TIMESTAMP = 0;

  /**
   * Query factory for main tdb.
   */
  private SQueryFactory _mainQuery;

  /**
   * Query factory for this proxy tdb.
   */
  private SQueryFactory _proxyQuery;

  /**
   * Local triple database.
   */
  private Dataset _tdb;

  /**
   * Set containing all the URIs from resources loaded from remote endpoints and locally available.
   */
  private Set<String> _uriSet;

  /**
   * File for storing {@link #_uriSet} in disk.
   */
  private File _uriSetFile;

  /**
   * Cache storing the model of the resources.
   */
  private final LoadingCache<String, Model> _modelCache = CacheBuilder.newBuilder().maximumSize(100)
      .build(new CacheLoader<String, Model>() {
        @Override
        public Model load(String resourceUri) {
          return get(resourceUri);
        }
      });

  /**
   * Default method to get the model of a resource.
   * 
   * Do not use directly, instead use {@link #_modelCache}
   * 
   * @param resourceUri
   * @return the DESCRIBE model of the resource
   */
  private Model get(String resourceUri) {
    Model resultModel;
    // if we don't have the resource locally, do remote query and store
    // otherwhise just do a local query
    if (!_uriSet.contains(resourceUri)) {
      // sparql describe of resource
      Log.i(TAG + "/get", "<" + resourceUri + "> not in local tdb, querying remote endpoint...");
      SQuery query = _mainQuery.getDescribeQuery(resourceUri);
      waitDelay();
      resultModel = query.doDescribe();
      Log.i(TAG + "/get", "<" + resourceUri + "> query done");
      // write query result into tdb
      _tdb.begin(ReadWrite.WRITE);
      Model model = _tdb.getDefaultModel();
      model.add(resultModel.listStatements());
      _tdb.commit();
      _tdb.end();
      // free model resources
      model.close();
      // update list of locally available resources
      _uriSet.add(resourceUri);
      saveUriSet();
    } else {
      Log.i(TAG + "/get", "<" + resourceUri + "> locally available");
      _tdb.begin(ReadWrite.READ);
      // SQuery query = _proxyQuery.getDescribeQuery(resourceUri);
      // resultModel = query.doDescribe();
      SQuery query = _proxyQuery.getManualDescribeQuery(resourceUri);
      resultModel = query.doConstruct();
      _tdb.end();
    }
    return resultModel;
  }

  private void loadUriSet() {
    FileReader fileReader;
    try {
      fileReader = new FileReader(_uriSetFile);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      String line = null;
      while ((line = bufferedReader.readLine()) != null)
        _uriSet.add(line);
      bufferedReader.close();
    } catch (IOException e) {
      Log.e(TAG, "<loadUriSet> error reading file " + _uriSetFile);
    }
  }

  private void saveUriSet() {
    FileWriter fileWriter;
    try {
      fileWriter = new FileWriter(_uriSetFile);
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      for (String uri : _uriSet) {
        bufferedWriter.write(uri);
        bufferedWriter.newLine();
      }
      bufferedWriter.close();
    } catch (IOException e) {
      Log.e(TAG, "<loadUriSet> error writing file " + _uriSetFile);
    }
  }

  private void waitDelay() {
    long currentTime = System.currentTimeMillis();
    if (currentTime - REMOTE_QUERY_LAST_TIMESTAMP < REMOTE_QUERY_DELAY) {
      try {
        Thread.sleep(REMOTE_QUERY_DELAY - (currentTime - REMOTE_QUERY_LAST_TIMESTAMP));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    REMOTE_QUERY_LAST_TIMESTAMP = currentTime;
  }

}
