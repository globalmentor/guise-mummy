package com.javaguise.component;

import com.javaguise.component.layout.*;
import com.javaguise.model.MenuModel;
import com.javaguise.session.GuiseSession;

/**An abstract menu component.
This implementation initially closes any child menu added to this menu.
@author Garret Wilson
*/
public abstract class AbstractMenu<C extends Menu<C>> extends AbstractContainer<C> implements Menu<C>  
{

	/**@return The data model used by this component.*/
	public MenuModel getModel() {return (MenuModel)super.getModel();}

	/**@return The layout definition for the menu.*/
	public MenuLayout getLayout() {return (MenuLayout)super.getLayout();}	//a menu can only have a menu layout

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractMenu(final GuiseSession session, final String id, final MenuLayout layout, final MenuModel model)
	{
		super(session, id, layout, model);	//construct the parent class
	}

	/**Adds a child component.
	If this component is itself a menu, this version closes that menu. 
	@param component The component to add to this component.
	*/
	protected void addComponent(final Component<?> component)
	{
		super.addComponent(component);	//do the default adding
		if(component instanceof Menu)	//if the component is a menu
		{
			((Menu<?>)component).getModel().setOpen(false);	//close the child menu
		}
	}

}
