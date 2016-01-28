package es.unizar.sened.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;

import sid.VOXII.propertyRanking.PropertyRanker;
import sid.VOXII.propertyRanking.PropertyRankerFactory;
import sid.VOXII.propertyRanking.RankedProperty;

public class SenedResource {

  private String _URI;
  private Model _model;

  public SenedResource(String URI) {
    _URI = URI;
  }

  public String getURI() {
    return _URI;
  }

  public void addModel(Model model) {
    _model = model;
  }

  /**
   * @param rankType
   *          from {@link PropertyRankerFactory}
   */
  public List<? extends RankedProperty> getObjectProperties(String rankType) {
    Set<String> definedProperties = new HashSet<>();
    for (OntProperty prop : DomainOntology.getObjectProperties())
      definedProperties.add(prop.getURI());
    PropertyRanker propRanker = PropertyRankerFactory.getPropertyRanker(rankType);
    return propRanker == null ? null : propRanker.rankDefinedObjectProperties(_model, definedProperties, _URI);
  }
  
}
