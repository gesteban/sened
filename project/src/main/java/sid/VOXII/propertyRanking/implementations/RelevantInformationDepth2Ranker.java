///////////////////////////////////////////////////////////////////////////////
// File: RelevantInformationAmountDepth2Ranker.java
// Author: Carlos Bobed
// Date: 10 July 2014
// Version: 0.1
// Comments: Class that implements a rank based on the amount of relevant 
// 		information which is accessible trough a particular property. 
// 		The information is considered to be accessible if there exists 
// 		an inbound or an outbound edge to the resource labeled with 
// 		a given property. 
// Modifications: 
///////////////////////////////////////////////////////////////////////////////
package sid.VOXII.propertyRanking.implementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

import sid.VOXII.propertyRanking.PropertyRanker;
import sid.VOXII.propertyRanking.RankedProperty;

public class RelevantInformationDepth2Ranker extends PropertyRanker {

  @Override
  public ArrayList<RelevantInformationRankedProperty> rankDefinedObjectProperties(Model model,
      HashSet<String> definedObjectProperties, String initialResource) {

    double totalWeight = 0.0;
    ArrayList<RelevantInformationRankedProperty> result = new ArrayList<RelevantInformationRankedProperty>();
    HashSet<Resource> reachableResources = new HashSet<Resource>();
    // To track down the resources whose information weigth has already been added to the global.
    HashSet<Resource> alreadyAdded = new HashSet<Resource>(); // TODO not used, necessary?

    Resource relevantResource = model.getResource(initialResource);
    if (relevantResource == null) {
      return null;
    }

    for (String property : definedObjectProperties) {

      // Check property belongs to model.
      Property relevantProperty = model.getProperty(property);
      if (relevantProperty == null) {
        continue;
      }

      // Retrieve the triples with this property in both directions.
      double propertyWeight = 0;
      NodeIterator nIterator = model.listObjectsOfProperty(relevantResource, relevantProperty);
      while (nIterator.hasNext()) {
        RDFNode auxNode = nIterator.next();
        if (auxNode.isResource()) {
          reachableResources.add(auxNode.asResource());
        }
      }
      ResIterator rIterator = model.listResourcesWithProperty(relevantProperty, relevantResource);
      reachableResources.addAll(rIterator.toSet());

      // Create ranked property object and fill weight.
      RelevantInformationRankedProperty auxRankedProperty = new RelevantInformationRankedProperty(property);
      auxRankedProperty.setReachableResources(reachableResources.size());
      for (Resource res : reachableResources) {
        double resourceWeight = obtainRelevantInformationWeight(model, definedObjectProperties, res);
        propertyWeight += resourceWeight;
        if (!alreadyAdded.contains(res)) {
          totalWeight += resourceWeight;
        }
      }
      auxRankedProperty.setInformationWeight(propertyWeight);
      result.add(auxRankedProperty);

    }

    if (totalWeight != 0.0) {
      for (RelevantInformationRankedProperty infoRanking : result) {
        infoRanking.setRankValue(infoRanking.getInformationWeight() / totalWeight);
      }
      Collections.sort(result);
    }
    return result;
  }

  @Override
  public ArrayList<RankedProperty> rankNonDefinedObjectProperties(Model RDFModel,
      HashSet<String> definedObjectProperties, String initialResource) {
    // TODO null method
    return null;
  }

  private double obtainRelevantInformationWeight(Model model, HashSet<String> definedObjectProperties, Resource res) {
    // TODO check method, maybe try different weight assign
    double result = 0.0;
    for (String property : definedObjectProperties) {
      // Retrieve the triples with this property in both directions.
      Property relevantProperty = model.getProperty(property);
      if (relevantProperty != null) {
        NodeIterator nIterator = model.listObjectsOfProperty(res, relevantProperty);
        result += nIterator.toList().size();
        ResIterator rIterator = model.listResourcesWithProperty(relevantProperty, res);
        result += rIterator.toList().size();
      }
    }
    return result;
  }

}
