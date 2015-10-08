package es.unizar.sened.query;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

import es.unizar.sened.ontology.Article;
import es.unizar.sened.ontology.SKOSCategory;

/**
 * @author gesteban@unizar.es
 */
public class JResult {

	private List<String> varNames;
	private List<QuerySolution> results;

	protected JResult(ResultSet rs) {
		this.varNames = rs.getResultVars();
		this.results = ResultSetFormatter.toList(rs);
	}

	public int getResultSize() {
		return results.size();
	}

	public int getColumnCount() {
		return varNames.size();
	}

	public Set<Article> asArticleSet() throws Exception {
		Set<Article> articleSet = new HashSet<Article>();
		for (QuerySolution qs : results) {
			// Creamos el articulo a partir del resultado
			Article article = new Article(URLDecoder.decode(qs.get("uri").toString(), "UTF-8"));
			for (String varName : varNames) {
				// article.add(varName, URLDecoder.decode(qs.get(varName).toString(), "UTF-8"));
				article.add(varName, new String(qs.get(varName).toString().getBytes(), "UTF-8"));
			}
			articleSet.add(article);
		}
		return articleSet;
	}

	public Set<SKOSCategory> asCategorySet() {
		List<String> uriList = this.getColumn(0);
		Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
		for (int i = 0; i < uriList.size(); i++) {
			categorySet.add(new SKOSCategory(uriList.get(i)));
		}
		return categorySet;
	}

	public Set<String> asSimpleColumn() {
		return new HashSet<String>(this.getColumn(0));
	}

	public List<String> getRow(int row) {
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

	public String getElemment(int row, String columnName) {
		return results.get(row).get(columnName).toString();
	}

}
