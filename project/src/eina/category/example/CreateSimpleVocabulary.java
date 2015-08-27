
package eina.category.example;

import eina.ontology.beans.SKOSCategory;
import eina.jena.JQueryFactory;
import eina.jena.JQueryFactoryConfig;
import eina.category.CategoryVocabulary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Ejemplo que ilustra la creación, consulta y guardado de un vocabulario de
 * categorías simple.
 * @author Guillermo Esteban
 */
public class CreateSimpleVocabulary {

    static public void main (String[] args) {
        
        // Primero de todo, creamos un JQueryFactory para las consultas que haremos
//        Map<String, String> datasetMap = new HashMap<String, String>();
//        datasetMap.put("http://purl.org/dc/terms/subject", "http://www.w3.org/2004/02/skos/core#subject");
//        JQueryFactoryConfig qfc = new JQueryFactoryConfig("http://dbpedia.linkeddata.es:8898/sparql", datasetMap);
        JQueryFactoryConfig qfc = new JQueryFactoryConfig();
        JQueryFactory qf = new JQueryFactory(qfc);
        
        // Para poder utilizar CategoryVocabulary necesitaremos generar un grafo de 
        // categorías, éste se puede generar automáticamente creando una categoría
        // existente en la DBpedia y llamando luego al método explore, que nos
        // creará el grafo de subcategorías partiendo de la dada.
        SKOSCategory root = new SKOSCategory("http://dbpedia.org/resource/Category:Mechanics");
        root.explore(3, qf, new HashSet<String>());
        
        // Ahora creamos el vocabulario, al principio no contiene nada, simplemente
        // se crea el objeto y luego se crea el vocabulario llamando a create con
        // el nombre que se le quiera dar al vocabulario (el nombre no importa de cara
        // al usuario en la versión actual, puesto que CategoryVocabulary maneja un
        // sólo SKOSDataset).
        CategoryVocabulary vocabulary = new CategoryVocabulary();
        vocabulary.create("SimpleVocabularyExample");
        
        // Ahora se le añade la categoría que hemos creado y sobre la que hemos 
        // explorado, addCategory añade las subcategorías de manera recursiva, por
        // lo que no tenemos que andar añadiendo una a una, solo la superior.
        vocabulary.addCategory(root, true);
        
        
        // Ahora podemos hacer una simple consulta sobre el vocabulario
        System.out.println("La categoría " + root.getLabel( ) + " tiene las siguientes"
                + " subcategorías:");
        for(SKOSCategory cat : vocabulary.getNarrowerCategories(root))
            System.out.println("\t" + cat.getLabel());
        
        // Ahora podremos guardar el vocabulario para su posterior uso, en éste 
        // ejemplo se guardará en la carpeta activa del proyecto con el nombre
        // del vocabulario, es decir "SimpleVocabularyExample" con tipo rdf.
        String stringDir = System.getProperty("user.dir").replace("\\", "/");
        stringDir = "file:/" + stringDir + "/" + vocabulary.getURI() + ".rdf";
        vocabulary.save(java.net.URI.create(stringDir));
        
    }
}
