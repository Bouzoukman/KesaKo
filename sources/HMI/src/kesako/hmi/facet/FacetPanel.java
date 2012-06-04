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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.TitledBorder;

import kesako.common.FacetSearch;
import kesako.hmi.SearchPanel;
import kesako.utilities.SOLRUtilities;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;




public class FacetPanel extends JPanel {
	private static final long serialVersionUID = -4502384658374903402L;
	private static final Logger logger = Logger.getLogger(FacetPanel.class);
	private String facetName;
	private String facetFilter;
	private JCheckBox sAll;
	private int nbFacetItem;
	private int nbSelectedItem;
	private SearchPanel searchPanel;
	private String label;
	private JSeparator sep;
	private GridBagConstraints c;
	private Map<String,FacetItem> mFacet;
	private FacetSearch fS;

	public FacetPanel(String label, String facetName,SearchPanel sP){
		this.setBorder(new TitledBorder(label));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//this.setUI(new FacetUI());
		mFacet=new HashMap<String, FacetItem>();
		fS=new FacetSearch(facetName);
		
		c=new GridBagConstraints();
		c.insets=new Insets(2,5,2,5);
		c.ipadx=5;
		c.ipady=5;
		c.fill=GridBagConstraints.BOTH;
		c.gridheight=1;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.anchor=GridBagConstraints.NORTH;
		c.weightx=1;

		this.facetName=facetName;
		this.facetFilter="";
		this.searchPanel=sP;
		this.label=label;
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
		sep=new JSeparator();
	}

	public void doSearch(String query,String filter){
		boolean sAllState=sAll.isSelected();
		TreeMap<String,FacetItem> mSortedFacet;
		this.removeAll();
		if(fS.doSearch(query)==FacetSearch.RESULTS){
			c.gridx=0;
			this.add(sAll,c);
			this.add(sep,c);
			nbFacetItem=0;
			nbSelectedItem=0;
			facetFilter="";
			int facetCount;
			for(String key : fS.getData().keySet()){
				logger.debug(key+" : "+fS.getData().get(key).intValue());
				facetCount=fS.getData().get(key).intValue();
				if(mFacet.containsKey(key)){
					mFacet.get(key).setCount(facetCount);
				}else{
					mFacet.put(key,new FacetItem(key,facetCount,this));	
				}
			}
			mSortedFacet=new TreeMap<String,FacetItem>(new FacetComparator(mFacet));
			mSortedFacet.putAll(mFacet);
			nbFacetItem=mFacet.size();
			for(String key : mSortedFacet.keySet()){
				this.add(mSortedFacet.get(key),c);
				if(mSortedFacet.get(key).isSelected() || sAllState){
					//addFilter(mSortedFacet.get(key).getLabel(),false);
					mSortedFacet.get(key).setSelected(true);
				}
				if(mSortedFacet.get(key).getCount()==0){
					mSortedFacet.get(key).setEnabled(false);
					if(mSortedFacet.get(key).isSelected() || sAllState){
						mSortedFacet.get(key).setSelected(false);
					}
					nbFacetItem--;
					logger.debug("New nbFacetItem="+nbFacetItem);
				}else{
					mSortedFacet.get(key).setEnabled(true);					
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
	public void doSearchOld(String query){
		NamedList<String> q = new NamedList<String>();
		boolean sAllState=sAll.isSelected();
		q.add("fl", "file_uri");
		q.add("q", query);
		q.add("rows", "1");
		q.add("start", "0");
		q.add("facet","true");
		q.add("facet.field",facetName);

		logger.debug("query : "+q);
		QueryResponse r;
		String facetName;
		long facetCount;
		TreeMap<String,FacetItem> mSortedFacet;
		try {
			r = SOLRUtilities.getSOLRServer().query(SolrParams.toSolrParams(q));
			List<Count> lFI=r.getFacetFields().get(0).getValues();
			this.removeAll();
			if(lFI!=null){
				c.gridx=0;
				this.add(sAll,c);
				this.add(sep,c);
				nbFacetItem=0;
				nbSelectedItem=0;
				facetFilter="";
				for(int i=0;i<lFI.size();i++){
					logger.debug(lFI.get(i).getName()+" : "+lFI.get(i).getCount());
					facetName=lFI.get(i).getName();
					facetCount=lFI.get(i).getCount();
					if(mFacet.containsKey(facetName)){
						mFacet.get(facetName).setCount(facetCount);
					}else{
						mFacet.put(facetName,new FacetItem(facetName,facetCount,this));	
					}
				}
				mSortedFacet=new TreeMap<String,FacetItem>(new FacetComparator(mFacet));
				mSortedFacet.putAll(mFacet);
				nbFacetItem=mFacet.size();
				for(String key : mSortedFacet.keySet()){
					this.add(mSortedFacet.get(key),c);
					if(mSortedFacet.get(key).isSelected() || sAllState){
						//addFilter(mSortedFacet.get(key).getLabel(),false);
						mSortedFacet.get(key).setSelected(true);
					}
					if(mSortedFacet.get(key).getCount()==0){
						mSortedFacet.get(key).setEnabled(false);
						if(mSortedFacet.get(key).isSelected() || sAllState){
							mSortedFacet.get(key).setSelected(false);
						}
						nbFacetItem--;
						logger.debug("New nbFacetItem="+nbFacetItem);
					}else{
						mSortedFacet.get(key).setEnabled(true);					
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
		} catch (SolrServerException e) {
			logger.fatal("doSearch",e);
		}
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
