package es.unizar.sened.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 * @author gesteban@unizar.es
 */
public class SResource extends Resource {

    private Map<String, Set<String>> propertyMap;

    public SResource(String stringURI) {
        super(stringURI);
        propertyMap = new HashMap<String, Set<String>>();
    }

    public void add(String key, String value) {
        Set<String> values;
        if ((values = propertyMap.get(key)) == null) {
            values = new HashSet<String>();
            values.add(value);
            propertyMap.put(key, values);
        } else {
            values.add(value);
            propertyMap.put(key, values);
        }
    }

    public Set<String> keySet() {
        return propertyMap.keySet();
    }

    public Set<String> get(String key) {
        return (Set<String>) propertyMap.get(key);
    }

    @Override
    public String getName() {
        return getURI().toString()
                .substring(getURI().toString().lastIndexOf("/"));
    }

    public String toString() {
        ToStringHelper stringHelper = MoreObjects
                .toStringHelper(SResource.class);
        for (String key : propertyMap.keySet()) {
            stringHelper.add(key, propertyMap.get(key));
        }
        return stringHelper.toString();
    }

}
