///////////////////////////////////////////////////////////////////////////////
// File: PrunedDescribeDepth2IExtractor.java
// Author: Carlos Bobed
// Date: 3 July 14
// Version: 0.1
// Comments: Class that extends the InformationExtractor abstract class using 
// 	the following steps to extract the information: 
// Modifications: 
///////////////////////////////////////////////////////////////////////////////
package sid.VOXII.informationExtraction.implementations;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import sid.VOXII.informationExtraction.InformationExtractor;

public class PrunedDescribeDepth2IExtractor extends InformationExtractor {

  public static final String PROPERTY_NUM = "http://sid.cps.unizar.es/VOXII/numProperty";

  @Override
  public Model retrieveResourceInformation(String uriResource, ArrayList<String> relevantProperties,
      String sparqlEndpoint) {

    Model model = null;
    int numberOfExecutedQueries = 0;

    // First query is aimed at describing the given resource.
    Query describeResourceQuery = QueryFactory.create("DESCRIBE <" + uriResource + ">");
    QueryExecution qExec = QueryExecutionFactory.sparqlService(sparqlEndpoint, describeResourceQuery);
    System.out.printf("Executing DESCRIBE <" + uriResource + "> ... ");
    try {
      QueryEngineHTTP qehttp = (QueryEngineHTTP) qExec;
      qehttp.setModelContentType(WebContent.contentTypeJSONLD);
      model = qehttp.execDescribe();
      qehttp.close();
      qExec.close();
    } finally {
      qExec.close();
    }
    System.out.printf("Done\n");

    if (model != null && !model.isEmpty()) {
      numberOfExecutedQueries++;
      Resource relevantRes = model.getResource(uriResource);
      HashSet<RDFNode> relevantHarvestedResources = new HashSet<RDFNode>();

      // Harvest information about the resources that are related
      // to the main resource via relevant object properties.
      for (String prop : relevantProperties) {
        Property relevantProp = model.getProperty(prop);
        NodeIterator nIterator = model.listObjectsOfProperty(relevantRes, relevantProp);
        while (nIterator.hasNext()) {
          relevantHarvestedResources.add(nIterator.next());
        }
        ResIterator rIterator = model.listResourcesWithProperty(relevantProp, relevantRes);
        while (rIterator.hasNext()) {
          relevantHarvestedResources.add(rIterator.next());
        }
      }
      // In this version, the rest of resources are not consulted/described
      // as they are out of the scope of our domain.

      for (RDFNode node : relevantHarvestedResources) {
        numberOfExecutedQueries++;
        String auxQuery = "DESCRIBE <" + node.asResource().getURI() + ">";
        // Treat the special chars in the query before providing Jena with
        // it this is an adhoc approxj just with test purposes.
        auxQuery = auxQuery.replaceAll("[á|Á]", "a");
        auxQuery = auxQuery.replaceAll("[é|É]", "e");
        auxQuery = auxQuery.replaceAll("[í|Í]", "i");
        auxQuery = auxQuery.replaceAll("[ó|Ó]", "o");
        auxQuery = auxQuery.replaceAll("[ú|Ú]", "u");
        describeResourceQuery = QueryFactory.create(auxQuery);
        qExec = QueryExecutionFactory.sparqlService(sparqlEndpoint, describeResourceQuery);
        System.out.printf("Executing " + auxQuery + " ... ");
        try {
          QueryEngineHTTP qehttp = (QueryEngineHTTP) qExec;
          qehttp.setModelContentType(WebContent.contentTypeJSONLD);
          qehttp.execDescribe(model);
          qehttp.close();
          qExec.close();
        } finally {
          qExec.close();
        }
        System.out.printf("Done\n");
      }
      // Add the information about how many queries have been actually performed.
      Property numProp = model.createProperty(PROPERTY_NUM);
      model.add(model.createStatement(relevantRes, numProp, String.valueOf(numberOfExecutedQueries)));

    }
    return model;
  }

}
