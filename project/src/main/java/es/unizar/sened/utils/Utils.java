package es.unizar.sened.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

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

  public static Set<String> obtainNonDefinedProperties(Model RDFModel, Set<String> definedObjectProperties,
      String initialResource) {
    // TODO filter out rdf:type and 'similar' properties
    Resource relevantResource = RDFModel.getResource(initialResource);
    HashSet<String> nonDefinedObjectProperties = new HashSet<String>();
    // we first have to retrieve the properties that are not in the ontology and appear in the rdfmodel
    StmtIterator sIterator = RDFModel.listStatements(new SimpleSelector(relevantResource, null, (RDFNode) null));
    Statement rdfTriple = null;
    while (sIterator.hasNext()) {
      rdfTriple = sIterator.nextStatement();
      if (rdfTriple.getObject().isResource()
          && !(definedObjectProperties.contains(rdfTriple.getPredicate().getURI().toString())))
        nonDefinedObjectProperties.add(rdfTriple.getPredicate().getURI().toString());
    }
    // now we turn our attention to the inbound connections
    sIterator = RDFModel.listStatements(new SimpleSelector(null, null, relevantResource));
    while (sIterator.hasNext()) {
      rdfTriple = sIterator.nextStatement();
      if (rdfTriple.getSubject().isResource()
          && !(definedObjectProperties.contains(rdfTriple.getPredicate().getURI().toString()))) {
        nonDefinedObjectProperties.add(rdfTriple.getPredicate().getURI().toString());
      }
    }
    return nonDefinedObjectProperties;
  }

  private static final String TAG = Utils.class.getSimpleName();
  private static OntModel _factory;

}
