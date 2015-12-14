package es.unizar.sened.model;

import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * Clase que representa una {@link OWLObjectProperty} y la dirección en la que
 * se debe aplicar en una consulta. <br>
 * 
 * @author gesteban@unizar.es
 */
public class DirectionalProperty {

	public OWLObjectProperty property;
	public boolean straight;

	protected DirectionalProperty(OWLObjectProperty prop, boolean straight) {
		this.property = prop;
		this.straight = straight;
	}

	@Override
	public String toString() {
		return property.getIRI().toString() + " - " + straight;
	}
}
