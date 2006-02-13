package com.guiseframework.component;

/**A frame for communication of an option.
An option frame defaults to a single composite child panel with a row of options along the bottom.
The contents of an option dialog frame should be accessed by {@link #getOptionContent()} and {@link #setOptionContent(Component)}.
@param <O> The type of options available.
@author Garret Wilson
*/
public interface OptionDialogFrame<O, C extends OptionDialogFrame<O, C>> extends DialogFrame<O, C>
{

	/**@return The component representing option contents, or <code>null</code> if this frame does not have an option contents component.*/ 
	public Component<?> getOptionContent();

	/**Sets the component representing option contents.
	@param newOptionContent The single option contents component, or <code>null</code> if this frame does not have an option contents component.
	*/
	public void setOptionContent(final Component<?> newOptionContent);

	/**@return The container containing the options.*/
	public Container<?> getOptionContainer();

}
