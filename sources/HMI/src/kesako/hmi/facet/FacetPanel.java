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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.TitledBorder;

import kesako.common.FacetSearch;
import kesako.hmi.SearchPanel;

import org.apache.log4j.Logger;

public class FacetPanel extends JPanel {
	private static final long serialVersionUID = -4502384658374903402L;
	private static final Logger logger = Logger.getLogger(FacetPanel.class);
	private String facetName;
	private String facetFilter;
	private JCheckBox sAll;
	private JPanel pTitle;
	private JComboBox<String> cbSortOrder;
	private int nbFacetItem;
	private int nbSelectedItem;
	private SearchPanel searchPanel;
	private String label;
	private JSeparator sep;
	private GridBagConstraints c;
	private Map<String,FacetItem> mFacet;
	private FacetSearch fS;
	private String query;
	private int limit;
	private String filter;

	public FacetPanel(String label, String facetName,SearchPanel sP){
		this.setBorder(new TitledBorder(label));
		//this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setLayout(new GridBagLayout());
		//this.setUI(new FacetUI());
		mFacet=new LinkedHashMap<String, FacetItem>();
		fS=new FacetSearch(facetName);
		query="";
		limit=-1;
		filter="";
		
		c=new GridBagConstraints();
		c.insets=new Insets(1,5,1,5);
		//c.ipadx=5;
		//c.ipady=5;
		c.fill=GridBagConstraints.BOTH;
		c.gridheight=1;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.anchor=GridBagConstraints.NORTH;
		c.weightx=1;

		this.facetName=facetName;
		this.facetFilter="";
		this.searchPanel=sP;
		this.label=label;
		
		this.pTitle=new JPanel();
		this.pTitle.setLayout(new BoxLayout(this.pTitle, BoxLayout.X_AXIS));
		sAll = new JCheckBox("SelectAll");
		sAll.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Component c;
				boolean test=sAll.isSelected();
				for(int i=0; i<getComponents().length;i++){
					c=getComponents()[i];
					if(c instanceof FacetItem && ((JCheckBox)c).isEnabled()){
						((JCheckBox)c).setSelected(test);
					}
				}
				searchPanel.showResults(0,false);
			}
		});
		pTitle.add(sAll);
		cbSortOrder=new JComboBox<String>();
		cbSortOrder.addItem("Count");
		cbSortOrder.addItem("Alphabetical");
		cbSortOrder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				@SuppressWarnings("unchecked")
				JComboBox<String> cb=(JComboBox<String>)arg0.getSource();
				String selected=cb.getSelectedItem().toString();
				logger.debug("Sortorder selected : "+selected);
				if(selected.equalsIgnoreCase("Count")){
					doSearch(query,filter,FacetSearch.COUNT,limit);
				}else{
					if(selected.equalsIgnoreCase("Alphabetical")){
						doSearch(query,filter,FacetSearch.ALPHABETICAL,limit);
					}					
				}
				paintAll(getGraphics());
			}
		});
		
		pTitle.add(cbSortOrder);
		sep=new JSeparator();
	}

	public void doSearch(String query,String filter,int facetOrder,int limit){
		boolean sAllState=sAll.isSelected();
		this.removeAll();
		this.query=query;
		this.filter=filter;
		if(fS.doSearch(query,facetOrder,limit)==FacetSearch.RESULTS){
			c.gridx=0;
			this.add(pTitle,c);
			this.add(sep,c);
			nbFacetItem=0;
			nbSelectedItem=0;
			facetFilter="";
			int facetCount;
			mFacet.clear();
			for(String key : fS.getData().keySet()){
				logger.debug("New Key "+key+" : "+fS.getData().get(key).intValue());
				facetCount=fS.getData().get(key).intValue();
				if(mFacet.containsKey(key)){
					mFacet.get(key).setCount(facetCount);
				}else{
					mFacet.put(key,new FacetItem(key,facetCount,this));	
				}
			}
			nbFacetItem=mFacet.size();
			for(String key : mFacet.keySet()){
				this.add(mFacet.get(key),c);
				if(mFacet.get(key).isSelected() || sAllState){
					mFacet.get(key).setSelected(true);
				}
				if(mFacet.get(key).getCount()==0){
					mFacet.get(key).setEnabled(false);
					if(mFacet.get(key).isSelected() || sAllState){
						mFacet.get(key).setSelected(false);
					}
					nbFacetItem--;
					logger.debug("New nbFacetItem="+nbFacetItem);
				}else{
					mFacet.get(key).setEnabled(true);					
				}
			}
			if(nbFacetItem!=0){
				if(nbSelectedItem==nbFacetItem){
					sAll.setSelected(true);
				}else{
					sAll.setSelected(false);
				}
			}else{
				sAll.setSelected(sAllState);
			}
		}
	}
	public void doSearch(String query,String filter){
		this.doSearch(query, filter, FacetSearch.COUNT, -1);
	}

	public void addFilter(String value,boolean doSearch){
		if(!facetFilter.contains(value)){
			nbSelectedItem++;
			if(nbSelectedItem==nbFacetItem){
				sAll.setSelected(true);
			}else{
				sAll.setSelected(false);
			}
			if(facetFilter.trim().equals("")){
				facetFilter=facetName+":\""+value+"\"";
			}else{
				facetFilter+=" "+facetName+":\""+value+"\"";
			}
		}
		if(doSearch){
			//searchPanel.showResults(0,false);
			//try to update facet when an item is selected
			searchPanel.showResults(0,true);
		}
		logger.debug("Filter="+facetFilter+"|"+nbSelectedItem+"/"+nbFacetItem);
	}

	public void removeFilter(String value,boolean doSearch){
		nbSelectedItem--;
		sAll.setSelected(false);
		String regex="\\s*"+facetName+"\\:\""+value+"\"";
		logger.debug("regex : "+regex);
		facetFilter=facetFilter.replaceAll(regex, "").trim();
		logger.debug("Filter="+facetFilter+"|"+nbSelectedItem+"/"+nbFacetItem);
		if(doSearch){
			searchPanel.showResults(0,false);
		}
	}

	public void init(boolean isSelected){
		Component c;
		this.doSearch("*:*","");	
		sAll.setSelected(isSelected);
		for(int i=0; i<getComponents().length;i++){
			c=getComponents()[i];
			if(c instanceof FacetItem){
				((JCheckBox)c).setSelected(isSelected);
			}
		}
	}

	public String getLabel() {
		return label;
	}

	public String getFacetFilter() {
		return facetFilter;
	}
}
