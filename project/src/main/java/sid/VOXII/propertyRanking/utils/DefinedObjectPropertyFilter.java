package sid.VOXII.propertyRanking.utils;

import java.util.HashSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class DefinedObjectPropertyFilter {

  public static HashSet<String> obtainNonDefinedProperties(Model RDFModel, HashSet<String> definedObjectProperties,
      String initialResource) {
    Resource relevantResource = RDFModel.getResource(initialResource);
    HashSet<String> nonDefinedObjectProperties = new HashSet<String>();

    // we first have to retrieve the properties that are not in the
    // ontology and appear in the rdfmodel

    StmtIterator sIterator = RDFModel.listStatements(new SimpleSelector(relevantResource, null, (RDFNode) null));
    Statement rdfTriple = null;
    while (sIterator.hasNext()) {
      rdfTriple = sIterator.nextStatement();
      if (rdfTriple.getObject().isResource()
          && !(definedObjectProperties.contains(rdfTriple.getPredicate().getURI().toString()))) {
        nonDefinedObjectProperties.add(rdfTriple.getPredicate().getURI().toString());
      }
    }
    // now we turn our attention to the inbound connections
    sIterator = RDFModel.listStatements(new SimpleSelector(null, null, relevantResource));
    while (sIterator.hasNext()) {
      rdfTriple = sIterator.nextStatement();
      if (rdfTriple.getSubject().isResource()
          && !(definedObjectProperties.contains(rdfTriple.getPredicate().getURI().toString()))) {
        nonDefinedObjectProperties.add(rdfTriple.getPredicate().getURI().toString());
      }
    }

    return nonDefinedObjectProperties;
  }

}
