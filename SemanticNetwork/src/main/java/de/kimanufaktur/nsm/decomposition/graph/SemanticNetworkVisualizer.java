///*
// * Copyright (C) Johannes Fähndrich - All Rights Reserved.
// * Unauthorized copying of this file, via any medium is strictly
// * prohibited Proprietary and confidential.
// * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
// */
//
//package de.kimanufaktur.nsm.decomposition.graph;
//
//import de.kimanufaktur.nsm.decomposition.WordType;
//import de.kimanufaktur.nsm.decomposition.graph.conceptCache.GraphUtil;
//import org.jgraph.JGraph;
//import org.jgraph.graph.DefaultGraphCell;
//import org.jgraph.graph.GraphConstants;
//import org.jgraph.layout.CircleGraphLayout;
//import org.jgrapht.Graph;
//import org.jgrapht.ext.JGraphModelAdapter;
//import org.jgrapht.graph.DefaultEdge;
//import org.jgrapht.graph.ListenableDirectedGraph;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.geom.Rectangle2D;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * A applet that visualizes JGraphT graphs.
// *
// * @author Johannes Fähndrich
// * @since 2013
// */
//public class SemanticNetworkVisualizer extends JApplet {
//
//    Graph graph = new ListenableDirectedGraph(DefaultEdge.class);
//    public static void main(String[] arguments) {
//        String decompositionWord = "smile";
//        int decompositionDepth = 1;
//        WordType wordType = WordType.VB;
//        if (arguments != null && arguments.length == 3) {
//            decompositionWord = arguments[0];
//            decompositionDepth = Integer.valueOf(arguments[1]);
//            wordType = WordType.valueOf(arguments[2]);
//        }
//
//        SemanticNetworkVisualizer semanticNetworkVisualizer = new SemanticNetworkVisualizer();
//        semanticNetworkVisualizer.init(decompositionWord, wordType,decompositionDepth);
//    }
//
//    private JGraphModelAdapter m_jgAdapter;
//
//    /**
//     * @see java.applet.Applet#init().
//     */
//    public void init(String decompositionWord, WordType wordType, int decompositionDepth) {
//        // create a JGraphT graph
//        if (this.graph == null) {
//            loadGraph(decompositionWord, wordType, decompositionDepth);
//            // System.out.println(this.graph);
//        }
//        // create a visualization using JGraph, via an adapter
//        m_jgAdapter = new JGraphModelAdapter(graph);
//
//
//        JGraph jgraph = new JGraph(m_jgAdapter);
//        /*
//        Select the layout algorithm
//         */
//        CircleGraphLayout layout = new CircleGraphLayout();
//        // TreeLayoutAlgorithm layout = new TreeLayoutAlgorithm();
//        //GEMLayoutAlgorithm layout = new GEMLayoutAlgorithm();
//        //RadialTreeLayoutAlgorithm layout = new RadialTreeLayoutAlgorithm();
//        //SpringEmbeddedLayoutAlgorithm layout = new SpringEmbeddedLayoutAlgorithm();
//        //SugiyamaLayoutAlgorithm layout = new SugiyamaLayoutAlgorithm();
//        layout.run(jgraph, jgraph.getRoots(), 100);
//
//
//        //Add everything together into the window
//
//        JScrollPane scroller = new JScrollPane(jgraph);
//        getContentPane().add(scroller);
//        adjustDisplaySettings(jgraph);
//    }
//
//    private void loadGraph(String decompositionWord, WordType wordType, int decompositionDepth) {
//        try {
//            graph = GraphUtil.loadGraph(decompositionWord,wordType, decompositionDepth);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void adjustDisplaySettings(JGraph jg) {
//        Color c = Color.decode("#FAFBFF");
//        String colorStr = null;
//
//        try {
//            colorStr = getParameter("bgcolor");
//        } catch (Exception e) {
//        }
//
//        if (colorStr != null) {
//            c = Color.decode(colorStr);
//        }
//
//        jg.setBackground(c);
//    }
//
//
//    private void positionVertexAt(Object vertex, int x, int y) {
//        DefaultGraphCell cell = m_jgAdapter.getVertexCell(vertex);
//        Map attr = cell.getAttributes();
//        Rectangle2D b = GraphConstants.getBounds(attr);
//
//        GraphConstants.setBounds(attr, new Rectangle(x, y, (int) b.getWidth(), (int) b.getHeight()));
//
//        Map cellAttr = new HashMap();
//        cellAttr.put(cell, attr);
//        m_jgAdapter.edit(cellAttr, null, null, null);
//    }
//
//
//    public Graph getGraph() {
//        return graph;
//    }
//
//    public void setGraph(Graph graph) {
//        this.graph = graph;
//    }
//
//    public void paint(Graphics g) {
//        g.drawString("Hit Start to continue.", 30, 60 );
//    }
//
//}
