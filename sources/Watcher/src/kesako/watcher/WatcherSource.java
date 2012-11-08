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
package kesako.watcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import kesako.utilities.DBUtilities;
import kesako.utilities.LoadJarFile;
import kesako.utilities.Parameters;
import kesako.utilities.SOLRUtilities;
import kesako.watcher.runnable.IndexFileErrorProcess;
import kesako.watcher.runnable.IndexFileProcess;
import kesako.watcher.runnable.ListSourceProcess;
import kesako.watcher.runnable.MetaFileProcess;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;

/**
 * Process that launch all needed document processes.<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 * @version 311F2.1
 */
public class WatcherSource {
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(WatcherSource.class);
	/**
	 * Read the parameters, initialize utilities and launch document processes.<br>
	 * The document processes are:
	 * <ul>
	 * <li>ListSourceProcess: This process look after the list of sources parameterized, and create or delete a SourceWatcher process for each source.</li>
	 * <li>IndexFileProcess: This process index new files.</li>
	 * <li>MetaFileProcess: This process update meta-data of indexed files.</li>
	 * <li>IndexFileErrorProcess: This process try to index files that have an indexing error </li>
	 * </ul> 
	 * @see ListSourceProcess 
	 * @see IndexFileProcess 
	 * @see MetaFileProcess 
	 * @see IndexFileErrorProcess 
	 * @see kesako.watcher.runnable.SourceWatcher
	 */
	public WatcherSource(){
		//création des proccess de surveillance des répertoires
		logger.debug("création des proccess de surveillance des répertoires");
		ListSourceProcess lW=new ListSourceProcess(Parameters.getListInterval(),Parameters.getFileInterval());
		lW.startWorking();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					Connection cn=DriverManager.getConnection(Parameters.getSqlConnectionString(), Parameters.getLogin(),Parameters.getPwd());
					DBUtilities.executeQuery(cn, "SHUTDOWN COMPACT");
				} catch (SQLException e) {
					logger.fatal("ERROR processing connection",e);
				}
			}
		});
	}
	/**
	 * Launch Kes@Ko watcher. It needs log4j_watcher.xml to initialize Log4J logger, and kesako.ini to initialize parameters of Kes@Ko.
	 * @param args not used
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure("log4j_watcherSource.xml");
		Thread[] threadList=new Thread[Thread.activeCount()+10];
		logger.debug("Read Parameters in kesako.ini");
		new Parameters("kesako.ini");
		logger.debug("SOLR Utilities creation");
		//Verification that the SOLR server is running and then create SOLR utilities.
		String url = Parameters.getSolrURL();
		boolean test=false;
		while(!test){
			try {
				new CommonsHttpSolrServer(url).ping();
				test=true;
			} catch (MalformedURLException e) {
				logger.fatal("BAD URL "+url,e);
				test=true;
			} catch (SolrServerException e) {
			} catch (IOException e) {
				test=true;
				logger.fatal(e);
			}
		}
		new SOLRUtilities();
		logger.debug("DB Utilities creation");
		//SQL driver loading
		LoadJarFile.loadFile(Parameters.getSqlLibPath());       
		test=false;
		while(!test){
			try {
				DriverManager.getConnection(Parameters.getSqlConnectionString(), Parameters.getLogin(),Parameters.getPwd()).close();
				test=true;
			} catch (SQLException e) {
				logger.fatal(e);
			}
		}
		new DBUtilities();
		logger.debug("Watcher creation");		
		new WatcherSource();
		logger.debug("Nb Thread "+Thread.activeCount());
		if(logger.getLevel()==Level.DEBUG){
			Thread.enumerate(threadList);
			for(int i=0;i<threadList.length;i++){
				if(threadList[i]!=null){
					logger.debug(threadList[i].getName());
				}
			}
		}
	}
}
