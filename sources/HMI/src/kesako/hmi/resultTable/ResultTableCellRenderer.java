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

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import kesako.common.FileTypeIcon25;


public class ResultTableCellRenderer implements TableCellRenderer {
	private Vector<Vector<Component>>vComponent;
	
	public ResultTableCellRenderer(){
		vComponent=new Vector<Vector<Component>>();
		//date
		vComponent.add(new Vector<Component>());
		//title
		vComponent.add(new Vector<Component>());
		//type
		vComponent.add(new Vector<Component>());
		//score
		vComponent.add(new Vector<Component>());
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(vComponent.get(column).size()<=row){
			vComponent.get(column).setSize(row+1);
		}
		if(vComponent.get(column).get(row)==null){
			vComponent.get(column).setElementAt(new JLabel(),row);
			switch(column){
			case 0://date
				vComponent.get(column).setElementAt(new JTextField(),row);
				((JTextField)vComponent.get(column).get(row)).setBorder(null);
				break;
			case 1://title
				JEditorPane title=new JEditorPane();
				title.setContentType("text/html");
				vComponent.get(column).setElementAt(title,row);
				//((JEditorPane)vComponent.get(column).get(row)).setBorder(new LineBorder(Color.BLACK));
				break;
			case 2://type
				vComponent.get(column).setElementAt(new JLabel(),row);
				break;
			case 3://relevance
				JProgressBar relevantBar=new JProgressBar();
				relevantBar.setMaximum(100);
				relevantBar.setStringPainted(true);
				relevantBar.setBackground(Color.WHITE);
				relevantBar.setBorderPainted(false);
				vComponent.get(column).setElementAt(relevantBar,row);
				//vComponent.get(column).setElementAt(new JTextField(),row);
				//((JProgressBar)vComponent.get(column).get(row)).setBorder(new LineBorder(Color.BLACK));
				break;
			}
		}
		switch(column){
		case 0://date
			((JTextField)vComponent.get(column).get(row)).setText(value.toString());
			break;
		case 1://title
			//String title="<b><u>"+value.toString()+"</u></b><br>";
			((JEditorPane)vComponent.get(column).get(row)).setText(value.toString());
			//((JTextField)vComponent.get(column).get(row)).setText(value.toString());
			break;
		case 2://type
			((JLabel)vComponent.get(column).get(row)).setIcon(((FileTypeIcon25)value).getIcon());
			((JLabel)vComponent.get(column).get(row)).setHorizontalAlignment(JLabel.CENTER);
			if(!((FileTypeIcon25)value).getToolTip().trim().equals("")){
				((JLabel)vComponent.get(column).get(row)).setToolTipText(((FileTypeIcon25)value).getToolTip());
			}
			break;
		case 3://relevance
			//((JTextField)vComponent.get(column).get(row)).setText(value.toString());
			((JProgressBar)vComponent.get(column).get(row)).setValue(Integer.parseInt(value.toString()));
			break;
		}
		if(isSelected){
			vComponent.get(column).get(row).setBackground(table.getSelectionBackground());
		}else{
			vComponent.get(column).get(row).setBackground(Color.WHITE);
		}
		return vComponent.get(column).get(row);
	}
}
