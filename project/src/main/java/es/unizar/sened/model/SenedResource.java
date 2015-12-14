package es.unizar.sened.model;

public class SenedResource {

	public static final String TAG = SenedResource.class.getSimpleName();

	private org.apache.jena.rdf.model.Resource _resource;

	public SenedResource(org.apache.jena.rdf.model.Resource resource) {
		_resource = resource;
	}

}
