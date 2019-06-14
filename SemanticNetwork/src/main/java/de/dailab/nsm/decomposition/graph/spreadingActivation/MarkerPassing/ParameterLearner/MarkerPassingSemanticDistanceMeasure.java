/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.spreadingActivation.MarkerPassing.ParameterLearner;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.IConcept;
import de.dailab.nsm.decomposition.WordType;
import de.dailab.nsm.decomposition.graph.conceptCache.GraphUtil;
import de.dailab.nsm.decomposition.graph.entities.marker.DoubleMarkerWithOrigin;
import de.dailab.nsm.decomposition.graph.entities.nodes.DoubleNodeWithMultipleThresholds;
import de.dailab.nsm.decomposition.graph.spreadingActivation.MarkerPassing.DoubleMarkerPassing;
import de.dailab.nsm.decomposition.graph.spreadingActivation.MarkerPassing.MarkerPassingConfig;
import de.dailab.nsm.semanticDistanceMeasures.DataExample;
import de.dailab.nsm.semanticDistanceMeasures.SemanticDistanceMeasureInterface;
import de.tuberlin.spreadalgo.Marker;
import de.tuberlin.spreadalgo.Node;
import org.jgrapht.Graph;

import java.util.*;

/**
 * Created by faehndrich on 19.02.16.
 */
public class MarkerPassingSemanticDistanceMeasure implements SemanticDistanceMeasureInterface {

    static MarkerPassingConfig markerPassingConfig = new MarkerPassingConfig();


    /**
     * Use a marker passing where which marker has a double as data. This is quite similar to activation spreading.
     *
     * @param resultGraph     this is the graph the marker passing takes place on.
     * @param startActivation the markers in the beginning of the algorithem. Please notice that there should be at least one
     *                        threshold reached. Else there will be no passing of markers.
     * @param threshold       the threshold from which an node is active.
     * @return the graph given as input but now with markern on top, representing the result of the marker passing algorithm.
     */
    private static DoubleMarkerPassing getDoubleMarkerPassing(Graph resultGraph, List<Map<Concept, List<? extends Marker>>> startActivation, Map<Concept, Double> threshold) {
        DoubleMarkerPassing doubleMarkerPassingAlgo = new DoubleMarkerPassing(resultGraph, threshold, DoubleNodeWithMultipleThresholds.class);
        DoubleMarkerPassing.doInitialMarking(startActivation, doubleMarkerPassingAlgo);
        doubleMarkerPassingAlgo.execute();
        return doubleMarkerPassingAlgo;
    }


    @Override
    public double compareConcepts(IConcept c1, IConcept c2) {

        try {
            return findSim((Concept) c1, (Concept) c2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Double.NaN;
    }

    /**
     * Find the similarity as cosine similarity with the deeplearning4j framework.
     *
     * @param c1 the concept which should be
     * @param c2
     * @return
     * @throws Exception
     */
    public double findSim(Concept c1, Concept c2) {
        double sim = this.passMarker(c1.getLitheral(), c1.getWordType(), c2.getLitheral(), c2.getWordType(), MarkerPassingConfig.getDecompositionDepth(), MarkerPassingConfig.getStartActivation(), MarkerPassingConfig.getThreshold(), MarkerPassingConfig.getThreshold(), DoubleNodeWithMultipleThresholds.class);
        return sim;
    }

    /**
     * Marker passing, given two concepts and their POS this function creates two decompositions of the given
     * decomposition depth, merges the two graphs and activates over them.
     *
     * @param word1              the fist word to decompose
     * @param wordType1          the POS of the first word
     * @param word2              the second word to decompose
     * @param wordType2          the POS of the second word
     * @param decompositionDepth the decomposition depth with which the words should be decomposed
     * @return the average activation found with dounble activation, as an measure of distance.
     */
    public  <T extends DoubleNodeWithMultipleThresholds> Double  passMarker(String word1, WordType wordType1, String word2, WordType wordType2, int decompositionDepth, double startActivationLevel, double thresholdNode1, double thresholdNode2, Class<T> nodeType) {

        if (word1.equals(word2)) { //TODO: this is a hack. Remove to test if our appraoch can find synonyms. For now just a optimization to save time.
            return 1.0;
        }
        //Get Decompositions
        Graph graphword1 = GraphUtil.getGraph(word1, wordType1, decompositionDepth);
        Graph graphword2 = GraphUtil.getGraph(word2, wordType2, decompositionDepth);
        //Merge the two graphs
        if(graphword1.edgeSet().isEmpty() || graphword2.edgeSet().isEmpty()){
            return 0.0;
        }
        Graph commonGraph = GraphUtil.mergeGraph(graphword1, graphword2);
        //Create Marker Passsing configuration
        //create start marker1
        Concept activeNode1 = (Concept) graphword1.vertexSet().toArray()[0];
        DoubleMarkerWithOrigin startMarker1 = new DoubleMarkerWithOrigin();
        startMarker1.setActivation(startActivationLevel);
        startMarker1.setOrigin(activeNode1);
        List<Marker> markers1 = new ArrayList<>();
        markers1.add(startMarker1);
        //create start marker2
        Concept activeNode2 = (Concept) graphword2.vertexSet().toArray()[0];
        DoubleMarkerWithOrigin startMarker2 = new DoubleMarkerWithOrigin();
        startMarker2.setActivation(startActivationLevel);
        startMarker2.setOrigin(activeNode2);
        List<Marker> markers2 = new ArrayList<>();
        markers2.add(startMarker2);
        //set start markers
        List<Map<Concept, List<? extends Marker>>> startActivation = new ArrayList<>();
        Map<Concept, List<? extends Marker>> conceptMarkerMap = new HashMap<>();
        //add to marker concept map
        conceptMarkerMap.put(activeNode1, markers1);
        conceptMarkerMap.put(activeNode2, markers2);
        startActivation.add(conceptMarkerMap);
        //set thresholds
        Map<Concept, Double> threshold = new HashMap<>(2);
        threshold.put(activeNode1, thresholdNode1);
        threshold.put(activeNode2, thresholdNode2);
        //create algorithm
        DoubleMarkerPassing doubleMarkerPassing = getDoubleMarkerPassing(commonGraph, startActivation, threshold);
        Collection<Node> activeNodes = doubleMarkerPassing.getActiveNodes();

        double totalActivation = getTotalActivation(activeNodes);
        List<DoubleNodeWithMultipleThresholds> doubleActiveNodes = getDoubleActivation(activeNodes);

        int pulscount = doubleMarkerPassing.getPulsecount();

        //double diffDoubleActivation  = getDifferernceOfDoubleActivation(doubleActiveNodes);
        //double sumDoubleActivation  = getCurentDoubleActivation(doubleActiveNodes);
        double sumDoubleActivation = getSumOfDoubleActivationHistory(doubleActiveNodes);
        return sumDoubleActivation / (2 * startActivationLevel);
        //System.out.println(sumDoubleActivation + ";" + totalActivation);
        //return Math.cos((Math.PI/2)*(1-(sumDoubleActivation/2*startActivationLevel)));
        //return ((sumDoubleActivation/pulscount));
    }

    /**
     * For use when the decomposition graph was created at an earlier time.
     * Also you can give an abitrary amount of activation nodes.
     *
     * NOTE
     *
     * You must give a threshold for every given activation node. Thus the resulting array is of the same
     * size as the list of activation nodes.
     *
     * @param commonGraph           -   the common decomposition graph. must contain the given start activation nodes
     * @param activationNodes       -   nodes to start spreading from.
     * @param startActivationLevel  -   the "activeness" of the given start nodes
     * @param nodeThresholds        -   activation node's thresholds
     * @param <T>
     * @return
     */
    public  <T extends DoubleNodeWithMultipleThresholds> Double  markerPassing(Graph commonGraph, List<Concept> activationNodes, double startActivationLevel, double... nodeThresholds) {

        List<Map<Concept, List<? extends Marker>>> startActivation = new ArrayList<>();
        Map<Concept, List<? extends Marker>> conceptMarkerMap = new HashMap<>();
        Map<Concept, Double> threshold = new HashMap<>(2);
        startActivation.add(conceptMarkerMap);

        //set start markers
        for(int i=0; i<activationNodes.size(); i++){
            Concept c = activationNodes.get(i);
            DoubleMarkerWithOrigin startMarker = new DoubleMarkerWithOrigin();
            startMarker.setActivation(startActivationLevel);
            startMarker.setOrigin(c);
            List<Marker> startMarkers = new ArrayList<>();
            startMarkers.add(startMarker);
            conceptMarkerMap.put(c, startMarkers);
            //set thresholds
            threshold.put(c, nodeThresholds[i]);
        }

        //create algorithm
        DoubleMarkerPassing doubleMarkerPassing = getDoubleMarkerPassing(commonGraph, startActivation, threshold);
        Collection<Node> activeNodes = doubleMarkerPassing.getActiveNodes();

        double totalActivation = getTotalActivation(activeNodes);
        List<DoubleNodeWithMultipleThresholds> doubleActiveNodes = getDoubleActivation(activeNodes);

        int pulscount = doubleMarkerPassing.getPulsecount();

        //double diffDoubleActivation  = getDifferernceOfDoubleActivation(doubleActiveNodes);
        //double sumDoubleActivation  = getCurentDoubleActivation(doubleActiveNodes);
        double sumDoubleActivation = getSumOfDoubleActivationHistory(doubleActiveNodes);
        return sumDoubleActivation / (2 * startActivationLevel); //TODO: maybe n * startActivationLevel, where n is activationNodes.size
        //System.out.println(sumDoubleActivation + ";" + totalActivation);
        //return Math.cos((Math.PI/2)*(1-(sumDoubleActivation/2*startActivationLevel)));
        //return ((sumDoubleActivation/pulscount));
    }

    /**
     * Get the sum of activation of all active nodes.
     *
     * @param activeNodes the nodes from which the actiavtion should be summed up.
     * @return the total amount of action present in the network.
     */
    private double getTotalActivation(Collection<Node> activeNodes) {
        double totalActivation = 0.0d;
        for (Node activeNode : activeNodes) {
            if (activeNode != null && ((DoubleNodeWithMultipleThresholds) activeNode).getActivation() != null) {
                Map<Concept,Map<Double, List<Concept>>> activationMap = ((DoubleNodeWithMultipleThresholds) activeNode).getActivation();
                for (Map <Double, List<Concept>> currentActivationMap : activationMap.values()){
                    for (Double activation : currentActivationMap.keySet()) {
                        totalActivation += activation;
                    }
                }
            }

        }
        return totalActivation;
    }

    /**
     * calculate the average activation of all nodes. Given activ nodes (nodes containing markers)
     *
     * @param activeNodes the activated node which contain markers.
     * @return the average double activation of all given nodes.
     */
    private double getAvgActivation(List<DoubleNodeWithMultipleThresholds> activeNodes) {
        if (activeNodes.size() > 0) {
            double doubleActivation = getCurentDoubleActivation(activeNodes);
            double avgActivation = doubleActivation / activeNodes.size();
            return avgActivation;
        } else {
            return 0.0;
        }

    }

    /**
     * calculate the current double activation of all nodes. Given activ nodes (nodes containing markers)
     *
     * @param activeNodes the activated node which contain markers.
     * @return the current double activation of all given nodes.
     */
    private double getCurentDoubleActivation(List<DoubleNodeWithMultipleThresholds> activeNodes) {
        double doubleActivation = 0.0;
        if (activeNodes.size() > 0) {
            for (DoubleNodeWithMultipleThresholds node : activeNodes) {
                if (node.getActivation().size() > 1) {
                    Map<Concept,Map<Double, List<Concept>>> activationMap = node.getActivation();
                    for (Map <Double, List<Concept>> currentActivationMap : activationMap.values()) {
                        for (Double activation : currentActivationMap.keySet()) {
                            if (activation != Double.NaN) {
                                doubleActivation += activation;
                            }
                        }
                    }
                }
            }
            return doubleActivation;
        } else {
            return 0.0;
        }

    }


    /**
     * calculate the the sum of the history of double activation of all nodes. Given activ nodes (nodes containing markers)
     *
     * @param activeNodes the activated node which contain markers.
     * @return the sum double activation of all given nodes.
     */
    private double getSumOfDoubleActivationHistory(List<DoubleNodeWithMultipleThresholds> activeNodes) {
        double doubleActivationHistory = 0.0;
        if (activeNodes.size() > 0) {
            for (DoubleNodeWithMultipleThresholds node : activeNodes) {
                for (Marker marker : node.getActivationHistory()) {
                    if (marker instanceof DoubleMarkerWithOrigin) {
                        doubleActivationHistory += ((DoubleMarkerWithOrigin) marker).getActivation();
                    }
                }
            }
            return doubleActivationHistory;
        } else {
            return 0.0;
        }

    }

    /**
     * calculate the the sum of the history of double activation of all nodes. Given activ nodes (nodes containing markers)
     *
     * @param activeNodes the activated node which contain markers.
     * @return the sum double activation of all given nodes.
     */
    private double getSumOfMaximumOfDoubleActivation(List<DoubleNodeWithMultipleThresholds> activeNodes) {
        double doubleActivationHistory = 0.0;
        if (activeNodes.size() > 0) {
            for (DoubleNodeWithMultipleThresholds node : activeNodes) {
                double max = 0.0d;
                for (Marker marker : node.getActivationHistory()) {
                    if (marker instanceof DoubleMarkerWithOrigin) {
                        double da = ((DoubleMarkerWithOrigin) marker).getActivation();
                        if (max < da) {
                            max = da;
                        }
                    }
                    doubleActivationHistory += max;
                }
            }
            return doubleActivationHistory;
        } else {
            return 0.0;
        }

    }


    /**
     * calculate the the difference of the history of activation of all origines. Given activ nodes (nodes containing markers)
     *
     * @param activeNodes the activated node which contain markers.
     *                    TODO: what happens if we have more then two origins
     * @return the average double activation of all given nodes.
     */
    private double getDifferernceOfDoubleActivation(List<DoubleNodeWithMultipleThresholds> activeNodes) {
        double doubleActivationdifference = 0.0;
        HashMap<Concept, Double> activaitonByOrigin = new HashMap<>(2);
        if (activeNodes.size() > 0) {
            for (DoubleNodeWithMultipleThresholds node : activeNodes) {
                for (Marker marker : node.getActivationHistory()) {
                    if (marker instanceof DoubleMarkerWithOrigin) {
                        if (activaitonByOrigin.get(((DoubleMarkerWithOrigin) marker).getOrigin()) != null) {
                            double currentActivation = activaitonByOrigin.get(((DoubleMarkerWithOrigin) marker).getOrigin());
                            activaitonByOrigin.put(((DoubleMarkerWithOrigin) marker).getOrigin(), currentActivation + ((DoubleMarkerWithOrigin) marker).getActivation());
                        } else {
                            activaitonByOrigin.put(((DoubleMarkerWithOrigin) marker).getOrigin(), ((DoubleMarkerWithOrigin) marker).getActivation());
                        }
                    }
                }
            }
            if (activaitonByOrigin.size() == 2) {
                double firstOriginActivation = 0.0;
                double secondOriginActivation = 0.0;
                Iterator<Double> originIterator = activaitonByOrigin.values().iterator();
                firstOriginActivation = originIterator.next();
                secondOriginActivation = originIterator.next();
                doubleActivationdifference = Math.abs(firstOriginActivation - secondOriginActivation);
            }

            return Math.abs(doubleActivationdifference);
        } else {
            return 0.0;
        }

    }


    /**
     * Helper method which gets all markers of nodes which have been activated by at least two sources.
     * This method is thought for the decomposition to be run before, because we use the list of ignor®ed concepts, to
     * filter unwanted nodes.
     *
     * @param activeNodes The active nodes which should be analyzed.
     * @return a list of Node which have been activated by at least two sources.
     */
    private List<DoubleNodeWithMultipleThresholds> getDoubleActivation(Collection<Node> activeNodes) {
        List<DoubleNodeWithMultipleThresholds> doubleActiveNodes = new ArrayList<>();
        for (Node node : activeNodes) {
            if (node instanceof DoubleNodeWithMultipleThresholds) {
                DoubleNodeWithMultipleThresholds doubleNodeWithMultipleThresholds = (DoubleNodeWithMultipleThresholds) node;
                if (doubleNodeWithMultipleThresholds.getActivation().size() > 1 && !Decomposition.getConcepts2Ignore().contains(doubleNodeWithMultipleThresholds.getConcept().getLitheral())) {
                    doubleActiveNodes.add(doubleNodeWithMultipleThresholds);
                }
            }
        }
        return doubleActiveNodes;
    }

//
//    @Override
//    public void test() {
//        for (DataExample pair : testSynonymPairs) {
//            try {
//                TestRunnable test = new TestRunnable() {
//                    @Override
//                    public void run() {
//                        //spreadActivation(this.word1, this.wordType1, this.word2, this.wordType2, this.decompositionDepth);
//                        pair.setResult(passMarker(((SynonymPair)pair).getWord(), WordType.NN, ((SynonymPair)pair).getSynonym(), WordType.NN, markerPassingConfig.getDecompositionDepth(), markerPassingConfig.getStartActivation(), markerPassingConfig.getThreshold(), markerPassingConfig.getThreshold(), DoubleNodeWithMultipleThresholds.class));
//                        System.out.println(((SynonymPair)pair).getWord() + ";" + ((SynonymPair)pair).getSynonym() + ";" + pair.getResult() + ";" + pair.getTrueResult());
//                    }
//                };
//                test.setPair(pair);
//                threadPoolExecutor.submit(test);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        threadPoolExecutor.shutdown();
//        return;
//    }


}

