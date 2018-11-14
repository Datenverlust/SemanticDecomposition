/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package test.test.de.dailab.nsm.decomposition;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.Definition;
import de.dailab.nsm.decomposition.WordType;
import de.dailab.nsm.decomposition.manualDefinition.model.Delegate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ghadh on 17.12.2015.
 */
public class CycleDefintionTest {
    Decomposition decomposition = null;
    Concept use = null;
    Delegate d = null;

    private void testInit() {
        decomposition = new Decomposition();
        decomposition.init();
        d = new Delegate();
        d.setDecomposition(decomposition);
        use = new Concept("use");
        use.setWordType(WordType.VB);

        Concept syn = new Concept("habituate");
        Concept syn2 = new Concept("accustom");
        Concept syn3 = new Concept("exercise");
        Concept c = new Concept("abnormal");
        Concept ant = new Concept("unusable");

        use.getSynonyms().add(syn);
        use.getAntonyms().add(ant);
        syn.getSynonyms().add(syn2);
        syn2.getAntonyms().add(c);
        syn2.getSynonyms().add(syn3);

    }

   //@Test
    public void cycleTestDefinedConcept() {
        testInit();
        boolean cycle = d.checkCycle(use, new Definition("the act of use"), 3);
        assertEquals(true, cycle);
    }

    //@Test
    public void cycleTestSynonym() {
        testInit();
        boolean cycle = d.checkCycle(use, new Definition("habituate"), 3);
        assertEquals(true, cycle);
    }


    //@Test
    public void cycleTestSynonymDepth2() {
        testInit();
        boolean cycle = d.checkCycle(use, new Definition("to accustom "), 2);
        assertEquals(true, cycle);
    }

    //@Test
    public void cycleTestDepth2() {
        testInit();
        boolean cycle = d.checkCycle(use, new Definition("exercise"), 1);
        assertEquals(false, cycle);
    }

    //@Test
    public void cycleTestAntonymDepth1() {
        testInit();
        boolean cycle = d.checkCycle(use, new Definition("something is not unusable "), 2);
        assertEquals(true, cycle);
    }

    //@Test
    public void cycleTestSynonymDepth3() {
        testInit();
        boolean cycle = d.checkCycle(use, new Definition("exercise"), 3);
        assertEquals(true, cycle);
    }

    //@Test
    public void cycleTestAntonymDepth3() {
        testInit();
        boolean cycle = d.checkCycle(use, new Definition("abnormal"), 3);
        assertEquals(true, cycle);
    }
    //@Test
    public void cycleTest() {
        testInit();
        Definition def = new Definition("employ for a particular purpose ");
        boolean cycle = d.checkCycle(use, def, 3);
        assertEquals(false, cycle);
    }
    //@Test
    public void cycleTestSynOfDef() {
        testInit();
        Concept syn = new Concept("use");
        Definition def = new Definition("employ for a particular purpose");
        def.getDefinition().get(0).getSynonyms().add(syn);
        use.getDefinitions().add(def);
        boolean cycle = d.checkCycle(use,use.getDefinitions().iterator().next(), 3);
        assertEquals(true, cycle);
    }

    //@Test
    public void getCycleTest() {
        testInit();
        List<Concept> list = new ArrayList<>();
        Concept d1= new Concept("make");
        Concept d2 = new Concept("work");
        list.add(d1);
        list.add(d2);
        Concept syn = new Concept("habituate");
        d1.getSynonyms().add(syn);
        Definition def =new Definition("");
        def.setDefinition(list);
        use.getDefinitions().add(def);
        Concept cycle = d.getConceptWithCycle(use,use.getDefinitions().iterator().next(), 3);
        assertEquals(d1, cycle);
    }
}
