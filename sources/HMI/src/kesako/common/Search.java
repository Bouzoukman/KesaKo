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


import java.util.List;
import java.util.Map;
import java.util.Vector;

import kesako.utilities.SOLRUtilities;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

/**
 * Implement the search mechanism.<br>
 * To make a search, use the method doSearch. This method return the status of the search (RESULTS, NO_RESULTS, RESULT_ERROR)<br>
 * To access the results of the search use the method getDocs<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class Search {
	/**
	 * Log4J logger of the class
	 */
	private static final Logger logger = Logger.getLogger(Search.class);
	/**
	 * Vector that stores the results of the research
	 */
	private Vector<ResultDoc> vDoc;
	/**
	 * Number of documents found by the research.
	 */
	private long nbFound;
	/**
	 * Constant that indicates that there is at least one results
	 */
	public static final int RESULTS=1;
	/**
	 * Constant that indicates that there is no result
	 */
	public static final int NO_RESULT=0;
	/**
	 * Constant that indicates that an error occurred during the search.
	 */
	public static final int RESULT_ERROR=-1;

	/**
	 * Constructor of the class that initialize the vDoc object.
	 */
	public Search(){
		vDoc=new Vector<ResultDoc>();
	}
	/**
	 * Make a research. Return the status of the search and store the results in vDoc.<br>
	 * The syntax of the different parameters is the SOLR syntax.
	 * @param query query-string of the research
	 * @param sortingString string to sort results. If sortingString is empty or null, the default sort order (by date) is done. 
	 * @param filter implementation of the facet mechanism
	 * @param start The method doSearch return 20 results beginning from start to start+20 or nbFound if nbFound < start+20
	 * @return RESULTS if there is results, NO_RESULT if there is no result, RESULT_ERROR if an error occurs during the search
	 */
	public int doSearch(String query,String filter, String sortingString,int start){
		NamedList<String> q = new NamedList<String>();
		String sortingString2;
		if(query.trim().equals("")||query.trim().equalsIgnoreCase("AllDoc")){
			q.add("q","*:*");
		}else{
			q.add("q", query);
		}
		q.add("fl", "*,score");
		q.add("rows", "20");
		q.add("start", Integer.toString(start));
		q.add("hl", "on");
		q.add("hl.fl","text");
		sortingString2=sortingString.trim();
		if(!sortingString2.equals("")){
			q.add("sort", sortingString2);
		}else{
			q.add("sort", "score desc");
		}
		if(!filter.trim().equals("")){
			q.add("fq",filter.trim());
		}
		/*
		 * liste des champs à afficher :
		 * titre_f : arrayList
		 * titre_doc :string
		 * score : float
		 * extract_date : date
		 * content_type : arraylist
		 * author :string
		 */
		logger.debug("query : "+q);
		QueryResponse r;
		SolrDocumentList listDoc;
		ResultDoc rDoc;
		try {
			r = SOLRUtilities.getSOLRServer().query(SolrParams.toSolrParams(q));
			listDoc=r.getResults();
			nbFound=listDoc.getNumFound();
			logger.debug("Nb Results : "+nbFound);
			Map<String,Map<String,List<String>>> listHighlight=r.getHighlighting();
			vDoc.removeAllElements();
			if(listDoc.size()>0){
				for(int i=0; i<listDoc.size();i++){
					rDoc=new ResultDoc(listDoc.get(i));
					if(listHighlight.get(rDoc.getFileURI()).get("text")!=null){
						logger.debug("Highlight : "+listHighlight.get(rDoc.getFileURI()).get("text").get(0));
						rDoc.setHighlight(listHighlight.get(rDoc.getFileURI()).get("text").get(0));
					}else{
						logger.debug("Highlight : No Highlight");
						rDoc.setHighlight("");
					}
					vDoc.add(rDoc);
				}
			}else{
				return NO_RESULT;
			}
		} catch (SolrServerException e) {
			logger.fatal("doSearch",e);
			return RESULT_ERROR;
		}
		return RESULTS;
	}
	/**
	 * Return the list of results
	 */
	public Vector<ResultDoc> getDocs() {
		return vDoc;
	}
	/**
	 * Return the number of results
	 */
	public long getNbFound() {
		return nbFound;
	}

}
