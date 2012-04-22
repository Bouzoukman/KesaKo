/*
 * Copyright 2012 Frederic SACHOT: bouzoukman@gmail.com
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
package kesako.common;

import kesako.utilities.XMLUtilities;

import org.w3c.dom.Node;

/**
 * Meta is a class that instantiates an object representing the definition of a meta-data.<br>
 * The best way to instantiate Meta object is to read the file meta.xml<br>
 * The root node of the XML documents is < metas > <br>
 * For each meta-data there is an element < meta >(example):<br>
 * < meta ><br>
 * < label >Sources< /label ><br>
 * < name >folder_name< /name ><br>
 * < hmiPosition >left< /hmiPosition ><br>
 * < isVisibleInMetaPanel >0< /isVisibleInMetaPanel ><br>
 * < /meta >
 * @author Frederic SACHOT
 */
public class Meta {
	/**
	 * Label naming the meta-data.
	 */
	private String label;
	/**
	 * name of the meta-data
	 */
	private String name;
	/**
	 * Position of the facet in the HMI.<br>
	 * The possible values are: left,  right or nothing. 
	 */
	private String hmiPosition;
	/**
	 * This parameter indicate if the meta-data is displayed in the Add Meta Panel. 
	 */
	private boolean isVisibleInMetaPanel;
	/**
	 * Constructor based on an XML element
	 * @param parentXML XML element
	 */
	public Meta(Node parentXML){
		this(XMLUtilities.getNodeValue(parentXML, "label"),
				XMLUtilities.getNodeValue(parentXML, "name"),
				XMLUtilities.getNodeValue(parentXML,"hmiPosition"),
				Integer.parseInt(XMLUtilities.getNodeValue(parentXML,"isVisibleInMetaPanel"))
				);
	}
	/**
	 * 
	 * @param label Label naming the meta-data.
	 * @param name Name of the meta-data
	 * @param hmiPosition Position of the facet in the HMI.<br>
	 * The possible values are: left,  right or nothing. 
	 * @param isVisibleInMetaPanel This parameter indicate if the meta-data is displayed in the Add Meta Panel. 
	 */
	public Meta(String label, String name,String hmiPosition, int isVisibleInMetaPanel){
		this.label=label;
		this.name=name;
		if(hmiPosition.trim().equalsIgnoreCase("right")||hmiPosition.trim().equalsIgnoreCase("left")){
			this.hmiPosition = hmiPosition.trim().toLowerCase();
		}else{
			this.hmiPosition="";
		}
		this.isVisibleInMetaPanel=(isVisibleInMetaPanel!=0);
	}
	/**
	 * Return the label of the meta-data
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * Return the position of the facet in the HMI.<br>
	 * The possible values are: left,  right or nothing. 
	 */
	public String getHMIPosition() {
		return hmiPosition;
	}

	/**
	 * return the name of the meta-data
	 */
	public String getName() {
		return name;
	}
	/**
	 * Indicate if the meta-data is displayed in the Add Meta Panel. 
	 */
	public boolean isVisibleInMetaPanel() {
		return this.isVisibleInMetaPanel;
	}
}
