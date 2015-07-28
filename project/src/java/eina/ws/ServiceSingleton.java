package eina.ws;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hp.hpl.jena.sparql.resultset.ResultSetException;
import eina.category.CategoryVocabulary;
import eina.jena.JQuery;
import eina.jena.JQueryFactory;
import eina.jena.JQueryFactoryConfig;
import eina.jena.JResult;
import eina.lucene.LuceneIndex;
import eina.ontology.OntologyManager;
import eina.ontology.beans.Article;
import eina.ontology.beans.Resource;
import eina.ontology.beans.SKOSCategory;
import eina.ontology.propertypath.PropertyPath;
import eina.ontology.propertypath.PropertyPathSet;
import eina.utils.Log;
import eina.utils.XMLutil;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * @author Guillermo Esteban
 */
public class ServiceSingleton {

//    public final String ENDPOINT = "http://dbpedia.org/sparql";
    public final String ENDPOINT = "http://horus.cps.unizar.es:8890/sparql";
    public final String INITIAL_SKOS_CATEGORY = "http://dbpedia.org/resource/Category:Mechanics";
//    public final String INITIAL_SKOS_CATEGORY = "http://dbpedia.org/resource/Category:Chemistry";
//    public final String INITIAL_SKOS_CATEGORY = "http://dbpedia.org/resource/Category:Industry";
    public final int SKOS_TAXONOMY_DEEP = 3;
    public final int QUERY_DEEP = 3;

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
            return Objects.toStringHelper(KeywordAndResource.class).add("keyword", keyword).add("resource", resource).toString();
        }
    }
    // Cache for dbpedia keyword queries
    private final LoadingCache<KeywordAndResource, JResult> requestsCache =
            CacheBuilder.newBuilder().maximumSize(100).build(new CacheLoader<KeywordAndResource, JResult>() {

        @Override
        public JResult load(KeywordAndResource kAndC) throws Exception {
            JQuery query;
            if (om.getTaxonomyType().equals(om.TAXONOMY_CLASSES)) {
                query = qf.getKeywordQuery_ClassTaxonomy(kAndC.getKeyword(), kAndC.getResource().getURI());
            } else if (om.getTaxonomyType().equals(om.TAXONOMY_CATEGORIES)) {
//                query = qf.getKeywordQuery_SKOSTaxonomy(kAndC.getKeyword(), kAndC.getResource().getURI(), QUERY_DEEP);
                query = qf.getKeywordQuery_SKOSTaxonomy(kAndC.getKeyword(), kAndC.getResource().getURI());
            } else {
                return null;
            }
            Log.getLog().info("<CacheLoader:load> Load invocation, performing search of " + kAndC.toString());
            return query.doQuery();

        }
    });
    private LuceneIndex li;
    public CategoryVocabulary categoryVocabulary;
    private JQueryFactory qf;
    private OntologyManager om = OntologyManager.getInstance();

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
            Log.getLog().error("<ServiceSingleton> Error al instanciar ServiceSingleton");
            ex.printStackTrace();
        }
    }

    ///////////////////////////////////////////////
    ///////////////////////////////////////////////
    private void init(boolean loadPreviousLuceneIndex) throws IOException {

        Set<SKOSCategory> skosCategoriesToAvoid = new HashSet<SKOSCategory>();
//        skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/Category:Industrial_automation"));
//        skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/Category:Industrial_computing"));
//        skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Industry_museums"));
//        skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Industrial_parks"));
//        skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Industry_in_the_Arctic"));
//        skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Labor_disputes_by_industry"));
//        skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Industry_in_Ancient_Rome"));
//        skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Industrial_history"));
//        skosCategoriesToAvoid.add(new SKOSCategory("http://dbpedia.org/resource/category:Bibliographies_of_industry"));

        Set<String> stringFilterSet = new HashSet<String>();
//        stringFilterSet.add("_in_");
//        stringFilterSet.add("_by_");
//        stringFilterSet.add("mitsubishi");
//        stringFilterSet.add("mining");
//        stringFilterSet.add("museum");
//        stringFilterSet.add("people");
//        stringFilterSet.add("automotive");
//        stringFilterSet.add("anime");
//        stringFilterSet.add("park");
//        stringFilterSet.add("buildings");

        // Creamos indice Lucene
        li = new LuceneIndex();
        if (loadPreviousLuceneIndex) {
            li.load();
        }

        // Creamos el QueryFactory
//        Map<String, String> datasetMap = new HashMap<String, String>();
//        datasetMap.put("dcterms:subject", "skos:subject");
//        JQueryFactoryConfig qfc = new JQueryFactoryConfig("http://dbpedia.linkeddata.es:8898/sparql", datasetMap);
        JQueryFactoryConfig qfc = new JQueryFactoryConfig(ENDPOINT);
        qf = new JQueryFactory(qfc);

        // Creamos el vocabulario de categorías
        categoryVocabulary = new CategoryVocabulary();
        SKOSCategory root = new SKOSCategory(INITIAL_SKOS_CATEGORY);
        String vocabularyName = "voc" + root.getLabel() + SKOS_TAXONOMY_DEEP;
        String vocabularyPath = System.getProperty("user.dir").replace("\\", "/");
        vocabularyPath = "file:/" + vocabularyPath + "/" + vocabularyName + ".rdf";
        Log.getLog().debug("<Service:init> Buscando vocabulario local: " + vocabularyPath);
        if (!categoryVocabulary.load(java.net.URI.create(vocabularyPath))) {
            // Si no existe el vocabulario que queremos, lo creamos
            SKOSCategory.visited = skosCategoriesToAvoid;
            root.explore(SKOS_TAXONOMY_DEEP, qf, stringFilterSet);
            categoryVocabulary.create(vocabularyPath);
            categoryVocabulary.addCategory(root, true);
            categoryVocabulary.save(java.net.URI.create(vocabularyPath));
            Log.getLog().info("<Service:init> Vocabulario [" + vocabularyName + "] creado");
        } else {
            Log.getLog().info("<Service:init> Vocabulario [" + vocabularyName + "] cargado");
        }
        System.out.println("-------------- " + categoryVocabulary.getAllCategories().size() + " categorias --------------");
        ///////////////
//        OWLClass tipo = om.getOWLDataFactory().getOWLClass(IRI.create(
//                "file:/c:/users/peonza/documents/proyecto/subsets_merged.owl#Articulo"));
//        Set<OWLEntity> queryAbout = om.getSubsetExternal(om.getDataPropertiesByDomain(tipo));


    }

    public Set<SKOSCategory> getRootCategories() {
        Log.getLog().info("<ServiceSingleton:getRootCategories> Llamada GET_ROOT_CATS");
        return categoryVocabulary.getRootCategories();
    }

    public Set<SKOSCategory> getSubCategories(String rootCategoryName) {
        Log.getLog().info("<ServiceSingleton:getSubCategories> Llamada GET_SUBCATS de " + rootCategoryName);
        Set<SKOSCategory> categorySet = new HashSet<SKOSCategory>();
        for (SKOSCategory aux : categoryVocabulary.getNarrowerCategories(
                new SKOSCategory(SKOSCategory.PREFIX + rootCategoryName))) {
            categorySet.add(aux);
        }
        return categorySet;
    }

    public String searchRelated(String URI) throws ResultSetException {

        OWLClass sourceClass = getInternalClass(URI);

        // Ahora nos dispondremos a obtener todas las clases a las que podemos acceder
        // partiendo de sourceClass, que son:
        // [rango de todas las ObjectProperty que tengan a sourceClass como dominio] + 
        // [dominio de todas las ObjectProperty que tengan a sourceClass como rango]
        Set<OWLClass> classSet = new HashSet<OWLClass>();
        Set<OWLEntity> entitySet = om.getSubsetInternal(om.getObjectPropertiesByDomain(sourceClass));
        Set<OWLObjectProperty> objectPropertySet = new HashSet<OWLObjectProperty>();
        for (OWLEntity ent : entitySet) {
            objectPropertySet.add(ent.asOWLObjectProperty());
        }
        entitySet = om.getSubsetInternal(om.getObjectPropertyRange(objectPropertySet));
        for (OWLEntity ent : entitySet) {
            classSet.add(ent.asOWLClass());
        }
        entitySet = om.getSubsetInternal(om.getObjectPropertiesByRange(sourceClass));
        objectPropertySet = new HashSet<OWLObjectProperty>();
        for (OWLEntity ent : entitySet) {
            objectPropertySet.add(ent.asOWLObjectProperty());
        }
        entitySet = om.getSubsetInternal(om.getObjectPropertyDomain(objectPropertySet));
        for (OWLEntity ent : entitySet) {
            classSet.add(ent.asOWLClass());
        }

        // Una vez tenemos todas las posibles clases destino, creamos un PropertyPath 
        // (que será o no vacío) por cada una de ellas
        Log.getLog().debug("<Service:searchRelated> Creando PropertyPathSet a partir del tipo " + sourceClass.getIRI());
        PropertyPathSet pps = new PropertyPathSet();
        for (OWLClass targetClass : classSet) {
            try {
                pps.addAll(PropertyPath.createSimplePaths(sourceClass, targetClass));
            } catch (Exception ex) {
                Log.getLog().warning("<Service:searchRelated> PropertyPath creation exception (size=0)");
            }
        }

        return doPropertyPathSetQuery(URI, pps);
    }

    public String searchRelatedWithSource(String URI, String sourceType) throws ResultSetException {

        // A continuación extraeremos la OWLClass del String de entrada targetType
        OWLClass sourceClass = null;
        for (OWLClass clase : om.getClasses()) {
            if (clase.getIRI().getFragment().equals(sourceType)) {
                sourceClass = clase;
                break;
            }
        }
        if (sourceClass == null) {// Si no hay hit, falla la búsqueda
            Log.getLog().error("<Service:searchRelatedWithSource> Sin hit en targetClass");
            return null;
        }

        // Ahora nos dispondremos a obtener todas las clases a las que podemos acceder
        // partiendo de sourceClass, que son:
        // [rango de todas las ObjectProperty que tengan a sourceClass como dominio] + 
        // [dominio de todas las ObjectProperty que tengan a sourceClass como rango]
        Set<OWLEntity> entitySet = om.getSubsetInternal(om.getObjectPropertiesByDomain(sourceClass));
        Set<OWLObjectProperty> objectPropertySet = new HashSet<OWLObjectProperty>();
        for (OWLEntity ent : entitySet) {
            objectPropertySet.add(ent.asOWLObjectProperty());
        }
        entitySet = om.getSubsetInternal(om.getObjectPropertyRange(objectPropertySet));
        Set<OWLClass> classSet = new HashSet<OWLClass>();
        for (OWLEntity ent : entitySet) {
            classSet.add(ent.asOWLClass());
        }
        entitySet = om.getSubsetInternal(om.getObjectPropertiesByRange(sourceClass));
        objectPropertySet = new HashSet<OWLObjectProperty>();
        for (OWLEntity ent : entitySet) {
            objectPropertySet.add(ent.asOWLObjectProperty());
        }
        entitySet = om.getSubsetInternal(om.getObjectPropertyDomain(objectPropertySet));
        for (OWLEntity ent : entitySet) {
            classSet.add(ent.asOWLClass());
        }

        // Una vez tenemos todas las posibles clases destino, creamos un PropertyPath 
        // (que será o no vacío) por cada una de ellas
        Log.getLog().debug("<Service:searchRelatedWithSource> Creando PropertyPathSet a partir del tipo " + sourceClass.getIRI());
        PropertyPathSet pps = new PropertyPathSet();
        for (OWLClass targetClass : classSet) {
            try {
                pps.addAll(PropertyPath.createSimplePaths(sourceClass, targetClass));
            } catch (Exception ex) {
                Log.getLog().warning("<Service:searchRelatedWithSource> PropertyPath creation exception (size=0)");
            }
        }

        return doPropertyPathSetQuery(URI, pps);
    }

    public String searchRelatedWithTarget(String URI, String targetType) throws ResultSetException {

        OWLClass sourceClass = getInternalClass(URI);

        // A continuación extraeremos la OWLClass del String de entrada targetType
        OWLClass targetClass = null;
        for (OWLClass clase : om.getClasses()) {
            if (clase.getIRI().getFragment().equals(targetType)) {
                targetClass = clase;
                break;
            }
        }
        if (targetClass == null) {// Si no hay hit, falla la búsqueda
            Log.getLog().error("<Service:searchRelatedWithTarget> Sin hit en targetClass");
            return null;
        }

        // Ahora creamos un PropertyPathSet con el único PropertyPath que tenemos y nos interesa
        PropertyPathSet pps = new PropertyPathSet();
        pps.addAll(PropertyPath.createSimplePaths(sourceClass, targetClass));

        return doPropertyPathSetQuery(URI, pps);
    }

    private OWLClass getInternalClass(String URI) throws ResultSetException {

        // Primero tendremos que ir a la DBPedia a ver que rdf:type tiene
        Log.getLog().debug("<Service:searchRelated> Se va a proceder a buscar el tipo interno de: " + URI);
        JQuery query = qf.getTypeQuery(URI);
        JResult result = query.doQuery();

        // Con los datos de los rdf:type extraídos de la DBPedia se averigua a qué clase
        // interna pertenece la URI
        OWLClass sourceClass = null;
        if (result.asSimpleColumn().isEmpty()) {
            // Estamos frente a un tipo #Articulo tal y como se define en nuestra ontología
            sourceClass = om.getOWLDataFactory().getOWLClass(IRI.create(
                    "file:/c:/users/peonza/documents/proyecto/subsets_merged.owl#Articulo"));
        } else {
            outerloop:
            for (String str : result.asSimpleColumn()) {
                for (OWLEntity clase : om.getSubsetExternal(om.getClasses())) {
                    if (clase.getIRI().toString().equals(str)) {
                        Set<OWLEntity> classSet = om.getSubsetInternal(om.getClassEquivalents(clase.asOWLClass()));
                        if (classSet.size() != 1 && !str.equals("http://www.w3.org/2002/07/owl#Thing")) {
                            Log.getLog().error("<Service:searchRelated> Fallo en la cantidad de equivalencias encontradas");
                        } else if (classSet.size() == 1) {
                            sourceClass = om.getOWLDataFactory().getOWLClass(classSet.iterator().next().getIRI());
                            break outerloop; // al primer acierto, salimos del loop
                        }
                    }
                }
            }
        }
        if (sourceClass == null) {
            Log.getLog().error("<Service:searchRelated> Error buscando el tipo del recurso " + URI);
            Log.getLog().error("<Service:searchRelated> El recurso no se encuentro en nuestra ontología");
        } else {
            Log.getLog().info("<Service:searchRelated> Recurso con tipo interno ["
                    + sourceClass.getIRI().getFragment() + "]");
        }

        return sourceClass;
    }

    private String doPropertyPathSetQuery(String URI, PropertyPathSet pps) {

        // Mapa que usaremos para almacenar los pares buscados
        HashMap<String, JResult> map = new HashMap<String, JResult>();

        // Una vez obtenemos los PropertyPath(s), los usaremos para extraer más información de la URI
        // por medio del constructor de JQuery
        Log.getLog().info("<Service:searchRelated> Se va a crear y ejecutar una búsqueda por cada PropertyPath ("
                + pps.size() + ")");
        int j = 0;
        for (PropertyPath pp : pps) {
            JQuery query = qf.getPropertyPathQuery(URI, pp);
            JResult result = query.doQuery();
            Log.getLog().info("<Service:doPropertyPathSetQuery> Búsqueda de PropertyPathSet[" + j + "] tiene "
                    + result.getResultSize() + " resultados de tipo " + pp.getTargetClass().getIRI().getFragment());

            // Print results
            ///////////////////////////////////////////////////////
//            for (int i = 0; i < result.getResultSize(); i++) {
//                System.out.print("\t");
//                for (String str : result.getRow(i)) {
//                    System.out.print(str + " - ");
//                }
//                System.out.println("");
//            }
            ///////////////////////////////////////////////////////

            j++;
            map.put(pp.getTargetClass().getIRI().getFragment(), result);
        }

//        return salida;
        return XMLutil.resultsSetsToXml(map);

    }

    public void borra() throws IOException {
        li.deleteLastUsedKeywordSearch();
        li.save();

    }

    public void string() {
        System.out.println(li.toString());
    }

    public String searchKeywords(String keywords, String resourceURI) throws Exception {
        if (om.getTaxonomyType().equals(om.TAXONOMY_CLASSES)) {
            return searchKeywords(keywords, new eina.ontology.beans.Class(resourceURI));
        } else if (om.getTaxonomyType().equals(om.TAXONOMY_CATEGORIES)) {
            return searchKeywords(keywords, new SKOSCategory(resourceURI));
        } else {
            return null;
        }
    }

    public String searchKeywords(String keywords, Resource categoryOrClass) throws Exception {
        // Dividimos el string de entrada en una lista de strings
        Set<String> listaKeys = new HashSet<String>();
        listaKeys.addAll(Arrays.asList(keywords.split(" ")));
        // Por cada keyword que tenemos
        for (String kw : listaKeys) {
            // Miramos si los resultados de su búsqueda están ya almacenadosen Lucene
            if (!li.existsKeywordDocument(kw, categoryOrClass.getName())) {
                // Recogemos resultado de cache (o no)
                JResult result = requestsCache.get(new KeywordAndResource(kw, categoryOrClass));
                // Y se almacenan los resultados en Lucene
                li.add(kw, result.asArticleSet(), categoryOrClass.getName());
            }
        }
        for (String aux : listaKeys) {
            if (!li.existsKeywordDocument(aux, categoryOrClass.getName())) {
                // No deberia entrar aqui, porque en el for de arriba se supone que hemos
                // añadido ya todos los keywordDocument a partir de li.add
                System.out.println("<Service:searchKeywords> ERROR GRAVE -- Missing keyword document/s");
            }
        }

        for (Article art : li.searchKeywords(listaKeys, categoryOrClass.getName())) {
            System.out.println(art);
        }
        return null;
//        String output = XMLutil.articleListToXml(li.searchKeywords(listaKeys, categoryOrClass.getName()));
//        return output;
    }

    public static void main(String[] args) throws Exception {
//        System.out.println(ServiceSingleton.getInstance().searchKeywords("cat", "http://dbpedia.org/resource/Category:Mechanics"));
//        System.out.println(ServiceSingleton.getInstance().searchKeywords("senegal", "http://dbpedia.org/ontology/Country"));;

        System.out.println(ServiceSingleton.getInstance().searchKeywords("fish movement", "http://dbpedia.org/resource/Category:Mechanics"));;
        
//        System.out.println(ServiceSingleton.getInstance().searchKeywords("web build", "http://dbpedia.org/resource/Category:Industry"));
//        System.out.println(ServiceSingleton.getInstance().searchKeywords("japan process", "http://dbpedia.org/resource/Category:Industry"));

//        System.out.println(ServiceSingleton.getInstance().searchKeywords("market reaction", "http://dbpedia.org/resource/Category:Chemistry"));
//        System.out.println(ServiceSingleton.getInstance().searchKeywords("flame extinguisher", "http://dbpedia.org/resource/Category:Chemistry"));
//        System.out.println(ServiceSingleton.getInstance().searchKeywords("water transfer", "http://dbpedia.org/resource/Category:Chemistry"));
//        System.out.println(ServiceSingleton.getInstance().searchKeywords("bubble size", "http://dbpedia.org/resource/Category:Chemistry"));





    }
}
