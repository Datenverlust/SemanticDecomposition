/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.owlConflictSolver.local;

import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

import de.dailab.nsm.owlConflictSolver.impl.ConflictSolver;

/**
 * Need to be applied to an ontology. Then it listens to ontology changes and
 * will get invoked after the changes will get applied. It maps the changes to
 * the related ontologies and passes them to the {@link ConflictSolver} to
 * resolve known conflicts. It will get invoked as long as there are no conflict
 * changes anymore, so there is a last call after applying the conflict changes
 * to check that no new conflicts were created.
 *
 */
public class OWLOntologyChangeListenerImpl implements OWLOntologyChangeListener {

    @Override
    public void ontologiesChanged(List<? extends OWLOntologyChange> impendingChanges) throws OWLException {
	MultivaluedMap<OWLOntology, OWLOntologyChange> changesByOntology = getChangesByOntology(impendingChanges);
	new ConflictSolver().resolveConflicts(changesByOntology);
    }

    private MultivaluedMap<OWLOntology, OWLOntologyChange> getChangesByOntology(List<? extends OWLOntologyChange> impendingChanges) {
	MultivaluedMap<OWLOntology, OWLOntologyChange> addAxiomsByOntology = new MultivaluedHashMap<>();

	for (OWLOntologyChange owlOntologyChange : impendingChanges) {
	    if (owlOntologyChange != null) {
		addAxiomsByOntology.add(owlOntologyChange.getOntology(), owlOntologyChange);
	    }
	}
	return addAxiomsByOntology;
    }

}
