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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.apache.log4j.Logger;

public class FacetItem extends JCheckBox {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(FacetItem.class);
	private FacetPanel facetPanel;
	private String label;
	private long count;

	public FacetItem(String label2,long number,FacetPanel fP){
		super(label2+" ("+number+")");
		logger.debug("Creation :"+label2);
		this.label=label2;
		this.count=number;
		this.facetPanel=fP;
		this.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBox jcb=(JCheckBox)e.getSource();
				if(jcb.isSelected()){
					facetPanel.addFilter(label,true);
				}else{
					facetPanel.removeFilter(label,true);
				}
			}		
		});
	}
	public void setSelected(boolean toSelect){
		super.setSelected(toSelect);
		if(isSelected()){
			facetPanel.addFilter(label,false);
		}else{
			facetPanel.removeFilter(label,false);
		}		
	}
	
	public void setCount(long number){
		this.setText(this.label+" ("+number+")");
		this.count=number;
	}
	public long getCount() {
		return count;
	}
	public String getLabel() {
		return label;
	}
}
