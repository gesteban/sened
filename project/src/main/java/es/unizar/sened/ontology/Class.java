package es.unizar.sened.ontology;

import es.unizar.sened.ontology.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author gesteban@unizar.es
 */
public class Class extends Resource {

	public Class(String stringURI) {
		super(stringURI);
	}

	public OWLClass toOWLClass() {
		return OntologyManager.getInstance().getOWLDataFactory().getOWLClass(IRI.create(uriString));
	}

	@Override
	public String getName() {
		return getURI();
	}
}
