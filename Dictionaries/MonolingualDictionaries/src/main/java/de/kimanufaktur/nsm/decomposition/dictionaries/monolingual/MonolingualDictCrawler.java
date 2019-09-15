/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.dictionaries.monolingual;

import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;



/**
 * Displays word forms and definitions for synsets containing the word form
 * specified on the command line. To use this application, specify the word form
 * that you wish to view synsets for, as in the following example which displays
 * all synsets containing the word form "airplane": <br>
 * java TestJAWS airplane
 */
public class MonolingualDictCrawler {
    File dictFile = null;
    /**
     * Main entry point. The command-line arguments are concatenated together
     * (separated by spaces) and used as the word form to look up.
     */
    /**
     * @param args
     */
    public static void main(String[] args) {
        MonolingualDictCrawler dictionaryCrawler = new MonolingualDictCrawler();
        dictionaryCrawler.init();

        try {
            dictionaryCrawler.readDict();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void readDict() throws IOException {
        CsvReader dictReader = null;
        try {
            if(dictFile.exists()) {
                dictReader = new CsvReader(dictFile.getPath());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        dictReader.readHeaders();

        while (dictReader.readRecord())
        {
            String productID = dictReader.get("ProductID");
            String productName = dictReader.get("ProductName");
            String supplierID = dictReader.get("SupplierID");
            String categoryID = dictReader.get("CategoryID");
            String quantityPerUnit = dictReader.get("QuantityPerUnit");
            String unitPrice = dictReader.get("UnitPrice");
            String unitsInStock = dictReader.get("UnitsInStock");
            String unitsOnOrder = dictReader.get("UnitsOnOrder");
            String reorderLevel = dictReader.get("ReorderLevel");
            String discontinued = dictReader.get("Discontinued");

            // perform program logic here
            System.out.println(productID + ":" + productName);
        }

        dictReader.close();

    }

    public void init() {
        ClassLoader classLoader = getClass().getClassLoader();
        dictFile = new File(classLoader.getResource("monoDict/DictionaryForMIDs_SimpleWiki_EngEng/DfM_SimpleWiki_EngEng/dictionary/directory1.csv").getFile());
    }


}
