package com.javaguise.component;

import java.io.IOException;
import java.util.Collection;

import com.garretwilson.beans.PropertyBindable;
import com.javaguise.context.GuiseContext;
import com.javaguise.controller.Controller;
import static com.garretwilson.lang.ClassUtilities.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;
import com.javaguise.validator.ValidationsException;

/**Base interface for all Guise components.
Each component must provide either a Guise session constructor; or a Guise session and string ID constructor.
Any component may contain other components, but only a {@link Container} allows for custom addition and removal of child components.
A component is iterable over its child components, and can be used in short <code>for(:)</code> form. 
@author Garret Wilson
*/
public interface Component<C extends Component<C>> extends PropertyBindable, Iterable<Component<?>>
{

	/**The bound property of the controller.*/
	public final static String CONTROLLER_PROPERTY=getPropertyName(Component.class, "controller");
	/**The bound property of the component style ID.*/
	public final static String STYLE_ID_PROPERTY=getPropertyName(Component.class, "styleID");
	/**The bound property of whether the component is visible.*/
	public final static String VISIBLE_PROPERTY=getPropertyName(Component.class, "visible");

	/**@return Whether this component has children.*/
	public boolean hasChildren();

	/**@return The model used by this component.*/
	public Controller<? extends GuiseContext<?>, ? super C> getController();

	/**Sets the controller used by this component.
	This is a bound property.
	@param newController The new controller to use.
	@see #CONTROLLER_PROPERTY
	*/
	public void setController(final Controller<? extends GuiseContext<?>, ? super C> newController);

	/**@return An iterable interface to all errors associated with this component.*/
	public Iterable<Throwable> getErrors();

	/**@return <code>true</code> if there is at least one error associated with this component.*/
	public boolean hasErrors();

	/**Adds an error to the component.
	@param error The error to add.
	*/
	public void addError(final Throwable error);

	/**Adds errors to the component.
	@param errors The errors to add.
	*/
	public void addErrors(final Collection<? extends Throwable> errors);

	/**Removes a specific error from this component.
	@param error The error to remove.
	*/
	public void removeError(final Throwable error);

	/**Clears all errors associated with this component.*/
	public void clearErrors();

	/**@return The component identifier.*/
	public String getID();

	/**@return An identifier unique within this component's parent, if any.*/
	public String getUniqueID();

	/**@return An identifier unique up this component's hierarchy.*/
	public String getAbsoluteUniqueID();

	/**Determines the unique ID of the provided child component within this component.
	This method is typically called by child components when determining their own unique IDs.
	@param childComponent A component within this component.
	@return An identifier of the given component unique within this component.
	@exception IllegalArgumentException if the given component is not a child of this component.
	*/
	public String getUniqueID(final Component<?> childComponent);

	/**Determines the absolute unique ID of the provided child component up the component's hierarchy.
	This method is typically called by child components when determining their own absolute unique IDs.
	@param childComponent A component within this component.
	@return An absolute identifier of the given component unique up the component's hierarchy.
	@exception IllegalArgumentException if the given component is not a child of this component.
	*/
	public String getAbsoluteUniqueID(final Component<?> childComponent);

	/**@return The parent of this component, or <code>null</code> if this component does not have a parent.*/
	public Component<?> getParent();

	/**Retrieves the first ancestor of the given type.
	@param <C> The type of ancestor component requested.
	@param ancestorClass The class of ancestor component requested.
	@return The first ancestor component of the given type, or <code>null</code> if this component has no such ancestor.
	*/
	public <A extends Component<?>> A getAncestor(final Class<A> ancestorClass);

	/**Sets the parent of this component.
	This method is managed by containers, and should usually never be called my other classes.
	In order to hinder inadvertent incorrect use, the parent must only be set after the component is added to the component, and only be unset after the component is removed from the component.
	If a component is given the same parent it already has, no action occurs.
	@param newParent The new parent for this component, or <code>null</code> if this component is being removed from a component.
	@exception IllegalStateException if a parent is provided and this component already has a parent.
	@exception IllegalStateException if no parent is provided and this component's old parent still recognizes this component as its child.
	@exception IllegalArgumentException if a parent is provided and the given parent does not already recognize this component as its child.
	*/
	public void setParent(final Component<?> newParent);

	/**@return The Guise session that owns this component.*/
	public GuiseSession<?> getSession();

	/**@return The style identifier, or <code>null</code> if there is no style ID.*/
	public String getStyleID();

	/**Identifies the style for the component.
	This is a bound property.
	@param newStyleID The style identifier, or <code>null</code> if there is no style ID.
	@see #STYLE_ID_PROPERTY
	*/
	public void setStyleID(final String newStyleID);

	/**@return Whether the component is visible.*/
	public boolean isVisible();

	/**Sets whether the component is visible.
	This is a bound property of type <code>Boolean</code>.
	@param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
	@see #VISIBLE_PROPERTY
	*/
	public void setVisible(final boolean newVisible);

	/**@return Whether the models of this component and all of its child components are valid.*/
	public boolean isValid();

	/**@return The character used by this component when building absolute IDs.*/
	public char getAbsoluteIDSegmentDelimiter();

	/**Collects the current data from the view of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error querying the view.
	@see GuiseContext.State#QUERY_VIEW
	@see #getController(GC, C)
	*/
	public <GC extends GuiseContext<?>> void queryView(final GC context) throws IOException;

	/**Decodes the data of the view of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error decoding the view.
	@exception ValidationsException if the view information is in an invalid format and cannot be decoded.
	@see #getController(GC, C)
	@see GuiseContext.State#DECODE_VIEW
	*/
	public <GC extends GuiseContext<?>> void decodeView(final GC context) throws IOException, ValidationsException;

	/**Validates the view of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error validating the view.
	@exception ValidationsException if the view information is not valid to store in the model.
	@see #getController(GC, C)
	@see GuiseContext.State#VALIDATE_VIEW
	*/
	public <GC extends GuiseContext<?>> void validateView(final GC context) throws IOException, ValidationsException;

	/**Updates the model of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error updating the model.
	@exception ValidationsException if the view information is not valid to store in the model.
	@see #getController(GC, C)
	@see GuiseContext.State#UPDATE_MODEL
	*/
	public <GC extends GuiseContext<?>> void updateModel(final GC context) throws IOException, ValidationsException;

	/**Collects the current data from the model of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error querying the model.
	@see #getController(GC, C)
	@see GuiseContext.State#QUERY_MODEL
	*/
	public <GC extends GuiseContext<?>> void queryModel(final GC context) throws IOException;

	/**Encodes the data of the model of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error encoding the model.
	@see #getController(GC, C)
	@see GuiseContext.State#ENCODE_MODEL
	*/
	public <GC extends GuiseContext<?>> void encodeModel(final GC context) throws IOException;

	/**Updates the view of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller, and if no controller is installed one is created and installed.
	@param context Guise context information.
	@exception IOException if there is an error updating the view.
	@see #getController(GC, C)
	@see GuiseContext.State#UPDATE_VIEW
	*/
	public <GC extends GuiseContext<?>> void updateView(final GC context) throws IOException;
}
