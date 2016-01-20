///////////////////////////////////////////////////////////////////////////////
// File: RankedProperty.java
// Author: Carlos Bobed
// Date: 10 July 2014
// Version: 0.1
// Comments: Class that stores the information of a ranked property, 
// 		it can be extended depending on the information used for the ranking 
// 		process
// Modifications: 
///////////////////////////////////////////////////////////////////////////////

package sid.VOXII.propertyRanking;

public class RankedProperty implements Comparable<RankedProperty> {

  String propertyURI = "";
  double rankValue = 0.0;

  public RankedProperty(String propertyURI) {
    this.propertyURI = propertyURI;
    this.rankValue = 0.0;
  }

  public String getPropertyURI() {
    return propertyURI;
  }

  public void setPropertyURI(String propertyURI) {
    this.propertyURI = propertyURI;
  }

  public double getRankValue() {
    return rankValue;
  }

  public void setRankValue(double rankValue) {
    this.rankValue = rankValue;
  }

  public int compareTo(RankedProperty o) {
    if (this.rankValue >= o.getRankValue())
      return -1;
    else
      return 1;
  }

}
