package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

/**A component that supports different modes of interaction, such as an editable label or a modal frame.
@author Garret Wilson
*/
public interface ModalComponent<C extends ModalComponent<C>> extends Component<C>
{
	/**The bound property of the mode.*/
	public final static String MODE_PROPERTY=getPropertyName(ModalComponent.class, "mode");

	/**@return The current mode of interaction, or <code>null</code> if the component is in a modeless state.*/
	public Mode getMode();

	/**Sets the mode of interaction.
	This is a bound property.
	@param newMode The new mode of component interaction.
	@see #MODE_PROPERTY 
	*/
	public void setMode(final Mode newMode);
	
}
