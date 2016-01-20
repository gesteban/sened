///////////////////////////////////////////////////////////////////////////////
// File: InformationExtractor.java
// Author: Carlos Bobed
// Date: 3 July 14
// Version: 0.1
// Comments: Abstract class that defines the methods and (possibly) the fields
// 	of a generic extractor that obtains information related to a given resource
// 	out from a given sparql endpoint
// Modifications: 
///////////////////////////////////////////////////////////////////////////////

package sid.VOXII.informationExtraction;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Model;

public abstract class InformationExtractor {

  /**
   * 
   * Method that retrieves the information related to a resource given. The information depth depends on the
   * implementation. The only requirement is that all the information must be returned in the form of an RDF model.
   * 
   * Besides, the returned model can be extended with adhoc information about statistics of the retrieval process. Each
   * of the classes that extend this one should specify it in their javadocs.
   * 
   * @param uriResource
   *          URI of the resource
   * @param relevantProperties
   *          ArrayList with the URIs of the properties that are marked as relevant/browsable in the domain ontology
   * @param sparqlEndpoint
   *          URI of the sparql endpoint used to extract the information
   * @return
   */

  public abstract Model retrieveResourceInformation(String uriResource, ArrayList<String> relevantProperties,
      String sparqlEndpoint);

}
