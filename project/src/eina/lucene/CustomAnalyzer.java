package eina.lucene;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standardd.StandardAnalyzer;
import org.apache.lucene.util.Version;

/**
 * Analizador personalizado para su uso en el {@link IndexWriterConfig}
 * necesario para realizar inserciones en {@link LuceneIndex}
 * <p>
 * Éste analizador deja pasar sin "tokenizar" todos los fields expecto 
 * aquellos con nombre {@link Constants#ABSTRACT}, a los que se les aplica 
 * un {@link StandardAnalyzer}
 * @author peonza
 */
public class CustomAnalyzer extends Analyzer {

    private Analyzer standardAnalyzer;
    private final String searchField;

    /**
     * Creates a new {@link CustomAnalyzer}
     * @param matchVersion Lucene version to match
     */
    public CustomAnalyzer(Version matchVersion, String searchField) {
        this.standardAnalyzer = new StandardAnalyzer(matchVersion);
        this.searchField = searchField;
    }
    
    public String getSearchField () {
        return searchField;
    }

    @Override
    public TokenStream tokenStream(String field, final Reader reader) {

//        if (FieldID.ABSTRACT.toString().equals(field)) {
        if (searchField.equals(field)) {
            return standardAnalyzer.tokenStream(field, reader);
        } else {
            // Do not tokenize
            return new CharTokenizer(reader) {
                @Override
                protected boolean isTokenChar(char c) {
                    return true;
                }
            };
        }
    }
}