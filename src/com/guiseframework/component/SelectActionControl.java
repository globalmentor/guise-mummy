package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;

import com.guiseframework.model.Selectable;

/**An action control that keeps track of its selected state.
If the control is set to be toggled, when the action is initiated the selected state alternates between <code>true</code> and <code>false</code>.
Otherwise, an action sets the selected state to <code>true</code>.
The control defaults to auto-select mode. If this mode is turned off, no selection or toggling occurs automatically when the action occurs.
@author Garret Wilson
*/
public interface SelectActionControl<C extends SelectActionControl<C>> extends ActionControl<C>, Selectable
{
	/**The auto-select bound property.*/
	public final static String AUTO_SELECT_PROPERTY=getPropertyName(SelectActionControl.class, "autoSelect");
	/**The selected icon bound property.*/
	public final static String SELECTED_ICON_PROPERTY=getPropertyName(SelectActionControl.class, "selectedIcon");
	/**The toggle bound property.*/
	public final static String TOGGLE_PROPERTY=getPropertyName(SelectActionControl.class, "toggle");
	/**The unselected icon bound property.*/
	public final static String UNSELECTED_ICON_PROPERTY=getPropertyName(SelectActionControl.class, "unselectedIcon");

	/**@return Whether this control automatically sets or toggles the selection state when the action occurs.*/
	public boolean isAutoSelect();

	/**Sets whether this control automatically sets or toggles the selection state when the action occurs.
	This is a bound property of type <code>Boolean</code>.
	@param newAutoSelect <code>true</code> if the control should automatically set or toggle the selection state when an action occurs.
	@see #AUTO_SELECT_PROPERTY
	*/
	public void setAutoSelect(final boolean newAutoSelect);

	/**@return The selected icon URI, which may be a resource URI, or <code>null</code> if there is no selected icon URI.*/
	public URI getSelectedIcon();

	/**Sets the URI of the selected icon.
	This is a bound property of type <code>URI</code>.
	@param newSelectedIcon The new URI of the selected icon, which may be a resource URI.
	@see #SELECTED_ICON_PROPERTY
	*/
	public void setSelectedIcon(final URI newSelectedIcon);

	/**@return Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.*/
	public boolean isToggle();

	/**Sets whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.
	This is a bound property of type <code>Boolean</code>.
	@param newToggle <code>true</code> if the component should act as a toggle, else <code>false</code> if the action should unconditionally set the value to <code>true</code>.
	@see #TOGGLE_PROPERTY
	*/
	public void setToggle(final boolean newToggle);

	/**@return The unselected icon URI, which may be a resource URI, or <code>null</code> if there is no unselected icon URI.*/
	public URI getUnselectedIcon();

	/**Sets the URI of the unselected icon.
	This is a bound property of type <code>URI</code>.
	@param newUnselectedIcon The new URI of the unselected icon, which may be a resource URI.
	@see #UNSELECTED_ICON_PROPERTY
	*/
	public void setUnselectedIcon(final URI newUnselectedIcon);

}
