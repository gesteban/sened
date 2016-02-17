///////////////////////////////////////////////////////////////////////////////
// File: RankedResource.java
// Author: Carlos Bobed
// Date: 10 July 2014
// Version: 0.2
// Comments: Class that stores the information of a ranked resources, 
// 		it can be extended depending on the information used for the ranking 
// 		process.
// Modifications:
//    Guillermo Esteban, 16 February 2016, this class no longer refers
//       exclusively to properties, as resources can be ordered too.
///////////////////////////////////////////////////////////////////////////////

package es.unizar.vox2.rank;

public class RankedResource implements Comparable<RankedResource> {

  private String _resourceUri = "";
  private double _rankValue = 0.0;

  public RankedResource(String resourceUri) {
    _resourceUri = resourceUri;
    _rankValue = 0.0;
  }

  public RankedResource(String resourceUri, double rankValue) {
    _resourceUri = resourceUri;
    _rankValue = rankValue;
  }

  public String getResourceUri() {
    return _resourceUri;
  }

  public void setResourceUri(String resourceUri) {
    _resourceUri = resourceUri;
  }

  public double getRankValue() {
    return _rankValue;
  }

  public void setRankValue(double rankValue) {
    _rankValue = rankValue;
  }

  public int compareTo(RankedResource o) {
    if (_rankValue >= o.getRankValue())
      return -1;
    else
      return 1;
  }

}
