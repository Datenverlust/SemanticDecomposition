package de.kimanufaktur.nsm.decomposition.graph;

import de.kimanufaktur.markerpassing.Node;
import de.kimanufaktur.markerpassing.Marker;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Decomposition;
import de.kimanufaktur.nsm.decomposition.graph.conceptCache.GraphUtil;
import de.kimanufaktur.nsm.decomposition.graph.edges.WeightedEdge;
import de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing.DoubleMarkerPassing;
import de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing.MarkerPassingConfig;
import de.kimanufaktur.nsm.graph.entities.marker.DoubleMarkerWithOrigin;
import de.kimanufaktur.nsm.graph.entities.nodes.DoubleNodeWithMultipleThresholds;
import org.jgrapht.Graph;
import static de.kimanufaktur.nsm.decomposition.WordType.UNKNOWN;
import java.util.*;

import static de.kimanufaktur.nsm.decomposition.graph.edges.EdgeType.Definition;
import static de.kimanufaktur.nsm.decomposition.graph.edges.EdgeType.Synonym;

/** Created by faehndrich on 30.09.16. */
public class MarkerpassingRunner {

  public static void main(String[] args) {
    Map<Concept, List<? extends Marker>> conceptMarkerMap = new HashMap<>();
    Map<Concept, Double> threshold = new HashMap<>();

    // Create Sentence graph

    List<Concept> words = new ArrayList<>();
    Concept middle = new Concept("middle");
    Concept left = new Concept("left");
    Concept right = new Concept("right");

    Set<Concept> middleSynonyms = new HashSet<>();
    middleSynonyms.add(left);
    middleSynonyms.add(right);
    Set<Concept> synonyms = new HashSet<>();
    synonyms.add(middle);

    HashMap<String, Set<Concept>> middleSynonymsMap = new HashMap<>();
    HashMap<String, Set<Concept>> synonymsMap = new HashMap<>();
    synonymsMap.put("", synonyms);
    middleSynonymsMap.put("", middleSynonyms);
    left.setSenseKeyToSynonymsMap(synonymsMap);
    right.setSenseKeyToSynonymsMap(synonymsMap);
    middle.setSenseKeyToSynonymsMap(middleSynonymsMap);

    words.add(left);
    words.add(right);
    Graph<Concept, WeightedEdge> graph = GraphUtil.getGraph(middle.getLitheral(), UNKNOWN, 0);
    for (Concept word : words) {
      graph.addVertex(word);

      //      WeightedEdge backwards = new WeightedEdge();
      //      towards.setSource(middle);
      //      towards.setTarget(word);
      //      towards.setEdgeType(Synonym);
      //      graph.addEdge(middle, word, backwards);

      // create start marker for sentence
      List<DoubleMarkerWithOrigin> markers1 = new ArrayList<>();
      Concept activeNode = word;
      DoubleMarkerWithOrigin startMarker = new DoubleMarkerWithOrigin();
      startMarker.setActivation(1.0);
      startMarker.setOrigin(activeNode);
      markers1.add(startMarker);
      conceptMarkerMap.put(activeNode, markers1);
      threshold.put(activeNode, MarkerPassingConfig.getThreshold());
    }

    // do marker passing
    // configure start markers
    // set start markers
    List<Map<Concept, List<? extends Marker>>> startActivation = new ArrayList<>();
    startActivation.add(conceptMarkerMap);

    // set config params
    //    MarkerPassingConfig.setDoubleActivationLimit(5.0);

    // create marker passing algorithm
    DoubleMarkerPassing doubleMarkerPassing =
        new DoubleMarkerPassing(graph, threshold, DoubleNodeWithMultipleThresholds.class);
    DoubleMarkerPassing.doInitialMarking(startActivation, doubleMarkerPassing);
    doubleMarkerPassing.execute();
    // analyse results
    Collection<Node> activeNodes = doubleMarkerPassing.getActiveNodes();
  }
}
