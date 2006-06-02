package com.guiseframework.component.transfer;

import com.guiseframework.component.Component;

/**A strategy for importing data into a component.
@param <C> The type of component supported by this export strategy.
@author Garret Wilson
*/
public interface ImportStrategy<C extends Component<?>>
{

	/**Determines whether this strategy can import the given transferable object.
	@param component The component into which the object will be transferred.
	@param transferable The object to be transferred.
	@return <code>true</code> if the given object can be imported.
	*/
	public boolean canImportTransfer(final C component, final Transferable<?> transferable);

	/**Imports the given data into the given component.
	@param component The component into which the object will be transferred.
	@param transferable The object to be transferred.
	@return <code>true</code> if the given object was imported.
	*/
	public boolean importTransfer(final C component, final Transferable<?> transferable);
}
