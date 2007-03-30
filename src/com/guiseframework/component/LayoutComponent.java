package com.guiseframework.component;

import com.guiseframework.component.layout.Layout;

import static com.garretwilson.lang.ClassUtilities.*;

/**Composite component that allows for layout of its children.
@author Garret Wilson
*/
public interface LayoutComponent<C extends LayoutComponent<C>> extends CompositeComponent<C>
{

	/**The bound property of the layout.*/
	public final static String LAYOUT_PROPERTY=getPropertyName(LayoutComponent.class, "layout");

	/**@return The layout definition for the container.*/
	public Layout<?> getLayout();

}
