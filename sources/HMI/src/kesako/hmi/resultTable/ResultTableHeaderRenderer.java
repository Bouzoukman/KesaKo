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
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

public class ResultTableHeaderRenderer implements TableCellRenderer {
	private static final Logger logger = Logger.getLogger(ResultTableHeaderRenderer.class);
	private Vector<ResultTableHeaderComponent>vComponent;
	
	public ResultTableHeaderRenderer(){
		vComponent=new Vector<ResultTableHeaderComponent>();
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(vComponent.size()<=column){
			vComponent.setSize(column+1);
		}
		if(vComponent.get(column)==null){
			vComponent.setElementAt(new ResultTableHeaderComponent(value.toString()),column);
		}
		vComponent.get(column).setText(value.toString());
		logger.debug(value.toString() + " : "+vComponent.size());
		return vComponent.get(column);
	}
	public ResultTableHeaderComponent getComponent(int id) {
		if(id<vComponent.size()){
			return vComponent.get(id);
		}else{
			return null;
		}
	}
}
