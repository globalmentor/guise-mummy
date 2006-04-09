package com.guiseframework.component;

import com.guiseframework.model.*;

/**Selectable link.
@author Garret Wilson
*/
public class SelectLink extends AbstractSelectActionControl<SelectLink> implements SelectLinkControl<SelectLink>
{

	/**Default constructor with a default data model.*/
	public SelectLink()
	{
		this(new DefaultActionModel());	//construct the class with a default model
	}

	/**Action model constructor.
	@param actionModel The component action model.
	@exception NullPointerException if the given action model is <code>null</code>.
	*/
	public SelectLink(final ActionModel actionModel)
	{
		super(actionModel);	//construct the parent class
	}

}
