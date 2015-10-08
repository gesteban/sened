package es.unizar.sened.ontology;

import java.lang.Class;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.PropAndDir;

/**
 * @author gesteban@unizar.es
 */
public class OntologyManager {

	public static final String TAG = OntologyManager.class.getSimpleName();

	public static final String ONTOLOGY_RESOURCE_PATH = "/es/unizar/sened/ontology/domain-ontology.rdf";

	public static final String TAXONOMY_CLASSES = "classes";
	public static final String TAXONOMY_CATEGORIES = "categories";

	private OWLOntologyManager _manager;
	private OWLOntology _ontology;
	private OWLReasoner _reasoner;
	private String _taxonomyType;

	// Start of singleton.

	private OntologyManager() {
		_manager = OWLManager.createOWLOntologyManager();
		try {
			_ontology = _manager.loadOntology(IRI.create(getClass().getResource(ONTOLOGY_RESOURCE_PATH)));
			for (OWLAnnotation anon : _ontology.getAnnotations()) {
				if (anon.getProperty().getIRI().toString().equals(DomainOntology.taxonomyDefinedBy)) {
					_taxonomyType = anon.getValue().toString().replace("\"", "");
					break;
				}
			}
			_reasoner = new StructuralReasonerFactory().createReasoner(_ontology);
		} catch (OWLOntologyCreationException ex) {
			Log.e(TAG, "Error while loading the domain ontology");
			_taxonomyType = null;
		}
	}

	private static class SingletonHolder {
		public static final OntologyManager instance = new OntologyManager();
	}

	public static OntologyManager getInstance() {
		return SingletonHolder.instance;
	}

	// End of singleton.

	public IRI getIRI() {
		return _ontology.getOntologyID().getOntologyIRI().get();
	}

	public String getTaxonomyType() {
		return _taxonomyType;
	}

	public OWLReasoner getReasoner() {
		return _reasoner;
	}

	public OWLDataFactory getOWLDataFactory() {
		return _manager.getOWLDataFactory();
	}

	public Set<OWLDataProperty> getDataPropertiesByDomain(OWLClass domain) {
		Set<OWLDataProperty> propSet = new HashSet<OWLDataProperty>();
		Set<OWLDataProperty> objProps = _ontology.getDataPropertiesInSignature();
		for (OWLDataProperty prop : objProps) {
			for (OWLClass owlClass : _reasoner.getDataPropertyDomains(prop, true).getFlattened()) {
				if (owlClass.equals(domain)) {
					propSet.add(prop);
				}
			}
		}
		return propSet;
	}

	public Set<OWLDataProperty> getDataPropertiesByDomainExtended(OWLClass domain) {
		Log.d(TAG, "<getDataPropertiesByDomainExtended> Buscando DataProperties con dominio "
				+ domain.getIRI().toString());
		Set<OWLDataProperty> propSet = new HashSet<OWLDataProperty>();
		for (OWLDataProperty prop : _ontology.getDataPropertiesInSignature()) {
			if (isDomainOfDataProperty(domain, prop)) {
				propSet.add(prop);
			}
		}
		return propSet;
	}

	public boolean isDomainOfDataProperty(OWLClass clase, OWLDataProperty prop) {

		for (OWLClass owlClass1 : _reasoner.getDataPropertyDomains(prop, false).getFlattened()) {
			if (owlClass1.getIRI().equals(clase.getIRI())) {
				Log.d(TAG, "<idDomainOfDataProperty> " + clase.getIRI().getRemainder().get() + " dominio de "
						+ prop.getIRI().getRemainder().get() + "? SI!");
				return true;
			}
		}
		Log.d(TAG, "<isDomainOfDataProperty> " + clase.getIRI().getRemainder().get() + " dominio de "
				+ prop.getIRI().getRemainder().get() + "? NO");
		return false;
	}

	public boolean isDomainOrRangeOfObjectProperty(OWLClass clase, OWLObjectProperty prop) {

		for (OWLClass owlClass1 : _reasoner.getObjectPropertyDomains(prop, false).getFlattened()) {
			if (owlClass1.getIRI().equals(clase.getIRI())) {
				Log.d(TAG, "<isDomainOrRangeOfObjectProperty> " + clase.getIRI().getRemainder().get()
						+ " dominio o rango de " + prop.getIRI().getRemainder().get() + "? SI!");
				return true;
			}
		}
		for (OWLClass owlClass1 : _reasoner.getObjectPropertyRanges(prop, false).getFlattened()) {
			if (owlClass1.getIRI().equals(clase.getIRI())) {
				Log.d(TAG, "<isDomainOrRangeOfObjectProperty> " + clase.getIRI().getRemainder().get()
						+ " dominio o rango de " + prop.getIRI().getRemainder().get() + "? SI!");
				return true;
			}
		}
		Log.d(TAG, "<isDomainOrRangeOfObjectProperty> " + clase.getIRI().getRemainder().get() + " dominio o rango de "
				+ prop.getIRI().getRemainder().get() + "? NO");
		return false;
	}

	public Set<OWLObjectProperty> getObjectPropertiesByDomain(OWLClass domain) {
		Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
		Set<OWLObjectProperty> objProps = _ontology.getObjectPropertiesInSignature();
		for (OWLObjectProperty prop : objProps) {
			for (OWLClass owlClass : _reasoner.getObjectPropertyDomains(prop, true).getFlattened()) {
				if (owlClass.equals(domain)) {
					propSet.add(prop);
				}
			}
		}
		return propSet;
	}

	public Set<OWLObjectProperty> getObjectPropertiesByRange(OWLClass domain) {
		Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
		Set<OWLObjectProperty> objProps = _ontology.getObjectPropertiesInSignature();
		for (OWLObjectProperty prop : objProps) {
			for (OWLClass owlClass : _reasoner.getObjectPropertyRanges(prop, true).getFlattened()) {
				if (owlClass.equals(domain)) {
					propSet.add(prop);
				}
			}
		}
		return propSet;
	}

	public Set<OWLObjectProperty> getObjectPropertiesByDomainOrRange(OWLClass domainOrRange) {
		Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
		Set<OWLObjectProperty> objProps = _ontology.getObjectPropertiesInSignature();
		for (OWLObjectProperty prop : objProps) {
			for (OWLClass owlClass : _reasoner.getObjectPropertyRanges(prop, true).getFlattened()) {
				if (owlClass.equals(domainOrRange)) {
					propSet.add(prop);
				}
			}
			for (OWLClass owlClass : _reasoner.getObjectPropertyDomains(prop, true).getFlattened()) {
				if (owlClass.equals(domainOrRange)) {
					propSet.add(prop);
				}
			}
		}
		return propSet;
	}

	public Set<OWLObjectProperty> getObjectPropertiesByDomainOrRangeExtended(OWLClass domainOrRange) {
		Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
		for (OWLObjectProperty prop : _ontology.getObjectPropertiesInSignature()) {
			if (isDomainOrRangeOfObjectProperty(domainOrRange, prop)) {
				propSet.add(prop);
			}
		}
		return propSet;
	}

	public Set<OWLObjectProperty> getObjectPropertiesByDomainAndRange(OWLClass domain, OWLClass range) {
		Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
		for (OWLObjectProperty prop : _ontology.getObjectPropertiesInSignature()) {
			for (OWLClass owlClass : _reasoner.getObjectPropertyDomains(prop, true).getFlattened()) {
				if (owlClass.equals(domain)) {
					for (OWLClass owlClass2 : _reasoner.getObjectPropertyRanges(prop, true).getFlattened()) {
						if (owlClass2.equals(range)) {
							propSet.add(prop);
						}
					}
				}
			}
		}
		return propSet;
	}

	public Set<OWLClass> getClasses() {
		Set<OWLClass> classSet = new HashSet<OWLClass>();
		for (OWLClass clase : _ontology.getClassesInSignature()) {
			classSet.add(clase);
		}
		return classSet;
	}

	public Set<OWLClass> getClassEquivalents(OWLClass clase) {
		Set<OWLClass> classSet = new HashSet<OWLClass>();
		for (OWLEquivalentClassesAxiom eca : _ontology.getEquivalentClassesAxioms(clase)) {
			for (OWLClassExpression ce : eca.getClassesInSignature()) {
				if (!ce.equals(clase))
					classSet.add(ce.asOWLClass());
			}
		}
		return classSet;
	}

	public Set<OWLObjectProperty> getObjectPropertyEquivalents(OWLObjectProperty prop) {
		Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
		for (OWLEquivalentObjectPropertiesAxiom eopa : _ontology.getEquivalentObjectPropertiesAxioms(prop)) {
			for (OWLObjectPropertyExpression ope : eopa.getObjectPropertiesInSignature()) {
				if (!ope.equals(prop))
					propSet.add(ope.asOWLObjectProperty());
			}
		}
		return propSet;
	}

	public Set<OWLClass> getObjectPropertyRange(Set<OWLObjectProperty> propSet) {
		Set<OWLClass> classSet = new HashSet<OWLClass>();
		for (OWLObjectProperty prop : propSet) {
			for (OWLClass clase : _reasoner.getObjectPropertyRanges(prop, true).getFlattened()) {
				classSet.add(clase);
			}
		}
		return classSet;
	}

	public Set<OWLClass> getObjectPropertyDomain(Set<OWLObjectProperty> propSet) {
		Set<OWLClass> classSet = new HashSet<OWLClass>();
		for (OWLObjectProperty prop : propSet) {
			for (OWLClass clase : _reasoner.getObjectPropertyDomains(prop, true).getFlattened()) {
				classSet.add(clase);
			}
		}
		return classSet;
	}

	public Set<OWLClass> getObjectPropertyDomain(OWLObjectProperty prop) {
		Set<OWLClass> classSet = new HashSet<OWLClass>();
		for (OWLClass clase : _reasoner.getObjectPropertyDomains(prop, true).getFlattened()) {
			classSet.add(clase);
		}
		return classSet;
	}

	public Set<OWLClass> getObjectPropertyRange(OWLObjectProperty prop) {
		Set<OWLClass> classSet = new HashSet<OWLClass>();
		for (OWLClass clase : _reasoner.getObjectPropertyRanges(prop, true).getFlattened()) {
			classSet.add(clase);
		}
		return classSet;
	}

	@Deprecated
	public Set<OWLEntity> getSubsetInternal(Set<? extends OWLEntity> entitySet) {
		Set<OWLEntity> outSet = new HashSet<OWLEntity>();
		for (OWLEntity ent : entitySet) {
			if (isInternal(ent)) {
				outSet.add(ent);
			}
		}
		return outSet;
	}

	@Deprecated
	public <T extends OWLEntity> Set<T> getSubsetInternal(Class<T> clase, Set<T> entitySet) {
		Set<T> outSet = new HashSet<T>();
		for (T ent : entitySet) {
			if (isInternal(ent)) {
				outSet.add(ent);
			}
		}
		return outSet;
	}

	@Deprecated
	public Set<OWLEntity> getSubsetExternal(Set<? extends OWLEntity> entitySet) {
		Set<OWLEntity> outSet = new HashSet<OWLEntity>();
		for (OWLEntity ent : entitySet) {
			if (!isInternal(ent)) {
				outSet.add(ent);
			}
		}
		return outSet;
	}

	@Deprecated
	public <T extends OWLEntity> Set<T> getSubsetExternal(Class<T> clase, Set<T> entitySet) {
		Set<T> outSet = new HashSet<T>();
		for (T ent : entitySet) {
			if (!isInternal(ent)) {
				outSet.add(ent);
			}
		}
		return outSet;
	}

	@Deprecated
	protected boolean isInternal(OWLEntity ent) {
		return ent.getIRI().toURI().getPath()
				.equals(_ontology.getOntologyID().getOntologyIRI().get().toURI().getPath());
	}

	public String getAnnotationQueryLanguage(OWLDataProperty dataProp) {
		/*
		 * for (OWLDataProperty dp : owlReasoner.getEquivalentDataProperties(dataProp)) { for (OWLAnnotation a :
		 * dp.getAnnotations(ont)) { if
		 * (a.getProperty().getIRI().getFragment().toString().equals(IRI_QUERYLANGUAGE_ANNOTATION_FRAGMENT)) { return
		 * ((OWLLiteral) a.getValue()).getLiteral(); } } }
		 */
		return null;
	}

	public Set<OWLObjectProperty> getObjectPropertyEquivalents2(OWLObjectProperty prop) {
		/*
		 * Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>(); for (OWLObjectPropertyExpression ope :
		 * prop.getEquivalentProperties(ont)) { propSet.add(ope.asOWLObjectProperty()); } return propSet;
		 */
		return null;
	}

	// TODO necessary method??
	public String getDataRetriavablePropertyFragments() {
		for (OWLDataProperty prop : _ontology.getDataPropertiesInSignature()) {
			if (isKeywordSearchable(prop)) {
				return prop.getIRI().getRemainder().get();
			}
		}
		return null;
	}

	public <T extends OWLEntity> Set<T> getRetrievableProperties(Class<T> clase, Set<T> entitySet) {
		Set<T> outSet = new HashSet<T>();
		for (T ent : entitySet) {
			if (isDataRetrievable((OWLProperty) ent)) {
				outSet.add(ent);
			}
		}
		return outSet;

	}

	public boolean isKeywordSearchable(OWLProperty prop) {
		for (OWLAnnotationAssertionAxiom aaa : _ontology.getAnnotationAssertionAxioms(prop.getIRI())) {
			if (aaa.getProperty().getIRI().toString().equals(DomainOntology.kwdSearchable)) {
				return true;
			}
		}
		return false;
	}

	public boolean isDataRetrievable(OWLProperty prop) {
		for (OWLAnnotationAssertionAxiom aaa : _ontology.getAnnotationAssertionAxioms(prop.getIRI())) {
			if (aaa.getProperty().getIRI().toString().equals(DomainOntology.dataRetrievable)) {
				return true;
			}
		}
		return false;
	}

	public Set<PropAndDir> getPropertiesToRetrieve(OWLClass tipo) {
		Set<PropAndDir> propSet = new HashSet<PropAndDir>();
		for (OWLDataProperty prop : getRetrievableProperties(OWLDataProperty.class,
				getDataPropertiesByDomainExtended(tipo))) {
			propSet.add(new PropAndDir(prop, true));
		}
		for (OWLObjectProperty prop : getRetrievableProperties(OWLObjectProperty.class,
				getObjectPropertiesByDomainOrRangeExtended(tipo))) {
			propSet.add(new PropAndDir(prop, false));
		}
		return propSet;
	}

	public static void main(String[] args) {

		OntologyManager om = OntologyManager.getInstance();

		OWLClass aClass = new OWLDataFactoryImpl().getOWLClass(IRI.create(DomainOntology.Articulo));
		for (PropAndDir propAndDir : om.getPropertiesToRetrieve(aClass)) {
			Log.i(TAG, propAndDir.toString());
		}

		/* FUNCIONA
		 * OWLObjectProperty prop = new OWLDataFactoryImpl().getOWLObjectProperty(IRI.create(DomainOntology.label));
		 * Log.i(TAG, prop.toString()); Log.i(TAG, om.isDataRetrievable(prop) ? "yes" : "no"); Log.i(TAG, "=="); prop =
		 * new OWLDataFactoryImpl().getOWLObjectProperty(IRI.create(DomainOntology.dataRetrievable)); Log.i(TAG,
		 * prop.toString()); Log.i(TAG, om.isDataRetrievable(prop) ? "yes" : "no"); Log.i(TAG, "=="); prop = new
		 * OWLDataFactoryImpl().getOWLObjectProperty(IRI.create(DomainOntology.abstractt)); Log.i(TAG, prop.toString());
		 * Log.i(TAG, om.isDataRetrievable(prop) ? "yes" : "no");
		 */

	}
}
