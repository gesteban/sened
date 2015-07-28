package eina.category;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import eina.ontology.beans.SKOSCategory;
import eina.utils.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.skos.*;
import org.semanticweb.skosapibinding.SKOSFormatExt;
import org.semanticweb.skosapibinding.SKOSManager;
import org.semanticweb.skosapibinding.SKOSReasoner;
import org.semanticweb.skosapibinding.SKOStoOWLConverter;

/**
 * Vocabulario de categorías
 * <p>
 * Éste objeto contendrá un vocabulario de categorías, que se almacenará en un
 * {@link SKOSDataset} interno. Sólo se puede manejar un vocabulario por cada objeto
 * CategoryVocabulary, por lo que cualquier llamada a {@link #create} o {@link load}
 * sobreescribirá el vocabulario existente anteriormente.
 * <p>
 * El propósito de éste objeto es proveer de un conjunto de categorías sobre las que
 * basar las consultas por keyword en nuestro sistema. Con éste objeto podremos navegar
 * por las categorías sin necesidad de ir continuamente a la DBpedia a consultar
 * qué subcategorías tiene tal categoría; también puede actuar como filtro de 
 * especialización cuando no queramos que nuestras consultas se salgan de un 
 * determinado ámbito de conocimiento.
 * <p>
 * Para entender el funcionamiento léase los ejemplos de eina.category.example
 * @author Guillermo Esteban
 */
public class CategoryVocabulary {

    private final static String SKOS_URL = "http://www.w3.org/TR/skos-reference/skos-owl1-dl.rdf";
    private String URI;
    private SKOStoOWLConverter conv;
    private SKOSManager skosManager;
    private SKOSDataset skosDataset;
    private SKOSDataFactory sdf;
    private SKOSObjectProperty narrower;
    private Set<SKOSCategory> allCategories = new HashSet<SKOSCategory>();

    public CategoryVocabulary() {
        try {
            this.conv = new SKOStoOWLConverter();
            this.skosManager = new SKOSManager();
            this.sdf = skosManager.getSKOSDataFactory();
            this.narrower = sdf.getSKOSNarrowerProperty();
        } catch (SKOSCreationException ex) {
            Log.getLog().error("<CategoryVocabulary:CategoryVocabulary> SKOSCreationException");
        }
    }

    public String getURI() {
        return this.URI;
    }

    /**
     * Crea un vocabulario nuevo con nombre dado.
     * <p>
     * Éste método sobreescribe (en memoria, no en disco), cualquier dataset anteriormente 
     * almacenado por éste objeto.
     * @param vocabularyURI
     */
    public void create(String vocabularyURI) {
        try {
            this.URI = vocabularyURI;
            this.skosDataset = skosManager.createSKOSDataset(java.net.URI.create(vocabularyURI));
            // Se añade el import de la ontología SKOS
            OWLImportsDeclaration importDeclaration = skosManager.getOWLManger().getOWLDataFactory().getOWLImportsDeclaration(
                    IRI.create(SKOS_URL));
            AddImport addImport = new AddImport(conv.getAsOWLOntology(skosDataset), importDeclaration);
            skosManager.getOWLManger().applyChange(addImport);
        } catch (SKOSCreationException ex) {
            Log.getLog().error("<CategoryVocabulary:create> SKOSCreationException");
        }
    }

    public void save(java.net.URI localURI) {
        try {
            this.skosManager.save(skosDataset, SKOSFormatExt.RDFXML, localURI);
            Log.getLog().info("<CategoryVocabulary:save> Vocabulario guardado en: " + localURI.getPath());
        } catch (SKOSStorageException ex) {
            Log.getLog().error("<CategoryVocabulary:save> SKOSStorageException");
        }
    }

    /**
     * Carga un vocabulario de categorías desde la URI especificada.<br>
     * Cualquier vocabulario existente en éste objeto antes de la llamada a éste 
     * método se perderá.
     * @param localURI 
     */
    public boolean load(java.net.URI localURI) {
        try {
            this.skosDataset = skosManager.loadDataset(localURI);
            this.URI = skosDataset.getURI().toString();
            for(SKOSConcept concept : skosDataset.getSKOSConcepts()) {
                this.allCategories.add(new SKOSCategory(concept.getURI().toString()));
            }
            Log.getLog().info("<CategoryVocabulary:load> Vocabulario cargado");
            return true;
        } catch (SKOSCreationException ex) {
            Log.getLog().error("<CategoryVocabulary:load> SKOSCreationException");
            return false;
        }
    }

    public Set<SKOSCategory> getNarrowerCategories(SKOSCategory cat) {
        Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
        OWLReasoner owlReasoner = new PelletReasoner(conv.getAsOWLOntology(skosDataset), BufferingMode.BUFFERING);
        SKOSReasoner reasoner = new SKOSReasoner(skosManager, owlReasoner);
        for (SKOSEntity ent : reasoner.getSKOSNarrowerConcepts(sdf.getSKOSConcept(java.net.URI.create(cat.getURI())))) {
            categorySet.add(new SKOSCategory(ent.getURI().toString()));
        }
        owlReasoner.dispose();
        return categorySet;
    }

    public Set<SKOSCategory> getBroaderCategories(SKOSCategory cat) {
        Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
        OWLReasoner owlReasoner = new PelletReasoner(conv.getAsOWLOntology(skosDataset), BufferingMode.BUFFERING);
        SKOSReasoner reasoner = new SKOSReasoner(skosManager, owlReasoner);
        for (SKOSEntity ent : reasoner.getSKOSBroaderConcepts(sdf.getSKOSConcept(java.net.URI.create(cat.getURI())))) {
            categorySet.add(new SKOSCategory(ent.getURI().toString()));
        }
        owlReasoner.dispose();
        return categorySet;
    }

    public Set<SKOSCategory> getNarrowerTransitiveCategories(SKOSCategory cat) {
        Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
        OWLReasoner owlReasoner = new PelletReasoner(conv.getAsOWLOntology(skosDataset), BufferingMode.BUFFERING);
        SKOSReasoner reasoner = new SKOSReasoner(skosManager, owlReasoner);
        for (SKOSEntity ent : reasoner.getSKOSNarrowerTransitiveConcepts(sdf.getSKOSConcept(java.net.URI.create(cat.getURI())))) {
            categorySet.add(new SKOSCategory(ent.getURI().toString()));
        }
        owlReasoner.dispose();
        return categorySet;
    }

    public Set<SKOSCategory> getBroaderTransitiveCategories(SKOSCategory cat) {
        Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
        OWLReasoner owlReasoner = new PelletReasoner(conv.getAsOWLOntology(skosDataset), BufferingMode.BUFFERING);
        SKOSReasoner reasoner = new SKOSReasoner(skosManager, owlReasoner);
        for (SKOSEntity ent : reasoner.getSKOSBroaderTransitiveConcepts(sdf.getSKOSConcept(java.net.URI.create(cat.getURI())))) {
            categorySet.add(new SKOSCategory(ent.getURI().toString()));
        }
        owlReasoner.dispose();
        return categorySet;
    }

    /**
     * Añade la categoría y subcategorías de ésta (recursivamente) al vocabulario
     * @param cat Categoría
     * @param root Indica si la categoría a añadir se trata de una categoría raíz
     */
    public void addCategory(SKOSCategory cat, boolean root) {
        allCategories.add(cat);
        try {
            List<SKOSChange> addAssertions = new ArrayList<SKOSChange>();
            SKOSConcept concept = sdf.getSKOSConcept(java.net.URI.create(cat.getURI()));
            SKOSEntityAssertion entityAssertion = sdf.getSKOSEntityAssertion(concept);
            addAssertions.add(new AddAssertion(skosDataset, entityAssertion));

            if (root) { // Añadimos la marca de root si es raíz de categorías
                SKOSAnnotation anon = sdf.getSKOSAnnotation(IRI.create("http://www.w3.org/2000/01/rdf-schema#comment").toURI(),
                        sdf.getSKOSUntypedConstant("rootCategory", "en"));
                addAssertions.add(new AddAssertion(skosDataset, sdf.getSKOSAnnotationAssertion(concept, anon)));
            }
            for (SKOSCategory subcat : cat.getSubCategories()) {
                addCategory(subcat, false);
                SKOSConcept conceptSon = sdf.getSKOSConcept(java.net.URI.create(subcat.getURI()));
                SKOSObjectRelationAssertion propertyAssertion = sdf.getSKOSObjectRelationAssertion(concept, narrower, conceptSon);
                addAssertions.add(new AddAssertion(skosDataset, propertyAssertion));
            }
            this.skosManager.applyChanges(addAssertions);
        } catch (SKOSChangeException ex) {
            Log.getLog().error("<CategoryVocabulary:addCategory> SKOSChangeException");
        }
    }
    
    public Set<SKOSCategory> getRootCategories() {
        Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
        OWLReasoner owlReasoner = new PelletReasoner(conv.getAsOWLOntology(skosDataset), BufferingMode.BUFFERING);
        SKOSReasoner reasoner = new SKOSReasoner(skosManager, owlReasoner);
        for (SKOSConcept con : reasoner.getSKOSConcepts()) {
            for (SKOSAnnotation annontation : con.getSKOSAnnotations(skosDataset)) {
                if (annontation.getAnnotationValueAsConstant().getLiteral().equals("rootCategory")) {
                    categorySet.add(new SKOSCategory(con.getURI().toString()));
                }
            }
        }
        owlReasoner.dispose();
        return categorySet;
    }
    
    public Set<SKOSCategory> getAllCategories () {
        return allCategories;
    }
}
