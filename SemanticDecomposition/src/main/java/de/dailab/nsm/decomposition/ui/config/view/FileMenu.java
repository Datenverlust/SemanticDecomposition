/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.ui.config.view;

import de.dailab.nsm.decomposition.ui.config.ConfigGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Created by borchert on 25.07.17.
 *
 * Menubar menu for saving the configuration and terminating the application.
 */
public class FileMenu extends JMenu implements ActionListener{

    private static final String CMD_SAVE = "Save";
    //private static final String CMD_QUIT = "Quit";


    public FileMenu(){
        super("File");
        //ALT+F for accessing the menu
        this.setMnemonic(KeyEvent.VK_F);

        initItems();
    }

    private void initItems(){
        JMenuItem save = new JMenuItem(CMD_SAVE);
        save.addActionListener(this);
        this.add(save);
        //probably no need for a dedicated quit item.
        //It also is not that trivial to dispose of the main frame here.
        //And we do not wish to terminate the VM (System.exit(0)) since the
        //Ui may be called from within another thread.

//        JMenuItem quit = new JMenuItem(CMD_QUIT);
//        quit.addActionListener(this);
//        this.add(quit);

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if( e.getActionCommand().equals(CMD_SAVE) ){
            //we save the opened configursation file directly.
            ConfigGUI.getConfig().save();

//                JFileChooser fc= new JFileChooser();
//                fc.showSaveDialog(this.getParent());
//                File saveTo = fc.getSelectedFile();
//                if(! saveTo.exists() ){
//                    saveTo.createNewFile();
//                    ConfigGUI.getConfig().save(saveTo);
//                }
        }

    }
}
