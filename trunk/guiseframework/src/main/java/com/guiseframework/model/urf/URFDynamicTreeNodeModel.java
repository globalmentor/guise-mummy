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

package com.guiseframework.model.urf;

import java.util.*;

import org.urframework.*;

import com.guiseframework.model.*;

/**A dynamic tree node model that represents an URF data model.
<p>Any resources will be dynamically loaded</p>
@author Garret Wilson
*/
public class URFDynamicTreeNodeModel extends AbstractURFDynamicTreeNodeModel<URF>
{

	/**Default constructor with no initial value.*/
	public URFDynamicTreeNodeModel()
	{
		this(null);	//construct the class with no initial value
	}

	/**Initial value constructor.
	@param initialValue The initial value, which will not be validated.
	*/
	public URFDynamicTreeNodeModel(final URF initialValue)
	{
		super(URF.class, initialValue);	//construct the parent class
	}

	/**Dynamically determines whether this node is a leaf.
	This version determines if the URF data model has resources.
	@return Whether this node should be considered a leaf with no children.
	*/
	protected boolean determineLeaf()
	{
		final URF urf=getValue();	//get the URF data model
		return urf==null || !urf.hasResources();	//if we have URF and the URF has resources, this is not a leaf
	}

	/**Dynamically determines children.
	This version returns models representing the root children of the URF data model, if any.
	@return The dynamically loaded list of children.
	*/
	protected List<TreeNodeModel<?>> determineChildren()
	{
		final List<TreeNodeModel<?>> children=new ArrayList<TreeNodeModel<?>>();	//create a list to hold the children, even if we don't have any here, for child classes may add to this list
		final URF urf=getValue();	//get the URF data model
		if(urf!=null)	//if we have an URF data model
		{
			for(final URFResource rootResource:urf.getRootResources())	//look at each root resource TODO fix to make sure that circularly-referenced resources are included
			{
				final URFResourceDynamicTreeNodeModel<?> treeNode=createURFResourceTreeNode(null, rootResource);	//create a tree node for this root resource
				children.add(treeNode);	//add the tree node to our list
			}
		}
		return children;	//return the children we determined
	}
}
