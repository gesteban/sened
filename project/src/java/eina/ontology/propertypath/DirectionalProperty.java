/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eina.ontology.propertypath;

import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * Clase para tratar una {@link OWLObjectProperty} conjuntamente con la 
 * dirección en la que se debe aplicar. <br>
 * Es necesario crear ésta clase ya que a lo que consultas con SPARQL se
 * refiere, no es lo mismo consultar [URI propiedad ?x] que [?x propiedad URI]. <br>
 * Nota: las propiedades contenidas en éste objeto serán por regla general
 * externas, ya que serán las usadas para crear las consultas a la DBPedia
 *
 * @author Guillermo Esteban
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
