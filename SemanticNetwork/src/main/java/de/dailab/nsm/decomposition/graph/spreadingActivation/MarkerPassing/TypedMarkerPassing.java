/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.spreadingActivation.MarkerPassing;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.Definition;
import de.dailab.nsm.decomposition.graph.entities.links.DefinitionLink;
import de.dailab.nsm.decomposition.graph.entities.links.SynonymLink;
import de.dailab.nsm.decomposition.graph.entities.marker.TypedMarker;
import de.dailab.nsm.decomposition.graph.entities.nodes.TypedNode;
import de.tuberlin.spreadalgo.*;
import org.jgrapht.Graph;

import java.util.*;

/**
 * Created by root on 10.12.15.
 */
public class TypedMarkerPassing extends SpreadingAlgorithm {

    //###########################
    // VARS
    //###########################
    private int pulseCount = 0;
    private BiMap<Concept, Node> nodes = HashBiMap.create();
    private TypedMarkerPassingConfig config;
    private List<Concept> initialConcepts = new ArrayList<>();


    public TypedMarkerPassing(Graph graph, TypedMarkerPassingConfig config) {
        this.config = config;
        // set active nodes
        final HashSet<Node> activeNodes = new HashSet<Node>();
        this.setActiveNodes(activeNodes);

        // set firing nodes
        HashSet<Node> firingNodes = new HashSet<Node>();
        this.setFiringNodes(firingNodes);

        // fill nodes
        fillNodes(graph, config.threshold);

        // set termination condition
        TerminationCondition terminationCondition = new TerminationCondition() {
            @Override
            public boolean compute() {
                return config.terminationPulseCount <= pulseCount;
            }
        };
        this.setTerminationCondition(terminationCondition);

        // set select firing nodes function
        SelectFiringNodesFunction selectFiringNodesFunction = new SelectFiringNodesFunction() {
            @Override
            public Collection<Node> compute(Collection<Node> collection) {
                List<Node> firingNodes = new ArrayList<Node>();
                for (Node node : collection) {
                    if (node != null) {
                        if (node.checkThresholds(null))
                            firingNodes.add(node);
                    }
                }
                return firingNodes;
            }
        };
        this.setSelectFiringNodes(selectFiringNodesFunction);

        // set in function
        InFunction inFunction = new InFunction() {
            @Override
            public void compute(Collection<SpreadingStep> collection, Node node) {
                if (node instanceof TypedNode) {
                    ((TypedNode) node).in(collection);
                    getActiveNodes().add(node);
                }
            }
        };
        this.setIn(inFunction);

        // set out function
        OutFunction outFunction = new OutFunction() {
            @Override
            public Collection<SpreadingStep> compute(Node node) {
                List<SpreadingStep> activationOut = new ArrayList<>();
                if (node instanceof TypedNode) {
                    activationOut.addAll(((TypedNode) node).out());
                }
                return activationOut;
            }
        };
        this.setOut(outFunction);

        // set preprocessing steps
        List<ProcessingStep> preProcessing = new ArrayList<>();
        ProcessingStep preProcessStep = new ProcessingStep() {
            @Override
            public void execute() {
                pulseCount++;
            }
        };
        preProcessing.add(preProcessStep);
        this.setPreprocessingSteps(preProcessing);

        // set postprocessing steps
        List<ProcessingStep> postProcessing = new ArrayList<>();
        this.setPostprocessingSteps(postProcessing);
    }

    public void fillNodes(Graph graph, double threshold) {
        for (Concept concept : (Set<Concept>) graph.vertexSet()) {
            if (Decomposition.getConcepts2Ignore().contains(concept)) { // checks if the concept is on the ignore list
                continue;
            } else {
                Node node = nodes.get(concept);
                if (node == null ||
                        nodes.inverse().get(node).getDecompositionElementCount() <=
                                concept.getDecompositionElementCount()) { // only add node if it is not already added.
                    addConceptRecursively(concept, threshold);

                }
            }
        }
        return;
    }

    private Node addConceptRecursively(Concept concept, double threshold) {
        if (Decomposition.getConcepts2Ignore().contains(concept)) { // checks if the concept is on the ignore list
            return null;
        } else {
            Node node = nodes.get(concept);
            if (node == null) {// check if concept doesn't exist in the Graph
                node = new TypedNode(concept.getLitheral()); // create node
                ((TypedNode) node).setThreshold(threshold); // add threshold to node
                nodes.put(concept, node); // add node to nodes BiMap
            } else if (nodes.inverse().get(node).getDecompositionElementCount() > concept.getDecompositionElementCount()) { // check if all decompositions are already in the list?
                return node;
            }
            for (Concept syn : concept.getSynonyms()) {
                if (syn != null && !Decomposition.getConcepts2Ignore().contains(syn)) { // check if synonym is not on the ignore list and exists
                    Node synNode = nodes.get(syn);
                    if (synNode == null) { // if the synonym node doesn't exist, add it to the graph recursively
                        synNode = addConceptRecursively(syn, this.config.synThreshold); // TODO: evaluate threshold option!
                    }
                    if (synNode != null) {
                        SynonymLink link = new SynonymLink(); // create a new synonymlink
                        link.setWeight(this.config.synWeight); // specific weight for synonym links
                        link.setSource(node);
                        link.setTarget(synNode);
                        if (!node.getLinks().contains(link)) { // add the synonymlink if it doesn't already exist
                            node.addLink(link);
                        }
                    }
                }
            }
            for (Concept hypo : concept.getHyponyms()) {
                if (hypo != null && !Decomposition.getConcepts2Ignore().contains(hypo)) { // check if hypoonym is not on the ignore list and exists
                    Node hypoNode = nodes.get(hypo);
                    if (hypoNode == null) { // if the hypoonym node doesn't exist, add it to the graph recursively
                        hypoNode = addConceptRecursively(hypo, this.config.hypoThreshold); // TODO: evaluate threshold option!
                    }
                    if (hypoNode != null) {
                        SynonymLink link = new SynonymLink(); // create a new hypoonymlink
                        link.setWeight(this.config.hypoWeight); // specific weight for hyponyms
                        link.setSource(node);
                        link.setTarget(hypoNode);
                        if (!node.getLinks().contains(link)) { // add the hypoonymlink if it doesn't already exist
                            node.addLink(link);
                        }
                    }
                }
            }
            for (Concept hyper : concept.getHypernyms()) {
                if (hyper != null && !Decomposition.getConcepts2Ignore().contains(hyper)) { // check if hyperonym is not on the ignore list and exists
                    Node hyperNode = nodes.get(hyper);
                    if (hyperNode == null) { // if the hyperonym node doesn't exist, add it to the graph recursively
                        hyperNode = addConceptRecursively(hyper, this.config.hyperThreshold); // TODO: evaluate threshold option!
                    }
                    if (hyperNode != null) {
                        SynonymLink link = new SynonymLink(); // create a new hyperonymlink
                        link.setWeight(this.config.hyperWeight); // specific synonym weight
                        link.setSource(node);
                        link.setTarget(hyperNode);
                        if (!node.getLinks().contains(link)) { // add the hyperonymlink if it doesn't already exist
                            node.addLink(link);
                        }
                    }
                }
            }
            for (Concept antonym : concept.getAntonyms()) {
                if (antonym != null && !Decomposition.getConcepts2Ignore().contains(antonym)) { // check if antonymonym is not on the ignore list and exists
                    Node antonymNode = nodes.get(antonym);
                    if (antonymNode == null) { // if the antonymonym node doesn't exist, add it to the graph recursively
                        antonymNode = addConceptRecursively(antonym, this.config.antoThreshold); // TODO: evaluate threshold option!
                    }
                    if (antonymNode != null) {
                        SynonymLink link = new SynonymLink(); // create a new antonymonymlink
                        link.setWeight(this.config.antoWeight); // specific antonym weight
                        link.setSource(node);
                        link.setTarget(antonymNode);
                        if (!node.getLinks().contains(link)) { // add the antonymonymlink if it doesn't already exist
                            node.addLink(link);
                        }
                    }
                }
            }
            for (Definition definition : concept.getDefinitions()) {
                for (Concept def : definition.getDefinition()) {
                    if (def != null && !Decomposition.getConcepts2Ignore().contains(def)) {
                        Node defNode = nodes.get(def);
                        if (defNode == null) {
                            defNode = addConceptRecursively(def, this.config.defThreshold); // TODO: evalute threshold option!
                        }
                        if (defNode != null) { // if the returned node is not empty, add it as a new node with a link
                            DefinitionLink link = new DefinitionLink();
                            link.setWeight(this.config.defWeight); // specific defenition weight
                            link.setSource(node);
                            link.setTarget(defNode);
                            if (!node.getLinks().contains(link)) { // changed !defNode to !node
                                node.addLink(link);
                            }
                        }
                    }
                }
            }
            return node;
        }
    }

    public void setInitialConcepts(HashMap<Concept, Double> concepts) {
        HashSet<Node> active = new HashSet<>();
        for (Map.Entry<Concept, Double> e : concepts.entrySet()) {
            initialConcepts.add(e.getKey());
            Node n = nodes.get(e.getKey());
            if (n == null)
                continue;
            n.addMarker(new TypedMarker(e.getValue(), e.getKey(), 0));
            active.add(n);
        }
        setActiveNodes(active);
    }


    public HashMap<Concept, Definition> getDefinitionOfInitialConcepts() {
        HashMap<Concept, Definition> r = new HashMap<>();
        //For earch concept of the sentence to do the WSD for.
        for (Concept concept : initialConcepts) {
            if (!Decomposition.getConcepts2Ignore().contains(concept)) {
                Definition highestDef = null;
                double highestAcc = 0.d;
                for (Definition def : concept.getDefinitions()) {
                    double acc = 0.d;
                    Definition tmp = def;
                    if (def.getSensekey() == null || def.getSensekey().equals(""))
                        continue;
                    for (Concept c : def.getDefinition()) {
                        if (!Decomposition.getConcepts2Ignore().contains(c)) {
                            TypedNode node = (TypedNode) nodes.get(c);
                            if (node != null) {
                                acc += node.getCumulativeWeightedActivation(); //node.getCumulativeActivation();
                            }
                        }
                        if (highestAcc < acc) {
                            highestDef = def;
                            highestAcc = acc;
                        }
                    }
                }
                r.put(concept, highestDef);
            }
        }
        return r;
    }


    public void start() {
        this.execute();
    }
}
