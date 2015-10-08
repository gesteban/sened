package es.unizar.sened.ontology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.skos.AddAssertion;
import org.semanticweb.skos.SKOSAnnotation;
import org.semanticweb.skos.SKOSChange;
import org.semanticweb.skos.SKOSChangeException;
import org.semanticweb.skos.SKOSConcept;
import org.semanticweb.skos.SKOSCreationException;
import org.semanticweb.skos.SKOSDataFactory;
import org.semanticweb.skos.SKOSDataset;
import org.semanticweb.skos.SKOSEntity;
import org.semanticweb.skos.SKOSEntityAssertion;
import org.semanticweb.skos.SKOSObjectProperty;
import org.semanticweb.skos.SKOSObjectRelationAssertion;
import org.semanticweb.skos.SKOSStorageException;
import org.semanticweb.skosapibinding.SKOSFormatExt;
import org.semanticweb.skosapibinding.SKOSManager;
import org.semanticweb.skosapibinding.SKOSReasoner;
import org.semanticweb.skosapibinding.SKOStoOWLConverter;

import arq.load;
import es.unizar.sened.utils.Log;

// TODO repasar descripciones

/**
 * Vocabulario de categorías
 * <p>
 * Éste objeto contendrá un vocabulario de categorías, que se almacenará en un {@link SKOSDataset} interno. Sólo se
 * puede manejar un vocabulario por cada objeto CategoryVocabulary, por lo que cualquier llamada a {@link #create} o
 * {@link load} sobreescribirá el vocabulario existente anteriormente.
 * <p>
 * El propósito de éste objeto es proveer de un conjunto de categorías sobre las que basar las consultas por keyword en
 * nuestro sistema. Con éste objeto podremos navegar por las categorías sin necesidad de ir continuamente a la DBpedia a
 * consultar qué subcategorías tiene tal categoría; también puede actuar como filtro de especialización cuando no
 * queramos que nuestras consultas se salgan de un determinado ámbito de conocimiento.
 * <p>
 * Para entender el funcionamiento léase los ejemplos de eina.category.example
 * 
 * @author gesteban@unizar.es
 * @deprecated As SPARQL 1.1 includes <a
 *             href="http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#propertypaths">property path</a> syntax we
 *             do not need to store a category vocabulary in memory to improve performance of queries.
 */
public class CategoryVocabulary {

	public static final String TAG = CategoryVocabulary.class.getSimpleName();

	private static final String SKOS_URL = "http://www.w3.org/TR/skos-reference/skos-owl1-dl.rdf";

	private String _vocabularyUri;
	private SKOStoOWLConverter _converter;
	private SKOSManager _skosManager;
	private SKOSDataset _skosDataset;
	private SKOSDataFactory _skosDataFactory;
	private SKOSObjectProperty _narrowerProperty;
	private Set<SKOSCategory> allCategories = new HashSet<SKOSCategory>();
	private SKOSReasoner _skosReasoner;

	public CategoryVocabulary() {
		try {
			_converter = new SKOStoOWLConverter();
			_skosManager = new SKOSManager();
			_skosDataFactory = _skosManager.getSKOSDataFactory();
			_narrowerProperty = _skosDataFactory.getSKOSNarrowerProperty();
			_skosReasoner = new SKOSReasoner(_skosManager, OntologyManager.getInstance().getReasoner());
		} catch (SKOSCreationException ex) {
			Log.e(TAG, "<CategoryVocabulary> SKOSCreationException");
		}
	}

	public String getURI() {
		return _vocabularyUri;
	}

	/**
	 * Crea un vocabulario nuevo con nombre dado.
	 * <p>
	 * Éste método sobreescribe (en memoria, no en disco), cualquier dataset anteriormente almacenado por éste objeto.
	 * 
	 * @param vocabularyUri
	 */
	public void create(String vocabularyUri) {
		try {
			_vocabularyUri = vocabularyUri;
			_skosDataset = _skosManager.createSKOSDataset(java.net.URI.create(vocabularyUri));
			// Se añade el import de la ontología SKOS
			OWLImportsDeclaration importDeclaration = _skosManager.getOWLManger().getOWLDataFactory()
					.getOWLImportsDeclaration(IRI.create(SKOS_URL));
			AddImport addImport = new AddImport(_converter.getAsOWLOntology(_skosDataset), importDeclaration);
			_skosManager.getOWLManger().applyChange(addImport);
		} catch (SKOSCreationException ex) {
			Log.e(TAG, "<create> SKOSCreationException");
		}
	}

	public void save(java.net.URI localURI) {
		try {
			_skosManager.save(_skosDataset, SKOSFormatExt.RDFXML, localURI);
			Log.i(TAG, "<save> Vocabulario guardado en: " + localURI.getPath());
		} catch (SKOSStorageException ex) {
			Log.e(TAG, "<save> SKOSStorageException");
		}
	}

	/**
	 * Carga un vocabulario de categorías desde la URI especificada.<br>
	 * Cualquier vocabulario existente en este objeto antes de la llamada a este método se perderá.
	 * 
	 * @param localURI
	 */
	public boolean load(java.net.URI localURI) {
		try {
			this._skosDataset = _skosManager.loadDataset(localURI);
			this._vocabularyUri = _skosDataset.getURI().toString();
			for (SKOSConcept concept : _skosDataset.getSKOSConcepts()) {
				this.allCategories.add(new SKOSCategory(concept.getURI().toString()));
			}
			Log.i(TAG, "<load> Vocabulario cargado");
			return true;
		} catch (SKOSCreationException ex) {
			Log.e(TAG, "<load> SKOSCreationException");
			return false;
		}
	}

	public Set<SKOSCategory> getNarrowerCategories(SKOSCategory cat) {
		Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
		for (SKOSEntity ent : _skosReasoner.getSKOSNarrowerConcepts(_skosDataFactory.getSKOSConcept(java.net.URI
				.create(cat.getURI())))) {
			categorySet.add(new SKOSCategory(ent.getURI().toString()));
		}
		return categorySet;
	}

	public Set<SKOSCategory> getBroaderCategories(SKOSCategory cat) {
		Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
		for (SKOSEntity ent : _skosReasoner.getSKOSBroaderConcepts(_skosDataFactory.getSKOSConcept(java.net.URI
				.create(cat.getURI())))) {
			categorySet.add(new SKOSCategory(ent.getURI().toString()));
		}
		return categorySet;
	}

	public Set<SKOSCategory> getNarrowerTransitiveCategories(SKOSCategory cat) {
		Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
		for (SKOSEntity ent : _skosReasoner.getSKOSNarrowerTransitiveConcepts(_skosDataFactory
				.getSKOSConcept(java.net.URI.create(cat.getURI())))) {
			categorySet.add(new SKOSCategory(ent.getURI().toString()));
		}
		return categorySet;
	}

	public Set<SKOSCategory> getBroaderTransitiveCategories(SKOSCategory cat) {
		Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
		for (SKOSEntity ent : _skosReasoner.getSKOSBroaderTransitiveConcepts(_skosDataFactory
				.getSKOSConcept(java.net.URI.create(cat.getURI())))) {
			categorySet.add(new SKOSCategory(ent.getURI().toString()));
		}
		return categorySet;
	}

	/**
	 * Añade la categoría y subcategorías de esta (recursivamente) al vocabulario.
	 * 
	 * @param cat
	 *            Categoría
	 * @param root
	 *            Indica si la categoría a añadir se trata de una categoría raíz
	 */
	public void addCategory(SKOSCategory cat, boolean root) {
		allCategories.add(cat);
		try {
			List<SKOSChange> addAssertions = new ArrayList<SKOSChange>();
			SKOSConcept concept = _skosDataFactory.getSKOSConcept(java.net.URI.create(cat.getURI()));
			SKOSEntityAssertion entityAssertion = _skosDataFactory.getSKOSEntityAssertion(concept);
			addAssertions.add(new AddAssertion(_skosDataset, entityAssertion));

			if (root) { // Añadimos la marca de root si es raíz de categorías.
				SKOSAnnotation anon = _skosDataFactory.getSKOSAnnotation(
						IRI.create("http://www.w3.org/2000/01/rdf-schema#comment").toURI(),
						_skosDataFactory.getSKOSUntypedConstant("rootCategory", "en"));
				addAssertions.add(new AddAssertion(_skosDataset, _skosDataFactory.getSKOSAnnotationAssertion(concept,
						anon)));
			}
			for (SKOSCategory subcat : cat.getSubCategories()) {
				addCategory(subcat, false);
				SKOSConcept conceptSon = _skosDataFactory.getSKOSConcept(java.net.URI.create(subcat.getURI()));
				SKOSObjectRelationAssertion propertyAssertion = _skosDataFactory.getSKOSObjectRelationAssertion(
						concept, _narrowerProperty, conceptSon);
				addAssertions.add(new AddAssertion(_skosDataset, propertyAssertion));
			}
			_skosManager.applyChanges(addAssertions);
		} catch (SKOSChangeException ex) {
			Log.e(TAG, "<addCategory> SKOSChangeException");
		}
	}

	public Set<SKOSCategory> getRootCategories() {
		Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
		for (SKOSConcept con : _skosReasoner.getSKOSConcepts()) {
			for (SKOSAnnotation annontation : con.getSKOSAnnotations(_skosDataset)) {
				if (annontation.getAnnotationValueAsConstant().getLiteral().equals("rootCategory")) {
					categorySet.add(new SKOSCategory(con.getURI().toString()));
				}
			}
		}
		return categorySet;
	}

	public Set<SKOSCategory> getAllCategories() {
		return allCategories;
	}

}
