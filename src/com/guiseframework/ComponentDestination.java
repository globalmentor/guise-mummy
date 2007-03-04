package com.guiseframework;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import java.net.URI;
import java.util.regex.Pattern;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.component.Component;

/**Default implementation of a navigation point based upon a component.
@author Garret Wilson
*/
public class ComponentDestination extends AbstractDestination
{

	/**The style bound property.*/
	public final static String STYLE_PROPERTY=getPropertyName(ComponentDestination.class, "style");

	/**The class of the component to represent this destination.*/
	private final Class<? extends Component<?>> componentClass;

		/**@return The class of the component to represent this destination.*/
		public Class<? extends Component<?>> getComponentClass() {return componentClass;}

	/**The style of this destination, or <code>null</code> if no destination-specific style is specified.*/
	private URI style=null;

		/**@return The style of this destination, or <code>null</code> if no destination-specific style is specified.*/
		public URI getStyle() {return style;}

		/**Sets the style of this destination.
		This is a bound property of type <code>URI</code>.
		@param newStyle The style of this destination, or <code>null</code> if no destination-specific style is specified.
		@see #STYLE_PROPERTY
		*/
		public void setStyle(final URI newStyle)
		{
			if(!ObjectUtilities.equals(style, newStyle))	//if the value is really changing
			{
				final URI oldStyle=style;	//get the old value
				style=newStyle;	//actually change the value
				firePropertyChange(STYLE_PROPERTY, oldStyle, newStyle);	//indicate that the value changed
			}			
		}
		
	/**Path and component constructor with no style specified.
	@param path The appplication context-relative path within the Guise container context, which does not begin with '/'.
	@param componentClass The class of the component to represent this destination.
	@exception NullPointerException if the path and/or the component class is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public ComponentDestination(final String path, final Class<? extends Component<?>> componentClass)
	{
		this(path, componentClass, null);	//construct the class with no style specified
	}

	/**Path, component, and style constructor.
	@param path The appplication context-relative path within the Guise container context, which does not begin with '/'.
	@param componentClass The class of the component to represent this destination.
	@param style The style of this destination, or <code>null</code> if no destination-specific style is specified.
	@exception NullPointerException if the path and/or the component class is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public ComponentDestination(final String path, final Class<? extends Component<?>> componentClass, final URI style)
	{
		super(path);	//construct the parent class
		this.componentClass=checkInstance(componentClass, "Component class cannot be null.");	//store the associated class
		this.style=style;		
	}
	
	/**Path pattern and component constructor with no style specified.
	@param pathPattern The pattern to match an appplication context-relative path within the Guise container context, which does not begin with '/'.
	@param componentClass The class of the component to represent this destination.
	@exception NullPointerException if the path pattern and/or the component class is <code>null</code>.
	*/
	public ComponentDestination(final Pattern pathPattern, final Class<? extends Component<?>> componentClass)
	{
		this(pathPattern, componentClass, null);	//construct the class with no style specified
	}

	/**Path pattern, component, and style constructor.
	@param pathPattern The pattern to match an appplication context-relative path within the Guise container context, which does not begin with '/'.
	@param componentClass The class of the component to represent this destination.
	@param style The style of this destination, or <code>null</code> if no destination-specific style is specified.
	@exception NullPointerException if the path pattern and/or the component class is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public ComponentDestination(final Pattern pathPattern, final Class<? extends Component<?>> componentClass, final URI style)
	{
		super(pathPattern);	//construct the parent class
		this.componentClass=checkInstance(componentClass, "Component class cannot be null.");	//store the associated class
		this.style=style;		
	}
}
