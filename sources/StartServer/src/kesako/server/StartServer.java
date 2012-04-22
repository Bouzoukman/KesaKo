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
package kesako.server;

import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import kesako.utilities.XMLUtilities;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Read the XML file ServerList.xml and execute all shell commands.<br>
 * The class implements the Log4J logging system.<br>
 * To launch the different servers on UBUNTU, add the two following lines in the script /etc/rc.local<br>
 * Be careful, this script is launch for every user.<br>
 * <br>
 * cd < path of Kes@Ko package >/Kes@Ko_package/package #the directory where the jar file is. <br>
 * java -jar kesako-StartServer-311F2.0.jar #the command to launch the utility that launch the different server.
 * @author Frederic SACHOT
 * @version 311F2.0
 *
 */
public class StartServer {
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(StartServer.class);
	/**
	 * Vector of the ServerCommand objects to execute
	 */
	private Vector<ServerCommand> listServer;
	/**
	 * Create the commands list in the file fileNameXML
	 * @param fileNameXML XML file containing all command to execute. <br>
	 * Example of a server command: (HSQLDB server)<br>
	 * < server ><br>
	 * < name >HSQLDB< /name ><br>
	 * < description >Start the SQL Server< /description ><br>
	 * < workDir >../SQL< /workDir ><br>
	 * < command >java -cp hsqldb.jar org.hsqldb.Server< /command ><br>
	 * < /server >
	 */
	public StartServer(String fileNameXML){
		try {
			Node n;
			Document doc=XMLUtilities.getXMLDocument(fileNameXML);
			Element root=doc.getDocumentElement();
			NodeList l=root.getElementsByTagName("server");
			listServer=new Vector<ServerCommand>();
			for(int i=0;i<l.getLength();i++){
				n=l.item(i);
				listServer.add(new ServerCommand(n));
			}			
		} catch (ParserConfigurationException e) {
			logger.fatal("StartServer",e);
		} catch (SAXException e) {
			logger.fatal("StartServer",e);
		} catch (IOException e) {
			logger.fatal("StartServer",e);
		}
	}

	/**
	 * Configure the log4J system with the file log4j_server.xml, create the list of commands listed in the file ServerList.xml, and execute them.
	 * @param args not used
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure("log4j_server.xml");
		StartServer s=new StartServer("ServerList.xml");
		for(int i=0;i<s.listServer.size();i++){
			try {
				s.listServer.get(i).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
