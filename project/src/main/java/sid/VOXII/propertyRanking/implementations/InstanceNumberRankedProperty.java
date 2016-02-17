package sid.VOXII.propertyRanking.implementations;

import es.unizar.vox2.rank.RankedResource;

public class InstanceNumberRankedProperty extends RankedResource {

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
