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
package kesako.watcher.runnable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import kesako.utilities.Constant;
import kesako.utilities.DBUtilities;

import org.apache.log4j.Logger;

/**
 * Monitor the list of sources and create a watcher by source to survey the content of the source and update the index.<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class ListSourceProcess extends IntervalDBWork{
	/**
	 * Log4J logger of the class
	 */
	private static final Logger logger = Logger.getLogger(ListSourceProcess.class);
	/**
	 * Store each source process.  
	 */
	private Map<String, SourceWatcher> listWatcher = new HashMap<String, SourceWatcher>();
	/**
	 * Number of seconds between two check of the content of a source.
	 */
	private int fileWatcherInterval;
	/**
	 * Process that monitors the list of sources.
	 * @param listIntervalSeconds Number of seconds between two check of the list of sources.
	 * @param fileIntervalSeconds Number of seconds between two check of the content of a source.
	 */
	public ListSourceProcess(int listIntervalSeconds,int fileIntervalSeconds) {
		super(listIntervalSeconds, "ListSourceWatcher");
		this.fileWatcherInterval=fileIntervalSeconds;
		logger.debug("ListSourceWatcher : "+listIntervalSeconds+ " / "+fileIntervalSeconds);
	}
	/**
	 * Check if the source has already a watcher or create one if not.<br>
	 * The name of the source watcher is: Th_SW< idSource >
	 * @param idSource id of the source
	 * @param sourcePath path of the source
	 */
	private void createSourceWatcher(int idSource, String sourcePath){
		logger.debug("CreateSourceWatcher");
		SourceWatcher s;
		String watcherName;
		watcherName = giveThreadName("Th_SW"+Integer.toString(idSource));	
		if(!listWatcher.containsKey(watcherName)){
			logger.info("create sourceWatcher "+watcherName);
			s=new SourceWatcher(idSource,sourcePath,fileWatcherInterval);
			listWatcher.put(s.getThredName(),s);
			logger.debug(s);
			s.startWorking();	
		}
	}

	/**
	 * Delete the source's watcher of the source idSource.<br>
	 * The name of the source watcher is: Th_SW< idSource >
	 * Before suppressing the watcher, the method stop it and then delete it. Then all file of the source are marked to be suppressed. 
	 * @param idSource id of the source for which the watcher will be suppressed
	 * @throws SQLException 
	 */
	private void deleteSourceWatcher(Connection cn,int idSource) throws SQLException{
		logger.debug("deleteSourceWatcher");
		String nomWatcher;
		String requete;
		ResultSet rs;
		nomWatcher = giveThreadName("Th_SW"+Integer.toString(idSource));
		if(listWatcher.containsKey(nomWatcher)){
			listWatcher.get(nomWatcher).stopWorking();
			listWatcher.remove(nomWatcher);
		}
		requete="select count(*) as nb from t_fichiers where id_source="+idSource;
		rs=DBUtilities.executeQuery(cn, requete);
		rs.next();
		if(rs.getInt("nb")>0){
			//mise à jour de la base pour suppression des fichiers
			requete="update t_fichiers set flag="+Constant.TO_SUPPRESSED+
					" where id_source="+idSource;
		}else{
			requete="delete from t_sources where id_source="+idSource;
		}
		DBUtilities.executeQuery(cn,requete);
	}
	/**
	 * Check the list of sources. If the source is marked to be suppressed, the source's Watcher is suppressed, else the source's watcher is created.
	 */
	@Override
	protected void doWork() {
		logger.debug("ListSourceWatcher.doWork");
		ResultSet rs;
		String sqlQuery="";
		Connection cn=getConnection();
		try {
			sqlQuery="start transaction";
			DBUtilities.executeQuery(cn,sqlQuery);

			sqlQuery="select * from t_sources";
			rs = DBUtilities.executeQuery(cn,sqlQuery);
			while(rs.next()){
				if(rs.getInt("flag")==Constant.TO_SUPPRESSED){
					deleteSourceWatcher(cn,rs.getInt("id_source"));
				}else{
					createSourceWatcher(rs.getInt("id_source"),rs.getString("chemin"));
				}
			}
			cn.commit();
		} catch (SQLException e) {
			logger.fatal("ERROR : "+ sqlQuery,e);
			try {
				cn.rollback();
			} catch (SQLException e1) {
				logger.fatal("ERROR Rollback : "+sqlQuery,e1);
			}
		}
		try {
			cn.close();
		} catch (SQLException e) {
			logger.fatal("ERROR closing connection",e);
		}
	}
}
