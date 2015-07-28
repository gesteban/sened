/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eina.utils;

import com.google.common.base.Objects;
import eina.ontology.OntologyManager;
import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;

/**
 *
 * @author peonza
 */
public class PropAndDir {

    private static OntologyManager om = OntologyManager.getInstance();
    public OWLProperty prop;
    public boolean wantValue;

    public PropAndDir(OWLProperty prop, boolean wantValue) {
        this.prop = prop;
        this.wantValue = wantValue;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(PropAndDir.class).add("prop", prop.getIRI().toString()).add("straight", wantValue).toString();
    }

    public static Set<PropAndDir> getPropertiesAndDirectionFrom(OWLClass tipo) {
        Set<PropAndDir> propSet = new HashSet<PropAndDir>();
        for (OWLDataProperty prop : om.getSubsetExternal(OWLDataProperty.class,
                om.getRetreavableProperties(OWLDataProperty.class,
                om.getDataPropertiesByDomainExtended(tipo)))) {
            propSet.add(new PropAndDir(prop, true));
        }
        System.out.println(">>>>>>>>>>>>>>>>>propSet=" + propSet.size());
        for (OWLObjectProperty prop : om.getSubsetExternal(OWLObjectProperty.class,
                om.getRetreavableProperties(OWLObjectProperty.class,
                om.getObjectPropertiesByDomainOrRangeExtended(tipo)))) {
            propSet.add(new PropAndDir(prop, false));
        }
        return propSet;
    }
}