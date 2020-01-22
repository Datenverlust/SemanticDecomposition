/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.graph.entities.relations;

/**
 * Created by faehndrich on 01.05.15.
 */

import de.kimanufaktur.nsm.graph.entities.Entity;

/**
 * This is the lowerst edge in the graph and is implemented through the DefaultWeightedEdge of the Jgrapht framework
 * A role has a direction and a name. While activation these are the edges on which activation is passed. Relationships
 * are broken down to the point where they only contain roles.
 */
public class Role extends Relation {
    String Name;
    Entity source;
    Entity target;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Entity getSource() {
        return source;
    }

    public void setSource(Entity source) {
        this.source = source;
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }
}
