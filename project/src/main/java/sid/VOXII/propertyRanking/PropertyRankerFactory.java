///////////////////////////////////////////////////////////////////////////////
// File: PropertyRankerFactory.java
// Author: Carlos Bobed
// Date: 10 July 2014
// Version: 0.1
// Comments: Class that provides instances of different implementations of an 
// 		PropertyRanker
// Modifications: 
///////////////////////////////////////////////////////////////////////////////

package sid.VOXII.propertyRanking;

import sid.VOXII.propertyRanking.implementations.InstanceNumberBidirDepth1Ranker;
import sid.VOXII.propertyRanking.implementations.InstanceNumberInboundDepth1Ranker;
import sid.VOXII.propertyRanking.implementations.InstanceNumberOutboundDepth1Ranker;
import sid.VOXII.propertyRanking.implementations.RelevantInformationDepth2Ranker;

public class PropertyRankerFactory {

  public static final String INSTANCE_NUMBER_RANKING_BIDIR_DEPTH_1 = "instanceNumberRanking-depth1";
  public static final String INSTANCE_NUMBER_RANKING_INBOUND_DEPTH_1 = "instanceNumberRankingInbound-depth1";
  public static final String INSTANCE_NUMBER_RANKING_OUTBOUND_DEPTH_1 = "instanceNumberRankingOutbound-depth1";
  public static final String RELEVANT_INFORMATION_AMOUNT_RANKING_DEPTH_2 = "relevantInformationAmountRanking-depth2";

  public static PropertyRanker getPropertyRanker(String type) {
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
}
