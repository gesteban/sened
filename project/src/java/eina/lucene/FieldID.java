package eina.lucene;


/**
 * Ésta clase enumera los identificadores de los Field que se usarán en los 
 * documentos de Lucene.
 * @author Guillermo Esteban
 */
public enum FieldID {
    
    // Fields de los documentos tipo recurso
    URI ("URI"),
    ABSTRACT ("abstract"),
    CATEGORY ("category"),
    COUNT ("count"),
    
    // Fields para los documentos tipo keyword
    KEYWORD ("keyword"),
    KW_CATEGORY ("cat"),
    LAST_USE ("last_use");
    
    private final String name;
    
    FieldID(String x) {
        this.name = x;
    }
    
    @Override
    public String toString () {
        return name;
    }
    
}
