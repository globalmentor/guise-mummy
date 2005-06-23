package com.garretwilson.guise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.io.IOException;

import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.Controller;

/**Base interface for all Guise components.
@author Garret Wilson
*/
public interface Component
{

	/**The bound property of the controller.*/
	public final static String CONTROLLER_PROPERTY=getPropertyName(Component.class, "controller");
	/**The bound property of the component style ID.*/
	public final static String STYLE_ID_PROPERTY=getPropertyName(Component.class, "styleID");
	/**The bound property of whether the component is visible.*/
	public final static String VISIBLE_PROPERTY=getPropertyName(Component.class, "visible");

	/**@return The model used by this component.*/
	public <GC extends GuiseContext, C extends Component> Controller<GC, C> getController();

	/**Sets the controller used by this component.
	This is a bound property.
	@param newController The new controller to use.
	@see Component#CONTROLLER_PROPERTY
	*/
	public <GC extends GuiseContext, C extends Component> void setController(final Controller<GC, C> newController);

	/**@return The component identifier.*/
	public String getID();

	/**@return The container parent of this component, or <code>null</code> if this component is not embedded in any container.*/
	public Container getParent();

	/**Retrieves the first ancestor of the given type.
	@param <C> The type of ancestor container requested.
	@param ancestorClass The class of ancestor container requested.
	@return The first ancestor container of the given type, or <code>null</code> if this component has no such ancestor.
	*/
	public <C extends Container> C getAncestor(final Class<C> ancestorClass);

	/**Sets the parent of this component.
	This method is managed by containers, and should usually never be called my other classes.
	In order to hinder inadvertent incorrect use, the parent must only be set after the component is added to the container, and only be unset after the component is removed from the container.
	If a component is given the same parent it already has, no action occurs.
	@param newParent The new parent for this component, or <code>null</code> if this component is being removed from a container.
	@exception IllegalStateException if a parent is provided and this component already has a parent.
	@exception IllegalStateException if no parent is provided and this component's old parent still recognizes this component as its child.
	@exception IllegalArgumentException if a parent is provided and the given parent does not already recognize this component as its child.
	*/
	public void setParent(final Container newParent);

	/**@return The style identifier, or <code>null</code> if there is no style ID.*/
	public String getStyleID();

	/**Identifies the style for the component.
	This is a bound property.
	@param newStyleID The style identifier, or <code>null</code> if there is no style ID.
	@see STYLE_ID_PROPERTY
	*/
	public void setStyleID(final String newStyleID);

	/**@return Whether the component is visible.*/
	public boolean isVisible();

	/**Sets whether the component is visible.
	This is a bound property of type <code>Boolean</code>.
	@param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
	@see VISIBLE_PROPERTY
	*/
	public void setVisible(final boolean newVisible);

	/**Updates the view of this component.
	This method delegates to the isntalled controller, and if no controller is installed one is created.
	@param context Guise context information.
	@param component The component being rendered.
	@exception IOException if there is an error updating the view.
	*/
	public <GC extends GuiseContext> void updateView(final GC context) throws IOException;

	/**Updates the model of this component.
	@param context Guise context information.
	@exception IOException if there is an error updating the model.
	*/
	public <GC extends GuiseContext> void updateModel(final GC context) throws IOException;
}