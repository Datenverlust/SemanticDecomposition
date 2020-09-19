/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Decomposition;
import de.kimanufaktur.nsm.decomposition.Definition;
import de.kimanufaktur.nsm.graph.entities.links.*;
import de.kimanufaktur.nsm.graph.entities.nodes.DoubleNodeWithMultipleThresholds;
import de.kimanufaktur.markerpassing.*;
import org.jgrapht.Graph;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by faehndrich on 06.05.15.
 */
public class DoubleMarkerPassing extends SpreadingAlgorithm {



    int pulsecount = 0;

    double currentDoubleActivation = 0.0;
    double maximalDoubleActivation = 0.0;

    Map<Concept, Double> doubleActivation = new HashMap<>();
    BiMap<Concept, Node> nodes = HashBiMap.create();
    List<Marker> originMarkerClasses = new ArrayList<>();

    public <T extends DoubleNodeWithMultipleThresholds> DoubleMarkerPassing(Graph graph, Map<Concept, Double> threshold, Class<T> nodeType) {
        //set active nodes
        final HashSet<Node> activenodes = new HashSet<Node>();
        this.setActiveNodes(activenodes);
        //set firing nodes
        HashSet<Node> firingnodes = new HashSet<>();
        this.setFiringNodes(firingnodes);
        //fill node network with nodes;
//        for (Map.Entry<Concept,Double> entry : threshold.entrySet()){
//            System.out.println("DEBUG lökojö: "+entry.getKey().getLitheral()+" "+entry.getValue());
//        }
        fillNodes(graph, threshold, nodeType);

        //Create termination condition
        TerminationCondition terminationCondition = new TerminationCondition() {
            @Override
            public boolean compute() {
                double lastDoubleActivation = currentDoubleActivation;

                currentDoubleActivation = 0.0;

                for (Node activeNode : getActiveNodes()) {
                    if (activeNode instanceof DoubleNodeWithMultipleThresholds) {
                        currentDoubleActivation += ((DoubleNodeWithMultipleThresholds) activeNode).getDoubleActivation();
                    }
                }
                if (currentDoubleActivation > maximalDoubleActivation) {
                    maximalDoubleActivation = currentDoubleActivation;
                }
                if (currentDoubleActivation >= MarkerPassingConfig.getDoubleActivationLimit()) {
                    return true;
                } /*else if (lastDoubleActivation != 0.0 && lastDoubleActivation == currentDoubleActivation) {
                    return true;
                }*/ else {
                    return ++pulsecount >= MarkerPassingConfig.getTerminationPulsCount();
                }


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
                if (node instanceof DoubleNodeWithMultipleThresholds) {
                    ((DoubleNodeWithMultipleThresholds) node).in(list);
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
                if (node instanceof DoubleNodeWithMultipleThresholds) {
                    activationOutput.addAll(((DoubleNodeWithMultipleThresholds) node).out());
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

    public static void doInitialMarking(List<Map<Concept, List<? extends Marker>>> startActivation, DoubleMarkerPassing doubleMarkerPassingAlgo) {
        for (Map<Concept, List<? extends Marker>> m : startActivation) {
            for (Map.Entry<Concept, List<? extends Marker>> e : m.entrySet()) {

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

    public Map<Concept, Double> getDoubleActivation() {
        return doubleActivation;
    }

    public void setDoubleActivation(Map<Concept, Double> doubleActivation) {
        this.doubleActivation = doubleActivation;
    }

    public BiMap<Concept, Node> getNodes() {
        return nodes;
    }

    public int getPulsecount() {
        return pulsecount;
    }

    public void setPulsecount(int pulsecount) {
        this.pulsecount = pulsecount;
    }

    public Node getNodeForConcept(Concept concept) {
        return nodes.get(concept);
    }

    public Concept getConceptForNode(Node node) {
        for (Map.Entry entry : nodes.entrySet()) {
            if (entry.getValue().equals(node)) {
                return (Concept) entry.getKey();
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
    public <T extends DoubleNodeWithMultipleThresholds> void fillNodes(Graph graph, Map<Concept, Double> threshold, Class<T> nodeType) {

        for (Concept concept : (Set<Concept>) graph.vertexSet()) {
            if (Decomposition.getConcepts2Ignore().contains(concept)) {
                continue;
            } else {
                Node node = nodes.get(concept);
                if (node == null || nodes.inverse().get(node).getDecompositionElementCount() < concept.getDecompositionElementCount()) {
                    Node tmpnode = addConceptRecursivly(concept, threshold, nodeType);
                    nodes.put(concept, tmpnode);
                }
            }
        }
        return;
    }

    /**
     * add the concept to the nodes network recursively
     *
     * @param concept the concept to add.
     */
    private <T extends DoubleNodeWithMultipleThresholds> Node addConceptRecursivly(Concept concept, Map<Concept, Double> threshold, Class<T> nodeType) {
        if (Decomposition.getConcepts2Ignore().contains(concept)) {
            return null;
        } else {
            T node = (T) nodes.get(concept);
            if (node == null) {
                try {
                    node = nodeType.getDeclaredConstructor(Concept.class).newInstance(concept);//concept.nodeType.newInstance(); //new DoubleNodeWithMultipleThresholds(concept);

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                node.setThreshold(threshold);
                nodes.put(concept, node);
            } else if (nodes.inverse().get(node).getDecompositionElementCount() > concept.getDecompositionElementCount()) {
                return node;
            }
            for (Concept syn : concept.getSynonyms()) {
                if (syn != null && !Decomposition.getConcepts2Ignore().contains(concept)) {
                    Node synNode = nodes.get(syn);
                    if (synNode == null) {
                        synNode = addConceptRecursivly(syn, threshold, nodeType); //TODO: have a specific threshold for synonyms
                    }
                    if (synNode != null) {
                        SynonymLink link = new SynonymLink();
                        link.setSource(node);
                        link.setTarget(synNode);
                        link.setWeight(MarkerPassingConfig.getSynonymLinkWeight());
                        if (!node.getLinks().contains(link)) {
                            node.addLink(link);
                        }
                        SynonymLink backLink = new SynonymLink();
                        backLink.setSource(synNode);
                        backLink.setSource(node);
                        backLink.setWeight(MarkerPassingConfig.getSynonymLinkWeight());
                        if(!synNode.getLinks().contains(backLink)){
                            synNode.addLink(backLink);
                        }
                    }
                }
            }

            for (Concept hypo : concept.getHyponyms()) {
                if (hypo != null && !Decomposition.getConcepts2Ignore().contains(concept)) {
                    Node hypoNode = nodes.get(hypo);
                    if (hypoNode == null) {
                        hypoNode = addConceptRecursivly(hypo, threshold, nodeType); //TODO: have a specific threshold for hyponym
                    }
                    if (hypoNode != null) {
                        HyponymLink link = new HyponymLink();
                        link.setSource(node);
                        link.setTarget(hypoNode);
                        link.setWeight(MarkerPassingConfig.getHyponymLinkWeight());
                        if (!node.getLinks().contains(link)) {
                            node.addLink(link);
                        }
                    }
                }
            }
            for (Concept hyper : concept.getHypernyms()) {
                if (hyper != null && !Decomposition.getConcepts2Ignore().contains(concept)) {
                    Node hyperNode = nodes.get(hyper);
                    if (hyperNode == null) {
                        hyperNode = addConceptRecursivly(hyper, threshold, nodeType); //TODO: have a specific threshold for hypernyms
                    }
                    if (hyperNode != null) {
                        HypernymLink link = new HypernymLink();
                        link.setSource(node);
                        link.setTarget(hyperNode);
                        link.setWeight(MarkerPassingConfig.getHypernymLinkWeight());
                        if (!node.getLinks().contains(link)) {
                            node.addLink(link);
                        }
                    }
                }
            }
            for (Definition definition : concept.getDefinitions()) {
                for (Concept def : definition.getDefinition()) {
                    if (def != null && !Decomposition.getConcepts2Ignore().contains(concept)) {
                        Node defnode = nodes.get(def);
                        if (defnode == null && !Decomposition.getConcepts2Ignore().contains(def)) {
                            defnode = addConceptRecursivly(def, threshold, nodeType); //TODO: have a specific threshold for definitions
                        }
                        if (defnode != null) {
                            DefinitionLink link = new DefinitionLink();
                            link.setSource(node);
                            link.setTarget(defnode);
                            link.setWeight(MarkerPassingConfig.getDefinitionLinkWeight());
                            if (!defnode.getLinks().contains(link)) {
                                node.addLink(link);
                            }
                        }
                    }
                }
            }
            for (Concept antonym : concept.getAntonyms()) {
                if (antonym != null) {
                    Node antoNode = nodes.get(antonym);
                    if (antoNode == null) {
                        antoNode = addConceptRecursivly(antonym, threshold, nodeType); //TODO: have a specific threshold for hypernyms
                    }
                    AntonymLink link = new AntonymLink();
                    link.setSource(node);
                    link.setTarget(antoNode);
                    link.setWeight(MarkerPassingConfig.getAntonymLinkWeight());
                    if (!node.getLinks().contains(link)) {
                        node.addLink(link);
                    }
                }
            }
            for (Concept arbitraryRelation : concept.getArbitraryRelations()) {
                if (arbitraryRelation != null) {
                    Node relatedNode = nodes.get(arbitraryRelation);
                    if (relatedNode == null) {
                        relatedNode = addConceptRecursivly(arbitraryRelation, threshold, nodeType); //TODO: have a specific threshold for arbitrary relationships
                    }
                    ArbitraryRelationLink link = new ArbitraryRelationLink();
                    link.setSource(node);
                    link.setTarget(relatedNode);
                    link.setWeight(MarkerPassingConfig.getDefaultArbitraryRelationLinkWeight());
                    link.setRelationName(arbitraryRelation.getOriginatedRelationName());
                    if (!node.getLinks().contains(link)) {
                        node.addLink(link);
                    }
                }
            }

            return node;
        }
    }

    /**
     * Example implementation of the Node interface for nodes which have a double threshold.
     */


    public void addMarkerToNode(Concept concept2Activate, Marker activationMarker) {
        Node node2activate = nodes.get(concept2Activate);
        node2activate.getMarkers().add(activationMarker);
        getActiveNodes().add(node2activate);
    }


}
