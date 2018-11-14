/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.ui.config;

import de.dailab.nsm.decomposition.settings.Config;
import de.dailab.nsm.decomposition.ui.config.view.MainFrame;

import java.awt.*;

/**
 * Created by borchert on 25.07.17.
 *
 * This class provides a graphical user interface for editing the decomposition configuration file
 * for all those who are not familiar with editing text based files in a simple text editor or a sophisticated
 * editing tool of choice.
 *
 *      [WIP]
 */
public class ConfigGUI {

    private static Config _Config;

    /**
     * Creates and displayes the GUI
     * @return
     */
    public static boolean start(Config cfg){
        _Config = cfg;
        try {
            //TODO: application does not terminate when the main frame is closed
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MainFrame frame = new MainFrame(cfg);
                    frame.setVisible(true);
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Global accessor of the config instance for use in ui components.
     * @return
     */
    public static Config getConfig(){
        return _Config;
    }

    public static void dispose(){
        _Config = null;
    }

    public static void main(String[] args){
        start(Config.getInstance());
    }

}
