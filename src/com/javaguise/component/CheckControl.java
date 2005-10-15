package com.javaguise.component;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

/**Control accepting boolean input rendered as a check, either a checked square or a circle. 
<p>The default model used by a check control defaults to a value of {@link Boolean#FALSE}, as a check control does not have the capability of indicating <code>null</code>.</p>
@author Garret Wilson
*/
public class CheckControl extends AbstractValueControl<Boolean, CheckControl>
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

	/**The type of check area presented to the user; the default is a rectangle.*/
	private CheckType checkType=CheckType.RECTANGLE;

		/**@return The type of check area presented to the user; the default is a rectangle.*/
		public CheckType getCheckType() {return checkType;}

		/**Sets the type of check area to present to the user.
		This is a bound property.
		@param newCheckType The type of check area to use.
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

	/**Session constructor with a default boolean data model and a default rectangle check type.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CheckControl(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session constructor with a default boolean data model.
	@param session The Guise session that owns this component.
	@param checkType The type of check area presented to the user.
	@exception NullPointerException if the given session and/or check type is <code>null</code>.
	*/
	public CheckControl(final GuiseSession session, final CheckType checkType)
	{
		this(session, null, checkType);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default boolean data model and default rectangle check type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CheckControl(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultValueModel<Boolean>(session, Boolean.class, Boolean.FALSE));	//construct the class with a default model
	}

	/**Session, ID, and check type constructor with a default boolean data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param checkType The type of check area presented to the user.
	@exception NullPointerException if the given session and/or check type is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CheckControl(final GuiseSession session, final String id, final CheckType checkType)
	{
		this(session, id, new DefaultValueModel<Boolean>(session, Boolean.class, Boolean.FALSE), checkType);	//construct the class with a default model
	}

	/**Session and model constructor with a default rectangle check type.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public CheckControl(final GuiseSession session, final ValueModel<Boolean> model)
	{
		this(session, null, model);	//construct the class, indicating that a default ID should be used
	}

	/**Session, ID, and model constructor with a default rectangle check type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CheckControl(final GuiseSession session, final String id, final ValueModel<Boolean> model)
	{
		this(session, id, model, CheckType.RECTANGLE);	//construct the class with a rectangle check type
	}

	/**Session, ID, model, and check type constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param checkType The type of check area presented to the user.
	@exception NullPointerException if the given session, model, and/or check type is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CheckControl(final GuiseSession session, final String id, final ValueModel<Boolean> model, final CheckType checkType)
	{
		super(session, id, model);	//construct the parent class
		this.checkType=checkNull(checkType, "Check type cannot be null");	//save the check type
	}

}
