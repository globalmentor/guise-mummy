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

package com.guiseframework.component.rdf.maqro;

import org.urframework.maqro.Question;

import com.globalmentor.rdf.*;
import com.guiseframework.component.TreeControl;
import com.guiseframework.model.TreeModel;
import com.guiseframework.model.TreeNodeModel;

/**A tree node representation strategy representing a MAQRO question.
@author Garret Wilson
*/
public class QuestionTreeNodeRepresentationStrategy extends AbstractInteractionTreeNodeRepresentationStrategy<Question>
{

	/**Default constructor with a default RDF XMLifier.*/
	public QuestionTreeNodeRepresentationStrategy()
	{
		this(new RDFXMLGenerator());	//create the class with a default RDF XMLifier
	}

	/**RDF XMLifier constructor.
	@param rdfXMLifier The RDF XMLifier to use for creating labels.
	@exception NullPointerException if the given RDF XMLifier is <code>null</code>.
	*/
	public QuestionTreeNodeRepresentationStrategy(final RDFXMLGenerator rdfXMLifier)
	{
		super(rdfXMLifier);	//construct the parent
	}


	/**Builds the label to be used for a tree node.
	This version adds the query of the question if any.
	@param <N> The type of value contained in the node.
	@param stringBuilder The string builder to hold the label text.
	@param treeControl The component containing the model.
	@param model The model containing the value.
	@param treeNode The node containing the value.
	@param value The value contained in the node.
	@return The string builder used to construct the label. 
	*/
//TODO fix	protected <N extends V> StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final N value)	//TODO fix public
	protected StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<? extends Question> treeNode, final Question value)	//TODO later put this method hierarchy in a custom label model
	{
		super.buildLabelText(stringBuilder, treeControl, model, treeNode, value);	//do the default label text building
		if(value!=null)	//if there is a value
		{
			if(stringBuilder.length()>0)	//if there already is content
			{
				stringBuilder.append(' ');	//add separater content
			}
			stringBuilder.append(value);	//TODO testing; improve
		}
//TODO del		super.buildLabelText(stringBuilder, treeControl, model, treeNode, value);	//do the default label text building
		return stringBuilder;	//return the string builder
	}
}
