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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import kesako.utilities.Constant;
import kesako.utilities.DBUtilities;
import kesako.utilities.SOLRUtilities;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

/**
 * Process that index files within a batch of nbFiles.<br>
 * To do that, the process takes the first nbFiles where the field flag = Constant.TO_INDEXED.<br>
 * If the file's indexing ended with no error, the field flag is updated to Constant.INDEXED, else the flag is updated to Constant.INDEXE_ERROR.<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 *
 */
public class IndexFileProcess extends IntervalDBWork{
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(IndexFileProcess.class);
	/**
	 * indicate if an indexing batch is running.<br>
	 * It's a static variable to be sure that only one indexing process is running at all times. 
	 */
	private static boolean indexingBatch=false;
	/**
	 * list of id of files to process within a batch
	 */
	private int[] idFileToIndex;
	/**
	 * maximum number of files that can be processed within a batch.
	 */
	private int nbFiles;
	/**
	 * Constructor of the indexing process. The process run in a specific thread.
	 * @param intervalSeconds number of seconds between to indexing batch
	 * @param nbFile maximum number of files that can be processed within a batch.
	 */
	public IndexFileProcess(int intervalSeconds,int nbFile) {
		super(intervalSeconds, "IndexProcess");
		this.nbFiles=nbFile;
		idFileToIndex= new int[this.nbFiles];
		logger.debug("IndexProcess / "+intervalSeconds+" / "+nbFile);
	}
	/**
	 * Clean the index, and initialize the thread.
	 */
	public void startWorking(){
		Connection cn=getConnection();
		cleanIndex(cn,0);
		try {
			cn.close();
		} catch (SQLException e) {
			logger.fatal("ERROR closing connection",e);
		}
		super.startWorking();
	}
	/**
	 * Verify that all files in the index are referenced in the data-base. <br>
	 * If a file in the index is not referenced in the data-base, it is suppressed from the index.
	 */
	private void cleanIndex(Connection cn,long start){
		logger.debug("cleanIndex");
		long nextStart=0;
		long nbResults=0;
		String query="*:*";
		String uri;
		NamedList<String> q = new NamedList<String>();
		q.add("fl", "*");
		q.add("q", query);
		q.add("rows", Integer.toString(this.nbFiles));
		q.add("start", Long.toString(start));
		logger.debug("query : "+q);
		QueryResponse r;
		SolrDocumentList listDoc;
		try {
			r = SOLRUtilities.getSOLRServer().query(SolrParams.toSolrParams(q));
			listDoc = r.getResults();
			nbResults=listDoc.getNumFound();
			if(listDoc!=null && listDoc.size()>0){
				nextStart=start;
				try {
					for(SolrDocument solrDoc : listDoc){
						if(solrDoc.getFieldValue("file_uri")!=null){
							uri=solrDoc.getFieldValue("file_uri").toString();
							if(DBUtilities.getFileId(cn,uri)<0){
								//suppression du fichier de l'index
								SOLRUtilities.getSOLRServer().deleteByQuery("file_uri:\""+uri.replace("\\","\\\\")+"\"");
							}else{
								//on décalle l'index du début de la prochaine recherche
								nextStart++;
							}
						}else{
							solrDoc.setField("file_uri", "FILEWATCHER_TO_DELETE");
							//suppression du fichier de l'index
							SOLRUtilities.getSOLRServer().deleteByQuery("file_uri:\"FILEWATCHER_TO_DELETE\"");
						}
					}
					SOLRUtilities.getSOLRServer().commit();
					SOLRUtilities.getSOLRServer().optimize();
					SOLRUtilities.getSOLRServer().commit();
				} catch (Exception e) {
					logger.fatal("cleanindex : ",e);
				}
			}
		} catch (SolrServerException e) {
			logger.fatal("cleanindex : ",e);
		}
		if(nextStart<nbResults){
			cleanIndex(cn,nextStart);
		}
		logger.debug("END Clean Index");
	}

	/**
	 * Execute the indexing process.<br>
	 * The process begin by a batch of document to suppress, and then continue with a batch of document to index.
	 */
	@Override
	protected void doWork() {
		int cpt;
		String sqlQuery;
		ResultSet rs;
		logger.debug("IndexFileProcess dowork");
		Connection cn=getConnection();
		try {
			if(!indexingBatch){
				indexingBatch=true;
				Date t=new Date();
				/******************************
				 * SUPPRESSING BATCH
				 ******************************/
				logger.debug("NEW SUPPRESSING BATCH "+t);
				//retrieve a batch of id of documents to suppress
				sqlQuery="select top "+nbFiles+" id_fichier from t_fichiers t1 "+
						"join t_sources t2 on t1.id_source=t2.id_source "+
						"where t1.flag="+Constant.TO_SUPPRESSED + " OR t2.flag="+Constant.TO_SUPPRESSED;
				rs=DBUtilities.executeQuery(cn,sqlQuery);
				cpt=0;
				while(rs.next()){
					idFileToIndex[cpt]=rs.getInt("id_fichier");
					cpt++;
				}
				//for each id of the batch, recover the uri of the file and suppress the file.
				for(int i=0;i<cpt;i++){
					suppressFileFromIndex(cn,idFileToIndex[i]);
				}
				SOLRUtilities.getSOLRServer().commit();

				/*******************************************
				 * INDEXING BATCH
				 *******************************************/
				logger.debug("NEW INDEXING BATCH "+t);
				//retrieve a batch of id of documents to index
				sqlQuery="select top "+nbFiles+" id_fichier from t_fichiers t1 " +
						"join t_sources t2 on t1.id_source=t2.id_source " +
						"where t1.flag="+Constant.TO_INDEX +" AND t2.flag="+Constant.TO_INDEX +
						" order by priority";
				rs=DBUtilities.executeQuery(cn,sqlQuery);
				cpt=0;
				while(rs.next()){
					idFileToIndex[cpt]=rs.getInt("id_fichier");
					cpt++;
				}

				//for each id of the batch, recover the data of the file and index the file.
				for(int i=0;i<cpt;i++){
					indexFile(cn,idFileToIndex[i]);
				}
				SOLRUtilities.getSOLRServer().commit();
				SOLRUtilities.getSOLRServer().optimize();
				SOLRUtilities.getSOLRServer().commit();
				indexingBatch=false;
				logger.debug("END INDEX BATCH "+t);
			}
		} catch(SQLException e) {
			logger.fatal("index file ",e);
			indexingBatch=false;
		} catch(Exception e) {
			logger.fatal("index file ",e);
			indexingBatch=false;
		}
		try {
			cn.close();
		} catch (SQLException e) {
			logger.fatal("ERROR closing connection",e);
		}
	}

	/**
	 * Initialize a ContentStreamUpdateRequest object.
	 * @param up object to initialize
	 * @throws IOException
	 * @throws SQLException
	 */
	private void initContentStream(Connection cn,ContentStreamUpdateRequest up,int idFichier, File fToIndex) throws IOException, SQLException{
		Date d;
		Calendar c=Calendar.getInstance();
		String titreDoc;
		String sqlQuery;
		ResultSet rs;

		up.addFile(fToIndex);

		sqlQuery="select t2.nom as sourceName,t2.chemin as sourceURI,t1.Titre_F,t1.author_F,t1.DATEEXTRACTED,t1.titre_doc from t_fichiers t1 "+
				"join t_sources t2 on t1.id_source=t2.id_source "+
				" where id_Fichier="+idFichier;
		rs=DBUtilities.executeQuery(cn,sqlQuery);
		if(rs.next()){
			up.setParam("literal.folder_name",rs.getString("sourceName"));
			up.setParam("literal.folder_uri",rs.getString("sourceURI"));
			up.setParam("literal.file_uri", fToIndex.getAbsolutePath());
			d=new Date(fToIndex.lastModified());
			c.setTime(d);
			up.setParam("literal.modif_date",c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH));

			if(rs.getString("Titre_F").equals("")){
				up.setParam("literal.titre_f","<no title>");				
			}else{
				up.setParam("literal.titre_f",rs.getString("Titre_F"));
			}
			up.setParam("literal.author_f",rs.getString("author_F"));
			up.setParam("literal.extract_date", rs.getString("DATEEXTRACTED"));		
			titreDoc=rs.getString("titre_doc");
			if(!titreDoc.trim().equals("")){
				up.setParam("literal.titre_doc",rs.getString("Titre_doc"));				
			}
		}else{
			logger.fatal("The file "+fToIndex.getAbsolutePath()+" doesn't exist in the data-base. Query: " +sqlQuery);
		}
		sqlQuery="select * from t_metas where id_fichier="+idFichier;
		rs=DBUtilities.executeQuery(cn,sqlQuery);
		while(rs.next()){
			up.setParam("literal."+rs.getString("nom"),rs.getString("value"));		
		}
	}
	/**
	 * Index the file fToIndex.<br>
	 * To index a file, the method create a ContentStreamUpdateRequest object with the url "/update/extract".<br>
	 * Then the object is initialized with initContentStream method, and sent to SOLR server. 
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	private void indexFile(Connection cn,int idFichier){
		String sqlQuery="";
		ResultSet rs;
		File fToIndex=null;
		ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");
		try{
			sqlQuery="start transaction";
			DBUtilities.executeQuery(cn,sqlQuery);

			sqlQuery="select chemin as fileURI from t_fichiers where id_Fichier="+idFichier;
			rs=DBUtilities.executeQuery(cn,sqlQuery);
			rs.next();
			fToIndex=new File(rs.getString("fileURI"));

			logger.debug("indexation fichier "+fToIndex.getAbsolutePath());
			initContentStream(cn,up,idFichier,fToIndex);
			//indexing the file
			SOLRUtilities.getSOLRServer().request(up);
			sqlQuery="update t_fichiers set "+
					"FLAG="+Constant.INDEXED+
					" where id_fichier="+idFichier;
			DBUtilities.executeQuery(cn,sqlQuery);
			cn.commit();
		} catch (SQLException e){
			logger.debug("ERROR index :"+sqlQuery,e);
			try {
				cn.rollback();
			} catch (SQLException e1) {
				logger.fatal("ERROR Rollback : "+sqlQuery,e1);
			}
		} catch (Exception e){
			if(e.getMessage().indexOf("tika")>0){
				logger.fatal("TIKA ERROR : "+ fToIndex.getPath());				
			}else{
				logger.fatal("Indexing ERROR : "+ fToIndex.getPath(),e);
			}

			try {
				sqlQuery="update t_fichiers set "+
						"FLAG="+Constant.INDEXED_ERROR+
						" where id_fichier="+idFichier;
				DBUtilities.executeQuery(cn,sqlQuery);
				cn.commit();
			} catch (SQLException e1) {
				logger.fatal(sqlQuery,e1);
				try {
					cn.rollback();
				} catch (SQLException e2) {
					logger.fatal("ERROR Rollback : "+sqlQuery,e2);
				}
			}
		}
		up=null;
	}
	/**
	 * Suppress a file from the index and from the data-base.<br>
	 * If a Source is empty (no file), the source is suppress from the data-base.<br>
	 * To suppress a source from the index, update the field flag to Constant.TO_SUPPRESS in the table T_sources. 
	 * The index process will suppress all files from the index and the source from the data-base.
	 * @param uri
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	private void suppressFileFromIndex(Connection cn,int idFichier ) throws SolrServerException, IOException{
		logger.debug("Début suppression "+idFichier);
		String sqlQuery="";
		ResultSet rs;
		String uri;
		int idSource;
		try {
			sqlQuery="start transaction";
			DBUtilities.executeQuery(cn,sqlQuery);

			//recupération id_source et du chemin
			sqlQuery="select id_source,chemin from t_fichiers "+
					" where id_fichier="+idFichier;
			rs=DBUtilities.executeQuery(cn,sqlQuery);
			if(rs.next()){
				idSource=rs.getInt("id_source");
				uri=rs.getString("chemin");
			}else{
				idSource=-1;
				uri="";
			}
			//suppression du fichier de l'index
			SOLRUtilities.getSOLRServer().deleteByQuery("file_uri:\""+uri.replace("\\","\\\\")+"\"");
			//suppression du fichier de la base
			sqlQuery="delete from T_metas where id_fichier="+idFichier;
			DBUtilities.executeQuery(cn,sqlQuery);
			sqlQuery="delete from t_fichiers where id_fichier="+idFichier;
			DBUtilities.executeQuery(cn,sqlQuery);
			//suppression de la source si plus de fichiers
			if(idSource!=-1){
				sqlQuery="select id_fichier from t_fichiers where id_source="+idSource;
				rs=DBUtilities.executeQuery(cn,sqlQuery);
				if(!rs.next()){//plus de fichier
					//on ne supprime la source que si flag=TO_SUPPRESSED
					sqlQuery="delete from t_sources where id_source="+idSource +" AND flag="+Constant.TO_SUPPRESSED; 
					DBUtilities.executeQuery(cn,sqlQuery);							
				}
			}
			cn.commit();
			logger.debug("Fin suppression "+uri);
		} catch (SQLException e) {
			logger.fatal("ERROR suppression: "+ sqlQuery,e);
			try {
				cn.rollback();
			} catch (SQLException e1) {
				logger.fatal("ERROR Rollback : "+sqlQuery,e1);
			}
		}
	}
}
