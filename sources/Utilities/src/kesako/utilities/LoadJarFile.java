/*
 * This class is derived from the class ClasspathHacker developed by the Federal University of So Carlos under the license below.
 */
/************************************************************************************************** 
 * Copyright (c) 2004, Federal University of So Carlos                                            *
 *                                                                                                *
 * All rights reserved.                                                                           *
 *                                                                                                *
 * Redistribution and use in source and binary forms, with or without modification, are permitted *
 * provided that the following conditions are met:                                                *
 *                                                                                                *
 *     * Redistributions of source code must retain the above copyright notice, this list of      *
 *       conditions and the following disclaimer.                                                 *
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of   *
 *     * conditions and the following disclaimer in the documentation and/or other materials      *
 *     * provided with the distribution.                                                          *
 *     * Neither the name of the Federal University of So Carlos nor the names of its             *
 *     * contributors may be used to endorse or promote products derived from this software       *
 *     * without specific prior written permission.                                               *
 *                                                                                                *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS                            *
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT                              *
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR                          *
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR                  *
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,                          *
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,                            *
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR                             *
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF                         *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING                           *
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS                             *
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                   *
 **************************************************************************************************/
package kesako.utilities;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

/**
 * Useful class for dynamically loading jar file during runtime.<br>
 * The class implements the Log4J logging system.
 * @author Frederic SACHOT
 */
public class LoadJarFile {
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(LoadJarFile.class);
	/**
	 * Parameters of the method to add an URL to the System classes. 
	 */
	private static final Class<?>[] parameters = new Class[]{URL.class};

	/**
	 * Add a file to the classpath.
	 * @param path path of the file to add
	 */
	public static void loadFile(String path){
		File f = new File(path);
		try {
			loadURL(f.toURI().toURL());
		} catch (MalformedURLException e) {
			logger.fatal("The file "+path+" doesn't have a well-formed URL");
		}
	}

	/**
	 * Add the content pointed by the URL to the classpath.
	 * @param u the URL pointing to the content to be added
	 */
	public static void loadURL(URL u){
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;
		Method method;
		try {
			method = sysclass.getDeclaredMethod("addURL",parameters);
			method.setAccessible(true);
			method.invoke(sysloader,new Object[]{ u }); 
		} catch (Exception e) {
			logger.fatal("Unnable to load URL "+u.toString(),e);
		}
	}
}

