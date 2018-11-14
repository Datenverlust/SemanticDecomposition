/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.owlConflictSolver.impl.rule;

import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyChange;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

/**
 * Interface each new ConflictRule has to implement.
 *
 */
public interface ConflictRule {

    boolean isApplicable(OWLOntologyChange change);

    List<OWLOntologyChange> resolveConflicts(PelletReasoner reasoner);

    List<OWLOntologyChange> resolveConflictIncrementally(PelletReasoner reasoner, OWLOntologyChange change);

}
