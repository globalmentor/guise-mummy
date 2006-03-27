package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.CardLayout;

/**An abstract panel with a card layout.
The component valid status is updated before a change in the {@link #VALUE_PROPERTY} or the {@link #VALIDATOR_PROPERTY} is fired. 
@author Garret Wilson
@see CardLayout
*/
public abstract class AbstractCardPanel<C extends Panel<C> & CardControl<C>> extends AbstractListSelectContainerControl<C> implements Panel<C>, CardControl<C>
{

		//TODO make sure we listen for enabled status changing on the layout and send an index enabled property change, maybe

	/**The list select model used by this component.*/
//TODO del	private final ListSelectModel<Component<?>> listSelectModel;

		/**@return The list select model used by this component.*/
//TODO del		protected ListSelectModel<Component<?>> getListSelectModel() {return listSelectModel;}
	
	/**@return The layout definition for the container.*/
	@SuppressWarnings("unchecked")
	public CardLayout getLayout() {return (CardLayout)super.getLayout();}

	/**Session, ID, and layout constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	protected AbstractCardPanel(final GuiseSession session, final String id, final CardLayout layout)
	{
		super(session, id, layout);	//construct the parent class
	}

}
