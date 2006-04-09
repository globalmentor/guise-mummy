package com.guiseframework.component;

import com.guiseframework.model.*;

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

	/**Default constructor with a default label model, a default value model, and default rectangle check type.*/
	public CheckControl()
	{
		this(new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE));	//construct the class with a value model
	}

	/**Check type constructor with a default label model and value model.
	@param checkType The type of check area presented to the user.
	@exception NullPointerException if the given check type is <code>null</code>.
	*/
	public CheckControl(final CheckType checkType)
	{
		this(new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE), checkType);	//construct the class with a default value model
	}

	/**Label model constructor with a default rectangle check type.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public CheckControl(final LabelModel labelModel)
	{
		this(labelModel, new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE));	//construct the class, indicating that a default value model should be used
	}

	/**Label model and value model constructor with a default rectangle check type.
	@param labelModel The component label model.
	@param valueModel The component value model.
	@exception NullPointerException if the given label model and/or value model is <code>null</code>.
	*/
	public CheckControl(final LabelModel labelModel, final ValueModel<Boolean> valueModel)
	{
		this(labelModel, valueModel, CheckType.RECTANGLE);	//construct the class, indicating that the check type should be rectangle
	}

	/**Value model constructor with a default rectangle check type.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public CheckControl(final ValueModel<Boolean> valueModel)
	{
		this(valueModel, CheckType.RECTANGLE);	//construct the class with a rectangle check type
	}

	/**Value model and check type constructor.
	@param valueModel The component value model.
	@param checkType The type of check area presented to the user.
	@exception NullPointerException if the given value model and/or check type is <code>null</code>.
	*/
	public CheckControl(final ValueModel<Boolean> valueModel, final CheckType checkType)
	{
		this(new DefaultLabelModel(), valueModel, checkType);	//construct the class with a default label model
	}

	/**Label model, value model and check type constructor.
	@param labelModel The component label model.
	@param valueModel The component value model.
	@param checkType The type of check area presented to the user.
	@exception NullPointerException if the given value model, label model, and/or check type is <code>null</code>.
	*/
	public CheckControl(final LabelModel labelModel, final ValueModel<Boolean> valueModel, final CheckType checkType)
	{
		super(labelModel, valueModel);	//construct the parent class
		this.checkType=checkInstance(checkType, "Check type cannot be null");	//save the check type
	}

}
