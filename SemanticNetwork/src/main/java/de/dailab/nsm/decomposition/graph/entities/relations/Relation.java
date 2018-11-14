/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.entities.relations;

import de.dailab.nsm.decomposition.graph.entities.Entity;

import java.util.List;

/**
 * Created by faehndrich on 01.05.15.
 */
public class Relation extends Entity {

    List<Role> roles = null;
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
    public void addRole(Role role){
        getRoles().add(role);
    }

}
