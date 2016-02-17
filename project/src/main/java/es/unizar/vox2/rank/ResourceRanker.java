package es.unizar.vox2.rank;

import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

import es.unizar.vox2.rank.impl.LdsdDirect;

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
  
  public abstract List<? extends RankedResource> rankResources(Model model, Set<String> resources, String initialResource);

}
