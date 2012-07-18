/*
 * Copyright 2012 Frédéric SACHOT: bouzoukman@gmail.com
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
package kesako.hmi;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;

import kesako.hmi.facet.FacetPanel;
import kesako.hmi.facet.ListFacetPanel;
import kesako.hmi.resultTable.ResultTable;
import kesako.hmi.resultTable.ResultTableHeaderComponent;
import kesako.search.FacetSearch;
import kesako.search.Meta;
import kesako.search.ResultDoc;
import kesako.search.Search;
import kesako.utilities.XMLUtilities;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Create the search interface and show the results.
 * @author Frédéric SACHOT
 */
public class SearchPanel extends JPanel {
	private static final long serialVersionUID = -8726226163374140534L;
	/**
	 * logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(SearchPanel.class);
	/**
	 * Panel that contains facets that will be shown on the right of the interface.
	 */
	private JPanel rightFacetPanel;
	/**
	 * Panel that contains facets that will be shown on the left of the interface.
	 */
	private JPanel leftFacetPanel;
	/**
	 * ScrollPane object that shows the table of results.
	 */
	private JScrollPane resultScrollPane;
	/**
	 * Text field object for the query of the research.
	 */
	private JTextField txtQuery;
	/**
	 * If the research returns results, the table of results tableData is shown in the resultScrollPane object.	
	 */
	private ResultTable tableData;
	/**
	 * If the research doesn't return results, the label labNoResults is shown in the resultScrollPane object.	
	 */
	private JLabel labNoResult;
	/**
	 * button to launch the research
	 */
	private JButton searchButton;
	/**
	 * button to show the previous page of results.<br>
	 * The button is activated only if there is a previous page of results.
	 */
	private JButton bPrev;
	/**
	 * button to show the next page of results<br>
	 * The button is activated only if there is a next page of results.
	 */
	private JButton bNext;
	/**
	 * index of the first item of the previous page of results.
	 */
	private int prevIndex;
	/**
	 * label to show the number of results.
	 */
	private JLabel nbResult;
	/**
	 * index of the first item of the next page of results.
	 */
	private int nextIndex;
	/**
	 * Variable to store the sorting string that will be used by the search object to sort results.
	 */
	private String sortingString;
	/**
	 * Variable to store the filter string that will be used by the Search object and the FacetSaerch object to filter the results.
	 */
	private String filter;
	/**
	 * search object that make the interface with the SOLR engine to retrieve results.
	 */
	private Search search;
	/**
	 * Vector to store FacetPanel objects.
	 */
	private Vector<FacetPanel> vFPanel;
	/**
	 * Vector to store ResultDoc objects. 
	 */
	private Vector<ResultDoc> vDoc;
	/**
	 * Constructor of the search panel.<br>
	 * The interface is composed by a query panel on the top, a panel of facet on the left and on the right and a result's zone in the center.<br>
	 * To build facet objects, the SearchPanel object need to read the meta.xml file.
	 */
	public SearchPanel(){
		boolean testRightFacet=false;
		this.sortingString="";
		this.filter="";
		
		vFPanel=new Vector<FacetPanel>();
		search=new Search();
		vDoc=search.getDocs();
		this.setLayout(new BorderLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.insets=new Insets(2,5,2,5);
		c.ipadx=5;
		c.ipady=5;
		c.fill=GridBagConstraints.BOTH;
		c.gridheight=1;
		c.gridwidth=1;
		c.weighty=0;

		//query Panel. The query panel contains the txtQuery object and the searchButton object
		JPanel queryPanel=new JPanel();
		queryPanel.setLayout(new GridBagLayout());
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		txtQuery=new JTextField();
		queryPanel.add(txtQuery,c);

		c.gridx=1;
		c.weightx=0;
		searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				sortingString="score desc";
				showResults(0,true,true);
			}		
		});
		queryPanel.add(searchButton,c);
		this.add(queryPanel,BorderLayout.NORTH);

		//leftFacetPanel. The panel is shown on the left of the interface. It contains at least the Sources facet.
		leftFacetPanel=new ListFacetPanel();
		this.add(leftFacetPanel,BorderLayout.WEST);

		//rightFacetPanel. The panel is shown on the right of the interface only if there is some facets to show.
		rightFacetPanel=new ListFacetPanel();

		//facet definition based on the meta.xml file.
		try {
			Document docXML=XMLUtilities.getXMLDocument("meta.xml");
			Node root=docXML.getDocumentElement();
			NodeList metaList=((Element)root).getElementsByTagName("meta");
			Meta m;
			FacetPanel fp;
			for(int i=0;i<metaList.getLength();i++){
				m=new Meta(metaList.item(i));
				fp=new FacetPanel(m.getLabel(),m.getName(),this);
				if(m.getHMIPosition().equalsIgnoreCase("left")){
					leftFacetPanel.add(fp);
					fp.showResults("",filter,FacetSearch.COUNT,-1);
					vFPanel.add(fp);
				}else{
					if(m.getHMIPosition().equalsIgnoreCase("right")){
						rightFacetPanel.add(fp);
						fp.showResults("",filter,FacetSearch.COUNT,-1);
						vFPanel.add(fp);
						testRightFacet=true;
					}
				}
			}			
		} catch (ParserConfigurationException e1) {
			logger.fatal("SearchPanel définition facette",e1);
		} catch (SAXException e1) {
			logger.fatal("SearchPanel définition facette",e1);
		} catch (IOException e1) {
			logger.fatal("SearchPanel définition facette",e1);
		}
		if(testRightFacet){
			this.add(rightFacetPanel,BorderLayout.EAST);
		}
		//resultPanel
		JPanel resultPanel=new JPanel();
		resultScrollPane=new JScrollPane();
		tableData=new ResultTable(vDoc,this);
		labNoResult = new JLabel("No Result");
		this.add(resultPanel,BorderLayout.CENTER);

		resultPanel.setLayout(new GridBagLayout());
		c.insets=new Insets(2,5,2,5);
		c.ipadx=5;
		c.ipady=5;
		c.fill=GridBagConstraints.BOTH;

		c.gridx=0;
		c.gridy=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.weighty=0;
		c.weightx=0;

		bPrev = new JButton("Prev");
		bPrev.setEnabled(false);
		bPrev.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showResults(prevIndex,false);		
			}
		});
		resultPanel.add(bPrev,c);

		c.gridx=1;
		c.weightx=1;
		nbResult = new JLabel();
		nbResult.setHorizontalAlignment(JLabel.CENTER);
		resultPanel.add(nbResult,c);

		bNext = new JButton("Next");
		bNext.setEnabled(false);
		bNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showResults(nextIndex,false);		
			}
		});
		c.gridx=2;
		c.weightx=0;
		resultPanel.add(bNext);

		c.gridx=0;
		c.gridy=1;
		c.gridwidth=3;
		c.weighty=1;
		resultPanel.add(resultScrollPane,c);
	}
	/**
	 * Method to launch a research and show the results. 
	 * @param startIndex index of the first item to be shown
	 * @param updateFacet If TRUE, the content of facets is updated.
	 */
	public void showResults(int startIndex,boolean updateFacet){
		this.showResults(startIndex,updateFacet,false);
	}
	/**
	 * Method to launch a research and show the results. 
	 * @param startIndex index of the first item to be shown
	 * @param updateFacet If TRUE, the content of facets is updated.
	 * @param initSorting If TRUE, the results are sorted with the default sorting mode: descending score
	 */
	public void showResults(int startIndex,boolean updateFacet,boolean initSorting){
		long begin,end;
		String query=txtQuery.getText().trim();
		tableData.clearSelection();
		if(query.equals("")||query.equalsIgnoreCase("AllDoc")){
			query="*:*";
		}
		if(initSorting){
			sortingString="score desc";
		}
		begin=startIndex+1;
		if(startIndex>=20){
			prevIndex=startIndex-20;
			bPrev.setEnabled(true);
		}else{
			bPrev.setEnabled(false);
		}
		this.updateFilter();
		if(search.doSearch(query,filter, sortingString,startIndex)>0){
			if(startIndex+20<search.getNbFound()){
				end=startIndex+20;
				nextIndex=startIndex+20;
				bNext.setEnabled(true);
			}else{
				end=search.getNbFound();
				bNext.setEnabled(false);
			}
			resultScrollPane.getViewport().setView(tableData);
			nbResult.setText("Results "+begin+" / "+end +" over " + search.getNbFound());
		}else{
			resultScrollPane.getViewport().setView(labNoResult);
			nbResult.setText("");
		}

		if(updateFacet){
			for(int i=0;i<vFPanel.size();i++){
				vFPanel.get(i).showResults(query,filter,FacetSearch.COUNT,-1);
			}
		}
		paintAll(getGraphics());
		if(initSorting){
			tableData.setSortOrder(3,ResultTableHeaderComponent.DESCENDING);
		}
	}
	/**
	 * Return a reference to the Search button. It can be used to specified the default action button of an interface.
	 */
	public JButton getSearchButton() {
		return searchButton;
	}
	/**
	 * Update the sorting string.
	 * @param sortingString the new value of the sorting string.
	 */
	public void setSortingString(String sortingString) {
		this.sortingString = sortingString;
	}

	public void updateFilter(){
		filter="";
		boolean isFirst=true;
		for(int i=0;i<vFPanel.size();i++){
			if(!vFPanel.get(i).getFacetFilter().trim().equals("")){
				if(isFirst){
					filter="("+vFPanel.get(i).getFacetFilter()+")";
					isFirst=false;
				}else{
					filter+=" AND ("+vFPanel.get(i).getFacetFilter()+")";
				}
			}
		}
	}
	/**
	 * @return the filter
	 */
	public String getFilter() {
		return this.filter;
	}
}
