/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.dictionaries.corpuslinguistics;


import de.dailab.nsm.decompostion.Dictionaries.DictUtil;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

import java.io.File;
import java.io.IOException;
import java.util.Collection;


public class CorpusLinguisticStatistics {
    File dictFile = null;
    //    String path2Vec = System.getProperty("user.home").toString() + File.separator + ".decomposition" + File.separator + "corpusLinguisticDictionary" +File.separator + ;
    String path2Vec = System.getProperty("user.home").toString() + File.separator + ".decomposition" + File.separator + "CorpusLinguisticDictionary";
    String corpusdownloadFileName = "GoogleNews-vectors-negative300.bin.gz";
    String corpusFileName = "GoogleNews-vectors-negative300.bin";
    String source = "http://dainas.dai-labor.de/~faehndrich@dai/Dictionaries/Corpora/";
    //String corpusdownloadFileName = "enwik9";
    //String corpusdownloadFileName = "freebase-vectors-skipgram1000.bin.gz"
    WordVectors vec;
    // WikisaurusArticleParser wikisaurusParser =null;
    // IWritableWiktionaryEdition wwkt=null;

    /**
     * Main entry point. The command-line arguments are concatenated together
     * (separated by spaces) and used as the word form to look up.
     */


    /**
     * @param args
     */
    public static void main(String[] args) {

        CorpusLinguisticStatistics linguisticStatistics = new CorpusLinguisticStatistics();
        linguisticStatistics.init();
        Collection<String> near = linguisticStatistics.vec.wordsNearest("frog", 10);
        for (String n : near) {
            System.out.println(n);
        }
        double sim1 = linguisticStatistics.vec.similarity("midday", "noon");
        double sim2 = linguisticStatistics.vec.similarity("cord", "smile");
        System.out.println("midday:noon: " + sim1);
        System.out.println("cord:smile: " + sim2);
        System.out.println("-ld(p(midday|noon)): " + sim1 * -1 * Math.log10(sim1) / Math.log10(2));
        System.out.println("-ld(cord|smile)): " + sim2 * -1 * Math.log10(sim2) / Math.log10(2));

        System.out.println("Decomposing frog, using the Word2Vec.");


    }


    /**
     * Initialize the Word2Vec ANN into a in memory database.
     */
    public void init() {
        if (vec == null) {
            String prtaindVectorLocation = path2Vec + File.separator + corpusFileName;
            // Connect get pre tained vectos or create them form a corpus
            File gModel = new File(prtaindVectorLocation);
            // If not availiable downlod the pre trained vector

            if (gModel.exists() == false) {
                gModel.mkdirs();
                DictUtil.downloadFileParalell(source + corpusdownloadFileName, prtaindVectorLocation);
                try {
                    DictUtil.unzip(path2Vec + File.separator +corpusdownloadFileName, prtaindVectorLocation);
                } finally {
                    DictUtil.deleteFile(path2Vec + File.separator +corpusdownloadFileName);
                }

            }
            gModel = new File(prtaindVectorLocation);
            try {
                vec = WordVectorSerializer.loadGoogleModel(gModel, true);
                //vec = WordVectorSerializer.loadTxtVectors(new File("glove.6B.50d.txt"));
            } catch (IOException e) {
                e.printStackTrace();
                try {
                   DictUtil.downloadFile(source + corpusdownloadFileName,prtaindVectorLocation);
                    vec = WordVectorSerializer.loadGoogleModel(gModel, true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }


        }
    }

}
