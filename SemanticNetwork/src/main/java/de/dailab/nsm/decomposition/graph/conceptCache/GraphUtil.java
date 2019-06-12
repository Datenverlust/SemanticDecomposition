/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.conceptCache;


import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.Decomposition;
import de.dailab.nsm.decomposition.Definition;
import de.dailab.nsm.decomposition.Dictionaries.BaseDictionary;
import de.dailab.nsm.decomposition.Dictionaries.WordNetDictionary;
import de.dailab.nsm.decomposition.WordType;
import de.dailab.nsm.decomposition.graph.SemanticNet;
import de.dailab.nsm.decomposition.graph.edges.*;
import de.dailab.nsm.decomposition.graph.entities.Entity;
import de.dailab.nsm.decomposition.graph.entities.relations.Relation;
import de.dailab.nsm.decomposition.graph.entities.relations.Synonym;
import de.dailab.nsm.decomposition.graph.spreadingActivation.MarkerPassing.MarkerPassingConfig;
import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.io.*;

import java.io.*;
import java.util.*;

/**
 * This is a utility class providing all needed functionality to create semantic networks.
 * This class has an own decomposition and can be used to multiThreadedDecompose concepts and transform them inte graphs.
 * Created by faehndrich on 30.04.15.
 */
public class GraphUtil {

    static Map<String, Graph> graphCache = Collections.synchronizedMap(new HashMap<String, Graph>());
    static Map<String, Object> lockMap = Collections.synchronizedMap(new Hashtable<String, Object>());
    static int lockcount = 0;
    static Decomposition decomposition = new Decomposition();
    private static Logger logger = Logger.getLogger(GraphUtil.class);
    private static HashMap<Integer, DefaultListenableGraph<Object, Object>> mergedGraphCache = new HashMap<>(65);
    static MarkerPassingConfig markerPassingConfig = new MarkerPassingConfig();

    /**
     * Create a ListenableGraph from a concept regarding its decomposition
     *
     * @param concept the concept from which to start the graph.
     * @return DefaultListenableGraph which models the decomposition of the given concept.
     */
    public static DefaultListenableGraph<Concept, WeightedEdge> createJGraph(Concept concept) {

        DefaultListenableGraph<Concept, WeightedEdge> graph = new DefaultListenableGraph(new DefaultDirectedWeightedGraph(WeightedEdge.class));
        assert (Decomposition.getConcepts2Ignore().size() > 0);
        if (Decomposition.getConcepts2Ignore().contains(concept)) {
            //System.out.println("The concept " + concept.getLitheral() + " is an stop word and is not decomposed.");
        } else {
            addConceptRecursivly(graph, concept);
        }

        return graph;
    }

    public static Graph<Concept, WeightedEdge> initializeJGraph(Concept concept) {
        DefaultListenableGraph<Concept, WeightedEdge> graph = new DefaultListenableGraph<>(new DefaultDirectedWeightedGraph<>(WeightedEdge.class));
        graph.addVertex(concept);
        return graph;
    }

    /**
     * Create a ListenableGraph from a concept regarding BDOS
     *
     * @param concept the concept from which to start the graph.
     * @return DefaultListenableGraph which models the decomposition of the given concept.
     */

    public static DefaultListenableGraph<Concept, WeightedEdge> createJGraphforBDOS(Concept concept) {

        DefaultListenableGraph<Concept, WeightedEdge> graph = new DefaultListenableGraph<>(new DefaultDirectedWeightedGraph<>(WeightedEdge.class));

        if (Decomposition.getConcepts2Ignore().contains(concept)) {
            //System.out.println("The concept " + concept.getLitheral() + " is an stop word and is not decomposed.");
        } else {
            graph.addVertex(concept);
            addConceptRecursivlyforBDOS(graph, concept);
        }
        return graph;
    }


    /**
     * Create a ListenableGraph from a entity regarding its decomposition
     *
     * @param entity the entity from which to start the graph.
     * @return DefaultListenableGraph which models the decomposition of the given entity.
     */
    public static Graph<Entity, Relation> createJGraph(Entity entity) {

        SemanticNet graph = (SemanticNet) new DefaultListenableGraph(new DefaultDirectedWeightedGraph<>(Relation.class));

        graph.addVertex(entity);
        addEntityRecursivly(graph, entity);

        return graph;
    }


    /**
     * Add all nodes relevant to the graph of the given context to the graph.
     * Caution only definitions and synonyms are added here.
     * TODO: We want this graph to be structured with multiple node and link types.
     * TODO: We want the links/edges to have a wight of the semantic distance.
     *
     * @param graph  the jGraph to add the nodes to
     * @param entity the entity to be added to the graph.
     *               TODO: this is incomplete.
     */
    private static void addEntityRecursivly(SemanticNet graph, Entity entity) {
        if (graph.containsVertex(entity)) {
            entity.getRelations().stream().filter(relation -> relation != null).forEach(relation -> {
                relation.getRoles().stream().filter(role -> !graph.containsEdge(role)).forEach(role -> {
                    graph.addEdge(role.getSource(), role.getTarget(), role);
                });
                if (graph.containsVertex(relation)) {
                    if (!graph.containsEdge(entity, relation))
                        graph.addEdge(entity, relation);
                } else {
                    graph.addVertex(relation);
                    graph.addEdge(entity, relation);
                    addEntityRecursivly(graph, relation);
                }
            });
        } else {
            graph.addVertex(entity);
            addEntityRecursivly(graph, entity);
        }
    }


    /**
     * Add all nodes relevant to the graph of the given context to the graph.
     * Caution only definitions and synonyms are added here.
     * TODO: We want this graph to be structured with multiple node and link types.
     * TODO: We want the links/edges to have a wight of the semantic distance.
     *
     * @param graph   the jGraph to add the nodes to
     * @param concept the concept to be added to the graph.
     */
    private static void addConceptRecursivly(DefaultListenableGraph<Concept, WeightedEdge> graph, Concept concept) {
        if (!Decomposition.getConcepts2Ignore().contains(concept)) {
            if (!graph.containsVertex(concept)) {
                graph.addVertex(concept);
                //add weight as semantic distance or as relational similarity. 0 meaning the same word and 1 meaning with a maximal distance
                concept.getSynonyms().stream().filter(syn -> syn != null && !Decomposition.getConcepts2Ignore().contains(syn) && !syn.equals(concept)).forEach(syn -> {
                    addConceptRecursivly(graph, syn);
                    Set<WeightedEdge> edges = graph.getAllEdges(concept, syn);
                    SynonymEdge synonymEdge = new SynonymEdge();
                    synonymEdge.setSource(concept);
                    synonymEdge.setTarget(syn);
                    double w = MarkerPassingConfig.getSynonymLinkWeight();
                    Map attributes = new HashMap();
                    attributes.put("edgeType", "synonym");
//                    attributes.put("weight",w);
                    synonymEdge.setWeight(w);

                    synonymEdge.setAttributes(attributes); //add weight as semantic distance or as relational similarity. 0 meaning the same word and 1 meaning with a maximal distance
                    if (edges == null || edges.size() == 0 || !edges.contains(synonymEdge)) {
                        graph.addEdge(concept, syn, synonymEdge);
                        graph.setEdgeWeight(synonymEdge, w);
                    }
                });
                for (Definition definition : concept.getDefinitions()) {
                    //Take out stop words, so the actiavtion does not screw up.
                    definition.getDefinition().stream().filter(def -> def != null && !Decomposition.getConcepts2Ignore().contains(def) && !def.equals(concept)).forEach(def -> { //Take out stop words, so the actiavtion does not screw up.
                        addConceptRecursivly(graph, def);
                        Set<WeightedEdge> edges = graph.getAllEdges(concept, def);
                        DefinitionEdge definitionEdge = new DefinitionEdge();
                        definitionEdge.setSource(concept);
                        definitionEdge.setTarget(def);
                        double w = MarkerPassingConfig.getDefinitionLinkWeight();
                        Map attributes = new HashMap();
                        attributes.put("edgeType", "definition");
                        definitionEdge.setWeight(w);
//                        attributes.put("weight",w);
                        definitionEdge.setAttributes(attributes);
                        if (!edges.contains(definitionEdge)) {
                            graph.addEdge(concept, def, definitionEdge);
                            graph.setEdgeWeight(definitionEdge, definitionEdge.getWeight());
                        }
                    });
                }
                concept.getHyponyms().stream().filter(hypo -> hypo != null && !Decomposition.getConcepts2Ignore().contains(hypo) && !hypo.equals(concept)).forEach(hypo -> {
                    addConceptRecursivly(graph, hypo);
                    Set<WeightedEdge> edges = graph.getAllEdges(concept, hypo);
                    double w = MarkerPassingConfig.getHyponymLinkWeight();
                    HyponymEdge hyponymEdge = new HyponymEdge();
                    hyponymEdge.setSource(concept);
                    hyponymEdge.setTarget(hypo);
                    hyponymEdge.setWeight(w);
                    Map attributes = new HashMap();
                    attributes.put("edgeType", "hyponym");
//                    attributes.put("weight",w);
                    hyponymEdge.setAttributes(attributes);
                    if (!edges.contains(hyponymEdge)) {
                        graph.addEdge(concept, hypo, hyponymEdge);
                        graph.setEdgeWeight(hyponymEdge, w);
                    }
                });
                concept.getHypernyms().stream().filter(hyper -> hyper != null && !Decomposition.getConcepts2Ignore().contains(hyper) && !hyper.equals(concept)).forEach(hyper -> {
                    addConceptRecursivly(graph, hyper);
                    Set<WeightedEdge> edges = graph.getAllEdges(concept, hyper);
                    double w = MarkerPassingConfig.getHypernymLinkWeight();
                    HypernymEdge hypernymEdge = new HypernymEdge();
                    hypernymEdge.setSource(concept);
                    hypernymEdge.setTarget(hyper);
                    hypernymEdge.setWeight(w);
                    Map attributes = new HashMap();
                    attributes.put("edgeType", "hypernym");
//                    attributes.put("weight",w);
                    hypernymEdge.setAttributes(attributes);
                    if (!edges.contains(hypernymEdge)) {
                        graph.addEdge(concept, hyper, hypernymEdge);
                        graph.setEdgeWeight(hypernymEdge, w);
                    }
                });
                concept.getAntonyms().stream().filter(antonym -> antonym != null && !Decomposition.getConcepts2Ignore().contains(antonym) && !antonym.equals(concept)).forEach(antonym -> {
                    addConceptRecursivly(graph, antonym);
                    Set<WeightedEdge> edges = graph.getAllEdges(concept, antonym);
                    double w = MarkerPassingConfig.getAntonymLinkWeight();
                    AntonymEdge antonymEdge = new AntonymEdge();
                    antonymEdge.setSource(concept);
                    antonymEdge.setTarget(antonym);
                    antonymEdge.setWeight(w);
                    Map attributes = new HashMap();
                    attributes.put("edgeType", "antonym");
//                    attributes.put("weight",w);
                    antonymEdge.setAttributes(attributes);
                    if (!edges.contains(antonymEdge)) {
                        graph.addEdge(concept, antonym, antonymEdge);
                        graph.setEdgeWeight(antonymEdge, w);
                    }
                });
                concept.getMeronyms().stream().filter(meronym -> meronym != null && !Decomposition.getConcepts2Ignore().contains(meronym) && !meronym.equals(concept)).forEach(meronym -> {
                    addConceptRecursivly(graph, meronym);
                    Set<WeightedEdge> edges = graph.getAllEdges(concept, meronym);
                    //TODO: Does not exist. Are these values actually used?
                    double w = MarkerPassingConfig.getMeronymLinkWeight();
                    MeronymEdge meronymEdge = new MeronymEdge();
                    meronymEdge.setSource(concept);
                    meronymEdge.setTarget(meronym);
                    Map attributes = new HashMap();
                    attributes.put("edgeType", "meronym");
//                    attributes.put("weight",0.0d);
                    meronymEdge.setAttributes(attributes);
                    if (!edges.contains(meronymEdge)) {
                        graph.addEdge(concept, meronym, meronymEdge);
                        graph.setEdgeWeight(meronymEdge, w);
                    }
                });
                concept.getArbitraryRelations().stream().filter(arbitraryRelation -> arbitraryRelation != null && !Decomposition.getConcepts2Ignore().contains(arbitraryRelation) && !arbitraryRelation.equals(concept)).forEach(arbitraryRelation -> {
                    addConceptRecursivly(graph, arbitraryRelation);
                    Set<WeightedEdge> edges = graph.getAllEdges(concept, arbitraryRelation);
                    ArbitraryEdge arbitraryEdge = new ArbitraryEdge();
                    //TODO: Clarify the use of arbitraryweights
                    //double w = MarkerPassingConfig.getArbitraryRelationWeights();
                    arbitraryEdge.setSource(concept);
                    arbitraryEdge.setTarget(arbitraryRelation);
                    arbitraryEdge.setRelationName(arbitraryRelation.getOriginatedRelationName());
                    Map attributes = new HashMap();
                    attributes.put("edgeType", arbitraryRelation.getOriginatedRelationName());
//                    attributes.put("weight",0.0d);
                    arbitraryEdge.setAttributes(attributes);
                    if (!edges.contains(arbitraryEdge)) {
                        graph.addEdge(concept, arbitraryRelation, arbitraryEdge);
                        graph.setEdgeWeight(arbitraryEdge, 0.0d);
                    }
                });
            }
        }
    }

    /**
     * Add all nodes relevant to the graph in BDOS.
     * Caution only hypernyms and meronyms are added here.
     * TODO: We want this graph to be structured with multiple node and link types.
     * TODO: We want the links/edges to have a wight of the semantic distance.
     *
     * @param graph   the jGraph to add the nodes to
     * @param concept the concept to be added to the graph.
     */

    private static void addConceptRecursivlyforBDOS(DefaultListenableGraph<Concept, WeightedEdge> graph, Concept concept) {

        BaseDictionary wordnet = WordNetDictionary.getInstance();
        ArrayList<Concept> meronyms = new ArrayList<>();
        meronyms.addAll(wordnet.getMeronyms(concept));
        Set<Concept> stepSet = new HashSet<>();
        stepSet.addAll(concept.getMeronyms());

        if (!graph.containsVertex(concept)) {
            graph.addVertex(concept);
        }
        concept.getHypernyms().stream().filter(hyp -> hyp != null).forEach(hyp -> {
            if (!graph.containsVertex(hyp)) {
                graph.addVertex(hyp);
                if (!graph.containsEdge(concept, hyp)) {
                    graph.addEdge(concept, hyp);
                    addConceptRecursivly(graph, hyp);
                }

            } else {

                if (!graph.containsEdge(concept, hyp)) {
                    graph.addEdge(concept, hyp);
                    addConceptRecursivly(graph, hyp);
                    //TODO kürzesten Weg finden
                }
            }
        });
        for (Concept mero : meronyms) {
            if (mero != null) {
                if (!graph.containsVertex(mero)) {
                    graph.addVertex(mero);
                } else {

                    if (!graph.containsEdge(concept, mero)) {
                        graph.addEdge(concept, mero);
                        addConceptRecursivly(graph, mero);
                    }
                }
            }
        }
    }


    /**
     * Saves a graph to GraphML format to be used in Gephi or equivalent.
     *
     * @param graph    the graph to save
     * @param filename the path where to save the graph.
     * @throws IOException
     */

    public static void saveToGraphML(final Graph<Concept, WeightedEdge> graph,
                                     String filename) throws IOException {
        logger.info("Saving decomposition to " + filename);
        //GraphMLExporter<String, MyWeightedEdge> exporter = new GraphMLExporter<concept, DefaultEdge>();

        // In order to be able to export edge and node labels and IDs, we must implement providers for them
        ComponentNameProvider<Concept> vertexIDProvider = vertex -> {
            if (vertex != null && vertex.getLitheral() != null) {
                return vertex.getLitheral();
            } else {
                assert vertex != null;
                return String.valueOf(vertex.hashCode());
            }

        };
        //Create vertex name provider. Here we can controll what is writen on the nodes of the graph
        ComponentNameProvider<Concept> vertexNameProvider = vertex -> {
            if (vertex != null) {
                return vertex.getLitheral();
            } else {
                return "";
            }
        };

        //Create a edge name provider. Here we want to differ the edge types.
        ComponentNameProvider<WeightedEdge> edgeIDProvider = edge -> {
            String edgeType;// = (String)edge.getAttributes().get("edgeType");
            if (edge instanceof ArbitraryEdge) {
                edgeType = graph.getEdgeSource(edge) + " " + ((ArbitraryEdge) edge).getRelationName() + " > " + graph.getEdgeTarget(edge);
            } else {
                edgeType = graph.getEdgeSource(edge) + " " + edge.getEdgeType().toString() + " > " + graph.getEdgeTarget(edge);
            }
            return edgeType;

        };
        ComponentNameProvider<WeightedEdge> edgeLabelProvider = edge -> {
            if (edge != null) {
                double w = edge.getWeight();
                return String.valueOf(w);
            } else {
                return " > ";//TODO: whats that for?
            }
        };
        //additional edge attributes
        ComponentAttributeProvider<WeightedEdge> vertexAttributeProvider = null;
        ComponentAttributeProvider<WeightedEdge> edgeAttributeProvider = edge -> {
            Map<String, Attribute> m = new HashMap<>();
            if (edge != null) {
                m.put("edgeType", DefaultAttribute.createAttribute(edge.getEdgeType().toString()));
                m.put("weight", DefaultAttribute.createAttribute(edge.getWeight() + ""));
            }
            return m;
        };

        GraphMLExporter<Concept, WeightedEdge> exporter =
                new GraphMLExporter(vertexIDProvider, vertexNameProvider, vertexAttributeProvider, edgeIDProvider,
                        edgeLabelProvider, edgeAttributeProvider);

        exporter.setExportEdgeWeights(true);
        exporter.registerAttribute("edgeType", GraphMLExporter.AttributeCategory.EDGE, AttributeType.STRING);
//        exporter.registerAttribute("weight", GraphMLExporter.AttributeCategory.EDGE, AttributeType.STRING);


        File graphFile = new File(filename);
        if (!graphFile.exists()) {
            if (!graphFile.getParentFile().exists()) {
                graphFile.getParentFile().mkdirs();
            }
        }
        FileWriter fw = new FileWriter(graphFile);
        try {
            exporter.exportGraph(graph, fw);
            fw.flush();
            fw.close();
        } catch (ExportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Save the given concept to the given graph. The result is stored unter ~/.decomposition/CONCEPTNAME_DEPTH.decompGraph or .GraphML
     *
     * @param jGraph             the JGraphT to save the concepts decomposition in
     * @param concept            the concept to add to the given graph
     * @param decompositionDepth the decomposition decompositionDepth, which is only used for the name of the file.
     */
    public static void saveGraph(Graph jGraph, Concept concept, int decompositionDepth) {
        if (concept.getWordType() == null) {
            concept.setWordType(WordType.UNKNOWN);
        }
        String u = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "Depth_" + decompositionDepth + File.separator + concept.getLitheral() + "_" + concept.getWordType().toString() + ".decompGraph";
        String graphPath = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "Depth_" + decompositionDepth + File.separator + concept.getLitheral() + "_" + concept.getWordType().toString() + ".GraphML";
        try {
            if (MarkerPassingConfig.isWriteObjectGraphs()) {
                saveGraphObject(jGraph, u);
            }
            if (MarkerPassingConfig.isWriteGraphMLGraphs()) {
                saveToGraphML(jGraph, graphPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Save the JGraph object of the decomposition into a file.
     *
     * @param DecompositionjGraph the decomposition of a concept transformed into a Graph
     * @param path2write2         the location on the hard drive to save the graph to.
     * @throws IOException
     */
    public static void saveGraphObject(Graph DecompositionjGraph, String path2write2) throws IOException {
        File graphFile = new File(path2write2);
        if (!graphFile.exists()) {
            if (!graphFile.getParentFile().exists()) {
                graphFile.getParentFile().mkdirs();
            }
        }
        logger.info("Saving decomposition to " + path2write2);
        FileOutputStream fileOut = new FileOutputStream(path2write2, false);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(DecompositionjGraph);
        out.close();
        return;
    }

    /**
     * Load the given graph form the user home directory in .decomposition with the ednign .decompGraph
     *
     * @param decompositionWord  the word decomposted to create the file name
     * @param decompositionDepth the decomposition depth for id the right folder.
     * @throws IOException
     * @throws ClassNotFoundException
     * @returnthe JGraph loaded from the given path.
     */
    public static Graph loadGraph(String decompositionWord, WordType wordType, int decompositionDepth) throws IOException, ClassNotFoundException {

        String u = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "Depth_" + decompositionDepth + File.separator + decompositionWord + "_" + wordType.toString() + ".decompGraph";
        Graph result = null;
        FileInputStream fileIn = new FileInputStream(u);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        result = (Graph) in.readObject();
        in.close();
        fileIn.close();
        return result;
    }

    /**
     * Create an entity out of a concept. This needs to be done when transformation to the semantic networks and the formal
     * model of concept of Mahr is used. Mahr, B. (2007). Ein Modell der Auffassung. Technische Universitaet Berlin.
     *
     * @param c the concept to be transformed into a entity
     * @return the entity representing the given concept.
     * TODO: This is incomplete since not only an entity but all its relations and the related entities need to be added.
     */
    public static Entity createEntity(Concept c) {
        Entity result = new Entity();
        if (c != null) {

            result.setName(c.getLitheral());
            for (Concept rel : c.getSynonyms()) {
                Relation s = new Synonym();
                s.setName(rel.getLitheral());

            }
        }

        return result;
    }

    public static Entity createEmojiEntity(String unicode) {
        Entity result = new Entity();
        if (unicode != null) {
            result.setName(unicode);
        }

        return result;
    }

    /**
     * Add the vertexes of the given graph to the result
     *
     * @param graph1 the graph to take the vertexes to add from
     * @param result the resulting graph, containing all the vertexes of the given graph.
     */
    private static void addVertexes(Graph graph1, DefaultListenableGraph result) {
        Iterator iterator;
        iterator = graph1.vertexSet().iterator();
        while (iterator.hasNext()) {
            Object sourceVertex = iterator.next();
            if (sourceVertex instanceof Concept) {
                if (!result.containsVertex(sourceVertex)) {
                    result.addVertex(sourceVertex);
                }

            }
        }
    }

    /**
     * Add the edges of the given graph to the result
     *
     * @param graph1 the graph to take the edges to add from
     * @param result the resulting graph, containing all the edges of the given graph.
     */
    private static void addEdges(Graph graph1, DefaultListenableGraph result) {
        Iterator iterator;
        iterator = graph1.edgeSet().iterator();
        while (iterator.hasNext()) {
            Object sourceEdge = iterator.next();
            if (sourceEdge instanceof WeightedEdge) {
                if (!result.containsEdge(sourceEdge)) {
                    result.addEdge(((WeightedEdge) sourceEdge).getSource(), ((WeightedEdge) sourceEdge).getTarget(), sourceEdge);
                }
            }
        }
    }

    public static List<WeightedEdge> getCommonEdges(Graph g1, Graph g2) {
        List<WeightedEdge> result = new ArrayList<>();
        for (WeightedEdge e1 : (Set<WeightedEdge>) g1.edgeSet()) {
            for (WeightedEdge e2 : (Set<WeightedEdge>) g2.edgeSet()) {
                if (compareEdge(e1, e2)) {

                    result.add(e1);
                }
            }
        }
        return result;
    }

    public static boolean compareEdge(WeightedEdge e1, WeightedEdge e2) {
        boolean result = false;
        if (e1.getSource() != null && e2.getSource() != null && e1.getTarget() != null && e2.getTarget() != null) {
            if (e1.getSource().equals(e2.getSource()) && e1.getTarget().equals(e2.getTarget())) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Get the graph from the location ~/.decomposition or create it via the decomposition algorithm.
     *
     * @param decompositionWord  the word to multiThreadedDecompose
     * @param wordType           the POS of the word to multiThreadedDecompose
     * @param decompositionDepth the decomposition depth to which the word should be decomposed
     * @return a jgrapht Graph containing the decomposition of the word.
     */
    public static Graph getGraph(String decompositionWord, WordType wordType, int decompositionDepth) {
        Graph graph = null;
        if (MarkerPassingConfig.isUseGraphCache()) {
            String key = decompositionWord + wordType.type() + decompositionDepth;
            graph = graphCache.get(key);
            if (graph == null) {
                Object lock = lockMap.get(key);
                if (lock == null) {
                    //System.out.println("locking: " + key + " with " + lockcount + " locks in use.");
                    lock = new Object();
                    lockMap.put(key, lock);
                    try {
                        graph = loadGraph(decompositionWord, wordType, decompositionDepth);
                        graphCache.put(key, graph);
                    } catch (Exception e) {
                        Concept c = decomposition.decompose(decompositionWord, wordType, decompositionDepth);
                        if (c != null) {
                            cleanUp(c, decomposition);
                            //Entity e = GraphUtil.createEntity(c);
                            graph = createJGraph(c);
                            graphCache.put(key, graph);
                            saveGraph(graph, c, decompositionDepth);
                        }
                    }
                    synchronized (lock) {
                        lock.notifyAll();
                        //System.out.println("Notify: " + key + " with " + lockcount + " locks in use.");
                    }
                } else {
                    synchronized (lock) {
                        try {
                            lockcount++;
                            //System.out.println("Wait: " + key + " with " + lockcount + " locks in use.");
                            lock.wait();
                            lockcount--;
                            //System.out.println("Continuing: " + key + " with " + lockcount + " locks in use.");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    graph = graphCache.get(key);
                }

            } else {
//                Object lock = lockMap.get(key);
//                if(lock != null){
//                    try {
//                        synchronized (lock) {
//                            lock.wait();
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        } else {
            // Concept c = decomposition.multiThreadedDecompose(decompositionWord, wordType, decompositionDepth);
            Concept c = decomposition.decompose(decompositionWord, wordType, decompositionDepth);
            //Concept decWord = new Concept(decompositionWord);
            //Concept c = decomposition.decompose(decWord, decompositionDepth);
            cleanUp(c, decomposition);
            //Entity e = GraphUtil.createEntity(c);
            graph = createJGraph(c);
            // GraphUtil.saveGraph(graph, c, decompositionDepth);
        }
        return graph;
    }

    /**
     * Clean up the decomposition and old graphs.
     *
     * @param c             concept to clean up.
     * @param decomposition decomposition to reset all known concepts and so on...
     */
    private static void cleanUp(Concept c, Decomposition decomposition) {
        decomposition.cleanUp();
    }

    public boolean compareGraphs(Graph g0, List<Concept> concepts) {
        boolean result = true;
        for (Concept findC : concepts) {
            boolean forConcept = false;
            for (Object o : g0.vertexSet()) {
                Concept c = (Concept) o;
                if (c.equals(findC)) {
                    forConcept = true;
                }
            }
            result &= forConcept;
        }
        return result;
    }

    public boolean compareGraphs(Graph g1, Graph g2) {
        if (g1.equals(g2)) {
            return true;
        }
        for (Object o : g1.vertexSet()) {
            Concept c = (Concept) o;
            if (!containsVertex(g2, c)) {
                return false;
            }
        }
        for (Object o : g2.vertexSet()) {
            Concept c = (Concept) o;
            if (!containsVertex(g1, c)) {
                return false;
            }
        }
        List<WeightedEdge> commonEdges = getCommonEdges(g1, g2);
        if (commonEdges.size() != g1.edgeSet().size()) {
            System.out.println("coomon edges:" + commonEdges.size() + " g1:" + g1.edgeSet().size());
            return false;
        }
        if (commonEdges.size() != g2.edgeSet().size()) {
            System.out.println("common edges:" + commonEdges.size() + " g1:" + g2.edgeSet().size());
            return false;
        }
        return true;
    }

    /**
     * Merging two graphs into one. Taking all node and edges from the first one
     * and adding those of the second graph if they do not already exist.
     *
     * @param graph1 the first graph to merge
     * @param graph2 the second graph to merge into the first one
     * @return a new graph containing all edges and nodes of the first two
     * graphs.
     */
    public static synchronized Graph<Object, Object> mergeGraph(Graph graph1, Graph graph2) {
        DefaultListenableGraph<Object, Object> result = null;
        if (MarkerPassingConfig.isUseMergedGraphCache()) {
            result = mergedGraphCache.get(graph1.hashCode() + graph2.hashCode());
        }
        if (result == null) {
            result = new DefaultListenableGraph<>(new DefaultDirectedWeightedGraph<>(WeightedEdge.class));
            // Get all vertex from the graph1
            Iterator iterator = graph1.vertexSet().iterator();
            AddVertexesOfGraph(result, iterator);
//            result.vertexSet().addAll(graph1.vertexSet());

            // Get all edges from graph1
            iterator = graph1.edgeSet().iterator();
            AddEdgesOfGraph(result, iterator);
//            result.edgeSet().addAll(graph1.edgeSet());

            // Get all vertex from the graph2
            iterator = graph2.vertexSet().iterator();
            AddVertexesOfGraph(result, iterator);
//            result.vertexSet().addAll(graph2.vertexSet());

            // Get all edges from graph2
            iterator = graph2.edgeSet().iterator();
            AddEdgesOfGraph(result, iterator);
//            result.edgeSet().addAll(graph1.edgeSet());

            if (MarkerPassingConfig.isUseMergedGraphCache()) {
                mergedGraphCache.put(graph1.hashCode() + graph2.hashCode(), result);
            }
        }

        return result;

    }


    public static synchronized Concept getConceptFromGraph(Graph graph, String word) {

        for (Object c : graph.vertexSet()) {
            if (((Concept) c).getLitheral().equals(word)) {
                return (Concept) c;
            }
        }
        return null;

    }

    /**
     * Add the the vertexes of the graph in the iterator to the graph given in the first argument.
     *
     * @param result   the graph to add the vertiexes to.
     * @param iterator the iterator which provides the concepts which should be added as nodes to the graph.
     */
    private static void AddVertexesOfGraph(DefaultListenableGraph result, Iterator iterator) {
        while (iterator.hasNext()) {
            Object sourceVertex = iterator.next();
            if (sourceVertex instanceof Concept) {
    /*            if (!containsVertex(result, sourceVertex)) {
                    //clone the concept
                    Concept newVertex = new Concept(((Concept) sourceVertex).getLitheral());
                    newVertex.setDecompositionlevel(((Concept) sourceVertex).getDecompositionlevel());
                    result.addVertex(newVertex);
                }*/
                result.addVertex(sourceVertex);
            }
        }
    }

    /**
     * Here a shallow copy of the concepts are added to the given graph.
     *
     * @param result   the graph to clone the concepts into
     * @param iterator the iterator from which the concepts should be cloned into the graph.
     */
    private static void AddEdgesOfGraph(DefaultListenableGraph result, Iterator iterator) {
        while (iterator.hasNext()) {
            Object sourceEdge = iterator.next();
            if (sourceEdge instanceof WeightedEdge) {
                WeightedEdge temEdge = (WeightedEdge) sourceEdge;
                result.addEdge(temEdge.getSource(), temEdge.getTarget(), temEdge);
                result.setEdgeWeight(temEdge, temEdge.getEdgeWeight());

                /* WeightedEdge newEdge = new WeightedEdge();

               if (!result.containsEdge(sourceEdge)) {
                    Concept source = (Concept) ((WeightedEdge) sourceEdge).getSource();
                    Concept target = (Concept) ((WeightedEdge) sourceEdge).getTarget();
                    //Clone the concepts
                    Concept newSource = new Concept(source.getLitheral());
                    newSource.setDecompositionlevel(source.getDecompositionlevel());
                    Concept newTarget = new Concept(target.getLitheral());
                    newTarget.setDecompositionlevel(target.getDecompositionlevel());
                    if (!containsVertex(result, newSource)) {
                        result.addVertex(newSource);
                    }
                    if (!containsVertex(result, newTarget)) {
                        result.addVertex(newTarget);
                    }
                    newEdge.setSource(newSource);
                    newEdge.setTarget(newTarget);
                    newEdge.setEdgeType(((WeightedEdge) sourceEdge).getEdgeType());
                    newEdge.setWeight(((WeightedEdge) sourceEdge).getWeight());
                    result.addEdge(newSource, newTarget,
                            newEdge);

                }*/
            }
        }
    }

    private static boolean containsVertex(Graph graph, Object sourceVertex) {
        return graph.containsVertex(sourceVertex);
//        Iterator iterator = graph.vertexSet().iterator();
//
//        if (!graph.containsVertex(sourceVertex)) {
//            return false;
//        } else {
//            while (iterator.hasNext()) {
//                Object vertex = iterator.next();
//                if (vertex instanceof Concept) {
//                    if (vertex.equals(sourceVertex)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
    }


}
