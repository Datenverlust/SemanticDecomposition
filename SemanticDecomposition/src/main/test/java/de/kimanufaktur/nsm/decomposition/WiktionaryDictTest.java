package de.dailab.nsm.decomposition;
/*
 *
 *
 *  Created by borchert on 2019-08-15
 *
 */

import de.dailab.nsm.decomposition.Dictionaries.WiktionaryDictionary;
import de.dailab.nsm.decomposition.settings.Config;

public class WiktionaryDictTest {

    public static void main(String[] args){

        Config cfg = Config.getInstance();
        cfg.setProperty(Config.LANGUAGE_KEY, "GER");
        cfg.save();
        Decomposition.init();
        WiktionaryDictionary wkd = WiktionaryDictionary.getInstance();
        Concept sun = wkd.getLemma("Sonne", WordType.NN);
        System.out.println( sun.getLitheral() );
        System.out.println(sun.getId4Dictionary(wkd));

    }
}
