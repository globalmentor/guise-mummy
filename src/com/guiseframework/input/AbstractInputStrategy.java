package com.guiseframework.input;

/**An abstract implementation of a strategy for processing input.
@author Garret Wilson
*/
public class AbstractInputStrategy implements InputStrategy
{

	/**The parent input strategy, or <code>null</code> if there is no parent input strategy.*/
	private final InputStrategy parent;

		/**@return The parent input strategy, or <code>null</code> if there is no parent input strategy.*/
		public InputStrategy getParent() {return parent;}

	/**Parent constructor.
	@param parent The parent input strategy, or <code>null</code> if there is no parent input strategy.
	*/
	public AbstractInputStrategy(final InputStrategy parent)
	{
		this.parent=parent;	//save the parent
	}

	/**Processes input, returning whether the input was consumed.
	If the input is not consumed by this input strategy, it is sent to the parent input strategy, if any, for processing.
	This version does nothing besides delegate to the parent input strategy.
	@param input The input to process.
	@return <code>true</code> if the input was consumed and should not be processed further.
	@exception NullPointerException if the given input is <code>null</code>.
	*/
	public boolean input(final Input input)
	{
		final InputStrategy parent=getParent();	//get the parent
		if(parent!=null)	//if there is a parent input strategy
		{
			return parent.input(input);	//pass the input to the parent
		}
		else	//if there is no parent
		{
			return false;	//indicate that the input was not consumed
		}
	}
}
