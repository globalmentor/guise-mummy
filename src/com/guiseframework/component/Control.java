package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

/**A component that accepts user interaction to manipulate a data model.
@author Garret Wilson
*/
public interface Control<C extends Control<C>> extends Component<C>
{

	/**The editable bound property.*/
	public final static String EDITABLE_PROPERTY=getPropertyName(Control.class, "editable");	//TODO decide if this should be moved down to ValueControl
	/**The enabled bound property.*/
	public final static String ENABLED_PROPERTY=getPropertyName(Control.class, "enabled");
	/**The valid bound property.*/
	public final static String VALID_PROPERTY=getPropertyName(Control.class, "valid");

	/**@return Whether the control is enabled and can receive user input.*/
	public boolean isEnabled();

	/**Sets whether the control is enabled and can receive user input..
	This is a bound property of type <code>Boolean</code>.
	@param newEnabled <code>true</code> if the control should indicate and accept user input.
	@see #ENABLED_PROPERTY
	*/
	public void setEnabled(final boolean newEnabled);

	/**@return Whether the text literal value represents a valid value for the model.*/
	public boolean isValid();

	/**Sets whether the text literal value represents a valid value for the value model.
	This is a bound property of type <code>Boolean</code>.
	@param newValid <code>true</code> if the text literal and model value should be considered valid.
	@see Control#VALID_PROPERTY
	*/
//TODO del	public void setValid(final boolean newValid);

}
