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

package com.guiseframework.component.rdf;

import com.globalmentor.rdf.*;
import com.guiseframework.component.TreeControl;
import com.guiseframework.model.*;

/**An tree node representation strategy representing an RDF literal.
@author Garret Wilson
*/
public class RDFLiteralTreeNodeRepresentationStrategy extends AbstractRDFObjectTreeNodeRepresentationStrategy<RDFLiteral>
{

	/**Default constructor with a default RDF XMLifier.*/
	public RDFLiteralTreeNodeRepresentationStrategy()
	{
		this(new RDFXMLGenerator());	//create the class with a default RDF XMLifier
	}

	/**RDF XMLifier constructor.
	@param rdfXMLifier The RDF XMLifier to use for creating labels.
	@throws NullPointerException if the given RDF XMLifier is <code>null</code>.
	*/
	public RDFLiteralTreeNodeRepresentationStrategy(final RDFXMLGenerator rdfXMLifier)
	{
		super(rdfXMLifier);	//construct the parent
	}

	/**Builds the label to be used for a tree node.
	This version appends information about the literal.
	@param <N> The type of value contained in the node.
	@param stringBuilder The string builder to hold the label text.
	@param treeControl The component containing the model.
	@param model The model containing the value.
	@param treeNode The node containing the value.
	@param value The value contained in the node.
	@return The string builder used to construct the label. 
	*/
//TODO fix	protected <N extends RDFLiteral> StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final N value)
	protected StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<? extends RDFLiteral> treeNode, final RDFLiteral value)	//TODO later put this method hierarchy in a custom label model
	{
		super.buildLabelText(stringBuilder, treeControl, model, treeNode, value);	//do the default label text building
		final boolean hasProperty=stringBuilder.length()>0;	//see if we have property information
		if(hasProperty) //if we had a property
			stringBuilder.append(':').append(' '); //append ": " to separate the property from the literal
		stringBuilder.append('"').append(value.getLexicalForm()).append('"');  //append the literal value in quotes
		return stringBuilder;	//return the string builder
	}
}
