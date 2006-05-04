package com.guiseframework;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

import java.net.URI;

import com.guiseframework.component.NavigationPanel;

/**Abstract implementation of a navigation point, its properties, and its restrictions.
@author Garret Wilson
*/
public abstract class AbstractDestination implements Destination
{

	/**The appplication context-relative path within the Guise container context, which does not begin with '/'.*/
	private final String path;

		/**@return The appplication context-relative path within the Guise container context, which does not begin with '/'.*/
		public String getPath() {return path;}

	/**The class of the panel to represent this destination.*/
	private final Class<? extends NavigationPanel> navigationPanelClass;

		/**@return The class of the panel to represent this destination.*/
		public Class<? extends NavigationPanel> getNavigationPanelClass() {return navigationPanelClass;}

	/**The style of this destination, or <code>null</code> if no destination-specific style is specified.*/
	private final URI style;

		/**@return The style of this destination, or <code>null</code> if no destination-specific style is specified.*/
		public URI getStyle() {return style;}

	/**Path, panel, and style constructor.
	@param path The appplication context-relative path within the Guise container context, which does not begin with '/'.
	@param navigationPanelClass The class of the panel to represent this destination.
	@param style The style of this destination, or <code>null</code> if no destination-specific style is specified.
	@exception NullPointerException if the path and/or the panel is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public AbstractDestination(final String path, final Class<? extends NavigationPanel> navigationPanelClass, final URI style)
	{
		this.path=checkInstance(path, "Navigation path cannot be null.");
		if(isAbsolutePath(path))	//if the path is absolute
		{
			throw new IllegalArgumentException("Navigation path cannot be absolute: "+path);
		}
		this.navigationPanelClass=checkInstance(navigationPanelClass, "Panel class cannot be null.");	//store the associated class
		this.style=style;		
	}
}
