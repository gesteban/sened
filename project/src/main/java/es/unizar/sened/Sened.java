package es.unizar.sened;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import es.unizar.sened.model.DomainOntology;
import es.unizar.sened.query.SQueryFactory;
import es.unizar.sened.store.LuceneIndex;
import es.unizar.sened.store.TdbProxy;
import es.unizar.sened.utils.Utils;
import es.unizar.vox2.rank.RankedResource;
import es.unizar.vox2.rank.prop.PropertyRanker;
import es.unizar.vox2.rank.res.ResourceRanker;

/**
 * @author gesteban@unizar.es
 */
public class Sened {

  // TODO parameters in arguments or config file
  // public static final String REMOTE_ENDPOINT = "http://dbpedia.org/sparql";
  public static final String REMOTE_ENDPOINT = "http://localhost:8890/sparql";
  public static final String LANGUAGE = "en";
  public static final int QUERY_DEEP = 3;

  public static Sened getInstance() {
    return SingletonHolder.instance;
  }

  public List<? extends RankedResource> searchKeyword(String keywords, String categoryUri) throws Exception {
    if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CLASSES))
      throw new NotImplemented("class taxonomy type not implemented yet");
    else if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CATEGORIES))
      return searchKeyword_CategoryTaxonomy(keywords, Utils.createResource(categoryUri));
    throw new Exception("no valid taxonomy type found");
  }

  public List<? extends RankedResource> searchKeyword_CategoryTaxonomy(String keywords, Resource category)
      throws Exception {
    Set<String> keywordSet = new HashSet<String>();
    // each keyword is separated by a space
    keywordSet.addAll(Arrays.asList(keywords.split(" ")));
    Set<String> uriSet = _index.get(keywordSet, category, QUERY_DEEP);
    for (String resourceUri : uriSet) {
      _tdb.get(resourceUri, 1);
    }
    return null;
  }

  public List<? extends RankedResource> getObjectProperties(String resourceUri, String rankType)
      throws ExecutionException {
    Model model = _tdb.get(resourceUri, 2);
    Set<String> definedProperties = new HashSet<>();
    for (OntProperty prop : DomainOntology.getObjectProperties())
      definedProperties.add(prop.getURI());
    PropertyRanker propRanker = PropertyRanker.create(rankType);
    if (propRanker == null)
      return null;
    else {
      List<? extends RankedResource> rankedProperties = propRanker.rankDefinedObjectProperties(model,
          definedProperties, resourceUri);
      return rankedProperties;
    }
  }

  public List<? extends RankedResource> getObjectProperties2(String resourceUri, String rankType)
      throws ExecutionException {
    Model model = _tdb.get(resourceUri, 2);
    Set<String> definedProperties = new HashSet<>();
    for (OntProperty prop : DomainOntology.getObjectProperties())
      definedProperties.add(prop.getURI());
    PropertyRanker propRanker = PropertyRanker.create(rankType);
    if (propRanker == null)
      return null;
    else {
      List<? extends RankedResource> rankedProperties = propRanker.rankNonDefinedObjectProperties(model,
          definedProperties, resourceUri);
      return rankedProperties;
    }
  }

  public List<? extends RankedResource> getObjectsOfProperty(String resourceUri, String propertyUri, String rankType)
      throws ExecutionException {
    Model model = _tdb.get(resourceUri, 2);
    Set<String> resourceUriSet = new HashSet<>();
    for (NodeIterator iter = model.listObjectsOfProperty(Utils.createResource(resourceUri),
        Utils.createProperty(propertyUri)); iter.hasNext();) {
      RDFNode node = iter.next();
      if (node.isURIResource())
        resourceUriSet.add(node.asResource().getURI());
    }
    ResourceRanker resRanker = ResourceRanker.create(rankType);
    if (resRanker == null)
      return null;
    else {
      List<? extends RankedResource> rankedResources = resRanker.rankResources(model, resourceUriSet, resourceUri);
      return rankedResources;
    }
  }

  // private static final String TAG = Sened.class.getSimpleName();

  private LuceneIndex _index;
  private SQueryFactory _remoteQuery;
  private TdbProxy _tdb;

  private static class SingletonHolder {
    public static final Sened instance = new Sened();
  }

  private Sened() {
    _remoteQuery = new SQueryFactory(REMOTE_ENDPOINT);
    _index = LuceneIndex.load(_remoteQuery);
    _tdb = new TdbProxy(_remoteQuery);
  }

}
