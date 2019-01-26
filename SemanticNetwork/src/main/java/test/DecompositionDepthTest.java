/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package test;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.WordType;
import de.dailab.nsm.decomposition.graph.conceptCache.GraphUtil;
import org.jgrapht.Graph;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by faehndrich on 15.06.16.
 */
@RunWith(Parameterized.class)
public class DecompositionDepthTest {

    private final Decomposition decomposition = new Decomposition();
    int decompositionDepth = 0;

    @Parameterized.Parameters
    public static Collection<Object[]>
    generateParams() {
        List<Object[]> params =
                new ArrayList<Object[]>();
        for (int i = 1; i <= 6; i++) {
            params.add(new Object[] {i});
        }
        return params;
    }


    public DecompositionDepthTest(int param){
        decompositionDepth = param;

    }

    @Before
    public void before() {
        Decomposition.init();
    }


    /**
     * Method: test the decomposition at depth 1
     */
    @Test
    public void testDecompositionDepth() throws Exception {

        Concept concept = decomposition.decompose("use", WordType.NN,decompositionDepth);
        Assert.assertNotNull(concept);
        Assert.assertEquals(concept.getDecompositionlevel(),decompositionDepth);
        Assert.assertTrue(concept.getId()== concept.hashCode());
        String graphMLPath = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "Depth_" + decompositionDepth + File.separator + concept.getLitheral() + "_" + concept.getWordType().toString() + ".GraphML";
        Graph g = GraphUtil.createJGraph(concept);
        GraphUtil.saveToGraphML(g,graphMLPath);
        String graphPath = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "Depth_" + decompositionDepth + File.separator + concept.getLitheral() + "_" + concept.getWordType().toString() + ".decompostion";
        GraphUtil.saveGraphObject(g,graphPath);
        Assert.assertTrue(new File(graphPath).exists());

    }


    @After
    public void after() {

    }
}
