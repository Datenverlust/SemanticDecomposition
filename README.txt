This is the readme for the setup of the Decomposition Framework





The next steps are only to be done if no settings.xml has been provided.


Installing Word2Vec

1. Download source of the Marper Passing algorithm :  git clone https://github.com/Datenverlust/MarkerPassingAlgorithm
2. Install Marker Passing: mvn clean install
3. Download source of JWKTL                 git clone https://github.com/dkpro/dkpro-jwktl.git
4. Install JWKTL: mvn clean install
5. Adapt all versions of the above libraries in the appropriate pom.xml.

Please keep in mind that the first setup and running the test and example projects might take a wile, since WordNet, Wikipedia and Wikidata have to be downloaded and converted into databases.

//////////////////////////
Trouble shooting:
/////////////////////////

Wikipedia DB not set up right? (aka some Parser exceptions during the parsing of Wikipedia)
    Run the initializer from de.dailab.nsm.decomposition.dictionaries.wiktionary.WiktionaryCrawler's main method.
