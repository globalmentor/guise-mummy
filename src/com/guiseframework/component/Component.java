package com.guiseframework.component;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.mail.internet.ContentType;

import com.garretwilson.beans.PropertyBindable;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.effect.Effect;
import com.guiseframework.component.layout.Constraints;
import com.guiseframework.component.layout.Corner;
import com.guiseframework.component.layout.Orientation;
import com.guiseframework.component.transfer.*;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.controller.ControlEvent;
import com.guiseframework.controller.Controller;
import com.guiseframework.event.*;
import com.guiseframework.geometry.*;
import com.guiseframework.model.*;
import com.guiseframework.style.Color;
import com.guiseframework.view.View;

import static com.garretwilson.lang.ClassUtilities.*;

/**Base interface for all Guise components.
Each component must provide either a Guise session constructor; or a Guise session and string ID constructor.
Any component may contain other components, but only a {@link Container} allows for custom addition and removal of child components.
A component is iterable over its child components, and can be used in short <code>for(:)</code> form.
<p>A component takes up space regardless of whether it is set to be visible using {@link Component#setVisible(boolean)}.
{@link Component#setDisplayed(boolean)} determines whether the component is displayed at all. If a component is not displayed, it takes up no space.
If a component is not displayed, it is not visible regardless of whether it is set to be visible.
If a developer must hide sensitive data, the developer should remove the component from its parent container altogether.</p>
<p>For widest platform support the general {@link #ROUNDED_CORNER_RADIUS_EXTENT} constant should be used whenever possible when requesting rounded corners.</p> 
@author Garret Wilson
*/
public interface Component<C extends Component<C>> extends PropertyBindable, LabelModel, Displayable
{

	/**The bound property of the background color.*/
	public final static String BACKGROUND_COLOR_PROPERTY=getPropertyName(Component.class, "backgroundColor");
	/**The bound property of the color.*/
	public final static String COLOR_PROPERTY=getPropertyName(Component.class, "color");
	/**The bound property of the layout constraints.*/
	public final static String CONSTRAINTS_PROPERTY=getPropertyName(Component.class, "constraints");
	/**The bound property of the controller.*/
	public final static String CONTROLLER_PROPERTY=getPropertyName(Component.class, "controller");
	/**The bound property of the line near page near corner arc size.*/
	public final static String CORNER_ARC_SIZE_LINE_NEAR_PAGE_NEAR_PROPERTY=getPropertyName(Component.class, "cornerArcSizeLineNearPageNear");
	/**The bound property of the line far page near corner arc size.*/
	public final static String CORNER_ARC_SIZE_LINE_FAR_PAGE_NEAR_PROPERTY=getPropertyName(Component.class, "cornerArcSizeLineFarPageNear");
	/**The bound property of the line near page far corner arc size.*/
	public final static String CORNER_ARC_SIZE_LINE_NEAR_PAGE_FAR_PROPERTY=getPropertyName(Component.class, "cornerArcSizeLineNearPageFar");
	/**The bound property of the line far page far corner arc size.*/
	public final static String CORNER_ARC_SIZE_LINE_FAR_PAGE_FAR_PROPERTY=getPropertyName(Component.class, "cornerArcSizeLineFarPageFar");
	/**The description bound property.*/
	public final static String DESCRIPTION_PROPERTY=getPropertyName(Component.class, "description");
	/**The description content type bound property.*/
	public final static String DESCRIPTION_CONTENT_TYPE_PROPERTY=getPropertyName(Component.class, "descriptionContentType");
	/**The description resource key bound property.*/
	public final static String DESCRIPTION_RESOURCE_KEY_PROPERTY=getPropertyName(Component.class, "descriptionResourceKey");
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
	/**The info resource key bound property.*/
	public final static String INFO_RESOURCE_KEY_PROPERTY=getPropertyName(Component.class, "infoResourceKey");
	/**The bound property of the component name.*/
	public final static String NAME_PROPERTY=getPropertyName(Component.class, "name");
	/**The bound property of whether the component has tooltips enabled.*/
	public final static String TOOLTIP_ENABLED_PROPERTY=getPropertyName(Component.class, "tooltipEnabled");
	/**The bound property of the notification.*/
	public final static String NOTIFICATION_PROPERTY=getPropertyName(Component.class, "notification");
	/**The opacity bound property.*/
	public final static String OPACITY_PROPERTY=getPropertyName(Component.class, "opacity");
	/**The orientation bound property.*/
	public final static String ORIENTATION_PROPERTY=getPropertyName(Component.class, "orientation");
	/**The preferred height bound property.*/
	public final static String PREFERRED_HEIGHT_PROPERTY=getPropertyName(Component.class, "preferredHeight");
	/**The preferred width bound property.*/
	public final static String PREFERRED_WIDTH_PROPERTY=getPropertyName(Component.class, "preferredWidth");
	/**The bound property of the component style ID.*/
	public final static String STYLE_ID_PROPERTY=getPropertyName(Component.class, "styleID");
	/**The valid bound property.*/
	public final static String VALID_PROPERTY=getPropertyName(Component.class, "valid");
	/**The bound property of the view.*/
	public final static String VIEW_PROPERTY=getPropertyName(Component.class, "view");
	/**The bound property of whether the component is visible.*/
	public final static String VISIBLE_PROPERTY=getPropertyName(Component.class, "visible");

	/*The constant value representing a general rounded corner.*/
	public final static Dimensions ROUNDED_CORNER_ARC_SIZE=new Dimensions(0.25, 0.25, Extent.Unit.EM);
	
	/**The character used to combine hierarchical IDs.*/
	public final static char ID_SEGMENT_DELIMITER='.';

	/**@return The data model used by this component.*/
//TODO del	public Model getModel();

	/**@return The name of the component, not guaranteed to be unique and useful only for searching for components within a component sub-hierarchy, or <code>null</code> if the component has no name.*/
	public String getName();

	/**Sets the name of the component.
	This is a bound property.
	@param newName The new name of the component, or <code>null</code> if the component should have no name.
	@see #NAME_PROPERTY 
	*/
	public void setName(final String newName);

	/**@return The label icon URI, or <code>null</code> if there is no icon URI.*/
	public URI getIcon();

	/**Sets the URI of the label icon.
	This is a bound property of type <code>URI</code>.
	@param newLabelIcon The new URI of the label icon.
	@see #ICON_PROPERTY
	*/
	public void setIcon(final URI newLabelIcon);

	/**@return The label icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
	public String getIconResourceKey();

	/**Sets the key identifying the URI of the label icon in the resources.
	This is a bound property.
	@param newIconResourceKey The new label icon URI resource key.
	@see #ICON_RESOURCE_KEY_PROPERTY
	*/
	public void setIconResourceKey(final String newIconResourceKey);

	/**@return The label text, or <code>null</code> if there is no label text.*/
	public String getLabel();

	/**Sets the text of the label.
	This is a bound property.
	@param newLabelText The new text of the label.
	@see #LABEL_PROPERTY
	*/
	public void setLabel(final String newLabelText);

	/**@return The content type of the label text.*/
	public ContentType getLabelContentType();

	/**Sets the content type of the label text.
	This is a bound property.
	@param newLabelTextContentType The new label text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #LABEL_CONTENT_TYPE_PROPERTY
	*/
	public void setLabelContentType(final ContentType newLabelTextContentType);

	/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
	public String getLabelResourceKey();

	/**Sets the key identifying the text of the label in the resources.
	This is a bound property.
	@param newLabelTextResourceKey The new label text resource key.
	@see #LABEL_RESOURCE_KEY_PROPERTY
	*/
	public void setLabelResourceKey(final String newLabelTextResourceKey);

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

	/**@return The advisory information text resource key, or <code>null</code> if there is no advisory information text resource specified.*/
	public String getInfoResourceKey();

	/**Sets the key identifying the text of the advisory information in the resources.
	This is a bound property.
	@param newInfoResourceKey The new advisory information text resource key.
	@see #INFO_RESOURCE_KEY_PROPERTY
	*/
	public void setInfoResourceKey(final String newInfoResourceKey);

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

	/**@return The description text resource key, or <code>null</code> if there is no description text resource specified.*/
	public String getDescriptionResourceKey();

	/**Sets the key identifying the text of the description in the resources.
	This is a bound property.
	@param newDescriptionResourceKey The new description text resource key.
	@see #DESCRIPTION_RESOURCE_KEY_PROPERTY
	*/
	public void setDescriptionResourceKey(final String newDescriptionResourceKey);

	/**@return The background color of the component, or <code>null</code> if no background color is specified for this component.*/
	public Color<?> getBackgroundColor();

	/**Sets the background color of the component.
	This is a bound property.
	@param newBackgroundColor The background color of the component, or <code>null</code> if the default background color should be used.
	@see #BACKGROUND_COLOR_PROPERTY 
	*/
	public void setBackgroundColor(final Color<?> newBackgroundColor);

	/**@return The foreground color of the component, or <code>null</code> if no foreground color is specified for this component.*/
	public Color<?> getColor();

	/**Sets the foreground color of the component.
	This is a bound property.
	@param newColor The foreground color of the component, or <code>null</code> if the default foreground color should be used.
	@see #COLOR_PROPERTY 
	*/
	public void setColor(final Color<?> newColor);

	/**@return The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.*/
	public Constraints getConstraints();

	/**Sets the layout constraints of this component.
	This is a bound property.
	@param newConstraints The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.
	@see #CONSTRAINTS_PROPERTY
	*/
	public void setConstraints(final Constraints newConstraints);

	/**Returns the arc size for the indicated corner.
	@param corner The corner for which an arc size should be returned.
	@return The dimensions indicating the two radiuses of the given corner arc, or dimensions of zero if the corner should not be rounded.
	*/
	public Dimensions getCornerArcSize(final Corner corner);

	/**Sets the arc size of a given corner.
	The radius of each corner represents a bound property.
	@param corner The corner for which the arc size should be set.
	@param newCornerArcSize The dimensions indicating the two radiuses of the corner, or dimensions of zero if the corner should not be rounded.
	@exception NullPointerException if the given corner and/or arc size is <code>null</code>. 
	@see Component#CORNER_ARC_SIZE_LINE_NEAR_PAGE_NEAR_PROPERTY
	@see Component#CORNER_ARC_SIZE_LINE_FAR_PAGE_NEAR_PROPERTY
	@see Component#CORNER_ARC_SIZE_LINE_NEAR_PAGE_FAR_PROPERTY
	@see Component#CORNER_ARC_SIZE_LINE_FAR_PAGE_FAR_PROPERTY
	*/
	public void setCornerArcSize(final Corner corner, final Dimensions newCornerArcSize);

	/**Sets the arc size of all corners.
	This is a convenience method that calls {@link #setCornerArcSize(Corner, Dimensions)} for each corner.
	@param newCornerArcSize The dimensions indicating the two radiuses of the corners, or dimensions of zero if the corners should not be rounded.
	@exception NullPointerException if the given arc size is <code>null</code>. 
	*/
	public void setCornerArcSize(final Dimensions newCornerArcSize);

	/**Determines the foreground color to use for the component.
	The color is determined by finding the first non-<code>null</code> color up the component hierarchy or the default color.
	@return The foreground color to use for the component.
	@see #getColor()
	*/
//TODO del if not needed	public Color<?> determineColor();

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

	/**@return The opacity of the entire component in the range (0.0-1.0), with a default of 1.0.*/
	public float getOpacity();

	/**Sets the opacity of the entire component.
	This is a bound property of type <code>Float</code>.
	@param newOpacity The new opacity of the entire component in the range (0.0-1.0).
	@exception IllegalArgumentException if the given opacity is not within the range (0.0-1.0).
	@see #OPACITY_PROPERTY 
	*/
	public void setOpacity(final float newOpacity);

	/**@return The preferred width of the component, or <code>null</code> if no preferred width has been specified.*/
	public Extent getPreferredWidth();

	/**Sets the preferred width of the component.
	This is a bound property.
	@param newPreferredWidth The new preferred width of the component, or <code>null</code> there is no width preference.
	@see #PREFERRED_WIDTH_PROPERTY 
	*/
	public void setPreferredWidth(final Extent newPreferredWidth);

	/**@return The preferred height of the component, or <code>null</code> if no preferred height has been specified.*/
	public Extent getPreferredHeight();

	/**Sets the preferred height of the component.
	This is a bound property.
	@param newPreferredHeight The new preferred height of the component, or <code>null</code> there is no height preference.
	@see #PREFERRED_HEIGHT_PROPERTY 
	*/
	public void setPreferredHeight(final Extent newPreferredHeight);

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
	public View<? extends GuiseContext, ? super C> getView();

	/**Sets the view used by this component.
	This is a bound property.
	@param newView The new view to use.
	@see #VIEW_PROPERTY
	@exception NullPointerException if the given view is <code>null</code>.
	*/
	public void setView(final View<? extends GuiseContext, ? super C> newView);

	/**@return The component identifier.*/
	public String getID();

	/**Creates an ID by combining this component's ID and the the given ID segment.
	@param idSegment The ID segment, which must itself be a valid ID, to include in the full ID.
	@return An ID appropriate for a child component of this component.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@see Component#ID_SEGMENT_DELIMITER
	*/
	public String createID(final String idSegment);

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

	/**@return The Guise session that owns this component.*/
	public GuiseSession getSession();

	/**@return The style identifier, or <code>null</code> if there is no style ID.*/
	public String getStyleID();

	/**Identifies the style for the component.
	This is a bound property.
	@param newStyleID The style identifier, or <code>null</code> if there is no style ID.
	@see #STYLE_ID_PROPERTY
	*/
	public void setStyleID(final String newStyleID);

	/**@return Whether the component is visible.
	@see #isDisplayed()
	*/
	public boolean isVisible();

	/**Sets whether the component is visible.
	This is a bound property of type <code>Boolean</code>.
	@param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
	@see #VISIBLE_PROPERTY
	@see #setDisplayed(boolean)
	*/
	public void setVisible(final boolean newVisible);

	/**@return Whether the component is displayed or has no representation, taking up no space.
	@see #isVisible()
	*/
	public boolean isDisplayed();

	/**Sets whether the component is displayed or has no representation, taking up no space.
	This is a bound property of type <code>Boolean</code>.
	@param newDisplayed <code>true</code> if the component should be displayed, else <code>false</code> if the component should take up no space.
	@see #DISPLAYED_PROPERTY
	@see #setVisible(boolean)
	*/
	public void setDisplayed(final boolean newDisplayed);

	/**@return Whether the component has dragging enabled.*/
	public boolean isDragEnabled();

	/**Sets whether the component is has dragging enabled.
	This is a bound property of type <code>Boolean</code>.
	@param newDragEnabled <code>true</code> if the component should allow dragging, else <code>false</code>.
	@see #DRAG_ENABLED_PROPERTY
	*/
	public void setDragEnabled(final boolean newDragEnabled);

	/**@return Whether the component has dropping enabled.*/
	public boolean isDropEnabled();

	/**Sets whether the component is has dropping enabled.
	This is a bound property of type <code>Boolean</code>.
	@param newDropEnabled <code>true</code> if the component should allow dropping, else <code>false</code>.
	@see #DROP_ENABLED_PROPERTY
	*/
	public void setDropEnabled(final boolean newDropEnabled);

	/**@return Whether flyovers are enabled for this component.*/
	public boolean isFlyoverEnabled();

	/**Sets whether flyovers are enabled for this component.
	Flyovers contain information from the component model's "description" property.
	This is a bound property of type <code>Boolean</code>.
	@param newFlyoverEnabled <code>true</code> if the component should display flyovers, else <code>false</code>.
	@see Model#getDescription()
	@see #FLYOVER_ENABLED_PROPERTY
	*/
	public void setFlyoverEnabled(final boolean newFlyoverEnabled);

	/**@return Whether tooltips are enabled for this component.*/
	public boolean isTooltipEnabled();

	/**Sets whether tooltips are enabled for this component.
	Tooltips contain information from the component model's "info" property.
	This is a bound property of type <code>Boolean</code>.
	@param newTooltipEnabled <code>true</code> if the component should display tooltips, else <code>false</code>.
	@see Model#getInfo()
	@see #TOOLTIP_ENABLED_PROPERTY
	*/
	public void setTooltipEnabled(final boolean newTooltipEnabled);

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
	@see #getView()
	@see GuiseContext.State#UPDATE_VIEW
	*/
	public <GC extends GuiseContext> void updateView(final GC context) throws IOException;

	/**Adds a mouse listener.
	@param mouseListener The mouse listener to add.
	*/
	public void addMouseListener(final MouseListener mouseListener);

	/**Removes a mouse listener.
	@param mouseListener The mouse listener to remove.
	*/
	public void removeMouseListener(final MouseListener mouseListener);

	/**@return all registered mouse listeners.*/
	public Iterator<MouseListener> getMouseListeners();

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
	public void fireMouseEntered(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition);

	/**Fires a mouse exited event to all registered mouse listeners.
	This method is used by the framework and should not be called directly by application code.
	@param componentBounds The absolute bounds of the component.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@exception NullPointerException if one or more of the arguments are <code>null</code>.
	@see MouseListener
	@see MouseEvent
	*/
	public void fireMouseExited(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition);

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
		/**@return The preferred width of the flyover component, or <code>null</code> if no preferred width has been specified.*/
		public Extent getPreferredWidth();

		/**Sets the preferred width of the flyover component.
		@param newPreferredWidth The new preferred width of the flyover component, or <code>null</code> there is no width preference.
		*/
		public void setPreferredWidth(final Extent newPreferredWidth);

		/**@return The preferred height of the flyover component, or <code>null</code> if no preferred height has been specified.*/
		public Extent getPreferredHeight();

		/**Sets the preferred height of the flyover component.
		@param newPreferredHeight The new preferred height of the flyover component, or <code>null</code> there is no height preference.
		*/
		public void setPreferredHeight(final Extent newPreferredHeight);

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
