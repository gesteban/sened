package es.unizar.sened.query;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.resultset.ResultSetException;

/**
 * @author gesteban@unizar.es
 */
public class SQuery {

  public static final String TAG = SQuery.class.getSimpleName();

  private Query query;
  private QueryExecution qexec;

  protected SQuery(String queryString, String endpoint) {
    this.query = QueryFactory.create(queryString);
    this.qexec = QueryExecutionFactory.sparqlService(endpoint, query);
  }

  protected SQuery(String queryString, Model model) {
    this.query = QueryFactory.create(queryString);
    this.qexec = QueryExecutionFactory.create(query, model);
  }

  protected SQuery(String queryString, Dataset dataset) {
    this.query = QueryFactory.create(queryString);
    this.qexec = QueryExecutionFactory.create(query, dataset);
  }

  @Override
  public String toString() {
    return query.toString();
  }

  public SQueryResult doSelect() throws ResultSetException, HttpException {
    ResultSet resultSet = qexec.execSelect();
    SQueryResult rs = new SQueryResult(resultSet);
    qexec.close();
    return rs;
  }

  public Model doDescribe() throws ResultSetException, HttpException {
    Model resultModel;
    if (qexec instanceof QueryEngineHTTP) {
      QueryEngineHTTP qehttp = (QueryEngineHTTP) qexec;
      qehttp.setModelContentType(WebContent.contentTypeJSONLD);
      resultModel = qehttp.execDescribe();
      qehttp.close();
    } else {
      resultModel = qexec.execDescribe();
    }
    qexec.close();
    return resultModel;
  }

  public Model doConstruct() throws ResultSetException, HttpException {
    Model resultModel;
    if (qexec instanceof QueryEngineHTTP) {
      QueryEngineHTTP qehttp = (QueryEngineHTTP) qexec;
      qehttp.setModelContentType(WebContent.contentTypeJSONLD);
      resultModel = qehttp.execConstruct();
      qehttp.close();
    } else {
      resultModel = qexec.execConstruct();
    }
    qexec.close();
    return resultModel;
  }

}
