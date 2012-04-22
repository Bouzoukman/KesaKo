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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kesako.utilities.SOLRUtilities;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

/**
 * Implement the facet-search mechanism.<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class FacetSearch {
	/**
	 * Log4J logger of the class
	 */
	private static final Logger logger = Logger.getLogger(FacetSearch.class);
	/**
	 * Vector that stores the results of the facet data
	 */
	private Map<String,Long> vData;
	/**
	 * name of the meta-data corresponding to the facet
	 */
	private String metaDataName;
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
	 * Constructor of the FacetSearch object
	 * @param metaDataName name of the meta-data corresponding to the facet
	 */
	public FacetSearch(String metaDataName){
		vData=new HashMap<String,Long>();
		this.metaDataName=metaDataName;
	}
	
	/**
	 * Execute the research and update the data
	 * @param query query of the research
	 * @return a flag that indicates the status.<br>
	 * <ul>
	 * <li>RESULTS: indicates that there is at least one result</li>
	 * <li>NO_RESULT: indicates that there is no result.</li>
	 * <li>RESULT_ERROR: indicates that an error occurred during the search.</li>
	 * </ul>
	 */
	public int doSearch(String query){
		NamedList<String> q = new NamedList<String>();
		q.add("fl", "file_uri");
		if(query.trim().equals("")||query.trim().equalsIgnoreCase("AllDoc")){
			q.add("q","*:*" );
		}else{
			q.add("q",query.trim());
		}
		q.add("rows", "1");
		q.add("start", "0");
		q.add("facet","true");
		q.add("facet.field",metaDataName);

		logger.debug("query : "+q);
		QueryResponse r;
		String itemName;
		long facetCount;
		try {
			r = SOLRUtilities.getSOLRServer().query(SolrParams.toSolrParams(q));
			List<Count> lFI=r.getFacetFields().get(0).getValues();
			if(lFI!=null){
				for(int i=0;i<lFI.size();i++){
					logger.debug(lFI.get(i).getName()+" : "+lFI.get(i).getCount());
					itemName=lFI.get(i).getName();
					facetCount=lFI.get(i).getCount();
					vData.put(itemName, new Long(facetCount));
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
	 * Return the data of the research.
	 */
	public Map<String,Long> getData() {
		return vData;
	}
}
