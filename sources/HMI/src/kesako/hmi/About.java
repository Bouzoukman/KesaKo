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
package kesako.hmi;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;

import kesako.utilities.ImageUtilities;

/**
 * Implement the about frame.
 * @author Frederic SACHOT
 */
public class About extends JDialog {
	private static final long serialVersionUID = -7118942548540334283L;
	
	public About(JFrame owner){
		super(owner,"About Kes@Ko - "+HMI.VERSION);
		setSize(500,400);
		//setAlwaysOnTop(true);
		this.setIconImage(ImageUtilities.getHmiImage());
		setResizable(false);
		setLocation(200,200);
		setLayout(new BorderLayout());
		add(new JLabel(new ImageIcon(ImageUtilities.getHmiImage())),BorderLayout.WEST);
		JEditorPane text=new JEditorPane();
		text.setContentType("text/html");
		text.setText("<p style=\"text-decoration:underline;font-weight:bold; color:blue\">Kes@Ko - "+HMI.VERSION+"</p>" +
				"<p>Kes@Ko is distributed under licence Apache 2.0</p>" +
				"<p>Kes@Ko is using HSQLDB, Jetty, and SOLR<br>" +
				"<span style=\"text-decoration:underline;\">HSQLDB</span> is distributed under licence HSQLDB<br>" +
				"<span style=\"text-decoration:underline;\">Jetty</span> is distributed under licence Eclipse<br>" +
				"<span style=\"text-decoration:underline;\">SOLR</span> is distributed under licence Apache 2.0</p>" +
				"<p>Kes@Ko logo is distributed under Creative Common licence</p>" +
				"<p>Icons used in Kes@Ko comme from Faenza project.<br>" +
				"Faenza is designed and developed by Matthieu James: matthieu.james@gmail.com.<br>"+
				"Faenza icons are all licensed under the GPL.</p>" +
				"<p>The source code can be found at: https://github.com/Bouzoukman/KesaKo</p>");
		add(text,BorderLayout.CENTER);
	}
}
