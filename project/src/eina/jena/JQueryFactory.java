package eina.jena;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLRuntimeException;

import eina.ontology.OntologyManager;
import eina.ontology.beans.SKOSCategory;
import eina.ontology.propertypath.DirectionalProperty;
import eina.ontology.propertypath.PropertyPath;
import eina.utils.Log;
import eina.utils.PropAndDir;
import eina.ws.ServiceSingleton;

/**
 *
 * @author Guillermo Esteban
 */
public class JQueryFactory {

    OntologyManager om = OntologyManager.getInstance();
    private static String QF_SUBCATEGORIES =
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
            + "SELECT ?subcat \n"
            + "WHERE { \n"
            + "  ?subcat skos:broader <#categoryURI> \n}";
    private static String QF_TYPE =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
            + "SELECT DISTINCT ?type \n"
            + "WHERE { \n"
            + "  {<#uri> rdf:type ?type} \n"
            + "  UNION \n"
            + "  {  <#uri> rdf:type ?xtype \n"
            + "   . ?xtype rdfs:subClassOf ?type \n"
            + "  } \n"
            + "}";
    private JQueryFactoryConfig config;

    public JQueryFactory(JQueryFactoryConfig config) {
        this.config = config;
    }

    public JQueryFactory() {
        this.config = new JQueryFactoryConfig();
    }

    /**
     * Consulta básica para las búsquedas por keyword <p> Del {@link JResult}
     * resultante se deberá invocar {@link JResult#asArticleSet()}. <p> Si el
     * parámetro es menos que 1 se ignorará y se utilizará 1 como profundidad en
     * la búsqueda.
     *
     * @param keyword Palabra a buscar en la DBPedia
     * @param categoryURI URI de la categoría raíz sobre la que se basará la
     * @param categoryDeep profundidad a la que se explorará por las categorías
     * para obtener más resultados búsqueda
     * @return un {@link JQuery} con la consulta apropiada
     */
    public JQuery getKeywordQuery_SKOSTaxonomy(String keyword, String categoryURI, int categoryDeep) {
        int i;
        // Primero debemos buscar los Data Properties que se extraerán en la búsqueda
        // Como en nuestro caso siempre extraremos artículos, buscamos los Data Properties
        // de éstos
        OWLClass tipo = om.getOWLDataFactory().getOWLClass(IRI.create(
                "file:/c:/users/peonza/documents/proyecto/subsets_merged.owl#Articulo"));

        Set<PropAndDir> queryAbout = PropAndDir.getPropertiesAndDirectionFrom(tipo);

        // Vamos montando la consulta paso a paso
        String queryString =
                "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
                + "SELECT DISTINCT ?uri";
        // La nueva elimina el @en y transforma todo en string
        for (PropAndDir propAndDir : queryAbout) {
//            queryString += " (xsd:string(str(?" + propAndDir.prop.getIRI().getFragment() + 
//                    ")) AS ?" + propAndDir.prop.getIRI().getFragment() + ")";
            queryString += "?" + propAndDir.prop.getIRI().getFragment();
        }
        queryString += " {\n";

        
        
        // Ponemos el filtro de categorias
        queryString += "{"
                // prof 0
                + " { ?uri dcterms:subject <#categoryURI>  }"
                // prof 1
                + " UNION { ?uri dcterms:subject ?subcat1. "
                + " ?subcat1 skos:broader <#categoryURI>.  }"
                // prof 2
                + " UNION { ?uri dcterms:subject ?subcat2. "
                + " ?subcat2_1  skos:broader <#categoryURI>.  "
                + " ?subcat2 skos:broader ?subcat2_1 }	"
                // PROF 3
                + " UNION { ?uri dcterms:subject ?subcat3. "
                + " ?subcat3_1  skos:broader <#categoryURI>.  "
                + " ?subcat3_2 skos:broader ?subcat3_1 .  "
                + " ?subcat3  skos:broader ?subcat3_2 } "
                //
                + "}";
//        queryString += "  OPTIONAL { ?subcat1 skos:broader <#categoryURI> } \n";
//        i = 1;
//        while (i < categoryDeep) {
//            i++;
//            queryString += ". OPTIONAL { ?subcat" + i + " skos:broader ?subcat" + (i - 1) + " } \n";
//        }
//        i = 1;
//        queryString += ".  { ?uri dcterms:subject <#categoryURI> } \n";
//        queryString += "   UNION { ?uri dcterms:subject ?subcat1 } \n";
//        while (i < categoryDeep) {
//            i++;
//            queryString += "   UNION { ?uri dcterms:subject ?subcat" + i + " } \n";
//        }
        queryString = queryString.replace("#categoryURI", categoryURI);

        // Y ahora el de los campos
        int count = 0;
        for (PropAndDir propAndDir : queryAbout) {
            if (count != 0) {
                queryString += ".";
            }

            if (propAndDir.wantValue) {
                queryString += " ?uri " + propAndDir.prop.getIRI().toQuotedString() + " ?" + propAndDir.prop.getIRI().getFragment() + "\n";
            } else {
                queryString += " ?" + propAndDir.prop.getIRI().getFragment() + propAndDir.prop.getIRI().toQuotedString() + " ?uri " + "\n";
            }

            try {
                String lang = om.getAnnotationQueryLanguage(propAndDir.prop.asOWLDataProperty());
                if (lang != null) {
                    queryString += "  FILTER langMatches(lang(?" + propAndDir.prop.getIRI().getFragment()
                            + "),\"" + lang + "\") \n";
                }
            } catch (OWLRuntimeException ex) {
                // NOT A DATA PROPERTY
                //System.out.println("<<<<<<< "+prop.toString()+" NOT A DATA PROPERTY");
            }
            count++;
        }

        for (PropAndDir propAndDir : queryAbout) {
            if (om.isKeywordSearchable(propAndDir.prop)) {
                queryString += "  FILTER regex(?" + propAndDir.prop.getIRI().getFragment() + ",\"[.., )(\\\\-\\\"]" + keyword + "[.., )(\\\\-\\\"]\",\"i\")\n";
            }
        }

        // Hardcoded
        queryString += "  FILTER regex(?primaryTopic,\"en.wikipedia.org\",\"i\")\n}"
                + "  ORDER BY ?uri";
        return new JQuery(queryString, config);
    }
    public JQuery getKeywordQuery_SKOSTaxonomy(String keyword, String categoryURI) {
        System.out.println("CATEGORIES SIZE = "+ ServiceSingleton.getInstance().categoryVocabulary.getAllCategories().size());
        int i;
        // Primero debemos buscar los Data Properties que se extraerán en la búsqueda
        // Como en nuestro caso siempre extraremos artículos, buscamos los Data Properties
        // de éstos
        OWLClass tipo = om.getOWLDataFactory().getOWLClass(IRI.create(
                "file:/c:/users/peonza/documents/proyecto/subsets_merged.owl#Articulo"));

        Set<PropAndDir> queryAbout = PropAndDir.getPropertiesAndDirectionFrom(tipo);

        // Vamos montando la consulta paso a paso
        String queryString =
                "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
                + "SELECT DISTINCT ?uri";
        // La nueva elimina el @en y transforma todo en string
        for (PropAndDir propAndDir : queryAbout) {
//            queryString += " (xsd:string(str(?" + propAndDir.prop.getIRI().getFragment() + 
//                    ")) AS ?" + propAndDir.prop.getIRI().getFragment() + ")";
            queryString += "?" + propAndDir.prop.getIRI().getFragment();
        }
        queryString += " {\n{\n";

        // Ponemos el filtro de categorias
        int catCount = 0;
        for(SKOSCategory cat : ServiceSingleton.getInstance().categoryVocabulary.getAllCategories()) {
            if (catCount!=0) {
                queryString += "UNION ";
            }
            queryString += "{ ?uri dcterms:subject <"+cat.getURI()+"> } \n";
            catCount++;
        }
        queryString += "}\n";
//        queryString += "  OPTIONAL { ?subcat1 skos:broader <#categoryURI> } \n";
//        i = 1;
//        while (i < categoryDeep) {
//            i++;
//            queryString += ". OPTIONAL { ?subcat" + i + " skos:broader ?subcat" + (i - 1) + " } \n";
//        }
//        i = 1;
//        queryString += ".  { ?uri dcterms:subject <#categoryURI> } \n";
//        queryString += "   UNION { ?uri dcterms:subject ?subcat1 } \n";
//        while (i < categoryDeep) {
//            i++;
//            queryString += "   UNION { ?uri dcterms:subject ?subcat" + i + " } \n";
//        }
//        queryString = queryString.replace("#categoryURI", categoryURI);

        // Y ahora el de los campos
        int count = 0;
        for (PropAndDir propAndDir : queryAbout) {
            if (count != 0) {
                queryString += ".";
            }

            if (propAndDir.wantValue) {
                queryString += " ?uri " + propAndDir.prop.getIRI().toQuotedString() + " ?" + propAndDir.prop.getIRI().getFragment() + "\n";
            } else {
                queryString += " ?" + propAndDir.prop.getIRI().getFragment() + propAndDir.prop.getIRI().toQuotedString() + " ?uri " + "\n";
            }

            try {
                String lang = om.getAnnotationQueryLanguage(propAndDir.prop.asOWLDataProperty());
                if (lang != null) {
                    queryString += "  FILTER langMatches(lang(?" + propAndDir.prop.getIRI().getFragment()
                            + "),\"" + lang + "\") \n";
                }
            } catch (OWLRuntimeException ex) {
                // NOT A DATA PROPERTY
                //System.out.println("<<<<<<< "+prop.toString()+" NOT A DATA PROPERTY");
            }
            count++;
        }

        for (PropAndDir propAndDir : queryAbout) {
            if (om.isKeywordSearchable(propAndDir.prop)) {
                queryString += "  FILTER regex(?" + propAndDir.prop.getIRI().getFragment() + ",\"[.., )(\\\\-\\\"]" + keyword + "[.., )(\\\\-\\\"]\",\"i\")\n";
            }
        }

        // Hardcoded
        queryString += "  FILTER regex(?primaryTopic,\"en.wikipedia.org\",\"i\")\n}"
                + "  ORDER BY ?uri";
        return new JQuery(queryString, config);
    }

    public JQuery getKeywordQuery_ClassTaxonomy(String keyword, String classURI) {
        OWLClass tipo = om.getOWLDataFactory().getOWLClass(IRI.create(classURI));
        Set<PropAndDir> queryAbout = PropAndDir.getPropertiesAndDirectionFrom(tipo);

        // Vamos montando la consulta paso a paso
        String queryString =
                "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
                + "SELECT DISTINCT ?uri";

        for (PropAndDir propAndDir : queryAbout) {
            queryString += " (xsd:string(str(?" + propAndDir.prop.getIRI().getFragment()
                    + ")) AS ?" + propAndDir.prop.getIRI().getFragment() + ")";
        }
        queryString += " {\n";

        // Ponemos el filtro de la clase
        queryString += " ?uri a " + tipo.toString() + " . \n";

        // Y ahora el de los campos
        int count = 0;
        for (PropAndDir propAndDir : queryAbout) {
            if (count != 0) {
                queryString += ".";
            }

            if (propAndDir.wantValue) {
                queryString += " ?uri " + propAndDir.prop.getIRI().toQuotedString() + " ?" + propAndDir.prop.getIRI().getFragment() + "\n";
            } else {
                queryString += " ?" + propAndDir.prop.getIRI().getFragment() + propAndDir.prop.getIRI().toQuotedString() + " ?uri " + "\n";
            }

            try {
                String lang = om.getAnnotationQueryLanguage(propAndDir.prop.asOWLDataProperty());
                if (lang != null) {
                    queryString += "  FILTER langMatches(lang(?" + propAndDir.prop.getIRI().getFragment()
                            + "),\"" + lang + "\") \n";
                }
            } catch (OWLRuntimeException ex) {
                // NOT A DATA PROPERTY
                Log.getLog().debug("<JQueryFactory:getKeywordQuery_ClassTaxonomy> " + propAndDir.prop.toString() + " not a data property");
            }
            count++;
        }

        for (PropAndDir propAndDir : queryAbout) {
            if (om.isKeywordSearchable(propAndDir.prop)) {
                queryString += "  FILTER regex(?" + propAndDir.prop.getIRI().getFragment() + ",\"[.., )(\\\\-\\\"]" + keyword + "[.., )(\\\\-\\\"]\",\"i\")\n";
            }

        }

        // Hardcoded
        queryString += "  FILTER regex(?primaryTopic,\"en.wikipedia.org\",\"i\")\n}"
                + "  ORDER BY ?uri";
        return new JQuery(queryString, config);
    }

    /**
     * Consulta para la construcción del árbol de categorías. <br> Del {@link JResult}
     * resultante se deberá invocar {@link JResult#asCategorySet()}
     *
     * @param categoryURI URI de la {@link SKOSCategory} a consultar
     * @return un {@link JQuery} con la consulta apropiada
     */
    public JQuery getSubCategoryQuery(String categoryURI) {
        String queryString = QF_SUBCATEGORIES;
        queryString = queryString.replace("#categoryURI", categoryURI);
        return new JQuery(queryString, config);
    }

    /**
     * Crea una {@link JQuery} a partir de una URI y un {@link PropertyPath}
     * <br>
     *
     * @param URI URI inicial de la que se desea extraer más información
     * @param pp {@link PropertyPath} que indicará el camino de propiedades a
     * seguir para extraer más información de la URI
     * @return {@link JQuery} con la consulta
     */
    public JQuery getPropertyPathQuery(String URI, PropertyPath pp) {

        // Buscamos las Data Properties que se pueden aplicar a la clase que buscamos
        OWLClass tipo = pp.getTargetClass();
        Log.getLog().debug("<JQueryFactory:getPropertyPathQuery> Creando consulta con clase destino = " + tipo.getIRI().getFragment());

        Set<PropAndDir> queryAbout = PropAndDir.getPropertiesAndDirectionFrom(tipo);

        // Extraemos las DataProperties externas que tengan como dominio la clase de la
        // que estoy buscando información
//        Set<OWLEntity> queryAbout = om.getRetreavableProperties(OWLEntity.class, om.getSubsetExternal(om.getDataPropertiesByDomain(tipo)));
//        queryAbout.addAll(om.getRetreavableProperties(OWLEntity.class, om.getSubsetExternal(om.getObjectPropertiesByDomain(tipo))));
        Log.getLog().debug("<JQueryFactory:getPropertyPathQuery> La consulta extraera " + queryAbout.size() + " Properties de la uri");
//
//        // Ahora crearemos la cadena SPARQL con la búsqueda
        String queryString = "SELECT DISTINCT ?uri";
        String regex = "";
        for (PropAndDir propAndDir : queryAbout) {
            queryString += " ((str(?" + propAndDir.prop.getIRI().getFragment()
                    + ")) AS ?" + propAndDir.prop.getIRI().getFragment() + ")";
            if (propAndDir.prop.getIRI().getFragment().equals("primaryTopic")) {
                regex = "  FILTER regex(?primaryTopic,\"en.wikipedia.org\",\"i\")";
            }
        }
        queryString += " {\n";
        // Primero aplicamos el PropertyPath
        DirectionalProperty dp = pp.get(0);
        if (dp.straight) {
            queryString += "  <#uri> " + dp.property.toString() + " ?uri .\n";
        } else {
            queryString += "  ?uri " + dp.property.toString() + " <#uri> .\n";
        }

        // Y ahora el de los campos
        int count = 0;
        for (PropAndDir propAndDir : queryAbout) {
            if (count != 0) {
                queryString += ".";
            }

            if (propAndDir.wantValue) {
                queryString += " ?uri " + propAndDir.prop.getIRI().toQuotedString() + " ?" + propAndDir.prop.getIRI().getFragment() + "\n";
            } else {
                queryString += " ?" + propAndDir.prop.getIRI().getFragment() + propAndDir.prop.getIRI().toQuotedString() + " ?uri " + "\n";
            }

            try {
                String lang = om.getAnnotationQueryLanguage(propAndDir.prop.asOWLDataProperty());
                if (lang != null) {
                    queryString += "  FILTER langMatches(lang(?" + propAndDir.prop.getIRI().getFragment()
                            + "),\"" + lang + "\") \n";
                }
            } catch (OWLRuntimeException ex) {
                // NOT A DATA PROPERTY
                //System.out.println("<<<<<<< "+prop.toString()+" NOT A DATA PROPERTY");
            }
            count++;
        }

        // Hardcoded
        queryString += regex + "\n}  ORDER BY ?uri";;
//        // A continuación hacemos pattern sobre los datos que queremos extraer
//        for (OWLEntity ent : queryAbout) {
//            queryString += "  . ?uri " + ent.getIRI().toQuotedString() + " ?"
//                    + ent.getIRI().getFragment() + "\n";
//            String lang = om.getAnnotationQueryLanguage(om.getOWLDataFactory().getOWLDataProperty(ent.getIRI()));
//            if (lang != null) {
//                queryString += "  FILTER langMatches(lang(?" + ent.getIRI().getFragment()
//                        + "),\"" + lang + "\") \n";
//            }
//        }
//        // Finalmente hacemos pattern sobre el rdf:type que esperamos obtener
//        if (om.getSubsetExternal(om.getClassEquivalents(tipo)).size() == 1) {
//            String typeMatch = om.getSubsetExternal(om.getClassEquivalents(tipo)).iterator().next().getIRI().toQuotedString();
//            queryString += ". ?uri a " + typeMatch + " \n";
//        } else {
//            Log.getLog().warning("<JQueryFactory:getPropertyPathQuery> Numero de externalEquivalentClasses != 1");
//        }
//
//        queryString += "}";
        queryString = queryString.replace("#uri", URI);
        System.out.println(queryString);
        return new JQuery(queryString, config);
    }

//    public JQuery getPropertyPathQuery(String URI, PropertyPath pp) {
//        
//        // Buscamos las Data Properties que se pueden aplicar a la clase que buscamos
//        OWLClass tipo = pp.getTargetClass();
//        Log.getLog().debug("<JQueryFactory:getPropertyPathQuery> Creando consulta con clase destino = " + tipo.getIRI().getFragment());
//
//        // Extraemos las DataProperties externas que tengan como dominio la clase de la
//        // que estoy buscando información
//        Set<OWLEntity> queryAbout = om.getRetreavableProperties(OWLEntity.class, om.getSubsetExternal(om.getDataPropertiesByDomain(tipo)));
//        queryAbout.addAll(om.getRetreavableProperties(OWLEntity.class, om.getSubsetExternal(om.getObjectPropertiesByDomain(tipo))));
//        Log.getLog().debug("<JQueryFactory:getPropertyPathQuery> La consulta extraera " + queryAbout.size() + " DataProperty de la uri");
//
//        // Ahora crearemos la cadena SPARQL con la búsqueda
//        String queryString = "SELECT DISTINCT ?uri";
//        for (OWLEntity ent : queryAbout) {
//            queryString += " ?" + ent.getIRI().getFragment();
//        }
//        queryString += " {\n";
//        // Primero aplicamos el PropertyPath
//        DirectionalProperty dp = pp.get(0);
//        if (dp.straight) {
//            queryString += "  <#uri> " + dp.property.toString() + " ?uri \n";
//        } else {
//            queryString += "  ?uri " + dp.property.toString() + " <#uri> \n";
//        }
//        // A continuación hacemos pattern sobre los datos que queremos extraer
//        for (OWLEntity ent : queryAbout) {
//            queryString += "  . ?uri " + ent.getIRI().toQuotedString() + " ?"
//                    + ent.getIRI().getFragment() + "\n";
//            String lang = om.getAnnotationQueryLanguage(om.getOWLDataFactory().getOWLDataProperty(ent.getIRI()));
//            if (lang != null) {
//                queryString += "  FILTER langMatches(lang(?" + ent.getIRI().getFragment()
//                        + "),\"" + lang + "\") \n";
//            }
//        }
//        // Finalmente hacemos pattern sobre el rdf:type que esperamos obtener
//        if (om.getSubsetExternal(om.getClassEquivalents(tipo)).size() == 1) {
//            String typeMatch = om.getSubsetExternal(om.getClassEquivalents(tipo)).iterator().next().getIRI().toQuotedString();
//            queryString += ". ?uri a " + typeMatch + " \n";
//        } else {
//            Log.getLog().warning("<JQueryFactory:getPropertyPathQuery> Numero de externalEquivalentClasses != 1");
//        }
//
//        queryString += "}";
//        queryString = queryString.replace("#uri", URI);
//        return new JQuery(queryString, config);
//    }
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
        JQueryFactory jqf = new JQueryFactory();

//        System.out.println(jqf.getKeywordQuery_ClassTaxonomy("spain", "http://dbpedia.org/ontology/Country").toString());
//        System.out.println(jqf.getKeywordQuery_SKOSTaxonomy("viscosity", "http://dbpedia.org/resource/Category:Mechanics", 1));
//        Set<PropAndDir> set = jqf.getPropertiesAndDirectionFrom(jqf.om.getOWLDataFactory().getOWLClass(IRI.create("http://dbpedia.org/ontology/Country")));
//        for(PropAndDir pd : set) {
//            System.out.println(pd);
//        }
    }
}
