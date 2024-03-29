This is the readme for the setup of the Decomposition Framework

This project implements a semantic decomposition. The basic idea is to automatically create a semantic graph from a given word. For the creation the algorithms uses a set of dictionaries like WordNet and Wikipedia.

There is a set of example implementations in the repository: https://github.com/Datenverlust/SemanticDecompositionExperiments.git

The decomposition is thought to provide semantic graphs for the implementation of artificial intelligence in the domain of natural language understanding.

To get the project running you will need:

    - Java 8
    - Maven
    - A machine with at least 8GB of RAM 16GB are recommended.

The algorithm itself does not need much resources. Depending on the used dictionaries, and the used parameters like decomposition depth, the algorithm will need resources. E.g. the downloead of Wikidata without pictures ia a 13GB JSON file. Extracted this is a ca. 250GB file, which needs to be parsed and saved in a DB.



Installing the semantic Decomposition and the needed references

1. Download source of the Marper Passing algorithm :  git clone https://github.com/Datenverlust/MarkerPassingAlgorithm
2. Install Marker Passing: mvn clean install
3. Download source of JWKTL                 git clone https://github.com/dkpro/dkpro-jwktl.git
4. Install JWKTL: mvn clean install
5. Adapt all versions of the above libraries in the appropriate pom.xml.

Please keep in mind that the first setup and running the test and example projects might take a while, since WordNet, Wikipedia and Wikidata have to be downloaded and converted into databases.

//////////////////////////
Trouble shooting:
/////////////////////////

Wikipedia DB not set up right? (aka some Parser exceptions during the parsing of Wikipedia)
    Run the initializer from WiktionaryCrawler's main method.

If you encounter any problems please contact datenverlust@gmail.com