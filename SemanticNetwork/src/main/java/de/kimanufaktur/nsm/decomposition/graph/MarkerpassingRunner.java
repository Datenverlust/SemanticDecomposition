package de.kimanufaktur.nsm.decomposition.graph;

import de.kimanufaktur.markerpassing.Marker;
import de.kimanufaktur.markerpassing.Node;
import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.graph.conceptCache.GraphUtil;
import de.kimanufaktur.nsm.decomposition.graph.edges.WeightedEdge;
import de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing.DoubleMarkerPassing;
import de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing.MarkerPassingConfig;
import de.kimanufaktur.nsm.graph.entities.marker.DoubleMarkerWithOrigin;
import de.kimanufaktur.nsm.graph.entities.nodes.DoubleNodeWithMultipleThresholds;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;

import static de.kimanufaktur.nsm.decomposition.WordType.UNKNOWN;

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

    words.add(middle);
    words.add(left);
    words.add(right);
    Graph<Concept, WeightedEdge> graph = GraphUtil.getGraph(middle.getLitheral(), UNKNOWN, 0);
    for (Concept word : words) {
      graph.addVertex(word);

      // create start marker for sentence
      List<DoubleMarkerWithOrigin> markers1 = new ArrayList<>();
      Concept activeNode = word;
      DoubleMarkerWithOrigin startMarker = new DoubleMarkerWithOrigin();
      startMarker.setActivation(100.0);
      startMarker.setOrigin(activeNode);
      markers1.add(startMarker);
      conceptMarkerMap.put(activeNode, markers1);
      threshold.put(activeNode, MarkerPassingConfig.getThreshold());
    }

    // do marker passing
    List<Map<Concept, List<? extends Marker>>> startActivation = new ArrayList<>();
    startActivation.add(conceptMarkerMap);

    // create marker passing algorithm
    DoubleMarkerPassing doubleMarkerPassing =
        new DoubleMarkerPassing(graph, threshold, DoubleNodeWithMultipleThresholds.class);
    DoubleMarkerPassing.doInitialMarking(startActivation, doubleMarkerPassing);
    doubleMarkerPassing.execute();
    // analyse results
    Collection<Node> activeNodes = doubleMarkerPassing.getActiveNodes();
  }
}
