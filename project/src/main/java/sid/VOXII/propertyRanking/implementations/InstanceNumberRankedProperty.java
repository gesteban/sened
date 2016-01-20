package sid.VOXII.propertyRanking.implementations;

import sid.VOXII.propertyRanking.RankedProperty;

public class InstanceNumberRankedProperty extends RankedProperty {

  int numberOfInstances = 0;

  public InstanceNumberRankedProperty(String property) {
    super(property);
  }

  public int getNumberOfInstances() {
    return numberOfInstances;
  }

  public void setNumberOfInstances(int numberOfInstances) {
    this.numberOfInstances = numberOfInstances;
  }

}
