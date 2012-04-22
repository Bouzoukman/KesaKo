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


import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import kesako.common.ResultDoc;
import kesako.utilities.OSValidator;

import org.apache.log4j.Logger;


public class ResultTableCellEditorOpenFile extends AbstractCellEditor implements TableCellEditor{
	private static final long serialVersionUID = 996568487354881135L;
	private static final Logger logger = Logger.getLogger(ResultTableCellEditorOpenFile.class);
	private Vector<ResultDoc>data;
	
	public ResultTableCellEditorOpenFile(Vector<ResultDoc>data){
		this.data=data;
	}
	@Override
	public Object getCellEditorValue() {
		return null;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		logger.debug(table.getModel().getValueAt(row, column) +" : "+ data.get(row).getFileURI());
		try {
			if(OSValidator.isWindows()){
				//two different methods to open file if the OS is Windows.
				Runtime.getRuntime().exec("cmd /c \""+data.get(row).getFileURI()+"\"");
				//Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + data.get(row).getFileURI());
			}else{
				//if Windows, the open method doesn't open network file.
				Desktop.getDesktop().open(new File(data.get(row).getFileURI()));
			}
		} catch (IOException e) {
			logger.fatal("Error open File : "+data.get(row).getFileURI(),e);
		}
		return null;
	}

}
