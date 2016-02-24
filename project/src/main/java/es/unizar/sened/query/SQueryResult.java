package es.unizar.sened.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

/**
 * @author gesteban@unizar.es
 */
public class SQueryResult {

  private List<String> varNames;
  private List<QuerySolution> results;

  protected SQueryResult(ResultSet rs) {
    varNames = rs.getResultVars();
    results = ResultSetFormatter.toList(rs);
  }

  public int getResultSize() {
    return results.size();
  }

  public int getColumnCount() {
    return varNames.size();
  }

  public Set<String> asSimpleColumn() {
    return new HashSet<String>(this.getColumn(0));
  }

  public QuerySolution getRow(int row) {
    return results.get(row);
  }

  public List<String> getSimpleRow(int row) {
    List<String> returnThis = new ArrayList<String>();
    for (int i = 0; i < varNames.size(); i++) {
      returnThis.add(results.get(row).get(varNames.get(i)).toString());
    }
    return returnThis;
  }

  public List<String> getColumn(int column) {
    List<String> returnThis = new ArrayList<String>();
    for (int i = 0; i < results.size(); i++) {
      returnThis.add(results.get(i).get(varNames.get(column)).toString());
    }
    return returnThis;
  }

  public String getColumnName(int column) {
    return varNames.get(column);
  }

  public String getElement(int row, int column) {
    return results.get(row).get(varNames.get(column)).toString();
  }

  public String getElement(int row, String columnName) {
    return results.get(row).get(columnName).toString();
  }

}
