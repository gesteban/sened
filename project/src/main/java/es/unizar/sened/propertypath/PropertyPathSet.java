package es.unizar.sened.propertypath;

import es.unizar.sened.ontology.OntologyManager;
import java.util.HashSet;

/**
 * @author gesteban@unizar.es
 */
public class PropertyPathSet extends HashSet<PropertyPath> {

	private static final long serialVersionUID = -9089653013529898008L;
	
	public static final OntologyManager om = OntologyManager.getInstance();

	public PropertyPathSet() {
		super();
	}

}
