/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eina.ontology.propertypath;

import eina.jena.JQueryFactory;
import eina.ontology.OntologyManager;
import eina.utils.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.semanticweb.owlapi.model.*;

/**
 * Camino de propiedades <br>
 * Destinado a crear consultas específicas para búsquedas de refinado 
 * (léase {@link JQueryFactory#getSpecificQuery(String, PropertyPath)}) <br>
 * Su funcionamiento es el siguiente: <br>
 * El {@link OntologyManager} contiene dos (en términos generales) ontologías,
 * una interna (con el namespace igual a la ruta del fichero) y una externa, 
 * que es un subconjunto de las ontologías usadas en la DBPedia. <br>
 * Cuando se llama al constructor de PropertyPath hay que darle 2 {@link OWLClass}
 * pertenecientes a la ontología "interna", entonces se nos creará un camino de
 * propiedades "externas" que enlace las dos clases elegidas. <br>
 * Se podría decir que éste objeto es el paso intermedio entre las ontologías
 * "interna" y "externa" y el que se ocupa de ocultar la ontología "interna" 
 * para no tener que preocuparnos por ella. <br>
 * Nota: "interna" y "externa" van entre comillas ya que realmente ambas están 
 * presentes en el fichero local a cargar, sin embargo la "externa" es la que
 * contiene las mismas clases y propiedades que la DBPedia.
 * @author Guillermo Esteban
 */
public class PropertyPath extends ArrayList<DirectionalProperty> {
    
    static final OntologyManager om = OntologyManager.getInstance();
    
    private OWLClass source;
    private OWLClass target;
    
    private PropertyPath (OWLClass source, OWLClass target, DirectionalProperty dp) {
        this.source = source;
        this.target = target;
        this.add(dp);
    }
    
    /**
     * Crea un camino de propiedades externas a partir de dos clases
     * @param source
     * @param target
     * @return 
     */
    static public Set<PropertyPath> createSimplePaths (OWLClass source, OWLClass target) {
        Set<PropertyPath> pathSet = new HashSet<PropertyPath>();
        
        for( OWLObjectProperty prop : om.getObjectPropertiesByDomainAndRange(source, target)) {
            for ( OWLEntity ent : om.getSubsetExternal(om.getObjectPropertyEquivalents(prop)) ) {
                pathSet.add(new PropertyPath(source, target, new DirectionalProperty(ent.asOWLObjectProperty(), true)));
            }
        }
        
        for( OWLObjectProperty prop : om.getObjectPropertiesByDomainAndRange(target, source)) {
            for ( OWLEntity ent : om.getSubsetExternal(om.getObjectPropertyEquivalents(prop)) ) {
                pathSet.add(new PropertyPath(source, target, new DirectionalProperty(ent.asOWLObjectProperty(), false)));
            }
        }
        
        Log.getLog().debug("<PropertyPath:createSimplePaths> Set<PropertyPath> de "+source.getIRI().getFragment()
                +" a "+target.getIRI().getFragment()+" creado con "+pathSet.size()+" PropertyPath/s");
        
        return pathSet;
    }
    
    /**
     * Crea un camino de propiedades externas a partir de dos clases internas y la profundidad
     * @param source
     * @param target
     * @param deep
     * @return 
     */
    static public Set<PropertyPath> createComplexPaths (OWLClass source, OWLClass target, int deep) {
        if(deep==1)
            return createSimplePaths(source, target);
        Set<PropertyPath> salida = new HashSet<PropertyPath>();
        Set<PropertyPath> set1 = new HashSet<PropertyPath>();
        for(OWLObjectProperty prop : om.getSubsetExternal(OWLObjectProperty.class, om.getObjectPropertiesByDomain(source))) {
            if(!om.isDataRetreavable(prop))
            set1.add(new PropertyPath(source, target, new DirectionalProperty(prop, true)));
        }
        for(OWLObjectProperty prop : om.getSubsetExternal(OWLObjectProperty.class, om.getObjectPropertiesByRange(source))) {
            if(!om.isDataRetreavable(prop))
            set1.add(new PropertyPath(source, target, new DirectionalProperty(prop, false)));
        }
        System.out.println("SET1:");
        for(PropertyPath pp : set1) {
            System.out.println(pp.toString());
        }
        System.out.println("FIN DE SET1");
        Set<PropertyPath> nextPaths = new HashSet<PropertyPath>();
        for(int i=1; i<deep; i++) {
            for(PropertyPath pp : set1) {
                if(pp.isAccepted()) {
                    salida.add(pp);
                }
                nextPaths.addAll(pp.expandPath());
            }
            set1 = nextPaths;
            nextPaths = new HashSet<PropertyPath>();
        }
        for(PropertyPath pp : set1) {
            if(pp.isAccepted()) {
                salida.add(pp);
            }
        }
        return salida;
    }
    
    private Set<PropertyPath> expandPath () {
        Set<PropertyPath> salida = new HashSet<PropertyPath>();
        OWLObjectProperty prop = this.get(this.size()-1).property;
        Set<OWLClass> classSet;
        if(this.get(this.size()-1).straight) {
            classSet = om.getObjectPropertyRange(prop);
        } else {
            classSet = om.getObjectPropertyDomain(prop);
        }
        for(OWLClass clase : classSet) {
            for(OWLObjectProperty prop2 : om.getSubsetExternal(OWLObjectProperty.class, om.getObjectPropertiesByDomain(clase))) {
                if(!om.isDataRetreavable(prop2)) {
                    PropertyPath newPP = (PropertyPath) this.clone();
                    newPP.add(new DirectionalProperty(prop2, true));
                    salida.add(newPP);
                }
            }
            for(OWLObjectProperty prop2 : om.getSubsetExternal(OWLObjectProperty.class, om.getObjectPropertiesByRange(clase))) {
                if(!om.isDataRetreavable(prop2)) {
                    PropertyPath newPP = (PropertyPath) this.clone();
                    newPP.add(new DirectionalProperty(prop2, false));
                    salida.add(newPP);
                }
            }
        }
//        for(PropertyPath pp :salida) {
//            System.out.println("expandido "+pp.toString());
//        }
        return salida;
    }
    
    public OWLClass getSourceClass() {
        return source;
    }

    public OWLClass getTargetClass() {
        return target;
    }
    
    @Override
    public String toString () {
        String out = "De "+source.getIRI().getFragment() + " a " + target.getIRI().getFragment() + " \n";
        int i = 1;
        for(DirectionalProperty x : this) {
            out += i+") "+x.toString()+"\n";
            i++;
        }
        return out;
    }
    
    public boolean isAccepted () {
        Set<OWLClass> claseSet;
        if(this.get(this.size()-1).straight) {
            claseSet = om.getObjectPropertyRange(this.get(this.size()-1).property);
        } else {
            claseSet = om.getObjectPropertyDomain(this.get(this.size()-1).property);
        }
        for(OWLClass clase :  claseSet) {
            System.out.print(clase.getIRI().getFragment());
            System.out.print(" is ");
            System.out.print(this.target.getIRI().getFragment());
            System.out.println(" ?");
            if(this.target.getIRI().equals(clase.getIRI())) {
                System.out.println(this.toString()+" ACCEPTED\n");
                return true;
            }
        }
        System.out.println(this.toString()+" NOT ACCEPTED\n");
        return false;
    }
    
    public static void main (String[] args) {
        OWLDataFactory fac = om.getOWLDataFactory();
//        Set<PropertyPath> pps = PropertyPath.createComplexPaths(fac.getOWLClass(IRI.create(
//                "file:/c:/users/peonza/documents/proyecto/subsets_merged.owl#Persona")), 
//                fac.getOWLClass(IRI.create(
//                "file:/c:/users/peonza/documents/proyecto/subsets_merged.owl#Articulo")), 3);
//        Set<PropertyPath> pps = PropertyPath.createComplexPaths(fac.getOWLClass(IRI.create(
//                "file:/c:/users/peonza/documents/proyecto/subsets_merged.owl#Articulo")), 
//                fac.getOWLClass(IRI.create(
//                "file:/c:/users/peonza/documents/proyecto/subsets_merged.owl#Persona")), 2);
        Set<PropertyPath> pps = PropertyPath.createSimplePaths(fac.getOWLClass(IRI.create(
                "file:/c:/users/peonza/documents/proyecto/subsets_merged.owl#Articulo")), 
                fac.getOWLClass(IRI.create(
                "file:/c:/users/peonza/documents/proyecto/subsets_merged.owl#Categoria")));
        for(PropertyPath pp : pps) {
            System.out.println(pp.toString());
        }
    }
//    
}
