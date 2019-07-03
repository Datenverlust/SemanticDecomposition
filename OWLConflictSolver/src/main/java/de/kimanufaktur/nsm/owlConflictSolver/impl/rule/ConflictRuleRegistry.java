/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.owlConflictSolver.impl.rule;

import de.kimanufaktur.nsm.owlConflictSolver.impl.ConflictSolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple rule registry to encapsulate {@link ConflictRule}'s and providing an
 * interface for {@link ConflictSolver}.
 * 
 */
public class ConflictRuleRegistry {

    public static List<? extends ConflictRule> getRegisteredConflictRules() {
	List<ConflictRule> registeredConflictRules = new ArrayList<>();

	registeredConflictRules.add(new ReflexiveISAConflictRule());

	return registeredConflictRules;
    }

}
