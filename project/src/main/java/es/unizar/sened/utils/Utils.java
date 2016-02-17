package es.unizar.sened.utils;

import java.util.List;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import sid.VOXII.propertyRanking.implementations.InstanceNumberRankedProperty;
import es.unizar.sened.query.SQuery;
import es.unizar.sened.query.SQueryFactory;
import es.unizar.sened.query.SQueryResult;
import es.unizar.vox2.rank.RankedResource;

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

  public static void printRank(String rankName, List<? extends RankedResource> rankedResources) {
    Log.d(TAG + "/printRank", "Rank of resources according to " + rankName);
    for (int i = 0; i < rankedResources.size(); i++) {
      RankedResource rankedResource = rankedResources.get(i);
      StringBuilder str = new StringBuilder();
      str.append("  " + i + "# <" + rankedResource.getResourceUri());
      str.append(">  value:" + rankedResource.getRankValue());
      if (rankedResource instanceof InstanceNumberRankedProperty)
        str.append("  instances:" + ((InstanceNumberRankedProperty) rankedResource).getNumberOfInstances());
      Log.d(TAG + "/printRank", str.toString());
    }
  }

  /**
   * Calculates the direct LDSD between two resources.
   * <p/>
   * It uses the approach of Alexandre Passant at 'Measuring Semantic Distance on Linking Data and Using it for
   * Resources Recommendations'. See {@link http://swl.ils.indiana.edu/files/ldrec.pdf}.
   * 
   * @param model
   * @param resourceOne
   * @param resourceTwo
   * @param keywords
   * @return a number between 0.0 and 1.0
   */
  public static double semanticDistance_Direct(Model model, String resourceOne, String resourceTwo) {
    SQueryFactory factory = new SQueryFactory(model);
    SQuery query = factory.getDirectDistanceQuery(resourceOne, resourceTwo);
    SQueryResult result = query.doSelect();
    String intString = result.asSimpleColumn().iterator().next();
    return 1.0 / (1.0 + Integer.parseInt(intString.substring(0, intString.indexOf("^"))));
  }

}
