/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.semanticNetworkToOWLConverter;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.graph.conceptCache.GraphUtil;
import de.dailab.nsm.decomposition.graph.edges.WeightedEdge;
import de.dailab.nsm.decomposition.owlConverter.model.OWLConcept;

public class SemanticNetworkConversionPerformanceTest {

    public static final String[] WORDS_TO_COMPOSE = { "bird", "human", "egg",
	    "use", "computer", "leaf", "science", "hedgehog", "letter",
	    "fish stick", "black", "sky", "tall", "imagination", "song", "apple", "meaning", "gravity", "god",
	    "bee" };
    private static final Logger logger = Logger.getLogger(SemanticNetworkConversionPerformanceTest.class);

    private OWLOntologyFactory owlOntologyFactory;

    private long allTime = 0;
    private int allNodes = 0;
    private int allRelations = 0;

    @Test
    public void wordToOWLOntologyPerformanceTest() throws OWLOntologyCreationException {
	for (int i = 0; i < WORDS_TO_COMPOSE.length; ++i) {

	    Graph<Concept, WeightedEdge> semanticNetwork = wordToSemanticNetwork(WORDS_TO_COMPOSE[i]);

	    long startTime = System.currentTimeMillis();

	    semanticNetworkToOWLOntology(semanticNetwork);

	    int nodes = semanticNetwork.vertexSet().size();
	    int relations = semanticNetwork.edgeSet().size();
	    logger.info(getFormattetRuntime(startTime) + getFormattedEntityAmountInfo(nodes, relations));
	    rememberStatistic(startTime, nodes, relations);
	}
	logger.info(getFormattetRuntime(allTime) + getFormattedEntityAmountInfo(allNodes, allRelations));
    }

    private static Graph<Concept, WeightedEdge> wordToSemanticNetwork(String word) {
	Decomposition.main(new String[] { word });
	return GraphUtil.createJGraph(Decomposition.getConcept());
    }

    private void semanticNetworkToOWLOntology(Graph<Concept, WeightedEdge> semanticNetwork) throws OWLOntologyCreationException {
	owlOntologyFactory = new OWLOntologyFactory();
	Collection<OWLConcept> owlConcepts = new SemanticNetworkParser().parse(semanticNetwork);

	for (OWLConcept concept : owlConcepts) {
	    owlOntologyFactory.addConceptRecursivly(concept);
	}
    }

    private static String getFormattedEntityAmountInfo(int nodeCount, int relationCount) {
	return "Nodes: " + nodeCount + "\n" + "Relations: " + relationCount + "\n";
    }

    private static String getFormattetRuntime(long start) {
	return (System.currentTimeMillis() - start) + " milliseconds needed.\n";
    }

    private void rememberStatistic(long startTime, int nodeCount, int relationCount) {
	allTime += startTime;
	allNodes += nodeCount;
	allRelations += relationCount;
    }
}
