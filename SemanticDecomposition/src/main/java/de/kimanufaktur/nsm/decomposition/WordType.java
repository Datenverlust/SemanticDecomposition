/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition;

import org.neo4j.ogm.annotation.NodeEntity;

import java.io.Serializable;

/**
 * Created by faehndrich on 12.11.14.
 * CC Coordinating conjunction
 CD Cardinal number
 DT Determiner
 EX Existential there
 FW Foreign word
 IN Preposition or subordinating conjunction
 JJ Adjective
 JJR Adjective, comparative
 JJS Adjective, superlative
 LS List item marker
 MD Modal
 NN Noun, singular or mass
 NNS Noun, plural
 NNP Proper noun, singular
 NNPS Proper noun, plural
 PDT Predeterminer
 POS Possessive ending
 PRP Personal pronoun
 PRP$ Possessive pronoun
 RB Adverb
 RBR Adverb, comparative
 RBS Adverb, superlative
 RP Particle
 SYM Symbol
 TO to
 UH Interjection
 VB Verb, base form
 VBD Verb, past tense
 VBG Verb, gerund or present participle
 VBN Verb, past participle
 VBP Verb, non­3rd person singular present
 VBZ Verb, 3rd person singular present
 WDT Wh­determiner
 WP Wh­pronoun
 WP$ Possessive wh­pronoun
 WRB Wh­adverb
 */
@NodeEntity
public enum WordType implements Serializable{
    //NOUN, VERB, ADJECTIVE, ADVERB, PARTICLE, PROPOSITION, UNKNOWN;
    JJ {
        @Override
        public String type() {
            return "ADJECTIVE";
        }
    }, NN {
        @Override
        public String type() {
            return "NOUN";
        }
    }, NNP {
        @Override
        public String type() {
            return "NOUN";
        }
    }, NNPS {
        @Override
        public String type() {
            return "NOUN";
        }
    }, NNS {
        @Override
        public String type() {
            return "NOUN";
        }
    }, RB {
        @Override
        public String type() {
            return "ADVERB";
        }
    }, RP {
        @Override
        public String type() {
            return "PARTICLE";
        }
    }, IN {
        @Override
        public String type() {
            return "PROPOSITION";
        }
    }, VB {
        @Override
        public String type() {
            return "VERB";
        }
    }, VBD {
        @Override
        public String type() {
            return "VERB";
        }
    }, VBN {
        @Override
        public String type() {
            return "VERB";
        }
    }, VBP {
        @Override
        public String type() {
            return "VERB";
        }
    }, TO {
        @Override
        public String type() {
            return "PREPOSITION";
        }
    }, VBG {
        @Override
        public String type() {
            return "VERB";
        }
    }, CC {
        @Override
        public String type() {
            return "CONJUNCTION";
        }
    }, DT {
        @Override
        public String type() {
            return "DETERMINER";
        }
    }, CD {
        @Override
        public String type() {
            return "NUMERAL";
        }
    }, PRP$ {
        @Override
        public String type() {
            return "PRONOUN";
        }
    }, PDT {
        @Override
        public String type() {
            return "PRONOUN";
        }
    },POS {
        @Override
        public String type() {
            return "POSITIVENDING";
        }
    },PRP {
        @Override
        public String type() {
            return "PRONOUN";
        }
    },SYM{
        @Override
        public String type() {
            return "SYMBOL";
        }
    }, WP {
        @Override
        public String type() {
            return "PRONOUN";
        }
    }, WP$ {
        @Override
        public String type() {
            return "PRONOUN";
        }
    },UH {
        @Override
        public String type() {
            return "INTERJECTION";
        }
    },
    FW {
        @Override
        public String type() {
            return "FOREIGN";
        }
    }, WRB {
        @Override
        public String type() {
            return "ADVERB";
        }
    }, WDT {
        @Override
        public String type() {
            return "DETERMINER";
        }
    }, VBZ {
        @Override
        public String type() {
            return "VERB";
        }
    }, MD {
        @Override
        public String type() {
            return "MODAL";
        }
    }, JJR {
        @Override
        public String type() {
            return "ADJECTIVE";
        }

    }, JJS {
        @Override
        public String type() {
            return "ADJECTIVE";
        }
    }, RBR {
        @Override
        public String type() {
            return "ADVERB";
        }
    }, RBS {
        @Override
        public String type() {
            return "ADVERB";
        }
    }, LS {
        @Override
        public String type() {
            return "VERB";
        }
    }, EX {
        @Override
        public String type() {
            return "PRONOUN";
        }
    },UNKNOWN {
        @Override
        public String type() {
            return "UNKNOWN";
        }
    };

    public static WordType getType(char name) {
        if(Character.isAlphabetic(name) && !Character.isDigit(name)){
        switch (name) {
            case 'v':
                return VB;
            case 'n':
                return NN;
            case 'a':
                return JJ;
            case 'r':
                return RB;
            case 's':
                return JJ;
            default:
                return UNKNOWN;
        }
        }else{
            return UNKNOWN;
        }
    }

    public static WordType getType4Letter(char name) {
        return getType(name);
    }

    public static WordType getType(String name){
        for(WordType t : WordType.values()){
            if(t.type().equals(name) || t.name().equals(name)){
                return  t;
            }
        }
        return WordType.UNKNOWN;
    }

    public abstract String type();


}


//        1. CC Coordinating conjunction 25. TO to
//        2. CD Cardinal number 26. UH Interjection
//        3. DT Determiner 27. VB Verb, base form
//        4. EX Existential there 28. VBD Verb, past tense
//        5. FW Foreign word 29. VBG Verb, gerund/present
//        6. IN Preposition/subordinating participle
//        conjunction 30. VBN Verb, past participle
//        7. JJ Adjective 31. VBP Verb, non-3rd ps. sing. present
//        8. JJR Adjective, comparative 32. VBZ Verb, 3rd ps. sing. present
//        9. JJS Adjective, superlative 33. WDT wh-determiner
//        10. LS List item marker 34. WP wh-pronoun
//        11. MD Modal 35. WP$ Possessive wh-pronoun
//        12. NN Noun, singular or mass 36. WRB wh-adverb
//        13. NNS Noun, plural 37. # Pound sign
//        14. NNP Proper noun, singular 38. $ Dollar sign
//        15. NNPS Proper noun, plural 39.. Sentence-final punctuation
//        16. PDT Predeterminer 40. , Comma
//        17. POS Possessive ending 41. : Colon, semi-colon
//        18. PRP Personal pronoun 42. ( Left bracket character
//        19. PP$ Possessive pronoun 43. ) Right bracket character
//        20. RB Adverb 44. " Straight double quote
//        21. RBR Adverb, comparative 45. ' Left open single quote
//        22. RBS Adverb, superlative 46. " Left open double quote
//        23. RP Particle 47. ' Right close single quote
//        24. SYM Symbol (mathematical or scientific) 48. " Right close double quote
