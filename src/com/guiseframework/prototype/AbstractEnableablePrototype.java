package com.guiseframework.prototype;

import java.net.URI;

import com.guiseframework.model.Enableable;

/**Contains enableable prototype information, appropriate for a control, for example.
@author Garret Wilson
*/
public abstract class AbstractEnableablePrototype extends AbstractPrototype implements Enableable
{
	/**Whether the control is enabled and can receive user input.*/
	private boolean enabled=true;

		/**@return Whether the control is enabled and can receive user input.*/
		public boolean isEnabled() {return enabled;}

		/**Sets whether the control is enabled and and can receive user input.
		This is a bound property of type <code>Boolean</code>.
		@param newEnabled <code>true</code> if the control should indicate and accept user input.
		@see #ENABLED_PROPERTY
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

	/**Default constructor.*/
	public AbstractEnableablePrototype()
	{
		this(null);	//construct the class with no label
	}

	/**Label constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public AbstractEnableablePrototype(final String label)
	{
		this(label, null);	//construct the label model with no icon
	}

	/**Label and icon constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	*/
	public AbstractEnableablePrototype(final String label, final URI icon)
	{
		super(label, icon);	//construct the parent class
	}
}
