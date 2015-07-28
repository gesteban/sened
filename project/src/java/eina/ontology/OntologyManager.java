package eina.ontology;

import eina.utils.Log;
import java.util.HashSet;
import java.util.Set;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 *
 * @author Guillermo Esteban
 */
public class OntologyManager {
    
    public static final String ONTOLOGY_FILE = "DomainOntology.owl";
    
    public static final String TAXONOMY_CLASSES = "classes";
    public static final String TAXONOMY_CATEGORIES = "categories";

    private static final String IRI_QUERYLANGUAGE_ANNOTATION_FRAGMENT = "queryLanguage";
    private static final String IRI_KWDSEARCHABLE_PROPERTY_FRAGMENT = "kwdSearchable";
    private static final String IRI_DATARETREAVABLE_PROPERTY_FRAGMENT = "dataRetreavable";
    
    private OWLOntology ont;
    private OWLOntologyManager man;
    private OWLReasoner owlReasoner;
    private String taxonomyType;

    private OntologyManager() {
        man = OWLManager.createOWLOntologyManager();
        try {
            ont = man.loadOntology(IRI.create(getClass().getResource("/eina/ontology/"+ONTOLOGY_FILE)));
            for(OWLAnnotation anon : ont.getAnnotations()) {
                if(anon.getProperty().getIRI().getFragment().equals("taxonomyDefinedBy")); {
                    taxonomyType = anon.getValue().toString().replace("\"", "");
                }
            }
            owlReasoner = new ReasonerFactory().createReasoner(ont);
        } catch (Exception ex) {
            Log.getLog().error("<OntologyManager> Fallo al cargar el fichero");
            taxonomyType = "";
        }
    }
    
    private static class SingletonHolder {
        public static final OntologyManager instance = new OntologyManager();
    }

    public static OntologyManager getInstance() {
        return SingletonHolder.instance;
    }

    /////////////
    /////////////
    
    public static void main (String[] args) {
        OntologyManager om = OntologyManager.getInstance();
        System.out.println(om.getIRI().toString()+"\n");
        
        System.out.println(om.getExternalDataRetriavablePropertyFragment());
    }
    
    public IRI getIRI() {
        return getOWLOntology().getOntologyID().getOntologyIRI();
    }

    public String getTaxonomyType () {
        return taxonomyType;
    }
    
    public OWLOntology getOWLOntology() {
        return ont;
    }

    public OWLDataFactory getOWLDataFactory() {
        return man.getOWLDataFactory();
    }

    public Set<OWLDataProperty> getDataPropertiesByDomain(OWLClass domain) {
        Set<OWLDataProperty> propSet = new HashSet<OWLDataProperty>();
        Set<OWLDataProperty> objProps = ont.getDataPropertiesInSignature();
        for (OWLDataProperty prop : objProps) {
            for (OWLClass owlClass : owlReasoner.getDataPropertyDomains(prop, true).getFlattened()) {
                if (owlClass.equals(domain)) {
                    propSet.add(prop);
                }
            }
        }
        return propSet;
    }
    
    public Set<OWLDataProperty> getDataPropertiesByDomainExtended(OWLClass domain) {
        Log.getLog().debug("<OntologyManager:getDataPropertiesByDomainExtended> Buscando DataProperties con dominio "+domain.getIRI().toString());
        Set<OWLDataProperty> propSet = new HashSet<OWLDataProperty>();
        for (OWLDataProperty prop : ont.getDataPropertiesInSignature()) {
            if(isDomainOfDataProperty(domain, prop)) {
                propSet.add(prop);
            }
        }
        return propSet;
    }
    
    public boolean isDomainOfDataProperty(OWLClass clase, OWLDataProperty prop) {
        
        for(OWLClass owlClass1 : owlReasoner.getDataPropertyDomains(prop, false).getFlattened()) {
            if(owlClass1.getIRI().equals(clase.getIRI())) {
                Log.getLog().debug("<OntologyManager:idDomainOfDataProperty> "+clase.getIRI().getFragment() + " dominio de " + prop.getIRI().getFragment() + "?     SI!");
                return true;
//            } else {
//                for(OWLClass owlClass2 : owlReasoner.getSuperClasses(clase, false).getFlattened()) {
//                    if(owlClass2.getIRI().equals(owlClass1.getIRI())) {
//                        Log.getLog().debug("<OntologyManager:idDomainOfDataProperty> "+clase.getIRI().getFragment() + " dominio de " + prop.getIRI().getFragment() + "?     SI!");
//                        return true;
//                    }
//                }
            }
        }
        Log.getLog().debug("<OntologyManager:idDomainOfDataProperty> "+clase.getIRI().getFragment() + " dominio de " + prop.getIRI().getFragment() + "? NO");
        return false;
    }
    
    public boolean isDomainOrRangeOfObjectProperty (OWLClass clase, OWLObjectProperty prop) {
        
        for(OWLClass owlClass1 : owlReasoner.getObjectPropertyDomains(prop, false).getFlattened()) {
            if(owlClass1.getIRI().equals(clase.getIRI())) {
                Log.getLog().debug("<OntologyManager:isDomainOrRangeOfObjectProperty>1 "+clase.getIRI().getFragment() + " dominio o rango de " + prop.getIRI().getFragment() + "?      SI!");
                return true;
//            } else {
//                for(OWLClass owlClass2 : owlReasoner.getSuperClasses(clase, false).getFlattened()) {
//                    if(owlClass2.getIRI().equals(owlClass1.getIRI())) {
//                        Log.getLog().debug("<OntologyManager:isDomainOrRangeOfObjectProperty>2 "+clase.getIRI().getFragment() + " dominio o rango de " + prop.getIRI().getFragment() + "?      SI!");
//                        return true;
//                    }
//                }
            }
        }
        for(OWLClass owlClass1 : owlReasoner.getObjectPropertyRanges(prop, false).getFlattened()) {
            if(owlClass1.getIRI().equals(clase.getIRI())) {
                Log.getLog().debug("<OntologyManager:isDomainOrRangeOfObjectProperty>3 "+clase.getIRI().getFragment() + " dominio o rango de " + prop.getIRI().getFragment() + "?      SI!");
                return true;
//            } else {
//                for(OWLClass owlClass2 : owlReasoner.getSuperClasses(clase, false).getFlattened()) {
//                    if(owlClass2.getIRI().equals(owlClass1.getIRI())) {
//                        Log.getLog().debug("<OntologyManager:isDomainOrRangeOfObjectProperty>4 "+clase.getIRI().getFragment() + " dominio o rango de " + prop.getIRI().getFragment() + "?      SI!");
//                        return true;
//                    }
//                }
            }
        }
        Log.getLog().debug("<OntologyManager:isDomainOrRangeOfObjectProperty> "+clase.getIRI().getFragment() + " dominio o rango de " + prop.getIRI().getFragment() + "? NO");
        return false;
    }

    public Set<OWLObjectProperty> getObjectPropertiesByDomain(OWLClass domain) {
        Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
        Set<OWLObjectProperty> objProps = ont.getObjectPropertiesInSignature();
        for (OWLObjectProperty prop : objProps) {
            for (OWLClass owlClass : owlReasoner.getObjectPropertyDomains(prop, true).getFlattened()) {
                if (owlClass.equals(domain)) {
                    propSet.add(prop);
                }
            }
        }
        return propSet;
    }

    public Set<OWLObjectProperty> getObjectPropertiesByRange(OWLClass domain) {
        Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
        Set<OWLObjectProperty> objProps = ont.getObjectPropertiesInSignature();
        for (OWLObjectProperty prop : objProps) {
            for (OWLClass owlClass : owlReasoner.getObjectPropertyRanges(prop, true).getFlattened()) {
                if (owlClass.equals(domain)) {
                    propSet.add(prop);
                }
            }
        }
        return propSet;
    }
    
    public Set<OWLObjectProperty> getObjectPropertiesByDomainOrRange(OWLClass domainOrRange) {
        Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
        Set<OWLObjectProperty> objProps = ont.getObjectPropertiesInSignature();
        for (OWLObjectProperty prop : objProps) {
            for (OWLClass owlClass : owlReasoner.getObjectPropertyRanges(prop, true).getFlattened()) {
                if (owlClass.equals(domainOrRange)) {
                    propSet.add(prop);
                }
            }
            for (OWLClass owlClass : owlReasoner.getObjectPropertyDomains(prop, true).getFlattened()) {
                if (owlClass.equals(domainOrRange)) {
                    propSet.add(prop);
                }
            }
        }
        return propSet;
    }
    
    public Set<OWLObjectProperty> getObjectPropertiesByDomainOrRangeExtended(OWLClass domainOrRange) {
        Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
        for (OWLObjectProperty prop : ont.getObjectPropertiesInSignature()) {
            if(isDomainOrRangeOfObjectProperty(domainOrRange, prop)) {
                propSet.add(prop);
            }
        }
        return propSet;
    }

    public Set<OWLObjectProperty> getObjectPropertiesByDomainAndRange(OWLClass domain, OWLClass range) {
        Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
        for (OWLObjectProperty prop : ont.getObjectPropertiesInSignature()) {
            for (OWLClass owlClass : owlReasoner.getObjectPropertyDomains(prop, true).getFlattened()) {
                if (owlClass.equals(domain)) {
                    for (OWLClass owlClass2 : owlReasoner.getObjectPropertyRanges(prop, true).getFlattened()) {
                        if (owlClass2.equals(range)) {
                            propSet.add(prop);
                        }
                    }
                }
            }
        }
        return propSet;
    }
    
    public Set<OWLClass> getClasses() {
        Set<OWLClass> classSet = new HashSet<OWLClass>();
        for (OWLClass clase : ont.getClassesInSignature()) {
            classSet.add(clase);
        }
        return classSet;
    }

    public Set<OWLClass> getClassEquivalents(OWLClass clase) {
        Set<OWLClass> classSet = new HashSet<OWLClass>();
        for (OWLClassExpression ce : clase.getEquivalentClasses(ont)) {
            classSet.add(ce.asOWLClass());
        }
        return classSet;
    }

    public Set<OWLObjectProperty> getObjectPropertyEquivalents(OWLObjectProperty prop) {
        Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
        for (OWLObjectPropertyExpression ope : prop.getEquivalentProperties(ont)) {
            propSet.add(ope.asOWLObjectProperty());
        }
        return propSet;
    }

    public Set<OWLClass> getObjectPropertyRange(Set<OWLObjectProperty> propSet) {
        Set<OWLClass> classSet = new HashSet<OWLClass>();
        for (OWLObjectProperty prop : propSet) {
            for (OWLClass clase : owlReasoner.getObjectPropertyRanges(prop, true).getFlattened()) {
                classSet.add(clase);
            }
        }
        return classSet;
    }

    public Set<OWLClass> getObjectPropertyDomain(Set<OWLObjectProperty> propSet) {
        Set<OWLClass> classSet = new HashSet<OWLClass>();
        for (OWLObjectProperty prop : propSet) {
            for (OWLClass clase : owlReasoner.getObjectPropertyDomains(prop, true).getFlattened()) {
                classSet.add(clase);
            }
        }
        return classSet;
    }
    
    public Set<OWLClass> getObjectPropertyDomain(OWLObjectProperty prop) {
        Set<OWLClass> classSet = new HashSet<OWLClass>();
        for (OWLClass clase : owlReasoner.getObjectPropertyDomains(prop, true).getFlattened()) {
            classSet.add(clase);
        }
        return classSet;
    }
    
    public Set<OWLClass> getObjectPropertyRange(OWLObjectProperty prop) {
        Set<OWLClass> classSet = new HashSet<OWLClass>();
        for (OWLClass clase : owlReasoner.getObjectPropertyRanges(prop, true).getFlattened()) {
            classSet.add(clase);
        }
        return classSet;
    }

    public Set<OWLEntity> getSubsetInternal(Set<? extends OWLEntity> entitySet) {
        Set<OWLEntity> outSet = new HashSet<OWLEntity>();
        for (OWLEntity ent : entitySet) {
            if (isInternal(ent)) {
                outSet.add(ent);
            }
        }
        return outSet;
    }
    
    public <T extends OWLEntity> Set<T> getSubsetInternal(Class<T> clase, Set<T> entitySet) {
        Set<T> outSet = new HashSet<T>();
        for (T ent : entitySet) {
            if (isInternal(ent)) {
                outSet.add(ent);
            }
        }
        return outSet;
    }

    public Set<OWLEntity> getSubsetExternal(Set<? extends OWLEntity> entitySet) {
        Set<OWLEntity> outSet = new HashSet<OWLEntity>();
        for (OWLEntity ent : entitySet) {
            if (!isInternal(ent)) {
                outSet.add(ent);
            }
        }
        return outSet;
    }
    
    public <T extends OWLEntity> Set<T> getSubsetExternal(Class<T> clase, Set<T> entitySet) {
        Set<T> outSet = new HashSet<T>();
        for (T ent : entitySet) {
            if (!isInternal(ent)) {
                outSet.add(ent);
            }
        }
        return outSet;
    }

    protected boolean isInternal(OWLEntity ent) {
        return ent.getIRI().toURI().getPath().equals(ont.getOntologyID().getOntologyIRI().toURI().getPath());
    }

    public String getAnnotationQueryLanguage(OWLDataProperty dataProp) {
        for (OWLDataProperty dp : owlReasoner.getEquivalentDataProperties(dataProp)) {
            for (OWLAnnotation a : dp.getAnnotations(ont)) {
                if (a.getProperty().getIRI().getFragment().toString().equals(IRI_QUERYLANGUAGE_ANNOTATION_FRAGMENT)) {
                    return ((OWLLiteral) a.getValue()).getLiteral();
                }
            }
        }
        return null;
    }

    public Set<OWLObjectProperty> getObjectPropertyEquivalents2(OWLObjectProperty prop) {
        Set<OWLObjectProperty> propSet = new HashSet<OWLObjectProperty>();
        for (OWLObjectPropertyExpression ope : prop.getEquivalentProperties(ont)) {
            propSet.add(ope.asOWLObjectProperty());
        }
        return propSet;
    }
    
    public boolean isKeywordSearchable (OWLProperty prop) {
        
        if(prop instanceof OWLObjectProperty) {
            for (OWLObjectPropertyExpression ope : ((OWLObjectProperty)prop).getEquivalentProperties(ont)) {
                for (OWLAnnotation a : ope.asOWLObjectProperty().getAnnotations(ont)) {
                    if (a.getProperty().getIRI().getFragment().toString().equals(IRI_KWDSEARCHABLE_PROPERTY_FRAGMENT)) {
                        return true;
                    }
                }
            }
        } else if (prop instanceof OWLDataProperty) {
            for (OWLDataPropertyExpression ope : ((OWLDataProperty)prop).getEquivalentProperties(ont)) {
                for (OWLAnnotation a : ope.asOWLDataProperty().getAnnotations(ont)) {
                    if (a.getProperty().getIRI().getFragment().toString().equals(IRI_KWDSEARCHABLE_PROPERTY_FRAGMENT)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isDataRetreavable (OWLProperty prop) {
        
        if(prop instanceof OWLObjectProperty) {
            for (OWLObjectPropertyExpression ope : ((OWLObjectProperty)prop).getEquivalentProperties(ont)) {
                for (OWLAnnotation a : ope.asOWLObjectProperty().getAnnotations(ont)) {
                    if (a.getProperty().getIRI().getFragment().toString().equals(IRI_DATARETREAVABLE_PROPERTY_FRAGMENT)) {
                        return true;
                    }
                }
            }
        } else if (prop instanceof OWLDataProperty) {
            for (OWLDataPropertyExpression ope : ((OWLDataProperty)prop).getEquivalentProperties(ont)) {
                for (OWLAnnotation a : ope.asOWLDataProperty().getAnnotations(ont)) {
                    if (a.getProperty().getIRI().getFragment().toString().equals(IRI_DATARETREAVABLE_PROPERTY_FRAGMENT)) {
                        return true;
                    }
                }
            }
        } else {
            System.out.println("error");
        }
        return false;
    }
    
    public String getExternalDataRetriavablePropertyFragment () {
        for(OWLDataProperty prop1 : ont.getDataPropertiesInSignature()) {
            if(isKeywordSearchable(prop1)) {
                for(OWLDataProperty prop2 : owlReasoner.getEquivalentDataProperties(prop1).getEntities()) {
                    if(!isInternal(prop2)) {
                        return prop2.getIRI().getFragment();
                    }
                }
            }
        }
        return null;
    }
    
    public <T extends OWLEntity> Set<T> getRetreavableProperties(Class<T> clase, Set<T> entitySet) {
        Set<T> outSet = new HashSet<T>();
        for (T ent : entitySet) {
            if (isDataRetreavable((OWLProperty)ent)) {
                outSet.add(ent);
            }
        }
        return outSet;
        
    }
}
