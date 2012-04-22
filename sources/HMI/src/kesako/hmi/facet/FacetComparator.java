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


import java.util.Comparator;
import java.util.Map;

public class FacetComparator implements Comparator<String> {
	private Map<String,FacetItem> m;

	public FacetComparator(Map<String,FacetItem> m){
		this.m=m;
	}
	@Override
	public int compare(String facetName1, String facetName2) {
		int test;
		FacetItem f1,f2;
		f1=m.get(facetName1);
		f2=m.get(facetName2);
		if(f1.getCount()==f2.getCount()){
			test=f1.getLabel().compareTo(f2.getLabel());
		}else{
			if(f1.getCount()<f2.getCount()){
				test=1;
			}else{
				test=-1;
			}
		}
		return test;
	}
}
