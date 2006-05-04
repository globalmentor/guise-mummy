package com.guiseframework;

import java.net.URI;

import com.guiseframework.component.NavigationPanel;

/**Description of a navigation point, its properties, and its restrictions.
@author Garret Wilson
*/
public interface Destination
{

	/**@return The appplication context-relative path within the Guise container context, which does not begin with '/'.*/
	public String getPath();

	/**@return The class of the panel to represent this destination.*/
	public Class<? extends NavigationPanel> getNavigationPanelClass();

	/**@return The style of this destination, or <code>null</code> if no destination-specific style is specified.*/
	public URI getStyle();

}
