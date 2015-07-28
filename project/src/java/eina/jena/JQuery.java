package eina.jena;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.resultset.ResultSetException;
import eina.utils.Log;

/**
 *
 * @author Guillermo Esteban
 */
public class JQuery {

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
        Log.getLog().info("<JQuery:doQuery> Realizando consulta a DBPedia...");
        Log.getLog().debug("<JQuery:doQuery> SPARQL QUERY: \n " + this.toString());
        ResultSet resultSet = qexec.execSelect();
        JResult rs = new JResult(resultSet);
        qexec.close();
        return rs;
    }
}
