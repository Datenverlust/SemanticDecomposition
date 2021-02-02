/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.dictionaries.wordnet;

import de.kimanufaktur.nsm.decompostion.Dictionaries.DictUtil;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.*;

import java.io.File;
import java.util.*;


/**
 * Displays word forms and definitions for synsets containing the word form
 * specified on the command line. To use this application, specify the word form
 * that you wish to view synsets for, as in the following example which displays
 * all synsets containing the word form "airplane": <br>
 * java TestJAWS airplane
 *
 */
public class WordNetCrawler {

    // Wordnet API RITA used the alternative would be JAWS see the lib directory
    //static RiWordnet wordnet = new RiWordnet();
    static IRAMDictionary wn = null;
    //String path2Dict = "/usr"+ File.separator + "share" + File.separator + "wordnet"+ File.separator + "dict";
    //String path2Dict = "C:"+ File.separator + "WordNet" + File.separator + "dict";
    //String path2Dict = "/usr/local/Cellar/wordnet/3.1/dict/";
    //String path2Dict = null;
    String path2DBLocation = null;

    String source = "http://kimanufaktur.de/Dictionaries/WordNet/";
    private String dictFileName = "wordNetDict.zip";
//    private String dictFileName = "wordnet.zip";
//    String source = "http://127.0.0.1/wordnet/ger/";


    /**
     * Main entry point. The command-line arguments are concatenated together
     * (separated by spaces) and used as the word form to look up.
     */
    /**
     * @param args
     */
    public static void main(String[] args) {
        WordNetCrawler wordNetCrawler = new WordNetCrawler();
        //init Wordnet into memmory
        wordNetCrawler.initWordNet();

        Map description = new HashMap<String, String>();
        // Concatenate the command-line arguments
        StringBuffer buffer = new StringBuffer();
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                buffer.append((i > 0 ? " " : "") + args[i]);
            }
        } else {
            // NSMPrime bad = nsm.getNsmConcepts().get("people");

            //buffer.append(bad.getLexicalForm());
            buffer.append("dog");
        }
        POS worttype = POS.NOUN;


        Set<IWord> synonyms = new HashSet<IWord>();
        Set<IWord> hypernyms = new HashSet<IWord>();
        Set<IWord> antonyms = new HashSet<IWord>();

        //ritaTest rita = new ritaTest();
        synonyms = wordNetCrawler.getSynonyms(buffer.toString(), worttype, 10);
        Set<IWord> recursiveSynonyms = new HashSet<IWord>();
        recursiveSynonyms.addAll(synonyms);
        for (IWord synonym : synonyms) {
            recursiveSynonyms.addAll(wordNetCrawler.getSynonyms(synonym.getLemma(), worttype, 10));

        }


        wordNetCrawler.printSynsets("Synonyms", recursiveSynonyms);

        // Now we get the antonyms
        antonyms = wordNetCrawler.getAntonymes(buffer.toString(), worttype, 10);
        Set<IWord> recursiveAntonyms = new HashSet<IWord>();
        recursiveAntonyms.addAll(antonyms);
        for (IWord synonym : antonyms) {
            recursiveAntonyms.addAll(wordNetCrawler.getAntonymes(synonym.getLemma(), worttype, 10));
        }
        wordNetCrawler.printSynsets("Antonyms", recursiveAntonyms);


        // Now we get recursive  hypernyms of all synonyms
        hypernyms = wordNetCrawler.getHypernyms(buffer.toString(), worttype, 10);
        wordNetCrawler.printSynset(buffer.toString(), hypernyms, Pointer.HYPERNYM);
        for (Iterator<IWord> i = synonyms.iterator(); i.hasNext(); ) {
            IWord synonym = i.next();
            hypernyms = wordNetCrawler.getHypernyms(synonym.getLemma(), worttype, 10);
            wordNetCrawler.printSynset(synonym.getLemma(), hypernyms, Pointer.HYPERNYM);
        }

    }

    /**
     * Initialize the WordNet instance to be used as an in-memory database for the english language.
     */
    public void initWordNet() {
        // Copy wordNet into RAM
        //path2Dict = getClass().getClassLoader().getResource("wordNetDict" + File.separator).getPath();
        path2DBLocation = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "WordNet" + File.separator + "DB";
        try {
            if (wn == null) {
                File wndic = new File(path2DBLocation);
                wn = createWordNetRAMDictionary(wndic);
                // Check for Wordnet
                // try {
                // testDictionary();
                // } catch (IOException e) {
                // System.err.println("WordNet is not set up on your computer.");
                // e.printStackTrace();
                // }

            }
        } catch (Exception e) {
            e.printStackTrace();
            DictUtil.downloadFileParalell(source + dictFileName, path2DBLocation+File.separator +dictFileName);
            DictUtil.unzip(path2DBLocation+File.separator +dictFileName, path2DBLocation+ File.separator);
            DictUtil.deleteFile(path2DBLocation+File.separator +dictFileName);

        }
    }


    /**
     * Look up all Hyperonyms of a word in WordNet.
     *
     * @param word              the english word for which the Hyperonyms should be looked up.
     * @param worttype          the type of word you want to look up: @see:edu.mit.jwi.item.IPointer
     * @param numberfoHypernyms the amount of Hypernyms you want restrict the return list to.
     * @return A list of all Hypernyms of the given word.
     */
    public Set<IWord> getHypernyms(String word, POS worttype, int numberfoHypernyms) {
        return getSynset(word, numberfoHypernyms, worttype, Pointer.HYPERNYM);
    }

    /**
     * Look up all Hyponyms of a word in WordNet.
     *
     * @param word              the english word for which the Hyponyms should be looked up.
     * @param worttype          the type of word you want to look up: @see:edu.mit.jwi.item.IPointer
     * @param numberfoHypernyms the amount of Hyponyms you want restrict the return list to.
     * @return A list of all Hyponyms of the given word.
     */
    public Set<IWord> getHyponyms(String word, POS worttype, int numberfoHypernyms) {
        return getSynset(word, numberfoHypernyms, worttype, Pointer.HYPONYM);
    }

    /**
     * Look up all Hyponyms of a word in WordNet and all hyponyms for all synonyms of the given word.
     *
     * @param word              the english word for which the Hyponyms should be looked up.
     * @param worttype          the type of word you want to look up: @see:edu.mit.jwi.item.IPointer
     * @param numberfoHypernyms the amount of Hyponyms you want restrict the return list to.
     * @return A list of all Hyponyms of the given word.
     */
    public Set<IWord> getRecursiveHyponyms(String word, POS worttype, int numberfoHypernyms) {
        // Now we get hyponyms of all synonyms
        Set<IWord> hypernyms = getHyponyms(word, worttype, numberfoHypernyms);

        Set<IWord> synonyms = getSynonyms(word, worttype, numberfoHypernyms);
        for (Iterator<IWord> i = synonyms.iterator(); i.hasNext(); ) {
            IWord synonym = i.next();
            hypernyms.addAll(getHyponyms(synonym.getLemma(), worttype, numberfoHypernyms));

        }
        return hypernyms;
    }


    /**
     * get the antonyms of a given word.
     *
     * @param word            the word to get the antonyme for
     * @param numberofResults the number of antonymes to get.
     * @param possition       the type of antonymes to get e.g. POS.Nouns
     * @return a list of antonymes to the given word.
     */
    public Set<IWord> getAntonymes(String word, POS possition, int numberofResults) {
        Set<IWord> result = new HashSet<IWord>();

        IIndexWord idxWord = wn.getIndexWord(word, possition);
        if (idxWord != null) {
            List<IWordID> wordIDs = idxWord.getWordIDs();
            if (wordIDs.size() > numberofResults) {
                wordIDs = wordIDs.subList(0, numberofResults);
            }
            for (IWordID wordID : wordIDs) {
                IWord iword = wn.getWord(wordID);
                ISynset synset = iword.getSynset();
                List<ISynsetID> antonyms = synset.getRelatedSynsets(Pointer.ANTONYM);
                if (numberofResults > antonyms.size()) {
                    numberofResults = antonyms.size();
                }
                if (antonyms.size() == 0) {
                    //ISynset synset1 = wn.getSynset(wordID.getSynsetID());
                    for (IWordID antonym : iword.getRelatedWords(Pointer.ANTONYM)) {
                        result.add(wn.getWord(antonym));
                    }
                }
                // add each antonym id to the result
                List<IWord> words;
                for (ISynsetID sid : antonyms) {
                    words = wn.getSynset(sid).getWords();
                    result.addAll(words);
                }
            }
        }
        return result;

    }


    public Map<Integer, String> getDescription(String wordForm) {
        Map<Integer, String> tmpdescription = new HashMap<Integer, String>();
        ArrayList<IWord> hypernyms = getHypernyms(wordForm, 1, POS.NOUN);
        // Print the hypernyms
        Iterator it = hypernyms.iterator();
        while (it.hasNext()) {

            String[] hypernym = (String[]) it.next();

            for (String hn : hypernym) {
                if (!tmpdescription.containsKey(hn)) {
                    tmpdescription.put(hn.hashCode(), hn);
                    tmpdescription.putAll(getDescription(hn));
                }
            }

        }
        hypernyms.clear();
        return tmpdescription;

    }


    /**
     * Display Synset on the terminal
     *
     * @param wordForm the work the synset has been calculated
     * @param synsets  the restuling sysnset fitting to the wordform form the first
     *                 parameter.
     */
    public void printSynsets(String wordForm, Set<IWord> synsets) {
        // Display the word forms and definitions for synsets retrieved
        if (!synsets.isEmpty()) {
            System.out.println("================= SYNSET for " + wordForm + " ====================");
            for (Iterator<IWord> i = synsets.iterator(); i.hasNext(); ) {
                System.out.print(i.next().getLemma());
                if (i.hasNext()) {
                    System.out.print(", ");
                } else {
                    System.out.println();
                }
            }
        } else {
            System.err.println("No synsets exist that contain "
                    + "the word form '" + wordForm + "'");
        }
    }


    /**
     * Display Synset on the terminal
     *
     * @param wordForm the work the synset has been calculated
     * @param synsets  the restuling sysnset fitting to the wordform form the first
     *                 parameter.
     */
    public void printAntonym(String wordForm, ArrayList<IWord> synsets) {
        // Display the word forms and definitions for synsets retrieved
        if (!synsets.isEmpty()) {
            System.out.println("================= ANTONYM====================");
            System.out.println("The following antonymes contain '" + wordForm
                    + "' or a possible base form " + "of that text:");
            for (Iterator<IWord> i = synsets.iterator(); i.hasNext(); ) {
                System.out.print(i.next().getLemma());
                if (i.hasNext()) {
                    System.out.print(", ");
                } else {
                    System.out.println();
                }
            }
        } else {
            System.err.println("No antonymes exist that contain "
                    + "the word form '" + wordForm + "'");
        }
    }


    /**
     * Print synset to the terminal
     *
     * @param wordForm the work the synset has been calculated
     * @param synset   the restuling hypernyms tree fitting to the wordform form the
     *                 first parameter
     * @param point    the type of realtion which is used in the synset
     */
    public void printSynset(String wordForm, Set<IWord> synset, Pointer point) {
        if (synset != null) {
            System.out.println("=======================" + point.getName() + "==================");
            System.out.println(point.getName() + " for: " + wordForm);
            Iterator it = synset.iterator();
            StringBuffer arrow = new StringBuffer("=>");
            while (it.hasNext()) {
                IWord w = (IWord) it.next();
                System.out.println(w.getLemma());
                if (it.hasNext()) {
                    System.out.print(arrow);
                    arrow.insert(0, "=");
                }

            }
        }
    }

    /**
     * Print Hypernyms to the terminal
     *
     * @param wordForm   the work the synset has been calculated
     * @param hypernymes the restuling hypernyms tree fitting to the wordform form the
     *                   first parameter
     */
    public void printHypernyms(String wordForm, Set<IWord> hypernymes) {
        if (hypernymes != null) {
            System.out.println("=======================HYPERNYMS==================");
            System.out.println("Hypernyms for: " + wordForm);
            Iterator it = hypernymes.iterator();
            StringBuffer arrow = new StringBuffer();
            arrow.append("=>");
            while (it.hasNext()) {
                IWord w = (IWord) it.next();
                System.out.println(w.getLemma());
                if (it.hasNext()) {
                    System.out.print(arrow);
                    arrow.insert(0, "=");
                }


            }
        }
    }

    /**
     * Get al Hypernyms form WordNet
     *
     * @param word             the word to get the hypernymes for
     * @param numberOfMeanings number of result to get. The result can become very big.
     * @param possition        the possition example: POS.Noun
     * @return a list of hupernumes for the given word.
     */
    public ArrayList<IWord> getHypernyms(String word,
                                         int numberOfMeanings, POS possition) {
        ArrayList<IWord> result = new ArrayList<IWord>();

        // get the synset
        IIndexWord idxWord = wn.getIndexWord(word, possition);
        List<IWordID> wordIDs = idxWord.getWordIDs();
        if (wordIDs.size() > numberOfMeanings) {
            wordIDs = wordIDs.subList(0, numberOfMeanings);
        }
        for (IWordID wirdid : wordIDs) {
            IWord iword = wn.getWord(wirdid);
            ISynset synset = iword.getSynset();
            // get the hypernyms
            List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);

            // add each hypernyms id and synonyms to the result
            List<IWord> words;
            for (ISynsetID sid : hypernyms) {
                words = wn.getSynset(sid).getWords();
                result.addAll(words);
            }
        }

        return result;
    }


    /**
     * Get given synset form WordNet
     *
     * @param word             the word to get the given synset for for
     * @param numberOfMeanings number of result to get. The result can become very big.
     * @param possition        the possition example: POS.Noun
     * @param point            the pinter to the synset. E.g. Pointer.HYPERNYM
     * @return a list of the given synset for the given word.
     */
    public Set<IWord> getSynset(String word,
                                int numberOfMeanings, POS possition, Pointer point) {
        Set<IWord> result = new HashSet<IWord>();

        // get the synset
        IIndexWord idxWord = wn.getIndexWord(word, possition);
        if (idxWord != null) {
            List<IWordID> wordIDs = idxWord.getWordIDs();
            if (wordIDs.size() > numberOfMeanings) {
                wordIDs = wordIDs.subList(0, numberOfMeanings);
            }

            for (IWordID wid : wordIDs) {
                IWord iword = wn.getWord(wid);
                ISynset synset = iword.getSynset();
                // get the hypernyms
                List<ISynsetID> hypernyms = synset.getRelatedSynsets(point);

                // add each hypernyms id and synonyms to the result
                List<IWord> words;
                for (ISynsetID sid : hypernyms) {
                    words = wn.getSynset(sid).getWords();
                    result.addAll(words);
                }
            }
        }
        return result;
    }

    /**
     * Get the synonyms of a word.
     *
     * @param word             word to get the synonyms for
     * @param numberOfMeanings number of synonyms to get. Because there can be alot. Perhaps
     *                         we just wont one?
     * @param possition        The Part of spreach to use i.e. POS.VERB, POS.NOUN,...
     * @return A map of WordNet Ids to the synonyms of the given word.
     */
    public Set<IWord> getSynonyms(String word, POS possition, int numberOfMeanings) {
        Set<IWord> result = new HashSet<IWord>();

        IIndexWord idxWord = wn.getIndexWord(word, possition);
        if (idxWord != null) {
            List<IWordID> wordIDs = idxWord.getWordIDs(); // 1st meaning
            if (wordIDs.size() > numberOfMeanings) {
                wordIDs = wordIDs.subList(0, numberOfMeanings);
            }
            for (IWordID wordid : wordIDs) {
                IWord iword = wn.getWord(wordid);
                ISynset synset = iword.getSynset();

                // iterate over words associated with the synset
                for (IWord w : synset.getWords()) {
                    if (result.size() <= numberOfMeanings) {
                        result.add(w);
                    } else {
                        break;
                    }
                }
            }
        }
        return result;
    }


    /**
     * Load WordNet into RAM for faster access
     *
     * @param wnDir path2Dict to the WordNet dictionary
     * @throws Exception
     */
    private IRAMDictionary createWordNetRAMDictionary(File wnDir) throws Exception {
        // construct the dictionary object and open it
        IRAMDictionary dict = new RAMDictionary(wnDir, ILoadPolicy.NO_LOAD);
        dict.open();
        // now load into memory
        System.out.print("\nLoading Wordnet into memory...");
        long t = System.currentTimeMillis();
        dict.load(true);
        System.out.printf("done (%1d msec)\n", System.currentTimeMillis() - t);
        // try it again, this time in memory
        //trek(dict);
        return dict;
    }

    /**
     * Treking over WordNet to test the in memory wordNet
     *
     * @param dict the dictionary to trek
     */
    public void trek(IDictionary dict) {
        int tickNext = 0;
        int tickSize = 20000;
        int seen = 0;
        System.out.print("Treking across Wordnet");
        long t = System.currentTimeMillis();
        for (POS pos : POS.values())
            for (Iterator<IIndexWord> i = dict.getIndexWordIterator(pos); i
                    .hasNext(); )
                for (IWordID wid : i.next().getWordIDs()) {
                    seen += dict.getWord(wid).getSynset().getWords().size();
                    if (seen > tickNext) {
                        System.out.print(".");
                        tickNext = seen + tickSize;
                    }
                }
        System.out.printf("done (%1d msec)\n", System.currentTimeMillis() - t);
        System.out.println("In my trek I saw " + seen + " words");
    }

    public IWord getWord(IWordID id) {
        return wn.getWord(id);
    }

    public List<IWord> getWord(String word, POS wordType) {
        List<IWord> result = new ArrayList<>();
        IIndexWord idxWord = wn.getIndexWord(word, wordType);
        if (idxWord == null) {
            return null;
        } else {
            for (IWordID wordID : idxWord.getWordIDs()) {
                result.add(wn.getWord(wordID));
            }
            return result;
        }
    }

    public Set<IWord> getMeronyms(String litheral, POS pos, int maxNumberOfMeronyms) {
        Set<IWord> result = new HashSet<>();
        result = getSynset(litheral, maxNumberOfMeronyms, pos, Pointer.MERONYM_PART);
        result.addAll(getSynset(litheral, maxNumberOfMeronyms, pos, Pointer.MERONYM_SUBSTANCE));
        result.addAll(getSynset(litheral, maxNumberOfMeronyms, pos, Pointer.MERONYM_MEMBER));
        return result;
    }
}
