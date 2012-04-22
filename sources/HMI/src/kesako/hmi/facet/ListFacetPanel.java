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
package kesako.hmi.facet;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class ListFacetPanel extends JPanel {
	private static final long serialVersionUID = 728288662070298328L;
	private GridBagConstraints c;
	private JPanel panelVide;

	public ListFacetPanel(){
		this.setLayout(new GridBagLayout());
		c=new GridBagConstraints();
		c.insets=new Insets(2,5,2,5);
		c.ipadx=5;
		c.ipady=5;
		c.fill=GridBagConstraints.BOTH;
		c.gridheight=1;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.anchor=GridBagConstraints.NORTH;
		c.weightx=1;
		panelVide=new JPanel();
	}

	@Override
	public Component add(Component comp) {
		if(this.getComponentCount()>0){
			this.remove(panelVide);
		}
		c.weighty=0;
		super.add(comp,c);
		c.weighty=1;
		super.add(panelVide,c);
		return this;
	}
	
	/*	
	public void paint(Graphics g){
		int maxWidth=0;
		maxWidth=getGraphics().getFontMetrics().charsWidth(label.toCharArray(),0,label.length())+20;
		for (int i=0;i<getComponentCount();i++){
			if(getComponent(i).getWidth()>maxWidth){
				maxWidth=getComponent(i).getWidth();
			}
		}
		setSize(maxWidth+10, getHeight());
		setPreferredSize(new Dimension(maxWidth+10, getHeight()));
		
		super.paint(g);
	}
	*/

}
