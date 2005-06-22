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

	/**@return The component identifier.*/
	public String getID();

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


	/**@return The model used by this component.*/
	public <GC extends GuiseContext, C extends Component> Controller<GC, C> getController();

	/**Sets the controller used by this component.
	This is a bound property.
	@param newController The new controller to use.
	@see Component#CONTROLLER_PROPERTY
	*/
	public <GC extends GuiseContext, C extends Component> void setController(final Controller<GC, C> newController);

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
