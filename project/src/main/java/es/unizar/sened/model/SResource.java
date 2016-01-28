package es.unizar.sened.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 * @author gesteban@unizar.es
 */
public class SResource {

  private String _URI;
  private Map<String, Set<String>> _propertyMap;

  public SResource(String URI) {
    _URI = URI;
    _propertyMap = new HashMap<String, Set<String>>();
  }

  public String getURI() {
    return _URI;
  }

  public void add(String key, String value) {
    Set<String> values;
    if ((values = _propertyMap.get(key)) == null) {
      values = new HashSet<String>();
      values.add(value);
      _propertyMap.put(key, values);
    } else {
      values.add(value);
      _propertyMap.put(key, values);
    }
  }

  public Set<String> keySet() {
    return _propertyMap.keySet();
  }

  public Set<String> get(String key) {
    return (Set<String>) _propertyMap.get(key);
  }

  public String toString() {
    ToStringHelper stringHelper = MoreObjects.toStringHelper(SResource.class);
    for (String key : _propertyMap.keySet()) {
      stringHelper.add(key, _propertyMap.get(key));
    }
    return stringHelper.toString();
  }

}
