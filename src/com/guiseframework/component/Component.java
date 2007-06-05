package com.guiseframework.component;

import java.io.IOException;

import javax.mail.internet.ContentType;

import com.garretwilson.beans.TargetedEvent;
import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.effect.Effect;
import com.guiseframework.component.layout.*;
import com.guiseframework.component.transfer.*;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.controller.*;
import com.guiseframework.event.*;
import com.guiseframework.geometry.*;
import com.guiseframework.input.Input;
import com.guiseframework.input.InputStrategy;
import com.guiseframework.model.*;
import com.guiseframework.model.ui.PresentationModel;
import com.guiseframework.theme.Theme;
import com.guiseframework.viewer.Viewer;

/**Base interface for all Guise components.
Each component must provide either a Guise session constructor; or a Guise session and string ID constructor.
Any component may contain other components, but only a {@link Container} allows for custom addition and removal of child components.
<p>A component takes up space regardless of whether it is set to be visible using {@link Component#setVisible(boolean)}.
{@link Component#setDisplayed(boolean)} determines whether the component is displayed at all. If a component is not displayed, it takes up no space.
If a component is not displayed, it is not visible regardless of whether it is set to be visible.
If a developer must hide sensitive data, the developer should remove the component from its parent container altogether.</p>
<p>For widest platform support the general {@link #ROUNDED_CORNER_RADIUS_EXTENT} constant should be used whenever possible when requesting rounded corners.</p>
@author Garret Wilson
*/
public interface Component<C extends Component<C>> extends PresentationModel, LabelModel
{

	/**The bound property of whether the component has bookmarks enabled.*/
	public final static String BOOKMARK_ENABLED_PROPERTY=getPropertyName(Component.class, "bookmarkEnabled");
	/**The bound property of the layout constraints.*/
	public final static String CONSTRAINTS_PROPERTY=getPropertyName(Component.class, "constraints");
	/**The bound property of the controller.*/
	public final static String CONTROLLER_PROPERTY=getPropertyName(Component.class, "controller");
	/**The description bound property.*/
	public final static String DESCRIPTION_PROPERTY=getPropertyName(Component.class, "description");
	/**The description content type bound property.*/
	public final static String DESCRIPTION_CONTENT_TYPE_PROPERTY=getPropertyName(Component.class, "descriptionContentType");
	/**The bound property of whether the component has dragging enabled.*/
	public final static String DRAG_ENABLED_PROPERTY=getPropertyName(Component.class, "dragEnabled");
	/**The bound property of whether the component has dropping enabled.*/
	public final static String DROP_ENABLED_PROPERTY=getPropertyName(Component.class, "dropEnabled");
	/**The bound property of whether the component has flyovers enabled.*/
	public final static String FLYOVER_ENABLED_PROPERTY=getPropertyName(Component.class, "flyoverEnabled");
	/**The bound property of the strategy controlling flyovers.*/
	public final static String FLYOVER_STRATEGY_PROPERTY=getPropertyName(Component.class, "flyoverStrategy");
	/**The info bound property.*/
	public final static String INFO_PROPERTY=getPropertyName(Component.class, "info");
	/**The info content type bound property.*/
	public final static String INFO_CONTENT_TYPE_PROPERTY=getPropertyName(Component.class, "infoContentType");
	/**The input strategy bound property.*/
	public final static String INPUT_STRATEGY_PROPERTY=getPropertyName(Component.class, "inputStrategy");
	/**The bound property of the component name.*/
	public final static String NAME_PROPERTY=getPropertyName(Component.class, "name");
	/**The bound property of the notification.*/
	public final static String NOTIFICATION_PROPERTY=getPropertyName(Component.class, "notification");
	/**The orientation bound property.*/
	public final static String ORIENTATION_PROPERTY=getPropertyName(Component.class, "orientation");
	/**The bound property of whether a theme has been applied to this component.*/
	public final static String THEME_APPLIED_PROPERTY=getPropertyName(Component.class, "themeApplied");
	/**The valid bound property.*/
	public final static String VALID_PROPERTY=getPropertyName(Component.class, "valid");
	/**The bound property of the viewer.*/
	public final static String VIEWER_PROPERTY=getPropertyName(Component.class, "viewer");

	/**@return The name of the component, not guaranteed to be unique (but guaranteed not to be the empty string) and useful only for searching for components within a component sub-hierarchy, or <code>null</code> if the component has no name.*/
	public String getName();

	/**Sets the name of the component.
	This is a bound property.
	@param newName The new name of the component, or <code>null</code> if the component should have no name.
	@exception IllegalArgumentException if the given name is the empty string.
	@see #NAME_PROPERTY
	*/
	public void setName(final String newName);

	/**@return The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.*/
	public String getInfo();

	/**Sets the advisory information text, such as might appear in a tooltip.
	This is a bound property.
	@param newInfo The new text of the advisory information, such as might appear in a tooltip.
	@see #INFO_PROPERTY
	*/
	public void setInfo(final String newInfo);

	/**@return The content type of the advisory information text.*/
	public ContentType getInfoContentType();

	/**Sets the content type of the advisory information text.
	This is a bound property.
	@param newInfoContentType The new advisory information text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #INFO_CONTENT_TYPE_PROPERTY
	*/
	public void setInfoContentType(final ContentType newInfoContentType);

	/**@return The description text, such as might appear in a flyover, or <code>null</code> if there is no description.*/
	public String getDescription();

	/**Sets the description text, such as might appear in a flyover.
	This is a bound property.
	@param newDescription The new text of the description, such as might appear in a flyover.
	@see #DESCRIPTION_PROPERTY
	*/
	public void setDescription(final String newDescription);

	/**@return The content type of the description text.*/
	public ContentType getDescriptionContentType();

	/**Sets the content type of the description text.
	This is a bound property.
	@param newDescriptionContentType The new description text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #DESCRIPTION_CONTENT_TYPE_PROPERTY
	*/
	public void setDescriptionContentType(final ContentType newDescriptionContentType);

	/**@return The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.*/
	public Constraints getConstraints();

	/**Sets the layout constraints of this component.
	This is a bound property.
	@param newConstraints The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.
	@see #CONSTRAINTS_PROPERTY
	*/
	public void setConstraints(final Constraints newConstraints);

	/**@return The strategy for processing input, or <code>null</code> if this component has no input strategy.*/
	public InputStrategy getInputStrategy();

	/**Sets the strategy for processing input.
	This is a bound property.
	@param newInputStrategy The new strategy for processing input, or <code>null</code> if this component is to have no input strategy.
	@see #INPUT_STRATEGY_PROPERTY
	*/
	public void setInputStrategy(final InputStrategy newInputStrategy);

	/**@return The notification associated with the component, or <code>null</code> if no notification is associated with this component.*/
	public Notification getNotification();

	/**Sets the component notification.
	This is a bound property.
	The notification is also fired as a {@link NotificationEvent} on this component if a new notification is given.
	Parents are expected to refire the notification event up the hierarchy.
	@param newNotification The notification for the component, or <code>null</code> if no notification is associated with this component.
	@see #NOTIFICATION_PROPERTY
	*/
	public void setNotification(final Notification newNotification);

	/**@return The controller installed in this component.*/
	public Controller<? extends GuiseContext, ? super C> getController();

	/**Sets the controller used by this component.
	This is a bound property.
	@param newController The new controller to use.
	@see #CONTROLLER_PROPERTY
	@exception NullPointerException if the given controller is <code>null</code>.
	*/
	public void setController(final Controller<? extends GuiseContext, ? super C> newController);

	/**@return The view installed in this component.*/
	public Viewer<? extends GuiseContext, ? super C> getViewer();

	/**Sets the viewer used by this component.
	This is a bound property.
	@param newViewer The new viewer to use.
	@see #VIEWER_PROPERTY
	@exception NullPointerException if the given viewer is <code>null</code>.
	*/
	public void setViewer(final Viewer<? extends GuiseContext, ? super C> newViewer);

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

	/**@return The parent of this component, or <code>null</code> if this component does not have a parent.*/
	public CompositeComponent<?> getParent();

	/**Retrieves the first ancestor of the given type.
	@param <A> The type of ancestor component requested.
	@param ancestorClass The class of ancestor component requested.
	@return The first ancestor component of the given type, or <code>null</code> if this component has no such ancestor.
	*/
	public <A extends CompositeComponent<?>> A getAncestor(final Class<A> ancestorClass);

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
	public void setParent(final CompositeComponent<?> newParent);

	/**@return Whether the component has dragging enabled.*/
	public boolean isDragEnabled();

	/**Sets whether the component has dragging enabled.
	This is a bound property of type {@link Boolean}.
	@param newDragEnabled <code>true</code> if the component should allow dragging, else <code>false</code>.
	@see #DRAG_ENABLED_PROPERTY
	*/
	public void setDragEnabled(final boolean newDragEnabled);

	/**@return Whether the component has dropping enabled.*/
	public boolean isDropEnabled();

	/**Sets whether the component has dropping enabled.
	This is a bound property of type {@link Boolean}.
	@param newDropEnabled <code>true</code> if the component should allow dropping, else <code>false</code>.
	@see #DROP_ENABLED_PROPERTY
	*/
	public void setDropEnabled(final boolean newDropEnabled);

	/**@return Whether flyovers are enabled for this component.*/
	public boolean isFlyoverEnabled();

	/**Sets whether flyovers are enabled for this component.
	Flyovers contain information from the component model's "description" property.
	This is a bound property of type {@link Boolean}.
	@param newFlyoverEnabled <code>true</code> if the component should display flyovers, else <code>false</code>.
	@see Model#getDescription()
	@see #FLYOVER_ENABLED_PROPERTY
	*/
	public void setFlyoverEnabled(final boolean newFlyoverEnabled);

	/**@return Whether a theme has been applied to this component.*/
	public boolean isThemeApplied();

	/**Sets whether a theme has been applied to this component.
	This is a bound property of type {@link Boolean}.
	@param newThemeApplied <code>true</code> if a theme has been applied to this component, else <code>false</code>.
	@see #THEME_APPLIED_PROPERTY
	*/
	public void setThemeApplied(final boolean newThemeApplied);

	/**Adds an export strategy to the component.
	The export strategy will take precedence over any compatible export strategy previously added.
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
	public boolean importTransfer(final Transferable<?> transferable);

	/**Initializes the component after construction.
	This method can only be called once during the life of a component.
	Subclasses should call this version.
	@exception IllegalStateException if this method has already been called.
	*/
	public void initialize();

	/**@return Whether the state of the component and all child components represents valid user input.*/
	public boolean isValid();

	/**Validates the user input of this component and all child components.
	The component will be updated with error information.
	@return The current state of {@link #isValid()} as a convenience.
	*/
	public boolean validate();

	/**Processes an event for the component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller.
	@param event The event to be processed.
	@see #getController()
	@see GuiseContext.State#PROCESS_EVENT
	*/
	public void processEvent(final ControlEvent event);

	/**Updates the view of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed view.
	@param context Guise context information.
	@exception IOException if there is an error updating the view.
	@see #getViewer()
	@see GuiseContext.State#UPDATE_VIEW
	*/
	public <GC extends GuiseContext> void updateView(final GC context) throws IOException;

	/**Update's this component's theme.
	This method checks whether a theme has been applied to this component.
	If no theme has been applied to the component, the current session theme will be applied by delegating to {@link #applyTheme(Theme)}.
	This method is called recursively for any child components before applying any theme on the component itself,
	to assure that child theme updates have already occured before theme updates occur for this component.
	There is normally no need to override this method or to call this method directly by applications.
	@exception IOException if there was an error loading or applying the theme.
	@see #applyTheme(Theme)
	@see #isThemeApplied()
	@see GuiseSession#getTheme()
	*/
	public void updateTheme() throws IOException;

	/**Dispatches an input event to this component and all child components, if any.
	If this is a {@link FocusedInputEvent}, the event will be directed towards the branch in which lies the focused component of any {@link InputFocusGroupComponent} ancestor of this component (or this component, if it is a focus group).
	If this is instead a {@link TargetedEvent}, the event will be directed towards the branch in which lies the target component of the event.
	Otherwise, the event will be dispatched to all child components.
	Only after the event has been dispatched to any children will the event be fired to any event listeners and then passed to the installed input strategy, if any.
	Once the event is consumed, no further processing takes place.
	@param inputEvent The input event to dispatch.
	@exception NullPointerException if the given event is <code>null</code>.
	@see TargetedEvent
	@see FocusedInputEvent
	@see InputEvent#isConsumed()
	@see #fireInputEvent(InputEvent)
	@see #getInputStrategy()
	@see InputStrategy#input(Input)
	*/
	public void dispatchInputEvent(final InputEvent inputEvent);

	/**Fire the given even to all registered listeners, if any.
	If the event is consumed further processing should cease.
	@param inputEvent The input event to fire.
	@exception NullPointerException if the given event is <code>null</code>.
	@see InputEvent#isConsumed()
	*/
	public void fireInputEvent(final InputEvent inputEvent);

	/**Applies a theme and its parents to this component.
	The theme's rules will be applied to this component and any related objects.
	Theme application occurs unconditionally, regardless of whether themes have been applied to this component before.
	This method may be overridden to effectively override theme settings by ensuring state of important properties after theme application. 
	There is normally no need to call this method directly by applications.
	If the theme is successfully applied, this method updates the theme applied status.
	@param theme The theme to apply to the component.
	@see #setThemeApplied(boolean)
	*/
	public void applyTheme(final Theme theme);

	/**Adds a command listener.
	@param commandListener The command listener to add.
	*/
	public void addCommandListener(final CommandListener commandListener);

	/**Removes a command listener.
	@param commandListener The command listener to remove.
	*/
	public void removeCommandListener(final CommandListener commandListener);

	/**@return <code>true</code> if there is one or more command listeners registered.*/
	public boolean hasCommandListeners();

	/**Adds a key listener.
	@param keyListener The key listener to add.
	*/
	public void addKeyListener(final KeyboardListener keyListener);

	/**Removes a key listener.
	@param keyListener The key listener to remove.
	*/
	public void removeKeyListener(final KeyboardListener keyListener);

	/**@return <code>true</code> if there is one or more key listeners registered.*/
	public boolean hasKeyListeners();

	/**Adds a mouse listener.
	@param mouseListener The mouse listener to add.
	*/
	public void addMouseListener(final MouseListener mouseListener);

	/**Removes a mouse listener.
	@param mouseListener The mouse listener to remove.
	*/
	public void removeMouseListener(final MouseListener mouseListener);

	/**@return all registered mouse listeners.*/
//TODO del if not needed	public Iterable<MouseListener> getMouseListeners();

	/**@return <code>true</code> if there is one or more mouse listeners registered.*/
	public boolean hasMouseListeners();

	/**Fires a mouse entered event to all registered mouse listeners.
	This method is used by the framework and should not be called directly by application code.
	@param componentBounds The absolute bounds of the component.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@exception NullPointerException if one or more of the arguments are <code>null</code>.
	@see MouseListener
	@see MouseEvent
	*/
//TODO del if not needed	public void fireMouseEntered(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition);

	/**Fires a mouse exited event to all registered mouse listeners.
	This method is used by the framework and should not be called directly by application code.
	@param componentBounds The absolute bounds of the component.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@exception NullPointerException if one or more of the arguments are <code>null</code>.
	@see MouseListener
	@see MouseEvent
	*/
//TODO del if not needed	public void fireMouseExited(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition);

	/**Adds a notification listener.
	@param notificationListener The notification listener to add.
	*/
	public void addNotificationListener(final NotificationListener notificationListener);

	/**Removes a notification listener.
	@param notificationListener The notification listener to remove.
	*/
	public void removeNotificationListener(final NotificationListener notificationListener);

	/**Notifies the user of the given notification information.
	The notification is stored in this component using {@link #setNotification(Notification)}, which fires appropriate notification events.
	This method calls {@link GuiseSession#notify(Notification)}.
	@param notification The notification information to relay.
	*/
	public void notify(final Notification notification);

	/**A strategy for showing and hiding flyovers in response to mouse events.
	@param <S> The type of component for which this object is to control flyovers.
	@author Garret Wilson
	*/
	public interface FlyoverStrategy<S extends Component<?>> extends MouseListener
	{
		/**@return The requested line extent (width in left-to-right top-to-bottom orientation) of the flyover component, or <code>null</code> if no preferred line extent has been specified.*/
		public Extent getLineExtent();

		/**Sets the requested line extent (width in left-to-right top-to-bottom orientation) of the flyover component.
		@param newLineExtent The new requested line extent of the flyover component, or <code>null</code> there is no line extent preference.
		*/
		public void setLineExtent(final Extent newLineExtent);

		/**@return The requested page extent (height in left-to-right top-to-bottom orientation) of the flyover component, or <code>null</code> if no preferred page extent has been specified.*/
		public Extent getPageExtent();

		/**Sets the requested page extent (height in left-to-right top-to-bottom orientation) of the flyover component.
		@param newPageExtent The new requested page extent of the flyover component, or <code>null</code> there is no page extent preference.
		*/
		public void setPageExtent(final Extent newPageExtent);

		/**@return The style identifier of the flyover, or <code>null</code> if there is no style ID.*/
		public String getStyleID();

		/**Identifies the style for the flyover component.
		@param newStyleID The style identifier of the flyover, or <code>null</code> if there is no style ID.
		*/
		public void setStyleID(final String newStyleID);

		/**@return The effect used for opening the flyover, or <code>null</code> if there is no open effect.*/
		public Effect getOpenEffect();

		/**Sets the effect used for opening the flyover.
		@param newEffect The new effect used for opening the flyover, or <code>null</code> if there should be no open effect.
		@see Frame#OPEN_EFFECT_PROPERTY 
		*/
		public void setOpenEffect(final Effect newOpenEffect);

		/**Shows a flyover for the component.*/
		public void openFlyover();

		/**Closes the flyover for the component.*/
		public void closeFlyover();
	}

}
