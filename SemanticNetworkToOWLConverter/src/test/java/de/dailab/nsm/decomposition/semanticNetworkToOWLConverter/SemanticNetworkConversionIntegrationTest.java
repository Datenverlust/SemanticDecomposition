/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.semanticNetworkToOWLConverter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.graph.conceptCache.GraphUtil;
import de.dailab.nsm.decomposition.graph.edges.WeightedEdge;
import de.dailab.nsm.decomposition.owlConverter.model.OWLConcept;
import de.dailab.nsm.decomposition.owlConverter.model.OWLProperty;
import de.dailab.nsm.decomposition.owlConverter.model.OWLRelation;
import de.dailab.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkRelationType;

/**
 * Integration test. Decomposes a word, creates a semanticNetwork out of it and
 * translates it to owl.
 * 
 * Choose a word for [WORD_TO_COMPOSE] (that exists in one of the configured
 * sources of the {@link Decomposition}) to test.
 */
public class SemanticNetworkConversionIntegrationTest {

    public static final String WORD_TO_COMPOSE = "bird";

    private Graph<Concept, WeightedEdge> semanticNetwork;
    private OWLOntologyFactory owlOntologyFactory;

    @Before
    public void init() throws OWLOntologyCreationException {
	Decomposition.main(new String[] { WORD_TO_COMPOSE });
	semanticNetwork = GraphUtil.createJGraph(Decomposition.getConcept());
	owlOntologyFactory = new OWLOntologyFactory();
    }

    @Test
    public void createSemanticNetworkTest() {
	assertFalse(semanticNetwork.vertexSet().isEmpty());
    }

    @Test
    public void semanticNetworkToOntologyTest() throws OWLOntologyStorageException, OWLOntologyCreationException, IOException {
	Collection<OWLConcept> owlConcepts = new SemanticNetworkParser().parse(semanticNetwork);

	for (OWLConcept concept : owlConcepts) {
	    owlOntologyFactory.addConceptRecursivly(concept);
	}

	OWLOntology ontology = OWLOntologyTestDataProvider.getTestOntology(owlOntologyFactory);
	assertFalse(ontology.isEmpty());

	checkEntities(semanticNetwork, owlConcepts);

    }

    private void checkEntities(Graph<Concept, WeightedEdge> semanticNetwork2, Collection<OWLConcept> owlConcepts) {
	Map<String, OWLConcept> owlConceptsByName = owlConcepts
		.stream()
		.collect(
			Collectors.toMap(
				o -> o.getName(),
				o -> o));

	Map<Concept, OWLConcept> owlConceptBySemanticNetConcept = semanticNetwork.vertexSet()
		.stream()
		.collect(
			Collectors.toMap(
				c -> c,
				c -> owlConceptsByName.get(c.getLitheral())));

	for (Entry<Concept, OWLConcept> entry : owlConceptBySemanticNetConcept.entrySet()) {
	    Concept key = entry.getKey();
	    OWLConcept value = entry.getValue();

	    assertTrue(key.getLitheral().equals(value.getName()));
	    if (key.getWordType() != null) {
		assertTrue(key.getWordType().type().equals(value.getType()));
	    }

	    checkRelations(key, value);
	    checkProperties(key, value);
	}
    }

    private void checkRelations(Concept concept, OWLConcept owlConcept) {
	Collection<OWLRelation> relations = owlConcept.getRelations();
	Collection<String> owlRelationTypes = getOWLRelationTypes(relations);

	String relationType = SemanticNetworkRelationType.ANTONYM.getName();
	checkRelations(getEntityNames(concept.getAntonyms()), relationType, getOWLEntityNames(relations, relationType),
		owlRelationTypes);

	relationType = SemanticNetworkRelationType.HYPERNYM.getName();
	checkRelations(getEntityNames(concept.getHypernyms()), relationType, getOWLEntityNames(relations, relationType.toLowerCase()),
		owlRelationTypes);

	relationType = SemanticNetworkRelationType.HYPONYM.getName();
	checkRelations(getEntityNames(concept.getHyponyms()), relationType, getOWLEntityNames(relations, relationType),
		owlRelationTypes);

	relationType = SemanticNetworkRelationType.MERONYM.getName();
	checkRelations(getEntityNames(concept.getMeronyms()), relationType, getOWLEntityNames(relations, relationType),
		owlRelationTypes);

	relationType = SemanticNetworkRelationType.SYNONYM.getName();
	checkRelations(getEntityNames(concept.getSynonyms()), relationType, getOWLEntityNames(relations, relationType),
		owlRelationTypes);

	relationType = SemanticNetworkRelationType.ALT_ANTONYM.getName();
	checkRelations(getEntityNames(concept.getAlternativeAnt()), relationType,
		getOWLEntityNames(relations, relationType),
		owlRelationTypes);

	relationType = SemanticNetworkRelationType.DERIVATION.getName();
	checkRelations(getEntityNames(concept.getDerivations()), relationType, getOWLEntityNames(relations, relationType),
		owlRelationTypes);

    }

    private void checkProperties(Concept key, OWLConcept value) {
	Collection<OWLProperty> properties = value.getProperties();
	Collection<String> owlPropertyValues = getOWLPropertyValues(properties);
	assertTrue(owlPropertyValues.contains(String.valueOf(key.getDecompositionlevel())));
	assertTrue(owlPropertyValues.contains(String.valueOf(key.getDecompositionElementCount())));
	assertTrue(owlPropertyValues.contains(String.valueOf(key.getLemma())));
	assertTrue(owlPropertyValues.contains(String.valueOf(key.getNer())));

    }

    private void checkRelations(Collection<String> targetNames, String expectedType, Collection<String> translatedTargetNames,
	    Collection<String> translatedTypes) {
	if (!targetNames.isEmpty()) {
	    assertTrue(
		    translatedTypes.contains(expectedType.toLowerCase()));
	    assertTrue(translatedTargetNames.containsAll(targetNames));
	}
    }

    private Collection<String> getEntityNames(Collection<Concept> concepts) {
	return concepts.stream().map(a -> a.getLitheral()).collect(Collectors.toSet());
    }

    private Collection<String> getOWLEntityNames(Collection<OWLRelation> concepts, String relationType) {
	return concepts.stream().filter(a -> a.getType().getName().equals(relationType)).map(a -> a.getTarget().getName())
		.collect(Collectors.toSet());
    }

    private Collection<String> getOWLRelationTypes(Collection<OWLRelation> relations) {
	return relations.stream().map(a -> a.getType().getName()).collect(Collectors.toSet());
    }

    private Collection<String> getOWLPropertyValues(Collection<OWLProperty> properties) {
	return properties.stream().map(p -> p.getValueAsString()).collect(Collectors.toSet());
    }

}
