/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.dailab.nsm.decomposition.manualDefinition.input;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;

import java.util.Objects;

/**
 * Created by Ghadh on 08.12.2015.
 */
public class LetterTextField extends TextField {


    @Override public void replaceText(int start, int end, String text) {
        if (text.matches("[A-Za-z]") || text == "") {
            super.replaceText(start, end, text);
        }

    }
    @Override public void replaceSelection(String text) {
        if (text.matches("[A-Za-z]") || text == "" ) {
            super.replaceSelection(text);
        }
    }

    public LetterTextField(){
        super();
    }


}
