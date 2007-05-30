package com.guiseframework.input;

/**An abstract encapsulation of user input from a mouse.
@author Garret Wilson
*/
public abstract class AbstractMouseInput extends AbstractGestureInput implements MouseInput
{

	/**Keys constructor.
	@param keys The keys that were pressed when this input occurred.
	@exception NullPointerException if the given keys is <code>null</code>.
	*/
	public AbstractMouseInput(final Key... keys)
	{
		super(keys);	//construct the parent class
	}

}
