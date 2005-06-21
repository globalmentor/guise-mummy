package com.garretwilson.guise.component;

/**A model for button state.
@author Garret Wilson
*/
public interface ButtonModel
{

	/**@return Whether the button is pressed.*/
	public boolean isPressed();

	/**Sets whether the button is pressed.
	@param newPressed Whether the button should be in the pressed state.
	*/
	public void setPressed(final boolean newPressed);

}
