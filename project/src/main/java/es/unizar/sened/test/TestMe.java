package es.unizar.sened.test;

import es.unizar.sened.ServiceSingleton;

public class TestMe {

	public static void main(String[] args) throws Exception {

		// Keyword search by CATEGORY taxonomy (WORKING)
		testSearchByKeywordUsingCategoryTaxonomy();

		// Keyword search by CLASS taxonomy (NOT WORKING)
		// testSearchByKeywordUsingClassTaxonomy();

		// Related search (NOT WORKING)
		// ServiceSingleton.getInstance().searchRelated("http://dbpedia.org/resource/Falling_cat_problem");

	}

	public static void testSearchByKeywordUsingCategoryTaxonomy() throws Exception {
		ServiceSingleton.getInstance().searchKeywords("cat", "http://dbpedia.org/resource/Category:Mechanics");
		// ServiceSingleton.getInstance()
		// .searchKeywords("fish movement", "http://dbpedia.org/resource/Category:Mechanics");
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
		ServiceSingleton.getInstance().searchKeywords("senegal", "http://dbpedia.org/ontology/Country");
	}
}
