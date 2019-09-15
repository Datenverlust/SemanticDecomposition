/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model;

import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLRelationType;

/**
 * All available relation types in the semantic net.
 */
public enum SemanticNetworkRelationType implements OWLRelationType {

    DECOMPOSITION("decomposition"),
    DEFINITION("definition"),
    HYPERNYM("hypernym"),
    HYPONYM("hyponym"),
    MERONYM("meronym"),
    ANTONYM("antonym"),
    ALT_ANTONYM("alternativeAntonym"),
    SYNONYM("synonym"),
    ALT_SYNONYM("alternativeSynonym"),
    DERIVATION("derivation"),
    FEATURE("feature");

    private final String name;

    private SemanticNetworkRelationType(String name) {
	this.name = name;
    }

    @Override
    public String getName() {
	return name;
    }

}
