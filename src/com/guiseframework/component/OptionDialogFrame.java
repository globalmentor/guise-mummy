package com.guiseframework.component;

import java.util.List;

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

	/**@return The read-only list of available options in order.*/
	public List<O> getOptions();

	/**Returns the component that represents the specified option.
	@param option The option for which a component should be returned.
	@return The component, such as a button, that represents the given option, or <code>null</code> if there is no component that represents the given option.
	*/
	public Component<?> getOptionComponent(final O option);

}
