package es.unizar.sened.test;

import es.unizar.sened.Sened;
import es.unizar.sened.utils.Log;
import es.unizar.sened.utils.SerializationUtils;

public class TestMe {

	public static void main(String[] args) throws Exception {
		Log.i("test", "start");

		// Keyword search by CATEGORY taxonomy (WORKING)
		// TODO check language response
		testSearchByKeywordUsingCategoryTaxonomy();

		// Keyword search by CLASS taxonomy (NOT WORKING)
		// testSearchByKeywordUsingClassTaxonomy();

		// Related search (NOT WORKING)
		// testSearchByRelated();

		// meh
		// ServiceSingleton.getInstance().searchRelated(
		// "http://dbpedia.org/resource/Falling_cat_problem");

		Log.i("test", "done");
	}

	public static void testSearchByKeywordUsingCategoryTaxonomy()
			throws Exception {
		Log.i("test", SerializationUtils.toString(Sened.getInstance()
				.searchKeyword("cat",
						"http://dbpedia.org/resource/Category:Mechanics")));
		// ServiceSingleton.getInstance()
		// .searchKeywords("fish movement",
		// "http://dbpedia.org/resource/Category:Mechanics");
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("web build",
		// "http://dbpedia.org/resource/Category:Industry"));
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("japan process",
		// "http://dbpedia.org/resource/Category:Industry"));
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("market reaction",
		// "http://dbpedia.org/resource/Category:Chemistry"));
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("flame extinguisher",
		// "http://dbpedia.org/resource/Category:Chemistry"));
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("water transfer",
		// "http://dbpedia.org/resource/Category:Chemistry"));
		// System.out.println(ServiceSingleton.getInstance().searchKeywords("bubble size",
		// "http://dbpedia.org/resource/Category:Chemistry"));
	}

	public static void testSearchByKeywordUsingClassTaxonomy() throws Exception {
		Sened.getInstance().searchKeyword("senegal",
				"http://dbpedia.org/ontology/Country");
	}

	public static void testSearchByRelated() throws Exception {
		// ServiceSingleton.getInstance().searchRelated("http://dbpedia.org/resource/Albert_Einstein");
		Sened.getInstance().searchRelated(
				"http://dbpedia.org/resource/Falling_cat_problem");
	}

}
