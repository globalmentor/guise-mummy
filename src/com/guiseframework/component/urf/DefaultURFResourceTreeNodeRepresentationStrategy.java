package com.guiseframework.component.urf;

import com.garretwilson.urf.*;

/**A default tree node representation strategy representing an URF resource.
@author Garret Wilson
*/
public class DefaultURFResourceTreeNodeRepresentationStrategy extends AbstractURFResourceTreeNodeRepresentationStrategy<URFResource>
{

	/**Default constructor with a default namespace label manager.*/
	public DefaultURFResourceTreeNodeRepresentationStrategy()
	{
		this(new TURFNamespaceLabelManager());	//create the class with a default namespace label manager
	}

	/**RDF XMLifier constructor.
	@param namespaceLabelManager The manager responsible for generating namespace labels..
	@exception NullPointerException if the given label manager is <code>null</code>.
	*/
	public DefaultURFResourceTreeNodeRepresentationStrategy(final TURFNamespaceLabelManager namespaceLabelManager)
	{
		super(namespaceLabelManager);	//construct the parent class
	}

}
