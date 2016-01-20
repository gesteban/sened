package sid.VOXII.propertyRanking.implementations;

import sid.VOXII.propertyRanking.RankedProperty;

public class RelevantInformationRankedProperty extends RankedProperty {

  int reachableResources = 0;
  double informationWeight = 0.0;

  public double getInformationWeight() {
    return informationWeight;
  }

  public void setInformationWeight(double informationWeight) {
    this.informationWeight = informationWeight;
  }

  public int getReachableResources() {
    return reachableResources;
  }

  public void setReachableResources(int reachableResources) {
    this.reachableResources = reachableResources;
  }

  public RelevantInformationRankedProperty(String property) {
    super(property);
  }

}
