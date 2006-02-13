package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.ControlModel;
import com.guiseframework.model.Model;

/**An abstract implementation of a model component that allows user interaction to modify the model.
@author Garret Wilson
*/
public abstract class AbstractControl<C extends Control<C>> extends AbstractComponent<C> implements Control<C>
{

	/**Whether the state of the control represents valid user input.*/
//TODO fix	private boolean valid=true;

		/**@return Whether the state of the control represents valid user input.*/
//TODO fix		public boolean isValid() {return valid;}

		/**Sets whether the state of the control represents valid user input
		This is a bound property of type <code>Boolean</code>.
		@param newValid <code>true</code> if user input should be considered valid
		@see Control#VALID_PROPERTY
		*/
/*TODO fix
		public void setValid(final boolean newValid)
		{
			if(valid!=newValid)	//if the value is really changing
			{
				final boolean oldValid=valid;	//get the current value
				valid=newValid;	//update the value
				firePropertyChange(VALID_PROPERTY, Boolean.valueOf(oldValid), Boolean.valueOf(newValid));
			}
		}
*/

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ControlModel getModel() {return (ControlModel)super.getModel();}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractControl(final GuiseSession session, final String id, final Model model)
	{
		super(session, id, model);	//construct the parent class
	}

}
