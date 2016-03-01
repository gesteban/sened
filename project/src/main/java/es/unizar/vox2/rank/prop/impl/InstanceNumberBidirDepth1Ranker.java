///////////////////////////////////////////////////////////////////////////////
// File: InstanceNumberBidirDepth1Ranker.java
// Author: Carlos Bobed
// Date: 10 July 2014
// Version: 0.1
// Comments: Class that implements the simplest rank: it just counts the 
// 	instances of each of the properties in the set of relevant/nonRelevant properties, 
// 	and returns the properties sorted in descending order
// Modifications: 
///////////////////////////////////////////////////////////////////////////////

package es.unizar.vox2.rank.prop.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

import es.unizar.sened.utils.Utils;
import es.unizar.vox2.rank.prop.PropertyRanker;

public class InstanceNumberBidirDepth1Ranker extends PropertyRanker {

  public ArrayList<InstanceNumberRankedProperty> rankDefinedObjectProperties(Model model,
      Set<String> definedObjectProperties, String initialResource) {

    int totalInstances = 0;
    int numInstances = 0;
    Property relevantProperty = null;
    Resource relevantResource = model.getResource(initialResource);

    ResIterator rIterator = null;
    NodeIterator nIterator = null;

    InstanceNumberRankedProperty auxRankedProperty = null;

    ArrayList<InstanceNumberRankedProperty> result = new ArrayList<InstanceNumberRankedProperty>();

    if (relevantResource != null) {
      for (String property : definedObjectProperties) {
        // we retrieve the triples with this property in both directions
        relevantProperty = model.getProperty(property);

        if (relevantProperty != null) {
          nIterator = model.listObjectsOfProperty(relevantResource, relevantProperty);
          numInstances = nIterator.toList().size();
          rIterator = model.listResourcesWithProperty(relevantProperty, relevantResource);
          numInstances += rIterator.toList().size();
          totalInstances += numInstances;

          auxRankedProperty = new InstanceNumberRankedProperty(property);
          auxRankedProperty.setNumberOfInstances(numInstances);
          result.add(auxRankedProperty);

        }
      }

      if (totalInstances != 0) {
        for (InstanceNumberRankedProperty infoRanking : result) {
          infoRanking.setRankValue(((double) infoRanking.getNumberOfInstances()) / ((double) totalInstances));
        }
        Collections.sort(result);
      }

    }

    return result;
  }

  public ArrayList<InstanceNumberRankedProperty> rankNonDefinedObjectProperties(Model RDFModel,
      Set<String> definedObjectProperties, String initialResource) {

    int totalInstances = 0;
    int numInstances = 0;
    Resource relevantResource = RDFModel.getResource(initialResource);
    Property nonRelevantProperty = null;
    ResIterator rIterator = null;
    NodeIterator nIterator = null;

    InstanceNumberRankedProperty auxRankedProperty = null;

    ArrayList<InstanceNumberRankedProperty> result = new ArrayList<InstanceNumberRankedProperty>();

    Set<String> nonDefinedObjectProperties = new HashSet<String>();

    // we first have to retrieve the properties that are not in the
    // ontology and appear in the rdfmodel

    nonDefinedObjectProperties = Utils.obtainNonDefinedProperties(RDFModel, definedObjectProperties, initialResource);

    // we now apply the same algorithm to obtain the
    // ranking

    if (relevantResource != null) {
      for (String property : nonDefinedObjectProperties) {
        nonRelevantProperty = RDFModel.getProperty(property);
        if (nonRelevantProperty != null) {
          // we retrieve the triples with this property in both directions
          nIterator = RDFModel.listObjectsOfProperty(relevantResource, nonRelevantProperty);
          numInstances = nIterator.toList().size();
          rIterator = RDFModel.listResourcesWithProperty(nonRelevantProperty, relevantResource);
          numInstances += rIterator.toList().size();
          totalInstances += numInstances;

          auxRankedProperty = new InstanceNumberRankedProperty(nonRelevantProperty.getURI().toString());
          auxRankedProperty.setNumberOfInstances(numInstances);
          result.add(auxRankedProperty);
        }
      }

      if (totalInstances != 0) {
        for (InstanceNumberRankedProperty infoRanking : result) {
          infoRanking.setRankValue(((double) infoRanking.getNumberOfInstances()) / ((double) totalInstances));
        }
        Collections.sort(result);
      }

    }

    return result;

  }

}
