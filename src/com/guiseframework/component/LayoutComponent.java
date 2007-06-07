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
	/**The bound property of whether the properties of this component's layout have been initialized.*/
	public final static String LAYOUT_PROPERTIES_INITIALIZED_PROPERTY=getPropertyName(LayoutComponent.class, "layoutPropertiesInitialized");
	
	/**@return The layout definition for the container.*/
	public Layout<?> getLayout();

	/**@return Whether a theme has been applied to this component's layout.*/
	public boolean isLayoutPropertiesInitialized();

	/**Sets whether a theme has been applied to this component's layout.
	This is a bound property of type {@link Boolean}.
	@param newLayoutThemeApplied <code>true</code> if a theme has been applied to this component's layout, else <code>false</code>.
	@see #LAYOUT_PROPERTIES_INITIALIZED_PROPERTY
	@see #setPropertiesInitialized(boolean) 
	*/
	public void setLayoutPropertiesInitialized(final boolean newLayoutThemeApplied);

}
