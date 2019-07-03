/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package java.de.kimanufaktur.nsm.decomposition;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Decomposition;
import de.kimanufaktur.nsm.decomposition.Definition;
import de.kimanufaktur.nsm.decomposition.WordType;
import de.kimanufaktur.nsm.decomposition.manualDefinition.model.Delegate;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ghadh on 25.10.2015.
 * for run this test, we have to commit the interface to the JavaFX-Application in the class "Decomposition",
 * because only the logic will be tested
 */

public class ManualDefinitionTest {

    Decomposition decomposition = null;
    Delegate d = null;

    private void testInit() {
        decomposition = new Decomposition();
        Decomposition.init();
        d = new Delegate();
        d.setDecomposition(decomposition);

    }

    //@Test
    public void testDefinition() throws Exception {
        testInit();
        /* nothing: antonym of the prime "something"
         * get: synonym of the prime "do" */
        Definition definition = new Definition(
                "the principal activity in your life that you do to make " +
                        " money that nothing you can get without it");
        Concept job = new Concept("job");
        Concept primary = decomposition.multiThreadedDecompose("primary", WordType.NN, 2); /* synonym of "principle" */
        Concept death = decomposition.multiThreadedDecompose("death", WordType.NN, 2); /* antonym of "life" */
        Concept money = decomposition.multiThreadedDecompose("money", WordType.NN, 2); /* just a composed concept */
        job.getDefinitions().add(definition);
        job = d.checkDefinitionForSelection(job);
        Definition exceptedDef = new Definition(
                "the primary activity in your not death that you do to do" +
                        "  money that not something you can do without it");
        Definition resultDef = job.getDefinitions().iterator().next();
        assertEquals(exceptedDef.toString(), resultDef.toString());
    }
}