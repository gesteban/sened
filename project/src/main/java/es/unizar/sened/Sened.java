package es.unizar.sened;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import es.unizar.sened.cache.LuceneIndex;
import es.unizar.sened.model.DomainOntology;
import es.unizar.sened.model.PropAndDir;
import es.unizar.sened.model.SResource;
import es.unizar.sened.query.SQuery;
import es.unizar.sened.query.SQueryFactory;
import es.unizar.sened.query.SQueryFactoryConfig;
import es.unizar.sened.query.SQueryResult;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.Utils;

/**
 * @author gesteban@unizar.es
 */
public class Sened {

  public static final String TAG = Sened.class.getSimpleName();
  public static final String ENDPOINT = "http://dbpedia.org/sparql";
  public static int QUERY_DEEP = 2;

  private LuceneIndex _li;
  private SQueryFactory _factory;

  private final LoadingCache<QueryParameters, SQueryResult> _queryProxy = CacheBuilder.newBuilder().maximumSize(100)
      .build(new CacheLoader<QueryParameters, SQueryResult>() {

        @Override
        public SQueryResult load(QueryParameters params) throws Exception {
          SQuery query = null;
          if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CLASSES)) {
            throw new NotImplementedException("class taxonomy not implemented yet");
          } else if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CATEGORIES)) {
            query = _factory.getKeywordQuery_CategoryTaxonomy(params.getKeyword(), params.getResource().getURI(),
                QUERY_DEEP);
          }
          Log.d(TAG, "<LoadingCache> performing search of " + params.toString());
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
      init(true);
    } catch (IOException ex) {
      Log.e(TAG, "error creating service singleton");
      ex.printStackTrace();
    }
  }

  private void init(boolean loadPreviousLuceneIndex) throws IOException {

    _li = new LuceneIndex();
    if (loadPreviousLuceneIndex) {
      _li.load();
    }

    SQueryFactoryConfig qfc = new SQueryFactoryConfig(ENDPOINT);
    _factory = new SQueryFactory(qfc);

  }

  private Set<OntClass> getTypes(String instanceURI) {
    SQuery query = _factory.getTypeQuery(instanceURI);
    SQueryResult result = query.doSelect();
    Set<OntClass> classes = new HashSet<OntClass>();
    for (String classUri : result.asSimpleColumn()) {
      classes.add(Utils.createClass(classUri));
    }
    // Returning at least the special Void class.
    if (classes.size() == 0) {
      classes.add(DomainOntology.Void);
    }
    return classes;
  }

  public void maintenance() throws IOException {
    _li.deleteLastUsedKeywordSearch();
    _li.save();
  }

  public List<SResource> searchKeyword(String keywords, String uri) throws Exception {
    if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CLASSES)) {
      throw new NotImplemented("class taxonomy type not implemented yet");
    } else if (DomainOntology.getTaxonomyType().equals(DomainOntology.TAXONOMY_CATEGORIES)) {
      return searchKeyword_CategoryTaxonomy(keywords, Utils.createResource(uri));
    }
    throw new Exception("no valid taxonomy type found");
  }

  public List<SResource> searchKeyword_CategoryTaxonomy(String keywords, org.apache.jena.rdf.model.Resource category)
      throws Exception {
    // Parsing keywords string, each keyword is separated by a space.
    Set<String> keywordSet = new HashSet<String>();
    keywordSet.addAll(Arrays.asList(keywords.split(" ")));
    for (String keyword : keywordSet) {
      // Looking for already stored results.
      if (!_li.existsKeywordDocument(keyword, category.getLocalName())) {
        // If it does not exist, perform a SPARQL query.
        SQueryResult result = _queryProxy.get(new QueryParameters(keyword, category));
        _li.add(keyword, result.asArticleSet(), category.getLocalName());
      }
    }
    return _li.searchKeywords(keywordSet, category.getLocalName());
  }

  public void searchRelated(String instanceURI) {
    // SQuery query = _factory.getDescribeQuery(instanceURI);
    // Model resultModel = query.doDescribe();
    // RDFDataMgr.write(System.out, resultModel, RDFFormat.TURTLE_PRETTY);

    // SResource resource = new SResource(resultModel.getResource("http://dbpedia.org/resource/Falling_cat_problem"));
    // senedResource.getRelevantProperties(); <<< ALGO ASI

    Set<PropAndDir> propSet = new HashSet<>();
    for (OntClass aClass : getTypes(instanceURI)) {
      propSet.addAll(DomainOntology.getRetrievableProperties(aClass));
    }
    Log.i(TAG, propSet.size()+"");
    for (PropAndDir propAndDir : propSet) {
      Log.i(TAG, propAndDir.toString());
    }

    // List<SResource> resourceList = new ArrayList<>();
    // Map<PropAndDir, List<SResource>> map = new HashMap<>();

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
