/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.owlConflictSolver.impl.rule;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import de.kimanufaktur.nsm.owlConflictSolver.local.OWLOntologyChangeListenerImpl;
import de.kimanufaktur.nsm.owlConflictSolver.util.Messages;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rule to resolve reflexive ISA relation conflicts by removing them. Rule can
 * be invoked in two different ways:
 * 1.{@link ReflexiveISAConflictRule#resolveConflicts(PelletReasoner)} to search
 * for all conflicts in the whole ontology wrapped by the pellet reasoner
 * 
 * 2.{@link ReflexiveISAConflictRule#resolveConflictsIncrementally(PelletReasoner, List)}
 * (invoked through {@link OWLOntologyChangeListenerImpl}) after any changes
 * where applied to search only in relevant classes for conflicts. In this case
 * the rule determines if its applicable
 * {@link ReflexiveISAConflictRule#isApplicable(List)} for at least one of the
 * changes to save unnecessary computation time.
 */
public class ReflexiveISAConflictRule implements ConflictRule {

    private final Logger logger = Logger.getRootLogger();

    protected ReflexiveISAConflictRule() {
    }

    /**
     * Rule is only applicable to AddAxioms (because a reflexive IS_A relation
     * won't appear by removing axioms).
     */
    @Override
    public boolean isApplicable(OWLOntologyChange change) {
	return change.isAddAxiom();
    }

    /**
     * Finds all reflexive ISA conflicts, repairs them (by removing) and returns
     * the resulting ontology changes.
     * 
     * @param reasoner
     *            Passes pellet reasoner instead of only the ontology for
     *            further implementations of other rules, so one can use the
     *            pre-implemented functions of pellet.
     * 
     * @return Resulting ontology changes for resolving the found conflicts.
     */
    @Override
    public List<OWLOntologyChange> resolveConflicts(PelletReasoner reasoner) {
	List<OWLOntologyChange> conflictChanges = new ArrayList<>();
	OWLOntology ont = reasoner.getRootOntology();

	for (OWLClass owlClass : ont.getClassesInSignature()) {
	    if (owlClass.getSubClasses(ont).contains(owlClass) || owlClass.getSuperClasses(ont).contains(owlClass)) {
		conflictChanges.addAll(resolveConflict(ont, owlClass));
	    }
	}
	return conflictChanges;
    }

    /**
     * Finds all reflexive ISA conflicts, repairs them (by removing) and returns
     * the resulting ontology changes. It only concerns relevant
     * {@link OWLClass}es in the signature of the given change.
     * 
     * @param reasoner
     *            Passes pellet reasoner instead of only the ontology for
     *            further implementations of other rules, so one can use the
     *            pre-implemented functions of pellet.
     * 
     * @param change
     *            Ontology was changed anyhow, the
     *            {@link OWLOntologyChangeListenerImpl} registered them and
     *            invokes this function. The changes are already applied to the
     *            ontology, so this is a post-changes function. The given change
     *            is one of these changes and applicable to this conflict rule
     *            (see {@link ReflexiveISAConflictRule.isApplicable(change)}).
     * 
     * @return Resulting ontology changes for resolving the found conflicts.
     */
    @Override
    public List<OWLOntologyChange> resolveConflictIncrementally(PelletReasoner reasoner, OWLOntologyChange change) {
	OWLOntology ont = reasoner.getRootOntology();

	return change.getSignature().stream().filter(e -> e.isOWLClass()).flatMap(c -> resolveConflict(ont, (OWLClass) c).stream())
		.collect(Collectors.toList());
    }

    /**
     * Removes the class itself from it's explicit sub- and super classes. The
     * {@link OWLOntologyManager} only creates changes if the axioms were
     * removed from the ontology (so there are no changes if nothing happened).
     * 
     * @param ont
     * @param conflictedClass
     * @return
     */
    private List<OWLOntologyChange> resolveConflict(OWLOntology ont, OWLClass conflictedClass) {
	OWLOntologyManager manager = ont.getOWLOntologyManager();
	OWLDataFactory factory = manager.getOWLDataFactory();

	OWLSubClassOfAxiom subClassAxiom = factory.getOWLSubClassOfAxiom(conflictedClass, conflictedClass);
	OWLSubClassOfAxiom superClassAxiom = factory.getOWLSubClassOfAxiom(conflictedClass, conflictedClass);
	List<OWLOntologyChange> removeAxioms = manager.removeAxioms(ont, new HashSet<>(Arrays.asList(subClassAxiom, superClassAxiom)));

	if (!removeAxioms.isEmpty()) {
	    logger.info(Messages.resolvedNonReflexiveISAConflict +
		    conflictedClass.getIRI().getFragment());
	}

	return removeAxioms;
    }

}
