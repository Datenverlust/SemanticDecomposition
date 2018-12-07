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
            +"graphs"+File.separator;


    public static void main(String[] args){
        System.out.println("Test output will be saved in " + GRAPH_FILE_NAME);
        Decomposition decomp = new Decomposition();

        //decompose two related words.
        Concept noonConcpet = decomp.decompose("Mittag", WordType.NN, 1);
        Concept morningConcpet = decomp.decompose("Morgen", WordType.NN, 1);

        //construct the decomposition graphs of the words
        //those will contain vertices for concepts and edges for all found relations, like antonyms, synonyms, hypernyms etc.
        //with their respective weights and edge type names
        Graph noonGraph = GraphUtil.createJGraph(noonConcpet);
        Graph morningGraph = GraphUtil.createJGraph(morningConcpet);

        //create a single graph out of the two decomposition graphs
        //FIXME: HERE the problem is that all edge type names are replaced with "Unknown"
        Graph merged = GraphUtil.mergeGraph(noonGraph, morningGraph);
        try {

            GraphUtil.saveToGraphML(noonGraph, GRAPH_FILE_NAME+"noon.graphml");
            GraphUtil.saveToGraphML(morningGraph, GRAPH_FILE_NAME+"morning.graphml");
            GraphUtil.saveToGraphML(merged, GRAPH_FILE_NAME+"noon_morning_merged.graphml");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
