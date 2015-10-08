package es.unizar.sened.ontology;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.MoreObjects;

import es.unizar.sened.query.JQuery;
import es.unizar.sened.query.JQueryFactory;
import es.unizar.sened.query.JResult;
import es.unizar.sened.utils.Log;

/**
 * Class representing a DBpedia category, organized using SKOS model.
 * 
 * @author gesteban@unizar.es
 */
public class SKOSCategory extends Resource {

	public static final String TAG = SKOSCategory.class.getSimpleName();
	public static final String PREFIX = "http://dbpedia.org/resource/Category:";

	// TODO if deprecated, remove
	@Deprecated
	private String label;

	private Set<SKOSCategory> subCategories;

	public SKOSCategory(String URI) {
		super(URI);
		this.label = URI.replace("http://dbpedia.org/resource/Category:", "");
		subCategories = new HashSet<SKOSCategory>();
	}

	public Set<SKOSCategory> getSubCategories() {
		return subCategories;
	}

	@Deprecated
	public String getLabel() {
		return label;
	}

	@Override
	public String getName() {
		return getURI().toString().replace(PREFIX, "");
	}

	public void addSubCategory(SKOSCategory cat) {
		if (!subCategories.contains(cat))
			subCategories.add(cat);
	}

	static public Set<SKOSCategory> visited = new HashSet<SKOSCategory>();

	/**
	 * Método para explorar a partir de la DBpedia todas las subcategorias de ésta categoría recursivamente hasta un
	 * nivel de profundidad dado.
	 * 
	 * @param deep
	 *            Profundidad de la búsqueda.<br>
	 *            Se recomienda que éste valor no supere los 6 grados.
	 * @param visited
	 *            Conjunto de categorías ya visitadas.
	 *            <p>
	 *            Este parámetro se usa para no repetir búsquedas ya hechas sobre la DBPedia, sin embargo, nótese que
	 *            puede usarse como filtro para determinadas categorías, si se añade la categoría [Black_holes] por
	 *            ejemplo, al encontrar tal categoría el buscador pensará que ya está incluída en los resultados, por lo
	 *            que tal acción funcionará como filtro.
	 * @param qf
	 *            JQueryFactory que se usará para construir las JQuery que realizarán la exploración
	 */
	// TODO move this method out of here (no searching in this class) || remove it
	public void explore(int deep, JQueryFactory qf, Set<String> uriStringFilter) {
		if (deep != 0) {
			if (visited.contains(this))
				Log.d(TAG, "<:exploreImproved> Categoría [" + this.getLabel() + "] repetida");
			else {
				// Consultamos a la DBPedia y creamos una lista de categorias
				Log.d(TAG, "<exploreImproved> Buscando subcategorias de [" + this.getLabel() + "]");
				JQuery query = qf.getSubCategoryQuery(uriString);
				JResult res = query.doQuery();
				Set<SKOSCategory> subCategorySet = res.asCategorySet();
				// Marcamos la categoría como visitada
				visited.add(this);

				// Las añadimos a la categoría actual como subcategorías de ésta
				// siempre y cuando no esten presentes en alguno de los filtros
				for (SKOSCategory newCategory : subCategorySet) {
					if (!visited.contains(newCategory)) {
						boolean addNewCategory = true;
						for (String filterString : uriStringFilter) {
							if (newCategory.getURI().toLowerCase().contains(filterString.toLowerCase())) {
								addNewCategory = false;
								break;
							}
						}
						if (addNewCategory) {
							this.subCategories.add(newCategory);
						}
					}
				}
				// this.subCategories.addAll(subCategorySet);
				// Ahora exploramos con 1 nivel de profundidad menos todas las subcategorías encontradas
				for (SKOSCategory subCat : subCategories)
					subCat.explore(deep - 1, qf, uriStringFilter);
			}
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(SKOSCategory.class).add("URI", this.uriString).toString();
	}

}
