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

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Icon class. It's composed by an Icon object with a with of 25 pixels and a Tooltip.
 * @author Frederic SACHOT
 */
public class FileTypeIcon25 {
	/**
	 * the icon object with a width of 25 pixel
	 */
	private Icon icon;
	/**
	 * the tooltip
	 */
	private String toolTip;
	/**
	 * This constructor is initialized with an Image object and a string for the tooltip.
	 * @param image an Image object that will be transformed into Icon object with a width or 25.
	 * @param toolTip a string that will be used as tooltip.
	 */
	public FileTypeIcon25(Image image,String toolTip){
		this.icon=	new ImageIcon(image.getScaledInstance(25, -1, Image.SCALE_SMOOTH));
		this.toolTip=toolTip;
	}
	/**
	 * Return the Icon object
	 */
	public Icon getIcon() {
		return icon;
	}
	/**
	 * Return the tooltip string
	 */
	public String getToolTip() {
		return toolTip;
	}
}
