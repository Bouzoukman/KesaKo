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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * File utilities.<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class FileUtilities {
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(FileUtilities.class);
	
	/**
	 *Return the execution path.
	 */	
	public static String getExecutionPath(){
		String path="Execution path:\n";
		File racine=new File(".");
		path+=racine.getAbsolutePath();
		logger.debug(path);
		return (path);
	}
	/**
	 * Return the extension of a file. The method works also with the absolute path of a file.
	 * @param filename name of a file
	 */
	public static String getExtension(String filename){
		String extension="";
		int i = filename.lastIndexOf('.');
		if(i>0 && i<filename.length()-1) {
			extension=filename.substring(i+1).toLowerCase();
		}
		return extension;
	}
	/**
	 * Return TRUE if the file can be indexed.
	 * @param f file to index
	 */
	public static boolean isValidExtansion(File f){
		if(f.isFile()){
			String extension=getExtension(f.getName().trim());
			if(extension.equals("pdf")){
				logger.debug("extension pdf OK ");
				return true;
			}
			if(extension.equals("htm")){
				logger.debug("extension htm OK ");
				return true;
			}
			if(extension.equals("html")){
				logger.debug("extension html OK ");
				return true;
			}
			if(extension.equals("doc")){
				logger.debug("extension doc OK ");
				return true;
			}
			if(extension.equals("docx")){
				logger.debug("extension docx OK ");
				return true;
			}
			if(extension.equals("ppt")){
				logger.debug("extension ppt OK ");
				return true;
			}
			if(extension.equals("pptx")){
				logger.debug("extension pptx OK ");
				return true;
			}
			if(extension.equals("pps")){
				logger.debug("extension pps OK ");
				return true;
			}
			if(extension.equals("xlsx")){
				logger.debug("extension xlsx OK ");
				return true;
			}
			if(extension.equals("xls")){
				logger.debug("extension xls OK ");
				return true;
			}
			if(extension.equals("txt")){
				logger.debug("extension txt OK ");
				return true;
			}
			if(extension.equals("odt")){
				logger.debug("extension odt OK ");
				return true;
			}
			if(extension.equals("ods")){
				logger.debug("extension ods OK ");
				return true;
			}
			if(extension.equals("odp")){
				logger.debug("extension odp OK ");
				return true;
			}
		}
		return false;
	}
	/**
	 * The meta-data of a file is stored in a file with the extension .meta. The meta-data file is a XML file.<br>
	 * The method return the name of the file of meta-data of an indexed file.
	 * @param nomFic name of an indexed file.
	 */
	public static String getFileMetaName(String nomFic){
		String nomFicMeta=nomFic.substring(0,nomFic.lastIndexOf('.'));
		return nomFicMeta=nomFicMeta+"_"+getExtension(nomFic)+".meta";
	}
	
	public static void openFile(String filePath){
		try {
			if(OSValidator.isWindows()){
				//two different methods to open file if the OS is Windows.
				Runtime.getRuntime().exec("cmd /c \""+filePath+"\"");
				//Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + data.get(row).getFileURI());
			}else{
				//if Windows, the open method doesn't open network file.
				Desktop.getDesktop().open(new File(filePath));
			}
		} catch (IOException e) {
			logger.fatal("Error open File : "+filePath,e);
		}
	}
}