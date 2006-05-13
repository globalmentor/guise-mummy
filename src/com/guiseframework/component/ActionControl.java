package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.model.ActionModel;

/**A general control with an action model.
@author Garret Wilson
*/
public interface ActionControl<C extends ActionControl<C>> extends Control<C>, ActionModel
{
	/**The bound property of whether the icon is displayed or has no representation, taking up no space.*/
	public final static String ICON_DISPLAYED_PROPERTY=getPropertyName(ActionControl.class, "iconDisplayed");
	/**The bound property of whether the label is displayed or has no representation, taking up no space.*/
	public final static String LABEL_DISPLAYED_PROPERTY=getPropertyName(ActionControl.class, "labelDisplayed");

	/**@return Whether the icon is displayed.*/
	public boolean isIconDisplayed();

	/**Sets whether the icon is displayed.
	This is a bound property of type <code>Boolean</code>.
	@param newIconDisplayed <code>true</code> if the icon should be displayed, else <code>false</code> if the icon should not be displayed and take up no space.
	@see #ICON_DISPLAYED_PROPERTY
	*/
	public void setIconDisplayed(final boolean newIconDisplayed);

	/**@return Whether the label is displayed.*/
	public boolean isLabelDisplayed();

	/**Sets whether the label is displayed.
	This is a bound property of type <code>Boolean</code>.
	@param newLabelDisplayed <code>true</code> if the label should be displayed, else <code>false</code> if the label should not be displayed and take up no space.
	@see #LABEL_DISPLAYED_PROPERTY
	*/
	public void setLabelDisplayed(final boolean newLabelDisplayed);
}
