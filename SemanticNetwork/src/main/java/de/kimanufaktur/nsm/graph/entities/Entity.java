/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.graph.entities;

import de.kimanufaktur.nsm.decomposition.graph.entities.relations.Relation;

import java.util.List;

/**
 * Created by faehndrich on 01.05.15.
 */
public class Entity {

    List<Relation> relations = null;
    String Name = null;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }
    public void addRelation(Relation relation){
        getRelations().add(relation);
    }

    /**
     * Check if this entity is an relationship
     * @return boolean indicating if this is a relationship or not
     */
    public boolean isRelationship(){
       return relations.size() != 0;
    }

}
