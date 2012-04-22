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

/**
 * Constants of Kes@Ko 
 * @author Frederic SACHOT
 * @version 311F2.0
 */
public class Constant {
	/**
	 * Constant priority for new files to index. <br>
	 * New files are indexed first, then files after meta-processing, then modified files, then files for which an indexing error occurred. 
	 */
	public static final int PRIORITY_NEW_FILE=0;
	/**
	 * Constant priority for files after meta-processing. <br>
	 * New files are indexed first, then files after meta-processing, then modified files, then files for which an indexing error occurred. 
	 */
	public static final int PRIORITY_META_FILE=1;
	/**
	 * Constant priority for modified files. <br>
	 * New files are indexed first, then files after meta-processing, then modified files, then files for which an indexing error occurred. 
	 */
	public static final int PRIORITY_MODIFIED_FILE=2;
	/**
	 * Constant priority for files for which an indexing error occurred. <br>
	 * New files are indexed first, then files after meta-processing, then modified files, then files for which an indexing error occurred. 
	 */
	public static final int PRIORITY_ERROR_FILE=3;
	/**
	 * Flag for file with indexing error
	 */
	public static final int INDEXED_ERROR=2;
	/**
	 * Flag for indexed file
	 */
	public static final int INDEXED=1;
	/**
	 * Flag for file or source to index
	 */
	public static final int TO_INDEX=0;
	/**
	 * Flag for file or source to suppress
	 */
	public static final int TO_SUPPRESSED=-1;
	/**
	 * Flag to extract meta-data (date)
	 */
	public static final int TO_EXTRACT_META=10;
	/**
	 * Flag when meta-data are extracted (date)
	 */
	public static final int META_EXTRACTED=11;
	/**
	 * Flag when an error occurred during extraction of meta-data
	 */
	public static final int ERROR_META=12;
}