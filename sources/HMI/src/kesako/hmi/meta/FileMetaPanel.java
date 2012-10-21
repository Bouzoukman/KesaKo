/*
 * Copyright 2012 Frédéric SACHOT
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
package kesako.hmi.meta;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import kesako.search.Meta;
import kesako.utilities.FileUtilities;
import kesako.utilities.XMLUtilities;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FileMetaPanel extends JPanel{
	private static final long serialVersionUID = -8668407800902178469L;
	private static final Logger logger = Logger.getLogger(FileMetaPanel.class);
	/**
	 * Root node of the document that list meta-data
	 */
	private Node rootMetaXML;
	/**
	 * Vector of meta-data
	 */
	private Vector<Meta> vMetas;
	/**
	 * Vector of panels representing the different meta-data
	 */
	private TreeMap<String,MetaPanel> vMetaPanels;

	private Vector<String> metaValues;
	
	private String fileName;

	public FileMetaPanel (File f,AddMetaPanel parent,int flagColor,boolean first){
		logger.debug("FileMetaPanel construction");
		fileName=f.getAbsolutePath();
		try {
			Document doc=XMLUtilities.getXMLDocument("meta.xml");
			rootMetaXML=doc.getDocumentElement();

			metaValues = new Vector<String>();

			GridBagConstraints cMeta=new GridBagConstraints();
			cMeta.insets=new Insets(2,5,2,5);
			cMeta.ipadx=5;
			cMeta.ipady=5;
			cMeta.fill=GridBagConstraints.BOTH;
			cMeta.gridheight=1;
			cMeta.gridwidth=1;
			cMeta.weightx=1;
			cMeta.weighty=0;

			//Meta list construction
			this.setLayout(new GridBagLayout());
			this.setBorder(new TitledBorder(f.getName()));
			if(flagColor==1){
				this.setOpaque(true);
				this.setBackground(Color.GRAY);
			}else{
				this.setOpaque(false);
			}
			NodeList listMetaXML = ((Element)rootMetaXML).getElementsByTagName("meta");
			Node item;
			Meta meta;
			MetaPanel mp;
			vMetaPanels=new TreeMap<String,MetaPanel>();
			vMetas=new Vector<Meta>();
			cMeta.gridx=0;
			int nbMeta;
			for(nbMeta=0;nbMeta<listMetaXML.getLength();nbMeta++){
				cMeta.gridy=nbMeta;
				item=listMetaXML.item(nbMeta);
				meta=new Meta(item);
				if(meta.isVisibleInMetaPanel()){
					mp=new MetaPanel(meta,parent,nbMeta%2,first);
					if(meta.getName().trim().equalsIgnoreCase("date")){
						mp.setInputVerifier(new DateVerifier());
						mp.setDefaultValue("yyyy-mm-dd");
					}
					vMetaPanels.put(meta.getName(), mp);
					vMetas.add(meta);
					this.add(mp,cMeta);
				}
			}

		} catch (ParserConfigurationException e1) {
			logger.fatal("erreur XML :",e1);
		} catch (SAXException e1) {
			logger.fatal("erreur XML :",e1);
		} catch (IOException e1) {
			logger.fatal("erreur XML :",e1);
		}
	}

	/**
	 * Initiating values of meta-data. If multiple files are selected, meta-data are initiated by the first value of the meta-data. 
	 * @param fileName
	 */
	public void initMeta(){
		File fileMeta;
		try {
			String nomFicMeta=FileUtilities.getFileMetaName(fileName);
			String nomMeta,valueMeta;
			logger.debug("nomFicMeta="+nomFicMeta);
			fileMeta=new File(nomFicMeta);
			if(fileMeta.exists() && fileMeta.isFile()){
				Document doc = XMLUtilities.getXMLDocument(nomFicMeta);
				Node root=doc.getDocumentElement();
				logger.debug("Root element " + root.getNodeName());
				NodeList nodeLst = root.getChildNodes();
				for (int s = 0; s < nodeLst.getLength(); s++) {
					Node fstNode = nodeLst.item(s);
					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						logger.debug(fstNode.getNodeName()+" : "+fstNode.getAttributes().item(0).getNodeValue()+" : "+fstNode.getAttributes().item(1).getNodeValue());
						if(fstNode.getNodeName().equalsIgnoreCase("meta")){
							nomMeta="";
							valueMeta="";
							for(int j=0;j<fstNode.getAttributes().getLength();j++){
								if(fstNode.getAttributes().item(j).getNodeName().equalsIgnoreCase("name")){
									nomMeta=fstNode.getAttributes().item(j).getNodeValue();														
								}
								if(fstNode.getAttributes().item(j).getNodeName().equalsIgnoreCase("value")){
									valueMeta=fstNode.getAttributes().item(j).getNodeValue();														
								}
							}
							logger.debug("nomMeta : "+nomMeta+", value="+valueMeta);
							if(vMetaPanels.containsKey(nomMeta)){
								if(!valueMeta.trim().equals("")){
									vMetaPanels.get(nomMeta).setMetaValue(valueMeta);
								}else{
									vMetaPanels.get(nomMeta).setMetaValue(vMetaPanels.get(nomMeta).getDefaultValue());
								}
							}
						}
					}
				}	
			}
		} catch (SAXException e) {
			logger.fatal("file processing : "+e);
		} catch (IOException e) {
			logger.fatal("file processing : "+e);
		} catch (ParserConfigurationException e) {
			logger.fatal("file processing : "+e);
		}
		for(String key:vMetaPanels.keySet()){
			vMetaPanels.get(key).updateValues();
		}
	}

	public Vector<String> getMetaValues() {
		return metaValues;
	}

	private class DateVerifier extends InputVerifier{
		@Override
		public boolean verify(JComponent input) {
			JTextField tf = (JTextField) input;
			String dateTxt=tf.getText().trim();
			boolean valid=dateTxt.matches("\\d{4}-\\d{2}-\\d{2}");
			if(!valid && !dateTxt.trim().equals("") && !dateTxt.trim().equalsIgnoreCase("yyyy-mm-dd")){
				JOptionPane.showMessageDialog(input, "The date is in the wrong format.\n The format is yyyy-mm-dd","Date Format Error", JOptionPane.ERROR_MESSAGE);
			}else{
				valid=true;
			}
			return valid;
		}	
	}
	
	public void saveMetaXML(){
		//création du fichier xml
		logger.debug("Création fichier de sources XML");
		DocumentBuilderFactory factoryXML;
		DocumentBuilder builderXML;
		DOMImplementation implDOM;
		Document docXML;
		Element rootXML,metaXML;
		factoryXML = DocumentBuilderFactory.newInstance();
		try {
			builderXML = factoryXML.newDocumentBuilder();
			implDOM = builderXML.getDOMImplementation();
			docXML = implDOM.createDocument(null,null,null);
			rootXML=docXML.createElement("root");
			docXML.appendChild(rootXML);
			for(int i=0;i<vMetas.size();i++){
				metaXML=docXML.createElement("meta");
				metaXML.setAttribute("name", vMetas.get(i).getName());
				metaXML.setAttribute("value",vMetaPanels.get(vMetas.get(i).getName()).getMetaValue());
				rootXML.appendChild(metaXML);
			}			
			XMLUtilities.saveFileXML(docXML,FileUtilities.getFileMetaName(fileName));
		} catch (ParserConfigurationException e) {
			logger.fatal("initXML ",e);
		} catch (TransformerConfigurationException e) {
			logger.fatal("initXML ",e);
		} catch (TransformerException e) {
			logger.fatal("initXML ",e);
		} catch (DOMException e) {
			logger.fatal("initXML ",e);
		} catch (IOException e) {
			logger.fatal("initXML ",e);
		}
		logger.debug("Fin création fichier de sourcesXML");
	}

	/**
	 * @return the vMetaPanels
	 */
	public TreeMap<String, MetaPanel> getMetaPanels() {
		return this.vMetaPanels;
	}

}
