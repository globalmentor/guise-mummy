package com.javaguise.component;

import com.javaguise.component.layout.MenuLayout;
import com.javaguise.model.MenuModel;

/**A group of components arranged as a menu.
This component uses a {@link MenuModel} and a {@link MenuLayout}.
@author Garret Wilson
@see MenuLayout
@see MenuModel
*/
public interface Menu<C extends Menu<C>> extends Container<C>, Control<C>
{

	/**@return The data model used by this component.*/
	public MenuModel getModel();

	/**@return The layout definition for the menu.*/
	public MenuLayout getLayout();

}
