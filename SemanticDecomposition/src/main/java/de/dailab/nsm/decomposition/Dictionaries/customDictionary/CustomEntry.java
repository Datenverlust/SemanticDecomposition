package de.dailab.nsm.decomposition.Dictionaries.customDictionary;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.WordType;

import java.io.Serializable;

public class CustomEntry implements Serializable {
    public int index;
    public int fileIndex = -1;
    public String word = null;
    public WordType type = WordType.UNKNOWN;

    transient boolean loaded = false;
    transient Concept concept = null;

    CustomEntry(int index) {
        this.index = index;
    }

    CustomEntry(int index, Concept concept) {
        this(index);

        this.concept = concept;
        this.loaded = true;

        this.word = concept.getLitheral();
        this.type = concept.getWordType();
    }

    @Override
    public String toString() {
        return "[index: "+ index + ", fileIndex: " + fileIndex + ", word: " + word +  ", type: " + type + "]";
    }
}
