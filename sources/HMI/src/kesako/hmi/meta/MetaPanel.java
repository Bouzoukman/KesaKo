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
package kesako.hmi.meta;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.TreeSet;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import kesako.hmi.facet.FacetAlphabeticalComparator;
import kesako.search.FacetSearch;
import kesako.search.Meta;

import org.apache.log4j.Logger;
/**
 * Panel that displays a textfield to specify a meta-value.
 * @author Frédéric SACHOT
 * */
public class MetaPanel extends JPanel{
	private static final long serialVersionUID = -8668407800902178469L;
	private static final Logger logger = Logger.getLogger(MetaPanel.class);
	private JTextField txtMetaValue;
	private Meta meta;
	private TreeSet<String> values;
	private AddMetaPanel parent;
	private MetaPanel me;
	private String defaultValue;
	public static final String STRING_DEFAULT_VALUE="< meta value >";
	private JButton bAll;

	public MetaPanel (Meta meta2,AddMetaPanel parent2,int flagColor,boolean showAllButton){
		logger.debug("Construction MetaPanel");
		defaultValue=STRING_DEFAULT_VALUE;
		me=this;
		values=new TreeSet<String>(new FacetAlphabeticalComparator());
		this.parent=parent2;
		this.meta=meta2;
		if(flagColor==1){
			this.setOpaque(true);
			this.setBackground(Color.WHITE);
		}else{
			this.setOpaque(false);
		}

		GridBagConstraints c=new GridBagConstraints();
		c.insets=new Insets(2,5,2,5);
		c.ipadx=5;
		c.ipady=5;
		c.gridheight=1;
		c.gridwidth=1;

		this.setLayout(new GridBagLayout());
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.fill=GridBagConstraints.NONE;
		this.add(new JLabel(meta.getLabel()),c);
		c.gridx=1;
		c.weightx=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		txtMetaValue = new JTextField(defaultValue);
		txtMetaValue.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				if(((JTextField)e.getSource()).getText().trim().equalsIgnoreCase(defaultValue)){
					((JTextField)e.getSource()).setText("");
				}				
				parent.getMetaValues().removeAllElements();
				parent.getMetaValues().addAll(values);
				parent.setSelectedMetaPanel(me);
				((ListMetaModel)parent.getListValues().getModel()).updateData();
			}
			@Override
			public void focusLost(FocusEvent e) {
				if(((JTextField)e.getSource()).getText().trim().equalsIgnoreCase("")){
					((JTextField)e.getSource()).setText(defaultValue);
				}
			}
		});
		this.add(txtMetaValue,c);
		c.gridx=2;
		c.weightx=0;
		c.fill=GridBagConstraints.NONE;
		bAll = new JButton("All");
		bAll.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(int i=0;i<parent.getvFileMeta().size();i++){
					parent.getvFileMeta().get(i).getMetaPanels().get(meta.getName()).setMetaValue(getMetaValue());
				}
			}
		});
		this.add(bAll,c);
		bAll.setVisible(showAllButton);
	}

	public String getMetaValue() {
		String value="";
		if(!txtMetaValue.getText().trim().equalsIgnoreCase(defaultValue)){
			value=txtMetaValue.getText().trim();
		}						
		return value;
	}
	public void setMetaValue(String value){
		if(!value.trim().equals("")){
			txtMetaValue.setText(value.trim());
		}else{
			txtMetaValue.setText(defaultValue);
		}
	}

	public void updateValues(){
		values.clear();
		//Si on veut lister le choix des auteurs, il faut ajouter un nouveau champ
		if(!meta.getName().trim().equalsIgnoreCase("titre_f")&&!meta.getName().trim().equalsIgnoreCase("titre_doc")&&!meta.getName().trim().equalsIgnoreCase("author_f")&&!meta.getName().trim().equalsIgnoreCase("date")){
					FacetSearch fS=new FacetSearch(meta.getName());
			if(fS.doSearch("","",FacetSearch.INDEX,-1)==FacetSearch.RESULTS){
				for(String key:fS.getData().keySet()){
					values.add(key);
					logger.debug("META value="+key);
				}
			}
		}
	}

	public void setInputVerifier(InputVerifier iv){
		txtMetaValue.setInputVerifier(iv);	
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		if (txtMetaValue.getText().trim().equalsIgnoreCase(STRING_DEFAULT_VALUE)||txtMetaValue.getText().trim().equals("")){
			txtMetaValue.setText(defaultValue);
		}
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return this.defaultValue;
	}
	
	public void showAllButton(boolean showAllButton){
		bAll.setVisible(showAllButton);
		paintAll(getGraphics());
	}
}
