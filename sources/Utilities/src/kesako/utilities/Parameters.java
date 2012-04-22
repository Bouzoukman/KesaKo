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
package kesako.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.PropertyResourceBundle;

import org.apache.log4j.Logger;

/**
 * This class initializes all parameters needed by the application Kes@Ko.<br>
 * The class implements the Log4J logging system. 
 * @author Frederic SACHOT
 */
public class Parameters {
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(Parameters.class);
	/**
	 * Path to the directory of the SQL library
	 */
	private static String sqlLibPath;
	/**
	 * SQL connection string
	 */
	private static String sqlConnectionString;
	/**
	 * SQL user's login
	 */
	private static String loginSQL;
	/**
	 * SQL user's password 
	 */
	private static String pwdSQL;
	/**
	 * SOLR URL
	 */
	private static String solrURL;
	/**
	 * Number of seconds between 2 scans of a directory
	 */
	private static int fileInterval;
	/**
	 * Number of seconds between 2 checks of the list of directories to index
	 */
	private static int listInterval;
	/**
	 * Number of seconds between 2 indexing tasks 
	 */
	private static int indexInterval;
	/**
	 * Number of seconds between 2 indexing tasks of files with indexing error.
	 */
	private static int indexErrorInterval;
	/**
	 * Maximum number of files to index during an indexing task
	 */
	private static int nbFile;
	
	/**
	 * Read the property file and initialize all parameters.
	 * This class use the PropertyResourceBundle class to read the property file.
	 * @param nomfic property file name
	 * @see PropertyResourceBundle
	 */
	public Parameters(String nomfic){
		File f=new File(nomfic);
		FileInputStream stream=null;
		PropertyResourceBundle fconfig=null;
		try {
			stream=new FileInputStream(f);
			fconfig = new PropertyResourceBundle(stream);
			Parameters.sqlLibPath=fconfig.getString("sqlLibPath");
			Parameters.sqlConnectionString=fconfig.getString("sqlConnectionString");
			Parameters.loginSQL=fconfig.getString("login");
			Parameters.pwdSQL=fconfig.getString("password");
			if(fconfig.getString("solrURL")!= null && !fconfig.getString("solrURL").trim().equals("")){
				Parameters.solrURL=fconfig.getString("solrURL");
			}else{
				Parameters.solrURL="http://localhost:8085/solr";
			}
			Parameters.fileInterval=Integer.parseInt(fconfig.getString("fileWatcherInterval"));
			Parameters.listInterval=Integer.parseInt(fconfig.getString("listWatcherInterval"));
			Parameters.indexInterval=Integer.parseInt(fconfig.getString("indexInterval"));
			Parameters.indexErrorInterval=Integer.parseInt(fconfig.getString("indexErrorInterval"));
			Parameters.nbFile=Integer.parseInt(fconfig.getString("nbFile"));
		}
		catch (FileNotFoundException e) {
			logger.fatal(nomfic +" file not found",e);
		}
		catch (IOException e) {
			logger.fatal("Parameters loading error: " + e.getMessage(),e);
		}
	}
	/**
	 * Return the path of the SQL library
	 */
	public static String getSqlLibPath() {
		return sqlLibPath;
	}
	/**
	 * Return the SQL connection string.
	 */
	public static String getSqlConnectionString() {
		return sqlConnectionString;
	}
	/**
	 * Return the login to log to the SQL server
	 */
	public static String getLogin(){
		return Parameters.loginSQL;
	}
	/**
	 * Return the password links to the login.
	 */
	public static String getPwd(){
		return Parameters.pwdSQL;
	}
	/**
	 * Return the URL of the SOLR server.<br>
	 * If the value in the config file is null, the value is http://localhost:8085/solr
	 */
	public static String getSolrURL() {
		return solrURL;
	}
	/**
	 * Return the number of seconds between 2 scans of a directory
	 */
	public static int getFileInterval() {
		return fileInterval;
	}
	/**
	 * Return the number of seconds between 2 checks of the list of directories to index
	 */
	public static int getListInterval() {
		return listInterval;
	}
	/**
	 * Return the number of seconds between 2 indexing tasks 
	 */
	public static int getIndexInterval() {
		return indexInterval;
	}
	/**
	 * Return the number of seconds between 2 indexing tasks of files with indexing error.
	 */
	public static int getIndexErrorInterval() {
		return indexErrorInterval;
	}
	/**
	 * Return the maximum number of files to index during an indexing task
	 */
	public static int getNbFile() {
		return nbFile;
	}
}
