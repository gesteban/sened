package es.unizar.sened.utils;

import java.util.List;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import es.unizar.sened.model.SenedResource;
import sid.VOXII.propertyRanking.PropertyRanker;
import sid.VOXII.propertyRanking.RankedProperty;
import sid.VOXII.propertyRanking.implementations.InstanceNumberRankedProperty;

public class Utils {

  public static final String TAG = Utils.class.getSimpleName();

  private static OntModel _factory;

  static {
    _factory = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
  }

  public static Resource createResource(String uri) {
    return ResourceFactory.createResource(uri);
  }

  public static OntClass createClass(String uri) {
    return _factory.createClass(uri);
  }

  public static Property createProperty(String uri) {
    return _factory.createProperty(uri);
  }

  public static void printResource(SenedResource resource, String propertyRank) {
    Log.d(TAG + "/printResource", "Ranked properties according to "
        + PropertyRanker.INSTANCE_NUMBER_RANKING_BIDIR_DEPTH_1);
    List<? extends RankedProperty> rankedProperties = resource.getObjectProperties(propertyRank);
    for (int i = 0; i < rankedProperties.size(); i++) {
      RankedProperty auxInfo = rankedProperties.get(i);
      StringBuilder str = new StringBuilder();
      str.append("  " + i + "# <" + auxInfo.getPropertyUri());
      str.append(">  value:" + auxInfo.getRankValue());
      if (auxInfo instanceof InstanceNumberRankedProperty)
        str.append("  instances:" + ((InstanceNumberRankedProperty) auxInfo).getNumberOfInstances());
      Log.d(TAG + "/printResource", str.toString());
    }
    for (int i = 0; i < rankedProperties.size(); i++) {
      Log.d(TAG + "/printResource", "Objects of <" + rankedProperties.get(i).getPropertyUri() + ">");
      for (String resourceUri : resource.getObjectsOfProperty(rankedProperties.get(i).getPropertyUri())) {
        Log.d(TAG + "/printResource", "  <" + resourceUri + ">");
      }
    }
  }

}
