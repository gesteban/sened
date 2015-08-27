
package eina.ontology.beans;

import eina.ontology.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;

public class Class extends Resource {
    
    public Class (String stringURI) {
        super(stringURI);
    }
    
    public OWLClass toOWLClass () {
        return OntologyManager.getInstance().getOWLDataFactory().getOWLClass(IRI.create(uriString));
    }
    
    @Override
    public String getName() {
        return getURI();//.toString().substring(getURI().toString().lastIndexOf("/")+1);
    }
}
