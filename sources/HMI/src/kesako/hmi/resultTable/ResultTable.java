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
package kesako.hmi.resultTable;



import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;

import kesako.common.ResultDoc;
import kesako.hmi.SearchPanel;

import org.apache.log4j.Logger;


public class ResultTable extends JTable {
	private static final long serialVersionUID = 757084680349975000L;
	private static final Logger logger = Logger.getLogger(ResultTable.class);
	private SearchPanel searchPanel;

	public ResultTable(Vector<ResultDoc> data,SearchPanel sp){
		this.searchPanel=sp;
		this.setOpaque(true);
		this.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.setModel(new ResultTableModel(data));
		this.setRowHeight(80);
		ResultTableCellRenderer myCellRenderer=new ResultTableCellRenderer();
		ResultTableHeaderRenderer myHeaderRenderer=new ResultTableHeaderRenderer();
		for(int i=0;i<this.getColumnCount();i++){
			this.getColumnModel().getColumn(i).setCellRenderer(myCellRenderer);
			this.getColumnModel().getColumn(i).setHeaderRenderer(myHeaderRenderer);			
		}
		this.setUI(new ResultTableUI());
		this.getColumnModel().getColumn(1).setCellEditor(new ResultTableCellEditorOpenFile(data));
		this.getTableHeader().setReorderingAllowed(false);
		this.getTableHeader().setFocusable(true);
		this.getTableHeader().addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {
				logger.debug(" header mouse click");
				int c=getColumnModel().getColumnIndexAtX(e.getX());
				String sortingString="";
				logger.debug("Id Column="+c);
				for(int i=0; i<getColumnCount();i++){					
					ResultTableHeaderComponent cpH=((ResultTableHeaderRenderer)getColumnModel().getColumn(i).getHeaderRenderer()).getComponent(i);
					if(i==c){
						cpH.rollSortorder();
						switch (c) {
							case 0:
								sortingString="extract_date";
								break;
							case 1:
								//	sortingString="titre_f";
								break;
							case 2:
								//sortingString="content_type";
								break;
							case 3:
								sortingString="score";
								break;
						}
					}else{
						cpH.setSorted(false);
					}
					if(!sortingString.trim().equals("")){
						switch(cpH.getSortOrder()){
							case ResultTableHeaderComponent.ASCENDING:
								sortingString+=" asc";
								getTableHeader().getColumnModel().getColumn(i).setHeaderValue(cpH.getLabel()+" [a]");
								break;
							case ResultTableHeaderComponent.DESCENDING:
								sortingString+=" desc";
								getTableHeader().getColumnModel().getColumn(i).setHeaderValue(cpH.getLabel()+" [d]");
								break;
							default :
								getTableHeader().getColumnModel().getColumn(i).setHeaderValue(cpH.getLabel());
						}
					}else{
						getTableHeader().getColumnModel().getColumn(i).setHeaderValue(cpH.getLabel());
					}
				}
				if(!sortingString.trim().equals("")){
					searchPanel.setSortingString(sortingString);	
					searchPanel.showResults(0, false);
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				logger.debug(" header mouse pressed");					
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				logger.debug(" header mouse released");					
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

		});
	}

	public String setSortOrder(int column, int order){
		String sortingString="";
		logger.debug("Id Column="+column);
		for(int i=0; i<getColumnCount();i++){					
			ResultTableHeaderComponent cpH=((ResultTableHeaderRenderer)getColumnModel().getColumn(i).getHeaderRenderer()).getComponent(i);
			if(i==column){
				if(order==ResultTableHeaderComponent.ASCENDING){
					cpH.setAscendOrder();
				}else if (order==ResultTableHeaderComponent.DESCENDING){
					cpH.setDescendOrder();
				}else{
					sortingString="";
				}
				switch (column) {
					case 0:
						sortingString="extract_date";
						break;
					case 1:
						sortingString="titre_f";
						break;
					case 2:
						sortingString="content_type";
						break;
					case 3:
						sortingString="score";
						break;
				}
			}else{
				cpH.setSorted(false);
			}
			switch(cpH.getSortOrder()){
				case ResultTableHeaderComponent.ASCENDING:
					sortingString+=" asc";
					getTableHeader().getColumnModel().getColumn(i).setHeaderValue(cpH.getLabel()+" [a]");
					break;
				case ResultTableHeaderComponent.DESCENDING:
					sortingString+=" desc";
					getTableHeader().getColumnModel().getColumn(i).setHeaderValue(cpH.getLabel()+" [d]");
					break;
				default :
					getTableHeader().getColumnModel().getColumn(i).setHeaderValue(cpH.getLabel());
			}
		}
		return sortingString;
	}
}
