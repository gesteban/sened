package es.unizar.sened.query;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gesteban@unizar.es
 */
public class JQueryFactoryConfig {

	private String endpoint;
	private Map<String, String> datasetMap;
	/**
	 * @deprecated
	 */
	private String language;

	/**
	 * Constructor estándar con la configuración para acceder al endpoint oficial de la DBpedia.
	 * <p>
	 * Las consultas de {@link JQueryFactory} están basadas en ésta configuración.
	 */
	public JQueryFactoryConfig() {
		this.endpoint = "http://dbpedia.org/sparql";
		this.datasetMap = new HashMap<String, String>();
		this.language = "ES";
	}

	public JQueryFactoryConfig(String endpoint) {
		this.endpoint = endpoint;
		this.datasetMap = new HashMap<String, String>();
		this.language = "ES";
	}

	/**
	 * Constructor para cuando usemos algún otro endpoint
	 * 
	 * @param endpoint
	 * @param datasetMap
	 */
	public JQueryFactoryConfig(String endpoint, Map<String, String> datasetMap) {
		this.endpoint = endpoint;
		this.datasetMap = datasetMap;
		this.language = "ES";
	}

	public Map<String, String> getDatasetMap() {
		return datasetMap;
	}

	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * @deprecated
	 * @return
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @deprecated
	 * @return
	 */
	public void setLanguage(String lang) {
		this.language = lang;
	}

}
