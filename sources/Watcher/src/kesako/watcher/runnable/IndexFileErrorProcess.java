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
import java.sql.SQLException;

import kesako.utilities.Constant;
import kesako.utilities.DBUtilities;

import org.apache.log4j.Logger;

/**
 * Process that push files in index error to be re-indexed.<br>
 * To do this, simply update the field "Flag" in the table t_fichiers to TO_INDEX where the value of the field is INDEXE-ERROR. 
 * <br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class IndexFileErrorProcess extends IntervalDBWork{
	/**
	 * logger of the class;
	 */
	private static final Logger logger = Logger.getLogger(IndexFileErrorProcess.class);
	/**
	 * Constructor of the process that try to re-index files fore which an error occurs during the indexing process. 
	 * @param intervalSeconds
	 */
	public IndexFileErrorProcess(int intervalSeconds) {
		super(intervalSeconds, "IndexFileErrorProcess");
		logger.debug("IndexFileErrorProcess / "+intervalSeconds);
	}

	/**
	 * Change flag_meta to Constant.TO_EXTRACT_META and flag to Constant.TO_INDEX where flag = Constant.INDEXED_ERROR in the table T_fichiers.
	 * @throws SQLException 
	 */
	protected void doWork(){
		String query="";
		Connection cn=getConnection();
		try {
			query="start transaction";
			DBUtilities.executeQuery(cn,query);
			query="update t_fichiers set flag="+Constant.TO_INDEX+
					", flag_meta="+Constant.TO_EXTRACT_META+
					", priority="+Constant.PRIORITY_ERROR_FILE+
					" where flag="+Constant.INDEXED_ERROR +
					" OR flag_meta="+Constant.ERROR_META;
			logger.debug("index fichiers en erreur "+query);		
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
		try {
			cn.close();
		} catch (SQLException e) {
			logger.fatal("ERROR closing connection",e);
		}
	}
}
