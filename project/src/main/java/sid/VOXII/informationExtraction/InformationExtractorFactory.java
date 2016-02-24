///////////////////////////////////////////////////////////////////////////////
// File: InformationExtractorFactory.java
// Author: Carlos Bobed
// Date: 3 July 14
// Version: 0.1
// Comments: Class that provides instances of different implementations of an 
// 		InformationExtractor
// Modifications: 
///////////////////////////////////////////////////////////////////////////////

package sid.VOXII.informationExtraction;

import sid.VOXII.informationExtraction.impl.PrunedDescribeDepth2IExtractor;

public class InformationExtractorFactory {

  public static final String PRUNED_DESCRIBE_DEPTH_2 = "prunedDescribe-depth2";

  public static InformationExtractor getInformationExtractor(String type) {

    InformationExtractor result = null;
    // switch (type) {
    // case PRUNED_DESCRIBE_DEPTH_2:
    // result = new PrunedDescribeDepth2IExtractor();
    // break;
    // }
    if (type.equals(PRUNED_DESCRIBE_DEPTH_2))
      result = new PrunedDescribeDepth2IExtractor();

    return result;
  }

}
