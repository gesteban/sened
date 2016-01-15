# sened v2
sened is a service to perform keyword and related searches over the semantic web

## TODO
- [ ] basic keyword and related search using only jena framework
- [ ] sort results

## future work
- [ ] F1: use quantities in search field easily
  - [ ] F1-CQ1: donald trump
  - [ ] F1-CQ2: actor k-pax american beauty
  - [ ] F1-CQ3: building height>500m

## to take into account
- know if two or more keywords are related with the same entity , making it (and its related entities) more relevant (CQ1 and CQ2)
- some keywords may be related to a specific class or property, thus we have to search labels and descriptions of classes and properties to ensure if that keywords is pointing any of them; maybe we could perform a search of that keywords over classes/properties (its labels, properties and synonyms), rank them and decide if the keyword meaning is that class/property using a ranking; e.g. keyword "actor" should be related to the class <http://umbel.org/umbel/rc/Actor>, thus we don't have to search that keyword in the abstract fields
- we probably may have to find the keywords also in the related resources (not sure if this makes sense)
- maybe use the questions of the QALD-4? (http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/index.php?x=task3&q=4)
- each keyword must not be used individually in a search, use other keywords as context (as in QueryGen from C. Bobed et al)