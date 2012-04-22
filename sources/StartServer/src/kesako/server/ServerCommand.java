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

import java.io.File;
import java.io.IOException;

import kesako.utilities.XMLUtilities;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;


/**
 * Object that allow to execute a shell command.<br>
 * The object is built from a XML node that describes the command to execute.<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class ServerCommand {
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(ServerCommand.class);
	/**
	 * command to execute
	 */
	private String[] command;
	/**
	 * description of the command.
	 */
	private String description;
	/**
	 * name of the command
	 */
	private String name;
	/**
	 * working directory in which the command must be executed.
	 */
	private File workDir;
	/**
	 * Construction of the object.
	 * @param n XML node which contains the command to be executed:(example of the HSQLDB server)<br>
	 * < server ><br>
	 * < name >HSQLDB< /name ><br>
	 * < description >Start the SQL Server< /description ><br>
	 * < workDir >../SQL< /workDir ><br>
	 * < command >java -cp hsqldb.jar org.hsqldb.Server< /command ><br>
	 * < /server >
	 */
	public ServerCommand(Node n){
		name=XMLUtilities.getNodeValue(n, "name");
		description=XMLUtilities.getNodeValue(n, "description");
		logger.debug("description : "+description);
		workDir=new File(XMLUtilities.getNodeValue(n, "workDir"));
		logger.debug("workDir : "+workDir);
		command=XMLUtilities.getNodeValue(n, "command").split(" ");
		logger.debug(XMLUtilities.getNodeValue(n, "command"));
	}
	/**
	 * Execute the command.
	 * @throws IOException
	 */
	public void start() throws IOException{
		logger.info("launch of server : "+name);
		Runtime r=Runtime.getRuntime();
		r.exec(command,null,workDir);
		logger.info("server "+name+" launched");
	}
}
