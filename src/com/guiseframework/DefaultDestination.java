package com.guiseframework;

import java.net.URI;

import com.guiseframework.component.NavigationPanel;

/**Default implementation of a navigation point, its properties, and its restrictions.
@author Garret Wilson
*/
public class DefaultDestination extends AbstractDestination
{

	/**Path, panel, and style constructor.
	@param path The appplication context-relative path within the Guise container context, which does not begin with '/'.
	@param navigationPanelClass The class of the panel to represent this destination.
	@param style The style of this destination, or <code>null</code> if no destination-specific style is specified.
	@exception NullPointerException if the path and/or the panel is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public DefaultDestination(final String path, final Class<? extends NavigationPanel> navigationPanelClass, final URI style)
	{
		super(path, navigationPanelClass, style);	//construct the parent class
	}
}
