/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.kimanufaktur.nsm.decomposition.owlConverter.AbstractOWLOntologyFactory;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLProperty;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLRelation;
import de.kimanufaktur.nsm.decomposition.owlConverter.util.OWLNamespace;

public class OWLOntologyFactory extends AbstractOWLOntologyFactory {

    public OWLOntologyFactory() throws OWLOntologyCreationException {
	super();
    }

    public OWLOntologyFactory(OWLOntology owlOntology) throws OWLOntologyCreationException {
	super(owlOntology);
    }

    @Override
    protected OWLAxiom addProperty(OWLProperty property, OWLIndividual idv) {

	OWLDataProperty owlDataProperty = factory.getOWLDataProperty(property.getType().getIRI());
	OWLLiteral owlLiteral = factory.getOWLLiteral(property.getValueAsString(), property.getType());

	return factory.getOWLDataPropertyAssertionAxiom(owlDataProperty, idv,
		owlLiteral);
    }

    @Override
    protected OWLAxiom addRelation(OWLRelation relation) {
	OWLObjectProperty role = factory
		.getOWLObjectProperty(
			IRI.create(OWLNamespace.OWL_RELATION_NAMESPACE
				+ OWLNamespace.removeIllegalNamespaceCharacters(relation.getType().toString())));

	OWLNamedIndividual source = factory
		.getOWLNamedIndividual(
			IRI.create(OWLNamespace.OWL_INDIVIDUAL_NAMESPACE
				+ OWLNamespace.removeIllegalNamespaceCharacters(relation.getSource().getName())));
	OWLNamedIndividual target = factory
		.getOWLNamedIndividual(
			IRI.create(OWLNamespace.OWL_INDIVIDUAL_NAMESPACE
				+ OWLNamespace.removeIllegalNamespaceCharacters(relation.getTarget().getName())));

	return factory.getOWLObjectPropertyAssertionAxiom(role, source,
		target);
    }

}
