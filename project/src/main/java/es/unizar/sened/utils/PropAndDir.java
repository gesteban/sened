package es.unizar.sened.utils;

import org.semanticweb.owlapi.model.OWLProperty;

import com.google.common.base.MoreObjects;

public class PropAndDir {

	public OWLProperty prop;
	public boolean straight;

	// TODO what about direction of property?
	public PropAndDir(OWLProperty prop, boolean straight) {
		this.prop = prop;
		this.straight = straight;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PropAndDir.class).add("prop", prop.getIRI().toString())
				.add("straight", straight).toString();
	}
}