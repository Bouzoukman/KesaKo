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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import kesako.utilities.Constant;
import kesako.utilities.DBUtilities;
import kesako.utilities.SOLRUtilities;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
/**
 * Update the file meta-data by reading the meta-data file < filename >.meta<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class MetaFileProcess extends IntervalDBWork{
	/**
	 * Log4J logger of the class
	 */
	private static final Logger logger = Logger.getLogger(MetaFileProcess.class);
	/**
	 * Format of dates stored in the index: "EEE MMM dd HH:mm:ss z yyyy"
	 */
	private static SimpleDateFormat formatIndex=new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US); 
	/**
	 * Format of dates stored in the data-base: "yyyy-MM-dd"
	 */
	private static SimpleDateFormat formatMETA=new SimpleDateFormat("yyyy-MM-dd"); 
	/**
	 * id of the file currently process
	 */
	private int idFichier;
	/**
	 * title of the file currently process
	 */
	private String title;
	/**
	 * author of the file currently process
	 */
	private String author;
	/**
	 * variable to build DocumentBuilder object that can read meta-file.
	 */
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	/**
	 * variable to store date
	 */
	private Date date;
	/**
	 * vector to store id of files to process in the batch.
	 */
	private int[] idFileToProcess;
	/**
	 * variable to indicate if a batch is running.
	 */
	private boolean isBatchProcessing=false;
	/**
	 * number files in a batch.
	 */
	private int nbFile;
	/**
	 * Constructor.
	 * @param intervalSeconds number of seconds between to process
	 * @param nbFile number of files in a batch
	 */
	public MetaFileProcess(int intervalSeconds,int nbFile) {
		super(intervalSeconds, "MetaFileProcess");
		this.nbFile=nbFile;
		idFileToProcess=new int[this.nbFile];
		//this.titreF=new ArrayList<String>();
		logger.debug("MetaFileProcess / "+intervalSeconds+" / "+nbFile);
	}
	/**
	 * Recover the meta-data of the file fileURI from the index.
	 * @param fileURI
	 * @throws SolrServerException
	 */
	private void getIndexMeta(String fileURI) throws SolrServerException{
		logger.debug("getIndexMeta");
		//titreF.clear();
		String query="file_uri:\""+fileURI.replace("\\", "\\\\")+"\"";
		NamedList<String> q = new NamedList<String>();
		q.add("fl", "title,titre_f,titre_doc,author,date");
		q.add("q", query);
		q.add("rows","1");
		q.add("start","0");
		logger.debug("query : "+q);
		QueryResponse r;
		SolrDocumentList listDoc;
		SolrDocument solrDoc;
		r = SOLRUtilities.getSOLRServer().query(SolrParams.toSolrParams(q));
		listDoc = r.getResults();
		if(listDoc!=null && listDoc.size()>0){
			solrDoc=listDoc.iterator().next();
			if(solrDoc.getFieldValue("title")!=null){
				title=solrDoc.getFieldValue("title").toString();
			}else{
				title="";
			}
			if(solrDoc.getFieldValue("author")!=null){
				author=solrDoc.getFieldValue("author").toString();
			}else{
				author="";
			}
			try {
				if(solrDoc.getFieldValue("date")!=null){
					date=formatIndex.parse(solrDoc.getFieldValue("date").toString());
				}else{
					date=null;
				}
			} catch (ParseException e) {
				logger.fatal("Extract meta-data",e);
			}
			/*
			if(solrDoc.getFieldValue("titre_f")!=null){
				if(solrDoc.getFieldValue("titre_f") instanceof ArrayList<?>){
					titreF.addAll((ArrayList<String>)solrDoc.getFieldValue("titre_f"));						
				}else{
					titreF.add(solrDoc.getFieldValue("titre_f").toString());
				}
			}*/
		}else{
			title="";
			author="";
			date=null;
		}
	}
	/**
	 * Recover the meta-data of the file from the meta-file<br>
	 * First step: all meta-data of the file are suppress from the data-base,<br>
	 * Second step: meta-data are recovered from the index with getIndexMeta<br>
	 * Third step: if the file has a meta-file, the meta-data are recovered from the meta-file.
	 * Final step: the meta-data are saved in the data-base and the file is marked to be reindexing.
	 * @param file 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParseException 
	 * @throws SolrServerException 
	 */
	private void metaProcessing(Connection cn,int idFichier) throws ParserConfigurationException, SAXException, IOException, ParseException, SolrServerException{
		File fileMeta;
		String nomFicMeta, nomMeta,valueMeta;
		String query="";
		ResultSet rs;
		DocumentBuilder db;
		Document doc;
		File file;
		//traitement des fichiers de meta-donnees
		/*
		 * La solution mise en oeuvre supprime toutes les valeurs de la table T_metas recrée toutes les metas. 
		 * Ceci permet de traiter en même temps la mise à jour et la suppression des metas puisque seule les metas existantes seront ajoutées.
		 */
		try {
			query="start transaction";
			DBUtilities.executeQuery(cn,query);
			//retrieve file 
			query="select chemin as fileURI from t_fichiers "+
					"where id_fichier="+idFichier; 
			rs=DBUtilities.executeQuery(cn,query);
			rs.next();
			file=new File(rs.getString("fileURI"));
			getIndexMeta(rs.getString("fileURI"));
			logger.debug("title="+title);
			logger.debug("author="+author);

			query="delete from t_metas where id_fichier="+idFichier;
			DBUtilities.executeQuery(cn,query);
			nomFicMeta=file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf('.'));
			nomFicMeta=nomFicMeta+".meta";
			logger.debug("nomFicMeta="+nomFicMeta);
			fileMeta=new File(nomFicMeta);
			logger.debug("fileMeta : "+fileMeta.getAbsolutePath());
			if(fileMeta.exists() && fileMeta.isFile()){
				logger.debug("fileMeta exist");
				query="update t_fichiers set date_meta_modified="+fileMeta.lastModified() +" where id_fichier="+idFichier;
				DBUtilities.executeQuery(cn,query);
				db = dbf.newDocumentBuilder();
				doc = db.parse(fileMeta);
				doc.getDocumentElement().normalize();
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
							logger.debug("nomMeta="+nomMeta+" , value="+valueMeta);
							if(nomMeta.equalsIgnoreCase("Titre_f")){
								if(!valueMeta.trim().equals("")){
									title=valueMeta.trim();
								}
							}else if(nomMeta.trim().equalsIgnoreCase("titre_doc")){
								query="update t_fichiers set titre_doc='"+DBUtilities.getStringSQL(valueMeta.trim())+"' where id_fichier="+idFichier;
								DBUtilities.executeQuery(cn,query);
							}else if(nomMeta.equalsIgnoreCase("author_f")){
								if(!valueMeta.trim().equals("")){
									author=valueMeta.trim();
								}
							}else if(nomMeta.equalsIgnoreCase("date")){
								if(!valueMeta.trim().equals("")){
									date=formatMETA.parse(valueMeta.trim());
								}
							}else if(!nomMeta.equalsIgnoreCase("nomFic")){
								query="insert into t_metas (nom,value,id_fichier) values ('"+
										DBUtilities.getStringSQL(nomMeta)+"','" + DBUtilities.getStringSQL(valueMeta)+"'," + idFichier+")";
								DBUtilities.executeQuery(cn,query);
							}
						}
					}
				}	
			}else{
				//no meta-file
			}
			if(!title.trim().equals("")){
				query="update t_fichiers set titre_f='"+DBUtilities.getStringSQL(title)+"' where id_fichier="+idFichier;
				DBUtilities.executeQuery(cn,query);
			}
			if(!author.trim().equals("")){
				query="update t_fichiers set author_f='"+DBUtilities.getStringSQL(author)+"' where id_fichier="+idFichier;
				DBUtilities.executeQuery(cn,query);
			}
			if(date!=null){
				Calendar c=Calendar.getInstance();
				c.setTime(date);
				query="update t_fichiers set dateextracted='"+c.get(Calendar.YEAR)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.DAY_OF_MONTH)+"' where id_fichier="+idFichier;
				DBUtilities.executeQuery(cn,query);
			}
			query="update t_fichiers set flag="+Constant.TO_INDEX+
					", flag_meta="+Constant.META_EXTRACTED+ 
					", priority="+Constant.PRIORITY_META_FILE+
					" where id_fichier="+idFichier;
			DBUtilities.executeQuery(cn,query);

			cn.commit();
		} catch (SQLException e) {
			logger.fatal("ERROR : "+ query,e);
			try {
				cn.rollback();
			} catch (SQLException e1) {
				logger.fatal("ERROR Rollback : "+query,e1);
			}
		}
	}
	/**
	 * Do the meta-data process.
	 */
	@Override
	protected void doWork() {
		logger.debug("MetaFileProcess dowork");
		String query="";
		ResultSet rs;
		Connection cn=getConnection();
		if(!isBatchProcessing){
			isBatchProcessing=true;
			Date t=new Date();
			logger.debug("NEW META-PROCESS BATCH "+t);
			int cpt=0;
			try {
				query="select top "+nbFile+" id_fichier from t_fichiers where flag_META="+Constant.TO_EXTRACT_META+" AND flag="+Constant.INDEXED;
				rs=DBUtilities.executeQuery(cn,query);
				while(rs.next()){
					idFileToProcess[cpt]=rs.getInt("id_fichier");
					cpt++;
				}
			} catch (SQLException e) {
				//if an error occurs, it is important to set cpt to 0 so that the metaProcessing is skipped. 
				cpt=0;
				logger.fatal("ERROR : "+query,e);
			}
			try {
				for(int i=0;i<cpt;i++){
					idFichier=idFileToProcess[i];
					metaProcessing(cn,idFileToProcess[i]);
				}
			} catch (Exception e) {
				isBatchProcessing=false;
				logger.fatal("Meta-Processing ",e);
				try {
				query="update t_fichiers set flag="+Constant.INDEXED_ERROR+
						", flag_meta="+Constant.ERROR_META+
						" where id_fichier="+idFichier;
					DBUtilities.executeQuery(cn,query);
				} catch (SQLException e1) {
					logger.fatal("ERROR : "+query,e1);
				}
			}
			isBatchProcessing=false;
			logger.debug("END META-PROCESS BATCH "+t);
		}else{
			logger.debug("Meta in progress");
		}
		try {
			cn.close();
		} catch (SQLException e) {
			logger.fatal("ERROR closing connection",e);
		}
	}
}
