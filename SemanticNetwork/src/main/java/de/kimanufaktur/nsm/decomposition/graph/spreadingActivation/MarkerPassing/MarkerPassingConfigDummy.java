//package de.kimanufaktur.nsm.decomposition.graph.spreadingActivation.MarkerPassing;
//
//import Concept;
//import WordType;
//import de.kimanufaktur.spreadalgo.Marker;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class MarkerPassingConfigDummy {
//    //Paraemters of the N-fold-crossvalidation
//    static int folds = 500;
//
//    //Startconfiguration
//    List<Map<Concept, List<Marker>>> startMarker = new ArrayList<Map<Concept, List<Marker>>>();
//    Map<Concept, Double> conceptualThreshold = new HashMap<>(2);
//    double startActivation = 1;
//    double threshold = 0.32;
//    double negativeThreshold= -0.32;
//
//
//
//    //Link Weights maximuls for the learning
//    double DefinitionLinkWeight = 0.25;
//    double synonymLinkWeight = -0.94;
//    double antonymLinkWeight = -0.11;
//    double hypernymLinkWeight = 0.3;
//    double hyponymLinkWeight = 0.11;
//    double defaultArbitraryRelationLinkWeight = 0.1;
//    HashMap<String, Double> arbitraryRelationWeights;
//    double syntaxLinkWeight = 0.5;
//    double contrastLinkWeight = -0.5;
//    double nerLinkWeight = 0.3;
//    double roleLinkWeight = -0.94;
//    double vnRoleLinkWeight = 0.3;
//
//    //Termination condition
//    int terminationPulsCount =5;//20;
//    double doubleActivationLimit = 0.53;
//    //I set that higher for the reflux experiment
//    //static double doubleActivationLimit = 10000000;
//
//
//    //Decomposition parameters
//    static int decompositionDepth = 2;
//    static WordType wordType = WordType.UNKNOWN;
//    static boolean writeObjectGraphs = false;
//    static boolean writeGraphMLGraphs = false;
//    static boolean useGraphCache = true;
//    private static boolean useMergedGraphCache = false;
//    private  Double decay = 0.2;
//
//    public  MarkerPassingConfigDummy(){
//
//    }
//
//    public MarkerPassingConfigDummy(MarkerPassingConfig config) {
//        this.terminationPulsCount = config.terminationPulsCount;
//        this.threshold = config.threshold;
//        DefinitionLinkWeight = config.DefinitionLinkWeight;
//        synonymLinkWeight = config.synonymLinkWeight;
//        antonymLinkWeight = config.antonymLinkWeight;
//        hypernymLinkWeight = config.hypernymLinkWeight;
//        hyponymLinkWeight = config.hyponymLinkWeight;
//        defaultArbitraryRelationLinkWeight = config.defaultArbitraryRelationLinkWeight;
//        syntaxLinkWeight = config.syntaxLinkWeight;
//        contrastLinkWeight = config.contrastLinkWeight;
//        nerLinkWeight = config.nerLinkWeight;
//        roleLinkWeight = config.roleLinkWeight;
//        vnRoleLinkWeight = config.vnRoleLinkWeight;
//    }
//
//    public static int getFolds() {
//        return folds;
//    }
//
//    public static void setFolds(int folds) {
//        MarkerPassingConfig.folds = folds;
//    }
//
//    public static boolean isWriteObjectGraphs() {
//        return writeObjectGraphs;
//    }
//
//    public static void setWriteObjectGraphs(boolean writeObjectGraphs) {
//        MarkerPassingConfig.writeObjectGraphs = writeObjectGraphs;
//    }
//
//    public static boolean isWriteGraphMLGraphs() {
//        return writeGraphMLGraphs;
//    }
//
//    public static void setWriteGraphMLGraphs(boolean writeGraphMLGraphs) {
//        MarkerPassingConfig.writeGraphMLGraphs = writeGraphMLGraphs;
//    }
//
//    public static boolean isUseGraphCache() {
//        return useGraphCache;
//    }
//
//    public static void setUseGraphCache(boolean useGraphCache) {
//        MarkerPassingConfig.useGraphCache = useGraphCache;
//    }
//
//    public static WordType getWordType() {
//        return wordType;
//    }
//
//    public static void setWordType(WordType wordType) {
//        MarkerPassingConfig.wordType = wordType;
//    }
//
//    public static int getDecompositionDepth() {
//        return decompositionDepth;
//    }
//
//    public  void setDecompositionDepth(int decompositionDepth) {
//        this.decompositionDepth = decompositionDepth;
//    }
//
//
//    public  Map<Concept, Double> getConceptualThreshold() {
//        return this.conceptualThreshold;
//    }
//
//    public  void setConceptualThreshold(Map<Concept, Double> conceptualThreshold) {
//        this.conceptualThreshold = conceptualThreshold;
//    }
//
//    public double getStartActivation() {
//        return this.startActivation;
//    }
//
//    public  void setStartActivation(double startActivation) {
//        this.startActivation = startActivation;
//    }
//    public  double getThreshold() {
//        return threshold;
//    }
//    public  void setThreshold(Double threshold) {
//        this.threshold=threshold;
//    }
//
//    public static boolean isUseMergedGraphCache() {
//        return useMergedGraphCache;
//    }
//
//    public  Double getDecay() {
//        return decay;
//    }
//
//    public void setThreshold(double threshold) {
//        this.threshold = threshold;
//    }
//
//    public  List<Map<Concept, List<Marker>>> getStartMarker() {
//        return startMarker;
//    }
//
//    public void setStartMarker(List<Map<Concept, List<Marker>>> startMarker) {
//        this.startMarker = startMarker;
//    }
//
//    public  double getDefinitionLinkWeight() {
//        return DefinitionLinkWeight;
//    }
//
//    public  void setDefinitionLinkWeight(double definitionLinkWeight) {
//        DefinitionLinkWeight = definitionLinkWeight;
//    }
//
//    public  double getAntonymLinkWeight() {
//        return antonymLinkWeight;
//    }
//
//    public void setAntonymLinkWeight(double antonymLinkWeight) {
//        this.antonymLinkWeight = antonymLinkWeight;
//    }
//
//    public  double getSynonymLinkWeight() {
//        return synonymLinkWeight;
//    }
//
//    public  void setSynonymLinkWeight(double synonymLinkWeight) {
//        this.synonymLinkWeight = synonymLinkWeight;
//    }
//
//    public  double getHypernymLinkWeight() {
//        return hypernymLinkWeight;
//    }
//
//    public  void setHypernymLinkWeight(double hypernymLinkWeight) {
//        this.hypernymLinkWeight = hypernymLinkWeight;
//    }
//
//    public  double getHyponymLinkWeight() {
//        return hyponymLinkWeight;
//    }
//
//    public  void setHyponymLinkWeight(double hyponymLinkWeight) {
//        this.hyponymLinkWeight = hyponymLinkWeight;
//    }
//
//    public  HashMap<String, Double> getArbitraryRelationWeights() {
//        if (this.arbitraryRelationWeights == null) {
//            this.arbitraryRelationWeights = getArbitraryRelationWeightMap();
//        }
//        return this.arbitraryRelationWeights;
//    }
//
//    public  double getLinkWeightForRelationWithName(String relationName) {
//        if (!getArbitraryRelationWeights().containsKey(relationName)) {
//            getArbitraryRelationWeights().put(relationName, defaultArbitraryRelationLinkWeight);
//        }
//        return getArbitraryRelationWeights().get(relationName);
//    }
//
//    public  void setArbitraryRelationLinkWeight(String relationName, double linkWeight) {
//        getArbitraryRelationWeights().put(relationName, linkWeight);
//    }
//
//    public  double getDefaultArbitraryRelationLinkWeight() {
//        return defaultArbitraryRelationLinkWeight;
//    }
//
//    public  int getTerminationPulsCount() {
//        return terminationPulsCount;
//    }
//
//    public  void setTerminationPulsCount(int terminationPulsCount) {
//        this.terminationPulsCount = terminationPulsCount;
//    }
//
//    public double getDoubleActivationLimit() {
//        return doubleActivationLimit;
//    }
//
//    public  void setDoubleActivationLimit(double doubleActivationLimit) {
//        this.doubleActivationLimit = doubleActivationLimit;
//    }
//
//    public  double getNegativeThreshold() {
//        return negativeThreshold;
//    }
//
//    public  void setNegativeThreshold(double negativeThreshold) {
//        this.negativeThreshold = negativeThreshold;
//    }
//
//    public  double getSyntaxLinkWeight() {
//        return syntaxLinkWeight;
//    }
//
//    public  void setSyntaxLinkWeight(double syntaxLinkWeight) {
//        this.syntaxLinkWeight = syntaxLinkWeight;
//    }
//
//    public  double getContrastLinkWeight() {
//        return contrastLinkWeight;
//    }
//
//    public  void setContrastLinkWeight(double contrastLinkWeight) {
//        this.contrastLinkWeight = contrastLinkWeight;
//    }
//
//    public  double getNerLinkWeight() {
//        return nerLinkWeight;
//    }
//
//    public  void setNerLinkWeight(double nerLinkWeight) {
//        this.nerLinkWeight = nerLinkWeight;
//    }
//
//    public  double getRoleLinkWeight() {
//        return roleLinkWeight;
//    }
//
//    public  void setRoleLinkWeight(double roleLinkWeight) {
//        this.roleLinkWeight = roleLinkWeight;
//    }
//
//    public  double getVnRoleLinkWeight() {
//        return vnRoleLinkWeight;
//    }
//
//    public  void setVnRoleLinkWeight(double vnRoleLinkWeight) {
//        this.vnRoleLinkWeight = vnRoleLinkWeight;
//    }
//
//    private  HashMap<String,Double> getArbitraryRelationWeightMap() {
//        HashMap<String, Double> weightMap = new HashMap<>();
//        return weightMap;
//    }
//    public String toString() {
//        StringBuilder stringBuilder = new StringBuilder();
//        //stringBuilder.append(System.getProperty("line.separator"));
////        stringBuilder.append("\n--- start of config ---");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(getStartActivation() + ";");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(getThreshold() + ";");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(getDefinitionLinkWeight()+ ";");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(getSynonymLinkWeight() + ";");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(getAntonymLinkWeight() + ";");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(+ getHypernymLinkWeight() + ";");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(+ getHyponymLinkWeight() + ";");
//        stringBuilder.append(+ getNerLinkWeight() + ";");
//        stringBuilder.append(+ getRoleLinkWeight() + ";");
//        stringBuilder.append(+ getSyntaxLinkWeight() + ";");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(+ getTerminationPulsCount() + ";");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(+ getDoubleActivationLimit() + ";");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(+ getDecompositionDepth() + ";");
//        //stringBuilder.append(System.getProperty("line.separator"));
//        stringBuilder.append(+ getFolds());
//        //stringBuilder.append(System.getProperty("line.separator"));
////        stringBuilder.append("ArbitraryRelations: ");
//
////        for (String relationName : getArbitraryRelationWeights().keySet()) {
////            double linkWeight = getLinkWeightForRelationWithName(relationName);
////            stringBuilder.append("\n\t" + relationName + ", " + linkWeight);
////        }
////
////        stringBuilder.append("\n--- end of config ---");
//
//        return stringBuilder.toString();
//    }
//
//}
