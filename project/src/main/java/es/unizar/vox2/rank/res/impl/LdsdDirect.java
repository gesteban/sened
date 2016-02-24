package es.unizar.vox2.rank.res.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import es.unizar.sened.store.TdbProxy;
import es.unizar.vox2.rank.RankedResource;
import es.unizar.vox2.rank.res.ResourceRanker;

public class LdsdDirect extends ResourceRanker {

  @Override
  public List<? extends RankedResource> rankResources(Set<String> resourceUriSet, String resourceUri, TdbProxy tdb) {
    List<RankedResource> rankedResources = new ArrayList<>();
    for (String resource : resourceUriSet) {
      double distance = tdb.semanticDistance_Direct(resource, resourceUri);
      rankedResources.add(new RankedResource(resource, 1.0 - distance));
    }
    Collections.sort(rankedResources);
    return rankedResources;
  }

}
