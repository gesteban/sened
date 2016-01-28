///////////////////////////////////////////////////////////////////////////////
// File: InstanceNumberInboundDepth1Ranker.java
// Author: Carlos Bobed
// Date: 10 July 2014
// Version: 0.1
// Comments: Class that implements the simplest rank on the inbound 
// 	edges/properties of the RDFgraph. It just counts the 
// 	inbound instances of each of the properties in the set of 
// 	relevant/nonRelevant properties, and returns the properties sorted in descending order
// Modifications: 
///////////////////////////////////////////////////////////////////////////////

package sid.VOXII.propertyRanking.implementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

import sid.VOXII.propertyRanking.PropertyRanker;
import sid.VOXII.propertyRanking.utils.DefinedObjectPropertyFilter;

public class InstanceNumberInboundDepth1Ranker extends PropertyRanker {

  public ArrayList<InstanceNumberRankedProperty> rankDefinedObjectProperties(Model RDFModel,
      Set<String> definedObjectProperties, String initialResource) {
    int totalInstances = 0;
    int numInstances = 0;
    Property relevantProperty = null;
    Resource relevantResource = RDFModel.getResource(initialResource);

    ResIterator rIterator = null;

    InstanceNumberRankedProperty auxRankedProperty = null;

    ArrayList<InstanceNumberRankedProperty> result = new ArrayList<InstanceNumberRankedProperty>();

    if (relevantResource != null) {
      for (String property : definedObjectProperties) {
        // we retrieve the triples with this property in both directions
        relevantProperty = RDFModel.getProperty(property);

        if (relevantProperty != null) {
          // we just take into account the properties that have the relevant
          // resource as subject
          rIterator = RDFModel.listResourcesWithProperty(relevantProperty, relevantResource);
          numInstances = rIterator.toList().size();
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
      HashSet<String> definedObjectProperties, String initialResource) {
    int totalInstances = 0;
    int numInstances = 0;
    Resource relevantResource = RDFModel.getResource(initialResource);
    Property nonRelevantProperty = null;
    ResIterator rIterator = null;
    NodeIterator nIterator = null;

    InstanceNumberRankedProperty auxRankedProperty = null;

    ArrayList<InstanceNumberRankedProperty> result = new ArrayList<InstanceNumberRankedProperty>();

    HashSet<String> nonDefinedObjectProperties = new HashSet<String>();

    // we first have to retrieve the properties that are not in the
    // ontology and appear in the rdfmodel

    nonDefinedObjectProperties = DefinedObjectPropertyFilter.obtainNonDefinedProperties(RDFModel,
        definedObjectProperties, initialResource);

    // we now apply the same algorithm to obtain the
    // ranking

    if (relevantResource != null) {
      for (String property : nonDefinedObjectProperties) {
        nonRelevantProperty = RDFModel.getProperty(property);
        if (nonRelevantProperty != null) {
          // we retrieve the inbound edges
          rIterator = RDFModel.listResourcesWithProperty(nonRelevantProperty, relevantResource);
          numInstances = rIterator.toList().size();
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
