/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 *//*


package de.kimanufaktur.nsm.decomposition.manualDefinition.model;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.Decomposition;
import de.kimanufaktur.nsm.decomposition.Definition;
import de.kimanufaktur.nsm.decomposition.Dictionaries.BaseDictionary;
import de.kimanufaktur.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;
import de.kimanufaktur.nsm.decomposition.manualDefinition.output.ManualDefinitionController;
import de.kimanufaktur.nsm.decomposition.manualDefinition.selectDefinition.SelectDefinitionController;
import edu.stanford.nlp.util.ArraySet;

import java.util.*;

*/
/**
 * Created by Ghadh on 11.11.2015.
 *//*

public class Delegate {
    static Map<Integer, Concept> definedConcepts = Collections.synchronizedMap(new HashMap<>());
    private Decomposition decomposition;

    public Delegate() {
    }

    public static Concept getDefinedConcepts(Concept concept) {
        assert concept != null;
        if (definedConcepts.containsKey(concept.hashCode())) {
            return definedConcepts.get(concept.hashCode());
        }
        String lemma = concept.getLemma();
        if (lemma != null) {
            if (definedConcepts.containsKey(new Concept(lemma))) {
                return definedConcepts.get(new Concept(lemma));
            }
        }
        return null;
    }

    public void setDecomposition(Decomposition decomposition) {
        this.decomposition = decomposition;
    }

    */
/**
     * Create a list of ignored {@link Concept} from the List of {@link Definition}
     *
     * @param definition list of concept that should be checked
     * @return a list of ignored concept
     *//*

    public List<Concept> createIgnoredConcepts(List<Concept> definition) {
        List<Concept> ignoredList = new ArrayList<Concept>();
        Collection<Concept> cs = Decomposition.getConcepts2Ignore();

        for (int i = 0; i < definition.size(); i++) {
            if (cs.contains(definition.get(i))) {
                ignoredList.add(definition.get(i));
            }
        }
        return ignoredList;
    }

    */
/**
     * Create a definition from a string-input
     *
     * @param concept a concept that should be defined
     * @param input   a string definition
     * @return the given concept with the created definition
     *//*

    public Concept createDefinitionFromString(Concept concept, String input) {
        if (concept != null && input != null) {
            if (concept.getDefinitions() != null) {
                if (!concept.getDefinitions().isEmpty()) {
                    concept.getDefinitions().clear();
                }
                concept.getDefinitions().add(new Definition(input));
            }
        }
        return concept;
    }

    */
/**
     * Count of concepts in the first definition of the given concept
     *
     * @param concept a concept that its first definition will be counted
     * @return integer number of the concepts in the first definition in the given concept
     *//*

    public int noOfConceptsInDefinition(Concept concept) {
        if (concept != null) {
            if (concept.getDefinitions() != null) {
                if (!concept.getDefinitions().isEmpty()) {
                    return concept.getDefinitions().size();
                }
            }
        }
        return 0;
    }

    */
/**
     * remove all circular definitions of the given concept
     *
     * @param concept a concept that its definition should be checked if a circular definition
     * @return the given concept with its non-circular definitions
     *//*

    public Concept removeCycleDef(Concept concept) {

        List<Definition> list4removedDef = new ArrayList<>();
        for (Definition definition : concept.getDefinitions()) {
            if (checkCycle(concept, definition, 3)) {
                list4removedDef.add(definition);
            }
        }
        concept.getDefinitions().removeAll(list4removedDef);
        return concept;
    }

    */
/**
     * A interface to GUI, that allows a user to select a definition for a given concept
     *
     * @param concept a concept, that should be defined
     * @return the given concept with the selected definition
     *//*

    public Concept showSelectDefinition(Concept concept) {
        assert concept != null;
        if (!definedConcepts.containsKey(concept.hashCode())) {
            concept = removeCycleDef(concept);
            if (concept.getDefinitions().size() > 1) {
                SelectDefinitionController selectContr = new SelectDefinitionController(concept);
                selectContr.setDelegate(this);
                concept = selectContr.getSelectedDefinition();

            } else {
                // if all definitions have been deleted cause cycle
                concept = getManualDefinition(concept);
            }
            definedConcepts.put(concept.hashCode(), concept);
        } else {
            concept = definedConcepts.get(concept.hashCode());
        }
        return concept;
    }

    */
/**
     * Compare all concepts in the definition and their features with the given concept
     *
     * @param concept    a concept that will be compared with concepts of a given definition
     * @param definition a definition to compare it with a given concept
     * @return true if the given concept equal to any concept in the definition
     *//*

    private boolean compareIfEqual(Concept concept, Definition definition, int depth) {
        boolean equal = false;
        if (depth < 0) {
            return equal;
        }
        Queue<Concept> queue = new LinkedList<>();
        Set<Concept> checked = new ArraySet<>();

        for (Concept c : definition.getDefinition()) {
            queue.add(c);
            int currentDepth = 0,
                    counterToDepthIncrease = 1,
                    featuresSize = 0;

            for (int i = 0; i < 3; i++) {
                while (!queue.isEmpty() && !equal) {
                    Concept element = queue.poll();
                    if (element.equals(concept)) {
                        equal = true;
                        return equal;
                    }
                    featuresSize += element.getAllFeatures().size();
                    if (--counterToDepthIncrease == 0) {
                        if (++currentDepth > depth) return equal;
                        counterToDepthIncrease = featuresSize;
                        featuresSize = 0;
                    }
                    checked.add(element);
                    Collection<Concept> features = element.getAllFeatures();
                    for (Concept feature : features) {
                        if (!checked.contains(feature))
                            queue.add(feature);
                    }
                }

            }

        }
        return equal;
    }

    */
/**
     * Circularity detecting .
     *
     * @param concept  a concept that will be checked if circular with the given concept
     * @param def      a definition that will be checked if it is circular with the given concept
     * @param maxDepth a depth of the search for the circularity
     * @return false if the definition does not contain cycle with the given concept
     *//*

    public boolean checkCycle(Concept concept, Definition def, int maxDepth) {
        boolean isCircular = false;
        if (maxDepth < 0) {
            return isCircular;
        }
        Queue<Concept> queue = new ArrayDeque<Concept>();
        Set<Concept> checked = new ArraySet<>();
        queue.add(concept);
        int currentDepth = 0,
                counterToDepthIncrease = 1,
                featuresSize = 0;
        while (!queue.isEmpty() && !isCircular) {
            Concept current = queue.poll();
            if (compareIfEqual(current, def, maxDepth)) {
                isCircular = true;
                return isCircular;
            }
            featuresSize += current.getAllFeatures().size();
            if (--counterToDepthIncrease == 0) {
                if (++currentDepth > maxDepth) return isCircular;
                counterToDepthIncrease = featuresSize;
                featuresSize = 0;
            }
            checked.add(current);
            Collection<Concept> features = current.getAllFeatures();
            if (features.size() > 0)
                for (Concept feature : features) {
                    if (!checked.contains(feature))
                        queue.add(feature);
                }
        }
        return isCircular;
    }

    */
/**
     * @param concept  a concept that will be compared with concepts of the given definition and their synonyms and antonyms
     * @param def      a definition that should  be checked if it is circular
     * @param maxDepth a maximal depth for the checking of synonyms and antonyms
     * @return a concept in the given definition that it is not allowed to exists in the definition
     *//*

    public Concept getConceptWithCycle(Concept concept, Definition def, int maxDepth) {
        Concept cycleConceptInDef = null;
        if (maxDepth < 0) {
            return cycleConceptInDef;

        }
        Queue<Concept> queue = new ArrayDeque<Concept>();
        Set<Concept> checked = new ArraySet<>();
        queue.add(concept);

        int currentDepth = 0,
                counterToDepthIncrease = 1,
                featuresSize = 0;

        while (!queue.isEmpty()) {
            Concept current = queue.poll();
            cycleConceptInDef = getCycleConceptInDef(current, def, maxDepth);
            if (cycleConceptInDef != null) {
                //    cycleConcept.addAll(cycleConceptInDef);
                return cycleConceptInDef;
            }

            featuresSize += current.getAllFeatures().size();
            if (--counterToDepthIncrease == 0) {
                if (++currentDepth > maxDepth) return cycleConceptInDef;
                counterToDepthIncrease = featuresSize;
                featuresSize = 0;
            }
            checked.add(current);
            Collection<Concept> features = current.getAllFeatures();
            if (features.size() > 0)
                for (Concept feature : features) {
                    if (!checked.contains(feature))
                        queue.add(feature);
                }

        }
        return cycleConceptInDef;
    }

    */
/**
     * Checking, which concept in the given definition or its synonyms and antonyms equals the given concept
     *
     * @param concept    a concept that will be compared with the concepts of the given definition
     * @param definition a definition that it concepts should be checked
     * @param maxDepth   a maximal depth for the checking of synonyms and antonyms
     * @return a concept in the given definition that equals the given concept or one of its synonyms or antonyms
     *//*

    private Concept getCycleConceptInDef(Concept concept, Definition definition, int maxDepth) {
        Concept cycleConcept = null;
        if (maxDepth < 0) {
            return cycleConcept;
        }
        Queue<Concept> queue = new LinkedList<>();
        Set<Concept> checked = new ArraySet<>();

        for (Concept c : definition.getDefinition()) {
            queue.add(c);
            int currentDepth = 0,
                    counterToDepthIncrease = 1,
                    featuresSize = 0;

            while (!queue.isEmpty()) {
                Concept element = queue.poll();
                if (element.equals(concept)) {
                    cycleConcept = c;
                    return cycleConcept;
                }
                featuresSize += element.getAllFeatures().size();
                if (--counterToDepthIncrease == 0) {
                    if (++currentDepth > maxDepth) return cycleConcept;
                    counterToDepthIncrease = featuresSize;
                    featuresSize = 0;
                }
                checked.add(element);
                Collection<Concept> features = element.getAllFeatures();
                for (Concept feature : features) {
                    if (!checked.contains(feature))
                        queue.add(feature);
                }
            }


        }
        return cycleConcept;
    }

    */
/**
     * Fill all concepts in the first definition of the given concept (e.g. the synonyms and the definitions)
     *
     * @param concept a concept, which its definition should  be filled
     * @return the given concept with a first filled definition
     *//*

    public Concept fillConceptInDefinition(Concept concept) {
        if (concept != null) {
            if (concept.getDefinitions() != null && concept.getDefinitions().size() > 0) {
                Iterator<Definition> defIt = concept.getDefinitions().iterator();
                while(defIt.hasNext()){
                    Definition d = defIt.next();
                    List<Concept> IgnoreList = createIgnoredConcepts(d.getDefinition());
                    for (int i = 0; i < d.getDefinition().size(); i++) {
                        if (!IgnoreList.contains(d.getDefinition().get(i))) {
                            for (BaseDictionary dic : Decomposition.dictionaries) {
                                try {
                                    //update the concept with further information
                                    d.getDefinition().set(i,
                                            dic.fillConcept(d.getDefinition().get(i),
                                                    d.getDefinition().get(i).getWordType()));
                                } catch (DictionaryDoesNotContainConceptException e) {
                                    continue;
                                }
                            }
                        }

                    }
                }
            }
        }
        return concept;
    }

    */
/**
     * Check the definition of the given concept to create a good definition for
     * the semantic decomposition (this will be used by the manuel definition)
     *
     * @param concept the concept to be checked
     * @return the given concept with the created definition
     *//*

    public Concept checkDefinition(Concept concept) {

        int k = noOfConceptsInDefinition(concept);
        List<Concept> definition = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            // extract ignored concept
            definition = concept.getDefinitions().iterator().next().getDefinition();
            List<Concept> ignored = createIgnoredConcepts(definition);

            if (ignored.contains(concept.getDefinitions().iterator().next().getDefinition().get(i)))
                continue;

            Concept prime = Decomposition.getPrimeofConcept(definition.get(i));

            if (prime != null) {//check of prime or synonym of prime
                definition.set(i, prime);
                continue;
            }

            if (Decomposition.getKnownConcept(definition.get(i)).getDecompositionlevel()>=0) {
                continue;
            }

            HashSet<Concept> synonyms = null;
            HashSet<Concept> antonyms = null;
            Concept currentConcept = definition.get(i);
            if (currentConcept.getSynonyms() != null) synonyms = currentConcept.getSynonyms();
            if (currentConcept.getAntonyms() != null) antonyms = currentConcept.getAntonyms();

            boolean flagForKnownAnt = false;
            boolean flagForPrimeAnt = false;
            Concept primeAnt = null;
            List<Concept> knownAnt = new ArrayList<>();
            if (antonyms != null && antonyms.size() >= 1) {
                for (Concept ant : antonyms) {
                    if (Decomposition.checkIsPrime(ant)) {
                        primeAnt = ant;
                        flagForPrimeAnt = true;
                        break;
                    }
                }
                if (!flagForPrimeAnt) {
                    for (Concept ant : antonyms) {
                        if (Decomposition.getKnownConcept(ant).getDecompositionlevel()>=0) {
                            flagForKnownAnt = true;
                            knownAnt.add(ant);
                        }
                    }
                }
            }

            if (synonyms != null && synonyms.size() >= 1) {
                for (Concept c : synonyms) {
                    if (Decomposition.getKnownConcept(c).getDecompositionlevel()>=0|| Decomposition.checkIsPrime(c)) {
                        definition.get(i).getAlternativeSyn().add(c);
                    }
                }
                */
/* known synonym is fined*//*

                if (concept.getDefinitions().iterator().next().getDefinition().get(i).getAlternativeSyn().size() > 1) continue;
                if (flagForPrimeAnt) {
                    Concept not = Decomposition.createConcept("not");
                    definition.set(i, not);
                    definition.add(i + 1, primeAnt);
                    k++;
                    i++;
                    continue;
                }
                if (flagForKnownAnt) {
                    definition.get(i).getAlternativeAnt().addAll(knownAnt);
                    continue;
                }

            } else { // synonym size= 0
                if (flagForPrimeAnt) {
                    Concept not = Decomposition.createConcept("not");
                    definition.set(i, not);
                    definition.add(i + 1, primeAnt);
                    continue;
                }
                if (flagForKnownAnt) {
                    definition.get(i).getAlternativeAnt().addAll(knownAnt);
                    continue;
                }
            }
            if (definition.get(i).getDefinitions().size() > 1) {  // TODO: choose one def? brauchen wir das? es ist endlich?
                Concept c = showSelectDefinition(definition.get(i));
                definition.set(i, c);
                continue;
            }
            if (definition.get(i).getDefinitions().size() == 0 ||
                    definition.get(i).getDefinitions().isEmpty()) {
                Concept c = getManualDefinition(definition.get(i));
                definition.set(i, c);

                continue;
            }

        }
        concept.getDefinitions().iterator().next().setDefinition(definition);
        return concept;

    }

    */
/**
     * Check the definition if its concepts can will be replaced with synonym or antonym of primes or known concepts
     *
     * @param concept the concept, that its definition will be checked
     * @return the given concept with its definition after the checking
     *//*

    public Concept checkDefinitionForSelection(Concept concept) {
        assert concept != null;
        int k = noOfConceptsInDefinition(concept);

        for (int i = 0; i < k; i++) {
            // extract ignored concept
            List<Concept> ignored = createIgnoredConcepts(concept.getDefinitions().iterator().next().getDefinition());
            if (ignored.contains(concept.getDefinitions().iterator().next().getDefinition().get(i)))
                continue;

            Concept prime = Decomposition.getPrimeofConcept(concept.getDefinitions().iterator().next().getDefinition().get(i));

            if (prime != null) {//check of prime or synonym of prime
                concept.getDefinitions().iterator().next().getDefinition().set(i, prime);
                continue;
            }

            if (Decomposition.getKnownConcept(concept.getDefinitions().iterator().next().getDefinition().get(i)).getDecompositionlevel()>=0) {
                continue;
            }

            HashSet<Concept> synonyms = null;
            HashSet<Concept> antonyms = null;
            Concept currentConcept = concept.getDefinitions().iterator().next().getDefinition().get(i);
            if (currentConcept.getSynonyms() != null) synonyms = currentConcept.getSynonyms();
            if (currentConcept.getAntonyms() != null) antonyms = currentConcept.getAntonyms();

            boolean flagForKnownAnt = false;
            boolean flagForPrimeAnt = false;
            Concept primeAnt = null;
            Concept knownAnt = null;
            if (antonyms.size() >= 1 && antonyms != null) {
                for (Concept ant : antonyms) {
                    if (Decomposition.checkIsPrime(ant)) {
                        primeAnt = ant;
                        flagForPrimeAnt = true;
                        break;
                    }
                }
                if (!flagForPrimeAnt) {
                    for (Concept ant : antonyms) {
                        if (Decomposition.getKnownConcept(ant).getDecompositionlevel()>=0) {
                            flagForKnownAnt = true;
                            knownAnt = ant;
                            break;
                        }
                    }
                }
            }

            if (synonyms.size() >= 1 && synonyms != null) {
                boolean foundSyn = false;
                for (Concept con : synonyms) {
                    if (Decomposition.checkIsPrime(con)) {
                        foundSyn = true;
                        concept.getDefinitions().iterator().next().getDefinition().set(i, con);
                        break;
                    }
                }
                if (foundSyn) continue;
                for (Concept con : synonyms) {
                    if (Decomposition.getKnownConcept(con).getDecompositionlevel()>=0) {
                        foundSyn = true;
                        concept.getDefinitions().iterator().next().getDefinition().set(i, con);
                    }
                }
                if (foundSyn) continue;
                if (flagForPrimeAnt) {
                    Concept not = Decomposition.createConcept("not");
                    concept.getDefinitions().iterator().next().getDefinition().set(i, not);
                    concept.getDefinitions().iterator().next().getDefinition().add(i + 1, primeAnt);
                    k++;
                    i++;
                    continue;
                }
                if (flagForKnownAnt) {
                    Concept not = Decomposition.createConcept("not");
                    concept.getDefinitions().iterator().next().getDefinition().set(i, not);
                    concept.getDefinitions().iterator().next().getDefinition().add(i + 1, knownAnt);
                    k++;
                    i++;
                    continue;
                }

            } else { // synonym size= 0
                if (flagForPrimeAnt) {
                    Concept not = Decomposition.createConcept("not");
                    concept.getDefinitions().iterator().next().getDefinition().set(i, not);
                    concept.getDefinitions().iterator().next().getDefinition().add(i + 1, primeAnt);
                    k++;
                    i++;
                    continue;
                }
                if (flagForKnownAnt) {
                    Concept not = Decomposition.createConcept("not");
                    concept.getDefinitions().iterator().next().getDefinition().set(i, not);
                    concept.getDefinitions().iterator().next().getDefinition().add(i + 1, knownAnt);
                    k++;
                    i++;
                    continue;
                }
            }

        }
        return concept;
    }

    */
/**
     * A interface for a GUI, which the user can enter a definition for the given concept
     *
     * @param concept the concept which will be defined by the user
     * @return the given concept with the entered definition
     *//*

    public Concept getManualDefinition(Concept concept) {
        assert concept != null;
        if (!definedConcepts.containsKey(concept.hashCode())) {
            ManualDefinitionController defController = new ManualDefinitionController(concept);
            defController.setDelegate(this);
            concept = defController.show();
            definedConcepts.put(concept.hashCode(), concept);
        } else {
            concept = definedConcepts.get(concept.hashCode());
        }
        return concept;
    }

    public boolean isPrimeOrKnownConcept(Concept concept) {
        if (concept != null) {
            if (Decomposition.checkIsPrime(concept)) return true;
            return Decomposition.getKnownConcept(concept).getDecompositionlevel() >= 0;

        }
        return false;
    }

    public Map<Integer, Concept> getDefinedConcepts() {
        return definedConcepts;
    }
}
*/
