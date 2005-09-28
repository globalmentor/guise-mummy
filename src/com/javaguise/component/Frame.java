package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.javaguise.model.LabelModel;

/**A root-level component such as a window or an HTML page.
The title is specified by the frame model's label.
@author Garret Wilson
*/
public interface Frame<C extends Frame<C>> extends CompositeComponent<C>
{
	/**The component bound property.*/
	public final static String COMPONENT_PROPERTY=getPropertyName(Frame.class, "component");

	/**@return The data model used by this component.*/
	public LabelModel getModel();

	/**@return The single child component, or <code>null</code> if this frame does not have a child component.*/
	public Component<?> getComponent();

	/**Sets the single child component.
	This is a bound property
	@param newComponent The single child component, or <code>null</code> if this frame does not have a child component.
	@see #COMPONENT_PROPERTY
	*/
	public void setComponent(final Component<?> newComponent);

}
