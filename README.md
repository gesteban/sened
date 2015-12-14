# sened v2
Sened is a service to perform keyword searches over the Semantic Web.

## Requirements

### Keyword Search Requirements
- Use quantities in search field easily

### Competency Questions
Keywords used for a search that returns relevant results.

- CQ1: donald trump
- CQ2: actor k-pax american beauty
- CQ3: building height>500m

## Design concepts to take into account
- Know if two or more keywords are related with the same entity , making it (and its related entities) more relevant (CQ1 and CQ2).
- Some keywords may be related to a specific class or property, thus we have to search labels and descriptions of classes and properties to ensure if that keywords is pointing any of them. Maybe we could perform a search of that keywords over classes/properties (its labels, properties and synonyms), rank them and decide if the keyword meaning is that class/property using a ranking. e.g. keyword "actor" should be related to the class <http://umbel.org/umbel/rc/Actor>, thus we don't have to search that keyword in the abstract fields.
- We probably may have to find the keywords also in the related resources (not sure if this makes sense).
- Maybe use the questions of the QALD-4? (http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/index.php?x=task3&q=4).
- Each keyword must not be used individually in a search, use other keywords as context (as in QueryGen from C. Bobed et al).


