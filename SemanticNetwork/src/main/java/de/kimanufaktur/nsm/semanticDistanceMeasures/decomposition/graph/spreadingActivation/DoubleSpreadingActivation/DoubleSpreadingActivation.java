/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.DoubleSpreadingActivation;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Definition;
import de.kimanufaktur.nsm.graph.entities.marker.DoubleMarker;
import de.kimanufaktur.nsm.graph.entities.nodes.DoubleNode;
import de.kimanufaktur.markerpassing.*;
import de.kimanufaktur.nsm.graph.entities.links.*;
import org.jgrapht.Graph;

import java.util.*;

/**
 * Created by faehndrich on 06.05.15.
 */
public class DoubleSpreadingActivation extends SpreadingAlgorithm {
    SpreadingAlgorithm algorithm = new SpreadingAlgorithm();
    int pulsecount = 0;

    HashMap<Concept, Node> nodes = new HashMap<>();

    public HashMap<Concept, Node> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<Concept, Node> nodes) {
        this.nodes = nodes;
    }

    public Node getNodeForConcept(Concept concept){
        return nodes.get(concept);
    }

    public Concept getConceptForNode(Node node){
        for(Map.Entry entry : nodes.entrySet()){
            if(entry.getValue().equals(node)){
                return (Concept)entry.getKey();
            }
        }
        return null;
    }

    public DoubleSpreadingActivation(Graph graph, double threshold) {
        //set active nodes
        HashSet<Node> activenodes = new HashSet<Node>();
        this.setActiveNodes(activenodes);
        //set firing nodes
        HashSet<Node> firingnodes = new HashSet<>();
        this.setFiringNodes(firingnodes);

        //fill node network with nodes;
        fillNodes(graph,threshold);

        //Create termination condition
        TerminationCondition terminationCondition = new TerminationCondition() {
            int terminationPulsCount = 10;
            @Override
            public boolean compute() {
                return ++pulsecount >= terminationPulsCount ;
            }
        };
        this.setTerminationCondition(terminationCondition);

        //Create a SelectFiringNodesFunction which add all nodes which are over the threshold
        SelectFiringNodesFunction selectFiringNodesFunction = new SelectFiringNodesFunction() {
            List<Class<? extends Marker>> markerklasses = null;

            @Override
            public Collection<Node> compute(Collection<Node> list) {
                if (markerklasses == null) {
                    markerklasses = new ArrayList<>();
                    markerklasses.add(DoubleMarker.class);
                }
                List<Node> firingNodes = new ArrayList<>();
                for (Node node : list) {
                    if(node != null) {
                        if (node.checkThresholds(markerklasses)) {
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
                if (node instanceof DoubleNode) {
                    ((DoubleNode) node).in(list);
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
                if (node instanceof DoubleNode) {
                    activationOutput.addAll(((DoubleNode) node).out());
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


    /**
     * Fill the nodes for the activation spreading with the given graph. This graph is used as basis, for the
     * spreading activation. The graph is not altered, but its vertices are used to implement nodes for the spreading.
     *
     * @param graph the graph to take the vertices and edges from. The vertices are concepts and the relations are links.
     * @param threshold the threshold to set for each node. TODO: have a sprecific threshold for each node type
     */
    public void fillNodes(Graph graph, double threshold) {

        for (Concept concept : (Set<Concept>) graph.vertexSet()) {
            Node node = nodes.get(concept);
            if (node == null) {
                addConceptRecursivly(concept,threshold);
            }
        }
        return;

    }

    /**
     * add the concept to the nodes network recursively
     *
     * @param concept the concept to add.
     */
    private Node addConceptRecursivly(Concept concept, double threshold) {
        Node node = nodes.get(concept);
        if (node == null) {
            node = new DoubleNode(concept.getLitheral());
            ((DoubleNode)node).setThreshold(threshold);
            nodes.put(concept, node);
        }
        for (Concept syn : concept.getSynonyms()) {
            if (syn != null) {
                Node synNode = nodes.get(syn);
                if (synNode == null) {
                    synNode = addConceptRecursivly(syn, threshold); //TODO: have a specific threshold for synonyms
                }
                SynonymLink link = new SynonymLink();
                link.setSource(node);
                link.setTarget(synNode);
                if (!node.getLinks().contains(link)) {
                    node.addLink(link);
                }
            }
        }
        for (Concept hypo : concept.getHyponyms()) {
            if (hypo != null) {
                Node hypoNode = nodes.get(hypo);
                if (hypoNode == null) {
                    hypoNode = addConceptRecursivly(hypo, threshold); //TODO: have a specific threshold for hyponyms
                }
                HyponymLink link = new HyponymLink();
                link.setSource(node);
                link.setTarget(hypoNode);
                if (!node.getLinks().contains(link)) {
                    node.addLink(link);
                }
            }
        }
        for (Concept hyper : concept.getHypernyms()) {
            if (hyper != null) {
                Node hyperNode = nodes.get(hyper);
                if (hyperNode == null) {
                    hyperNode = addConceptRecursivly(hyper, threshold); //TODO: have a specific threshold for hypernyms
                }
                HypernymLink link = new HypernymLink();
                link.setSource(node);
                link.setTarget(hyperNode);
                if (!node.getLinks().contains(link)) {
                    node.addLink(link);
                }
            }
        }
        for (Definition definition : concept.getDefinitions()) {
            for (Concept def : definition.getDefinition()) {
                if (def != null) {
                    Node defnode = nodes.get(def);
                    if (defnode == null) {
                        defnode = addConceptRecursivly(def, threshold); //TODO: have a specific threshold for definitions
                    }
                    DefinitionLink link = new DefinitionLink();
                    link.setSource(node);
                    link.setTarget(defnode);
                    if (!defnode.getLinks().contains(link)) {
                        node.addLink(link);
                    }
                }
            }
        }
        for (Concept antonym : concept.getAntonyms()) {
            if (antonym != null) {
                Node antoNode = nodes.get(antonym);
                if (antoNode == null) {
                    antoNode = addConceptRecursivly(antonym, threshold); //TODO: have a specific threshold for antonyms
                }
                AntonymLink link = new AntonymLink();
                link.setSource(node);
                link.setTarget(antoNode);
                if (!node.getLinks().contains(link)) {
                    node.addLink(link);
                }
            }
        }
        for (Concept arbitraryRelation : concept.getArbitraryRelations()) {
            if (arbitraryRelation != null) {
                Node relatedNode = nodes.get(arbitraryRelation);
                if (relatedNode == null) {
                    relatedNode = addConceptRecursivly(arbitraryRelation, threshold); //TODO: have a specific threshold for arbitrary relationships
                }
                ArbitraryRelationLink link = new ArbitraryRelationLink();
                link.setSource(node);
                link.setTarget(relatedNode);
                link.setRelationName(arbitraryRelation.getOriginatedRelationName());
                if (!node.getLinks().contains(link)) {
                    node.addLink(link);
                }
            }
        }

        return node;
    }

    /**
     * Example implementation of the Node interface for nodes which have a double threshold.
     */


    public void addMarkerToNode(Concept concept2Activate, Marker activationMarker){
        Node node2activate = nodes.get(concept2Activate);
        node2activate.getMarkers().add(activationMarker);
        getActiveNodes().add(node2activate);
    }


}
