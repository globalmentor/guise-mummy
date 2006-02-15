package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.ActionValueModel;

/**Abstract implementation of an action control containing a value.
@author Garret Wilson
@param <V> The type of value the control represents.
*/
public abstract class AbstractActionValueControl<V, C extends ActionValueControl<V, C>> extends AbstractActionControl<C> implements ActionValueControl<V, C>
{

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ActionValueModel<V> getModel() {return (ActionValueModel<V>)super.getModel();}

	/**Whether the value is editable and the control will allow the the user to change the value.*/
	private boolean editable=true;

		/**@return Whether the value is editable and the control will allow the the user to change the value.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the value is editable and the control will allow the the user to change the value.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the control should allow the user to change the value.
		@see #EDITABLE_PROPERTY
		*/
		public void setEditable(final boolean newEditable)
		{
			if(editable!=newEditable)	//if the value is really changing
			{
				final boolean oldEditable=editable;	//get the old value
				editable=newEditable;	//actually change the value
				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractActionValueControl(final GuiseSession session, final String id, final ActionValueModel<V> model)
	{
		super(session, id, model);	//construct the parent class
	}
}
