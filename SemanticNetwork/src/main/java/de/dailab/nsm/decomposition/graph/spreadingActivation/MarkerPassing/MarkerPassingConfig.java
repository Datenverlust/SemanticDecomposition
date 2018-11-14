/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.graph.spreadingActivation.MarkerPassing;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.WordType;
import de.tuberlin.spreadalgo.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by faehndrich on 31.07.15.
 */
public class MarkerPassingConfig implements Cloneable {
    //Paraemters of the N-fold-crossvalidation
    static int folds = 150;

    //Startconfiguration
    static List<Map<Concept, List<Marker>>> startMarker = new ArrayList<Map<Concept, List<Marker>>>();
    static Map<Concept, Double> conceptualThreshold = new HashMap<>(2);
    static double startActivation = 1;
    static double threshold = 0.064;
    static double negativeThreshold= -0.32;


    //Link Weights maximuls for the learning
    static double DefinitionLinkWeight = -0.78;
    static double synonymLinkWeight = 0.62;
    static double antonymLinkWeight = -0.9;
    static double hypernymLinkWeight = -0.02;
    static double hyponymLinkWeight = 0.79;
    static double defaultArbitraryRelationLinkWeight = 1;
    static HashMap<String, Double> arbitraryRelationWeights;
    static double syntaxLinkWeight = 0.5;
    static double contrastLinkWeight = -0.5;
    static double nerLinkWeight = 0.3;
    static double roleLinkWeight = -0.94;
    static double vnRoleLinkWeight = 0.3;

    static int pathlength = 0;
    //Termination condition

    static int terminationPulsCount = 80;
    static double doubleActivationLimit = 20;


    //Decomposition parameters
    static int decompositionDepth = 1;
    static WordType wordType = WordType.UNKNOWN;
    static boolean writeObjectGraphs = false;
    static boolean writeGraphMLGraphs = false;
    static boolean useGraphCache = true;
    private static boolean useMergedGraphCache = false;
    private static Double decay = 0.2;

    public static int getFolds() {
        return folds;
    }

    public static void setFolds(int folds) {
        MarkerPassingConfig.folds = folds;
    }

    public static boolean isWriteObjectGraphs() {
        return writeObjectGraphs;
    }

    public static void setWriteObjectGraphs(boolean writeObjectGraphs) {
        MarkerPassingConfig.writeObjectGraphs = writeObjectGraphs;
    }

    public static boolean isWriteGraphMLGraphs() {
        return writeGraphMLGraphs;
    }

    public static void setWriteGraphMLGraphs(boolean writeGraphMLGraphs) {
        MarkerPassingConfig.writeGraphMLGraphs = writeGraphMLGraphs;
    }

    public static boolean isUseGraphCache() {
        return useGraphCache;
    }

    public static void setUseGraphCache(boolean useGraphCache) {
        MarkerPassingConfig.useGraphCache = useGraphCache;
    }

    public static WordType getWordType() {
        return wordType;
    }

    public static void setWordType(WordType wordType) {
        MarkerPassingConfig.wordType = wordType;
    }

    public static int getDecompositionDepth() {
        return decompositionDepth;
    }

    public static void setDecompositionDepth(int decompositionDepth) {
        MarkerPassingConfig.decompositionDepth = decompositionDepth;
    }

    public static int getPathlength() {
        return pathlength;
    }

    public static void setPathlength(int pathlength) {
        MarkerPassingConfig.pathlength = pathlength;
    }

    public static Map<Concept, Double> getConceptualThreshold() {
        return conceptualThreshold;
    }

    public static void setConceptualThreshold(Map<Concept, Double> conceptualThreshold) {
        MarkerPassingConfig.conceptualThreshold = conceptualThreshold;
    }

    public static double getStartActivation() {
        return startActivation;
    }

    public static void setStartActivation(double startActivation) {
        MarkerPassingConfig.startActivation = startActivation;
    }

    public static double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        MarkerPassingConfig.threshold = threshold;
    }

    public static boolean isUseMergedGraphCache() {
        return useMergedGraphCache;
    }

    public static Double getDecay() {
        return decay;
    }

    public static void setDecay(Double decay) {
        MarkerPassingConfig.decay = decay;
    }

    public static List<Map<Concept, List<Marker>>> getStartMarker() {
        return startMarker;
    }

    public void setStartMarker(List<Map<Concept, List<Marker>>> startMarker) {
        MarkerPassingConfig.startMarker = startMarker;
    }

    public static double getDefinitionLinkWeight() {
        return DefinitionLinkWeight;
    }

    public static void setDefinitionLinkWeight(double definitionLinkWeight) {
        DefinitionLinkWeight = definitionLinkWeight;
    }

    public static double getAntonymLinkWeight() {
        return antonymLinkWeight;
    }

    public static void setAntonymLinkWeight(double antonymLinkWeight) {
        MarkerPassingConfig.antonymLinkWeight = antonymLinkWeight;
    }

    public static double getSynonymLinkWeight() {
        return synonymLinkWeight;
    }

    public static void setSynonymLinkWeight(double synonymLinkWeight) {
        MarkerPassingConfig.synonymLinkWeight = synonymLinkWeight;
    }

    public static double getHypernymLinkWeight() {
        return hypernymLinkWeight;
    }

    public static void setHypernymLinkWeight(double hypernymLinkWeight) {
        MarkerPassingConfig.hypernymLinkWeight = hypernymLinkWeight;
    }

    public static double getHyponymLinkWeight() {
        return hyponymLinkWeight;
    }

    public static void setHyponymLinkWeight(double hyponymLinkWeight) {
        MarkerPassingConfig.hyponymLinkWeight = hyponymLinkWeight;
    }

    public static HashMap<String, Double> getArbitraryRelationWeights() {
        if (MarkerPassingConfig.arbitraryRelationWeights == null) {
            MarkerPassingConfig.arbitraryRelationWeights = getArbitraryRelationWeightMap();
        }
        return MarkerPassingConfig.arbitraryRelationWeights;
    }

    public static void setArbitraryRelationWeights(HashMap<String, Double> arbitraryRelationWeights) {
        MarkerPassingConfig.arbitraryRelationWeights = arbitraryRelationWeights;
    }

    public static double getLinkWeightForRelationWithName(String relationName) {
        if (!getArbitraryRelationWeights().containsKey(relationName)) {
            getArbitraryRelationWeights().put(relationName, defaultArbitraryRelationLinkWeight);
        }
        return getArbitraryRelationWeights().get(relationName);
    }

    public static void setArbitraryRelationLinkWeight(String relationName, double linkWeight) {
        getArbitraryRelationWeights().put(relationName, linkWeight);
    }

    public static double getDefaultArbitraryRelationLinkWeight() {
        return defaultArbitraryRelationLinkWeight;
    }

    public static void setDefaultArbitraryRelationLinkWeight(double defaultArbitraryRelationLinkWeight) {
        MarkerPassingConfig.defaultArbitraryRelationLinkWeight = defaultArbitraryRelationLinkWeight;
    }

    public static int getTerminationPulsCount() {
        return terminationPulsCount;
    }

    public static void setTerminationPulsCount(int terminationPulsCount) {
        if (terminationPulsCount < 1) {
            terminationPulsCount = 1;
        }
        MarkerPassingConfig.terminationPulsCount = terminationPulsCount;
    }

    public static double getDoubleActivationLimit() {
        return doubleActivationLimit;
    }

    public static void setDoubleActivationLimit(double doubleActivationLimit) {
        MarkerPassingConfig.doubleActivationLimit = doubleActivationLimit;
    }

    public static double getNegativeThreshold() {
        return negativeThreshold;
    }

    public static void setNegativeThreshold(double negativeThreshold) {
        MarkerPassingConfig.negativeThreshold = negativeThreshold;
    }

    public static double getSyntaxLinkWeight() {
        return syntaxLinkWeight;
    }

    public static void setSyntaxLinkWeight(double syntaxLinkWeight) {
        MarkerPassingConfig.syntaxLinkWeight = syntaxLinkWeight;
    }

    public static double getContrastLinkWeight() {
        return contrastLinkWeight;
    }

    public static void setContrastLinkWeight(double contrastLinkWeight) {
        MarkerPassingConfig.contrastLinkWeight = contrastLinkWeight;
    }

    public static double getNerLinkWeight() {
        return nerLinkWeight;
    }

    public static void setNerLinkWeight(double nerLinkWeight) {
        MarkerPassingConfig.nerLinkWeight = nerLinkWeight;
    }

    public static double getRoleLinkWeight() {
        return roleLinkWeight;
    }

    public static void setRoleLinkWeight(double roleLinkWeight) {
        MarkerPassingConfig.roleLinkWeight = roleLinkWeight;
    }

    public static double getVnRoleLinkWeight() {
        return vnRoleLinkWeight;
    }

    public static void setVnRoleLinkWeight(double vnRoleLinkWeight) {
        MarkerPassingConfig.vnRoleLinkWeight = vnRoleLinkWeight;
    }

    private static HashMap<String, Double> getArbitraryRelationWeightMap() {
        return new HashMap<>();
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append("\n--- start of config ---");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(getStartActivation()).append(";");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(getThreshold()).append(";");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(getDefinitionLinkWeight()).append(";");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(getSynonymLinkWeight()).append(";");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(getAntonymLinkWeight()).append(";");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(+getHypernymLinkWeight()).append(";");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(+getHyponymLinkWeight()).append(";");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(+getTerminationPulsCount()).append(";");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(+getDoubleActivationLimit()).append(";");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(+getDecompositionDepth()).append(";");
        //stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(+getFolds());
        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append("ArbitraryRelations: ");

//        for (String relationName : getArbitraryRelationWeights().keySet()) {
//            double linkWeight = getLinkWeightForRelationWithName(relationName);
//            stringBuilder.append("\n\t" + relationName + ", " + linkWeight);
//        }
//
//        stringBuilder.append("\n--- end of config ---");

        return stringBuilder.toString();
    }
}
