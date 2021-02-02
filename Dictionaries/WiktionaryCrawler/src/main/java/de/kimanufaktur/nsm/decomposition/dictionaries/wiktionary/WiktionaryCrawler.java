/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.dictionaries.wiktionary;

import de.kimanufaktur.nsm.decompostion.Dictionaries.DictUtil;
import de.tudarmstadt.ukp.jwktl.JWKTL;
import de.tudarmstadt.ukp.jwktl.api.*;

import de.tudarmstadt.ukp.jwktl.api.util.Language;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Displays word forms and definitions for synsets containing the word form
 * specified on the command line. To use this application, specify the word form
 * that you wish to view synsets for, as in the following example which displays
 * all synsets containing the word form "airplane": <br>
 * java TestJAWS airplane
 */
public class WiktionaryCrawler {
    public static IWiktionaryEdition wkt = null;
    private File dictFile = null;
    private boolean overwriteExisting = false;
    String path2DBLocation = null;//System.getProperty("user.home").toString() + File.separator + ".decomposition" + File.separator + "wiktionary" + File.separator + "en";
    protected String dictFileName = null;
    protected String source = null;
    // WikisaurusArticleParser wikisaurusParser =null;
    // IWritableWiktionaryEdition wwkt=null;

    /**
     * Main entry point. The command-line arguments are concatenated together
     * (separated by spaces) and used as the word form to look up.
     */

    public WiktionaryCrawler(){
        path2DBLocation = System.getProperty("user.home") + File.separator + ".decomposition" + File.separator + "wiktionary";
        dictFileName = "enwiktionary-latest-pages-meta-current.xml";
        source = "https://dumps.wikimedia.org/enwiktionary/latest/";
    }

    /**
     * Constructor for configuring the crawler's paths
     * @param path2DB
     * @param dictFileName
     * @param sourceURI
     */
    public WiktionaryCrawler(String path2DB, String dictFileName, String sourceURI){
        this.path2DBLocation = path2DB;
        this.dictFileName = dictFileName;
        this.source = sourceURI;
    }
    /**
     * @param args
     */
    public static void main(String[] args) {

        WiktionaryCrawler dictionaryCrawler = new WiktionaryCrawler();
        dictionaryCrawler.initDictDBfromDumpFile();
        dictionaryCrawler.init();
        System.out.println("Decomposing dog, using the Wiktionary.");
        dictionaryCrawler.getPage("dog");
    }

//    public List<String> getWordDefinitions(String word){
//        List<String> definitions
//    }


    /**
     * Get the wikionary page of the given word
     *
     * @param word the word to look up in the wiktionary
     * @return a IWiktionaryPage or null if the word has no entry
     */
    public synchronized IWiktionaryPage getPage(String word) {

        IWiktionaryPage page = wkt.getPageForWord(word);
        //wikisaurusParser.setText(word); //if a word has no senses, the db is extended with senses regarding the wikisaurus.

        // Close the database connection.
        // wkt.close();
        return page;
    }

    /**
     * get Entries of a page
     *
     * @param page the page to get the entry from
     * @return Set<IWiktionaryEntry> the entries of the given page
     */
    public Set<IWiktionaryEntry> getEntries(IWiktionaryPage page) {
        Set<IWiktionaryEntry> entriesOfPage = new HashSet<IWiktionaryEntry>();
        if (page != null && page.getEntries() != null && page.getEntries().size() > 0) {
            entriesOfPage.addAll(page.getEntries());
        }
        return entriesOfPage;
    }

    /**
     * get all entries of a page with a given part of speech
     *
     * @param page page to get the entries from
     * @param pos  the part of speech to get
     * @return Set<IWiktionaryEntry> entries with this pos
     */
    public Set<IWiktionaryEntry> getEntries(IWiktionaryPage page, PartOfSpeech pos) {
        Set<IWiktionaryEntry> entriesOfPage = new HashSet<IWiktionaryEntry>();

        for (IWiktionaryEntry entry : page.getEntries()) {
            if (entry != null) {
                if (pos != null) {
                    if (entry.getPartOfSpeech() != null && entry.getPartOfSpeech().equals(pos)) {
                        entriesOfPage.add(entry);
                    }
                } else {
                    entriesOfPage.add(entry);
                }

            }
        }

        return entriesOfPage;
    }

    /**
     * Get the word senses of an entry
     *
     * @param entry the entry to get the senses for
     * @return a List<IWiktionarySense> with all senses
     */
    public List<IWiktionarySense> getSenses(IWiktionaryEntry entry) {
        List<IWiktionarySense> result = new ArrayList<IWiktionarySense>();
        for (IWiktionarySense sense : entry.getSenses()) {
            result.add(sense);
        }
        return result;

    }

    /**
     * Get the cleaned up List of definitions.
     *
     * @param entry the entry to extract the definitions from
     * @return List of definitions without POS
     */
    public List<String> getGlosses(IWiktionaryEntry entry) {
        List<String> result = new ArrayList<String>();
        for (IWikiString def : entry.getGlosses()) {
            result.add(def.getPlainText());
        }
        return result;
    }

    /**
     * Get the relation of a entry
     *
     * @param entry the entry to be analyzed
     * @param rel   RelationType to get. Here the following are possible:
     *              SYNONYM,ANTONYM,HYPERNYM,HYPONYM,HOLONYM,MERONYM,COORDINATE_TERM,
     *              TROPONYM,SEE_ALSO,DERIVED_TERM,ETYMOLOGICALLY_RELATED_TERM,DESCENDANT,
     *              CHARACTERISTIC_WORD_COMBINATION
     * @return List<IWiktionaryRelation> the relation of the given rel of the given entry
     */
    List<IWiktionaryRelation> getRelations(IWiktionaryEntry entry, RelationType rel) {
        List<IWiktionaryRelation> result = new ArrayList<IWiktionaryRelation>();
        for (IWiktionaryRelation relation : entry.getRelations(rel)) {
            result.add(relation);
        }
        return result;
    }


    /**
     * Get the relation of a entry
     *
     * @param word a word to look up in the dictinoarry
     * @param rel  RelationType to get. Here the following are possible:
     *             SYNONYM,ANTONYM,HYPERNYM,HYPONYM,HOLONYM,MERONYM,COORDINATE_TERM,
     *             TROPONYM,SEE_ALSO,DERIVED_TERM,ETYMOLOGICALLY_RELATED_TERM,DESCENDANT,
     *             CHARACTERISTIC_WORD_COMBINATION
     * @return List of the words having the given relation with the given word
     */
    public Set<IWiktionaryRelation> getRelations(String word, String rel) {
        Set<IWiktionaryRelation> result = new HashSet<IWiktionaryRelation>();
        Set<IWiktionaryEntry> entries = getEntries(getPage(word));
        RelationType type = RelationType.valueOf(rel);
        for (IWiktionaryEntry wiktionaryEntry : entries) {
            for (IWiktionaryRelation relation : wiktionaryEntry.getRelations(type)) {
                result.add(relation);
            }
        }
        return result;
    }

    /**
     * Get the relation of a entry
     *
     * @param word a word to look up in the dictinoarry
     * @param rel  RelationType to get. Here the following are possible:
     *             SYNONYM,ANTONYM,HYPERNYM,HYPONYM,HOLONYM,MERONYM,COORDINATE_TERM,
     *             TROPONYM,SEE_ALSO,DERIVED_TERM,ETYMOLOGICALLY_RELATED_TERM,DESCENDANT,
     *             CHARACTERISTIC_WORD_COMBINATION
     * @return List of the words having the given relation with the given word
     */
    public Set<IWiktionaryRelation> getRelations(String word, RelationType rel) {
        Set<IWiktionaryRelation> result = new HashSet<IWiktionaryRelation>();
        if (rel != null) {
            Set<IWiktionaryEntry> entries = getEntries(getPage(word));
            for (IWiktionaryEntry wiktionaryEntry : entries) {
                if (wiktionaryEntry != null) {
                    if (wiktionaryEntry.getWordLanguage() != null) {
                        //if (wiktionaryEntry.getWordLanguage().equals(Language.ENGLISH)) {
                            for (IWiktionaryRelation relation : wiktionaryEntry.getRelations(rel)) {
                                result.add(relation);
                            }
                        //}
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the entries of the given page which belong to the given part of speech
     *
     * @param word the word to get
     * @param pos  the part of speech to filter the entries. Possible are: NOUN, PROPER_NOUN, FIRST_NAME, LAST_NAME,
     *             TOPONYM, SINGULARE_TANTUM, PLURALE_TANTUM, MEASURE_WORD, VERB, AUXILIARY_VERB, ADJECTIVE, ADVERB,
     *             INTERJECTION, SALUTATION, ONOMATOPOEIA, PHRASE, IDIOM, COLLOCATION, PROVERB, MNEMONIC, PRONOUN,
     *             PERSONAL_PRONOUN, REFLEXIVE_PRONOUN, POSSESSIVE_PRONOUN, DEMONSTRATIVE_PRONOUN, RELATIVE_PRONOUN,
     *             INDEFINITE_PRONOUN, INTERROGATIVE_PRONOUN, INTERROGATIVE_ADVERB, PARTICLE, ANSWERING_PARTICLE, N
     *             EGATIVE_PARTICLE, COMPARATIVE_PARTICLE, FOCUS_PARTICLE, INTENSIFYING_PARTICLE, MODAL_PARTICLE,
     *             ARTICLE, DETERMINER, ABBREVIATION, ACRONYM, INITIALISM, CONTRACTION, CONJUNCTION, SUBORDINATOR,
     *             PREPOSITION, POSTPOSITION, AFFIX, PREFIX, SUFFIX, PLACE_NAME_ENDING, LEXEME, CHARACTER, LETTER,
     *             NUMBER, NUMERAL, PUNCTUATION_MARK, SYMBOL, HANZI, KANJI, KATAKANA, HIRAGANA, GISMU, WORD_FORM,
     *             PARTICIPLE, TRANSLITERATION
     * @return set of enties which are of the kind of part of speech
     */
    public Set<IWiktionaryEntry> getEntry(String word, PartOfSpeech pos) {
        Set<IWiktionaryEntry> result = new HashSet<IWiktionaryEntry>();
        Set<IWiktionaryEntry> entries = getEntries(getPage(word));
        for (IWiktionaryEntry wiktionaryEntry : entries) {
            if (wiktionaryEntry.getPartOfSpeech() == pos) {
                result.add(wiktionaryEntry);
            }
        }
        return result;
    }


    /**
     * Initialize the wiktionary into a in memory database.
     */
    public void init() {
        // Connect to the Wiktionary database.
        //wkt = JWKTL.openEdition(new File(getClass().getClassLoader().getResource("dict/DB/").getPath()));
        //InputStream db = getClass().getClassLoader().getResourceAsStream("dict/DB/");
        //System.out.println("Initializing the Wikipedia Wiktionary...");
        //ClassLoader classLoader = getClass().getClassLoader();
//      wkt = JWKTL.openEdition(new File(getClass().getResource("dict/DB/").toString()));

        if (wkt == null) {
            File dict = null;
            try {
                dict = new File(path2DBLocation + File.separator + "DB");
                wkt = JWKTL.openEdition(dict, 100000L);
            } catch (Exception e) {
                //e.printStackTrace();
//                    DictUtil.downloadFile(source + dictFileName, path2DBLocation+dictFileName);

                //the actual unpacked dump file is a file with almost identical name as the downloaded archive - except the archive type ending
                String dictDumpFileName = dictFileName.replace(".bz2","");
                File dumpFile = new File(path2DBLocation + File.separator + dictDumpFileName);
                if( !dumpFile.exists() ){
                    //if we never downloaded the archive do so now
                    DictUtil.downloadFileParalell(source + dictFileName+".bz2", path2DBLocation + File.separator + dictFileName);
                }
                //parse the extracted archives dump file and create a databasae from it
                initDictDBfromDumpFile();
                wkt = JWKTL.openEdition(new File(path2DBLocation + File.separator +dictFileName), 100000L);

                //delete the donwloaded archive
                DictUtil.deleteFile(path2DBLocation + File.separator + dictFileName);
            }

        }
    }

    /**
     * This created the Wikitionarry database in the home folder under .decomposition/wiktionary/DB
     */
    protected void initDictDBfromDumpFile() {
//        dictFile = new File(classLoader.getResource("WiktionaryDict/enwiktionary-20150102-pages-meta-current.xml.bz2").getFile());
        dictFile = new File(path2DBLocation + File.separator + dictFileName);
        //wkt = new WritableBerkeleyDBWiktionaryEdition(new File(getClass().getClassLoader().getResource("WiktionaryDict/DB/").getPath()),false,Runtime.getRuntime().maxMemory() / 10);

        //URL path = classLoader.getResource("dict"+ File.separator);
        File file = new File(path2DBLocation);
        if (file.exists() == false) {
            file.mkdirs();
        }
        file = new File(path2DBLocation+ File.separator + "DB");
        /*try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docFactory.newDocumentBuilder();
                Document doc = null;
                doc = docBuilder.parse(dictFile.getAbsoluteFile());
                NodeList list = doc.getElementsByTagName("page");
                System.out.println("Total Number of Pages to scan: " + list.getLength());

            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

        try {
            JWKTL.parseWiktionaryDump(dictFile, file, true, true);
        } catch (Exception e) {
            //There seems to be a problem with different german dumps which all lead to a nullpointer exeption
            //in the parsing library when about 811 MB of data have been parsed.
            e.printStackTrace();
        }
       /* }catch (IllegalStateException e){
            e.printStackTrace();
        }*/

    }


}

