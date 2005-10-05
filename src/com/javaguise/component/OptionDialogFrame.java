package com.javaguise.component;

/**A frame for communication of an option.
An option frame defaults to a single composite child panel with a row of options along the bottom.
A center content component within the child panel may be specified.
@param <O> The type of options available.
@author Garret Wilson
*/
public interface OptionDialogFrame<O, C extends OptionDialogFrame<O, C>> extends DialogFrame<O, C>
{

	/**@return The container component used to hold content, including the child component.*/
	public Container<?> getContainer();

	/**@return The container containing the options.*/
	public Container<?> getOptionContainer();

}
