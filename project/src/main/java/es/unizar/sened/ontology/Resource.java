package es.unizar.sened.ontology;

import java.net.URI;

/**
 * @author gesteban@unizar.es
 */
public abstract class Resource {

	protected final URI URI;
	protected final String uriString;

	public Resource(String stringURI) {
		this.uriString = stringURI;
		this.URI = java.net.URI.create(stringURI.replace(" ", ","));
	}

	public Resource(URI URI) {
		this.URI = URI;
		this.uriString = URI.toASCIIString();
	}

	public String getURI() {
		return uriString;
	}

	public abstract String getName();

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Resource other = (Resource) obj;
		if ((this.uriString == null) ? (other.uriString != null) : !this.uriString.equals(other.uriString)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 47 * hash + (this.uriString != null ? this.uriString.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return uriString;
	}

}
