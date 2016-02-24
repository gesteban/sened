package es.unizar.sened.query;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

import es.unizar.sened.model.DomainOntology;
import es.unizar.sened.model.PropAndDir;

/**
 * @author gesteban@unizar.es
 */
public class SQueryFactory {

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

  public SQuery getKeywordQuery_CategoryTaxonomy_v2(String keyword, String categoryUri, int categoryDeep) {
    // TODO stop using getLocalName to name variables (some variables could get the same name)
    StringBuilder sb = new StringBuilder();
    // get keyword searchable properties
    Set<OntProperty> keywordSearchableProps = new HashSet<>();
    for (OntProperty property : DomainOntology.getProperties())
      if (DomainOntology.isKeywordSearchable(property))
        keywordSearchableProps.add(property);
    // build sparql select clause
    sb.append(PREFIX_ALL + "SELECT DISTINCT ?uri");
    // [complete lines version] uncomment to retrieve keyword searchable properties
    // for (OntProperty property : keywordSearchableProps)
    // sb.append(" ?" + property.getLocalName());
    sb.append(" WHERE {\n");
    // build category taxonomy of sparql where clause
    sb.append("  {\n");
    for (int i = 0; i < categoryDeep; i++) {
      if (i != 0)
        sb.append("    UNION ");
      else
        sb.append("    ");
      sb.append("{ ?uri dcterms:subject");
      for (int j = 0; j < i; j++)
        sb.append("/skos:broader");
      sb.append(" <" + categoryUri + "> }\n");
    }
    sb.append("  }\n");
    // [complete lines version] uncomment to build general property match
    // for (OntProperty property : keywordSearchableProps) {
    // sb.append("    ?uri <" + property.getURI() + "> ?" + property.getLocalName() + " .\n");
    // String lang = DomainOntology.getQueryLanguage(property);
    // if (lang != null)
    // sb.append("    FILTER langMatches(lang(?" + property.getLocalName() + "),\"" + lang + "\") .\n");
    // }
    // build filter match of sparql where clause
    for (OntProperty property : keywordSearchableProps) {
      sb.append("  OPTIONAL {\n");
      sb.append("    ?uri <" + property.getURI() + "> ?" + property.getLocalName() + "_search\n");
      String lang = DomainOntology.getQueryLanguage(property);
      if (lang != null)
        sb.append("    FILTER langMatches(lang(?" + property.getLocalName() + "_search),\"" + lang + "\")\n");
      sb.append("    FILTER regex(?" + property.getLocalName() + "_search,\"[.., )(\\\\-\\\"]" + keyword
          + "[.., )(\\\\-\\\"]\",\"i\") }\n");
    }
    // state we need at least one bounded keyword searchable property
    int i = 0;
    sb.append("  FILTER(");
    for (OntProperty property : keywordSearchableProps) {
      if (i++ > 0)
        sb.append(" || ");
      sb.append("bound(?" + property.getLocalName() + "_search)");
    }
    sb.append(")\n");
    // end where clause
    sb.append("}");
    // return query
    if (_endpoint != null)
      return new SQuery(sb.toString(), _endpoint);
    else if (_model != null)
      return new SQuery(sb.toString(), _model);
    else
      // _dataset != null
      return new SQuery(sb.toString(), _dataset);
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

  public SQuery getDirectDistanceQuery(String resourceOne, String resourceTwo) {
    String queryString = QUERY_DIRECTDISTANCE;
    queryString = queryString.replaceAll("#resourceOne", resourceOne);
    queryString = queryString.replaceAll("#resourceTwo", resourceTwo);
    if (_endpoint != null)
      return new SQuery(queryString, _endpoint);
    else if (_model != null)
      return new SQuery(queryString, _model);
    else
      // _dataset != null
      return new SQuery(queryString, _dataset);
  }

  private static final String PREFIX_ALL = "\nPREFIX dcterms: <http://purl.org/dc/terms/>\n"
      + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
  private static final String QUERY_SUBCATEGORIES = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
      + "SELECT ?subcat  WHERE { ?subcat skos:broader <#categoryURI> }";
  private static final String QUERY_TYPE = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
      + "SELECT DISTINCT ?type WHERE { <#uri> rdf:type ?type }";
  private static final String QUERY_MANUALDESCRIBE = "construct { ?x ?y ?z } where { "
      + "{ <#uri> ?y ?z BIND(<#uri> AS ?x) } UNION { ?x ?y <#uri> BIND(<#uri> AS ?z) } }";
  private static final String QUERY_DIRECTDISTANCE = "select (count(distinct ?x) as ?count) where { { <#resourceOne> ?x <#resourceTwo> } "
      + " UNION { <#resourceTwo> ?x <#resourceOne> } }";

  private String _endpoint;
  private Model _model;
  private Dataset _dataset;

}
