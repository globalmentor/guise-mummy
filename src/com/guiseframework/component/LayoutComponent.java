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
	/**The bound property of whether a theme has been applied to this component's layout.*/
	public final static String LAYOUT_THEME_APPLIED_PROPERTY=getPropertyName(LayoutComponent.class, "layoutThemeApplied");

	/**@return The layout definition for the container.*/
	public Layout<?> getLayout();

	/**@return Whether a theme has been applied to this component's layout.*/
	public boolean isLayoutThemeApplied();

	/**Sets whether a theme has been applied to this component's layout.
	This is a bound property of type {@link Boolean}.
	@param newLayoutThemeApplied <code>true</code> if a theme has been applied to this component's layout, else <code>false</code>.
	@see #LAYOUT_THEME_APPLIED_PROPERTY
	@see #setThemeApplied(boolean) 
	*/
	public void setLayoutThemeApplied(final boolean newLayoutThemeApplied);

}
