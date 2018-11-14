/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.semanticNetworkToOWLConverter.model;

import org.semanticweb.owlapi.vocab.OWL2Datatype;

import de.dailab.nsm.decomposition.owlConverter.model.OWLProperty;

/**
 * A property of a semantic network entity.
 *
 */
public class SemanticNetworkProperty implements OWLProperty {

    private final String value;
    private final OWL2Datatype type;

    public SemanticNetworkProperty(String value, OWL2Datatype type) {
	this.value = value;
	this.type = type;
    }

    @Override
    public String getValueAsString() {
	return value;
    }

    @Override
    public OWL2Datatype getType() {
	return type;
    }

}
