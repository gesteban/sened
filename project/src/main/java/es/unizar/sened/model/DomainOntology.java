package es.unizar.sened.model;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

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

  public static final String TAXONOMY_CATEGORIES = "TAXONOMY_CATEGORIES";
  public static final String TAXONOMY_CLASSES = "TAXONOMY_CLASSES";

  protected static final OntModel _ontology;
  protected static final String _taxonomyType;

  static {
    _ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
    InputStream in = DomainOntology.class.getResourceAsStream(PATH);
    _ontology.read(in, BASE_URI);
    Void = _ontology.getOntClass(BASE_URI + "Void");
    taxonomyDefinedBy = _ontology.getOntProperty(BASE_URI + "taxonomyDefinedBy");
    queryLanguage = _ontology.getOntProperty(BASE_URI + "queryLanguage");
    kwdSearchable = _ontology.getOntProperty(BASE_URI + "kwdSearchable");
    // TODO retrieve _taxonomyType from _ontology properly
    _taxonomyType = TAXONOMY_CATEGORIES;
  }

  public static Set<OntClass> getClasses() {
    return _ontology.listClasses().toSet();
  }

  public static Set<OntProperty> getProperties() {
    return _ontology.listAllOntProperties().toSet();
  }
  
  public static Set<ObjectProperty> getObjectProperties() {
    return _ontology.listObjectProperties().toSet();
  }

  public static Set<PropAndDir> getProperties(OntClass aClass) {
    if (!getClasses().contains(aClass))
      return Collections.emptySet();
    Set<PropAndDir> propSet = new HashSet<>();
    for (OntProperty prop : getProperties()) {
      for (ExtendedIterator<? extends OntResource> iter = prop.listDomain(); iter.hasNext();) {
        if (iter.next().equals(aClass)) {
          propSet.add(new PropAndDir(prop, true));
        }
      }
      for (ExtendedIterator<? extends OntResource> iter = prop.listRange(); iter.hasNext();) {
        if (iter.next().equals(aClass)) {
          propSet.add(new PropAndDir(prop, false));
        }
      }
    }
    return propSet;
  }

  public static Set<PropAndDir> getObjectProperties(OntClass aClass) {
    if (!getClasses().contains(aClass))
      return Collections.emptySet();
    Set<PropAndDir> propSet = new HashSet<>();
    for (OntProperty prop : getObjectProperties()) {
      for (ExtendedIterator<? extends OntResource> iter = prop.listDomain(); iter.hasNext();) {
        if (iter.next().equals(aClass)) {
          propSet.add(new PropAndDir(prop, true));
        }
      }
      for (ExtendedIterator<? extends OntResource> iter = prop.listRange(); iter.hasNext();) {
        if (iter.next().equals(aClass)) {
          propSet.add(new PropAndDir(prop, false));
        }
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
