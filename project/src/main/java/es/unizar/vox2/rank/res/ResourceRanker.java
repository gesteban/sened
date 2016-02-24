package es.unizar.vox2.rank.res;

import java.util.List;
import java.util.Set;

import es.unizar.sened.store.TdbProxy;
import es.unizar.vox2.rank.RankedResource;
import es.unizar.vox2.rank.res.impl.LdsdDirect;

/**
 * @author gesteban@unizar.es
 */
public abstract class ResourceRanker {

  public static final String DIRECT_DISTANCE = "direct-distance";

  public static ResourceRanker create(String type) {
    ResourceRanker instance = null;
    if (type.equals(DIRECT_DISTANCE))
      instance = new LdsdDirect();
    return instance;
  }

  public abstract List<? extends RankedResource> rankResources(Set<String> resourceUriSet, String resourceUri,
      TdbProxy tdb);

}