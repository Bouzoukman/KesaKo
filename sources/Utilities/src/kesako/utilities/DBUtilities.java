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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.TreeSet;


import org.apache.log4j.Logger;

/**
 * Data-base utilities.<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class DBUtilities {
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(DBUtilities.class);
	/**
	 * Vector object that stores the paths of directories of all indexed files.
	 */
	private static TreeSet<String> validPath= new TreeSet<String>();
	/**
	 * Vector object that stores the paths of sources.
	 */
	private static TreeSet<String> sourcePath= new TreeSet<String>();
	private static Connection cn;

	/**
	 * The constructor create a connection object to the data-base, the statement object, and initialize the validPath vector.
	 */
	public DBUtilities(){
		//SQL driver loading
		LoadJarFile.loadFile(Parameters.getSqlLibPath());       
		//initialization of the list of valid paths.
		populateValidPath();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if(cn!=null && !cn.isClosed()){
						cn.commit();
						cn.close();
					}
				} catch (SQLException e) {
					logger.fatal("ENDING DBUtilities ",e);
				}
			}
		});
	}

	/**
	 * Execute a SQL query and return a ResultSet object.
	 * @param cn
	 * @param query SQL query to execute.
	 * @return
	 * @throws SQLException
	 */
	public static ResultSet executeQuery(Connection cn,String query) throws SQLException {
		ResultSet rs=null;
		Statement st;
		logger.debug("execution of the query: "+query);
		logger.debug("Statement object creation");
		st=cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		logger.debug("statement setPoolable");
		if(!st.isPoolable()){
			st.setPoolable(true);
		}
		logger.debug("statement "+st.isPoolable());
		st.execute(query);
		logger.debug("statement execution");
		rs=st.getResultSet();
		logger.debug(query+" EXECUTED / "+rs==null);
		if(rs==null){
			logger.debug("query="+query+" / resultset NULL");
		}
		return rs;
	}
	/**
	 * Return a valid String for a SQL query
	 * @param st string to be parsed
	 */
	public static String getStringSQL(String st){
		return st.replace("'", "''");
	}
	/**
	 * Return the id of the file in the data-base 
	 * @param uri path of the file for which the id is researched.
	 * @return if the file is in the data-base the id is returned, else -1
	 * @throws SQLException 
	 */
	public static int getFileId(Connection cn,String uri) throws SQLException{
		int idFichier;
		String query="select id_fichier from t_fichiers where chemin='"+DBUtilities.getStringSQL(uri)+"'";
		ResultSet rs;
		rs=DBUtilities.executeQuery(cn,query);
		if(rs!=null && rs.next()){
			idFichier=rs.getInt("id_fichier");
		}else{
			idFichier=-1;
		}
		logger.debug("getFileId : "+query +" / "+idFichier);
		return idFichier;
	}	
	/**
	 * Create a source in the data-base only if the path doesn't exist in the data-base.
	 * @param sourceName Name of the source
	 * @param sourcePath path of the source
	 * @throws SQLException 
	 */
	public static void createSource(Connection cn,String sourceName, String sourcePath) throws SQLException{
		logger.debug("createSource");
		String query="select id_Source from t_sources where chemin like'"+sourcePath+"'";
		ResultSet rs=DBUtilities.executeQuery(cn,query);
		if(!rs.next()){
			query="insert into t_sources (nom,chemin,flag) values ('"+
			DBUtilities.getStringSQL(sourceName)+"','"+
			DBUtilities.getStringSQL(sourcePath)+"',"+
			Constant.TO_INDEX+")";
			DBUtilities.executeQuery(cn,query);
			populateValidPath();
		}else{
			logger.debug("The source : "+sourcePath+" is allready in the data-base");
		}
	}
	/**
	 * Update the name and the flag of the source which the id is idSource. <br>
	 * Be careful. The existence of the source is not verified before updating. <br>
	 * The flag's value is not verified before updating.
	 * @param idSource id of the source to update
	 * @param sourceName name of the source to update.
	 * @param flag flag of the source to update. Values of the flag are: Constant.TO_INDEX and Constant.TO_SUPPRESSED
	 * @throws SQLException 
	 */
	public static void updateSource(Connection cn,int idSource,String sourceName,int flag) throws SQLException{
		String requete="update t_sources set nom='"+DBUtilities.getStringSQL(sourceName)+"',"+
		"flag="+flag+
		" where id_source="+idSource;
		DBUtilities.executeQuery(cn,requete);
	}
	/**
	 * The method populate the vector validPath with the paths of all directory's paths from root to all source's paths.<br>
	 * Today, the method works only with Unix and Linux systems.
	 */
	private static void populateValidPath() {
		String query="";
		String path;
		String[] items;
		String totalPath,separator;
		int i;
		ResultSet rs;
		try {
			cn= DriverManager.getConnection(Parameters.getSqlConnectionString(), Parameters.getLogin(),Parameters.getPwd());
			try {
				logger.debug("Connection object creation");			
				query="select chemin from t_sources order by chemin";
				rs = DBUtilities.executeQuery(cn,query);
				while(rs.next()){
					path=rs.getString("chemin");
					DBUtilities.sourcePath.add(path);
					logger.debug("split : " +path +" / "+File.separator);
					separator=File.separator;
					if(File.separator.equals("\\")){
						separator="\\\\";
					}
					items=path.split(separator);
					totalPath="";
					if(OSValidator.isUnix()){
						for(i=1;i<items.length;i++){
							totalPath+=File.separator+items[i];
							DBUtilities.validPath.add(totalPath);
							System.out.println(i+" : "+totalPath);
						}
					}else{
						if(OSValidator.isWindows()){
							for(i=0;i<items.length;i++){
								if(i>0){
									totalPath+=File.separator+items[i];
								}else{
									totalPath=items[i];
								}
								DBUtilities.validPath.add(totalPath);
								System.out.println(i+" : "+totalPath);
							}
						}else{
							DBUtilities.validPath=null;
						}
					}
				}
			} catch (SQLException e) {
				logger.fatal("ERROR : "+query,e);
			}
			try{
				cn.close();
			} catch (SQLException e) {
				logger.fatal("ERROR closing connection",e);
			}
		} catch (SQLException e1) {
			logger.fatal("ERROR creating connection",e1);
		}
	}
	/**
	 * Return TRUE if the directory or the file is in the path of a source.<br>
	 * Today, the method works only with Unix and Linux operating systems. If the OS is not Unix or Linux, the method always return TRUE.<br>
	 * This method is useful for a JFileChooser object to show only paths needed to access to indexed files.
	 * @param dir directory for with the validity of the path is tested.
	 */
	public static boolean isAccetableFile (File dir){
		boolean test=false;
		String dirPath;
		String iteratorPath;
		Iterator<String> it=sourcePath.iterator(); 
		while(!test && it.hasNext()){
			iteratorPath=it.next()+File.separator;
			dirPath=dir.getAbsolutePath();
			if(dir.isDirectory()){
				dirPath+=File.separator;
			}
			test=dirPath.contains(iteratorPath);
		}
		if(!test && DBUtilities.validPath!=null){
			test=DBUtilities.validPath.contains(dir.getAbsolutePath());
		}
		logger.debug("IsAcceptableDirectory : "+dir.getAbsolutePath()+" / "+test);
		return test;
	}
}