package es.unizar.sened.query;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.resultset.ResultSetException;

import es.unizar.sened.utils.Log;

/**
 * @author gesteban@unizar.es
 */
public class JQuery {

	public static final String TAG = JQuery.class.getSimpleName();

	private Query query;
	private QueryExecution qexec;

	protected JQuery(String queryString, JQueryFactoryConfig config) {
		for (String key : config.getDatasetMap().keySet()) {
			queryString = queryString.replace(key, config.getDatasetMap().get(key));
		}
		this.query = QueryFactory.create(queryString);
		this.qexec = QueryExecutionFactory.sparqlService(config.getEndpoint(), query);
	}

	@Override
	public String toString() {
		return query.toString();
	}

	public JResult doQuery() throws ResultSetException {
		Log.i(TAG, "<doQuery> Realizando consulta a DBPedia...");
		ResultSet resultSet = qexec.execSelect();
		JResult rs = new JResult(resultSet);
		qexec.close();
		return rs;
	}
}
