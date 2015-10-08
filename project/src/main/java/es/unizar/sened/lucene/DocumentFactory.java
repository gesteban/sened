package es.unizar.sened.lucene;

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

import es.unizar.sened.ontology.Article;
import es.unizar.sened.utils.Log;

/**
 * @author gesteban@unizar.es
 */
public class DocumentFactory {

	public static final String TAG = DocumentFactory.class.getSimpleName();

	/**
	 * Crea un Document del tipo que almacenan las keyword de las búsquedas sobre la DBPedia cuyos resultados están en
	 * el índice Lucene.
	 * <p>
	 * Con el documento se añade también la fecha en formato long por razones de mantenimiento.
	 * 
	 * @param keyWord
	 *            Palabra clave de la búsqueda ya almacenada.
	 * @param category
	 *            Categoría sobre la que se hizo la búsqueda.
	 * @return Documento tipo KEYWORD
	 */
	public static Document createKeywordDocument(String keyWord, String category) {
		Document doc = new Document();
		doc.add(new StringField(Fields.KEYWORD, keyWord, Field.Store.YES));
		doc.add(new StringField(Fields.KW_CATEGORY, category, Field.Store.YES));
		doc.add(new StringField(Fields.LAST_USE, String.valueOf(new Date().getTime()), Field.Store.YES));
		return doc;
	}

	public static Document createResourceDocument(Article article, String categoryName) {
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

	public static Set<Document> createResourceDocuments(Set<Article> articleSet, String categoryName) {
		Set<Document> documentSet = new HashSet<Document>();
		for (Article art : articleSet) {
			documentSet.add(createResourceDocument(art, categoryName));
		}
		return documentSet;
	}

	public static List<Article> documentsToArticles(List<Document> docList) {
		List<Article> articleSet = new ArrayList<Article>();
		for (Document doc : docList) {
			Article art = new Article(doc.get(Fields.URI));
			for (IndexableField prop : doc.getFields()) {
				if (!prop.name().equals("count")) {
					for (IndexableField value : doc.getFields(prop.name())) {
						art.add(prop.name(), value.stringValue());
					}
				}
			}
			if (!articleSet.add(art))
				// No debería saltar este error, porque los documentos que vienen se suponen
				// que no existen con URIs repetidas
				Log.e(TAG, "<documentsToArticles> No debería saltar este error");
		}
		return articleSet;
	}

}
