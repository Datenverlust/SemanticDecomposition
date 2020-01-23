/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model;

import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLConcept;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLRelation;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLRelationType;

/**
 * A relation in a semantic network.
 */
public class SemanticNetworkRelation implements OWLRelation {

    private final OWLConcept source;
    private final OWLConcept target;
    private final OWLRelationType type;
    private String name;

    public SemanticNetworkRelation(String name, OWLConcept source, OWLConcept target, OWLRelationType type) {
	this.name = name;
	this.source = source;
	this.target = target;
	this.type = type;
    }

    @Override
    public OWLConcept getSource() {
	return source;
    }

    @Override
    public OWLConcept getTarget() {
	return target;
    }

    @Override
    public OWLRelationType getType() {
	return type;
    }

    @Override
    public String getName() {
	return name;
    }

}
