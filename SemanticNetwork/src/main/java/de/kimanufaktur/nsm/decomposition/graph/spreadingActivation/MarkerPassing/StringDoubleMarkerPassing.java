/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.kimanufaktur.nsm.graph.entities.nodes.StringDoubleNodeWithMultipleThresholds;
import de.kimanufaktur.markerpassing.*;
import org.jgrapht.Graph;

import java.util.*;

public class StringDoubleMarkerPassing extends SpreadingAlgorithm {



    int pulsecount = 0;

    double currentDoubleActivation = 0.0;
    double maximalDoubleActivation = 0.0;

    Map<String, Double> doubleActivation = new HashMap<>();
    BiMap<String, Node> nodes = HashBiMap.create();
    List<Marker> originMarkerClasses = new ArrayList<>();

    public <T extends StringDoubleNodeWithMultipleThresholds> StringDoubleMarkerPassing(Graph graph, Map<String, Double> threshold, Class<T> nodeType) {
        //set active nodes
        final HashSet<Node> activenodes = new HashSet<Node>();
        this.setActiveNodes(activenodes);
        //set firing nodes
        HashSet<Node> firingnodes = new HashSet<>();
        this.setFiringNodes(firingnodes);

        fillNodes(graph, threshold, nodeType);

        //Create termination condition
        TerminationCondition terminationCondition = new TerminationCondition() {
            @Override
            public boolean compute() {
                    return ++pulsecount >= MarkerPassingConfig.getTerminationPulsCount();
//                }


            }
        };
        this.setTerminationCondition(terminationCondition);

        //Create a SelectFiringNodesFunction which add all nodes which are over the threshold
        SelectFiringNodesFunction selectFiringNodesFunction = new SelectFiringNodesFunction() {
            @Override
            public Collection<Node> compute(Collection<Node> list) {
                List<Node> firingNodes = new ArrayList<>();
                for (Node node : list) {
                    if (node != null) {
                        if (node.checkThresholds(originMarkerClasses)) {
                            firingNodes.add(node);
                        }
                    }
                }
                return firingNodes;
            }
        };
        this.setSelectFiringNodes(selectFiringNodesFunction);

        //Create in-function
        InFunction inFunction = new InFunction() {
            @Override
            public void compute(Collection<SpreadingStep> list, Node node) {
                if (node instanceof StringDoubleNodeWithMultipleThresholds) {
                    ((StringDoubleNodeWithMultipleThresholds) node).in(list);
                    getActiveNodes().add(node);
                }
                //Node has gotton input, so it is a active node. The decision if the node will fire in the
                //next pulse, depends on the selection in the SelectFiringNodes function.
            }
        };
        this.setIn(inFunction);

        //Create out-function
        OutFunction outFunction = new OutFunction() {
            @Override
            public List<SpreadingStep> compute(Node node) {
                List<SpreadingStep> activationOutput = new ArrayList<>();
                if (node instanceof StringDoubleNodeWithMultipleThresholds) {
                    activationOutput.addAll(((StringDoubleNodeWithMultipleThresholds) node).out());
                }
                return activationOutput;
            }
        };
        this.setOut(outFunction);

        //Create pre-processing steps
        List<ProcessingStep> preProcessing = new ArrayList<>();
        this.setPreprocessingSteps(preProcessing);

        //Create post-processing steps
        List<ProcessingStep> postProcessing = new ArrayList<>();
        this.setPostprocessingSteps(postProcessing);
    }

    public static void doInitialMarking(List<Map<String, List<? extends Marker>>> startActivation, de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing.StringDoubleMarkerPassing doubleMarkerPassingAlgo) {
        for (Map<String, List<? extends Marker>> m : startActivation) {
            for (Map.Entry<String, List<? extends Marker>> e : m.entrySet()) {

                // 4 testing
//            	System.out.print("doInitialMarking startActivation concept: " + e.getKey().getLitheral());

                for (Marker marker : e.getValue()) {
                    doubleMarkerPassingAlgo.addMarkerToNode(e.getKey(), marker);

                    // 4 testing
//                    DoubleMarkerWithOrigin tmp = (DoubleMarkerWithOrigin)marker;
//                    System.out.println("; set marker activation: " + tmp.getActivation());

                    //add start markers to threshold
                    doubleMarkerPassingAlgo.getOriginMarkerClasses().add(marker);
                }
            }
        }
        // 4 testing
//        List<Marker> tmpmarker = doubleMarkerPassingAlgo.getOriginMarkerClasses();
//        for(Marker asdf : tmpmarker)
//        	System.out.println("doInitialMarking getOriginMarkerClasses are: " + ((DoubleMarkerWithOrigin)asdf).getOrigin().getLitheral());

    }

    public double getCurrentDoubleActivation() {
        return currentDoubleActivation;
    }

    public List<Marker> getOriginMarkerClasses() {
        return this.originMarkerClasses;
    }

    public double getMaximalDoubleActivation() {
        return maximalDoubleActivation;
    }

    public void setMaximalDoubleActivation(double maximalDoubleActivation) {
        this.maximalDoubleActivation = maximalDoubleActivation;
    }

    public Map<String, Double> getDoubleActivation() {
        return doubleActivation;
    }

    public void setDoubleActivation(Map<String, Double> doubleActivation) {
        this.doubleActivation = doubleActivation;
    }

    public BiMap<String, Node> getNodes() {
        return nodes;
    }

    public int getPulsecount() {
        return pulsecount;
    }

    public void setPulsecount(int pulsecount) {
        this.pulsecount = pulsecount;
    }

    public Node getNodeForConcept(String concept) {
        return nodes.get(concept);
    }

    public String getConceptForNode(Node node) {
        for (Map.Entry entry : nodes.entrySet()) {
            if (entry.getValue().equals(node)) {
                return (String) entry.getKey();
            }
        }
        return null;
    }

    /**
     * Fill the nodes for the activation spreading with the given graph. This graph is used as basis, for the
     * spreading activation. The graph is not altered, but its vertices are used to implement nodes for the spreading.
     *
     * @param graph     the graph to take the vertices and edges from. The vertices are concepts and the relations are links.
     * @param threshold the threshold to set for each node. TODO: have a sprecific threshold for each node type
     */
    public <T extends StringDoubleNodeWithMultipleThresholds> void fillNodes(Graph graph, Map<String, Double> threshold, Class<T> nodeType) {

    }

    /**
     * Example implementation of the Node interface for nodes which have a double threshold.
     */

    
    public void addMarkerToNode(String concept2Activate, Marker activationMarker) {
        Node node2activate = nodes.get(concept2Activate);
        node2activate.getMarkers().add(activationMarker);
        getActiveNodes().add(node2activate);
    }


}
