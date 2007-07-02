package com.guiseframework.component;

import com.guiseframework.component.layout.Layout;
import com.guiseframework.component.layout.RegionLayout;

/**Default implementation of a panel that represents a point of modal navigation with default region layout.
@param <R> The type of modal result this modal panel produces.
@author Garret Wilson
@see RegionLayout
*/
public class DefaultModalNavigationPanel<R> extends AbstractModalNavigationPanel<R>
{

	/**Default constructor with a default region layout.*/
	public DefaultModalNavigationPanel()
	{
		this(new RegionLayout());	//default to a region layout
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public DefaultModalNavigationPanel(final Layout<?> layout)
	{
		super(layout);	//construct the parent class
	}

	/**Ends this frame's modal interaction and navigates either to the previous modal navigation or to this frame's referring URI, if any.
	@param result The result of this frame's modal interaction, or <code>null</code> if no result is given.
	@see #setResult(R)
	@see GuiseSession#endModalNavigation(ModalPanel)
	*/
	public void endModal(final R result)
	{
		setResult(result);	//update the result
		getSession().endModalNavigation(this);	//end modal navigation for this modal frame
	}

}
