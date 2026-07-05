from enum import Enum


class WordType(Enum):
    JJ = "JJ"
    NN = "NN"
    NNP = "NNP"
    NNPS = "NNPS"
    NNS = "NNS"
    RB = "RB"
    RP = "RP"
    IN = "IN"
    VB = "VB"
    VBD = "VBD"
    VBN = "VBN"
    VBP = "VBP"
    TO = "TO"
    VBG = "VBG"
    CC = "CC"
    DT = "DT"
    CD = "CD"
    PRP = "PRP"
    PDT = "PDT"
    POS = "POS"
    PRPS = "PRP$"
    SYM = "SYM"
    WP = "WP"
    WPS = "WP$"
    UH = "UH"
    FW = "FW"
    WRB = "WRB"
    WDT = "WDT"
    VBZ = "VBZ"
    MD = "MD"
    JJR = "JJR"
    JJS = "JJS"
    RBR = "RBR"
    RBS = "RBS"
    LS = "LS"
    EX = "EX"
    UNKNOWN = "UNKNOWN"

    def type(self) -> str:
        noun_tags = {
            WordType.NN, WordType.NNS, WordType.NNP, WordType.NNPS,
        }
        verb_tags = {
            WordType.VB, WordType.VBD, WordType.VBG, WordType.VBN,
            WordType.VBP, WordType.VBZ, WordType.MD,
        }
        adj_tags = {WordType.JJ, WordType.JJR, WordType.JJS}
        adv_tags = {WordType.RB, WordType.RBR, WordType.RBS, WordType.WRB}

        if self in noun_tags:
            return "noun"
        if self in verb_tags:
            return "verb"
        if self in adj_tags:
            return "adjective"
        if self in adv_tags:
            return "adverb"
        return "other"
