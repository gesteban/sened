///////////////////////////////////////////////////////////////////////////////
// File: PropertyRanker.java
// Author: Carlos Bobed
// Date: 10 July 14
// Version: 0.1
// Comments: Abstract class that defines the methods and (possibly) the fields
// 	of a generic ranker that works on RDF models and returns lists of 
//  properties and/or resources ranked according to different implemented 
// 	criteria
// Modifications: 
///////////////////////////////////////////////////////////////////////////////

package es.unizar.vox2.rank.prop;

import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

import es.unizar.vox2.rank.RankedResource;
import es.unizar.vox2.rank.prop.impl.InstanceNumberBidirDepth1Ranker;
import es.unizar.vox2.rank.prop.impl.InstanceNumberInboundDepth1Ranker;
import es.unizar.vox2.rank.prop.impl.InstanceNumberOutboundDepth1Ranker;
import es.unizar.vox2.rank.prop.impl.RelevantInformationDepth2Ranker;

public abstract class PropertyRanker {

  public static final String INSTANCE_NUMBER_RANKING_BIDIR_DEPTH_1 = "instanceNumberRanking-depth1";
  public static final String INSTANCE_NUMBER_RANKING_INBOUND_DEPTH_1 = "instanceNumberRankingInbound-depth1";
  public static final String INSTANCE_NUMBER_RANKING_OUTBOUND_DEPTH_1 = "instanceNumberRankingOutbound-depth1";
  public static final String RELEVANT_INFORMATION_AMOUNT_RANKING_DEPTH_2 = "relevantInformationAmountRanking-depth2";

  public static PropertyRanker create(String type) {
    PropertyRanker instance = null;
    if (type.equals(INSTANCE_NUMBER_RANKING_BIDIR_DEPTH_1))
      instance = new InstanceNumberBidirDepth1Ranker();
    else if (type.equals(INSTANCE_NUMBER_RANKING_INBOUND_DEPTH_1))
      instance = new InstanceNumberInboundDepth1Ranker();
    else if (type.equals(INSTANCE_NUMBER_RANKING_OUTBOUND_DEPTH_1))
      instance = new InstanceNumberOutboundDepth1Ranker();
    else if (type.equals(RELEVANT_INFORMATION_AMOUNT_RANKING_DEPTH_2))
      instance = new RelevantInformationDepth2Ranker();
    return instance;
  }

  /**
   * Method that ranks the relevant properties (marked in the domain ontology as so), and returns an ORDERED list with
   * such properties along with their ranking information.
   * 
   * @param model
   *          Model with the RDF-triples to be ranked
   * @param definedObjectProperties
   *          Properties that are marked in the ontology as relevant ones
   * @param initialResource
   *          Resource which we are ranking the properties for
   * @return Ordered arrayList of ranked properties
   */
  public abstract List<? extends RankedResource> rankDefinedObjectProperties(Model model,
      Set<String> definedObjectProperties, String initialResource);

  /**
   * Method that ranks the rest of the properties (properties which are not defined/marked in the domain ontology, but
   * which appear in the data model retrieved), and returns an ORDERED arraylist with such properties along with their
   * ranking information.
   * 
   * @param model
   *          Model with the RDF-triples to be ranked
   * @param definedObjectProperties
   *          Properties that are marked in the ontology as relevant ones, thus, it is used as a "black-list" filter
   * @param initialResource
   *          Resource which we are ranking the properties for
   * @return Ordered arrayList of ranked properties
   */
  public abstract List<? extends RankedResource> rankNonDefinedObjectProperties(Model model,
      Set<String> definedObjectProperties, String initialResource);

}
