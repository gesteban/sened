package es.unizar.sened.utils;

import org.apache.jena.ontology.OntProperty;

import com.google.common.base.MoreObjects;

public class PropAndDir {

	public OntProperty prop;
	public boolean straight;

	public PropAndDir(OntProperty prop, boolean straight) {
		this.prop = prop;
		this.straight = straight;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(OntProperty.class)
				.add("prop", prop.getURI()).add("straight", straight)
				.toString();
	}
}