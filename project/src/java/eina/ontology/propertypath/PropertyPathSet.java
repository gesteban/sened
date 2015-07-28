package eina.ontology.propertypath;

import eina.ontology.OntologyManager;
import java.util.HashSet;

/**
 * 
 * @author Guillermo Esteban
 */
public class PropertyPathSet extends HashSet<PropertyPath> {

    static final OntologyManager om = OntologyManager.getInstance();
    
    public PropertyPathSet () {
        super();
    }
    
}
