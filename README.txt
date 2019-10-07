This is the readme for the setup of the Decomposition Framework

Install Pellet Reasoner
Clone: https://github.com/stardog-union/pellet.git
Install pellet: mvn clean install



The next steps are only to be done if no settings.xml has been provided.




For some experiments we need to installing Word2Vec. Do not do this until you realy need to.

3. Download source of nd4j:             git clone https://github.com/deeplearning4j/nd4j
4. Download source of Canova:           git clone https://github.com/deeplearning4j/Canova
5. Download source of deeplearning4j:    git clone https://github.com/deeplearning4j/deeplearning4j

6. Install Canova: mvn clean install
7. Install deeplearning4j: mvn clean install
8. Install nd4j: mvn clean install

9. Download source of OpenBLAS: https://github.com/xianyi/OpenBLAS/
10. Install OpenBLAS:
        MAC: brew install openblas
11. Download source of JWKTL                 git clone https://github.com/dkpro/dkpro-jwktl.git
12. Install JWKTL: mvn clean install

13. Adapt all versions of the above libraries in the appropriate pom.xml. 



//////////////////////////
Trouble shooting:
/////////////////////////

Wikipedia DB not set up right? (aka some Parser exceptions during the parsing of Wikipedia)
    Run the initializer from de.kimanufaktur.nsm.decomposition.dictionaries.wiktionary.WiktionaryCrawler's main method.
