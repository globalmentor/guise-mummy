package com.javaguise.component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.beans.PropertyBindable;
import com.garretwilson.util.ReverseIterator;
import com.javaguise.component.layout.Orientation;
import com.javaguise.component.transfer.ExportStrategy;
import com.javaguise.component.transfer.ImportStrategy;
import com.javaguise.component.transfer.Transferable;
import com.javaguise.context.GuiseContext;
import com.javaguise.controller.Controller;
import static com.garretwilson.lang.ClassUtilities.*;

import com.javaguise.session.GuiseSession;
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
	/**The bound property of whether the component has dragging enabled.*/
	public final static String DRAG_ENABLED_PROPERTY=getPropertyName(Component.class, "dragEnabled");
	/**The bound property of whether the component has dropping enabled.*/
	public final static String DROP_ENABLED_PROPERTY=getPropertyName(Component.class, "dropEnabled");
	/**The orientation bound property.*/
	public final static String ORIENTATION_PROPERTY=getPropertyName(Component.class, "orientation");
	/**The bound property of the component style ID.*/
	public final static String STYLE_ID_PROPERTY=getPropertyName(Component.class, "styleID");
	/**The bound property of whether the component is visible.*/
	public final static String VISIBLE_PROPERTY=getPropertyName(Component.class, "visible");

	/**@return Whether this component has children.*/
	public boolean hasChildren();

	/**@return An iterator to contained components in reverse order.*/
	public Iterator<Component<?>> reverseIterator();

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

	/**Returns this component's requested orientation.
	To resolve the orientation up the hierarchy, {@link #getComponentOrientation()} should be used.
	@return The internationalization orientation of the component's contents, or <code>null</code> if the default orientation should be used.
	@see #getComponentOrientation()
	*/
	public Orientation getOrientation();

	/**Determines the internationalization orientation of the component's contents.
	This method returns the local orientation value, if there is one.
	If there is no orientation specified for this component, the request is deferred to this component's parent.
	If there is no parent component, a default orientation is retrieved from the current session.
	@return The internationalization orientation of the component's contents.
	@see #getOrientation()
	@see GuiseSession#getOrientation()
	*/
	public Orientation getComponentOrientation();

	/**Sets the orientation.
	This is a bound property
	@param newOrientation The new internationalization orientation of the component's contents, or <code>null</code> if default orientation should be determined based upon the session's locale.
	@see #ORIENTATION_PROPERTY
	*/
	public void setOrientation(final Orientation newOrientation);

	/**@return An identifier unique within this component's parent, if any.*/
//TODO del when works	public String getUniqueID();

	/**@return An identifier unique up this component's hierarchy.*/
//TODO del when works	public String getAbsoluteUniqueID();

	/**Determines the unique ID of the provided child component within this component.
	This method is typically called by child components when determining their own unique IDs.
	@param childComponent A component within this component.
	@return An identifier of the given component unique within this component.
	@exception IllegalArgumentException if the given component is not a child of this component.
	*/
//TODO del when works	public String getUniqueID(final Component<?> childComponent);

	/**Determines the absolute unique ID of the provided child component up the component's hierarchy.
	This method is typically called by child components when determining their own absolute unique IDs.
	@param childComponent A component within this component.
	@return An absolute identifier of the given component unique up the component's hierarchy.
	@exception IllegalArgumentException if the given component is not a child of this component.
	*/
//TODO del when works	public String getAbsoluteUniqueID(final Component<?> childComponent);

	/**@return The parent of this component, or <code>null</code> if this component does not have a parent.*/
	public Component<?> getParent();

	/**Retrieves the first ancestor of the given type.
	@param <C> The type of ancestor component requested.
	@param ancestorClass The class of ancestor component requested.
	@return The first ancestor component of the given type, or <code>null</code> if this component has no such ancestor.
	*/
	public <A extends Component<?>> A getAncestor(final Class<A> ancestorClass);

	/**Sets the parent of this component.
	This method is managed by containers, and normally should not be called by applications.
	A component cannot be given a parent if it already has a parent.
	A component's parent cannot be removed if that parent is a container and this component is still a child of that container.
	A container's parent cannot be set to a container unless that container already recognizes this component as one of its children.
	If a component is given the same parent it already has, no action occurs.
	@param newParent The new parent for this component, or <code>null</code> if this component is being removed from a parent.
	@exception IllegalStateException if a parent is provided and this component already has a parent.
	@exception IllegalStateException if no parent is provided and this component's old parent is a container that still recognizes this component as its child.
	@exception IllegalArgumentException if a parent container is provided and the given parent container does not already recognize this component as its child.
	@see Container#add(Component)
	@see Container#remove(Component)
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

	/**@return Whether the component has dragging enabled.*/
	public boolean isDragEnabled();

	/**Sets whether the component is has dragging enabled.
	This is a bound property of type <code>Boolean</code>.
	@param newDragEnabled <code>true</code> if the component should allow dragging, else false, else <code>false</code>.
	@see #DRAG_ENABLED_PROPERTY
	*/
	public void setDragEnabled(final boolean newDragEnabled);

	/**@return Whether the component has dropping enabled.*/
	public boolean isDropEnabled();

	/**Sets whether the component is has dropping enabled.
	This is a bound property of type <code>Boolean</code>.
	@param newDropEnabled <code>true</code> if the component should allow dropping, else false, else <code>false</code>.
	@see #DROP_ENABLED_PROPERTY
	*/
	public void setDropEnabled(final boolean newDropEnabled);

	/**Adds an export strategy to the component.
	The export strategy will take prececence over any compatible export strategy previously added.
	@param exportStrategy The export strategy to add.
	*/
	public void addExportStrategy(final ExportStrategy<? super C> exportStrategy);

	/**Removes an export strategy from the component.
	@param exportStrategy The export strategy to remove.
	*/
	public void removeExportStrategy(final ExportStrategy<? super C> exportStrategy);

	/**Exports data from the component.
	Each export strategy, from last to first added, will be asked to export data, until one is successful.
	@return The object to be transferred, or <code>null</code> if no data can be transferred.
	*/
	public Transferable exportTransfer();

	/**Adds an import strategy to the component.
	The import strategy will take prececence over any compatible import strategy previously added.
	@param importStrategy The importstrategy to add.
	*/
	public void addImportStrategy(final ImportStrategy<? super C> importStrategy);

	/**Removes an import strategy from the component.
	@param importStrategy The import strategy to remove.
	*/
	public void removeImportStrategy(final ImportStrategy<? super C> importStrategy);

	/**Imports data to the component.
	Each import strategy, from last to first added, will be asked to import data, until one is successful.
	@param transferable The object to be transferred.
	@return <code>true</code> if the given object was be imported.
	*/
	public boolean importTransfer(final Transferable transferable);

	/**@return Whether the models of this component and all of its child components are valid.*/
	public boolean isValid();

	/**@return The character used by this component when building absolute IDs.*/
//TODO del when works	public char getAbsoluteIDSegmentDelimiter();

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
