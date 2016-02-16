package es.unizar.sened.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;

import es.unizar.sened.utils.Utils;
import sid.VOXII.propertyRanking.PropertyRanker;
import sid.VOXII.propertyRanking.RankedProperty;

public class SenedResource {

  private String _resourceUri;
  private Model _model;

  public SenedResource(String resourceUri, Model model) {
    _resourceUri = resourceUri;
    _model = model;
  }

  public String getURI() {
    return _resourceUri;
  }

  /**
   * @param rankType
   *          from {@link PropertyRankerFactory}
   */
  public List<? extends RankedProperty> getObjectProperties(String rankType) {
    Set<String> definedProperties = new HashSet<>();
    for (OntProperty prop : DomainOntology.getObjectProperties())
      definedProperties.add(prop.getURI());
    PropertyRanker propRanker = PropertyRanker.create(rankType);
    return propRanker == null ? null : propRanker.rankDefinedObjectProperties(_model, definedProperties, _resourceUri);
  }

  public Set<String> getObjectsOfProperty(String propertyUri) {
    Set<String> resultSet = new HashSet<>();
    for (NodeIterator iter = _model.listObjectsOfProperty(Utils.createResource(_resourceUri),
        Utils.createProperty(propertyUri)); iter.hasNext();) {
      RDFNode node = iter.next();
      if (node.isURIResource())
        resultSet.add(node.asResource().getURI());
    }
    return resultSet;
  }

}
