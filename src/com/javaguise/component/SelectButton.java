package com.javaguise.component;

import com.javaguise.event.*;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.*;

/**Button that stores a boolean value in its model representing the selected state.
A validator requiring a non-<code>null</code> value is automatically installed.
@author Garret Wilson
*/
public class SelectButton extends AbstractValueButtonControl<Boolean, SelectButton> implements SelectActionControl<SelectButton>
{

	/**Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.*/
	private boolean toggle=false;

		/**@return Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.*/
		public boolean isToggle() {return toggle;}

		/**Sets whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.
		This is a bound property of type <code>Boolean</code>.
		@param newToggle <code>true</code> if the component should act as a toggle, else <code>false</code> if the action should unconditionally set the value to <code>true</code>.
		@see SelectActionControl#TOGGLE_PROPERTY
		*/
		public void setToggle(final boolean newToggle)
		{
			if(toggle!=newToggle)	//if the value is really changing
			{
				final boolean oldToggle=toggle;	//get the current value
				toggle=newToggle;	//update the value
				firePropertyChange(TOGGLE_PROPERTY, Boolean.valueOf(oldToggle), Boolean.valueOf(newToggle));
			}
		}

	/**Session constructor with a default data model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public SelectButton(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public SelectButton(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultActionValueModel<Boolean>(session, Boolean.class, Boolean.FALSE));	//construct the class with a default model
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public SelectButton(final GuiseSession session, final ActionValueModel<Boolean> model)
	{
		this(session, null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public SelectButton(final GuiseSession session, final String id, final ActionValueModel<Boolean> model)
	{
		super(session, id, model);	//construct the parent class
		model.setValidator(new ValueRequiredValidator<Boolean>(session));	//install a value-required validator
		model.addActionListener(new ActionListener<ActionModel>()	//listen for an action and set the selected state accordingly
				{		
					public void actionPerformed(final ActionEvent<ActionModel> actionEvent)	//if an action occurs
					{
						final Boolean newSelected;
						if(isToggle())	//if we should toggle
						{
							final Boolean oldSelected=model.getValue();	//get the old selection value
							newSelected=Boolean.TRUE.equals(oldSelected) ? Boolean.FALSE : Boolean.TRUE;	//toggle the selection value, compensating for null just for safety
						}
						else	//if we shouldn't toggle
						{
							newSelected=Boolean.TRUE;	//just set the selection to true
						}
						try
						{
							model.setValue(newSelected);	//updated the model's selected status
						}
						catch(final ValidationException validationException)
						{
							throw new AssertionError(validationException);	//TODO improve error handling
						}
					}
				});
	}

}
