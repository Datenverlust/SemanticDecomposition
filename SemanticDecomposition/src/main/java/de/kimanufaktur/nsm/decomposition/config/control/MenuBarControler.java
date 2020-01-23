/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.ui.config.control;

import javax.swing.*;

/**
 * Created by borchert on 25.07.17.
 *
 * Simply manages the MenuBarView.
 * Currently this class does not do much, but maybe for extending the functionality this comes in handy.
 */
public class MenuBarControler {

    private JMenuBar _menuBarView;

    public MenuBarControler(){
        _menuBarView = new JMenuBar();

    }

    public void addMenu(JMenu menu){
        _menuBarView.add(menu);
    }

    public JMenuBar getMenuBar(){
        return _menuBarView;
    }

}
