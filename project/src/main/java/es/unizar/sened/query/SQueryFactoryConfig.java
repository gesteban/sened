package es.unizar.sened.query;


/**
 * @author gesteban@unizar.es
 */
public class SQueryFactoryConfig {

	private String endpoint;

	public SQueryFactoryConfig() {
		this.endpoint = "http://dbpedia.org/sparql";
	}

	public SQueryFactoryConfig(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getEndpoint() {
		return endpoint;
	}

}
