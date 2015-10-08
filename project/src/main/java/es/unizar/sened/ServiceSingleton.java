package es.unizar.sened;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.resultset.ResultSetException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import es.unizar.sened.lucene.LuceneIndex;
import es.unizar.sened.ontology.Article;
import es.unizar.sened.ontology.DomainOntology;
import es.unizar.sened.ontology.OntologyManager;
import es.unizar.sened.ontology.Resource;
import es.unizar.sened.ontology.SKOSCategory;
import es.unizar.sened.propertypath.PropertyPath;
import es.unizar.sened.propertypath.PropertyPathSet;
import es.unizar.sened.query.JQuery;
import es.unizar.sened.query.JQueryFactory;
import es.unizar.sened.query.JQueryFactoryConfig;
import es.unizar.sened.query.JResult;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.XMLutil;

/**
 * @author gesteban@unizar.es
 */
public class ServiceSingleton {

	public static final String TAG = ServiceSingleton.class.getSimpleName();
	public static final String ENDPOINT = "http://dbpedia.org/sparql";
	public static int QUERY_DEEP = 2;

	private LuceneIndex _luceneIndex;
	private JQueryFactory _queryFactory;
	private OntologyManager _ontologyManager = OntologyManager.getInstance();

	/**
	 * Cache for dbpedia keyword queries.
	 */
	private final LoadingCache<KeywordAndResource, JResult> requestsCache = CacheBuilder.newBuilder().maximumSize(100)
			.build(new CacheLoader<KeywordAndResource, JResult>() {

				@Override
				public JResult load(KeywordAndResource kAndC) throws Exception {
					JQuery query = null;
					if (_ontologyManager.getTaxonomyType().equals(OntologyManager.TAXONOMY_CLASSES)) {
						query = _queryFactory.getKeywordQuery_ClassTaxonomy(kAndC.getKeyword(), kAndC.getResource()
								.getURI());
					} else if (_ontologyManager.getTaxonomyType().equals(OntologyManager.TAXONOMY_CATEGORIES)) {
						query = _queryFactory.getKeywordQuery(kAndC.getKeyword(), kAndC.getResource().getURI(),
								QUERY_DEEP);
					}
					Log.i(TAG, "<LoadingCache> Performing search of " + kAndC.toString());
					return query == null ? null : query.doQuery();

				}
			});

	private static class ServiceSingletonHolder {

		public static final ServiceSingleton instance = new ServiceSingleton();
	}

	public static ServiceSingleton getInstance() {
		return ServiceSingletonHolder.instance;
	}

	private ServiceSingleton() {
		try {
			init(true);
		} catch (IOException ex) {
			Log.e(TAG, "Error al instanciar ServiceSingleton");
			ex.printStackTrace();
		}
	}

	private void init(boolean loadPreviousLuceneIndex) throws IOException {
		
		_luceneIndex = new LuceneIndex();
		if (loadPreviousLuceneIndex) {
			_luceneIndex.load();
		}

		// TODO implement category filters
		// Set<SKOSCategory> skosCategoriesToAvoid = new HashSet<SKOSCategory>();
		// skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/Category:Industrial_automation"));
		// skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/Category:Industrial_computing"));
		// skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Industry_museums"));
		// skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Industrial_parks"));
		// skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Industry_in_the_Arctic"));
		// skosCategoriesToAvoid.add(new
		// SKOSCategory("http://dbpedia.org/resource/category:Labor_disputes_by_industry"));
		// skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Industry_in_Ancient_Rome"));
		// skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Industrial_history"));
		// skosCategoriesToAvoid.add(new
		// SKOSCategory("http://dbpedia.org/resource/category:Bibliographies_of_industry"));
		// Set<String> stringFilterSet = new HashSet<String>();
		// stringFilterSet.add("_in_");
		// stringFilterSet.add("_by_");
		// stringFilterSet.add("mitsubishi");
		// stringFilterSet.add("mining");
		// stringFilterSet.add("museum");
		// stringFilterSet.add("people");
		// stringFilterSet.add("automotive");
		// stringFilterSet.add("anime");
		// stringFilterSet.add("park");
		// stringFilterSet.add("buildings");
		
		// Map<String, String> datasetMap = new HashMap<String, String>();
		// datasetMap.put("dcterms:subject", "skos:subject");
		// JQueryFactoryConfig qfc = new JQueryFactoryConfig("http://dbpedia.linkeddata.es:8898/sparql", datasetMap);
		
		JQueryFactoryConfig qfc = new JQueryFactoryConfig(ENDPOINT);
		_queryFactory = new JQueryFactory(qfc);

	}

	public String searchRelated(String URI) throws ResultSetException {
		OWLClass sourceClass = getInternalClass(URI);

		// Ahora nos dispondremos a obtener todas las clases a las que podemos acceder
		// partiendo de sourceClass, que son:
		// [rango de todas las ObjectProperty que tengan a sourceClass como dominio] +
		// [dominio de todas las ObjectProperty que tengan a sourceClass como rango]
		Set<OWLClass> classSet = new HashSet<OWLClass>();
		Set<OWLEntity> entitySet = _ontologyManager.getSubsetInternal(_ontologyManager
				.getObjectPropertiesByDomain(sourceClass));
		Set<OWLObjectProperty> objectPropertySet = new HashSet<OWLObjectProperty>();
		for (OWLEntity ent : entitySet) {
			objectPropertySet.add(ent.asOWLObjectProperty());
		}
		entitySet = _ontologyManager.getSubsetInternal(_ontologyManager.getObjectPropertyRange(objectPropertySet));
		for (OWLEntity ent : entitySet) {
			classSet.add(ent.asOWLClass());
		}
		entitySet = _ontologyManager.getSubsetInternal(_ontologyManager.getObjectPropertiesByRange(sourceClass));
		objectPropertySet = new HashSet<OWLObjectProperty>();
		for (OWLEntity ent : entitySet) {
			objectPropertySet.add(ent.asOWLObjectProperty());
		}
		entitySet = _ontologyManager.getSubsetInternal(_ontologyManager.getObjectPropertyDomain(objectPropertySet));
		for (OWLEntity ent : entitySet) {
			classSet.add(ent.asOWLClass());
		}

		// Una vez tenemos todas las posibles clases destino, creamos un PropertyPath
		// (que será o no vacío) por cada una de ellas
		Log.d(TAG, "<searchRelated> Creando PropertyPathSet a partir del tipo " + sourceClass.getIRI());
		PropertyPathSet pps = new PropertyPathSet();
		for (OWLClass targetClass : classSet) {
			try {
				pps.addAll(PropertyPath.createSimplePaths(sourceClass, targetClass));
			} catch (Exception ex) {
				Log.w(TAG, "<searchRelated> PropertyPath creation exception (size=0)");
			}
		}
		return doPropertyPathSetQuery(URI, pps);
	}

	public String searchRelatedWithSource(String URI, String sourceType) throws ResultSetException {

		// A continuación extraeremos la OWLClass del String de entrada targetType
		OWLClass sourceClass = null;
		for (OWLClass clase : _ontologyManager.getClasses()) {
			if (clase.getIRI().getFragment().equals(sourceType)) {
				sourceClass = clase;
				break;
			}
		}
		if (sourceClass == null) {// Si no hay hit, falla la búsqueda
			Log.e(TAG, "<searchRelatedWithSource> Sin hit en targetClass");
			return null;
		}

		// Ahora nos dispondremos a obtener todas las clases a las que podemos acceder
		// partiendo de sourceClass, que son:
		// [rango de todas las ObjectProperty que tengan a sourceClass como dominio] +
		// [dominio de todas las ObjectProperty que tengan a sourceClass como rango]
		Set<OWLEntity> entitySet = _ontologyManager.getSubsetInternal(_ontologyManager
				.getObjectPropertiesByDomain(sourceClass));
		Set<OWLObjectProperty> objectPropertySet = new HashSet<OWLObjectProperty>();
		for (OWLEntity ent : entitySet) {
			objectPropertySet.add(ent.asOWLObjectProperty());
		}
		entitySet = _ontologyManager.getSubsetInternal(_ontologyManager.getObjectPropertyRange(objectPropertySet));
		Set<OWLClass> classSet = new HashSet<OWLClass>();
		for (OWLEntity ent : entitySet) {
			classSet.add(ent.asOWLClass());
		}
		entitySet = _ontologyManager.getSubsetInternal(_ontologyManager.getObjectPropertiesByRange(sourceClass));
		objectPropertySet = new HashSet<OWLObjectProperty>();
		for (OWLEntity ent : entitySet) {
			objectPropertySet.add(ent.asOWLObjectProperty());
		}
		entitySet = _ontologyManager.getSubsetInternal(_ontologyManager.getObjectPropertyDomain(objectPropertySet));
		for (OWLEntity ent : entitySet) {
			classSet.add(ent.asOWLClass());
		}

		// Una vez tenemos todas las posibles clases destino, creamos un PropertyPath
		// (que será o no vacío) por cada una de ellas
		Log.d(TAG, "<searchRelatedWithSource> Creando PropertyPathSet a partir del tipo " + sourceClass.getIRI());
		PropertyPathSet pps = new PropertyPathSet();
		for (OWLClass targetClass : classSet) {
			try {
				pps.addAll(PropertyPath.createSimplePaths(sourceClass, targetClass));
			} catch (Exception ex) {
				Log.w(TAG, "<searchRelatedWithSource> PropertyPath creation exception (size=0)");
			}
		}

		return doPropertyPathSetQuery(URI, pps);
	}

	public String searchRelatedWithTarget(String URI, String targetType) throws ResultSetException {

		OWLClass sourceClass = getInternalClass(URI);

		// A continuación extraeremos la OWLClass del String de entrada targetType.
		OWLClass targetClass = null;
		for (OWLClass clase : _ontologyManager.getClasses()) {
			if (clase.getIRI().getFragment().equals(targetType)) {
				targetClass = clase;
				break;
			}
		}
		if (targetClass == null) {// Si no hay hit, falla la búsqueda
			Log.e(TAG, "<searchRelatedWithTarget> Sin hit en targetClass");
			return null;
		}

		// Ahora creamos un PropertyPathSet con el único PropertyPath que tenemos y nos interesa.
		PropertyPathSet pps = new PropertyPathSet();
		pps.addAll(PropertyPath.createSimplePaths(sourceClass, targetClass));

		return doPropertyPathSetQuery(URI, pps);
	}

	private OWLClass getInternalClass(String URI) throws ResultSetException {

		// Primero tendremos que ir a la DBPedia a ver que rdf:type tiene.
		Log.d(TAG, "<searchRelated> Se va a proceder a buscar el tipo interno de: " + URI);
		JQuery query = _queryFactory.getTypeQuery(URI);
		JResult result = query.doQuery();

		// Con los datos de los rdf:type extraídos de la DBPedia se averigua a qué clase
		// interna pertenece la URI.
		OWLClass sourceClass = null;
		if (result.asSimpleColumn().isEmpty()) {
			// Estamos frente a un tipo #Articulo tal y como se define en nuestra ontología.
			sourceClass = _ontologyManager.getOWLDataFactory().getOWLClass(IRI.create(DomainOntology.Articulo));
		} else {
			outerloop: for (String str : result.asSimpleColumn()) {
				for (OWLEntity clase : _ontologyManager.getSubsetExternal(_ontologyManager.getClasses())) {
					if (clase.getIRI().toString().equals(str)) {
						Set<OWLEntity> classSet = _ontologyManager.getSubsetInternal(_ontologyManager
								.getClassEquivalents(clase.asOWLClass()));
						if (classSet.size() != 1 && !str.equals("http://www.w3.org/2002/07/owl#Thing")) {
							Log.e(TAG, "<searchRelated> Fallo en la cantidad de equivalencias encontradas");
						} else if (classSet.size() == 1) {
							sourceClass = _ontologyManager.getOWLDataFactory().getOWLClass(
									classSet.iterator().next().getIRI());
							break outerloop; // al primer acierto, salimos del loop.
						}
					}
				}
			}
		}
		if (sourceClass == null) {
			Log.e(TAG, "<searchRelated> Error buscando el tipo del recurso " + URI);
			Log.e(TAG, "<searchRelated> El recurso no se encuentro en nuestra ontología");
		} else {
			Log.i(TAG, "<searchRelated> Recurso con tipo interno [" + sourceClass.getIRI().getFragment() + "]");
		}

		return sourceClass;
	}

	private String doPropertyPathSetQuery(String URI, PropertyPathSet pps) {

		// Mapa que usaremos para almacenar los pares buscados.
		HashMap<String, JResult> map = new HashMap<String, JResult>();

		// Una vez obtenemos los PropertyPath(s), los usaremos para extraer más información de la URI
		// por medio del constructor de JQuery.
		Log.i(TAG, "<searchRelated> Se va a crear y ejecutar una búsqueda por cada PropertyPath (" + pps.size() + ")");
		int j = 0;
		for (PropertyPath pp : pps) {
			JQuery query = _queryFactory.getPropertyPathQuery(URI, pp);
			JResult result = query.doQuery();
			Log.i(TAG,
					"<doPropertyPathSetQuery> Búsqueda de PropertyPathSet[" + j + "] tiene " + result.getResultSize()
							+ " resultados de tipo " + pp.getTargetClass().getIRI().getFragment());

			// Print results
			// /////////////////////////////////////////////////////
			// for (int i = 0; i < result.getResultSize(); i++) {
			// System.out.print("\t");
			// for (String str : result.getRow(i)) {
			// System.out.print(str + " - ");
			// }
			// System.out.println("");
			// }
			// /////////////////////////////////////////////////////

			j++;
			map.put(pp.getTargetClass().getIRI().getFragment(), result);
		}

		// return salida;
		return XMLutil.resultsSetsToXml(map);

	}

	public void borra() throws IOException {
		_luceneIndex.deleteLastUsedKeywordSearch();
		_luceneIndex.save();
	}

	public void string() {
		System.out.println(_luceneIndex.toString());
	}

	public String searchKeywords(String keywords, String resourceURI) throws Exception {
		if (_ontologyManager.getTaxonomyType().equals(OntologyManager.TAXONOMY_CLASSES)) {
			return searchKeywords(keywords, new es.unizar.sened.ontology.Class(resourceURI));
		} else if (_ontologyManager.getTaxonomyType().equals(OntologyManager.TAXONOMY_CATEGORIES)) {
			return searchKeywords(keywords, new SKOSCategory(resourceURI));
		} else {
			return null;
		}
	}

	public String searchKeywords(String keywords, Resource categoryOrClass) throws Exception {
		// Dividimos el string de entrada en una lista de strings.
		Set<String> keywordSet = new HashSet<String>();
		keywordSet.addAll(Arrays.asList(keywords.split(" ")));
		// Por cada keyword que tenemos.
		for (String keyword : keywordSet) {
			// Miramos si los resultados de su búsqueda están ya almacenadosen Lucene.
			if (!_luceneIndex.existsKeywordDocument(keyword, categoryOrClass.getName())) {
				// Recogemos resultado de cache (o no).
				JResult result = requestsCache.get(new KeywordAndResource(keyword, categoryOrClass));
				// Y se almacenan los resultados en Lucene.
				_luceneIndex.add(keyword, result.asArticleSet(), categoryOrClass.getName());
			}
		}

		return XMLutil.articleListToXml(_luceneIndex.searchKeywords(keywordSet, categoryOrClass.getName()));
	}

	private class KeywordAndResource {

		private final String keyword;
		private final Resource resource;

		public Resource getResource() {
			return resource;
		}

		public String getKeyword() {
			return keyword;
		}

		public KeywordAndResource(String keyword, Resource resource) {
			this.keyword = Preconditions.checkNotNull(keyword, "keyword cannot be null");
			this.resource = Preconditions.checkNotNull(resource, "resource cannot be null");
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final KeywordAndResource other = (KeywordAndResource) obj;
			if (!this.keyword.equals(other.keyword)) {
				return false;
			}
			if (!this.resource.equals(other.resource)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 89 * hash + this.keyword.hashCode();
			hash = 89 * hash + this.resource.hashCode();
			return hash;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(KeywordAndResource.class).add("keyword", keyword).add("resource", resource)
					.toString();
		}
	}

	public static void main(String[] args) throws Exception {
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("cat",
		// "http://dbpedia.org/resource/Category:Mechanics"));
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("senegal",
		// "http://dbpedia.org/ontology/Country"));;

		ServiceSingleton.getInstance()
				.searchKeywords("fish movement", "http://dbpedia.org/resource/Category:Mechanics");

		// System.out.println(ServiceSingleton.getInstance().searchKeywords("web build",
		// "http://dbpedia.org/resource/Category:Industry"));
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("japan process",
		// "http://dbpedia.org/resource/Category:Industry"));

		// System.out.println(ServiceSingleton.getInstance().searchKeywords("market reaction",
		// "http://dbpedia.org/resource/Category:Chemistry"));
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("flame extinguisher",
		// "http://dbpedia.org/resource/Category:Chemistry"));
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("water transfer",
		// "http://dbpedia.org/resource/Category:Chemistry"));
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("bubble size",
		// "http://dbpedia.org/resource/Category:Chemistry"));

	}
}
