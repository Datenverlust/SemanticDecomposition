/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;

/**
 * Created by borchert on 19.01.2015.
 *
 * Class for conveniently managing settings
 * Offers methods for loading and saving settings
 */
public class Config {

    public enum LANGUAGE{
        EN,
        GER
    }

    private Set<String> primes = new HashSet<>();
    private Set<String> stopwords = new HashSet<>();
    private Properties userProps;
    private File propFile;
    private static Config _instance = null;
    private Log logger = LogFactory
            .getLog(Config.class);
    /**
     *              Default Properties
     *              (Property file is always assumed to be loacated in DEFAULT_PROPERTY_DIRECTORY )
     */
    public static final String DEFAULT_BASE_DIRECTORY = System.getProperty("user.home");
    public static final String DEFAULT_PROPERTY_DIRECTORY = DEFAULT_BASE_DIRECTORY + File.separator + ".decomposition";
    public static final String DEFAULT_PRIMES_DIRECTORY = DEFAULT_PROPERTY_DIRECTORY + File.separator + "primes";
    public static final String CONFIG_FILE_NAME = "decomposition.cfg";
    public static final LANGUAGE DEFAUL_LANGUAGE = Config.LANGUAGE.EN;
    public static final String PRIMES_FILE_PREFIX = "NSM_PRIMES_";
    public static final String STOPWORDS_FILE_PREFIX = "stopwords_";

    /**
     *  Custom properties
     */
    public static final String WIKTIONARY_DB_BASE_PATH= DEFAULT_PROPERTY_DIRECTORY + File.separator + "wiktionary";
    public static final String WIKTIONARY_DB_PATH_EN = WIKTIONARY_DB_BASE_PATH;
    public static final String WIKTIONARY_DB_PATH_GER = WIKTIONARY_DB_BASE_PATH + File.separator + "de";
    public static final String WIKTIONARY_DB_EN_ARCHIVE_FILE_NAME = "enwiktionary-latest-pages-meta-current.xml.bz2";
    public static final String WIKTIONARY_DB_GER_ARCHIVE_FILE_NAME = "dewiktionary-latest-pages-meta-current.xml.bz2";
    //public static final String WIKTIONARY_DB_ARCHIVE_SOURCE_URI = "http://dainas.dai-labor.de/~faehndrich@dai/Dictionaries/Wiktionary/";
    public static final String WIKTIONARY_DB_ARCHIVE_SOURCE_URI = "https://dumps.wikimedia.org/dewiktionary/latest/";


    /**
     *              Property keys
     */
    public static final String PRIMES_DIRECTORY_KEY = "primesDirectory";
    public static final String LANGUAGE_KEY = "language";
    public static final String CONFIG_FILE_KEY = "config";
    public static final String WIKTIONARY_DB_BASE_PATH_KEY = "config";
    public static final String WIKTIONARY_DB_PATH_EN_KEY = "wiktionaryDB_EN";
    public static final String WIKTIONARY_DB_PATH_GER_KEY = "wiktionaryDB_GER";
    public static final String WIKTIONARY_DB_EN_ARCHIVE_KEY  = "wiktionaryArchiveEN";
    public static final String WIKTIONARY_DB_GER_ARCHIVE_KEY  = "wiktionaryArchiveGER";
    public static final String WIKTIONARY_DB_ARCHIVE_SOURCE_URI_KEY  = "wiktionarySourceUri";
    public static final String WIKTIONARY_DB_ARCHIVE_SOURCE_URI_GER_KEY  = "wiktionarySourceUriGER";



    private static boolean FIRST_START = false;

    /**
     * Loaded matcher properties
     * @return
     */
    public final Properties getUserProps(){
        return this.userProps;
    }

    /**
     * Default matcher properties.
     * @return
     */
    public Properties getDefaultProperties(){
        Properties defaults = new Properties();
        fillInDefaultValues(defaults);
        return defaults;
    }


    private Config(){

        propFile = new File(DEFAULT_PROPERTY_DIRECTORY+File.separator+CONFIG_FILE_NAME);
        Reader fReader = null;
        try {
            fReader = new FileReader(propFile);
        } catch (FileNotFoundException e) {

            try {
                FIRST_START = true;

                /**
                 * Initialize and save properties
                 */

                userProps = new Properties();
                fillInDefaultValues(userProps);

                /**
                 * Save user properties
                 */

                if(!propFile.getParentFile().exists()){
                    propFile.getParentFile().mkdirs();
                }
                propFile.createNewFile();
                Writer fWriter = new FileWriter(propFile);
                userProps.store(fWriter, null);
                fWriter.close();
                logger.info("Created config in " + propFile.toString());

            } catch (Exception ex){
                logger.error(ex.getMessage());
                ex.printStackTrace();
            }
        }

        try {

            /**
             * Load from configs file
             */

            if(userProps == null){

                //load values
                userProps = new Properties();
                userProps.load(fReader);
                fReader.close();

                //fill in default values for missing entries
                Properties defaults = new Properties();
                fillInDefaultValues(defaults);
                mergeProperties(userProps, defaults);
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        setup();

        logger.info("Loaded config from " + propFile.toString());
    }

    /**
     * Merges loaded user properties with default values for every missing key.
     * @param userProps - Merge into this property map
     * @param defaultValues - Use this map for merging
     */
    private void mergeProperties(Properties userProps, Properties defaultValues){
        for(Map.Entry<Object,Object> e : defaultValues.entrySet()){
            if( userProps.get(e.getKey()) == null){
                userProps.put(e.getKey(), e.getValue());
                logger.debug("[CONFIG] No value for " + e.getKey() + ". Setting to default " + e.getValue());
            }
        }
    }

    /**
     * Fills the properties object with default values.
     * @param userProps
     */
    private void fillInDefaultValues(Properties userProps){
        userProps.put(PRIMES_DIRECTORY_KEY,DEFAULT_PRIMES_DIRECTORY);
        userProps.put(LANGUAGE_KEY,DEFAUL_LANGUAGE.toString());
        userProps.put(CONFIG_FILE_KEY,CONFIG_FILE_NAME);
        userProps.put(WIKTIONARY_DB_EN_ARCHIVE_KEY,WIKTIONARY_DB_EN_ARCHIVE_FILE_NAME);
        userProps.put(WIKTIONARY_DB_GER_ARCHIVE_KEY,WIKTIONARY_DB_GER_ARCHIVE_FILE_NAME);
        userProps.put(WIKTIONARY_DB_PATH_EN_KEY,WIKTIONARY_DB_PATH_EN);
        userProps.put(WIKTIONARY_DB_PATH_GER_KEY,WIKTIONARY_DB_PATH_GER);
        userProps.put(WIKTIONARY_DB_BASE_PATH_KEY,WIKTIONARY_DB_BASE_PATH);
        userProps.put(WIKTIONARY_DB_ARCHIVE_SOURCE_URI_KEY,WIKTIONARY_DB_ARCHIVE_SOURCE_URI);
    }

    private void setup(){
        /*nothing to do yet*/
    }

    public static Config getInstance(){
        if( _instance == null)
             _instance = new Config();
        return _instance;
    }

    public synchronized void setProperty(String property, String value){
        this.userProps.put(property, value);
    }

    public void save(){
        save(propFile);
    }

    public File getConfigFile(){
        return propFile;
    }

    public synchronized void save(File saveTo){
        try {
            Writer fWriter = new FileWriter(saveTo);
            userProps.store(fWriter, null);
            fWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Use this getter for retrieving the primes file to be used for the current language configuration
     * @return
     */
    public String PRIMES_FILE(){
        return PRIMES_FILE_PREFIX + getUserProps().getProperty(LANGUAGE_KEY);
    }

    /**
     * Use this getter for retrieving the stopwords file to be used for the current language configuration
     * @return
     */
    public String STOPWORDS_FILE(){
        return STOPWORDS_FILE_PREFIX + getUserProps().getProperty(LANGUAGE_KEY);
    }


    /**
     * Convenient method for retrieving the primes word list for current language setting
     * @return
     */
    public Set<String> primesWords() {
        String res = "/"+PRIMES_FILE();
        return getWordsFromResource(res);
    }

    /**
     * Convenient method for retrieving the stop word list for current language setting
     * Primes for the language setting get filtered out of the stop word list.
     * @return
     */
    public Set<String> stopWords(){
        if (stopwords.isEmpty()) {
            String res = "/" + STOPWORDS_FILE();
            stopwords.addAll(getWordsFromResource(res));
        }
        return filterPrimes(stopwords);
    }

    /**
     * Utility method for filtering out primes from the given set of words
     * @return a set of words without the semantic primes from NSM.
     */
    public Set<String> filterPrimes(Set<String> words){
        if (primes.isEmpty()) {
            primes.addAll(primesWords());
        }
        words.removeAll(primes);
        return words;
    }

    /**
     * Helper for laoding csv based resource
     * @param res - txt resource to load. Either stopwords or primes
     * @return
     */
    private Set<String> getWordsFromResource(String res){
        InputStream is = Config.class.getResourceAsStream(res);
        //BufferedReader br = new BufferedReader(  new FileReader(new File(uri)) );
        BufferedReader br = new BufferedReader(  new InputStreamReader(is) );
        Set<String> words = new HashSet();
       // br.lines().map(line ->  Collections.addAll(words, line.split(",")) );
        String line = "";
        try {
            while( (line = br.readLine()) != null){
                line = line.trim();
                if(line.length() == 0) continue;
                //ignore lines starting with '#' AND '|' (comments)
                if(line.charAt(0) != Character.valueOf('#') &&
                        line.charAt(0) != Character.valueOf('|')    ){
                    String[] token = line.split(",");
                    Collections.addAll(words, token);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

}
