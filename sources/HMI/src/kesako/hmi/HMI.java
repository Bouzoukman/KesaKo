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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import kesako.hmi.adminSource.AdminPanel;
import kesako.hmi.meta.AddMetaPanel;
import kesako.utilities.DBUtilities;
import kesako.utilities.ImageUtilities;
import kesako.utilities.Parameters;
import kesako.utilities.SOLRUtilities;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Implement the interface of Kes@Ko - 311F8.0<br>
 * The version number of the software is built as follow :
 * First number : month + year x 100 converted in Hexadecimal. Here 02+2012x100 = 201202 = 311F2<br>
 * Second number : the patch number converted in Hexadecimal. <br>
 * The class implements the Log4J logging system. 
 * @author Frederic SACHOT
 */
public class HMI extends JFrame{
	private static final long serialVersionUID = -8668407800902178469L;
	/**
	 * Log4J Logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(HMI.class);
	/**
	 * Version number of the software.
	 */
	public static final String VERSION="311F8.0";
	/**
	 * Panel for the research interface.
	 */
	private SearchPanel jpSearch;
	/**
	 * Panel for directories administration
	 */
	private AdminPanel jpAdminRepository;
	/**
	 * Panel for meta-data administration
	 */
	private AddMetaPanel jpAdminMeta;
	/**
	 * Scrollpane that show the different panels
	 */
	private JScrollPane contentZone;
	private JFrame me=this;
	
	public HMI (){
		super("Kes@Ko - "+VERSION);
		logger.debug("HMI Construction");
		this.setJMenuBar(menuIHM());
		this.setIconImage(ImageUtilities.getHmiImage());
		
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowListener() {
			//The frame is maximized when the software is launched.
			public void windowOpened(WindowEvent e) {
				setExtendedState(MAXIMIZED_BOTH);
			}
			//The software is closed when the window is closed
			public void windowClosing(WindowEvent e) {
				dispose();
				System.gc();
				System.exit(0);
			}
			public void windowClosed(WindowEvent e) {
			}
			public void windowIconified(WindowEvent e) {
			}
			public void windowDeiconified(WindowEvent e) {
			}
			public void windowActivated(WindowEvent e) {
			}
			public void windowDeactivated(WindowEvent e) {
			}
		});
		
		contentZone = new JScrollPane();
		this.getContentPane().add(contentZone);
 		
		jpAdminRepository = new AdminPanel(); 
		jpAdminMeta = new AddMetaPanel();
		jpSearch=new SearchPanel();
		contentZone.getViewport().setView(jpSearch);
		getRootPane().setDefaultButton(jpSearch.getSearchButton());
	}
	/**
	 * Build the Menu bar of the interface. 
	 * @return the JMenuBar object
	 */
	private JMenuBar menuIHM(){
		JMenuBar menu=new JMenuBar();
		//File menu. It contains Search Item, Admin Repository item and Meta admin item.
		JMenu mFichier=new JMenu("File");
		menu.add(mFichier);
		//Search item
		JMenuItem iSearch=new JMenuItem("Search");
		mFichier.add(iSearch);
		iSearch.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				contentZone.getViewport().setView(jpSearch);
				getRootPane().setDefaultButton(jpSearch.getSearchButton());
			}		
		});
		
		mFichier.addSeparator();
		//Admin repository item
		JMenuItem iAdmin=new JMenuItem("Admin Repository");
		mFichier.add(iAdmin);
		iAdmin.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				contentZone.getViewport().setView(jpAdminRepository);
				getRootPane().setDefaultButton(null);
			}
		});
		//Meta admin item
		JMenuItem iMeta=new JMenuItem("Add File Meta");
		mFichier.add(iMeta);
		iMeta.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				contentZone.getViewport().setView(jpAdminMeta);
				jpAdminMeta.drawInit();
				getRootPane().setDefaultButton(null);
			}		
		});
		//Help menu. It contains the About item.
		JMenu mHelp=new JMenu("Help");
		menu.add(mHelp);
		//About item.
		JMenuItem iAbout=new JMenuItem("About");
		mHelp.add(iAbout);
		iAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog fAbout=new About(me);
				fAbout.setVisible(true);
			}
		});
		return menu;
	}

	/**
	 * Main function. It launches the Kes@Ko interface.<br>
	 * The first thing to launch is the log4J configurator.
	 * Then the Parameters and Utilities objects are created.
	 * @param args not used
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure("log4j_HMI.xml");
		new Parameters("kesako.ini");
		new DBUtilities();
		new SOLRUtilities();
		new ImageUtilities();
		HMI f=new HMI();
		f.setSize(900, 500);
		f.setVisible(true);
	}
}
