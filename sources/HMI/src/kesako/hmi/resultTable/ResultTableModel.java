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

import java.util.Calendar;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import kesako.common.FileTypeIcon25;
import kesako.search.ResultDoc;
import kesako.utilities.ImageUtilities;

public class ResultTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -5266958879920356577L;
	private static final Calendar c=Calendar.getInstance();
	
	private Vector<ResultDoc>data;
	
	public ResultTableModel(Vector<ResultDoc> data){
		this.data=data;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(int columnIndex) {
		String columnName="";
		switch(columnIndex){
		case 0://date
			columnName="Date";
			break;
		case 1://title
			columnName="Title";
			break;
		case 2://type
			columnName="Type";
			break;
		case 3://relevance
			columnName="Relevance";
			break;
		default :
			columnName="<No Name>";
		}
		return columnName;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex==1){
			return true;
		}
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object value="";
		String type;
		FileTypeIcon25 fileType;
		switch(columnIndex){
		case 0://date
			c.setTime(data.get(rowIndex).getDocDate());
			value=(c.get(Calendar.DATE)<10?"0"+Integer.toString(c.get(Calendar.DATE)):Integer.toString(c.get(Calendar.DATE)))+"/"
			+(c.get(Calendar.MONTH)<10?"0"+Integer.toString(c.get(Calendar.MONTH)+1):Integer.toString(c.get(Calendar.MONTH)+1))+"/"
					+c.get(Calendar.YEAR);
			break;
		case 1://title of the file "<b><u>"+value.toString()+"</u></b><br>"
			value="<span style=\"font-style:italic; color:#9b5edd\">[" +data.get(rowIndex).getSourceName()+"]</span><br> "+
					"<span style=\"text-decoration:underline;font-weight:bold; color:blue\">"+data.get(rowIndex).getFileTitle()+"</span>" +
					"<br>"+data.get(rowIndex).getHighlight();
			break;
		case 2:
			type=data.get(rowIndex).getDocType();
			if(type.equalsIgnoreCase("msword")){
				fileType = new FileTypeIcon25(ImageUtilities.getMsWordImage(),"MsWord file");
			}else if(type.equalsIgnoreCase("plain")){
				fileType = new FileTypeIcon25(ImageUtilities.getTextImage(),"Text file");				
			}else if(type.equalsIgnoreCase("html")||type.equalsIgnoreCase("htm")){
				fileType = new FileTypeIcon25(ImageUtilities.getHtmlImage(),"HTML file");
			}else if(type.equalsIgnoreCase("pdf")){
				fileType = new FileTypeIcon25(ImageUtilities.getPdfImage(),"PDF file");
			}else {
				fileType = new FileTypeIcon25(ImageUtilities.getUnknownImage(),type);
			}

			value=fileType;
			break;
		case 3:
			value=Integer.toString(data.get(rowIndex).getScore());
			break;
		default :
			value="<No Value>";
		}
		return value;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub

	}
}
