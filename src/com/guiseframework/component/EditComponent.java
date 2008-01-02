package com.guiseframework.component;

import static com.globalmentor.java.Classes.*;

import com.guiseframework.event.EditListenable;

/**A component that has content that can be edited.
@author Garret Wilson
*/
public interface EditComponent extends Component, EditListenable
{

	/**The editable bound property.*/
	public final static String EDITABLE_PROPERTY=getPropertyName(EditComponent.class, "editable");

	/**@return Whether the value is editable and the component will allow the the user to change the value.*/
	public boolean isEditable();

	/**Sets whether the value is editable and the component will allow the the user to change the value.
	This is a bound property of type {@link Boolean}.
	@param newEditable <code>true</code> if the component should allow the user to change the value.
	@see #EDITABLE_PROPERTY
	*/
	public void setEditable(final boolean newEditable);

}
