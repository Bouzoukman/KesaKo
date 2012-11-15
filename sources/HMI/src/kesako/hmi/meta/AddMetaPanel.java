/*
 * Copyright 2012 Frédéric SACHOT
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
package kesako.hmi.meta;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import kesako.utilities.DBUtilities;
import kesako.utilities.FileUtilities;

import org.apache.log4j.Logger;

public class AddMetaPanel extends JPanel{
	private static final long serialVersionUID = -8668407800902178469L;
	private static final Logger logger = Logger.getLogger(AddMetaPanel.class);
	/**
	 * Choose file button
	 */
	private JPanel jpChooseFile;
	int nbFile=0;
	private File initialDirectory;
	/**
	 * List of FileMetaPanel
	 */
	Vector<FileMetaPanel>vFileMeta;

	private JList<String> listValues;
	private Vector<String> metaValues;
	private MetaPanel selectedMetaPanel;
	
	private AddMetaPanel me;
	private JPanel jpFileMeta;
	
	private JPanel jpMeta;

	public AddMetaPanel (){
		logger.debug("Construction AddMetaPanel");
		this.setLayout(new BorderLayout());
		this.initialDirectory=null;
		me=this;

		vFileMeta=new Vector<FileMetaPanel>();
		metaValues = new Vector<String>();
		listValues = new JList<String>(new ListMetaModel(metaValues));
		listValues.setBorder(new TitledBorder("Proposed value"));
		listValues.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {
				@SuppressWarnings("unchecked")
				JList<String> jL=(JList<String>)e.getSource();
				String temp = selectedMetaPanel.getMetaValue().trim();
				if(jL.getSelectedValue()!=null && !temp.toLowerCase().contains(jL.getSelectedValue().toLowerCase())){
					if(temp.equals("")){
						selectedMetaPanel.setMetaValue(jL.getSelectedValue());
					}else{
						selectedMetaPanel.setMetaValue(temp+", "+jL.getSelectedValue());
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}				
		});

		GridBagConstraints cMeta=new GridBagConstraints();
		cMeta.insets=new Insets(2,5,2,5);
		cMeta.ipadx=5;
		cMeta.ipady=5;
		cMeta.fill=GridBagConstraints.BOTH;
		cMeta.anchor=GridBagConstraints.CENTER;
		cMeta.gridheight=1;
		cMeta.gridwidth=1;
		cMeta.weightx=1;
		cMeta.weighty=0;

		//Meta list construction
		jpMeta=new JPanel();
		jpMeta.setLayout(new GridBagLayout());
		jpFileMeta = new JPanel();
		jpFileMeta.setLayout(new BoxLayout(jpFileMeta,BoxLayout.Y_AXIS));
		
		cMeta.gridx=0;
		cMeta.gridy=0;
		cMeta.weightx=0.5;
		cMeta.weighty=0;
		cMeta.gridheight=1;
		cMeta.gridwidth=2;			
		jpMeta.add(jpFileMeta,cMeta);
		
		cMeta.gridx=2;
		cMeta.gridy=0;
		cMeta.gridheight=3;
		cMeta.gridwidth=1;			
		cMeta.weighty=1;
		jpMeta.add(listValues,cMeta);
		
		cMeta.gridx=0;
		cMeta.gridy=1;
		cMeta.fill=GridBagConstraints.NONE;
		cMeta.weightx=0.25;
		cMeta.weighty=0;
		cMeta.gridheight=1;
		cMeta.gridwidth=1;			
		JButton bAddFile = new JButton("Add New File");
		jpMeta.add(bAddFile,cMeta);
		bAddFile.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				FileMetaPanel fMeta;
				boolean showAllButton=false;
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setMultiSelectionEnabled(true);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.addChoosableFileFilter(new FileFilter(){
					@Override
					public boolean accept(File f) {
						boolean test;
						test=DBUtilities.isAccetableFile(f);
						if(!f.isDirectory()){
							test=test&&FileUtilities.isValidExtansion(f);
						}
						return test;
					}
					@Override
					public String getDescription() {
						return "Indexable File";
					}
				});
				chooser.setCurrentDirectory(initialDirectory);
				int returnVal = chooser.showOpenDialog((Component)e.getSource());
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					initialDirectory=chooser.getCurrentDirectory();
					for(int i=0;i<chooser.getSelectedFiles().length;i++){
						logger.debug("Sélection fichier : "+chooser.getSelectedFiles()[i].getAbsolutePath());
						//show the button All only if this is the first file and if the number of file > 1
						showAllButton= (i==0 && chooser.getSelectedFiles().length>1);
						fMeta=new FileMetaPanel(chooser.getSelectedFiles()[i],me,nbFile%2,showAllButton);							
						vFileMeta.add(fMeta);
						jpFileMeta.add(fMeta);
						fMeta.initMeta();
						nbFile++;
					}
					add(jpMeta,BorderLayout.CENTER);
					SwingUtilities.invokeLater(new Runnable(){
						@Override
						public void run() {
							paintAll(getGraphics());
						}
					});
				}			
			}	
		});

		cMeta.gridx=1;
		cMeta.gridy=1;
		cMeta.fill=GridBagConstraints.NONE;
		JButton bSave = new JButton("Save");
		jpMeta.add(bSave,cMeta);
		bSave.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int i=0;i<vFileMeta.size();i++){
					vFileMeta.get(i).saveMetaXML();
				}
				drawInit();
			}
		});
		
		cMeta.gridx=0;
		cMeta.gridy=2;
		cMeta.gridwidth=2;			
		cMeta.weighty=1;
		jpMeta.add(new JPanel());
		

		JButton bChooseFile = new JButton("Choose File");
		jpChooseFile=new JPanel();
		jpChooseFile.add(bChooseFile);
		bChooseFile.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				FileMetaPanel fMeta;
				boolean showAllButton=false;
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setMultiSelectionEnabled(true);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.addChoosableFileFilter(new FileFilter(){
					@Override
					public boolean accept(File f) {
						boolean test;
						test=DBUtilities.isAccetableFile(f);
						if(!f.isDirectory()){
							test=test&&FileUtilities.isValidExtansion(f);
						}
						return test;
					}
					@Override
					public String getDescription() {
						return "Indexable File";
					}
				});
				chooser.setCurrentDirectory(initialDirectory);
				int returnVal = chooser.showOpenDialog((Component)e.getSource());
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					vFileMeta.removeAllElements();
					jpFileMeta.removeAll();
					nbFile=0;
					initialDirectory=chooser.getCurrentDirectory();
					for(int i=0;i<chooser.getSelectedFiles().length;i++){
						logger.debug("Sélection fichier : "+chooser.getSelectedFiles()[i].getAbsolutePath());
						//show the button All only if this is the first file and if the number of file > 1
						showAllButton= (i==0 && chooser.getSelectedFiles().length>1);
						fMeta=new FileMetaPanel(chooser.getSelectedFiles()[i],me,nbFile%2,showAllButton);							
						vFileMeta.add(fMeta);
						jpFileMeta.add(fMeta);
						fMeta.initMeta();
						nbFile++;
					}
					removeAll();
					add(jpMeta,BorderLayout.CENTER);
					SwingUtilities.invokeLater(new Runnable(){
						@Override
						public void run() {
							paintAll(getGraphics());
						}
					});
				}			
			}	
		});
	}

	public void drawInit(){
		//définition de l'interface
		logger.debug("Draw Init");
		this.removeAll();
		this.add(jpChooseFile,BorderLayout.NORTH);
		this.paintAll(this.getGraphics());
	}


	public JList<String> getListValues() {
		return listValues;
	}

	public Vector<String> getMetaValues() {
		return metaValues;
	}

	public void setSelectedMetaPanel(MetaPanel selectedMetaPanel) {
		this.selectedMetaPanel = selectedMetaPanel;
	}

	/**
	 * @return the vFileMeta
	 */
	public Vector<FileMetaPanel> getvFileMeta() {
		return this.vFileMeta;
	}
}
