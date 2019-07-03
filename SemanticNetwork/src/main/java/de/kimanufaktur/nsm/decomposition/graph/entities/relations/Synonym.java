/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.graph.entities.relations;

import de.kimanufaktur.nsm.decomposition.graph.entities.Entity;

import java.util.List;

/**
 * Relation represnting the synonym relation.
 * Created by faehndrich on 01.05.15.
 */
public class Synonym extends Relation {
    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public List<Entity> getSememe() {
        return sememe;
    }

    public void setSememe(List<Entity> sememe) {
        this.sememe = sememe;
    }

    Entity entity;
    List<Entity> sememe;

}
