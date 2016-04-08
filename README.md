# sened v2
sened is a service to perform keyword and related searches over the semantic web

## TODO
- [X] (2015) basic keyword and related search (jena framework)
- [ ] sorting
  - [X] (february 2016) basic sorting of properties and resources
  - [ ] improve resource rank (use # of inbound properties as importance measure)
  - [ ] improve property rank (object resource importance influences property rank)
  - [ ] lucene stores keywords searchable fields to calculate session specific keyword search ranking (session (1) -> (n) resource (1) -> (n) kwdSearchableField)
- [X] (march 2016) depth search > 1 only retrieves resources from outbound properties
- [ ] semantic distance (Passant and improvements)
  - these distances does not takes domain ontology into account
  - [X] (february 2016) d (direct)
  - [ ] dw (direct weighted)
  - [ ] i (indirect)
  - [ ] iw (indirect weighted)
  - [ ] c (combined)
  - [ ] cw (combined weighted)
  - [ ] r (refined, domain ontology defined properties are more relevant)
  - [ ] rw (refined weighted)
- [ ] depth in rankings as variable

## competency questions
- CQ1: donald trump
- CQ2: actor k-pax american beauty
- CQ3: 2016 oscar special effects

## to take into account
- some keywords may be related to a specific class or property, thus we have to search labels and descriptions of classes and properties to ensure if that keywords is pointing any of them; maybe we could perform a search of that keywords over classes/properties (its labels, properties and synonyms), rank them and decide if the keyword meaning is that class/property using a ranking; e.g. keyword "actor" should be related to the class <http://umbel.org/umbel/rc/Actor>, thus we don't have to search that keyword in the abstract fields
- maybe use the questions of the QALD-4? (http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/index.php?x=task3&q=4)
- each keyword must not be used individually in a search, use other keywords as context (as in QueryGen from C. Bobed et al)

## future work
- use comparators in keyword searches (e.g. "building height>500m")