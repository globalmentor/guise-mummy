package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.model.ControlModel;

/**A component that accepts user interaction to manipulate a data model.
@author Garret Wilson
*/
public interface Control<C extends Control<C>> extends Component<C>, LabeledComponent<C>
{

	/**The valid bound property.*/
	public final static String VALID_PROPERTY=getPropertyName(Control.class, "valid");

	/**@return Whether the text literal value represents a valid value for the model.*/
	public boolean isValid();

	/**@return The data model used by this component.*/
	public ControlModel getModel();

	/**Sets whether the text literal value represents a valid value for the value model.
	This is a bound property of type <code>Boolean</code>.
	@param newValid <code>true</code> if the text literal and model value should be considered valid.
	@see Control#VALID_PROPERTY
	*/
//TODO del	public void setValid(final boolean newValid);

}
