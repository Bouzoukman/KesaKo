/*
 * Copyright 2012 Frederic SACHOT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kesako.utilities;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * XML utilities.<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class XMLUtilities {
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(XMLUtilities.class);

	/**
	 * Return a Node object with the name is nodeName
	 * @param parent parent Node which contains the node nodeName
	 * @param nodeName name of the node
	 * @return a Node object with the name is nodeName<br>
	 * if the node parent doesn't have child nodes, null is return.
	 */
	public static Node getNode(Node parent, String nodeName){
		if(parent.hasChildNodes()){
			for(int i=0;i<parent.getChildNodes().getLength();i++){
				if(parent.getChildNodes().item(i).getNodeName().equalsIgnoreCase(nodeName)){
					return parent.getChildNodes().item(i);
				}
			}
		}
		return null;
	}
	/**
	 * Return the value of the node nodeName
	 * @param parent parent Node of the node nodeName
	 * @param nodeName name of the node
	 */
	public static String getNodeValue(Node parent, String nodeName){
		String value="";
		if(getNode(parent,nodeName).getChildNodes().item(0)!=null){
			value=getNode(parent,nodeName).getChildNodes().item(0).getNodeValue();
		}
		return value;
	}
	/**
	 * Return the value of the node nodeXML
	 * @param nodeXML Node object
	 */
	public static String getNodeValue(Node nodeXML){
		return nodeXML.getChildNodes().item(0).getNodeValue();
	}
	/**
	 * Update the value of a XML node
	 * @param parentXML parent node of the node for which the value has changed
	 * @param nodeName name of the node for which the value has changed
	 * @param value new value of the node.
	 */
	public static void updateNodeXML(Node parentXML,String nodeName,String value){
		logger.debug("updateNodeXML");
		Node nomXML;

		nomXML=getNode(parentXML,nodeName);
		nomXML.getChildNodes().item(0).setNodeValue(value);
	}

	/**
	 * Save a XML file
	 * @param docXML document object of the XML file to save
	 * @param nomFileXML name of the file to save
	 * @throws TransformerException 
	 * @throws IOException 
	 */
	public static void saveFileXML(Document docXML,String nomFileXML) throws TransformerException, IOException{
		logger.debug("saveFileXML "+nomFileXML);
		docXML.getDocumentElement().normalize();
		DOMSource ds = new DOMSource(docXML);
		StreamResult sr = new StreamResult(nomFileXML);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(ds, sr);
		logger.debug("fin saveFileXML");
	}
	/**
	 * Return the root node of a XML document.
	 * @param fileNameXML name of the XML file
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document getXMLDocument(String fileNameXML) throws ParserConfigurationException, SAXException, IOException{
		File file = new File(fileNameXML);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		return doc;
	}
}