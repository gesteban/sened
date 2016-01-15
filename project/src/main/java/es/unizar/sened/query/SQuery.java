package es.unizar.sened.query;

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

  protected SQuery(String queryString, SQueryFactoryConfig config) {
    this.query = QueryFactory.create(queryString);
    this.qexec = QueryExecutionFactory.sparqlService(config.getEndpoint(), query);
  }

  @Override
  public String toString() {
    return query.toString();
  }

  public SQueryResult doSelect() throws ResultSetException {
    ResultSet resultSet = qexec.execSelect();
    SQueryResult rs = new SQueryResult(resultSet);
    qexec.close();
    return rs;
  }

  public Model doDescribe() throws ResultSetException {
    QueryEngineHTTP qehttp = (QueryEngineHTTP) qexec;
    qehttp.setModelContentType(WebContent.contentTypeJSONLD);
    Model resultModel = qehttp.execDescribe();
    qehttp.close();
    qexec.close();
    return resultModel;
  }

}
