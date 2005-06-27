package com.garretwilson.guise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.garretwilson.guise.model.*;
import com.garretwilson.guise.session.GuiseSession;

/**Control accepting boolean input rendered as a check, either a checked square or a circle. 
@author Garret Wilson
*/
public class CheckControl extends ValueControl<Boolean>
{

	/**The check type bound property.*/
	public final static String CHECK_TYPE_PROPERTY=getPropertyName(CheckControl.class, "checkType");

	/**The type of check area to present to the user.*/
	public enum CheckType
	{
		/**A four-cornered check box, such as a square.*/
		RECTANGLE,
		
		/**A round fill-in oval, such as a circle.*/
		ELLIPSE;
	}

	/**The type of check are presented to the user; the default is a rectangle.*/
	private CheckType checkType=CheckType.RECTANGLE;

		/**@return The type of check are presented to the user; the default is a rectangle..*/
		public CheckType getCheckType() {return checkType;}

		/**Sets the type of check area to present to the user.
		This is a bound property.
		@param newCheckType The type of check are to use.
		@see #CHECK_TYPE_PROPERTY 
		*/
		public void setCheckType(final CheckType newCheckType)
		{
			if(checkType!=newCheckType)	//if the value is really changing
			{
				final CheckType oldCheckType=checkType;	//get the old value
				checkType=newCheckType;	//actually change the value
				firePropertyChange(CHECK_TYPE_PROPERTY, oldCheckType, newCheckType);	//indicate that the value changed
			}			
		}

	/**Session constructor with a default boolean data model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CheckControl(final GuiseSession<?> session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default boolean data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CheckControl(final GuiseSession<?> session, final String id)
	{
		this(session, id, new DefaultValueModel<Boolean>(session, Boolean.class));	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CheckControl(final GuiseSession<?> session, final String id, final ValueModel<Boolean> model)
	{
		super(session, id, model);	//construct the parent class
	}

}
