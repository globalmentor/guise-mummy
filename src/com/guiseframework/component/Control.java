package com.guiseframework.component;

import static com.globalmentor.java.Classes.*;

import com.guiseframework.model.Enableable;

/**A component that accepts user interaction to manipulate a data model.
@author Garret Wilson
*/
public interface Control extends Component, InputFocusableComponent, Enableable
{

	/**The input status of a control.*/
	public enum Status
	{
		/**The input is provisionally incorrect.*/
		WARNING,
		
		/**The input is incorrect.*/
		ERROR;
	}

	/**The status bound property.*/
	public final static String STATUS_PROPERTY=getPropertyName(Control.class, "status");

	/**@return The status of the current user input, or <code>null</code> if there is no status to report.*/
	public Status getStatus();

	/**Resets the control to its default value.*/
	public void reset();

}
