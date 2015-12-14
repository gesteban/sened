package es.unizar.sened.test;

import es.unizar.sened.Sened;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.SerializationUtils;

public class TestMe {

    static String test1_1[] = { "cat",
            "http://dbpedia.org/resource/Category:Mechanics" };
    static String test1_2[] = { "web build",
            "http://dbpedia.org/resource/Category:Mechanics" };
    static String test1_3[] = { "japan process",
            "http://dbpedia.org/resource/Category:Industry" };
    static String test1_4[] = { "market reaction",
            "http://dbpedia.org/resource/Category:Industry" };
    static String test1_5[] = { "flame extinguisher",
            "http://dbpedia.org/resource/Category:Chemistry" };
    static String test1_6[] = { "water transfer",
            "http://dbpedia.org/resource/Category:Chemistry" };
    static String test1_7[] = { "bubble size",
            "http://dbpedia.org/resource/Category:Chemistry" };

    static String test2_1[] = { "senegal",
            "http://dbpedia.org/ontology/Country" };

    static String test3_1 = "http://dbpedia.org/resource/Albert_Einstein";
    static String test3_2 = "http://dbpedia.org/resource/Falling_cat_problem";

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

    public static void testSearchByKeyword_CategoryTaxonomy(
            String[] keywordsAndCategory) throws Exception {
        Log.i("test",
                "\n" + SerializationUtils.toString(Sened.getInstance()
                        .searchKeyword(keywordsAndCategory[0],
                                keywordsAndCategory[1])));
    }

    public static void testSearchByKeyword_ClassTaxonomy(
            String[] keywordsAndClass) throws Exception {
        Sened.getInstance().searchKeyword(keywordsAndClass[0],
                keywordsAndClass[1]);
    }

    public static void testSearchByRelated(String resource) throws Exception {
        Sened.getInstance().searchRelated(resource);
    }

}
