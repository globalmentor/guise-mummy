/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.model;

/**A dummy tree node that holds no value and, if placed as the root of a tree, will not be displayed.
@author Garret Wilson
*/
public class DummyTreeNodeModel extends DefaultTreeNodeModel<String>	//TODO later make this just store a null Object rather than a String, once we have default editors for object types
{

	/**Constructs a dummy tree node model.*/
	public DummyTreeNodeModel()
	{
			//TODO decide what type to use for the default root node, and what type of editor to use in the default editor (Object won't work with the default text input component)
		super(String.class, null);	//construct the class with a null initial value
	}
}
