package es.unizar.sened.utils;

import java.util.List;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import sid.VOXII.propertyRanking.PropertyRankerFactory;
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

  public static void printRank(List<? extends RankedProperty> rankedProperties) {
    Log.d(TAG, "<printRank> " + PropertyRankerFactory.INSTANCE_NUMBER_RANKING_BIDIR_DEPTH_1);
    for (int i = 0; i < rankedProperties.size(); i++) {
      RankedProperty auxInfo = rankedProperties.get(i);
      StringBuilder str = new StringBuilder();
      str.append("  " + i + "# " + auxInfo.getPropertyURI());
      str.append("  value:" + auxInfo.getRankValue());
      if (auxInfo instanceof InstanceNumberRankedProperty)
        str.append("  instances:" + ((InstanceNumberRankedProperty) auxInfo).getNumberOfInstances());
      Log.d(TAG, str.toString());
    }
  }

}
