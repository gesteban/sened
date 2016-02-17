package sid.VOXII.propertyRanking.implementations;

import es.unizar.vox2.rank.RankedResource;

public class RelevantInformationRankedProperty extends RankedResource {

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
