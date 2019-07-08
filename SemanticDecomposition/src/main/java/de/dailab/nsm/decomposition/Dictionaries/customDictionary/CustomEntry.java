package de.dailab.nsm.decomposition.Dictionaries.customDictionary;

import de.dailab.nsm.decomposition.Concept;
import de.dailab.nsm.decomposition.WordType;

import java.io.Serializable;

public class CustomEntry implements Serializable {
    public int index;
    public int fileIndex = -1;
    public String word = null;
    public WordType type = WordType.UNKNOWN;

    protected transient boolean loaded = false;
    public transient Concept concept = null;

    static final long serialVersionUID = 3925206405957663075L;

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
