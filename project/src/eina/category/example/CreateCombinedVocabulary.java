
package eina.category.example;

import eina.ontology.beans.SKOSCategory;
import eina.jena.JQueryFactory;
import eina.jena.JQueryFactoryConfig;
import eina.category.CategoryVocabulary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author Guillermo Esteban
 */
public class CreateCombinedVocabulary {

    public static void main(String[] args) {
        
        // Primero de todo, creamos un JQueryFactory para las consultas que haremos
        Map<String, String> datasetMap = new HashMap<String, String>();
        datasetMap.put("dcterms:subject", "skos:subject");
        JQueryFactoryConfig qfc = new JQueryFactoryConfig("http://dbpedia.linkeddata.es:8898/sparql", datasetMap);
        //JQueryFactoryConfig qfc = new JQueryFactoryConfig();
        JQueryFactory qf = new JQueryFactory(qfc);
        
        // De la misma manera que anteriormente creabamos un vocabulario a partir de
        // una sola categoría, también tenemos la opción de crear un sólo vocabulario
        // a partir de varias categorías.
        
        // Primero creamos y exploramos en profundidad un poco las dos categorías
        SKOSCategory cat1 = new SKOSCategory("http://dbpedia.org/resource/Category:Mechanics");
        cat1.explore(2, qf, new HashSet<String>());
        SKOSCategory cat2 = new SKOSCategory("http://dbpedia.org/resource/Category:Medicine");
        cat2.explore(2, qf, new HashSet<String>());
        
        // Ahora creamos el vocabulario de la misma manera
        CategoryVocabulary vocabulary = new CategoryVocabulary();
        vocabulary.create("CombinedVocabularyExample");
        
        // Añadimos las dos categorías
        vocabulary.addCategory(cat1, true);
        vocabulary.addCategory(cat2, true);

        // Podremos consultar de la misma manera que sobre cualquier vocabulario
        System.out.println("La categoría " + cat2.getLabel( ) + " tiene las siguientes"
                + " subcategorías:");
        for(SKOSCategory cat : vocabulary.getNarrowerCategories(cat2))
            System.out.println("\t" + cat.getLabel());
        
        // Finalmente guardamos el vocabulario creado
        String stringDir = System.getProperty("user.dir").replace("\\", "/");
        stringDir = "file:/" + stringDir + "/" + vocabulary.getURI() + ".rdf";
        vocabulary.save(java.net.URI.create(stringDir));
        
    }
}
