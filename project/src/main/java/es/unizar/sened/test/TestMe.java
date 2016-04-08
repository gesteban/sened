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
    // testSearchByRelated(test3_4);

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
  static String test3_5 = "http://dbpedia.org/resource/Hechingen";
  static String test3_6 = "http://dbpedia.org/resource/Cancer";

  static String PROPERTY_RANK_TYPE = PropertyRanker.INSTANCE_NUMBER_RANKING_OUTBOUND_DEPTH_1;
  static String RESOURCE_RANK_TYPE = ResourceRanker.DIRECT_DISTANCE;

  public static void testSearchByKeyword_CategoryTaxonomy(String[] keywordsAndCategory) throws Exception {
    Sened.getInstance().searchKeyword(keywordsAndCategory[0], keywordsAndCategory[1]);
  }

  public static void testSearchByKeyword_ClassTaxonomy(String[] keywordsAndClass) throws Exception {
    Sened.getInstance().searchKeyword(keywordsAndClass[0], keywordsAndClass[1]);
  }

  public static void testSearchByRelated(String resource) throws Exception {
    Log.i("test" + "/testSearchByRelated", "Searching related information of [" + resource + "]");

    // get object properties
    List<? extends RankedResource> rankedProps = Sened.getInstance().getOutboundDefinedObjectProperties(resource,
        PROPERTY_RANK_TYPE);
    Utils.printRank(PROPERTY_RANK_TYPE, rankedProps);

    // get objects of first ranked property
    if (rankedProps.size() > 0) {
      List<? extends RankedResource> rankedRes = Sened.getInstance().getObjectsOfProperty(resource,
          rankedProps.get(0).getResourceUri(), RESOURCE_RANK_TYPE);
      Utils.printRank(RESOURCE_RANK_TYPE, rankedRes);
    }

  }

}
