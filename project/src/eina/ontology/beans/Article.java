
package eina.ontology.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Guillermo Esteban
 */
public class Article extends Resource {
    
    /**
     * Mapa que contiene las propiedades de un artículo de cualquier tipo junto con
     * los valores de estas propiedades
     */
    private Map<String,Set<String>> map;
    
    public Article (String stringURI) {
        super(stringURI);
        map = new HashMap<String,Set<String>>();
    }
    
    /**
     * 
     * @param key URI de una propiedad
     * @param value valor de una propiedad
     */
    public void add (String key, String value) {
        Set<String> values;
        if( (values=map.get(key)) == null) {
            values = new HashSet<String>();
            values.add(value);
            map.put(key,values);
        } else {
            values.add(value);
            map.put(key, values);
        }
    }
    
    public Set<String> keySet () {
        return map.keySet();
    }
    
    public Set<String> get (String key) {
        return (Set<String>)map.get(key);
    }

    @Override
    public String getName() {
        return getURI().toString().substring(getURI().toString().lastIndexOf("/"));
    }
    
}
