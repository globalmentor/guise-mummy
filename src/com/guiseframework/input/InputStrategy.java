package com.guiseframework.input;

/**A strategy for processing input.
@author Garret Wilson
*/
public interface InputStrategy
{

	/**@return The parent input strategy, or <code>null</code> if there is no parent input strategy.*/
	public InputStrategy getParent();

	/**Processes input, returning whether the input was consumed.
	If the input is not consumed by this input strategy, it is sent to the parent input strategy, if any, for processing.
	@param input The input to process.
	@return <code>true</code> if the input was consumed and should not be processed further.
	@exception NullPointerException if the given input is <code>null</code>.
	*/
	public boolean input(final Input input);

}
