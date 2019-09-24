package de.dailab.nsm.decomposition;
/*
 *
 *
 *  Created by borchert on 2019-08-20
 *
 */

import de.dailab.nsm.decomposition.Dictionaries.RDFXMLDictionary;
import de.dailab.nsm.decomposition.exceptions.DictionaryDoesNotContainConceptException;

public class MeasurementDictTest {

    public static void main(String[] args){
        Decomposition d = new Decomposition();
        RDFXMLDictionary measurementDict = null;

        for( int i= 0; i< Decomposition.getDictionaries().size(); i++){
            if( Decomposition.getDictionaries().get(i).getDictName().equals("Measurement") ){
                measurementDict = (RDFXMLDictionary)Decomposition.getDictionaries().get(i);
                break;
            }
        }

        Concept meter_ger = new Concept("kg"); //GER
        Concept metre = new Concept( "g"); //BR ENG
        Concept meter_am = new Concept("hasFactor"); //"AM ENG"

        Concept laenge = new Concept("meter"); //GER
        Concept length = new Concept( "kilogram"); //ENG


        try {

            meter_ger = measurementDict.fillConcept(meter_ger, WordType.NN);
            metre = measurementDict.fillConcept(metre, WordType.NN);
            meter_am = measurementDict.fillConcept(meter_am, WordType.NN);
            printConcept(meter_ger);
            printConcept(metre);
            printConcept(meter_am);

            laenge = measurementDict.fillConcept(laenge, WordType.NN);
            length = measurementDict.fillConcept(length, WordType.NN);
            printConcept(laenge);
            printConcept(length);

        } catch (DictionaryDoesNotContainConceptException e) {
            e.printStackTrace();
        }
    }

    public static void printConcept(Concept c){
        System.out.println(c.getLitheral() + ":");
        for( Concept hyper : c.hypernyms){
            System.out.println(c.getLitheral() + " is a/an " + hyper.getLitheral());
        }
        for( Concept hypo : c.hyponyms){
            System.out.println(hypo.getLitheral() + " is a/an " +c.getLitheral());
        }
        for( Concept mero : c.getMeronyms()){
            System.out.println(mero.getLitheral() + " is attribute of " +c.getLitheral());
        }
        System.out.println("------------------------");

    }

}
