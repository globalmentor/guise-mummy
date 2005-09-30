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
	/**The bound property of whether the component is moveable.*/
	public final static String MOVEABLE_PROPERTY=getPropertyName(Frame.class, "moveable");

	/**@return Whether the frame is moveable.*/
	public boolean isMoveable();

	/**Sets whether the frame is moveable.
	This is a bound property of type <code>Boolean</code>.
	@param newMoveable <code>true</code> if the frame should be moveable, else <code>false</code>.
	@see #MOVEABLE_PROPERTY
	*/
	public void setMoveable(final boolean newMoveable);

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

	/**Determines whether the frame should be allowed to close.
	This method is called from {@link #close()}.
	@return <code>true</code> if the frame should be allowed to close.
	*/
	public boolean canClose();
	
	/**Closes the frame.
	This method calls {@link #canClose()} and only performs closing functionality if that method returns <code>true</code>. 
	*/
	public void close();
}
