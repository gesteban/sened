package es.unizar.sened.utils;

import es.unizar.sened.query.JResult;
import es.unizar.sened.ontology.Article;
import es.unizar.sened.ontology.SKOSCategory;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 * @author gesteban@unizar.es
 */
public class XMLutil {

	static public String categorySetToXml(Set<SKOSCategory> categoryList) {
		String out = "";

		try {

			// We need a Document
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// Creating the XML tree
			Element root = doc.createElement("root");
			doc.appendChild(root);
			for (SKOSCategory cat : categoryList) {
				System.out.println(cat);

				Element child = doc.createElement("elemento");
				root.appendChild(child);

				Element nodeTipo = doc.createElement("tipo");
				child.appendChild(nodeTipo);
				Text nodeTipoText = doc.createTextNode("Categoria");
				nodeTipo.appendChild(nodeTipoText);

				Element nodeURI = doc.createElement("uri");
				child.appendChild(nodeURI);
				Text nodeURIText = doc.createTextNode(cat.getURI());
				nodeURI.appendChild(nodeURIText);

				Element nodeLabel = doc.createElement("label");
				child.appendChild(nodeLabel);
				Text nodeLabelText = doc.createTextNode(cat.getLabel());
				nodeLabel.appendChild(nodeLabelText);

			}

			// Output the XML
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			out = sw.toString();

		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println(out);
		return out;
	}

	static public String articleListToXml(List<Article> articleList) {
		String out = "";

		try {

			// We need a Document
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// Creating the XML tree
			Element root = doc.createElement("root");
			doc.appendChild(root);
			for (int i = 0; i < articleList.size(); i++) {

				Element child = doc.createElement("elemento");
				root.appendChild(child);

				Element nodeTipo = doc.createElement("tipo");
				child.appendChild(nodeTipo);
				Text nodeTipoText = doc.createTextNode("Articulo");
				nodeTipo.appendChild(nodeTipoText);

				// Element nodeURI = doc.createElement("uri");
				// child.appendChild(nodeURI);
				// Text nodeURIText = doc.createTextNode(articleList.get(i).getURI());
				// nodeURI.appendChild(nodeURIText);

				for (String prop : articleList.get(i).keySet()) {
					for (String value : articleList.get(i).get(prop)) {
						Element nodeLabel = doc.createElement(prop);
						child.appendChild(nodeLabel);
						Text nodeText = doc.createTextNode(value);
						nodeLabel.appendChild(nodeText);
					}
				}

			}

			// Output the XML
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			out = sw.toString();

		} catch (Exception e) {
			System.out.println(e);
		}

		return out;
	}

	/**
	 * Transforma un {@link JResult} en un XML adecuado para su consumo a través del servicio web. A cada nodo/elemento
	 * devuelto se le añade una hoja <tipo> con el valor de la entrada "nombreClase".
	 * <p>
	 * Usar {@link #resultsSetsToXml(java.util.Map) } en vez de éste.
	 *
	 * @param nombreClase
	 *            Tipo del recurso que se transforma en XML
	 * @param rs
	 *            {@link JResult}
	 * @return String conteniendo el XML
	 * @deprecated
	 */
	static public String resultSetToXml(String nombreClase, JResult rs) {
		String out = "";

		try {

			// We need a Document
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// Creating the XML tree
			Element root = doc.createElement("root");
			doc.appendChild(root);
			for (int i = 0; i < rs.getResultSize(); i++) {
				Node n = getNode(doc, "uri", rs.getRow(i).get(0));
				if (n == null) {
					Element child = doc.createElement("elemento");
					root.appendChild(child);
					Element tipo = doc.createElement("tipo");
					child.appendChild(tipo);
					Text nombreTipo = doc.createTextNode(nombreClase);
					tipo.appendChild(nombreTipo);
					for (int j = 0; j < rs.getColumnCount(); j++) {
						Element property = doc.createElement(rs.getColumnName(j));
						child.appendChild(property);
						Text text = doc.createTextNode(rs.getRow(i).get(j));
						property.appendChild(text);
					}
				} else {
					Node parent = n.getParentNode();
					for (int j = 0; j < rs.getColumnCount(); j++) {
						if (!existNode(parent, rs.getColumnName(j), rs.getElement(i, j))) {
							Element property = doc.createElement(rs.getColumnName(j));
							parent.appendChild(property);
							Text text = doc.createTextNode(rs.getRow(i).get(j));
							property.appendChild(text);
						}
					}
				}
			}

			// Output the XML
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			out = sw.toString();

		} catch (Exception e) {
			System.out.println(e);
		}

		return out;
	}

	static public String resultsSetsToXml(Map<String, JResult> map) {
		String out = "";

		try {

			// We need a Document
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// Creating the XML tree
			Element root = doc.createElement("root");
			doc.appendChild(root);
			for (String nombreClase : map.keySet()) {
				JResult rs = map.get(nombreClase);
				for (int i = 0; i < rs.getResultSize(); i++) {
					Node n = getNode(doc, "uri", rs.getRow(i).get(0));
					if (n == null) {
						Element child = doc.createElement("elemento");
						root.appendChild(child);
						Element tipo = doc.createElement("tipo");
						child.appendChild(tipo);
						Text nombreTipo = doc.createTextNode(nombreClase);
						tipo.appendChild(nombreTipo);
						for (int j = 0; j < rs.getColumnCount(); j++) {
							Element property = doc.createElement(rs.getColumnName(j));
							child.appendChild(property);
							Text text = doc.createTextNode(rs.getRow(i).get(j));
							property.appendChild(text);
						}
					} else {
						Node parent = n.getParentNode();
						for (int j = 0; j < rs.getColumnCount(); j++) {
							if (!existNode(parent, rs.getColumnName(j), rs.getElement(i, j))) {
								Element property = doc.createElement(rs.getColumnName(j));
								parent.appendChild(property);
								Text text = doc.createTextNode(rs.getRow(i).get(j));
								property.appendChild(text);
							}
						}
					}
				}
			}

			// Output the XML
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			out = sw.toString();

		} catch (Exception e) {
			System.out.println(e);
		}

		return out;
	}

	static private Node getNode(Document doc, String nodeName, String textValue) {
		NodeList nodelist = doc.getElementsByTagName(nodeName);
		for (int i = 0; i < nodelist.getLength(); i++) {
			if (nodelist.item(i).getTextContent().equals(textValue)) {
				return nodelist.item(i);
			}
		}
		return null;
	}

	static private boolean existNode(Node node, String nodeName, String textValue) {
		NodeList childs = node.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			if (childs.item(i).getNodeName().equals(nodeName) && childs.item(i).getTextContent().equals(textValue)) {
				return true;
			}
		}
		return false;
	}

	public static String resolveConflicts(String salida) {
		return "";
	}
}
