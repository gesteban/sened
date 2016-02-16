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

  String _propertyUri = "";
  double _rankValue = 0.0;

  public RankedProperty(String propertyURI) {
    this._propertyUri = propertyURI;
    this._rankValue = 0.0;
  }

  public String getPropertyUri() {
    return _propertyUri;
  }

  public void setPropertyURI(String propertyURI) {
    this._propertyUri = propertyURI;
  }

  public double getRankValue() {
    return _rankValue;
  }

  public void setRankValue(double rankValue) {
    this._rankValue = rankValue;
  }

  public int compareTo(RankedProperty o) {
    if (this._rankValue >= o.getRankValue())
      return -1;
    else
      return 1;
  }

}
