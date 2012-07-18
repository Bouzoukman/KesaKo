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
package kesako.search;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;

/**
 * This class represents a result item from a research. It is composed by the data necessary to display a result<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 *
 */
public class ResultDoc {
	/**
	 * Log4J logger of the class. 
	 */
	private static final Logger logger = Logger.getLogger(ResultDoc.class);
	/**
	 * Title of the document. A document can be composed by several files. Each file has a title and has the document title as meta-data.
	 */
	private String docTitle;
	/**
	 * Title of the file
	 */
	private String fileTitle;
	/**
	 * mime type of the file
	 */
	private String docType;
	/**
	 * author of the file
	 */
	private String author;
	/**
	 * score of the file regarding the research made
	 */
	private int score;
	/**
	 * date of the document
	 */
	private Date docDate;
	/**
	 * URI of the file
	 */
	private String fileURI;
	/**
	 * Name of the source of the file
	 */
	private String sourceName;
	/**
	 * highlight of the file regarding the research made
	 */
	private String highlight;
	/**
	 * Date format for string-date store in SOLR. The format is "EEE MMM dd HH:mm:ss z yyyy".
	 * @see SimpleDateFormat
	 */
	private static SimpleDateFormat SOLRformat=new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US); 
	/**
	 * Date format for a string-date store in the data-base. The format is "yyyy-MM-dd".
	 * @see SimpleDateFormat
	 */
	private static SimpleDateFormat DBformat=new SimpleDateFormat("yyyy-MM-dd"); 
	/**
	 * Constructor that takes a ResultSet object to construct a document object.
	 * @param rs ResultSet object
	 */
	public ResultDoc(ResultSet rs){
		highlight="";
		try{
			if(rs.getString("chemin")!=null){
				fileURI=rs.getString("chemin");						
			}else{
				fileURI="";
			}

			if(rs.getString("titre_f")!=null){
				fileTitle=rs.getString("titre_f");						
			}else{
				fileTitle="";
			}
			if(rs.getString("titre_doc")!=null){
				docTitle=rs.getString("titre_doc");						
			}else{
				docTitle="";
			}
			
			if(rs.getString("nom_source")!=null){
				sourceName=rs.getString("nom_source");						
			}else{
				sourceName="";
			}
			
			score=0;

			if(rs.getString("DateExtracted")!=null){
				docDate=DBformat.parse(rs.getString("DateExtracted"));						
			}else{
				docDate=DBformat.parse("1900-01-01");
			}

			docType="";
			if(rs.getString("author_f")!=null){
				author=rs.getString("author_f");						
			}else{
				author="";
			}
		} catch (ParseException e) {
			logger.fatal("doSearch",e);
		} catch (SQLException e) {
			logger.fatal("doSearch",e);
		}
		logger.debug(" Doc : "+fileTitle+" / "+docTitle+" - "+docType+" - "+author+" - "+docDate+" - "+score);
	}
	/**
	 * Constructor that takes a SolrDocument object to construct a document object. 
	 * @param doc SolrDocument object
	 */
	@SuppressWarnings("unchecked")
	public ResultDoc(SolrDocument doc){
		highlight="";
		try{
			if(doc.get("file_uri")!=null){
				fileURI=doc.get("file_uri").toString();						
			}else{
				fileURI="";
			}
			if(doc.get("titre_f")!=null){
				if(doc.get("titre_f") instanceof ArrayList<?>){
					fileTitle=((ArrayList<String>)doc.get("titre_f")).get(((ArrayList<String>)doc.get("titre_f")).size()-1);
				}else{
					fileTitle=doc.get("titre_f").toString();						
				}
			}else{
				fileTitle="";
			}
			if(doc.get("titre_doc")!=null){
				docTitle=doc.get("titre_doc").toString();						
			}else{
				docTitle="";
			}
			if(doc.get("folder_name")!=null){
				sourceName=doc.get("folder_name").toString();						
			}else{
				sourceName="";
			}
			if(doc.get("score")!=null){
				score=Float.valueOf((Float.valueOf(doc.get("score").toString())*100)).intValue();
			}else{
				score=0;
			}

			if(doc.get("extract_date")!=null){
				docDate=SOLRformat.parse(doc.get("extract_date").toString());						
			}else{
				docDate=SOLRformat.parse("Mon Jan 1 00:00:00 CEST 1900");
			}

			if(doc.get("content_type")!=null){
				if(doc.get("content_type") instanceof ArrayList<?>){
					docType=((ArrayList<String>)doc.get("content_type")).get(((ArrayList<String>)doc.get("content_type")).size()-1);						
				}else{
					docType=doc.get("content_type").toString();						
				}
				docType=docType.split("/")[docType.split("/").length-1];	
			}else{
				docType="";
			}
			if(doc.get("author_f")!=null){
				if(doc.get("author_f") instanceof ArrayList<?>){
					author=((ArrayList<String>)doc.get("author_f")).get(((ArrayList<String>)doc.get("author_f")).size()-1);
				}else{
					author=doc.get("author_f").toString();						
				}
			}else{
				author="";
			}
		} catch (ParseException e) {
			logger.fatal("doSearch",e);
		}
		logger.debug(" Doc : "+fileTitle+" / "+docTitle+" - "+docType+" - "+author+" - "+docDate+" - "+score);
	}
	/**
	 * Return the title of the document
	 */
	public String getDocTitle() {
		return docTitle;
	}
	/**
	 * Allow to set the title of the document. A document can be composed by several files. Each file has a title and has the document title as meta-data.
	 * @param docTitle String object that represent the title
	 */
	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}
	/**
	 * Return the title. of the file
	 */
	public String getFileTitle() {
		return fileTitle;
	}
	/**
	 * Allow to set the title of the file.
	 * @param fileTitle String object that represents the title of the file.
	 */
	public void setFileTitle(String fileTitle) {
		this.fileTitle = fileTitle;
	}
	/**
	 * Return the MIME type of the file
	 */
	public String getDocType() {
		return docType;
	}
	/**
	 * Allow to set the MIME type of the file
	 * @param docType String object that represents the MIME Type of the file
	 */
	public void setDocType(String docType) {
		this.docType = docType;
	}
	/**
	 * Return a String object that represents the author of the file.
	 */
	public String getAuthor() {
		return author;
	}
	/**
	 * Allow to set the Author of the file.
	 * @param author String object that represents the author of the file.
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	/**
	 * Return the result's score of the file.
	 */
	public int getScore() {
		return score;
	}
	/**
	 * Allow to set the result's score of the document.
	 * @param score integer value that represents the score of the file. 
	 */
	public void setScore(int score) {
		this.score = score;
	}
	/**
	 * Return a Date object that represents the date of the file.
	 */
	public Date getDocDate() {
		return docDate;
	}
	/**
	 * Allow to set the date of the file. <br>
	 * If the data comes from a data-base, use the DBformat object to parse the string into a Date object.<br>
	 * If the data comes from SOLR index, use the SOLRformat object to parse the string into a Date object.
	 * @param docDate Date object that represents the date of the file.
	 */
	public void setDocDate(Date docDate) {
		this.docDate = docDate;
	}
	/**
	 * Return a String object that represents the object.
	 */
	public String toString(){
		return(" Doc : "+fileTitle+" / "+docTitle+" - "+docType+" - "+author+" - "+docDate+" - "+score);
	}
	/**
	 * Return a String object that represents the URI of the file.
	 */
	public String getFileURI() {
		return fileURI;
	}
	/**
	 * Allow to set the URI of the file.
	 * @param fileURI String object that represents the URI of the file.
	 */
	public void setFileURI(String fileURI) {
		this.fileURI = fileURI;
	}
	/**
	 * Return the highlight of the file.
	 */
	public String getHighlight() {
		return highlight;
	}
	/**
	 * Return the source name of the file
	 */
	public String getSourceName() {
		return sourceName;
	}
	/**
	 * Allow to set the highlight of the file.
	 * @param highlight String object that represents the highlight of the file.
	 */
	public void setHighlight(String highlight) {
		String h=highlight.replaceAll("\\s+"," ");
		h=h.replaceAll("<", "< ");
		h=h.replaceAll(">", " >");
		h=h.replaceAll("< em >", "<span style=\"font-weight:bold; color:#A40E13\">");
		h=h.replaceAll("< /em >", "</span>");
		logger.debug("highlight : "+h);
		this.highlight = h;
	}
}
