/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.owlConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLConcept;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLProperty;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLRelation;
import de.kimanufaktur.nsm.decomposition.owlConverter.util.OWLNamespace;

/**
 * This factory wraps the OWLDataFactory. By constructing this factory, an
 * owlManager, an owlFactory and an owlOntology will be created. An owlManager
 * manages only one ontology at a time. So this factory manages only one
 * ontology at a time too. You can create a new ontology or work with a given
 * one to extend it with basic owl components.
 * 
 * All changes of the ontology are collected in 'changes' and applied at once
 * for a better performance.
 * 
 * Feel free to extend the factory for further components.
 * 
 * @author Maik
 */
public abstract class AbstractOWLOntologyFactory {

    private final static OWLOntologyManager MANAGER = OWLManager.createOWLOntologyManager();

    protected final OWLDataFactory factory;
    private final OWLOntology ontology;
    private final Queue<AddAxiom> changes = new LinkedList<AddAxiom>();

    /**
     * Creates a new factory and a new ontology, the factory works with.
     * 
     * @throws OWLOntologyCreationException
     *             Occurs when an ontology with the new generated ID already
     *             exists, thus never our fault.
     */
    public AbstractOWLOntologyFactory() throws OWLOntologyCreationException {
	this.factory = MANAGER.getOWLDataFactory();
	this.ontology = MANAGER.createOntology();
    }

    /**
     * Creates a new factory for a given ontology.
     * 
     * @param ontology
     *            Ontology the factory works with.
     * @throws OWLOntologyCreationException
     *             Occurs when an ontology with the given IRI already exists.
     */
    public AbstractOWLOntologyFactory(OWLOntology ontology) throws OWLOntologyCreationException {
	this.factory = MANAGER.getOWLDataFactory();
	this.ontology = MANAGER.createOntology(ontology.getOntologyID().getOntologyIRI(),
		Collections.singleton(ontology));
    }

    /**
     * Loads an OWLOntology from the given document IRI.
     * 
     * @param ontologyDocument
     *            IRI specifier of the owl document file.
     * @return Loaded OWLOntology.
     * @throws OWLOntologyCreationException
     *             Occurs when something went wrong. Thrown by
     *             OWLOntologyManager.
     */
    public static OWLOntology loadOntology(IRI ontologyDocument) throws OWLOntologyCreationException {
	return MANAGER.loadOntologyFromOntologyDocument(ontologyDocument);
    }

    /**
     * Saves the managed ontology to the specified document.
     * 
     * @param documentIRI
     *            IRI specifier of the owl document file.
     * @throws OWLOntologyStorageException
     *             Occurs when something went wrong. Thrown by
     *             OWLOntologyManager.
     */
    public void saveOntology(IRI documentIRI) throws OWLOntologyStorageException {
	MANAGER.saveOntology(ontology, documentIRI);
    }

    /**
     * Adds a whole concept with all it's parts to the ontology. Contains check
     * of every entity is done by the OWLDataFactory.
     * 
     * @pre All constraints of the semantic network hold so that a further check
     *      is not needed.
     * @pre Ontology and concept are not null.
     * @post Concept, all it's relations and target concepts will add recursivly
     *       to the ontology as an axiom.
     * 
     * @param ontology
     *            The ontology the concept shall be add to. (not null)
     * @param concept
     *            The cocept to add to the ontology. (not null)
     */
    public OWLNamedIndividual addConceptRecursivly(OWLConcept concept) {
	OWLNamedIndividual idv = null;

	if (ontology != null && concept != null) {
	    idv = addIndividual(concept);
	    addProperties(concept.getProperties(), idv);
	    addRelations(concept.getRelations());
	    applyChangesToOntology();
	}

	return idv;
    }

    /**
     * Adds properties to an OWLIndividual.
     * 
     * @param properties
     *            Properties to add to the individual. (not null)
     * @param owlIndividual
     *            The individual the properties shall add to. It has to be part
     *            of the ontology signature. (not null)
     */
    public void addProperties(Collection<OWLProperty> properties, OWLNamedIndividual owlIndividual) {

	if (properties != null && owlIndividual != null) {
	    for (OWLProperty property : properties) {
		if (property != null) {
		    addOntologyChange(addProperty(property, owlIndividual));
		}
	    }
	    applyChangesToOntology();
	}
    }

    /**
     * Adds relations to an OWLIndividual.
     * 
     * @param relations
     *            Relations to add to the individual. (not null)
     * @param owlIndividual
     *            The individual the relations shall add to. (not null)
     */
    public void addRelations(Collection<OWLRelation> relations) {
	if (relations != null) {
	    for (OWLRelation relation : relations) {
		if (relation != null && relation.getSource() != null && relation.getTarget() != null) {
		    addOntologyChange(addRelation(relation));
		}
	    }
	    applyChangesToOntology();
	}
    }

    /**
     * Adds a property to the specified individual of the ontology. Each
     * ToOWLConverter shall decide itself how to add a property internally, so
     * it's possible to use owl2 features like negation, symmetric, reflexive,
     * ... properties
     * 
     * @param property
     * @param owlIndividual
     * @return
     */
    protected abstract OWLAxiom addProperty(OWLProperty property, OWLIndividual owlIndividual);

    /**
     * Adds a relation to the ontology. Each ToOWLConverter shall decide itself
     * how to add a relation internally.
     * 
     * @param relation
     *            Relation to add to the ontology
     * @return OWLAxiom which describes the addition of the relation
     */
    protected abstract OWLAxiom addRelation(OWLRelation relation);

    /**
     * Creates a new individual from a concept and adds it to the ontology
     * changes. The concept's type determines the individuals class.
     * 
     * @param concept
     *            The new individual.
     * @return A new OWLIndividual.
     */
    private OWLNamedIndividual addIndividual(OWLConcept concept) {
	OWLNamedIndividual idv = factory
		.getOWLNamedIndividual(IRI
			.create(OWLNamespace.OWL_INDIVIDUAL_NAMESPACE + OWLNamespace.removeIllegalNamespaceCharacters(concept.getName())));
	OWLClass clazz = addClass(concept);
	addOntologyChange(factory.getOWLClassAssertionAxiom(clazz, idv));
	return idv;
    }

    /**
     * Adds an OWLClass to the managed ontology.
     * 
     * @param concept
     *            The type of that concept will be used to create the OWLClass.
     * @return A new or already created OWLClass.
     */
    private OWLClass addClass(OWLConcept concept) {
	String type = concept.getType();
	return factory.getOWLClass(
		IRI.create(
			OWLNamespace.OWL_CLASS_NAMESPACE
				+ (type != null ? OWLNamespace.removeIllegalNamespaceCharacters(type.toString()) : StringUtils.EMPTY)));
    }

    /**
     * Adds a new OWLAxiom for changing the ontology.
     * 
     * @param axiom
     *            A new OWLAxiom for changing the ontology
     * 
     */
    private void addOntologyChange(OWLAxiom axiom) {
	changes.add(new AddAxiom(ontology, axiom));
    }

    /**
     * Applies all collected changes for the managed ontology.
     */
    private void applyChangesToOntology() {
	for (AddAxiom addAxiom : changes) {
	    MANAGER.applyChange(addAxiom);
	}
    }
}