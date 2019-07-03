/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.owlConverter;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.graph.edges.WeightedEdge;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLConcept;
import org.jgrapht.Graph;

import java.util.Collection;

/**
 * Each ToOWLConverter needs a parser which parses the specific knowledge graph
 * to an OWLOntology. Those parsers need to implement this interface.
 */
public interface Parser {

    Collection<OWLConcept> parse(Graph<Concept, WeightedEdge> graph);

}
