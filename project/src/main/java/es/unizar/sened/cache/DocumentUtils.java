package es.unizar.sened.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;

import es.unizar.sened.model.SResource;

/**
 * @author gesteban@unizar.es
 */
public class DocumentUtils {

	public static final String TAG = DocumentUtils.class.getSimpleName();

	public static Document createKeywordDocument(String keyWord, String category) {
		Document doc = new Document();
		doc.add(new StringField(Fields.KEYWORD, keyWord, Field.Store.YES));
		doc.add(new StringField(Fields.KW_CATEGORY, category, Field.Store.YES));
		doc.add(new StringField(Fields.LAST_USE, String.valueOf(new Date()
				.getTime()), Field.Store.YES));
		return doc;
	}

	public static Document createResourceDocument(SResource article,
			String categoryName) {
		Document doc = new Document();
		doc.add(new StringField(Fields.URI, article.getURI(), Field.Store.YES));
		for (String key : article.keySet()) {
			for (String value : article.get(key)) {
				doc.add(new TextField(key, value, Field.Store.YES));
			}
		}
		doc.add(new StringField(Fields.CATEGORY, categoryName, Field.Store.YES));
		doc.add(new StringField(Fields.COUNT, "1", Field.Store.YES));
		return doc;
	}

	public static Set<Document> createResourceDocuments(
			Set<SResource> articleSet, String categoryName) {
		Set<Document> documentSet = new HashSet<Document>();
		for (SResource art : articleSet) {
			documentSet.add(createResourceDocument(art, categoryName));
		}
		return documentSet;
	}

	public static List<SResource> documentsToResources(List<Document> docList) {
		List<SResource> resources = new ArrayList<SResource>();
		for (Document doc : docList) {
			SResource res = new SResource(doc.get(Fields.URI));
			for (IndexableField prop : doc.getFields()) {
				if (!prop.name().equals("count")) {
					for (IndexableField value : doc.getFields(prop.name())) {
						res.add(prop.name(), value.stringValue());
					}
				}
			}
			resources.add(res);
		}
		return resources;
	}

}
