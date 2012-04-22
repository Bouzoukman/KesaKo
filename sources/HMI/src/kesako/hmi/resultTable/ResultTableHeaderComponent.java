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

import javax.swing.JLabel;

public class ResultTableHeaderComponent extends JLabel{
	private static final long serialVersionUID = -3019966448132824108L;
	public static final int  ASCENDING=-1;
	public static final int  NEUTRAL=0;
	public static final int  DESCENDING=1;
	private boolean isSorted;
	private int sortOrder;
	private String label;

	public ResultTableHeaderComponent(String label){
		super(label);
		//this.setBorder(new LineBorder(Color.black));
		setHorizontalAlignment(JLabel.CENTER);
		this.label=label;
		this.isSorted=false;
		this.sortOrder=NEUTRAL;
	}

	public void setSorted(boolean isSorted){
		this.isSorted=isSorted;
		if(!isSorted){
			this.setText(label);
			this.sortOrder=NEUTRAL;
		}
	}

	public void rollSortorder(){
		if(sortOrder==NEUTRAL || sortOrder==ASCENDING){
			sortOrder=DESCENDING;
			isSorted=true;
		}else{
			sortOrder=ASCENDING;
			isSorted=true;
		}
	}

	public void setAscendOrder(){
		sortOrder=ASCENDING;
		isSorted=true;
	}

	public void setDescendOrder(){
		sortOrder=DESCENDING;
		isSorted=true;
	}

	public String getLabel() {
		return label;
	}

	public boolean isSorted() {
		return isSorted;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
