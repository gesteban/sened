package es.unizar.sened.model;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.PropAndDir;

/**
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

	static {
		_ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		InputStream in = DomainOntology.class.getResourceAsStream(PATH);
		_ontology.read(in, BASE_URI);
		Void = _ontology.getOntClass(BASE_URI + "Void");
		taxonomyDefinedBy = _ontology.getOntProperty(BASE_URI
				+ "taxonomyDefinedBy");
		queryLanguage = _ontology.getOntProperty(BASE_URI + "queryLanguage");
		kwdSearchable = _ontology.getOntProperty(BASE_URI + "kwdSearchable");
		dataRetrievable = _ontology
				.getOntProperty(BASE_URI + "dataRetrievable");
		// TODO retrieve _taxonomyType from _ontology properly
		_taxonomyType = TAXONOMY_CATEGORIES;
	}

	public static org.apache.jena.rdf.model.Resource createResource(String uri) {
		return ResourceFactory.createResource(uri);
	}

	public static OntClass createOntClass(String uri) {
		return _ontology.getOntClass(uri);
	}

	public static Set<OntClass> getClasses() {
		return _ontology.listClasses().toSet();
	}

	public static Set<PropAndDir> getRetrievableProperties(OntClass aClass) {
		Set<PropAndDir> propSet = new HashSet<PropAndDir>();
		for (OntProperty prop : DomainOntology._ontology.listAllOntProperties()
				.toSet()) {
			if (prop.hasProperty(dataRetrievable)) {
				if (prop.getDomain() != null && prop.getDomain().equals(aClass)) {
					propSet.add(new PropAndDir(prop, true));
				} else if (prop.getRange() != null
						&& prop.getRange().equals(aClass)) {
					propSet.add(new PropAndDir(prop, false));
				}
			}
		}
		return propSet;
	}

	public static Set<OntProperty> getAllRetrievableProperties() {
		Set<OntProperty> propSet = new HashSet<OntProperty>();
		for (OntProperty prop : DomainOntology._ontology.listAllOntProperties()
				.toSet()) {
			if (prop.hasProperty(dataRetrievable)) {
				propSet.add(prop);
			}
		}
		return propSet;
	}

	public static String getQueryLanguage(OntProperty property) {
		return property.hasProperty(queryLanguage) ? property
				.getPropertyValue(queryLanguage).asLiteral().getLexicalForm()
				: null;
	}

	public static boolean isKeywordSearchable(OntProperty property) {
		return property.hasProperty(kwdSearchable);
	}

	public static String getTaxonomyType() {
		return _taxonomyType;
	}

	public static void main(String[] args) {

		Log.i(TAG, DomainOntology.getQueryLanguage(_ontology
				.getOntProperty("http://www.w3.org/2000/01/rdf-schema#label")));

	}

}
