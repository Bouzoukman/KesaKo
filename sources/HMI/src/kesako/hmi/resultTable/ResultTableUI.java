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

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTableUI;

public class ResultTableUI extends BasicTableUI {
	
	@Override
	public void paint(Graphics g, JComponent c) {
		int titleWidth;
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(0).setResizable(false);
		table.getColumnModel().getColumn(2).setPreferredWidth(80);
		table.getColumnModel().getColumn(2).setResizable(false);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setResizable(false);

		table.getColumnModel().getColumn(1).setResizable(true);
		//titleWidth=Math.max(table.getColumnModel().getColumn(1).getWidth(), 200);
		titleWidth=200;
		titleWidth=Math.max(titleWidth,table.getParent().getWidth()-260);
		table.getColumnModel().getColumn(1).setPreferredWidth(titleWidth);
		super.paint(g, c);
	}
}
