/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.owlConverter.model;

import org.semanticweb.owlapi.vocab.OWL2Datatype;

/**
 * Interface for a property of an entity.
 *
 */
public interface OWLProperty {

    String getValueAsString();

    OWL2Datatype getType();
}
