package com.garretwilson.guise.component;

import static com.garretwilson.lang.ClassUtilities.*;

/**Base interface for all Guise components.
@author Garret Wilson
*/
public interface Component
{

	/**The bound property of the component style ID.*/
	public final static String STYLE_ID_PROPERTY=getPropertyName(Component.class, "styleID");
	/**The bound property of whether the component is visible.*/
	public final static String VISIBLE_PROPERTY=getPropertyName(Component.class, "visible");

	/**@return The component identifier.*/
	public String getID();

	/**@return The style identifier, or <code>null</code> if there is no style ID.*/
	public String getStyleID();

	/**Identifies the style for the component.
	This is a bound property.
	@param newStyleID The style identifier, or <code>null</code> if there is no style ID.
	@see STYLE_ID_PROPERTY
	*/
	public void setStyleID(final String newStyleID);

	/**@return Whether the component is visible.*/
	public boolean isVisible();

	/**Sets whether the component is visible.
	This is a bound property of type <code>Boolean</code>.
	@param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
	@see VISIBLE_PROPERTY
	*/
	public void setVisible(final boolean newVisible);

}
