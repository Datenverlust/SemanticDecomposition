package test;/*
 *  Created by borchert on 07.12.18
 *
 *
 */

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.WordType;
import de.dailab.nsm.decomposition.graph.conceptCache.GraphUtil;
import org.jgrapht.Graph;

import java.io.File;
import java.io.IOException;

public class GraphTest {

    private static final String GRAPH_FILE_NAME = System.getProperty("user.home")+ File.separator
            +".decomposition" +File.separator
            +"graphs"+File.separator
            +"noonGraph.graphml";
    public static void main(String[] args){
        System.out.println("Test output will be saved in " + GRAPH_FILE_NAME);
        Decomposition decomp = new Decomposition();
        Concept noonConcpet = decomp.decompose("mittag", WordType.NN, 1);

        Graph noonGraph = GraphUtil.createJGraph(noonConcpet);
        try {
            GraphUtil.saveToGraphML(noonGraph, GRAPH_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
