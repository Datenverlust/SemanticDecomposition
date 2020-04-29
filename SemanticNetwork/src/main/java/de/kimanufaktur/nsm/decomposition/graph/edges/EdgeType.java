/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.graph.edges;

import java.io.Serializable;

/**
 * Enumeration of the considered edge types in a graph.
 * Created by faehndrich on 21.11.15.
 */

public enum EdgeType implements Serializable {
    Synonym, Antonym, Definition, Hypernym, Hyponym, Meronym, Arbitrary, Syntax, SemanticRole, NamedEntity, Unknown
}
