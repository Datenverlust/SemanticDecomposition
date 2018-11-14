/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.owlConverter.model;

import java.util.Collection;

/**
 * Represent common nodes in the OWL graph.
 *
 */
public interface OWLConcept extends OWLNamedEntity {

    String getType();

    Collection<OWLRelation> getRelations();

    void addRelations(Collection<OWLRelation> relations);

    Collection<OWLProperty> getProperties();

    void addProperties(Collection<OWLProperty> properties);

}
