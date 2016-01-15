package es.unizar.sened.model;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.BooleanClassDescription;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;

import es.unizar.sened.utils.Log;

/**
 * TODO make this class non-static
 * 
 * @author gesteban@unizar.es
 */
public class DomainOntology {

  private static final String TAG = DomainOntology.class.getSimpleName();

  private static final String PATH = "/es/unizar/sened/model/domain-ontology.rdf";
  private static final String BASE_URI = "http://sened.unizar.es/def#";

  public static final OntClass Void;
  public static final OntProperty taxonomyDefinedBy;
  public static final OntProperty queryLanguage;
  public static final OntProperty kwdSearchable;
  public static final OntProperty dataRetrievable;

  public static final String TAXONOMY_CATEGORIES = "TAXONOMY_CATEGORIES";
  public static final String TAXONOMY_CLASSES = "TAXONOMY_CLASSES";

  protected static final OntModel _ontology;
  protected static final String _taxonomyType;
  protected static OntModel _factory;

  static {
    _ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    InputStream in = DomainOntology.class.getResourceAsStream(PATH);
    _ontology.read(in, BASE_URI);
    Void = _ontology.getOntClass(BASE_URI + "Void");
    taxonomyDefinedBy = _ontology.getOntProperty(BASE_URI + "taxonomyDefinedBy");
    queryLanguage = _ontology.getOntProperty(BASE_URI + "queryLanguage");
    kwdSearchable = _ontology.getOntProperty(BASE_URI + "kwdSearchable");
    dataRetrievable = _ontology.getOntProperty(BASE_URI + "dataRetrievable");
    // TODO retrieve _taxonomyType from _ontology properly
    _taxonomyType = TAXONOMY_CATEGORIES;
  }

  public static Set<OntClass> getClasses() {
    return _ontology.listClasses().toSet();
  }

  public static Set<PropAndDir> getRetrievableProperties(OntClass aClass) {
    if (!getClasses().contains(aClass))
      return Collections.emptySet();
    Log.i(TAG, "get retrievable properties from " + aClass.toString());
    Set<PropAndDir> propSet = new HashSet<>();
    for (OntProperty prop : DomainOntology._ontology.listAllOntProperties().toSet()) {
      if (prop.hasProperty(dataRetrievable)) {
        Log.i(TAG, "prop = " + prop.toString());
        Log.i(TAG, "domain = " + prop.getDomain());
        if(prop.getDomain() instanceof BooleanClassDescription) {
          Log.i(TAG, "ALELUYA hermano");
        } else {
          Log.i(TAG, ((BooleanClassDescription)prop.getDomain()).toString());
        }
        if (prop.getDomain() != null && prop.getDomain().equals(aClass)) {
          propSet.add(new PropAndDir(prop, true));
        } else if (prop.getRange() != null && prop.getRange().equals(aClass)) {
          propSet.add(new PropAndDir(prop, false));
        }
      }
    }
    return propSet;
  }

  public static Set<OntProperty> getAllRetrievableProperties() {
    Set<OntProperty> propSet = new HashSet<OntProperty>();
    for (OntProperty prop : DomainOntology._ontology.listAllOntProperties().toSet()) {
      if (prop.hasProperty(dataRetrievable)) {
        propSet.add(prop);
      }
    }
    return propSet;
  }

  public static String getQueryLanguage(OntProperty property) {
    return property.hasProperty(queryLanguage) ? property.getPropertyValue(queryLanguage).asLiteral().getLexicalForm()
        : null;
  }

  public static boolean isKeywordSearchable(OntProperty property) {
    return property.hasProperty(kwdSearchable);
  }

  public static String getTaxonomyType() {
    return _taxonomyType;
  }

}
