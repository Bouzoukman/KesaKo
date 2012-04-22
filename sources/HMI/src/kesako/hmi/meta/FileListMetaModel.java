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


import java.util.Vector;

import javax.swing.AbstractListModel;

public class FileListMetaModel extends AbstractListModel<String> {
	private static final long serialVersionUID = -3117864723405912379L;
	private Vector<String> data;
	public FileListMetaModel(Vector<String> listMeta){
		this.data=listMeta;
	}
	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public String getElementAt(int index) {
		return data.get(index);
	}
	public void updateData(){
		fireContentsChanged(this, 0, getSize());
	}
}
