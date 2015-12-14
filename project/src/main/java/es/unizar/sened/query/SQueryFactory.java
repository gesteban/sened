package es.unizar.sened.query;

import java.util.Set;

import es.unizar.sened.model.DomainOntology;
import es.unizar.sened.utils.PropAndDir;

/**
 * @author gesteban@unizar.es
 */
public class SQueryFactory {

	public static final String TAG = SQueryFactory.class.getSimpleName();

	private static final String PREFIX_ALL = "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n";
	private static final String QF_SUBCATEGORIES = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
			+ "SELECT ?subcat \n"
			+ "WHERE { \n"
			+ "  ?subcat skos:broader <#categoryURI> \n}";
	private static final String QF_TYPE = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "SELECT DISTINCT ?type WHERE { <#uri> rdf:type ?type }";

	private SQueryFactoryConfig config;

	public SQueryFactory(SQueryFactoryConfig config) {
		this.config = config;
	}

	public SQueryFactory() {
		this.config = new SQueryFactoryConfig();
	}

	public SQuery getKeywordQuery_CategoryTaxonomy(String keyword,
			String categoryUri, int categoryDeep) {

		Set<PropAndDir> retrievableProps = DomainOntology
				.getRetrievableProperties(DomainOntology.Void);
		String queryString = PREFIX_ALL + "SELECT DISTINCT ?uri";
		for (PropAndDir propAndDir : retrievableProps) {
			queryString += " ?" + propAndDir.prop.getLocalName();
		}
		queryString += " {\n";
		queryString += "  {\n";
		for (int i = 0; i < categoryDeep; i++) {
			if (i != 0) {
				queryString += "  UNION \n";
			}
			queryString += "  { ?uri dcterms:subject";
			for (int j = 0; j < i; j++) {
				queryString += "/skos:broader";
			}
			queryString += " <" + categoryUri + "> } \n";
		}
		queryString += "  }\n";
		int count = 0;
		for (PropAndDir propAndDir : retrievableProps) {
			queryString += count != 0 ? "." : " ";
			if (propAndDir.straight) {
				queryString += " ?uri <" + propAndDir.prop.getURI() + "> ?"
						+ propAndDir.prop.getLocalName() + "\n";
			} else {
				queryString += " ?" + propAndDir.prop.getLocalName() + " <"
						+ propAndDir.prop.getURI() + "> ?uri " + "\n";
			}
			String lang = DomainOntology.getQueryLanguage(propAndDir.prop);
			if (lang != null) {
				queryString += "  FILTER langMatches(lang(?"
						+ propAndDir.prop.getLocalName() + "),\"" + lang
						+ "\") \n";
			}
			count++;
		}

		for (PropAndDir propAndDir : retrievableProps) {
			if (DomainOntology.isKeywordSearchable(propAndDir.prop)) {
				queryString += "  FILTER regex(?"
						+ propAndDir.prop.getLocalName()
						+ ",\"[.., )(\\\\-\\\"]" + keyword
						+ "[.., )(\\\\-\\\"]\",\"i\")\n";
			}
		}
		queryString += "}";
		return new SQuery(queryString, config);
	}

	public SQuery getSubCategoryQuery(String categoryURI) {
		String queryString = QF_SUBCATEGORIES;
		queryString = queryString.replace("#categoryURI", categoryURI);
		return new SQuery(queryString, config);
	}

	public SQuery getTypeQuery(String URI) {
		String queryString = QF_TYPE;
		queryString = queryString.replace("#uri", URI);
		return new SQuery(queryString, config);
	}

	public SQuery getCustomQuery(String query) {
		return new SQuery(query, config);
	}

	public SQuery getDescribeQuery(String URI) {
		return new SQuery("DESCRIBE <" + URI + ">", config);
	}

	public static void main(String[] args) {

	}
}
