/*
 * Copyright (C) Johannes Fähndrich - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly
 * prohibited Proprietary and confidential.
 * Written by Johannes Fähndrich <faehndrich@gmail.com.com>,  2011
 */

package de.kimanufaktur.nsm.decomposition;

import de.kimanufaktur.nsm.decomposition.Concept;
import de.kimanufaktur.nsm.decomposition.WordType;

class ExampleDecomposition {

  private Concept adjacent = null;

  public Concept getAdjacent() {
    if(adjacent==null){
      fillAdjasentExample();
    }
    return adjacent;
  }

  public void setAdjacent(Concept adjacent) {
    this.adjacent = adjacent;
  }





  private void fillAdjasentExample(){
    adjacent = new Concept("adjacent");
    adjacent.setWordType(WordType.JJ);
    //Add synonyms
    Concept sidebyside = new Concept("side by side");
    adjacent.getSynonyms().add(sidebyside);
    Concept next = new Concept("next");
    adjacent.getSynonyms().add(next);
    Concept near = new Concept("near");



  }

  }