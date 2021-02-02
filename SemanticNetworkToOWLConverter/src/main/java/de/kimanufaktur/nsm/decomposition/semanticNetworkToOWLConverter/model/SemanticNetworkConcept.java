/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.semanticNetworkToOWLConverter.model;

import java.util.Collection;
import java.util.HashSet;

import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLConcept;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLProperty;
import de.kimanufaktur.nsm.decomposition.owlConverter.model.OWLRelation;

/**
 * A concept of a semantic network. It's a possible node type in the graph. A
 * concept is the only mutable entity for the translation because a removal of a
 * concept is more complicated than a change (unless for relations or
 * properties).
 *
 */
public class SemanticNetworkConcept implements OWLConcept {

    private final String name;
    private final String type;
    private final Long id;
    private Collection<OWLProperty> properties;
    private Collection<OWLRelation> relations;

    public SemanticNetworkConcept(Long id, String name, String type, Collection<OWLProperty> properties,
	    Collection<OWLRelation> relations) {
	this.id = id;
	this.name = name;
	this.type = type;
	this.properties = properties != null ? properties : new HashSet<>();
	this.relations = relations != null ? relations : new HashSet<>();

    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public Collection<OWLProperty> getProperties() {
	return properties;
    }

    @Override
    public Collection<OWLRelation> getRelations() {
	return relations;
    }

    @Override
    public String getType() {
	return type;
    }

    public Long getId() {
	return id;
    }

    @Override
    public void addProperties(Collection<OWLProperty> properties) {
	this.properties.addAll(properties);
    }

    @Override
    public void addRelations(Collection<OWLRelation> relations) {
	for (OWLRelation relation : relations) {
	    if (relation.getSource().getName().equals(name)) {
		this.relations.add(relation);
	    }
	}
    }

}
