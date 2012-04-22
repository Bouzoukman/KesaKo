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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import kesako.utilities.Constant;
import kesako.utilities.DBUtilities;
import kesako.utilities.Parameters;

import org.apache.log4j.Logger;

public class SourcePanel extends JPanel{
	private static final long serialVersionUID = -2103547087341587343L;
	private static final Logger logger = Logger.getLogger(SourcePanel.class);
	private JLabel labName;
	private JLabel labPath;
	private int idSource;
	private String name;
	private String nbFile;
	private String nbIndex;
	private AdminPanel content;
	private Connection cnDataBase=null;

	public SourcePanel(ResultSet rs,int flag,AdminPanel conteneur){
		String path,sqlQuery;
		int isIndex;
		ResultSet rs2;
		
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

		content=conteneur;
		if(flag==1){
			this.setBackground(Color.WHITE);
		}
		if(rs!=null){
			try {
				if(cnDataBase==null || cnDataBase.isClosed()){
					cnDataBase= DriverManager.getConnection(Parameters.getSqlConnectionString(), Parameters.getLogin(),Parameters.getPwd());
				}
				idSource=rs.getInt("id_source");
				name=rs.getString("nom");
				path=rs.getString("chemin");
				isIndex=rs.getInt("flag");
				sqlQuery="select count(*) as nb_file from t_fichiers where id_source="+idSource;
				rs2=DBUtilities.executeQuery(cnDataBase,sqlQuery);
				if(rs2.next()){
					nbFile=rs2.getString("nb_file");
				}else{
					nbFile="0";
				}

				sqlQuery="select count(*) as nb_file from t_fichiers where id_source="+idSource+
						" and (flag="+Constant.INDEXED+" OR flag_meta="+Constant.META_EXTRACTED+")";
				rs2=DBUtilities.executeQuery(cnDataBase,sqlQuery);
				if(rs2.next()){
					nbIndex=rs2.getString("nb_file");
				}else{
					nbIndex="0";
				}
				cnDataBase.close();
			} catch (SQLException e) {
				logger.fatal("SourcePanel",e);
				name="Error";
				path=e.getMessage();
				isIndex=Constant.TO_SUPPRESSED;			
				nbFile="0";
				nbIndex="0";
			}
		}else{
			name="No Source";
			path=" ";
			isIndex=Constant.TO_SUPPRESSED;			
			nbFile="0";
			nbIndex="0";
		}
		
		GridBagConstraints c=new GridBagConstraints();
		c.insets=new Insets(2,5,2,5);
		c.ipadx=5;
		c.ipady=5;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridheight=1;
		c.gridwidth=1;
		c.weightx=1;
		c.weighty=0;

		c.gridx=0;
		c.gridy=0;
		this.setLayout(new GridBagLayout());
		labName = new JLabel(name+" ("+nbIndex+" / "+nbFile+")");
		labName.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if(cnDataBase==null || cnDataBase.isClosed()){
						cnDataBase= DriverManager.getConnection(Parameters.getSqlConnectionString(), Parameters.getLogin(),Parameters.getPwd());
					}
					content.showFileList(cnDataBase,idSource);
					cnDataBase.close();
				} catch (SQLException e1) {
					logger.fatal("ERROR connection",e1);
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
			}
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				labName.setBorder(new LineBorder(Color.BLACK));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				labName.setBorder(null);
			}			
		});

		if(isIndex==Constant.TO_SUPPRESSED){
			labName.setForeground(Color.RED);
		}
		this.add(labName,c);

		c.gridx=0;
		c.gridy=1;
		c.weightx=0;
		labPath = new JLabel(path);
		if(isIndex==Constant.TO_SUPPRESSED){
			labPath.setForeground(Color.RED);
		}
		this.add(labPath,c);
		/*
		c.weightx=1;
		//this.add(Box.createHorizontalGlue(),c);
		 */
		c.fill=GridBagConstraints.NONE;
		c.weightx=0;
		c.gridheight=2;
		c.gridx=1;
		c.gridy=0;
		JButton bIndexAll=new JButton("Index All");
		bIndexAll.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String sqlQuery="";
				try {
					if(cnDataBase==null || cnDataBase.isClosed()){
						cnDataBase= DriverManager.getConnection(Parameters.getSqlConnectionString(), Parameters.getLogin(),Parameters.getPwd());
					}
					sqlQuery="update t_fichiers set flag="+Constant.TO_INDEX+" ,flag_meta="+Constant.TO_EXTRACT_META+
							" where id_source="+idSource;					
					DBUtilities.executeQuery(cnDataBase,sqlQuery);
					cnDataBase.close();
				} catch (SQLException e1) {
					logger.fatal(sqlQuery,e1);
				}
				labName.setText(name+" (0 / "+nbFile+")");
			}

		});
		this.add(bIndexAll,c);
		c.gridx=2;	
		JButton bDel=new JButton("Delete");
		if(isIndex==Constant.TO_SUPPRESSED){
			bDel.setEnabled(false);
		}else{
			bDel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if(cnDataBase==null || cnDataBase.isClosed()){
							cnDataBase= DriverManager.getConnection(Parameters.getSqlConnectionString(), Parameters.getLogin(),Parameters.getPwd());
						}
						DBUtilities.updateSource(cnDataBase,idSource,name,Constant.TO_SUPPRESSED);
						labName.setForeground(Color.RED);
						labPath.setForeground(Color.RED);
						cnDataBase.close();
					} catch (SQLException e1) {
						logger.fatal("Pb in updating source "+idSource,e1);
						labName.setForeground(Color.RED);
						labPath.setForeground(Color.RED);
					}
				}
			});
		}
		this.add(bDel,c);
		this.addAncestorListener(new AncestorListener(){
			@Override
			public void ancestorAdded(AncestorEvent arg0) {
				String sqlQuery="";
				try {
					if(cnDataBase==null || cnDataBase.isClosed()){
						cnDataBase= DriverManager.getConnection(Parameters.getSqlConnectionString(), Parameters.getLogin(),Parameters.getPwd());
					}
					sqlQuery="select count(*) as nb_file from t_fichiers where id_source="+idSource;
					ResultSet rs2=DBUtilities.executeQuery(cnDataBase,sqlQuery);
					if(rs2.next()){
						nbFile=rs2.getString("nb_file");
					}else{
						nbFile="0";
					}

					sqlQuery="select count(*) as nb_file from t_fichiers where id_source="+idSource+
							" and (flag="+Constant.INDEXED+" OR flag_meta="+Constant.META_EXTRACTED+")";
					rs2=DBUtilities.executeQuery(cnDataBase,sqlQuery);
					if(rs2.next()){
						nbIndex=rs2.getString("nb_file");
					}else{
						nbIndex="0";
					}
					cnDataBase.close();
				} catch (SQLException e) {
					nbFile="0";
					nbIndex="0";
					labPath.setText(e.getMessage());
					logger.fatal(sqlQuery,e);
				}
				labName.setText(name+" ("+nbIndex+" / "+nbFile+")");
			}

			@Override
			public void ancestorMoved(AncestorEvent arg0) {
			}
			@Override
			public void ancestorRemoved(AncestorEvent arg0) {
			}
		});
	}
}
