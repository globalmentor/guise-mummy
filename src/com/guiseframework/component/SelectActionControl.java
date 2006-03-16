package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;

import com.guiseframework.model.Selectable;

/**An action control that keeps track of its selected state.
If the control is set to be toggled, when the action is initiated the selected state alternates between <code>true</code> and <code>false</code>.
Otherwise, an action sets the selected state to <code>true</code>.
@author Garret Wilson
*/
public interface SelectActionControl<C extends SelectActionControl<C>> extends ActionControl<C>, Selectable
{
	/**The selected icon bound property.*/
	public final static String SELECTED_ICON_PROPERTY=getPropertyName(SelectActionControl.class, "selectedIcon");
	/**The selected icon resource key bound property.*/
	public final static String SELECTED_ICON_RESOURCE_KEY_PROPERTY=getPropertyName(SelectActionControl.class, "selectedIconResourceKey");
	/**The toggle bound property.*/
	public final static String TOGGLE_PROPERTY=getPropertyName(SelectActionControl.class, "toggle");

	/**@return The selected icon URI, or <code>null</code> if there is no selected icon URI.*/
	public URI getSelectedIcon();

	/**Sets the URI of the selected icon.
	This is a bound property of type <code>URI</code>.
	@param newSelectedIcon The new URI of the selected icon.
	@see #SELECTED_ICON_PROPERTY
	*/
	public void setSelectedIcon(final URI newSelectedIcon);

	/**@return The selected icon URI resource key, or <code>null</code> if there is no selected icon URI resource specified.*/
	public String getSelectedIconResourceKey();

	/**Sets the key identifying the URI of the selected icon in the resources.
	This is a bound property.
	@param newSelectedIconResourceKey The new selected icon URI resource key.
	@see #SELECTED_ICON_RESOURCE_KEY_PROPERTY
	*/
	public void setSelectedIconResourceKey(final String newSelectedIconResourceKey);

	/**@return Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.*/
	public boolean isToggle();

	/**Sets whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.
	This is a bound property of type <code>Boolean</code>.
	@param newToggle <code>true</code> if the component should act as a toggle, else <code>false</code> if the action should unconditionally set the value to <code>true</code>.
	@see #TOGGLE_PROPERTY
	*/
	public void setToggle(final boolean newToggle);

}
