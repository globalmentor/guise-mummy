package com.javaguise.component.layout;

import com.javaguise.component.Component;

/**A layout that defines locations of components in internationalized relative terms.
This layout uses default constraints of {@link Region#CENTER}.
@author Garret Wilson
@see Region
*/
public class RegionLayout extends AbstractLayout<Region>
{

	/**Constructor.*/
	public RegionLayout()
	{
	}

	/**Creates default constraints for the given component.
	This implementation returns {@link Region#CENTER}.
	@param component The component for which constraints should be provided.
	@return New default constraints for the given component.
	*/
	public Region createDefaultConstraints(final Component<?> component)
	{
		return Region.CENTER;	//default to the center region
	}

}
