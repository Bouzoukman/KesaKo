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

import java.awt.Image;
import java.awt.Toolkit;

/**
 * Utilitie's class to return all images needed by Kes@Ko.
 * @author Frederic SACHOT
 */
public class ImageUtilities {
	/**
	 * logo of Kes@Ko
	 */
	private static Image hmiImage;
	/**
	 * logo for msWord file.
	 */
	private static Image msWordImage;
	/**
	 * logo for text file.
	 */
	private static Image textImage;
	/**
	 * logo for HTML file.
	 */
	private static Image htmlImage;
	/**
	 * logo for PDF file.
	 */
	private static Image pdfImage;
	/**
	 * logo for unknown type file.
	 */
	private static Image unknownImage;
	/**
	 * Initialization of the Image objects with image in the directory resources. This directory is include in the JAR file of the Utilities JAR.
	 */
	public ImageUtilities(){
		hmiImage=Toolkit.getDefaultToolkit().getImage(getClass().getResource("/kesako/resources/red.jpg"));
		msWordImage=Toolkit.getDefaultToolkit().getImage(getClass().getResource("/kesako/resources/application-msword.png"));
		textImage=Toolkit.getDefaultToolkit().getImage(getClass().getResource("/kesako/resources/text-plain.png"));
		htmlImage=Toolkit.getDefaultToolkit().getImage(getClass().getResource("/kesako/resources/text-html.png"));
		pdfImage=Toolkit.getDefaultToolkit().getImage(getClass().getResource("/kesako/resources/application-pdf.png"));
		unknownImage=Toolkit.getDefaultToolkit().getImage(getClass().getResource("/kesako/resources/unknown.png"));
	}
	/**
	 * Return the logo of Kes@Ko
	 */
	public static Image getHmiImage() {
		return hmiImage;
	}
	/**
	 * Return the logo of msWord file
	 */
	public static Image getMsWordImage() {
		return msWordImage;
	}
	/**
	 * Return the logo of Text file
	 */
	public static Image getTextImage() {
		return textImage;
	}
	/**
	 * Return the logo of HTML file
	 */
	public static Image getHtmlImage() {
		return htmlImage;
	}
	/**
	 * Return the logo of PDF file
	 */
	public static Image getPdfImage() {
		return pdfImage;
	}
	/**
	 * Return the logo of unknown type file
	 */
	public static Image getUnknownImage() {
		return unknownImage;
	}


}
