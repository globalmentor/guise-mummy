package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.component.layout.MenuLayout;

/**A group of components arranged as a menu.
This component uses a {@link MenuModel} and a {@link MenuLayout}.
@author Garret Wilson
@see MenuLayout
@see MenuModel
*/
public interface Menu extends ContainerControl, ActionControl, LabelDisplayableComponent
{

	/**The open bound property.*/
	public final static String OPEN_PROPERTY=getPropertyName(Menu.class, "open");
	/**The bound property of the rollover state.*/
	public final static String ROLLOVER_PROPERTY=getPropertyName(Menu.class, "rollover");
	/**The bound property of whether children will be displayed upon rollover.*/
	public final static String ROLLOVER_OPEN_ENABLED_PROPERTY=getPropertyName(Menu.class, "rolloverOpenEnabled");

	/**@return The layout definition for the menu.*/
	public MenuLayout getLayout();

	/**@return Whether the menu is open.*/
	public boolean isOpen();

	/**Sets whether the menu is open.
	This is a bound property of type <code>Boolean</code>.
	@param newOpen <code>true</code> if the menu should be open.
	@see #OPEN_PROPERTY
	*/
	public void setOpen(final boolean newOpen);

	/**@return Whether the component is in a rollover state.*/
	public boolean isRollover();

	/**Sets whether the component is in a rollover state.
	This is a bound property of type <code>Boolean</code>.
	@param newRollover <code>true</code> if the component should be in a rollover state, else <code>false</code>.
	@see #ROLLOVER_PROPERTY
	*/
	public void setRollover(final boolean newRollover);

	/**@return Whether the menu children will be shown during rollover.*/
	public boolean isRolloverOpenEnabled();

	/**Sets whether the menu children will be shown during rollover.
	If rollover open is enabled, the open state will not actually be changed during rollover.
	This is a bound property of type <code>Boolean</code>.
	@param newRolloverOpenEnabled <code>true</code> if the component should allow display during rollover, else <code>false</code>.
	@see #ROLLOVER_OPEN_ENABLED_PROPERTY
	*/
	public void setRolloverOpenEnabled(final boolean newRolloverOpenEnabled);
}
