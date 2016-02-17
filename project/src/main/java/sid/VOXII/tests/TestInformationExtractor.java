package sid.VOXII.tests;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;

import es.unizar.vox2.rank.PropertyRanker;
import es.unizar.vox2.rank.RankedResource;
import sid.VOXII.informationExtraction.InformationExtractor;
import sid.VOXII.informationExtraction.InformationExtractorFactory;
import sid.VOXII.informationExtraction.implementations.PrunedDescribeDepth2IExtractor;
import sid.VOXII.propertyRanking.implementations.InstanceNumberRankedProperty;
import sid.VOXII.propertyRanking.implementations.RelevantInformationRankedProperty;

public class TestInformationExtractor {

  public static final String SPARQL_ENDPOINT_DBPEDIA = "http://dbpedia.org/sparql";
  public static final String SPARQL_ENDPOINT_HORUS = "http://horus.cps.unizar.es:8890/sparql";

  public static final String PROPERTY_LEADER_PARTY = "http://dbpedia.org/ontology/leaderParty";
  public static final String PROPERTY_BIRTH_PLACE = "http://dbpedia.org/ontology/birthPlace";
  public static final String PROPERTY_DEATH_PLACE = "http://dbpedia.org/ontology/deathPlace";

  public static final String RESOURCE_EXAMPLE_NEWTON = "http://dbpedia.org/resource/Isaac_Newton";

  public static void main(String[] args) {

    PrintWriter out = null;
    try {
      out = new PrintWriter(new File("log.txt"));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }

    InformationExtractor iExtractor = InformationExtractorFactory
        .getInformationExtractor(InformationExtractorFactory.PRUNED_DESCRIBE_DEPTH_2);
    ArrayList<String> relevantProperties = new ArrayList<String>();
    relevantProperties.add(PROPERTY_LEADER_PARTY);
    relevantProperties.add(PROPERTY_BIRTH_PLACE);
    relevantProperties.add(PROPERTY_DEATH_PLACE);
    String sparqlEndpoint = SPARQL_ENDPOINT_DBPEDIA;
    // String resource = args[0];
    String resource = RESOURCE_EXAMPLE_NEWTON;

    long start = System.currentTimeMillis();
    Model model = iExtractor.retrieveResourceInformation(resource, relevantProperties, sparqlEndpoint);
    long end = System.currentTimeMillis();
    out.printf("Data retrieved in %d miliseconds\n\n", end - start);

    NodeIterator nIterator = model.listObjectsOfProperty(model.getResource(resource),
        model.getProperty(PrunedDescribeDepth2IExtractor.PROPERTY_NUM));
    int numDescribes = (nIterator.hasNext() ? Integer.valueOf(nIterator.next().toString()) : -1);

    // data.write(System.out, "N3");
    out.println("Extraction Statistics: ");
    out.println("-----------------------");
    out.println("Number of Statements: " + model.size());
    out.println("Number of Queries: " + numDescribes);
    out.println("Extraction time: " + (((float) (end - start)) / 1000.0));
    out.println();
    out.flush();

    // now the different rankings:
    HashSet<String> definedProperties = new HashSet<String>(relevantProperties);

    PropertyRanker propRanker = PropertyRanker.create(PropertyRanker.INSTANCE_NUMBER_RANKING_BIDIR_DEPTH_1);
    List<? extends RankedResource> rankedProperties = propRanker.rankDefinedObjectProperties(model, definedProperties,
        resource);
    out.println("Ranking: " + PropertyRanker.INSTANCE_NUMBER_RANKING_BIDIR_DEPTH_1);
    out.println("-------------------------");
    for (int i = 0; i < rankedProperties.size(); i++) {
      InstanceNumberRankedProperty auxInfo = (InstanceNumberRankedProperty) rankedProperties.get(i);
      out.println(" " + i + "# " + auxInfo.getResourceUri());
      out.println("     value:" + auxInfo.getRankValue() + "  #instances:" + auxInfo.getNumberOfInstances());
    }
    out.println();
    out.flush();

    propRanker = PropertyRanker.create(PropertyRanker.INSTANCE_NUMBER_RANKING_INBOUND_DEPTH_1);
    rankedProperties = propRanker.rankDefinedObjectProperties(model, definedProperties, resource);
    out.println("Ranking: " + PropertyRanker.INSTANCE_NUMBER_RANKING_INBOUND_DEPTH_1);
    out.println("-------------------------");
    for (int i = 0; i < rankedProperties.size(); i++) {
      InstanceNumberRankedProperty auxInfo = (InstanceNumberRankedProperty) rankedProperties.get(i);
      out.println(" " + i + "# " + auxInfo.getResourceUri());
      out.println("     value:" + auxInfo.getRankValue() + "  #instances:" + auxInfo.getNumberOfInstances());
    }
    out.println();
    out.flush();

    propRanker = PropertyRanker.create(PropertyRanker.INSTANCE_NUMBER_RANKING_OUTBOUND_DEPTH_1);
    rankedProperties = propRanker.rankDefinedObjectProperties(model, definedProperties, resource);
    out.println("Ranking: " + PropertyRanker.INSTANCE_NUMBER_RANKING_OUTBOUND_DEPTH_1);
    out.println("-------------------------");
    for (int i = 0; i < rankedProperties.size(); i++) {
      InstanceNumberRankedProperty auxInfo = (InstanceNumberRankedProperty) rankedProperties.get(i);
      out.println(" " + i + "# " + auxInfo.getResourceUri());
      out.println("     value:" + auxInfo.getRankValue() + "  #instances:" + auxInfo.getNumberOfInstances());
    }
    out.println();
    out.flush();

    propRanker = PropertyRanker.create(PropertyRanker.RELEVANT_INFORMATION_AMOUNT_RANKING_DEPTH_2);
    rankedProperties = propRanker.rankDefinedObjectProperties(model, definedProperties, resource);
    out.println("Ranking: " + PropertyRanker.RELEVANT_INFORMATION_AMOUNT_RANKING_DEPTH_2);
    out.println("-------------------------");
    for (int i = 0; i < rankedProperties.size(); i++) {
      RelevantInformationRankedProperty auxInfo = (RelevantInformationRankedProperty) rankedProperties.get(i);
      out.println(" " + i + "# " + auxInfo.getResourceUri());
      out.println("     value:" + auxInfo.getRankValue() + "  #reachable:" + auxInfo.getReachableResources()
          + "  #weight:" + auxInfo.getInformationWeight());

    }
    out.println();
    out.flush();
    out.close();
  }

}
