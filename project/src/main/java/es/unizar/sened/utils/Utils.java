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

import es.unizar.sened.query.SQuery;
import es.unizar.sened.query.SQueryFactory;
import es.unizar.sened.query.SQueryResult;
import es.unizar.vox2.rank.RankedResource;
import es.unizar.vox2.rank.prop.impl.InstanceNumberRankedProperty;

public class Utils {

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
    Log.i(TAG + "/printRank", "Rank of resources according to " + rankName);
    for (int i = 0; i < rankedResources.size(); i++) {
      RankedResource rankedResource = rankedResources.get(i);
      StringBuilder str = new StringBuilder();
      str.append("  " + i + "# <" + rankedResource.getResourceUri());
      str.append(">  value:" + rankedResource.getRankValue());
      if (rankedResource instanceof InstanceNumberRankedProperty)
        str.append("  instances:" + ((InstanceNumberRankedProperty) rankedResource).getNumberOfInstances());
      Log.i(TAG + "/printRank", str.toString());
    }
  }

  private static final String TAG = Utils.class.getSimpleName();
  private static OntModel _factory;

}
