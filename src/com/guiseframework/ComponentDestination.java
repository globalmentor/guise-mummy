package com.guiseframework;

import static com.garretwilson.lang.ObjectUtilities.checkInstance;

import java.net.URI;

import com.guiseframework.component.Component;

/**Default implementation of a navigation point based upon a component.
@author Garret Wilson
*/
public class ComponentDestination extends AbstractDestination
{

	/**The class of the component to represent this destination.*/
	private final Class<? extends Component> componentClass;

		/**@return The class of the component to represent this destination.*/
		public Class<? extends Component> getComponentClass() {return componentClass;}

	/**The style of this destination, or <code>null</code> if no destination-specific style is specified.*/
	private final URI style;

		/**@return The style of this destination, or <code>null</code> if no destination-specific style is specified.*/
		public URI getStyle() {return style;}

	/**Path, component, and style constructor.
	@param path The appplication context-relative path within the Guise container context, which does not begin with '/'.
	@param componentClass The class of the component to represent this destination.
	@param style The style of this destination, or <code>null</code> if no destination-specific style is specified.
	@exception NullPointerException if the path and/or the component class is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public ComponentDestination(final String path, final Class<? extends Component> componentClass, final URI style)
	{
		super(path);	//construct the parent class
		this.componentClass=checkInstance(componentClass, "Component class cannot be null.");	//store the associated class
		this.style=style;		
	}
}
