
package eina.category.example;

import eina.ontology.beans.SKOSCategory;
import eina.category.CategoryVocabulary;

/**
 *
 * @author Guillermo Esteban
 */
public class LoadVocabulary {

    public static void main(String[] args) {
        
        // En el caso de carga de un vocabulario, tendremos que darle la ruta del fichero rdf
        // que almacena el vocabulario.
        CategoryVocabulary vocabulary = new CategoryVocabulary();
        String stringDir = System.getProperty("user.dir").replace("\\", "/");
        stringDir = "file:/" + stringDir + "/SimpleVocabularyExample.rdf";
        vocabulary.load(java.net.URI.create(stringDir));
        
        // Consultar sobre un vocabulario cargado es igual que consultar sobre uno creado
        // desde cero, he aquí una simple consulta, en realidad, la misma que en el ejemplo
        // de CreateSimpleVocabulary
        SKOSCategory category = new SKOSCategory("http://dbpedia.org/resource/Category:Mechanics");
        System.out.println("La categoría " + category.getLabel( ) + " tiene las siguientes"
                + " subcategorías:");
        for(SKOSCategory cat : vocabulary.getNarrowerCategories(category))
            System.out.println("\t" + cat.getLabel());

        
    }
}
