/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.ui.config.view;

import de.kimanufaktur.nsm.decomposition.settings.Config;
import de.kimanufaktur.nsm.decomposition.ui.config.ConfigGUI;
import de.kimanufaktur.nsm.decomposition.ui.config.control.MenuBarControler;
import de.kimanufaktur.nsm.decomposition.ui.config.control.TableControler;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by borchert on 25.07.17.
 *
 * Main window instance. Creates a table view for displaying and editing of configuration values.
 */
public class MainFrame extends JFrame implements WindowListener{

    /**
     * Controller
     */
    protected TableControler _tableControl;
    protected MenuBarControler _menuBarControler;

    /**
     * Views
     *
     */
    protected JTable _tableView;
    protected JMenuBar _menuBar;

    public MainFrame(Config cfg){
        super();
        setTitle("SeMa² Config Editor");
        //data view
        _tableControl = new TableControler(cfg);
        _tableView = new JTable(_tableControl);

        //container view
        JScrollPane scrollPane = new JScrollPane(_tableView);
        _tableView.setFillsViewportHeight(true);

        //menu bar
        _menuBarControler = new MenuBarControler();
        _menuBarControler.addMenu(new FileMenu());

        this.setName("SeMa2 Properties");
        this.add(scrollPane);
        this.setJMenuBar(_menuBarControler.getMenuBar());
        this.setDefaultCloseOperation(this.DISPOSE_ON_CLOSE);
        this.pack();
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        _tableControl = null;
        _menuBarControler = null;
        _tableView = null;
        _tableView = null;
        System.exit(0);
        ConfigGUI.dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        e.getWindow().dispose();
        System.exit(0);
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

}
