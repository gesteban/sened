package eina.lucene;

import eina.ontology.beans.Article;
import eina.utils.Log;
import java.util.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

/**
 *
 * @author Guillermo Esteban
 */
public class DocumentFactory {

    public DocumentFactory() {
    }

    /**
     * Crea un Document del tipo que almacenan las keyword de las búsquedas sobre la
     * DBPedia cuyos resultados están en el índice Lucene.
     * <p>
     * Con el documento se añade también la fecha en formato long por razones de mantenimiento.
     * @param keyWord Palabra clave de la búsqueda ya almacenada
     * @param category Categoria sobre la que se hizo la búsqueda
     * @return Documento tipo KEYWORD
     */
    public static Document createKeywordDocument(String keyWord, String category) {
        Document doc = new Document();
        doc.add(new Field(FieldID.KEYWORD.toString(), keyWord, Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field(FieldID.KW_CATEGORY.toString(), category, Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field(FieldID.LAST_USE.toString(), String.valueOf(new Date().getTime()), Field.Store.YES, Field.Index.NO));
        return doc;
    }

//    /**
//     * Crea un documento referente a un recurso de la DBPedia para su uso en Lucene.
//     * @param URI URI del recurso
//     * @param label Etiqueta del recurso (rdfs:label)
//     * @param description Descripción del recurso (dbo:abstract)
//     * @param category Categoría desde la cual hemos realizado la búsqueda que nos ha
//     * dado éste recurso como resultado
//     * @param type Tipo del recurso obtenido See {@link utils.Constants}
//     * @return Documento de un recurso de la DBPedia
//     */
//    public static Document createResourceDocument(String URI, Set<String> labels,
//            Set<String> abstracts, String webpage, String categoryName) {
//        Document doc = new Document();
//        doc.add(new Field(FieldID.URI.toString(), URI, Field.Store.YES, Field.Index.ANALYZED));
//        for (String str : labels) {
//            doc.add(new Field(FieldID.LABEL.toString(), str.substring(0, str.length() - 3),
//                    Field.Store.YES, Field.Index.ANALYZED));
//        }
//        for (String str : abstracts) {
//            doc.add(new Field(FieldID.ABSTRACT.toString(), str.substring(0, str.length() - 3),
//                    Field.Store.YES, Field.Index.ANALYZED));
//        }
//        doc.add(new Field(FieldID.WEB.toString(), webpage, Field.Store.YES, Field.Index.ANALYZED));
//        doc.add(new Field(FieldID.CATEGORY.toString(), categoryName, Field.Store.YES, Field.Index.ANALYZED));
//        doc.add(new Field(FieldID.COUNT.toString(), "1", Field.Store.YES, Field.Index.NO));
//        return doc;
//    }
    
    public static Document createResourceDocument(Article article, String categoryName) {
        Document doc = new Document();
        doc.add(new Field(FieldID.URI.toString(), article.getURI(), Field.Store.YES, Field.Index.ANALYZED));
        for(String key : article.keySet()) {
            for(String value : article.get(key)) {
                doc.add(new Field(key, value, Field.Store.YES, Field.Index.ANALYZED));
            }
        }
        doc.add(new Field(FieldID.CATEGORY.toString(), categoryName, Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field(FieldID.COUNT.toString(), "1", Field.Store.YES, Field.Index.NO));
        return doc;
    }

//    public static Set<Document> createResourceDocuments(Set<Articulo> articleSet, String categoryName) {
//        Set<Document> documentSet = new HashSet<Document>();
//        for (Articulo art : articleSet) {
//            documentSet.add(createResourceDocument(art.getURI(), art.getLabels(),
//                    art.getAbstracts(), art.getWeb(), categoryName));
//        }
//        return documentSet;
//    }
    
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
            Article art = new Article(doc.get(FieldID.URI.toString()));
            for(Fieldable prop : doc.getFields()) {
                if(!prop.name().equals("count")) {
                    for(Fieldable value : doc.getFieldables(prop.name())) {
                        art.add(prop.name(), value.stringValue());
                    }
                }
            }
            if(!articleSet.add(art))
                // No debería saltar éste error, porque los documentos que vienen se suponen
                // que no existen con URIs repetidas
                Log.getLog().error("<DocumentFactory:documentsToArticles> No debería saltar este error");
        }
        return articleSet;
    }
    
//    public static List<Articulo> documentsToArticles(List<Document> docList) {
//        List<Articulo> articleSet = new ArrayList<Articulo>();
//        for (Document doc : docList) {
//            Set<String> labels = new HashSet<String>();
//            for(Fieldable field : doc.getFieldables(FieldID.LABEL.toString()))
//                labels.add(field.stringValue());
//            Set<String> abstracts = new HashSet<String>();
//            for(Fieldable field : doc.getFieldables(FieldID.ABSTRACT.toString()))
//                abstracts.add(field.stringValue());
//            if(!articleSet.add(new Articulo(doc.get(FieldID.URI.toString()), labels,
//                    abstracts, doc.get(FieldID.WEB.toString()))))
//                // No debería saltar éste error, porque los documentos que vienen se suponen
//                // que no existen con URIs repetidas
//                Log.getLog().error("<DocumentFactory:documentToArticle> No debería saltar este error");
//        }
//        return articleSet;
//    }
    
    

}
