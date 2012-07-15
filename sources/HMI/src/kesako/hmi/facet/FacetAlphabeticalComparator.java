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

public class FacetAlphabeticalComparator implements Comparator<String> {

	@Override
	public int compare(String facetName1, String facetName2) {
		String f1,f2;
		f1=facetName1.toLowerCase();
		f2=facetName2.toLowerCase();
		
		f1=f1.replaceAll("[éèêë]", "e");
		f2=f2.replaceAll("[éèêë]", "e");
		
		f1=f1.replaceAll("ç", "c");
		f2=f2.replaceAll("ç", "c");
		f1=f1.replaceAll("à", "a");
		f2=f2.replaceAll("à", "a");
				
		return f1.compareTo(f2);
		//return facetName1.compareTo(facetName2);
	}
}
