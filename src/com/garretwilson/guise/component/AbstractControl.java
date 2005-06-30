package com.garretwilson.guise.component;

import com.garretwilson.guise.model.Model;
import com.garretwilson.guise.session.GuiseSession;

/**An abstract implementation of a model component that allows user interaction to modify the model.
@author Garret Wilson
*/
public abstract class AbstractControl<M extends Model, C extends Control<M, C>> extends AbstractModelComponent<M, C> implements Control<M, C>
{

	/**Whether the control is enabled and can receive user input.*/
	private boolean enabled=true;

		/**@return Whether the control is enabled and can receive user input.*/
		public boolean isEnabled() {return enabled;}

		/**Sets whether the control is enabled and can receive user input.
		This is a bound property of type <code>Boolean</code>.
		@param newEnabled <code>true</code> if the component should indicate and accept user input.
		@see Control#ENABLED_PROPERTY
		*/
		public void setEnabled(final boolean newEnabled)
		{
			if(enabled!=newEnabled)	//if the value is really changing
			{
				final boolean oldEnabled=enabled;	//get the old value
				enabled=newEnabled;	//actually change the value
				firePropertyChange(ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled));	//indicate that the value changed
			}			
		}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractControl(final GuiseSession<?> session, final String id, final M model)
	{
		super(session, id, model);	//construct the parent class
	}

}
