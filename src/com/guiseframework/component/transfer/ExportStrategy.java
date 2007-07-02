package com.guiseframework.component.transfer;

import com.guiseframework.component.Component;

/**A strategy for exporting data from a component.
@param <C> The type of component supported by this export strategy.
@author Garret Wilson
*/
public interface ExportStrategy<C extends Component>
{

	/**Exports data from the given component.
	@param component The component from which data will be transferred.
	@return The object to be transferred, or <code>null</code> if no data can be transferred.
	*/
	public Transferable<C> exportTransfer(final C component);
}
