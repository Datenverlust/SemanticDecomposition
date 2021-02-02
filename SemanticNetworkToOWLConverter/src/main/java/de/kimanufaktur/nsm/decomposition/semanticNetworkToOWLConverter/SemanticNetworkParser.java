/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Definition;
import de.kimanufaktur.nsm.decomposition.WordType;
import de.kimanufaktur.nsm.decomposition.graph.edges.WeightedEdge;
import de.kimanufaktur.nsm.decomposition.owlConverter.Parser;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLConcept;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLProperty;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLRelation;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLRelationType;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkConcept;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkProperty;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkRelation;
import de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model.SemanticNetworkRelationType;

public final class SemanticNetworkParser implements Parser {

    private Map<String, OWLConcept> conceptByName = new HashMap<>();

    /**
     * Parses a given graph to a Collection of OWLConcepts. The nodes needn't to
     * be connected to the rest of the graph. It first creates concepts and adds
     * properties and relations afterwards to prevent infinite loops.
     * 
     * @param graph
     *            The graph to parse.
     * @return Collection of OWLConcepts created from graph vertexes.
     */
    @Override
    public Collection<OWLConcept> parse(Graph<Concept, WeightedEdge> graph) {
	for (Concept concept : graph.vertexSet()) {
	    if (concept != null) {
		conceptByName.put(concept.getLitheral(), getOWLConcept(concept));
	    }
	}

	for (Concept concept : graph.vertexSet()) {
	    OWLConcept owlConcept = conceptByName.get(concept.getLitheral());
	    owlConcept.addProperties(getProperties(concept));
	    owlConcept.addRelations(getRelations(concept));
	}

	return conceptByName.values();
    }

    private OWLConcept getOWLConcept(Concept concept) {
	String name = concept.getLitheral();

	if (!conceptByName.containsKey(name)) {
	    WordType wordType = concept.getWordType();
	    return new SemanticNetworkConcept(concept.getId(), concept.getLitheral(),
		    wordType != null ? wordType.type() : null, new HashSet<>(),
		    new HashSet<>());
	}

	return conceptByName.get(name);
    }

    // Extension for later works: You can get all properties dynamically from a
    // concept, maybe through annotations.
    private Collection<OWLProperty> getProperties(Concept concept) {
	Collection<OWLProperty> properties = new HashSet<>();
	properties.add(new SemanticNetworkProperty(String.valueOf(concept.getDecompositionElementCount()), OWL2Datatype.XSD_INTEGER));
	properties.add(new SemanticNetworkProperty(String.valueOf(concept.getDecompositionlevel()), OWL2Datatype.XSD_INTEGER));
	properties.add(new SemanticNetworkProperty(String.valueOf(concept.getLemma()), OWL2Datatype.XSD_NAME));
	properties.add(new SemanticNetworkProperty(String.valueOf(concept.getNer()), OWL2Datatype.XSD_NAME));
	return properties;
    }

    // Note: Same as getProperties(Concept concept)
    private Collection<OWLRelation> getRelations(Concept concept) {
	Collection<OWLRelation> relations = new HashSet<>();
	relations.addAll(getCommonRelations(concept, concept.getAlternativeSyn(), SemanticNetworkRelationType.ALT_SYNONYM));
	relations.addAll(getCommonRelations(concept, concept.getAlternativeAnt(), SemanticNetworkRelationType.ALT_ANTONYM));
	relations.addAll(getCommonRelations(concept, concept.getAllFeatures(), SemanticNetworkRelationType.FEATURE));
	relations.addAll(getCommonRelations(concept, concept.getAntonyms(), SemanticNetworkRelationType.ANTONYM));
	relations.addAll(getCommonRelations(concept, concept.getDecomposition(), SemanticNetworkRelationType.DECOMPOSITION));
	relations.addAll(getCommonRelations(concept, concept.getDerivations(), SemanticNetworkRelationType.DERIVATION));
	relations.addAll(getCommonRelations(concept, concept.getHypernyms(), SemanticNetworkRelationType.HYPERNYM));
	relations.addAll(getCommonRelations(concept, concept.getHyponyms(), SemanticNetworkRelationType.HYPONYM));
	relations.addAll(getCommonRelations(concept, concept.getMeronyms(), SemanticNetworkRelationType.MERONYM));
	relations.addAll(getCommonRelations(concept, concept.getSynonyms(), SemanticNetworkRelationType.SYNONYM));

	for (Definition definition : concept.getDefinitions()) {
	    relations.addAll(getCommonRelations(concept, definition.getDefinition(), SemanticNetworkRelationType.DEFINITION));
	}

	return relations;
    }

    private Collection<OWLRelation> getCommonRelations(Concept source, Collection<Concept> targetConcepts,
	    OWLRelationType type) {
	return targetConcepts.stream().filter(a -> a != null)
		.map(s -> new SemanticNetworkRelation(type.toString(), getOWLConcept(source), getOWLConcept(s),
			type))
		.collect(Collectors.toSet());
    }

}
