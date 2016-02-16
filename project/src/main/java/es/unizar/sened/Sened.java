package es.unizar.sened;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import sid.VOXII.propertyRanking.PropertyRanker;
import sid.VOXII.propertyRanking.RankedProperty;
import sid.VOXII.propertyRanking.implementations.InstanceNumberRankedProperty;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import es.unizar.sened.index.LuceneIndex;
import es.unizar.sened.model.DomainOntology;
import es.unizar.sened.model.PropAndDir;
import es.unizar.sened.model.SResource;
import es.unizar.sened.model.SenedResource;
import es.unizar.sened.query.SQuery;
import es.unizar.sened.query.SQueryFactory;
import es.unizar.sened.query.SQueryResult;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.Utils;

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
  private Model _dataFactory = ModelFactory.createDefaultModel();
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

  public SenedResource searchRelated(String resourceUri) throws Exception {
    Log.i(TAG + "/searchRelated", "Searching related information of [" + resourceUri + "]");

    // sparql describe of resource
    Model resultModel = _tdb.getDepth2(resourceUri);

    // ranking object properties
    SenedResource res = new SenedResource(resourceUri, resultModel);
    Utils.printResource(res, PropertyRanker.INSTANCE_NUMBER_RANKING_BIDIR_DEPTH_1);

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
    // }
    // }
    // } else {
    // stmtSet.addAll(resultModel.listStatements(null, prop.prop, resource).toSet());
    // }
    // }
    // for (Statement stmt : stmtSet)
    // Log.d("<<>>", stmt.toString());
    // Log.d("<<>>", "size = " + stmtSet.size());

    return res;
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
