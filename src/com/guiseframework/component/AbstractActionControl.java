package com.guiseframework.component;

import java.util.MissingResourceException;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.ActionModel;
import com.guiseframework.model.LabelModel;

/**Abstract control with an action model.
@author Garret Wilson
*/
public abstract class AbstractActionControl<C extends ActionControl<C>> extends AbstractControl<C> implements ActionControl<C>
{

	/**@return The data model used by this component.*/
	public ActionModel getModel() {return (ActionModel)super.getModel();}

	/**Determines the text of the label.
	If a label is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The label text, or <code>null</code> if there is no label text.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getLabelResourceKey()
	*/
	public String getLabelText() throws MissingResourceException	//TODO testing
	{
		return getModel().getLabel();	//TODO fix
	}

	/**Sets the text of the label.
	This is a bound property.
	@param newLabel The new text of the label.
	@see LabelModel#LABEL_PROPERTY
	*/
	public void setLabelText(final String newLabel)	//TODO testing
	{
		getModel().setLabel(newLabel);	//TODO fix
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractActionControl(final GuiseSession session, final String id, final ActionModel model)
	{
		super(session, id, model);	//construct the parent class
	}
}
