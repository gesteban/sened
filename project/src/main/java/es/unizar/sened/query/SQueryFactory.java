package es.unizar.sened.query;

import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

import es.unizar.sened.model.DomainOntology;
import es.unizar.sened.model.PropAndDir;

/**
 * @author gesteban@unizar.es
 */
public class SQueryFactory {

  public static final String TAG = SQueryFactory.class.getSimpleName();

  private static final String PREFIX_ALL = "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
      + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n";
  private static final String QUERY_SUBCATEGORIES = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
      + "SELECT ?subcat \n" + "WHERE { \n" + "  ?subcat skos:broader <#categoryURI> \n}";
  private static final String QUERY_TYPE = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
      + "SELECT DISTINCT ?type WHERE { <#uri> rdf:type ?type }";
  private static final String QUERY_MANUALDESCRIBE = "construct { ?x ?y ?z } where { "
      + "{ <#uri> ?y ?z BIND(<#uri> AS ?x) } UNION { ?x ?y <#uri> BIND(<#uri> AS ?z) } }";

  private String _endpoint;
  private Model _model;
  private Dataset _dataset;

  public SQueryFactory(String endpoint) {
    _endpoint = endpoint;
    _model = null;
    _dataset = null;
  }

  public SQueryFactory(Model model) {
    _endpoint = null;
    _model = model;
    _dataset = null;
  }

  public SQueryFactory(Dataset dataset) {
    _endpoint = null;
    _model = null;
    _dataset = dataset;
  }

  public SQuery getKeywordQuery_CategoryTaxonomy(String keyword, String categoryUri, int categoryDeep) {

    Set<PropAndDir> retrievableProps = DomainOntology.getProperties(DomainOntology.Void);
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
        queryString += " ?uri <" + propAndDir.prop.getURI() + "> ?" + propAndDir.prop.getLocalName() + "\n";
      } else {
        queryString += " ?" + propAndDir.prop.getLocalName() + " <" + propAndDir.prop.getURI() + "> ?uri " + "\n";
      }
      String lang = DomainOntology.getQueryLanguage(propAndDir.prop);
      if (lang != null) {
        queryString += "  FILTER langMatches(lang(?" + propAndDir.prop.getLocalName() + "),\"" + lang + "\") \n";
      }
      count++;
    }

    for (PropAndDir propAndDir : retrievableProps) {
      if (DomainOntology.isKeywordSearchable(propAndDir.prop)) {
        queryString += "  FILTER regex(?" + propAndDir.prop.getLocalName() + ",\"[.., )(\\\\-\\\"]" + keyword
            + "[.., )(\\\\-\\\"]\",\"i\")\n";
      }
    }
    queryString += "}";
    if (_endpoint != null)
      return new SQuery(queryString, _endpoint);
    else if (_model != null)
      return new SQuery(queryString, _model);
    else
      // _dataset != null
      return new SQuery(queryString, _dataset);
  }

  public SQuery getSubCategoryQuery(String categoryUri) {
    String queryString = QUERY_SUBCATEGORIES;
    queryString = queryString.replace("#categoryURI", categoryUri);
    if (_endpoint != null)
      return new SQuery(queryString, _endpoint);
    else if (_model != null)
      return new SQuery(queryString, _model);
    else
      // _dataset != null
      return new SQuery(queryString, _dataset);
  }

  public SQuery getTypeQuery(String resourceUri) {
    String queryString = QUERY_TYPE;
    queryString = queryString.replace("#uri", resourceUri);
    if (_endpoint != null)
      return new SQuery(queryString, _endpoint);
    else if (_model != null)
      return new SQuery(queryString, _model);
    else
      // _dataset != null
      return new SQuery(queryString, _dataset);
  }

  public SQuery getCustomQuery(String queryString) {
    if (_endpoint != null)
      return new SQuery(queryString, _endpoint);
    else if (_model != null)
      return new SQuery(queryString, _model);
    else
      // _dataset != null
      return new SQuery(queryString, _dataset);
  }

  public SQuery getDescribeQuery(String resourceUri) {
    if (_endpoint != null)
      return new SQuery("DESCRIBE <" + resourceUri + ">", _endpoint);
    else if (_model != null)
      return new SQuery("DESCRIBE <" + resourceUri + ">", _model);
    else
      // _dataset != null
      return new SQuery("DESCRIBE <" + resourceUri + ">", _dataset);
  }

  public SQuery getManualDescribeQuery(String resourceUri) {
    String queryString = QUERY_MANUALDESCRIBE;
    queryString = queryString.replaceAll("#uri", resourceUri);
    if (_endpoint != null)
      return new SQuery(queryString, _endpoint);
    else if (_model != null)
      return new SQuery(queryString, _model);
    else
      // _dataset != null
      return new SQuery(queryString, _dataset);
  }

}
