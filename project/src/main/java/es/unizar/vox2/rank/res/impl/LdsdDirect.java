package es.unizar.vox2.rank.res.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

import es.unizar.sened.query.SQuery;
import es.unizar.sened.query.SQueryFactory;
import es.unizar.sened.query.SQueryResult;
import es.unizar.vox2.rank.RankedResource;
import es.unizar.vox2.rank.res.ResourceRanker;

public class LdsdDirect extends ResourceRanker {

  @Override
  public List<? extends RankedResource> rankResources(Model model, Set<String> resourceUriSet, String resourceUri) {
    List<RankedResource> rankedResources = new ArrayList<>();
    for (String resource : resourceUriSet) {
      double distance = semanticDistance_Direct(resource, resourceUri, model);
      rankedResources.add(new RankedResource(resource, 1.0 - distance));
    }
    Collections.sort(rankedResources);
    return rankedResources;
  }

  /**
   * Calculates the direct LDSD between two resources.
   * <p/>
   * It uses the approach of Alexandre Passant at 'Measuring Semantic Distance on Linking Data and Using it for
   * Resources Recommendations'. See {@link http://swl.ils.indiana.edu/files/ldrec.pdf}.
   * 
   * @param model
   * @param resourceOne
   * @param resourceTwo
   * @param keywords
   * @return a number between 0.0 and 1.0
   */
  public static double semanticDistance_Direct(String resourceOne, String resourceTwo, Model model) {
    SQueryFactory queryFact = new SQueryFactory(model);
    SQuery query = queryFact.getDirectDistanceQuery(resourceOne, resourceTwo);
    SQueryResult result = query.doSelect();
    String intString = result.asSimpleColumn().iterator().next();
    return 1.0 / (1.0 + Integer.parseInt(intString.substring(0, intString.indexOf("^"))));
  }

}
