package es.unizar.sened.index;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.util.CharTokenizer;

/**
 * TODO once behaviour of system fully test with StandardAnalyzer, remove CustomAnalyzer and EmptyStringTokenFilter
 * classes
 * 
 * @author gesteban@unizar.es
 */
public class CustomAnalyzer extends Analyzer {

  private final String _searchField;

  public CustomAnalyzer(String searchField) {
    _searchField = searchField;
  }

  @Override
  protected TokenStreamComponents createComponents(String field) {
    // Unique token.
    Tokenizer tokenizer = new CharTokenizer() {
      @Override
      protected boolean isTokenChar(int c) {
        return true;
      }
    };
    // Empty string and lower case filter, because why not.
    TokenStream filter = new EmptyStringTokenFilter(tokenizer);
    filter = new LowerCaseFilter(filter);
    // If field analyzed is not the abstract, we filter out the unique token.
    if (_searchField.equals(field)) {
      filter = new TokenFilter(filter) {
        @Override
        public boolean incrementToken() throws IOException {
          return false;
        }
      };
    }
    return new TokenStreamComponents(tokenizer, filter);
  }
}