package com.guiseframework.prototype;

import com.guiseframework.model.DefaultLabelModel;
import com.guiseframework.model.Enableable;

/**Contains enableable prototype information, appropriate for a control, for example.
@author Garret Wilson
*/
public abstract class AbstractEnableablePrototype extends DefaultLabelModel implements Enableable
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
}
