/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.owlConflictSolver.impl;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import de.kimanufaktur.nsm.owlConflictSolver.impl.rule.ConflictRule;
import de.kimanufaktur.nsm.owlConflictSolver.impl.rule.ConflictRuleRegistry;
import de.kimanufaktur.nsm.owlConflictSolver.local.OWLOntologyChangeListenerImpl;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * The ConflictSolver is the main entry point for solving given owl conflicts.
 * While initialization it loads all applied conflict rules from the
 * {@link ConflictRuleRegistry}.
 * 
 * The ConflictSolver can be invoked in two different ways:
 * 1.{@link ConflictSolver#resolveConflicts(PelletReasoner)} to search for all
 * conflicts in the whole ontology wrapped by the pellet reasoner
 * 
 * 2.{@link ConflictSolver#resolveConflicts(MultivaluedMap<OWLOntology,
 * OWLOntologyChange> changesByOntology)} (invoked through
 * {@link OWLOntologyChangeListenerImpl}) after any changes where applied to
 * search only in relevant classes for conflicts.
 * 
 */
public class ConflictSolver {

    private List<ConflictRule> rules = new ArrayList<>();

    public ConflictSolver() {
	prepareConflictSolver();
    }

    /**
     * Finds all conflicts for each given rule, they repair their known
     * conflicts and return the resulting ontology changes.
     * 
     * @param reasoner
     *            Passes pellet reasoner instead of only the ontology for
     *            further implementations of other rules, so one can use the
     *            pre-implemented functions of pellet.
     * 
     * @return Resulting ontology changes for resolving the found conflicts.
     */
    public void resolveConflicts(PelletReasoner pellet) {
	List<OWLOntologyChange> conflictChanges = new ArrayList<>();
	for (ConflictRule rule : rules) {
	    conflictChanges.addAll(rule.resolveConflicts(pellet));
	}

	applyChangesToOntology(pellet.getManager(), conflictChanges);
	pellet.refresh();
    }

    /**
     * Finds all conflicts for each given rule, they repair their known
     * conflicts and return the resulting ontology changes. It only concerns
     * relevant entities in the signature of the given changes.
     * 
     * @param changesByOntology
     *            each ontology change mapped to the concerning ontology.
     * 
     * @return Resulting ontology changes will be applied to their ontologies.
     */
    public void resolveConflicts(MultivaluedMap<OWLOntology, OWLOntologyChange> changesByOntology) {

	for (Entry<OWLOntology, List<OWLOntologyChange>> entry : changesByOntology.entrySet()) {
	    OWLOntology ont = entry.getKey();
	    OWLOntologyManager manager = ont.getOWLOntologyManager();
	    List<OWLOntologyChange> changes = entry.getValue();

	    PelletReasoner pellet = PelletReasonerFactory.getInstance().createReasoner(ont);
	    pellet.prepareReasoner();

	    List<OWLOntologyChange> conflictChanges = new ArrayList<>();
	    for (ConflictRule rule : rules) {
		for (OWLOntologyChange change : changes) {
		    if (rule.isApplicable(change)) {
			conflictChanges.addAll(rule.resolveConflictIncrementally(pellet, change));
		    }
		}
	    }
	    applyChangesToOntology(manager, conflictChanges);
	}
    }

    private void applyChangesToOntology(OWLOntologyManager manager, List<OWLOntologyChange> conflictChanges) {
	if (!conflictChanges.isEmpty()) {
	    manager.applyChanges(conflictChanges);
	}
    }

    private void prepareConflictSolver() {
	this.rules.addAll(ConflictRuleRegistry.getRegisteredConflictRules());
    }

}
