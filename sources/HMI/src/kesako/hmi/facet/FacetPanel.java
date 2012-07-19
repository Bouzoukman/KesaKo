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


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.TitledBorder;

import kesako.hmi.SearchPanel;
import kesako.search.FacetSearch;

import org.apache.log4j.Logger;

public class FacetPanel extends JPanel {
	private static final long serialVersionUID = -4502384658374903402L;
	private static final Logger logger = Logger.getLogger(FacetPanel.class);
	private String facetName;
	private JPanel pTitle;
	private JComboBox<String> cbSortOrder;
	private int nbFacetItem;
	private int nbSelectedItem;
	private SearchPanel searchPanel;
	private JSeparator sep;
	private GridBagConstraints c;
	private Map<String,FacetItem> mFacet;
	private Vector<String> selectedFacet;
	private String selectedSortOrder;
	private FacetSearch fS;
	private String query;
	private int limit;
	private String facetFilter;

	public FacetPanel(String label, String facetName,SearchPanel sP){
		this.setBorder(new TitledBorder(label));
		this.setLayout(new GridBagLayout());

		mFacet=new LinkedHashMap<String, FacetItem>();
		selectedFacet=new Vector<String>();
		selectedSortOrder="Count";
		fS=new FacetSearch(facetName);
		query="";
		limit=-1;
		facetFilter="";

		c=new GridBagConstraints();
		c.insets=new Insets(1,5,1,5);
		c.fill=GridBagConstraints.BOTH;
		c.gridheight=1;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.anchor=GridBagConstraints.NORTH;
		c.weightx=1;

		this.facetName=facetName;
		this.searchPanel=sP;

		this.pTitle=new JPanel();
		this.pTitle.setLayout(new BoxLayout(this.pTitle, BoxLayout.X_AXIS));
		cbSortOrder=new JComboBox<String>();
		cbSortOrder.addItem("Count");
		cbSortOrder.addItem("Alphabetical");
		cbSortOrder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				@SuppressWarnings("unchecked")
				JComboBox<String> cb=(JComboBox<String>)arg0.getSource();
				selectedSortOrder=cb.getSelectedItem().toString();
				logger.debug("Sortorder selected : "+selectedSortOrder);
				if(selectedSortOrder.equalsIgnoreCase("Count")){
					showResults(query,searchPanel.getFilter(),FacetSearch.COUNT,limit);
				}else{
					if(selectedSortOrder.equalsIgnoreCase("Alphabetical")){
						showResults(query,searchPanel.getFilter(),FacetSearch.INDEX,limit);
					}					
				}
				paintAll(getGraphics());
			}
		});

		pTitle.add(cbSortOrder);
		sep=new JSeparator();
	}

	public void showResults(String query,String filter,int facetOrder,int limit){
		Map<String,FacetItem> mSortedFacet;
		this.removeAll();
		this.query=query;
		nbSelectedItem=0;
		//save the name of selected items
		this.selectedFacet.clear();
		for(String key : mFacet.keySet()){
			if(mFacet.get(key).isSelected()){
				selectedFacet.add(key);
				logger.debug("item "+key+" is selected");
			}
		}

		nbSelectedItem=selectedFacet.size();
		logger.debug("Nb selected item="+nbSelectedItem);
		if(fS.doSearch(query,filter,facetOrder,limit)==FacetSearch.RESULTS){
			c.gridx=0;
			this.add(pTitle,c);
			this.add(sep,c);
			nbFacetItem=0;
			int facetCount;
			//constitution of the list of facet items
			mFacet.clear();
			for(String key : fS.getData().keySet()){
				logger.debug("New Key "+key+" : "+fS.getData().get(key).intValue());
				facetCount=fS.getData().get(key).intValue();
				if(facetCount!=0){
					mFacet.put(key,new FacetItem(key,facetCount,this));	
				}
			}
			nbFacetItem=mFacet.size();
			logger.debug("Nb facet="+nbFacetItem);
			if(selectedSortOrder.equalsIgnoreCase("Count")){
				mSortedFacet=mFacet;
			}else{
				if(selectedSortOrder.equalsIgnoreCase("Alphabetical")){
					mSortedFacet=new TreeMap<String,FacetItem>(new FacetAlphabeticalComparator());
					mSortedFacet.putAll(mFacet);
				}else{
					mSortedFacet=null;
				}
			}

			//display facet items
			for(String key : mSortedFacet.keySet()){
				this.add(mSortedFacet.get(key),c);
				if(selectedFacet.contains(key)){
					mSortedFacet.get(key).setSelected(true);
				}
			}
			if(selectedSortOrder.equalsIgnoreCase("Count")){
				cbSortOrder.setSelectedIndex(0);
			}else{
				if(selectedSortOrder.equalsIgnoreCase("Alphabetical")){
					cbSortOrder.setSelectedIndex(1);
				}
			}
		}
	}

	public void addFilter(String value,boolean doSearch){
		if(!facetFilter.contains(value)){
			if(facetFilter.trim().equals("")){
				facetFilter=facetName+":\""+value+"\"";
			}else{
				facetFilter+=" AND "+facetName+":\""+value+"\"";
			}
			searchPanel.updateFilter();
		}
		if(doSearch){
			//searchPanel.showResults(0,false);
			//try to update facet when an item is selected
			searchPanel.showResults(0,true);
		}
		logger.debug("AddFilter="+facetFilter+"|"+nbSelectedItem+"/"+nbFacetItem);
	}

	public void removeFilter(String value,boolean doSearch){
		String regex="(?:AND)*\\s*"+facetName+"\\:\""+value+"\"";
		logger.debug("regex : "+regex);
		facetFilter=facetFilter.replaceAll(regex, "").trim();
		regex="\\A\\s*AND";
		facetFilter=facetFilter.replaceAll(regex, "").trim();
		logger.debug("RemoveFilter="+facetFilter+"|"+nbSelectedItem+"/"+nbFacetItem+" | ");
		searchPanel.updateFilter();
		if(doSearch){
			searchPanel.showResults(0,true);
		}
	}

	/**
	 * @return the facetFilter
	 */
	public String getFacetFilter() {
		return this.facetFilter;
	}
}
