/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.owlConverter.model;

/**
 * Interface for relations. A relation can be modelled as a vertex (to add
 * properties for vertexes) or an edge in the graph.
 *
 */
public interface OWLRelation extends OWLNamedEntity {

    OWLConcept getSource();

    OWLConcept getTarget();

    OWLRelationType getType();
}
