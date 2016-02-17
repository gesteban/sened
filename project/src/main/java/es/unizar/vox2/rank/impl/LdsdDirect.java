package es.unizar.vox2.rank.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

import es.unizar.sened.utils.Utils;
import es.unizar.vox2.rank.RankedResource;
import es.unizar.vox2.rank.ResourceRanker;

public class LdsdDirect extends ResourceRanker {

  @Override
  public List<? extends RankedResource> rankResources(Model model, Set<String> resources, String initialResource) {
    List<RankedResource> rankedResources = new ArrayList<>();
    for (String resource : resources) {
      double distance = Utils.semanticDistance_Direct(model, resource, initialResource);
      rankedResources.add(new RankedResource(resource, 1.0 - distance));
    }
    Collections.sort(rankedResources);
    return rankedResources;
  }

}
