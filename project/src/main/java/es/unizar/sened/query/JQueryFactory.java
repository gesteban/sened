package es.unizar.sened.query;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLRuntimeException;

import es.unizar.sened.ontology.DomainOntology;
import es.unizar.sened.ontology.OntologyManager;
import es.unizar.sened.ontology.SKOSCategory;
import es.unizar.sened.propertypath.DirectionalProperty;
import es.unizar.sened.propertypath.PropertyPath;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.PropAndDir;

/**
 * @author gesteban@unizar.es
 */
public class JQueryFactory {

	public static final String TAG = JQueryFactory.class.getSimpleName();

	private static final String PREFIX_ALL = "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n";
	private static final String QF_SUBCATEGORIES = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
			+ "SELECT ?subcat \n" + "WHERE { \n" + "  ?subcat skos:broader <#categoryURI> \n}";
	private static final String QF_TYPE = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "SELECT DISTINCT ?type \n" + "WHERE { \n"
			+ "  {<#uri> rdf:type ?type} \n" + "  UNION \n" + "  {  <#uri> rdf:type ?xtype \n"
			+ "   . ?xtype rdfs:subClassOf ?type \n" + "  } \n" + "}";

	private OntologyManager om = OntologyManager.getInstance();
	private JQueryFactoryConfig config;

	public JQueryFactory(JQueryFactoryConfig config) {
		this.config = config;
	}

	public JQueryFactory() {
		this.config = new JQueryFactoryConfig();
	}

	/**
	 * @deprecated Use {@link #getKeywordQuery} instead
	 */
	public JQuery getKeywordQuery_SKOSTaxonomy(String keyword, String categoryURI, int categoryDeep) {
		// Primero debemos buscar los Data Properties que se extraerán en la búsqueda
		// Como en nuestro caso siempre extraremos artículos, buscamos los Data Properties
		// de estos.
		OWLClass tipo = om.getOWLDataFactory().getOWLClass(IRI.create(DomainOntology.Articulo));

		Set<PropAndDir> queryAbout = om.getPropertiesToRetrieve(tipo);

		// Vamos montando la consulta paso a paso.
		String queryString = "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" + "SELECT DISTINCT ?uri";
		// La nueva elimina el @en y transforma todo en string.
		for (PropAndDir propAndDir : queryAbout) {
			queryString += "?" + propAndDir.prop.getIRI().getRemainder().get();
		}
		queryString += " {\n";

		// Ponemos el filtro de categorias.
		queryString += "{"
				// prof 0
				+ " { ?uri dcterms:subject <#categoryURI>  }"
				// prof 1
				+ " UNION { ?uri dcterms:subject ?subcat1. "
				+ " ?subcat1 skos:broader <#categoryURI>.  }"
				// prof 2
				+ " UNION { ?uri dcterms:subject ?subcat2. " + " ?subcat2_1  skos:broader <#categoryURI>.  "
				+ " ?subcat2 skos:broader ?subcat2_1 }	"
				// PROF 3
				+ " UNION { ?uri dcterms:subject ?subcat3. " + " ?subcat3_1  skos:broader <#categoryURI>.  "
				+ " ?subcat3_2 skos:broader ?subcat3_1 .  " + " ?subcat3  skos:broader ?subcat3_2 } "
				//
				+ "}";
		queryString = queryString.replace("#categoryURI", categoryURI);

		// Y ahora el de los campos.
		int count = 0;
		for (PropAndDir propAndDir : queryAbout) {
			if (count != 0) {
				queryString += ".";
			}
			if (propAndDir.straight) {
				queryString += " ?uri " + propAndDir.prop.getIRI().toQuotedString() + " ?"
						+ propAndDir.prop.getIRI().getRemainder().get() + "\n";
			} else {
				queryString += " ?" + propAndDir.prop.getIRI().getRemainder().get()
						+ propAndDir.prop.getIRI().toQuotedString() + " ?uri " + "\n";
			}
			try {
				String lang = om.getAnnotationQueryLanguage(propAndDir.prop.asOWLDataProperty());
				if (lang != null) {
					queryString += "  FILTER langMatches(lang(?" + propAndDir.prop.getIRI().getRemainder().get()
							+ "),\"" + lang + "\") \n";
				}
			} catch (OWLRuntimeException ex) {
				Log.e(TAG, "<getKeywordQuery_SKOSTaxonomy> OWLRuntimeException, " + propAndDir.prop.toString()
						+ " not a data property");
			}
			count++;
		}

		for (PropAndDir propAndDir : queryAbout) {
			if (om.isKeywordSearchable(propAndDir.prop)) {
				queryString += "  FILTER regex(?" + propAndDir.prop.getIRI().getRemainder().get()
						+ ",\"[.., )(\\\\-\\\"]" + keyword + "[.., )(\\\\-\\\"]\",\"i\")\n";
			}
		}

		// TODO hardcoded shit
		queryString += "  FILTER regex(?primaryTopic,\"en.wikipedia.org\",\"i\")\n}" + "  ORDER BY ?uri";
		return new JQuery(queryString, config);
	}

	/**
	 * @deprecated Use {@link #getKeywordQuery} instead
	 */
	@Deprecated
	public JQuery getKeywordQuery_SKOSTaxonomy(String keyword, String categoryURI, Set<SKOSCategory> categorySet) {
		Log.d(TAG, "<getKeywordQuery_SKOSTaxonomy> category set size = " + categorySet.size());
		// Primero debemos buscar los Data Properties que se extraerán en la búsqueda
		// Como en nuestro caso siempre extraremos artículos, buscamos los Data Properties
		// de estos.
		OWLClass tipo = om.getOWLDataFactory().getOWLClass(IRI.create(DomainOntology.Articulo));

		Set<PropAndDir> queryAbout = om.getPropertiesToRetrieve(tipo);

		// Vamos montando la consulta paso a paso.
		String queryString = "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" + "SELECT DISTINCT ?uri";
		// La nueva elimina el @en y transforma todo en string.
		for (PropAndDir propAndDir : queryAbout) {
			queryString += "?" + propAndDir.prop.getIRI().getRemainder().get();
		}
		queryString += " {\n{\n";

		// Ponemos el filtro de categorias.
		int catCount = 0;
		for (SKOSCategory cat : categorySet) {
			if (catCount != 0) {
				queryString += "UNION ";
			}
			queryString += "{ ?uri dcterms:subject <" + cat.getURI() + "> } \n";
			catCount++;
		}
		queryString += "}\n";

		// Y ahora el de los campos.
		int count = 0;
		for (PropAndDir propAndDir : queryAbout) {
			if (count != 0) {
				queryString += ".";
			}
			if (propAndDir.straight) {
				queryString += " ?uri " + propAndDir.prop.getIRI().toQuotedString() + " ?"
						+ propAndDir.prop.getIRI().getRemainder().get() + "\n";
			} else {
				queryString += " ?" + propAndDir.prop.getIRI().getRemainder().get()
						+ propAndDir.prop.getIRI().toQuotedString() + " ?uri " + "\n";
			}
			try {
				String lang = om.getAnnotationQueryLanguage(propAndDir.prop.asOWLDataProperty());
				if (lang != null) {
					queryString += "  FILTER langMatches(lang(?" + propAndDir.prop.getIRI().getRemainder().get()
							+ "),\"" + lang + "\") \n";
				}
			} catch (OWLRuntimeException ex) {
				Log.e(TAG, "<getKeywordQuery_SKOSTaxonomy> OWLRuntimeException, " + propAndDir.prop.toString()
						+ " not a data property");
			}
			count++;
		}

		for (PropAndDir propAndDir : queryAbout) {
			if (om.isKeywordSearchable(propAndDir.prop)) {
				queryString += "  FILTER regex(?" + propAndDir.prop.getIRI().getRemainder().get()
						+ ",\"[.., )(\\\\-\\\"]" + keyword + "[.., )(\\\\-\\\"]\",\"i\")\n";
			}
		}

		// TODO hardcoded shit
		queryString += "  FILTER regex(?primaryTopic,\"en.wikipedia.org\",\"i\")\n}" + "  ORDER BY ?uri";
		return new JQuery(queryString, config);
	}

	public JQuery getKeywordQuery(String keyword, String categoryURI, int categoryDeep) {
		Log.d(TAG, "<getKeywordQuery> Creating query for keyword:" + keyword + " categoryURI:" + categoryURI
				+ " categoryDeep:" + categoryDeep);

		// Primero debemos buscar los Data Properties que se extraerán en la búsqueda
		// Como en nuestro caso siempre extraremos artículos, buscamos los Data Properties
		// de estos.
		OWLClass type = om.getOWLDataFactory().getOWLClass(IRI.create(DomainOntology.Articulo));
		// Set<PropAndDir> queryAbout = PropAndDir.getPropertiesAndDirectionFrom(tipo);
		Set<PropAndDir> queryAbout = om.getPropertiesToRetrieve(type);

		String queryString = PREFIX_ALL + "SELECT DISTINCT ?uri";

		// La nueva elimina el @en y transforma todo en string.
		for (PropAndDir propAndDir : queryAbout) {
			queryString += " ?" + propAndDir.prop.getIRI().getRemainder().get();
		}
		queryString += " {\n  {\n";

		// Ponemos el filtro de categorias.
		for (int i = 0; i < categoryDeep; i++) {
			if (i != 0) {
				queryString += "    UNION \n";
			}
			queryString += "    { ?uri dcterms:subject";
			for (int j = 0; j < i; j++) {
				queryString += "/skos:broader";
			}
			queryString += " <" + categoryURI + "> } \n";
		}
		queryString += "  } \n";

		// Y ahora el de los campos.
		int count = 0;
		for (PropAndDir propAndDir : queryAbout) {
			Log.i(TAG, propAndDir.toString());
			queryString += count != 0 ? "." : " ";
			if (propAndDir.straight) {
				queryString += " ?uri " + propAndDir.prop.getIRI().toQuotedString() + " ?"
						+ propAndDir.prop.getIRI().getRemainder().get() + "\n";
			} else {
				queryString += " ?" + propAndDir.prop.getIRI().getRemainder().get() + " "
						+ propAndDir.prop.getIRI().toQuotedString() + " ?uri " + "\n";
			}
			try {
				String lang = om.getAnnotationQueryLanguage(propAndDir.prop.asOWLDataProperty());
				if (lang != null) {
					queryString += "  FILTER langMatches(lang(?" + propAndDir.prop.getIRI().getRemainder().get()
							+ "),\"" + lang + "\") \n";
				}
			} catch (OWLRuntimeException ex) {
				Log.e(TAG, "<getKeywordQuery_SKOSTaxonomy> OWLRuntimeException, " + propAndDir.prop.toString()
						+ " not a data property");
			}
			count++;
		}

		for (PropAndDir propAndDir : queryAbout) {
			if (om.isKeywordSearchable(propAndDir.prop)) {
				queryString += "  FILTER regex(?" + propAndDir.prop.getIRI().getRemainder().get()
						+ ",\"[.., )(\\\\-\\\"]" + keyword + "[.., )(\\\\-\\\"]\",\"i\")\n";
			}
		}

		queryString += "}";

		// TODO hardcoded shit
		Log.d(TAG, "\n" + queryString);
		return new JQuery(queryString, config);
	}

	/**
	 * TODO test properly
	 */
	public JQuery getKeywordQuery_ClassTaxonomy(String keyword, String classURI) {
		OWLClass tipo = om.getOWLDataFactory().getOWLClass(IRI.create(classURI));
		Set<PropAndDir> queryAbout = om.getPropertiesToRetrieve(tipo);

		// Vamos montando la consulta paso a paso.
		String queryString = "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" + "SELECT DISTINCT ?uri";

		for (PropAndDir propAndDir : queryAbout) {
			queryString += " (xsd:string(str(?" + propAndDir.prop.getIRI().getRemainder().get() + ")) AS ?"
					+ propAndDir.prop.getIRI().getRemainder().get() + ")";
		}
		queryString += " {\n";

		// Ponemos el filtro de la clase.
		queryString += " ?uri a " + tipo.toString() + " . \n";

		// Y ahora el de los campos.
		int count = 0;
		for (PropAndDir propAndDir : queryAbout) {
			if (count != 0) {
				queryString += ".";
			}
			if (propAndDir.straight) {
				queryString += " ?uri " + propAndDir.prop.getIRI().toQuotedString() + " ?"
						+ propAndDir.prop.getIRI().getRemainder().get() + "\n";
			} else {
				queryString += " ?" + propAndDir.prop.getIRI().getRemainder().get()
						+ propAndDir.prop.getIRI().toQuotedString() + " ?uri " + "\n";
			}
			try {
				String lang = om.getAnnotationQueryLanguage(propAndDir.prop.asOWLDataProperty());
				if (lang != null) {
					queryString += "  FILTER langMatches(lang(?" + propAndDir.prop.getIRI().getRemainder().get()
							+ "),\"" + lang + "\") \n";
				}
			} catch (OWLRuntimeException ex) {
				Log.e(TAG, "<getKeywordQuery_ClassTaxonomy> OWLRuntimeException, " + propAndDir.prop.toString()
						+ " not a data property");
			}
			count++;
		}

		for (PropAndDir propAndDir : queryAbout) {
			if (om.isKeywordSearchable(propAndDir.prop)) {
				queryString += "  FILTER regex(?" + propAndDir.prop.getIRI().getRemainder().get()
						+ ",\"[.., )(\\\\-\\\"]" + keyword + "[.., )(\\\\-\\\"]\",\"i\")\n";
			}
		}

		// TODO hardcoded shit
		queryString += "  FILTER regex(?primaryTopic,\"en.wikipedia.org\",\"i\")\n}" + "  ORDER BY ?uri";
		return new JQuery(queryString, config);
	}

	/**
	 * Consulta para la construcción del árbol de categorías. <br>
	 * Del {@link JResult} resultante se deberá invocar {@link JResult#asCategorySet()}
	 *
	 * @param categoryURI
	 *            URI de la {@link SKOSCategory} a consultar
	 * @return un {@link JQuery} con la consulta apropiada
	 */
	public JQuery getSubCategoryQuery(String categoryURI) {
		String queryString = QF_SUBCATEGORIES;
		queryString = queryString.replace("#categoryURI", categoryURI);
		return new JQuery(queryString, config);
	}

	/**
	 * Crea una {@link JQuery} a partir de una URI y un {@link PropertyPath} <br>
	 *
	 * @param URI
	 *            URI inicial de la que se desea extraer más información
	 * @param pp
	 *            {@link PropertyPath} que indicará el camino de propiedades a seguir para extraer más información de la
	 *            URI
	 * @return {@link JQuery} con la consulta
	 */
	public JQuery getPropertyPathQuery(String URI, PropertyPath pp) {

		// Buscamos las Data Properties que se pueden aplicar a la clase que buscamos.
		OWLClass tipo = pp.getTargetClass();
		Log.d(TAG, "<getPropertyPathQuery> Creando consulta con clase destino = " + tipo.getIRI().getRemainder().get());

		Set<PropAndDir> queryAbout = om.getPropertiesToRetrieve(tipo);

		// Extraemos las DataProperties externas que tengan como dominio la clase de la
		// que estoy buscando información.
		// Set<OWLEntity> queryAbout = om.getRetreavableProperties(OWLEntity.class,
		// om.getSubsetExternal(om.getDataPropertiesByDomain(tipo)));
		// queryAbout.addAll(om.getRetreavableProperties(OWLEntity.class,
		// om.getSubsetExternal(om.getObjectPropertiesByDomain(tipo))));
		Log.d(TAG, "<getPropertyPathQuery> La consulta extraera " + queryAbout.size() + " Properties de la uri");

		// Ahora crearemos la cadena SPARQL con la búsqueda.
		String queryString = "SELECT DISTINCT ?uri";
		String regex = "";
		for (PropAndDir propAndDir : queryAbout) {
			queryString += " ((str(?" + propAndDir.prop.getIRI().getRemainder().get() + ")) AS ?"
					+ propAndDir.prop.getIRI().getRemainder().get() + ")";
			if (propAndDir.prop.getIRI().getRemainder().get().equals("primaryTopic")) {
				regex = "  FILTER regex(?primaryTopic,\"en.wikipedia.org\",\"i\")";
			}
		}
		queryString += " {\n";

		// Primero aplicamos el PropertyPath.
		DirectionalProperty dp = pp.get(0);
		if (dp.straight) {
			queryString += "  <#uri> " + dp.property.toString() + " ?uri .\n";
		} else {
			queryString += "  ?uri " + dp.property.toString() + " <#uri> .\n";
		}

		// Y ahora el de los campos.
		int count = 0;
		for (PropAndDir propAndDir : queryAbout) {
			if (count != 0) {
				queryString += ".";
			}
			if (propAndDir.straight) {
				queryString += " ?uri " + propAndDir.prop.getIRI().toQuotedString() + " ?"
						+ propAndDir.prop.getIRI().getRemainder().get() + "\n";
			} else {
				queryString += " ?" + propAndDir.prop.getIRI().getRemainder().get()
						+ propAndDir.prop.getIRI().toQuotedString() + " ?uri " + "\n";
			}
			try {
				String lang = om.getAnnotationQueryLanguage(propAndDir.prop.asOWLDataProperty());
				if (lang != null) {
					queryString += "  FILTER langMatches(lang(?" + propAndDir.prop.getIRI().getRemainder().get()
							+ "),\"" + lang + "\") \n";
				}
			} catch (OWLRuntimeException ex) {
				Log.e(TAG, "<getPropertyPathQuery> OWLRuntimeException, " + propAndDir.prop.toString()
						+ " not a data property");
			}
			count++;
		}

		// TODO hardcoded shit
		queryString += regex + "\n}  ORDER BY ?uri";

		// // A continuación hacemos pattern sobre los datos que queremos extraer
		// for (OWLEntity ent : queryAbout) {
		// queryString += "  . ?uri " + ent.getIRI().toQuotedString() + " ?"
		// + ent.getIRI().getFragment() + "\n";
		// String lang = om.getAnnotationQueryLanguage(om.getOWLDataFactory().getOWLDataProperty(ent.getIRI()));
		// if (lang != null) {
		// queryString += "  FILTER langMatches(lang(?" + ent.getIRI().getFragment()
		// + "),\"" + lang + "\") \n";
		// }
		// }
		// // Finalmente hacemos pattern sobre el rdf:type que esperamos obtener
		// if (om.getSubsetExternal(om.getClassEquivalents(tipo)).size() == 1) {
		// String typeMatch =
		// om.getSubsetExternal(om.getClassEquivalents(tipo)).iterator().next().getIRI().toQuotedString();
		// queryString += ". ?uri a " + typeMatch + " \n";
		// } else {
		// Log.getLog().warning("<JQueryFactory:getPropertyPathQuery> Numero de externalEquivalentClasses != 1");
		// }
		//
		// queryString += "}";

		queryString = queryString.replace("#uri", URI);
		Log.i(TAG, "<getPropertyPathQuery> " + queryString);
		return new JQuery(queryString, config);
	}

	public JQuery getTypeQuery(String URI) {
		String queryString = QF_TYPE;
		queryString = queryString.replace("#uri", URI);
		return new JQuery(queryString, config);
	}

	public JQuery getCustomQuery(String query) {
		String queryString = query;
		for (String key : config.getDatasetMap().keySet()) {
			queryString = queryString.replace(key, config.getDatasetMap().get(key));
		}
		return new JQuery(queryString, config);
	}

	public static void main(String[] args) {
		// JQueryFactory jqf = new JQueryFactory();
		// System.out.println(jqf.getKeywordQuery_ClassTaxonomy("spain",
		// "http://dbpedia.org/ontology/Country").toString());
		// System.out.println(jqf.getKeywordQuery_SKOSTaxonomy("viscosity",
		// "http://dbpedia.org/resource/Category:Mechanics", 1));
		// Set<PropAndDir> set =
		// jqf.getPropertiesAndDirectionFrom(jqf.om.getOWLDataFactory().getOWLClass(IRI.create("http://dbpedia.org/ontology/Country")));
		// for(PropAndDir pd : set) {
		// System.out.println(pd);
		// }
	}
}
