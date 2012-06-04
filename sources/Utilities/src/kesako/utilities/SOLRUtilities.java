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

import java.io.IOException;
import java.net.MalformedURLException;


import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;


/**
 * SOLR utilities for Kes@Ko.<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class SOLRUtilities {
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(SOLRUtilities.class);
	/**
	 * SOLR indexing server
	 */
	private static SolrServer SOLRserver;

	/**
	 * This class initialize the SOLR indexing server object
	 */
	public SOLRUtilities(){
		logger.debug("Create SOLR Utilities");
		String url = Parameters.getSolrURL();
		logger.debug("SOLR URL : "+url);
		try {
			SOLRserver = new CommonsHttpSolrServer(url);
			logger.debug(SOLRserver.ping());
		} catch (MalformedURLException e) {
			logger.fatal(e);
		} catch (SolrServerException e) {
			logger.fatal(e);
		} catch (IOException e) {
			logger.fatal(e);
		}
	}

	/**
	 * Return a reference to the SOLR indexing server.
	 */
	public static SolrServer getSOLRServer() {
		return SOLRserver;
	}
}