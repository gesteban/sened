package es.unizar.sened2;

/**
 * Clase que representa una propiedad o una clase junto a una puntuación. Esta puntuación representa la posibilidad de
 * que la palabra clave dada para generar esta instancia se refiera efectivamente a la entidad que se indica
 * 
 * @author gesteban
 *
 */
public class RankedEntity {

  String keywordUsed;
  String classOrProperty; // URI
  Float score;

}
