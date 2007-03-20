package com.guiseframework.component;

import java.net.URI;

import com.guiseframework.model.*;
import com.guiseframework.prototype.ValuePrototype;
import com.guiseframework.validator.ValueRequiredValidator;

import static com.garretwilson.lang.ClassUtilities.*;

/**Control accepting boolean input rendered as a check, either a checked square or a circle. 
<p>If no check type is specified, the check type will automatically determined dynamically.
If there is a {@link MutualExclusionPolicyModelGroup} value listener, indicating that this is a mutual exclusion control, a {@link CheckType#ELLIPSE} check type will be used.
Otherwise, a {@link CheckType#RECTANGLE} check type will be used.</p>
<p>The default model used by a check control defaults to a value of {@link Boolean#FALSE}, as a check control does not have the capability of indicating <code>null</code>.</p>
<p>A check control automatically installs a {@link ValueRequiredValidator}, as a check control does not have the capability of indicating <code>null</code>.</p>
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

	/**The type of check area presented to the user, or <code>null</code> if the check type should be determined automatically.*/
	private CheckType checkType=null;

		/**@return The type of check area presented to the user, or <code>null</code> if the check type should be determined automatically.*/
		public CheckType getCheckType() {return checkType;}

		/**Sets the type of check area to present to the user.
		This is a bound property.
		@param newCheckType The type of check area to use, or <code>null</code> if the check type should be determined automatically.
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

	/**Default constructor with a default label model and a default value model.*/
	public CheckControl()
	{
		this(new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE));	//construct the class with a value model
	}

	/**Check type constructor with a default label model and value model.
	@param checkType The type of check area presented to the user, or <code>null</code> if the check type should be determined automatically.
	*/
	public CheckControl(final CheckType checkType)
	{
		this(new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE), checkType);	//construct the class with a default value model
	}

	/**Label constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public CheckControl(final String label)
	{
		this(label, null);	//construct the label model with no icon		
	}

	/**Label and icon constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	*/
	public CheckControl(final String label, final URI icon)
	{
		this(new DefaultLabelModel(label, icon));	//construct the class, indicating that a default value model should be used
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public CheckControl(final LabelModel labelModel)
	{
		this(labelModel, new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE));	//construct the class, indicating that a default value model should be used
	}

	/**Label model and value model constructor.
	@param labelModel The component label model.
	@param valueModel The component value model.
	@exception NullPointerException if the given label model and/or value model is <code>null</code>.
	*/
	public CheckControl(final LabelModel labelModel, final ValueModel<Boolean> valueModel)
	{
		this(labelModel, valueModel, null);	//construct the class, indicating that the default check type should be used
	}

	/**Value model constructor.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public CheckControl(final ValueModel<Boolean> valueModel)
	{
		this(valueModel, null);	//construct the class with no specified check type
	}

	/**Value model and check type constructor.
	@param valueModel The component value model.
	@param checkType The type of check area presented to the user, or <code>null</code> if the check type should be determined automatically.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public CheckControl(final ValueModel<Boolean> valueModel, final CheckType checkType)
	{
		this(new DefaultLabelModel(), valueModel, checkType);	//construct the class with a default label model
	}

	/**Label model, value model and check type constructor.
	@param labelModel The component label model.
	@param valueModel The component value model.
	@param checkType The type of check area presented to the user, or <code>null</code> if the check type should be determined automatically.
	@exception NullPointerException if the given value model, and/or label model is <code>null</code>.
	*/
	public CheckControl(final LabelModel labelModel, final ValueModel<Boolean> valueModel, final CheckType checkType)
	{
		this(labelModel, valueModel, new DefaultEnableable(), checkType);	//construct the class with default enableable support
	}

	/**Label model, value model, enableable object, and check type constructor.
	@param labelModel The component label model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@param checkType The type of check area presented to the user, or <code>null</code> if the check type should be determined automatically.
	@exception NullPointerException if the given value model and/or label model is <code>null</code>.
	*/
	public CheckControl(final LabelModel labelModel, final ValueModel<Boolean> valueModel, final Enableable enableable, final CheckType checkType)
	{
		super(labelModel, valueModel, enableable);	//construct the parent class
		this.checkType=checkType;	//save the check type
		setValidator(new ValueRequiredValidator<Boolean>());	//require a value, as a check control cannot indicate the absence of a value
	}

	/**Prototype constructor.
	@param valuePrototype The prototype on which this component should be based.
	*/
	public CheckControl(final ValuePrototype<Boolean> valuePrototype)
	{
		this(valuePrototype, valuePrototype, valuePrototype, null);	//use the value prototype as every needed model, using an automatic check type
	}

}
