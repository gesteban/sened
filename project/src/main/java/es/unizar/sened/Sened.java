package es.unizar.sened;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import es.unizar.sened.index.LuceneIndex;
import es.unizar.sened.model.DomainOntology;
import es.unizar.sened.model.SResource;
import es.unizar.sened.query.SQuery;
import es.unizar.sened.query.SQueryFactory;
import es.unizar.sened.query.SQueryResult;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.Utils;
import es.unizar.vox2.rank.PropertyRanker;
import es.unizar.vox2.rank.RankedResource;
import es.unizar.vox2.rank.ResourceRanker;

/**
 * @author gesteban@unizar.es
 */
public class Sened {

  private static final String TAG = Sened.class.getSimpleName();

  public static final boolean LOAD_LUCENE_INDEX = true;

  public static final String REMOTE_ENDPOINT = "http://dbpedia.org/sparql";
  public static final String LANGUAGE = "en";
  public static final int QUERY_DEEP = 2;

  private LuceneIndex _li;
  private SQueryFactory _remoteQuery;
  private TdbProxy _tdb;

  private final LoadingCache<QueryParameters, SQueryResult> _resultCache = CacheBuilder.newBuilder().maximumSize(100)
      .build(new CacheLoader<QueryParameters, SQueryResult>() {
        @Override
        public SQueryResult load(QueryParameters params) throws Exception {
          SQuery query = null;
          if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CLASSES))
            throw new NotImplementedException("class taxonomy not implemented yet");
          else if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CATEGORIES))
            query = _remoteQuery.getKeywordQuery_CategoryTaxonomy(params.getKeyword(), params.getResource().getURI(),
                QUERY_DEEP);
          System.out.println(query);
          Log.d(TAG + "/LoadingCache", "Performing search of " + params.toString());
          return query == null ? null : query.doSelect();
        }
      });

  private static class ServiceSingletonHolder {
    public static final Sened instance = new Sened();
  }

  public static Sened getInstance() {
    return ServiceSingletonHolder.instance;
  }

  private Sened() {
    try {
      _li = new LuceneIndex();
      if (LOAD_LUCENE_INDEX)
        _li.load();
      _remoteQuery = new SQueryFactory(REMOTE_ENDPOINT);
      _tdb = new TdbProxy(_remoteQuery);

    } catch (IOException ex) {
      Log.e(TAG, "error creating service singleton");
      ex.printStackTrace();
    }
  }

  public void maintenance() throws IOException {
    _li.deleteLastUsedKeywordSearch();
    _li.save();
  }

  public List<SResource> searchKeyword(String keywords, String uri) throws Exception {
    if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CLASSES))
      throw new NotImplemented("class taxonomy type not implemented yet");
    else if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CATEGORIES))
      return searchKeyword_CategoryTaxonomy(keywords, Utils.createResource(uri));

    throw new Exception("no valid taxonomy type found");
  }

  public List<SResource> searchKeyword_CategoryTaxonomy(String keywords, org.apache.jena.rdf.model.Resource category)
      throws Exception {
    // parsing keywords string, each keyword is separated by a space
    Set<String> keywordSet = new HashSet<String>();
    keywordSet.addAll(Arrays.asList(keywords.split(" ")));
    for (String keyword : keywordSet)
      // looking for already stored results
      if (!_li.existsKeywordDocument(keyword, category.getLocalName())) {
        // if it does not exist, perform a SPARQL query
        SQueryResult result = _resultCache.get(new QueryParameters(keyword, category));
        System.out.println(result.toString());
        _li.add(keyword, result.asArticleSet(), category.getLocalName());
      }

    return _li.searchKeywords(keywordSet, category.getLocalName());
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
      Utils.printRank(rankType, rankedProperties);
      return rankedProperties;
    }
  }

  public List<? extends RankedResource> getObjectsOfProperty(String resourceUri, String propertyUri, String rankType)
      throws ExecutionException {
    Model model = _tdb.get(resourceUri, 2);
    Set<String> resourceSet = new HashSet<>();
    for (NodeIterator iter = model.listObjectsOfProperty(Utils.createResource(resourceUri),
        Utils.createProperty(propertyUri)); iter.hasNext();) {
      RDFNode node = iter.next();
      if (node.isURIResource())
        resourceSet.add(node.asResource().getURI());
    }
    ResourceRanker resRanker = ResourceRanker.create(rankType);
    if (resRanker == null)
      return null;
    else {
      List<? extends RankedResource> rankedResources = resRanker.rankResources(model, resourceSet, resourceUri);
      Utils.printRank(rankType, rankedResources);
      return rankedResources;
    }
  }

  private void someCode(String resourceUri) throws Exception {

    /*               */
    /*               */
    /*               */

    // get relevant properties of the resource using domain ontology
    // Set<PropAndDir> propSetAux = new HashSet<>();
    // Set<PropAndDir> propSet = new HashSet<>();
    // for (OntClass aClass : getTypes(resourceURI)) {
    // propSetAux = DomainOntology.getObjectProperties(aClass);
    // if (propSetAux.size() > 0) {
    // propSet.addAll(propSetAux);
    // Log.d(TAG, "<searchRelated> Properties related with [" + aClass.toString() + "] : " + propSet.size() + "");
    // for (PropAndDir propAndDir : propSetAux)
    // Log.d(TAG, "<searchRelated> \t" + propAndDir.toString());
    // }
    // }

    // extracting all statements using relevant properties
    // Resource resource = _dataFactory.createResource(resourceURI);
    // Set<Statement> stmtSet = new HashSet<>();
    // for (PropAndDir prop : propSet) {
    // if (prop.straight) {
    // // TODO delete specific language on properties (use general setting)
    // String language = DomainOntology.getQueryLanguage(prop.prop);
    // for (StmtIterator stmtIter = resultModel.listStatements(resource, prop.prop, (RDFNode) null); stmtIter
    // .hasNext();) {
    // Statement stmt = stmtIter.next();
    // if (language == null || (language != null && language.equals(stmt.getLanguage()))) {
    // stmtSet.add(stmt);
    // } // }
    // } else {
    // stmtSet.addAll(resultModel.listStatements(null, prop.prop, resource).toSet());
    // }
    // }
    // for (Statement stmt : stmtSet)
    // Log.d("<<>>", stmt.toString());
    // Log.d("<<>>", "size = " + stmtSet.size());
  }

  private class QueryParameters {

    private final String keyword;
    private final org.apache.jena.rdf.model.Resource resource;

    public org.apache.jena.rdf.model.Resource getResource() {
      return resource;
    }

    public String getKeyword() {
      return keyword;
    }

    public QueryParameters(String keyword, org.apache.jena.rdf.model.Resource resource) {
      this.keyword = Preconditions.checkNotNull(keyword, "keyword cannot be null");
      this.resource = Preconditions.checkNotNull(resource, "resource cannot be null");
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final QueryParameters other = (QueryParameters) obj;
      if (!this.keyword.equals(other.keyword)) {
        return false;
      }
      if (!this.resource.equals(other.resource)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int hash = 5;
      hash = 89 * hash + this.keyword.hashCode();
      hash = 89 * hash + this.resource.hashCode();
      return hash;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(QueryParameters.class).add("keyword", keyword).add("resource", resource)
          .toString();
    }
  }

}
