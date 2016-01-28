package es.unizar.sened.test;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

import es.unizar.sened.Sened;
import es.unizar.sened.query.SQueryFactory;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.SerializationUtils;

public class TestMe {

  static String update_string = "insert data { graph <http://testgraph.com/> "
      + "{ <http://test.com/resource> <http://test.com/property> <http://test.com/object> } }";

  public static void main(String[] args) throws Exception {
    Log.i("test", "start");

    // Keyword search by CATEGORY taxonomy (WORKING)
    // testSearchByKeyword_CategoryTaxonomy(test1_1);

    // Keyword search by CLASS taxonomy (NOT WORKING)
    // testSearchByKeyword_ClassTaxonomy(test2_1);

    // Related search (NOT WORKING)
    testSearchByRelated(test3_1);

    Log.i("test", "done");

  }

  static String test1_1[] = { "cat", "http://dbpedia.org/resource/Category:Mechanics" };
  static String test1_2[] = { "web build", "http://dbpedia.org/resource/Category:Mechanics" };
  static String test1_3[] = { "japan process", "http://dbpedia.org/resource/Category:Industry" };
  static String test1_4[] = { "market reaction", "http://dbpedia.org/resource/Category:Industry" };
  static String test1_5[] = { "flame extinguisher", "http://dbpedia.org/resource/Category:Chemistry" };
  static String test1_6[] = { "water transfer", "http://dbpedia.org/resource/Category:Chemistry" };
  static String test1_7[] = { "bubble size", "http://dbpedia.org/resource/Category:Chemistry" };

  static String test2_1[] = { "senegal", "http://dbpedia.org/ontology/Country" };

  static String test3_1 = "http://dbpedia.org/resource/Albert_Einstein";
  static String test3_2 = "http://dbpedia.org/resource/Falling_cat_problem";

  public static void testSearchByKeyword_CategoryTaxonomy(String[] keywordsAndCategory) throws Exception {
    System.out.println(SerializationUtils.toString(Sened.getInstance().searchKeyword(keywordsAndCategory[0],
        keywordsAndCategory[1])));
  }

  public static void testSearchByKeyword_ClassTaxonomy(String[] keywordsAndClass) throws Exception {
    Sened.getInstance().searchKeyword(keywordsAndClass[0], keywordsAndClass[1]);
  }

  public static void testSearchByRelated(String resource) throws Exception {
    Sened.getInstance().searchRelated(resource);
  }

  public static void testVarious() {
    // Query query = QueryFactory.create("SELECT * WHERE { ?s a ?type }");
    // QueryExecution qe = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql-auth", query);
    HttpAuthenticator authenticator = new SimpleAuthenticator("dba", "PASSWORD-HERE".toCharArray());
    QueryExecution qe = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql-auth",
        "SELECT * WHERE { ?s a ?type }", authenticator);

    Log.i("tag", qe.execSelect().getRowNumber() + "");

    // Model model = qe.execSelect().getResourceModel();
    // RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);

    // UpdateRequest update = UpdateFactory.create(update_string);
    // UpdateProcessor updateProc = UpdateExecutionFactory.createRemoteForm(update, "http://localhost:8890/sparql");
    // updateProc.execute();

    // SQueryFactory fact = new SQueryFactory("http://localhost:8890/sparql");
    // fact.getCustomQuery(update_string);
  }

}
