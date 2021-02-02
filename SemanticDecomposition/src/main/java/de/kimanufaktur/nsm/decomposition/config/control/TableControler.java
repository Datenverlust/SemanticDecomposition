/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition.ui.config.control;

import de.kimanufaktur.nsm.decomposition.settings.Config;

import javax.swing.table.AbstractTableModel;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Created by borchert on 25.07.17.
 *
 * Manages changes in the table view.
 */
public class TableControler extends AbstractTableModel {

    protected Config _cfg;
    protected final String[] columnNames = {"Property","Value"};

    public TableControler(Config cfg){
        super();
        _cfg = cfg;
    }

    @Override
    public int getRowCount() {
        return _cfg.getUserProps().entrySet().size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        List<Entry<Object, Object>> entries = getSortedEntries();
        Entry<Object, Object> entry = entries.get(rowIndex);
        return columnIndex == 0 ? entry.getKey() : entry.getValue();
    }

    /*
    * Change a value of the table
    */
    public void setValueAt(Object value, int row, int col) {
        List<Entry<Object, Object>> entries = getSortedEntries();

        //update entry
        //we know that col has to be the value, not the key
        entries.get(row).setValue(value);

        //trigger change on UI
        fireTableCellUpdated(row, col);
    }

    public boolean isCellEditable(int row, int col) {
        //allow to edit values only.
        return col == 1;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    private List<Entry<Object, Object>> getSortedEntries() {
    	return _cfg.getUserProps().entrySet().stream()
    			.sorted(Comparator.comparing(e -> e.getKey().toString()))
    			.collect(Collectors.toList());
    }
}
