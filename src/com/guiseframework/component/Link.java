package com.guiseframework.component;

import com.guiseframework.model.ActionModel;
import com.guiseframework.model.DefaultActionModel;

/**Control with an action model rendered as a link.
@author Garret Wilson
*/
public class Link extends AbstractLinkControl<Link>
{

	/**Default constructor with a default data model.*/
	public Link()
	{
		this(new DefaultActionModel());	//construct the class with a default model
	}

	/**Action model constructor.
	@param actionModel The component action model.
	@exception NullPointerException if the given action model is <code>null</code>.
	*/
	public Link(final ActionModel actionModel)
	{
		super(actionModel);	//construct the parent class
	}

}
