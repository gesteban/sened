///////////////////////////////////////////////////////////////////////////////
// File: PropertyRanker.java
// Author: Carlos Bobed
// Date: 10 July 14
// Version: 0.1
// Comments: Abstract class that defines the methods and (possibly) the fields
// 	of a generic ranker that works on RDF models and returns lists of 
//  properties and/or resources ranked according to different implemented 
// 	criteria
// Modifications: 
///////////////////////////////////////////////////////////////////////////////

package sid.VOXII.propertyRanking;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.jena.rdf.model.Model;

public abstract class PropertyRanker {

  /**
   * 
   * Method that ranks the relevant properties (marked in the domain ontology as so), and returns an ORDERED arraylist
   * with such properties along with their ranking information.
   * 
   * @param RDFModel
   *          Model with the RDF-triples to be ranked
   * @param definedObjectProperties
   *          Properties that are marked in the ontology as relevant ones
   * @param initialResource
   *          Resource which we are ranking the properties for
   * @return Ordered arrayList of ranked properties
   */
  public abstract ArrayList<? extends RankedProperty> rankDefinedObjectProperties(Model RDFModel,
      HashSet<String> definedObjectProperties, String initialResource);

  /**
   * 
   * Method that ranks the rest of the properties (properties which are not defined/marked in the domain ontology, but
   * which appear in the data model retrieved), and returns an ORDERED arraylist with such properties along with their
   * ranking information.
   * 
   * @param RDFModel
   *          Model with the RDF-triples to be ranked
   * @param definedObjectProperties
   *          Properties that are marked in the ontology as relevant ones, thus, it is used as a "black-list" filter
   * @param initialResource
   *          Resource which we are ranking the properties for
   * @return Ordered arrayList of ranked properties
   */

  public abstract ArrayList<? extends RankedProperty> rankNonDefinedObjectProperties(Model RDFModel,
      HashSet<String> definedObjectProperties, String initialResource);

}
