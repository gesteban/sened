package es.unizar.sened.test;

import java.util.List;

import es.unizar.sened.Sened;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.Utils;
import es.unizar.vox2.rank.RankedResource;
import es.unizar.vox2.rank.prop.PropertyRanker;
import es.unizar.vox2.rank.res.ResourceRanker;

public class TestMe {

  public static void main(String[] args) throws Exception {
    Log.i("test", "start");

    // Keyword search by CATEGORY taxonomy (NOT RANKING)
    testSearchByKeyword_CategoryTaxonomy(test1_2);

    // Keyword search by CLASS taxonomy (NOT WORKING)
    // testSearchByKeyword_ClassTaxonomy(test2_1);

    // Related search (WORKING)
    // testSearchByRelated(test3_2);

    Log.i("test", "done");
  }

  static String test1_1[] = { "cat", "http://dbpedia.org/resource/Category:Mechanics" };
  static String test1_2[] = { "web build", "http://dbpedia.org/resource/Category:Mechanics" };
  static String test1_3[] = { "japan process", "http://dbpedia.org/resource/Category:Industry" };
  static String test1_4[] = { "market reaction", "http://dbpedia.org/resource/Category:Industry" };
  static String test1_5[] = { "flame extinguisher", "http://dbpedia.org/resource/Category:Chemistry" };
  static String test1_6[] = { "water transfer", "http://dbpedia.org/resource/Category:Chemistry" };
  static String test1_7[] = { "bubble size", "http://dbpedia.org/resource/Category:Chemistry" };
  static String test1_8[] = { "cancer", "http://dbpedia.org/resource/Category:Oncology" };

  static String test2_1[] = { "senegal", "http://dbpedia.org/ontology/Country" };

  static String test3_1 = "http://dbpedia.org/resource/Albert_Einstein";
  static String test3_2 = "http://dbpedia.org/resource/Falling_cat_problem";
  static String test3_3 = "http://dbpedia.org/resource/Elsa_Einstein";
  static String test3_4 = "http://dbpedia.org/resource/United_States";

  public static void testSearchByKeyword_CategoryTaxonomy(String[] keywordsAndCategory) throws Exception {
    Sened.getInstance().searchKeyword(keywordsAndCategory[0], keywordsAndCategory[1]);
  }

  public static void testSearchByKeyword_ClassTaxonomy(String[] keywordsAndClass) throws Exception {
    Sened.getInstance().searchKeyword(keywordsAndClass[0], keywordsAndClass[1]);
  }

  public static void testSearchByRelated(String resource) throws Exception {
    Log.i("test" + "/testSearchByRelated", "Searching related information of [" + resource + "]");

    // get object properties
    List<? extends RankedResource> rankedProps = Sened.getInstance().getObjectProperties(resource,
        PropertyRanker.INSTANCE_NUMBER_RANKING_BIDIR_DEPTH_1);
    Utils.printRank(PropertyRanker.INSTANCE_NUMBER_RANKING_BIDIR_DEPTH_1, rankedProps);

    // get objects of first ranked property
    List<? extends RankedResource> rankedRes = Sened.getInstance().getObjectsOfProperty(resource,
        rankedProps.get(0).getResourceUri(), ResourceRanker.DIRECT_DISTANCE);
    Utils.printRank(ResourceRanker.DIRECT_DISTANCE, rankedRes);

  }

}
