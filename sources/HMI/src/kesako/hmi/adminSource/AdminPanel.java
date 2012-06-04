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
package kesako.hmi.adminSource;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import kesako.common.ResultDoc;
import kesako.utilities.Constant;
import kesako.utilities.DBUtilities;
import kesako.utilities.Parameters;

import org.apache.log4j.Logger;

public class AdminPanel extends JPanel{
	private static final long serialVersionUID = -8668407800902178469L;
	private static final Logger logger = Logger.getLogger(AdminPanel.class);
	private JPanel jpSource;
	private JLabel labSourcePath;
	private JTextField txtSourceName;
	private JList <String>indexErrorFileList;
	private Vector<ResultDoc> vFile;
	private JPanel jpListErrorDoc;
	private Connection cnDataBase=null;
	
	public AdminPanel (){
		logger.debug("Construction AdminPanel");
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if(cnDataBase!=null && !cnDataBase.isClosed()){
						cnDataBase.rollback();
						cnDataBase.close();
					}
				} catch (SQLException e) {
					logger.fatal("ERROR processing connection",e);
				}
			}
		});

		
		GridBagConstraints c=new GridBagConstraints();
		c.insets=new Insets(2,5,2,5);
		c.ipadx=5;
		c.ipady=5;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.gridheight=1;
		c.weightx=1;
		c.weighty=0;
		
		this.setLayout(new GridBagLayout());
		jpSource = new JPanel();
		
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=2;
		JPanel jpAdd=new JPanel();
		jpAdd.setLayout(new GridBagLayout());
		jpAdd.setBorder(new TitledBorder("Adding Source"));
		this.add(jpAdd,c);
		
		c.gridy=1;
		c.weightx=0;
		c.gridwidth=1;
		jpSource.setLayout(new BoxLayout(jpSource, BoxLayout.Y_AXIS));
		jpSource.setBorder(new TitledBorder("Sources list"));
		this.add(jpSource,c);	
		c.gridx=1;
		c.weightx=1;
		jpListErrorDoc = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jpListErrorDoc.setBorder(new TitledBorder("Index Error Files"));
		jpListErrorDoc.setBackground(Color.WHITE);
		vFile = new Vector<ResultDoc>();
		indexErrorFileList = new JList<String>(new FileListModel(vFile));
		indexErrorFileList.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {
				@SuppressWarnings("unchecked")
				JList<String> jL=(JList<String>)e.getSource();
				((FileListModel)indexErrorFileList.getModel()).openFile(jL.getSelectedIndex());
			}
			@Override
			public void mousePressed(MouseEvent e) {
			}
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
		});
		jpListErrorDoc.add(indexErrorFileList);
		this.add(jpListErrorDoc,c);

		c.gridy=2;
		c.gridx=0;
		c.weightx=1;
		c.fill=GridBagConstraints.BOTH;
		c.weighty=1;
		this.add(Box.createGlue(),c);

		//Box Add new Folder
		GridBagConstraints c2=new GridBagConstraints();
		c2.insets=new Insets(2,5,2,5);
		c2.ipadx=5;
		c2.ipady=5;
		c2.fill=GridBagConstraints.HORIZONTAL;
		c2.gridheight=1;
		c2.gridwidth=1;

		c2.gridx=0;
		c2.gridy=0;
		c2.weightx=0;
		c2.weighty=0;
		c2.fill=GridBagConstraints.NONE;
		jpAdd.add(new JLabel("Source Name : "),c2);
		c2.gridx=1;
		c2.gridwidth=3;
		c2.weightx=1;
		c2.fill=GridBagConstraints.HORIZONTAL;
		txtSourceName = new JTextField("< name of the new source >");
		txtSourceName.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				if(((JTextField)e.getSource()).getText().trim().equalsIgnoreCase("< name of the new source >")){
					((JTextField)e.getSource()).setText("");
				}				
			}
			@Override
			public void focusLost(FocusEvent e) {
				if(((JTextField)e.getSource()).getText().trim().equalsIgnoreCase("")){
					((JTextField)e.getSource()).setText("< name of the new source >");
				}
			}
		});
		jpAdd.add(txtSourceName,c2);

		c2.gridx=0;
		c2.gridy=1;
		c2.gridwidth=2;
		c2.weightx=1;
		c2.fill=GridBagConstraints.HORIZONTAL;
		labSourcePath = new JLabel("<Select a folder>");
		jpAdd.add(labSourcePath,c2);

		c2.gridx=2;
		c2.gridwidth=1;
		c2.weightx=0;
		c2.fill=GridBagConstraints.NONE;
		JButton bBrowse=new JButton("Browse");
		bBrowse.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				String separator=File.separator;
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    int returnVal = chooser.showOpenDialog((Component)e.getSource());
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	logger.debug("Sélection répertoire : "+chooser.getSelectedFile().getAbsolutePath());
			    	labSourcePath.setText(chooser.getSelectedFile().getAbsolutePath());
			    	if(txtSourceName.getText().trim().equalsIgnoreCase("< name of the new source >")){
			    		if(separator=="\\"){
			    			separator="\\\\";
			    		}
			    		String[] temp=chooser.getSelectedFile().getAbsolutePath().split(separator);
			    		txtSourceName.setText(temp[temp.length-1]);
			    	}
			    }			
			}
		});
		jpAdd.add(bBrowse,c2);

		c2.gridx=3;
		JButton bAddSource=new JButton("Add");
		bAddSource.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!txtSourceName.getText().trim().equalsIgnoreCase("< name of the new source >")&&!txtSourceName.getText().trim().equalsIgnoreCase("")){
					File dir=new File(labSourcePath.getText());
					if(dir.isDirectory()){
						logger.debug("createSourceXML");
						try {
							cnDataBase= DriverManager.getConnection(Parameters.getSqlConnectionString(), Parameters.getLogin(),Parameters.getPwd());
							DBUtilities.createSource(cnDataBase,txtSourceName.getText().trim(), labSourcePath.getText().trim());
							afficheListSource(cnDataBase);
					    	labSourcePath.setText("");						
							txtSourceName.setText("< name of the new source >");
							cnDataBase.close();
						} catch (SQLException e1) {
							logger.fatal(txtSourceName.getText().trim()+" | "+ labSourcePath.getText().trim(),e1);
						}
					}
				}
			}			
		});
		jpAdd.add(bAddSource,c2);
		try {
			cnDataBase= DriverManager.getConnection(Parameters.getSqlConnectionString(), Parameters.getLogin(),Parameters.getPwd());
			afficheListSource(cnDataBase);
			cnDataBase.close();
		} catch (SQLException e1) {
			logger.fatal("ERROR process connection",e1);
		}
	}

	private void afficheListSource(Connection cn){
		jpSource.removeAll();
		String sqlQuery="";
		try {
			sqlQuery="select * from t_sources order by nom";
			ResultSet rs=DBUtilities.executeQuery(cn,sqlQuery);
			int i=0;
			while(rs.next()){
				jpSource.add(new SourcePanel(rs,i%2,this));
				i++;				
			}
		} catch (SQLException e) {
			logger.fatal(sqlQuery,e);
		}
		jpSource.doLayout();
	}
	
	public void showFileList(Connection cn,int idSource){
		vFile.removeAllElements();
		String sqlQuery="";
		try {
			sqlQuery="lock table T_fichiers read, t_sources read";
			DBUtilities.executeQuery(cn, sqlQuery);
			sqlQuery="select chemin,titre_f,titre_doc,DateExtracted,author_f,t2.nom as nom_source from t_fichiers t1 join t_sources t2 on t1.id_source=t2.id_source where id_source="+idSource +" and flag ="+Constant.INDEXED_ERROR+" order by titre_f";
			ResultSet rs=DBUtilities.executeQuery(cn,sqlQuery);
			while (rs.next()){
				vFile.add(new ResultDoc(rs));
			}
			if(rs.last()){
				((TitledBorder)jpListErrorDoc.getBorder()).setTitle("Index Error Files of "+rs.getString("nom_source"));
			}else{
				((TitledBorder)jpListErrorDoc.getBorder()).setTitle("Index Error Files");
			}
			DBUtilities.executeQuery(cn, "commit");
		} catch (SQLException e) {
			logger.fatal(sqlQuery,e);
		}
		((FileListModel)indexErrorFileList.getModel()).updateData();
	}
}
