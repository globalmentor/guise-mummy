package com.guiseframework.component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

import javax.mail.internet.ContentType;

import com.garretwilson.beans.TargetedEvent;
import com.garretwilson.lang.*;
import com.garretwilson.rdf.RDFResource;
import com.garretwilson.urf.URFResource;
import com.garretwilson.urf.ploop.PLOOPProcessor;
import com.garretwilson.urf.ploop.PLOOPURFGenerator;
import com.garretwilson.util.Debug;
import com.guiseframework.GuiseApplication;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.effect.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.component.transfer.*;
import com.guiseframework.event.*;
import com.guiseframework.geometry.*;
import com.guiseframework.input.*;
import com.guiseframework.model.*;
import com.guiseframework.model.ui.AbstractPresentationModel;
import com.guiseframework.platform.*;
import com.guiseframework.prototype.PrototypeConsumer;
import com.guiseframework.theme.Theme;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;
import static com.garretwilson.util.ArrayUtilities.*;

/**An abstract implementation of a component.
<p>A component should never fire a property event directly. It should rather create a postponed event and queue that event with the session.
This implementation automatically handles postponed property change events when {@link #firePropertyChange(String, Object, Object)} or a related method is called.</p>
<p>Property changes to a component's constraints are repeated with the component as the source and the constraints as the target.</p> 
@author Garret Wilson
*/
public abstract class AbstractComponent extends AbstractPresentationModel implements Component
{

	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The thread-safe set of properties saved and loaded as preferences.*/
	private final Set<String> preferenceProperties=new CopyOnWriteArraySet<String>();

		/**Adds a property to be saved and loaded as a preference.
		@param propertyName The property to store as a preference.
		@see #loadPreferences(boolean)
		@see #savePreferences(boolean)
		*/
		public void addPreferenceProperty(final String propertyName) {preferenceProperties.add(propertyName);}

		/**Determines whether the given property is saved and loaded as a preference.
		@param propertyName The property to determine if it is stored as a preference.
		@return <code>true</code> if the given property is saved and loaded as a preference.
		@see #loadPreferences(boolean)
		@see #savePreferences(boolean)
		*/
		public boolean isPreferenceProperty(final String propertyName) {return preferenceProperties.contains(propertyName);}

		/**Returns all properties stored as preferences.
		@return An iterable of all properties saved and loaded as preferences.
		@see #loadPreferences(boolean)
		@see #savePreferences(boolean)
		*/
		public Iterable<String> getPreferenceProperties() {return preferenceProperties;}

		/**Removes a property from being saved and loaded as preferences.
		@param propertyName The property that should no longer be stored as a preference.
		@see #loadPreferences(boolean)
		@see #savePreferences(boolean)
		*/
		public void removePreferenceProperty(final String propertyName) {preferenceProperties.remove(propertyName);}

	/**The label model decorated by this component.*/
	private final LabelModel labelModel;

		/**@return The label model decorated by this component.*/
		protected LabelModel getLabelModel() {return labelModel;}

	/**The name of the component, not guaranteed to be unique (but guaranteed not to be the empty string) and useful only for searching for components within a component sub-hierarchy, or <code>null</code> if the component has no name.*/
	private String name=null;

		/**@return The name of the component, not guaranteed to be unique (but guaranteed not to be the empty string) and useful only for searching for components within a component sub-hierarchy, or <code>null</code> if the component has no name.*/
		public String getName() {return name;}

		/**Sets the name of the component.
		This is a bound property.
		@param newName The new name of the component, or <code>null</code> if the component should have no name.
		@exception IllegalArgumentException if the given name is the empty string.
		@see #NAME_PROPERTY
		*/
		public void setName(final String newName)
		{
			if(!ObjectUtilities.equals(name, newName))	//if the value is really changing
			{
				if(newName!=null && newName.length()==0)	//if the empty string was passed
				{
					throw new IllegalArgumentException("Name cannot be the empty string.");
				}
				final String oldName=name;	//get the old value
				name=newName;	//actually change the value
				firePropertyChange(NAME_PROPERTY, oldName, newName);	//indicate that the value changed
			}
		}

	/**@return The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.*/
	public URI getGlyphURI() {return getLabelModel().getGlyphURI();}

	/**Sets the URI of the icon.
	This is a bound property of type <code>URI</code>.
	@param newLabelIcon The new URI of the icon, which may be a resource URI.
	@see #GLYPH_URI_PROPERTY
	*/
	public void setGlyphURI(final URI newLabelIcon) {getLabelModel().setGlyphURI(newLabelIcon);}

	/**@return The label text, which may include a resource reference, or <code>null</code> if there is no label text.*/
	public String getLabel() {return getLabelModel().getLabel();}

	/**Sets the text of the label.
	This is a bound property.
	@param newLabelText The new text of the label, which may include a resource reference.
	@see #LABEL_PROPERTY
	*/
	public void setLabel(final String newLabelText) {getLabelModel().setLabel(newLabelText);}

	/**@return The content type of the label text.*/
	public ContentType getLabelContentType() {return getLabelModel().getLabelContentType();}

	/**Sets the content type of the label text.
	This is a bound property.
	@param newLabelTextContentType The new label text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #LABEL_CONTENT_TYPE_PROPERTY
	*/
	public void setLabelContentType(final ContentType newLabelTextContentType) {getLabelModel().setLabelContentType(newLabelTextContentType);}

	/**The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.*/
	private String info=null;

		/**@return The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.*/
		public String getInfo() {return info;}

		/**Sets the advisory information text, such as might appear in a tooltip.
		This is a bound property.
		@param newInfo The new text of the advisory information, such as might appear in a tooltip.
		@see #INFO_PROPERTY
		*/
		public void setInfo(final String newInfo)
		{
			if(!ObjectUtilities.equals(info, newInfo))	//if the value is really changing
			{
				final String oldInfo=info;	//get the old value
				info=newInfo;	//actually change the value
				firePropertyChange(INFO_PROPERTY, oldInfo, newInfo);	//indicate that the value changed
			}			
		}

	/**The content type of the advisory information text.*/
	private ContentType infoContentType=PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the advisory information text.*/
		public ContentType getInfoContentType() {return infoContentType;}

		/**Sets the content type of the advisory information text.
		This is a bound property.
		@param newInfoContentType The new advisory information text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #INFO_CONTENT_TYPE_PROPERTY
		*/
		public void setInfoContentType(final ContentType newInfoContentType)
		{
			checkInstance(newInfoContentType, "Content type cannot be null.");
			if(infoContentType!=newInfoContentType)	//if the value is really changing
			{
				final ContentType oldInfoContentType=infoContentType;	//get the old value
				if(!isText(newInfoContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newInfoContentType+" is not a text content type.");
				}
				infoContentType=newInfoContentType;	//actually change the value
				firePropertyChange(INFO_CONTENT_TYPE_PROPERTY, oldInfoContentType, newInfoContentType);	//indicate that the value changed
			}			
		}

	/**The description text, such as might appear in a flyover, or <code>null</code> if there is no description.*/
	private String description=null;

		/**@return The description text, such as might appear in a flyover, or <code>null</code> if there is no description.*/
		public String getDescription() {return description;}

		/**Sets the description text, such as might appear in a flyover.
		This is a bound property.
		@param newDescription The new text of the description, such as might appear in a flyover.
		@see #DESCRIPTION_PROPERTY
		*/
		public void setDescription(final String newDescription)
		{
			if(!ObjectUtilities.equals(description, newDescription))	//if the value is really changing
			{
				final String oldDescription=description;	//get the old value
				description=newDescription;	//actually change the value
				firePropertyChange(DESCRIPTION_PROPERTY, oldDescription, newDescription);	//indicate that the value changed
			}			
		}

	/**The content type of the description text.*/
	private ContentType descriptionContentType=PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the description text.*/
		public ContentType getDescriptionContentType() {return descriptionContentType;}

		/**Sets the content type of the description text.
		This is a bound property.
		@param newDescriptionContentType The new description text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #DESCRIPTION_CONTENT_TYPE_PROPERTY
		*/
		public void setDescriptionContentType(final ContentType newDescriptionContentType)
		{
			checkInstance(newDescriptionContentType, "Content type cannot be null.");
			if(descriptionContentType!=newDescriptionContentType)	//if the value is really changing
			{
				final ContentType oldDescriptionContentType=descriptionContentType;	//get the old value
				if(!isText(newDescriptionContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newDescriptionContentType+" is not a text content type.");
				}
				descriptionContentType=newDescriptionContentType;	//actually change the value
				firePropertyChange(DESCRIPTION_CONTENT_TYPE_PROPERTY, oldDescriptionContentType, newDescriptionContentType);	//indicate that the value changed
			}			
		}

	/**The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.*/
	private Constraints constraints=null;

		/**@return The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.*/
		public Constraints getConstraints() {return constraints;}

		/**Sets the layout constraints of this component.
		This is a bound property.
		@param newConstraints The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.
		@see #CONSTRAINTS_PROPERTY
		*/
		public void setConstraints(final Constraints newConstraints)	//TODO see if any of the specialized components throw constraints property changes as well
		{
			if(constraints!=newConstraints)	//if the value is really changing
			{
				final Constraints oldConstraints=constraints;	//get the old value
				if(oldConstraints!=null)	//if there were old constraints
				{
					oldConstraints.removePropertyChangeListener(getRepeatPropertyChangeListener());	//stop repeating constraints property change events
				}
				constraints=newConstraints;	//actually change the value
				if(newConstraints!=null)	//if there are new constraints
				{
					newConstraints.addPropertyChangeListener(getRepeatPropertyChangeListener());	//repeat constraints property change events
				}
				firePropertyChange(CONSTRAINTS_PROPERTY, oldConstraints, newConstraints);	//indicate that the value changed
			}
		}

	/**The strategy for processing input, or <code>null</code> if this component has no input strategy.*/
	private InputStrategy inputStrategy=null;

		/**@return The strategy for processing input, or <code>null</code> if this component has no input strategy.*/
		public InputStrategy getInputStrategy() {return inputStrategy;}

		/**Sets the strategy for processing input.
		This is a bound property.
		@param newInputStrategy The new strategy for processing input, or <code>null</code> if this component is to have no input strategy.
		@see #INPUT_STRATEGY_PROPERTY
		*/
		public void setInputStrategy(final InputStrategy newInputStrategy)
		{
			if(!ObjectUtilities.equals(inputStrategy, newInputStrategy))	//if the value is really changing
			{
				final InputStrategy oldInputStrategy=inputStrategy;	//get the current value
				inputStrategy=newInputStrategy;	//update the value
				firePropertyChange(INPUT_STRATEGY_PROPERTY, oldInputStrategy, newInputStrategy);
			}
		}

	/**The notification associated with the component, or <code>null</code> if no notification is associated with this component.*/
	private Notification notification=null;

		/**@return The notification associated with the component, or <code>null</code> if no notification is associated with this component.*/
		public Notification getNotification() {return notification;}

		/**Sets the component notification.
		This is a bound property.
		The notification is also fired as a {@link NotificationEvent} on this component if a new notification is given.
		Parents are expected to refire the notification event up the hierarchy.
		@param newNotification The notification for the component, or <code>null</code> if no notification is associated with this component.
		@see #NOTIFICATION_PROPERTY
		*/
		public void setNotification(final Notification newNotification)
		{
			if(!ObjectUtilities.equals(notification, newNotification))	//if the value is really changing
			{
				final Notification oldNotification=notification;	//get the old value
				notification=newNotification;	//actually change the value
//TODO del unless status is promoted to Component				updateStatus();	//update the status before firing the notification event so that the status will already be updated for the listeners to access
				firePropertyChange(NOTIFICATION_PROPERTY, oldNotification, newNotification);	//indicate that the value changed
				if(newNotification!=null)	//if a new notification is provided
				{
					fireNotified(newNotification);	//fire a notification event here and up the hierarchy
				}
			}			
		}
		
	/**Whether the valid property has been initialized.
	Updating validity in a super constructor can sometimes call {@link #determineValid()} before subclass variables are initialized,
	especially in containers that add and/or remove children in the super constructor before subclasses have a chance to create class variables in their constructors,
	so this implementation of valid is lazily-initialized only when needed. The value is always initialized when being read or being set,
	and the {@link #updateValid()} method only calls {@link #determineValid()} if the valid property is initialized or there is at least one listener for the {@link #VALID_PROPERTY}.
	*/ 
	private boolean validInitialized=false;

	/**Whether the state of the component and all child component represents valid user input.
	Updating validity in a super constructor can sometimes call {@link #determineValid()} before subclass variables are initialized,
	especially in containers that add and/or remove children in the super constructor before subclasses have a chance to create class variables in their constructors,
	so this implementation of valid is lazily-initialized only when needed. The value is always initialized when being read or being set,
	and the {@link #updateValid()} method only calls {@link #determineValid()} if the valid property is initialized or there is at least one listener for the {@link #VALID_PROPERTY}.
	*/
	private Boolean valid=null;	//start with an uninitialized valid property

		/**Determines whether the state of the component and all child components represents valid user input.
		This implementation initializes the valid property if needed.
		@return Whether the state of the component and all child components represents valid user input.
		*/
		public boolean isValid()
		{
			if(valid==null)	//if valid is not yet initialized TODO eliminate race condition
			{
//TODO del Debug.traceStack("ready to call determineValid() for the first time from inside isValid()");
				valid=Boolean.TRUE;	//initialize valid to an arbitrary value so that if determineValid() calls isValid() there won't be inifinite recursion
				valid=Boolean.valueOf(determineValid());	//determine validity
			}
			return valid.booleanValue();	//return the valid state
		}

/*TODO del		
		public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener)
		{
			if(VALID_PROPERTY.equals(propertyName))
			{
				Debug.traceStack("we have a new validity listener:", listener);
			}
			super.addPropertyChangeListener(propertyName, listener);
		}
*/
		
		/**Sets whether the state of the component and all child components represents valid user input
		This is a bound property of type {@link Boolean}.
		This implementation initializes the valid property if needed.
		@param newValid <code>true</code> if user input of this component and all child components should be considered valid
		@see #VALID_PROPERTY
		*/
		protected void setValid(final boolean newValid)
		{
			final boolean oldValid=isValid();	//get the current value
			if(oldValid!=newValid)	//if the value is really changing
			{
				final Boolean booleanNewValid=Boolean.valueOf(newValid);	//get the BOolean form of the new value
				valid=booleanNewValid;	//update the value
				firePropertyChange(VALID_PROPERTY, Boolean.valueOf(oldValid), booleanNewValid);
			}
		}

		/**Rechecks user input validity of this component and all child components, and updates the valid state.
		This implementation only updates the valid property if the property is already initialized or there is at least one listener to the {@link #VALID_PROPERTY}.
		@see #setValid(boolean)
		*/ 
		protected void updateValid()
		{
			if(valid!=null || hasPropertyChangeListeners(VALID_PROPERTY))	//if valid is initialized or there is a listener for the valid property
			{
/*TODO del
if(valid==null)
{
	Debug.traceStack("ready to call determineValid() for the first time from inside updateValid()");	
}
*/
/*TODO del
				final boolean newValid=determineValid();
Debug.trace("ready to set valid in", this, "to", newValid);
				setValid(newValid);	//update the vailidity after rechecking it
Debug.trace("now valid of", this, "is", isValid());
*/
				setValid(determineValid());	//update the vailidity after rechecking it
			}
		}

		/**Checks the state of the component for validity.
		This version returns <code>true</code>.
		@return <code>true</code> if the component and all children passes all validity tests, else <code>false</code>.
		*/ 
		protected boolean determineValid()
		{
			return true;	//default to being valid
		}

	/**The depictor for this object; the depictor is lazily installed to allow construction to complete before the depictor is installed.*/
	private final Depictor<? extends Component> depictor;

		/**@return The depictor for this object.*/
		public Depictor<? extends Component> getDepictor()
		{
			if(depictor.getDepictedObject()==null)	//if the depictor has not yet been installed TODO fix the race condition
			{
				notifyDepictorInstalled(depictor);	//tell the the depictor it has been installed
			}
			return depictor;	//return the depictor
		}

		/**Processes an event from the platform.
		This method delegates to the currently installed depictor.
		@param event The event to be processed.
		@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
		@see #getDepictor()
		@see Depictor#processEvent(PlatformEvent)
		*/
		public void processEvent(final PlatformEvent event)
		{
			getDepictor().processEvent(event);	//ask the depictor to process the event
		}

		/**Updates the depiction of the object.
		The depiction will be marked as updated.
		This method delegates to the currently installed depictor.
		@exception IOException if there is an error updating the depiction.
		@see #getDepictor()
		@see Depictor#depict()
		*/
		public void depict() throws IOException
		{
			getDepictor().depict();	//ask the depictor to depict the object
		}

	/**The object depiction identifier*/
	private final long depictID;

		/**@return The object depiction identifier.*/
		public long getDepictID() {return depictID;}
		
	/**The internationalization orientation of the component's contents, or <code>null</code> if the default orientation should be used.*/
	private Orientation orientation=null;

		/**Returns this component's requested orientation.
		To resolve the orientation up the hierarchy, {@link #getComponentOrientation()} should be used.
		@return The internationalization orientation of the component's contents, or <code>null</code> if the default orientation should be used.
		@see #getComponentOrientation()
		*/
		public Orientation getOrientation() {return orientation;}

		/**Determines the internationalization orientation of the component's contents.
		This method returns the local orientation value, if there is one.
		If there is no orientation specified for this component, the request is deferred to this component's parent.
		If there is no parent component, a default orientation is retrieved from the current session.
		@return The internationalization orientation of the component's contents.
		@see #getOrientation()
		@see GuiseSession#getOrientation()
		*/
		public Orientation getComponentOrientation()
		{
			final Orientation orientation=getOrientation();	//get this component's orientation
			if(orientation!=null)	//if an orientation is explicitly set for this component
			{
				return orientation;	//return this component's orientation
			}
			else	//otherwise, try to defer to the parent
			{
				final Component parent=getParent();	//get this component's parent
				if(parent!=null)	//if we have a parent
				{
					return parent.getComponentOrientation();	//return the parent's orientation
				}
				else	//if we don't have a parent
				{
					return getSession().getOrientation();	//return the session's default orientation
				}
			}
		}

		/**Sets the orientation.
		This is a bound property
		@param newOrientation The new internationalization orientation of the component's contents, or <code>null</code> if default orientation should be determined based upon the session's locale.
		@see Component#ORIENTATION_PROPERTY
		*/
		public void setOrientation(final Orientation newOrientation)
		{
			if(!ObjectUtilities.equals(orientation, newOrientation))	//if the value is really changing
			{
				final Orientation oldOrientation=orientation;	//get the old value
				orientation=newOrientation;	//actually change the value
				firePropertyChange(ORIENTATION_PROPERTY, oldOrientation, newOrientation);	//indicate that the value changed
			}
		}

	/**The parent of this component, or <code>null</code> if this component does not have a parent.*/
	private CompositeComponent parent=null;

		/**@return The parent of this component, or <code>null</code> if this component does not have a parent.*/
		public CompositeComponent getParent() {return parent;}

		/**Retrieves the first ancestor of the given type.
		@param <A> The type of ancestor component requested.
		@param ancestorClass The class of ancestor component requested.
		@return The first ancestor component of the given type, or <code>null</code> if this component has no such ancestor.
		*/
		@SuppressWarnings("unchecked")	//we check to see if the ancestor is of the correct type before casting, so the cast is logically checked, though not syntactically checked
		public <A extends CompositeComponent> A getAncestor(final Class<A> ancestorClass)
		{
			final CompositeComponent parent=getParent();	//get this component's parent
			if(parent!=null)	//if there is a parent
			{
				return ancestorClass.isInstance(parent) ? (A)parent : parent.getAncestor(ancestorClass);	//if the parent is of the correct type, return it; otherwise, ask it to search its own ancestors
			}
			else	//if there is no parent
			{
				return null;	//there is no such ancestor
			}		
		}

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
		public void setParent(final CompositeComponent newParent)
		{
			final CompositeComponent oldParent=parent;	//get the old parent
			if(oldParent!=newParent)	//if the parent is really changing
			{
				if(newParent!=null)	//if a parent is provided
				{
					if(oldParent!=null)	//if we already have a parent
					{
						throw new IllegalStateException("Component "+this+" already has parent: "+oldParent);
					}
					if(newParent instanceof Container && !((Container)newParent).contains(this))	//if the new parent is a container that is not really our parent
					{
						throw new IllegalArgumentException("Provided parent container "+newParent+" is not really parent of component "+this);
					}
				}
				else	//if no parent is provided
				{
					if(oldParent instanceof Container && ((Container)oldParent).contains(this))	//if we had a container parent before, and that container still thinks this component is its child
					{
						throw new IllegalStateException("Old parent container "+oldParent+" still thinks this component, "+this+", is a child."); 
					}
				}
				parent=newParent;	//this is really our parent; make a note of it
			}
		}

	/**Whether the component has dragging enabled.*/
	private boolean dragEnabled=false;

		/**@return Whether the component has dragging enabled.*/
		public boolean isDragEnabled() {return dragEnabled;}

		/**Sets whether the component has dragging enabled.
		This is a bound property of type {@link Boolean}.
		@param newDragEnabled <code>true</code> if the component should allow dragging, else <code>false</code>.
		@see Component#DRAG_ENABLED_PROPERTY
		*/
		public void setDragEnabled(final boolean newDragEnabled)
		{
			if(dragEnabled!=newDragEnabled)	//if the value is really changing
			{
				final boolean oldDragEnabled=dragEnabled;	//get the current value
				dragEnabled=newDragEnabled;	//update the value
				firePropertyChange(DRAG_ENABLED_PROPERTY, Boolean.valueOf(oldDragEnabled), Boolean.valueOf(newDragEnabled));
			}
		}

	/**Whether the component has dropping enabled.*/
	private boolean dropEnabled=false;

		/**@return Whether the component has dropping enabled.*/
		public boolean isDropEnabled() {return dropEnabled;}

		/**Sets whether the component has dropping enabled.
		This is a bound property of type {@link Boolean}.
		@param newDropEnabled <code>true</code> if the component should allow dropping, else <code>false</code>.
		@see Component#DROP_ENABLED_PROPERTY
		*/
		public void setDropEnabled(final boolean newDropEnabled)
		{
			if(dropEnabled!=newDropEnabled)	//if the value is really changing
			{
				final boolean oldDropEnabled=dropEnabled;	//get the current value
				dropEnabled=newDropEnabled;	//update the value
				firePropertyChange(DRAG_ENABLED_PROPERTY, Boolean.valueOf(oldDropEnabled), Boolean.valueOf(newDropEnabled));
			}
		}

	/**Whether flyovers are enabled for this component.*/
	private boolean flyoverEnabled=false;

		/**@return Whether flyovers are enabled for this component.*/
		public boolean isFlyoverEnabled() {return flyoverEnabled;}

		/**A reference to the default flyover strategy, if we're using one.*/
		private FlyoverStrategy<?> defaultFlyoverStrategy=null;
		
		/**Sets whether flyovers are enabled for this component.
		Flyovers contain information from the component model's "description" property.
		This implementation adds or removes a default flyover strategy if one is not already installed.
		This is a bound property of type {@link Boolean}.
		@param newFlyoverEnabled <code>true</code> if the component should display flyovers, else <code>false</code>.
		@see #getDescription()
		@see Component#FLYOVER_ENABLED_PROPERTY
		*/
		public void setFlyoverEnabled(final boolean newFlyoverEnabled)
		{
			if(flyoverEnabled!=newFlyoverEnabled)	//if the value is really changing
			{
				final boolean oldFlyoverEnabled=flyoverEnabled;	//get the current value
				flyoverEnabled=newFlyoverEnabled;	//update the value
				if(newFlyoverEnabled)	//if flyovers are now enabled
				{
					if(getFlyoverStrategy()==null)	//if no flyover strategy is installed
					{
						defaultFlyoverStrategy=new DefaultFlyoverStrategy<Component>(this);	//create a default flyover strategy
						setFlyoverStrategy(defaultFlyoverStrategy);	//start using our default flyover strategy
					}
				}
				else	//if flyovers are now disabled
				{
					if(defaultFlyoverStrategy!=null)	//if we had created a default flyover strategy
					{
						if(getFlyoverStrategy()==defaultFlyoverStrategy)	//if we were using the default flyover strategy
						{
							setFlyoverStrategy(null);	//remove our default flyover strategy
						}
						defaultFlyoverStrategy=null;	//release the default flyover strategy
					}
				}
				firePropertyChange(FLYOVER_ENABLED_PROPERTY, Boolean.valueOf(oldFlyoverEnabled), Boolean.valueOf(newFlyoverEnabled));
			}
		}

		/**The installed flyover strategy, or <code>null</code> if there is no flyover strategy installed.*/
		private FlyoverStrategy<?> flyoverStrategy=null;

			/**@return The installed flyover strategy, or <code>null</code> if there is no flyover strategy installed.*/
			public FlyoverStrategy<?> getFlyoverStrategy() {return flyoverStrategy;}

			/**Sets the strategy for controlling flyovers.
			The flyover strategy will be registered as a mouse listener for this component.
			This is a bound property.
			@param newFlyoverStrategy The new flyover strategy, or <code>null</code> if there is no flyover strategy installed.
			@see #FLYOVER_STRATEGY_PROPERTY 
			*/
			public void setFlyoverStrategy(final FlyoverStrategy<?> newFlyoverStrategy)
			{
				if(flyoverStrategy!=newFlyoverStrategy)	//if the value is really changing
				{
					final FlyoverStrategy<?> oldFlyoverStrategy=flyoverStrategy;	//get the old value
					if(oldFlyoverStrategy!=null)	//if there was a flyover strategy
					{
						removeMouseListener(oldFlyoverStrategy);	//let the old flyover strategy stop listening for mouse events
						if(oldFlyoverStrategy==defaultFlyoverStrategy)	//if the default flyover strategy was just uninstalled
						{
							defaultFlyoverStrategy=null;	//we don't need to keep around the default flyover strategy
						}
					}
					flyoverStrategy=newFlyoverStrategy;	//actually change the value
					if(newFlyoverStrategy!=null)	//if there is now a new flyover strategy
					{
						addMouseListener(newFlyoverStrategy);	//let the new flyover strategy start listening for mouse events
					}					
					firePropertyChange(FLYOVER_STRATEGY_PROPERTY, oldFlyoverStrategy, newFlyoverStrategy);	//indicate that the value changed
				}			
			}

	/**Whether a theme has been applied to this component.*/
	private boolean themeApplied=false;

		/**@return Whether a theme has been applied to this component.*/
		public boolean isThemeApplied() {return themeApplied;}

		/**Sets whether a theme has been applied to this component.
		This is a bound property of type {@link Boolean}.
		@param newThemeApplied <code>true</code> if a theme has been applied to this component, else <code>false</code>.
		@see #THEME_APPLIED_PROPERTY
		*/
		public void setThemeApplied(final boolean newThemeApplied)
		{
			if(themeApplied!=newThemeApplied)	//if the value is really changing
			{
				final boolean oldThemeApplied=themeApplied;	//get the current value
				themeApplied=newThemeApplied;	//update the value
				firePropertyChange(THEME_APPLIED_PROPERTY, Boolean.valueOf(oldThemeApplied), Boolean.valueOf(newThemeApplied));
			}
		}

	/**The list of installed export strategies, from most recently added to earliest added.*/
	private List<ExportStrategy<?>> exportStrategyList=new CopyOnWriteArrayList<ExportStrategy<?>>();

		/**Adds an export strategy to the component.
		The export strategy will take precedence over any compatible export strategy previously added.
		@param exportStrategy The export strategy to add.
		*/
		public void addExportStrategy(final ExportStrategy<?> exportStrategy) {exportStrategyList.add(0, exportStrategy);}	//add the export strategy to the beginning of the list

		/**Removes an export strategy from the component.
		@param exportStrategy The export strategy to remove.
		*/
		public void removeExportStrategy(final ExportStrategy<?> exportStrategy) {exportStrategyList.remove(exportStrategy);}	//remove the export strategy from the list

		/**Exports data from the component.
		Each export strategy, from last to first added, will be asked to export data, until one is successful.
		@return The object to be transferred, or <code>null</code> if no data can be transferred.
		*/
		public Transferable<?> exportTransfer()
		{
			for(final ExportStrategy<?> exportStrategy:exportStrategyList)	//for each export strategy
			{
				final Transferable<?> transferable=((ExportStrategy<Component>)exportStrategy).exportTransfer(this);	//ask this export strategy to transfer data
				if(transferable!=null)	//if this export succeeded
				{
					return transferable;	//return this transferable data
				}
			}
			return null;	//indicate that no data could be exported
		}

	/**The list of installed import strategies, from most recently added to earliest added.*/
	private List<ImportStrategy<?>> importStrategyList=new CopyOnWriteArrayList<ImportStrategy<?>>();

		/**Adds an import strategy to the component.
		The import strategy will take prececence over any compatible import strategy previously added.
		@param importStrategy The importstrategy to add.
		*/
		public void addImportStrategy(final ImportStrategy<?> importStrategy) {importStrategyList.add(0, importStrategy);}	//add the import strategy to the beginning of the list

		/**Removes an import strategy from the component.
		@param importStrategy The import strategy to remove.
		*/
		public void removeImportStrategy(final ImportStrategy<?> importStrategy) {importStrategyList.remove(importStrategy);}	//remove the import strategy from the list

		/**Imports data to the component.
		Each import strategy, from last to first added, will be asked to import data, until one is successful.
		@param transferable The object to be transferred.
		@return <code>true</code> if the given object was be imported.
		*/
		public boolean importTransfer(final Transferable<?> transferable)
		{
			for(final ImportStrategy<?> importStrategy:importStrategyList)	//for each importstrategy
			{
				if(((ImportStrategy<Component>)importStrategy).canImportTransfer(this, transferable))	//if this import strategy can import the data
				{
					if(((ImportStrategy<Component>)importStrategy).importTransfer(this, transferable))	//import the data; if we are successful
					{
						return true;	//stop trying to import data, and indicate we were successful
					}
				}
			}
			return false;	//indicate that no data could be imported
		}

	/**Default constructor.
	@exception IllegalStateException if no controller is registered for this component type.
	@exception IllegalStateException if no view is registered for this component type.
	*/
	public AbstractComponent()
	{
		this(new DefaultLabelModel());	//construct the component with a default label model
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given model is <code>null</code>.
	@exception IllegalStateException if no depictor is registered for this component type.
	*/
	@SuppressWarnings("unchecked")
	public AbstractComponent(final LabelModel labelModel)
	{
		final GuiseSession session=getSession();	//get the Guise session
		final Platform platform=session.getPlatform();	//get the Guise platform
		this.depictID=platform.generateDepictID();	//ask the platform to generate a new depict ID
		this.depictor=(Depictor<? extends Component>)platform.getDepictor(this);	//ask the platform for a depictor for the object, and assume that it's a component depictor
		if(this.depictor==null)	//if no depictor is registered for this object
		{
			throw new IllegalStateException("No depictor registered for class "+getClass());
		}
//TODO del; lazily installed		notifyDepictorInstalled(depictor);	//tell the the depictor it has been installed
		platform.registerDepictedObject(this);	//register this depicted object with the platform
		this.labelModel=checkInstance(labelModel, "Label model cannot be null.");	//save the label model
		this.labelModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the label model
		this.labelModel.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the label model
	}

	/**Notifies a depictor that it has been installed in this object.
	@param <O> The type of depicted object expected by the depictor.
	@param depictor The depictor that has been installed.
	*/
	@SuppressWarnings("unchecked")	//at this point we have to assume that the correct type of depictor has been registered for this object
	private <O extends DepictedObject> void notifyDepictorInstalled(final Depictor<O> depictor)
	{
		depictor.installed((O)this);	//tell the depictor it has been installed		
	}

	/**Whether this component has been initialized.*/
	private boolean initialized=false;

	/**Initializes the component after construction.
	This method can only be called once during the life of a component.
	Subclasses should call this version.
	This implementation performs no actions.
	@exception IllegalStateException if this method has already been called.
	*/
	public void initialize()
	{
		if(initialized)	//if this method has already been called
		{
			throw new IllegalStateException("Component can only be initialized once.");
		}
		initialized=true;	//show that this component has been initialized
	}

	/**Validates the user input of this component and all child components.
	The component will be updated with error information.
	This version clears all notifications.
	This version calls {@link #updateValid()}.
	@return The current state of {@link #isValid()} as a convenience.
	*/
	public boolean validate()
	{
		setNotification(null);	//clear any notification
		updateValid();	//manually update the current component validity
		return isValid();	//return the current valid state
	}

	/**Dispatches an input event to this component and all child components, if any.
	If this is a {@link FocusedInputEvent}, the event will be directed towards the branch in which lies the focused component of any {@link InputFocusGroupComponent} ancestor of this component (or this component, if it is a focus group).
	If this is instead a {@link TargetedEvent}, the event will be directed towards the branch in which lies the target component of the event.
	Otherwise, the event will be dispatched to all child components.
	Only after the event has been dispatched to any children will the event be fired to any event listeners and then passed to the installed input strategy, if any.
	Once the event is consumed, no further processing takes place.
	This version fires all events that are not consumed.
	@param inputEvent The input event to dispatch.
	@exception NullPointerException if the given event is <code>null</code>.
	@see TargetedEvent
	@see FocusedInputEvent
	@see InputEvent#isConsumed()
	@see #fireInputEvent(InputEvent)
	@see #getInputStrategy()
	@see InputStrategy#input(Input)
	*/
	public void dispatchInputEvent(final InputEvent inputEvent)
	{
//Debug.trace("in component", this, "ready to do default dispatching of input event", inputEvent);		
		if(!inputEvent.isConsumed())	//if the input has not been consumed
		{
//Debug.trace("event is not consumed; ready to fire it to listeners");
			fireInputEvent(inputEvent);	//fire the event to any listeners
//Debug.trace("firing finised");
			if(!inputEvent.isConsumed())	//if the input has still not been consumed
			{
//Debug.trace("event is not still not consumed; checking input strategy");
				final InputStrategy inputStrategy=getInputStrategy();	//get our input strategy, if any
				if(inputStrategy!=null)	//if we have an input strategy
				{
//Debug.trace("got input strategy");
					final Input input=inputEvent.getInput();	//get the event's input, if any
					if(input!=null)	//if the event has input
					{
//Debug.trace("got input for this event:", input);
						if(inputStrategy.input(input))	//send the input to the input strategy; if the input was consumed
						{
//Debug.trace("our input strategy consumed the input");
							inputEvent.consume();	//mark the event as consumed
						}
					}
				}
			}
		}
	}

	/**Fire the given even to all registered listeners, if any.
	If the event is consumed further processing should cease.
	@param inputEvent The input event to fire.
	@exception NullPointerException if the given event is <code>null</code>.
	@see InputEvent#isConsumed()
	@see CommandEvent
	@see KeyboardEvent
	@see MouseEvent
	*/
	public void fireInputEvent(final InputEvent inputEvent)
	{
		if(inputEvent instanceof TargetedEvent && !this.equals(((TargetedEvent)inputEvent).getTarget()))	//if this is a targeted event that is not bound for this component TODO document, if it works; later allow for registration of pre/target/post bubble listening
		{
			return;	//don't fire the event
		}
		if(inputEvent instanceof CommandEvent)	//if this is a command event
		{
			if(hasCommandListeners())	//if there are command listeners registered
			{
				final CommandEvent commandEvent=new CommandEvent(this, (CommandEvent)inputEvent);	//create a new command event copy indicating that this component is the source
				for(final CommandListener commandListener:getCommandListeners())	//for each command listener
				{
					if(commandEvent.isConsumed())	//if the event copy has been consumed
					{
						inputEvent.consume();	//consume the original event
						return;	//stop further processing
					}
					commandListener.commanded(commandEvent);	//fire the command event
				}
			}
		}
		else if(inputEvent instanceof KeyboardEvent)	//if this is a keyboard event
		{
			if(hasKeyListeners())	//if there are key listeners registered
			{
				if(inputEvent instanceof KeyPressEvent)	//if this is a key press event
				{
					final KeyPressEvent keyPressEvent=new KeyPressEvent(this, (KeyPressEvent)inputEvent);	//create a new key event copy indicating that this component is the source
					for(final KeyboardListener keyListener:getKeyListeners())	//for each key listener
					{
						if(keyPressEvent.isConsumed())	//if the event copy has been consumed
						{
							inputEvent.consume();	//consume the original event
							return;	//stop further processing
						}
						keyListener.keyPressed(keyPressEvent);	//fire the key event
					}
				}
				if(inputEvent instanceof KeyReleaseEvent)	//if this is a key release event
				{
					final KeyReleaseEvent keyReleaseEvent=new KeyReleaseEvent(this, (KeyReleaseEvent)inputEvent);	//create a new key event copy indicating that this component is the source
					for(final KeyboardListener keyListener:getKeyListeners())	//for each key listener
					{
						if(keyReleaseEvent.isConsumed())	//if the event copy has been consumed
						{
							inputEvent.consume();	//consume the original event
							return;	//stop further processing
						}
						keyListener.keyReleased(keyReleaseEvent);	//fire the key event
					}
				}
			}
		}
		else if(inputEvent instanceof MouseEvent)	//if this is a mouse event
		{
			if(hasMouseListeners())	//if there are mouse listeners registered
			{
				if(inputEvent instanceof MouseClickEvent)	//if this is a mouse click event
				{
					final MouseClickEvent mouseClickEvent=new MouseClickEvent(this, (MouseClickEvent)inputEvent);	//create a new mouse event copy indicating that this component is the source
					for(final MouseListener mouseListener:getMouseListeners())	//for each mouse listener
					{
						if(mouseClickEvent.isConsumed())	//if the event copy has been consumed
						{
							inputEvent.consume();	//consume the original event
							return;	//stop further processing
						}
						mouseListener.mouseClicked(mouseClickEvent);	//fire the mouse event
					}
				}
				else if(inputEvent instanceof MouseEnterEvent)	//if this is a mouse enter event
				{
					final MouseEnterEvent mouseEnterEvent=new MouseEnterEvent(this, (MouseEnterEvent)inputEvent);	//create a new mouse event copy indicating that this component is the source
					for(final MouseListener mouseListener:getMouseListeners())	//for each mouse listener
					{
						if(mouseEnterEvent.isConsumed())	//if the event copy has been consumed
						{
							inputEvent.consume();	//consume the original event
							return;	//stop further processing
						}
						mouseListener.mouseEntered(mouseEnterEvent);	//fire the mouse event
					}
				}
				else if(inputEvent instanceof MouseExitEvent)	//if this is a mouse exit event
				{
					final MouseExitEvent mouseExitEvent=new MouseExitEvent(this, (MouseExitEvent)inputEvent);	//create a new mouse event copy indicating that this component is the source
					for(final MouseListener mouseListener:getMouseListeners())	//for each mouse listener
					{
						if(mouseExitEvent.isConsumed())	//if the event copy has been consumed
						{
							inputEvent.consume();	//consume the original event
							return;	//stop further processing
						}
						mouseListener.mouseExited(mouseExitEvent);	//fire the mouse event
					}
				}
			}
		}
	}

	/**Returns the theme to apply to this component.
	If there is no theme to this component, the parent theme will be returned. 
	This version delegates to the parent version, if there is a parent component; otherwise, the session theme is returned.
	@return The theme to apply to this component.
	@exception IOException if there is an error loading the theme.
	@see #getParent()
	@see GuiseSession#getTheme()
	*/
	public Theme getTheme() throws IOException
	{
		final CompositeComponent parent=getParent();	//get this component's parent, if any
		return parent!=null ? parent.getTheme() : getSession().getTheme();	//if there is no parent, return the session theme
	}

	/**Update's this object's theme.
	This method checks whether a theme has been applied to this object.
	If a theme has not been applied to this object this method calls {@link #applyTheme()}.
	This method is called for any child components before initializing applying the theme to the component itself,
	to assure that child theme updates have already occured before theme updates occur for this component.
	There is normally no need to override this method or to call this method directly by applications.
	@exception IOException if there was an error loading or applying a theme.
	@see #isThemeApplied()
	@see #applyTheme()
	*/
	public void updateTheme() throws IOException
	{
		if(!isThemeApplied())	//if a theme has not been applied to this component
		{
			applyTheme();	//apply the theme to this component
		}
	}

	/**Applies the theme to this objectd.
	Themes are only applied of the application is themed.
	This method may be overridden to effectively override theme settings by ensuring the state of important properties after the theme has been set. 
	If the theme is successfully applied, this method updates the theme applied status.
	@exception IOException if there was an error loading or applying a theme.
	@see GuiseApplication#isThemed()
	@see #getTheme()
	@see #applyTheme(Theme)
	@see #setThemeApplied(boolean)
	*/
	public void applyTheme() throws IOException
	{
		if(getSession().getApplication().isThemed())	//if the application applies themes
		{
			applyTheme(getTheme());	//get the theme and apply it
			setThemeApplied(true);	//indicate that the theme was successfully applied
		}
	}

	/**Applies a theme and its parents to this object.
	The theme's rules will be applied to this object and any related objects.
	Theme application occurs unconditionally, regardless of whether themes have been applied to this component before.
	There is normally no need to call this method directly by applications.
	@param theme The theme to apply to the object.
	*/
	public void applyTheme(final Theme theme)
	{
		theme.apply(this);	//apply the theme to this component
	}

	/**Loads the preferences for this component and optionally any descendant components.
	Any preferences returned from {@link #getPreferenceProperties()} will be loaded automatically.
	@param includeDescendants <code>true</code> if preferences of any descendant components should also be loaded, else <code>false</code>.
	@exception IOException if there is an error loading preferences.
	*/
	public void loadPreferences(final boolean includeDescendants) throws IOException
	{
		final Iterator<String> preferencePropertyIterator=getPreferenceProperties().iterator();	//get an iterator to all preferences properties
		if(preferencePropertyIterator.hasNext())	//if there are preference properties
		{
			final URFResource preferences=getSession().getPreferences(getClass());	//get existing preferences for this class
//TODO del Debug.traceStack("ready to load preferences; view:", ((ResourceChildrenPanel)this).getView(), "thumbnail size:", ((ResourceChildrenPanel)this).getThumbnailSize(), "preferences", RDFUtilities.toString(preferences));
			final PLOOPProcessor ploopProcessor=new PLOOPProcessor();	//create a new PLOOP processor for retrieving the properties
			do	//for each property
			{
				final String propertyName=preferencePropertyIterator.next();	//get the name of the next property
				try
				{
					ploopProcessor.setObjectProperty(this, preferences, propertyName);	//retrieve this property from the preferences
				}
				catch(final InvocationTargetException invocationTargetException)	//if there was an error accessing this resource
				{
					throw (IOException)new IOException(invocationTargetException.getMessage()).initCause(invocationTargetException);
				}
			}
			while(preferencePropertyIterator.hasNext());	//keep saving properties while there are more preference properties
//TODO del Debug.trace("loaded preferences; view:", ((ResourceChildrenPanel)this).getView(), "thumbnail size:", ((ResourceChildrenPanel)this).getThumbnailSize());
		}
	}

	/**Saves the preferences for this component and optionally any descendant components.
	Any preferences returned from {@link #getPreferenceProperties()} will be saved automatically.
	@param includeDescendants <code>true</code> if preferences of any descendant components should also be saved, else <code>false</code>.
	@exception IOException if there is an error saving preferences.
	*/
	public void savePreferences(final boolean includeDescendants) throws IOException
	{
		final Iterator<String> preferencePropertyIterator=getPreferenceProperties().iterator();	//get an iterator to all preferences properties
		if(preferencePropertyIterator.hasNext())	//if there are preference properties
		{
			final GuiseSession session=getSession();	//get the current session
			final Class<?> componentClass=getClass();	//get this component's class
			final URFResource preferences=session.getPreferences(componentClass);	//get existing preferences for this class
/*TODO convert to URF
			final PLOOPRDFGenerator ploopRDFGenerator=new PLOOPRDFGenerator();	//create a new PLOOP RDF generator for storing the properties
			do	//for each property
			{
				final String propertyName=preferencePropertyIterator.next();	//get the name of the next property
				try
				{
					ploopRDFGenerator.setRDFResourceProperty(preferences, this, propertyName);	//store this property in the preferences
				}
				catch(final InvocationTargetException invocationTargetException)	//if there was an error accessing this resource
				{
					throw (IOException)new IOException(invocationTargetException.getMessage()).initCause(invocationTargetException);
				}
			}
			while(preferencePropertyIterator.hasNext());	//keep saving properties while there are more preference properties
//TODO del Debug.trace("ready to save preferences; view:", ((ResourceChildrenPanel)this).getView(), "thumbnail size:", ((ResourceChildrenPanel)this).getThumbnailSize(), "preferences", RDFUtilities.toString(preferences));
			session.setPreferences(componentClass, preferences);	//set the new preferences
*/
		}
	}

	/**Adds a command listener.
	@param commandListener The command listener to add.
	*/
	public void addCommandListener(final CommandListener commandListener)
	{
		getEventListenerManager().add(CommandListener.class, commandListener);	//add the listener
	}

	/**Removes a command listener.
	@param commandListener The command listener to remove.
	*/
	public void removeCommandListener(final CommandListener commandListener)
	{
		getEventListenerManager().remove(CommandListener.class, commandListener);	//remove the listener
	}

	/**@return <code>true</code> if there is one or more command listeners registered.*/
	public boolean hasCommandListeners()
	{
		return getEventListenerManager().hasListeners(CommandListener.class);	//return whether there are command listeners registered
	}

	/**@return all registered command listeners.*/
	protected Iterable<CommandListener> getCommandListeners()
	{
		return getEventListenerManager().getListeners(CommandListener.class);	//return the registered listeners
	}
	
	/**Adds a key listener.
	@param keyListener The key listener to add.
	*/
	public void addKeyListener(final KeyboardListener keyListener)
	{
		getEventListenerManager().add(KeyboardListener.class, keyListener);	//add the listener
	}

	/**Removes a key listener.
	@param keyListener The key listener to remove.
	*/
	public void removeKeyListener(final KeyboardListener keyListener)
	{
		getEventListenerManager().remove(KeyboardListener.class, keyListener);	//remove the listener
	}

	/**@return <code>true</code> if there is one or more key listeners registered.*/
	public boolean hasKeyListeners()
	{
		return getEventListenerManager().hasListeners(KeyboardListener.class);	//return whether there are key listeners registered
	}

	/**@return all registered key listeners.*/
	protected Iterable<KeyboardListener> getKeyListeners()
	{
		return getEventListenerManager().getListeners(KeyboardListener.class);	//return the registered listeners
	}
	
	/**Adds a mouse listener.
	@param mouseListener The mouse listener to add.
	*/
	public void addMouseListener(final MouseListener mouseListener)
	{
		getEventListenerManager().add(MouseListener.class, mouseListener);	//add the listener
	}

	/**Removes a mouse listener.
	@param mouseListener The mouse listener to remove.
	*/
	public void removeMouseListener(final MouseListener mouseListener)
	{
		getEventListenerManager().remove(MouseListener.class, mouseListener);	//remove the listener
	}

	/**@return <code>true</code> if there is one or more mouse listeners registered.*/
	public boolean hasMouseListeners()
	{
		return getEventListenerManager().hasListeners(MouseListener.class);	//return whether there are mouse listeners registered
	}

	/**@return all registered mouse listeners.*/
	protected Iterable<MouseListener> getMouseListeners()
	{
		return getEventListenerManager().getListeners(MouseListener.class);	//return the registered listeners
	}

	/**Fires a mouse entered event to all registered mouse listeners.
	@param componentBounds The absolute bounds of the component.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@exception NullPointerException if one or more of the arguments are <code>null</code>.
	@see MouseListener
	@see MouseEvent
	*/
/*TODO del if not needed
	public void fireMouseEntered(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition)
	{
		if(hasMouseListeners())	//if there are mouse listeners registered
		{
			final MouseEnterEvent mouseEnterEvent=new MouseEnterEvent(getThis(), componentBounds, viewportBounds, mousePosition);	//create a new mouse event
			for(final MouseListener mouseListener:getMouseListeners())	//for each mouse listener
			{
				mouseListener.mouseEntered(mouseEnterEvent);	//fire the mouse entered event
			}
		}
	}
*/

	/**Fires a mouse exited event to all registered mouse listeners.
	@param componentBounds The absolute bounds of the component.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@exception NullPointerException if one or more of the arguments are <code>null</code>.
	@see MouseListener
	@see MouseEvent
	*/
/*TODO del if not needed
	public void fireMouseExited(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition)
	{
		if(hasMouseListeners())	//if there are mouse listeners registered
		{
			final MouseExitEvent mouseExitEvent=new MouseExitEvent(getThis(), componentBounds, viewportBounds, mousePosition);	//create a new mouse event
			for(final MouseListener mouseListener:getMouseListeners())	//for each mouse listener
			{
				mouseListener.mouseExited(mouseExitEvent);	//fire the mouse entered event
			}
		}
	}
*/

	/**Searches up the component hierarchy (including this one) and tells the first prototype consumer to consume prototypes.
	@param component The component on which to start the search for a prototype consumer.
	@exception NullPointerException if the given component is <code>null</code>.
	*/
	public static void initiatePrototypeConsumption(Component component)	//TODO improve prototype producer/consumer framework to allow consumers to be registered and send events when there is more information to publish
	{
		checkInstance(component, "Component cannot be null.");
		do	//tell the first prototype consumer ancestor, if any, to consumer prototypes
		{
			if(component instanceof PrototypeConsumer)	//if this is a prototype consumer
			{
				((PrototypeConsumer)component).consumePrototypes();	//tell the prototype consumer to consumer the prototypes we produce
				break;	//stop looking for a prototype consumer
			}
			else	//if this is not a prototype consumer
			{
				component=component.getParent();	//climb the tree
			}
		}
		while(component!=null);	//keep looking for a prototype consumer until we run out of ancestors		
	}
	
	/**Determines the root parent of the given component.
	@param component The component for which the root should be found.
	@return The root component (the component or ancestor which has no parent).
	*/
	public static Component getRootComponent(Component component)
	{
		Component parent;	//we'll keep track of the parent at each level when finding the root component
		while((parent=component.getParent())!=null)	//get the parent; while there is a parent
		{
			component=parent;	//move up the chain
		}
		return component;	//return whatever component we ended up with without a parent
	}

	/**Determines whether a component has a given component as its ancestor, not including the component itself.
	@param component The component for which the potential ancestor should be checked.
	@param ancestor The component to check as an ancestor.
	@return <code>true</code> if the given ancestor component is its parent or one of its parent's parents.
	@exception NullPointerException if the given component and/or ancestor is <code>null</code>.
	*/
	public static boolean hasAncestor(Component component, final CompositeComponent ancestor)
	{
		checkInstance(ancestor, "Ancestor cannot be null.");
		while((component=component.getParent())!=null)	//get the parent; while we're not out of parents
		{
			if(component==ancestor)	//if this is the ancestor
			{
				return true;	//indicate that the component has the ancestor
			}
		};
		return false;	//indicate that we ran out of ancestors before we found a matching ancestor
	}

	/**Retrieves a component with the given ID.
	This method checks the given component and all descendant components.
	@param component The component that should be checked, along with its descendants, for the given ID.
	@return The component with the given ID, or <code>null</code> if this component and all descendant components do not have the given ID. 
	*/
	public static Component getComponentByID(final Component component, final long id)
	{
		if(component.getDepictID()==id)	//if this component has the correct ID
		{
			return component;	//return this component
		}
		else if(component instanceof CompositeComponent)	//if this component doesn't have the correct ID, but it is a composite component
		{
			for(final Component childComponent:((CompositeComponent)component).getChildComponents())	//for each child component
			{
				final Component matchingComponent=getComponentByID(childComponent, id);	//see if we can find a component in this tree
				if(matchingComponent!=null)	//if we found a matching component
				{
					return matchingComponent;	//return the matching component
				}
			}
		}
		return null;
	}

	/**Retrieves a component with the given name.
	This method checks the given component and all descendant components.
	@param component The component that should be checked, along with its descendants, for the given name.
	@return The first component with the given name, or <code>null</code> if this component and all descendant components do not have the given name. 
	*/
	public static Component getComponentByName(final Component component, final String name)
	{
		if(name.equals(component.getName()))	//if this component has the correct name
		{
			return component;	//return this component
		}
		else if(component instanceof CompositeComponent)	//if this component doesn't have the correct name, but it is a composite component
		{
			for(final Component childComponent:((CompositeComponent)component).getChildComponents())	//for each child component
			{
				final Component matchingComponent=getComponentByName(childComponent, name);	//see if we can find a component in this tree
				if(matchingComponent!=null)	//if we found a matching component
				{
					return matchingComponent;	//return the matching component
				}
			}
		}
		return null;
	}

	/**Retrieves all components that have views needing updated.
	This method checks the given component and all descendant components.
	If a given component is dirty, its child views will not be checked.
	@param component The component that should be checked, along with its descendants, for out-of-date views.
	@return The components with views needing to be updated. 
	*/
	public static List<Component> getDirtyComponents(final Component component)
	{
		return getDirtyComponents(component, new ArrayList<Component>());	//gather dirty components and put them in a list
	}

	/**Retrieves all components that have views needing updated.
	This method checks the given component and all descendant components.
	If a given component is dirty, its child views will not be checked.
	@param component The component that should be checked, along with its descendants, for out-of-date views.
	@param dirtyComponents The list that will be updated with more dirty components if any are found.
	@return The components with views needing to be updated. 
	*/
	public static List<Component> getDirtyComponents(final Component component, final List<Component> dirtyComponents)
	{
		if(!component.getDepictor().isDepicted())	//if this component's view isn't updated
		{
			dirtyComponents.add(component);	//add this component to the list
		}
		else if(component instanceof CompositeComponent)	//if the component's view is updated, check its children if it has any
		{
			for(final Component childComponent:((CompositeComponent)component).getChildComponents())	//for each child component
			{
				getDirtyComponents(childComponent, dirtyComponents);	//gather dirty components in this child hierarchy
			}
		}
		return dirtyComponents;
	}

	/**Changes the updated status of the views of an entire component descendant hierarchy.
	@param newUpdated Whether the views of this component and all child components are up to date.
	*/
	public static void setDepicted(final Component component, final boolean newUpdated)
	{
		component.getDepictor().setDepicted(newUpdated);	//change the updated status of this component's view
		if(component instanceof CompositeComponent)	//if the component is a composite component
		{
			for(final Component childComponent:((CompositeComponent)component).getChildComponents())	//for each child component
			{
				setDepicted(childComponent, newUpdated);	//changed the updated status for this child's hierarchy
			}
		}
	}

	/**Retrieves the the notifications of all components in a hierarchy.
	This method checks the given component and all descendant components.
	Children that are not visible and/or not displayed are not taken into account.
	@param component The component from which, along with its descendants, notifications should be retrieved.
	@return The notifications of all components in the hierarchy. 
	*/
	public static List<Notification> getNotifications(final Component component)
	{
		return getNotifications(component, new ArrayList<Notification>());	//gather notifications and put them in a list
	}

	/**Retrieves the the notifications of all components in a hierarchy.
	This method checks the given component and all descendant components.
	Children that are not visible and/or not displayed are not taken into account.
	@param component The component from which, along with its descendants, notifications should be retrieved.
	@param notifications The list that will be updated with more dirty components if any are found.
	@return The notifications of all components in the hierarchy. 
	*/
	protected static List<Notification> getNotifications(final Component component, final List<Notification> notifications)
	{
		final Notification notification=component.getNotification();	//get the component's notification, if any
		if(notification!=null)	//if a notification is available
		{
			notifications.add(notification);	//add this notification to the list
		}
		if(component instanceof CompositeComponent)	//if the component is a composite component, check its children
		{
			for(final Component childComponent:((CompositeComponent)component).getChildComponents())	//for each child component
			{
				if(childComponent.isDisplayed() && childComponent.isVisible())	//if this child component is displayed and visible
				{
					getNotifications(childComponent, notifications);	//gather notifications from this child hierarchy
				}
			}
		}
		return notifications;
	}
	
	/**Adds a notification listener.
	@param notificationListener The notification listener to add.
	*/
	public void addNotificationListener(final NotificationListener notificationListener)
	{
		getEventListenerManager().add(NotificationListener.class, notificationListener);	//add the listener
	}

	/**Removes a notification listener.
	@param notificationListener The notification listener to remove.
	*/
	public void removeNotificationListener(final NotificationListener notificationListener)
	{
		getEventListenerManager().remove(NotificationListener.class, notificationListener);	//remove the listener
	}

	/**Fires an event to all registered notification listeners with the new notification information.
	Parents are expected to refire the notification event up the hierarchy.
	@param notification The notification to send to the notification listeners.
	@exception NullPointerException if the given notification is <code>null</code>.
	@see NotificationListener
	@see NotificationEvent
	*/
	protected void fireNotified(final Notification notification)
	{
		fireNotified(new NotificationEvent(this, notification));	//create and fire a new notification event
	}

	/**Fires an event to all registered notification listeners with the new notification information.
	Parents are expected to refire copies of the notification event up the hierarchy, keeping the original target.
	@param notificationEvent The notification event to send to the notification listeners.
	@exception NullPointerException if the given notification event is <code>null</code>.
	@see NotificationListener
	*/
	protected void fireNotified(final NotificationEvent notificationEvent)
	{
		for(final NotificationListener notificationListener:getEventListenerManager().getListeners(NotificationListener.class))	//for each notification listener
		{
			notificationListener.notified(notificationEvent);	//dispatch the notification event to the listener
		}
	}

	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return LongUtilities.hashCode(getDepictID());	//return the hash code of the ID
	}

	/**Indicates whether some other object is "equal to" this one.
	This implementation returns whether the object is a component with the same ID.
	@param object The reference object with which to compare.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		return object instanceof Component && getDepictID()==((Component)object).getDepictID();	//see if the other object is a component with the same ID
	}

	/**@return A string representation of this depicted object.*/
	public String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder(super.toString());	//create a string builder for constructing the string
		stringBuilder.append(' ').append('[').append(getDepictID()).append(']');	//append the ID
		return stringBuilder.toString();	//return the string builder
	}

	/**Notifies the user of the given notification information.
	The notification is stored in this component using {@link #setNotification(Notification)}, which fires appropriate notification events.
	This method calls {@link GuiseSession#notify(Notification)}.
	@param notification The notification information to relay.
	*/
	public void notify(final Notification notification)
	{
		setNotification(notification);	//store the notification, firing notification events
		getSession().notify(notification);	//notify the user directly
	}

	/**An abstract implementation of a strategy for showing and hiding flyovers in response to mouse events.
	@param <S> The type of component for which this object is to control flyovers.
	@author Garret Wilson
	*/
	public static abstract class AbstractFlyoverStrategy<S extends Component> extends MouseAdapter implements FlyoverStrategy<S>
	{
		/**The component for which this object will control flyovers.*/
		private final S component;

			/**@return The component for which this object will control flyovers.*/
			public S getComponent() {return component;}
			
		/**The array of flyover extents.*/
		private Extent[] extents=fill(new Extent[Flow.values().length], null);

			/**Returns the extent of the indicated flow.
			@param flow The flow for which an extent should be returned.
			@return The extent of the given flow.
			*/
			public Extent getExtent(final Flow flow) {return extents[flow.ordinal()];}

			/**Returns the extent of the line flow.
			In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>width</dfn>.
			@return The extent of the flow, or <code>null</code> if no preferred extent has been specified
			*/
			public Extent getLineExtent() {return getExtent(Flow.LINE);}

			/**Returns the extent of the page flow.
			In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>height</dfn>.
			@return The extent of the flow, or <code>null</code> if no preferred extent has been specified
			*/
			public Extent getPageExtent() {return getExtent(Flow.PAGE);}

			/**Sets the extent of a given flow.
			The extent of each flow represents a bound property.
			@param flow The flow for which the extent should be set.
			@param newExtent The new requested extent of the flyover, or <code>null</code> there is no extent preference.
			@exception NullPointerException if the given flow is <code>null</code>. 
			*/
			public void setExtent(final Flow flow, final Extent newExtent)
			{
				final int flowOrdinal=checkInstance(flow, "Flow cannot be null").ordinal();	//get the ordinal of the flow
				final Extent oldExtent=extents[flowOrdinal];	//get the old value
				if(!ObjectUtilities.equals(oldExtent, newExtent))	//if the value is really changing
				{
					extents[flowOrdinal]=newExtent;	//actually change the value
				}			
			}

			/**Sets the extent of the line flow.
			In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>width</dfn>.
			This is a bound property.
			@param newExtent The new requested extent of the flyover, or <code>null</code> there is no extent preference.
			*/
			public void setLineExtent(final Extent newExtent) {setExtent(Flow.LINE, newExtent);}

			/**Sets the extent of the page flow.
			In left-to-right top-to-bottom orientation, this is commonly known as the <dfn>height</dfn>.
			This is a bound property.
			@param newExtent The new requested extent of the flyover, or <code>null</code> there is no extent preference.
			*/
			public void setPageExtent(final Extent newExtent) {setExtent(Flow.PAGE, newExtent);}
			
		/**The style identifier of the flyover, or <code>null</code> if there is no style ID.*/
		private String styleID=null;

			/**@return The style identifier of the flyover, or <code>null</code> if there is no style ID.*/
			public String getStyleID() {return styleID;}

			/**Identifies the style for the flyover component.
			@param newStyleID The style identifier of the flyover, or <code>null</code> if there is no style ID.
			*/
			public void setStyleID(final String newStyleID)
			{
				if(ObjectUtilities.equals(styleID, newStyleID))	//if the value is really changing
				{
					final String oldStyleID=styleID;	//get the current value
					styleID=newStyleID;	//update the value
				}
			}
			
		/**The bearing of the tether in relation to the frame.*/
		private BigDecimal tetherBearing=CompassPoint.NORTHWEST_BY_WEST.getBearing();

			/**@return The bearing of the tether in relation to the frame.*/
			public BigDecimal getTetherBearing() {return tetherBearing;}

			/**Sets the bearing of the tether in relation to the frame.
			@param newTetherBearing The new bearing of the tether in relation to the frame.
			@exception NullPointerException if the given bearing is <code>null</code>.
			@exception IllegalArgumentException if the given bearing is greater than 360.
			*/
			public void setTetherBearing(final BigDecimal newTetherBearing)
			{
				if(!tetherBearing.equals(checkInstance(newTetherBearing, "Tether bearing cannot be null.")))	//if the value is really changing
				{
					final BigDecimal oldTetherBearing=tetherBearing;	//get the current value
					tetherBearing=CompassPoint.checkBearing(newTetherBearing);	//update the value
				}
			}

		/**The effect used for opening the flyover, or <code>null</code> if there is no open effect.*/
		private Effect openEffect=null;

			/**@return The effect used for opening the flyover, or <code>null</code> if there is no open effect.*/
			public Effect getOpenEffect() {return openEffect;}

			/**Sets the effect used for opening the flyover.
			@param newEffect The new effect used for opening the flyover, or <code>null</code> if there should be no open effect.
			@see Frame#OPEN_EFFECT_PROPERTY 
			*/
			public void setOpenEffect(final Effect newOpenEffect)
			{
				if(openEffect!=newOpenEffect)	//if the value is really changing
				{
					final Effect oldOpenEffect=openEffect;	//get the old value
					openEffect=newOpenEffect;	//actually change the value
//TODO fix					firePropertyChange(Frame.OPEN_EFFECT_PROPERTY, oldOpenEffect, newOpenEffect);	//indicate that the value changed
				}			
			}

		/**Component constructor.
		@param component The component for which this object will control flyovers.
		@exception NullPointerException if the given component is <code>null</code>.
		*/
		public AbstractFlyoverStrategy(final S component)
		{
			this.component=checkInstance(component, "Component cannot be null.");			
		}

		/**Called when the mouse enters the target.
		This implementation opens the flyover.
		@param mouseEvent The event providing mouse information
		@see #openFlyover()
		*/
		public void mouseEntered(final MouseEnterEvent mouseEvent)
		{
/*TODO del when works
Debug.trace("source bounds:", mouseEvent.getSourceBounds());
			final Dimensions sourceSize=mouseEvent.getSourceBounds().getSize();	//get the size of the source
			final Point sourceCenter=mouseEvent.getSourceBounds().getPosition().translate(sourceSize.getWidth().getValue()/2, sourceSize.getHeight().getValue()/2);	//determine the center of the source
Debug.trace("source center:", sourceCenter);
Debug.trace("viewport bounds:", mouseEvent.getViewportBounds());
			final Point viewportPosition=mouseEvent.getViewportBounds().getPosition();	//get the position of the viewport
			final Dimensions viewportSize=mouseEvent.getViewportBounds().getSize();	//get the size of the viewport
			final Point viewportSourceCenter=sourceCenter.translate(-viewportPosition.getX().getValue(), -viewportPosition.getY().getValue());	//translate the source center into the viewport
Debug.trace("viewport source center:", viewportSourceCenter);
*/
			final Rectangle viewportBounds=mouseEvent.getViewportBounds();	//get the bounds of the viewport
//TODO del Debug.trace("viewport bounds:", viewportBounds);
//TODO del Debug.trace("source bounds:", mouseEvent.getSourceBounds());
			final Dimensions viewportSize=viewportBounds.getSize();	//get the size of the viewport
			final Point mousePosition=mouseEvent.getMousePosition();	//get the mouse position
//TODO del Debug.trace("mouse position:", mousePosition);
				//get the mouse position inside the traditional coordinate space with the origin at the center of the viewport
			final Point traditionalMousePosition=new Point(mousePosition.getX().getValue()-(viewportSize.getWidth().getValue()/2), -(mousePosition.getY().getValue()-(viewportSize.getHeight().getValue()/2)));
//TODO del Debug.trace("traditional mouse position:", traditionalMousePosition);
				//get the angle of the point from the y axis in the range of (-PI, PI)
			final double atan2=Math.atan2(traditionalMousePosition.getX().getValue(), traditionalMousePosition.getY().getValue());
			final double normalizedAtan2=atan2>=0 ? atan2 : (Math.PI*2)+atan2;	//normalize the angle to the range (0, 2PI) 
			final BigDecimal tetherBearing=CompassPoint.MAX_BEARING.multiply(new BigDecimal(normalizedAtan2/(Math.PI*2)));	//get the fraction of the range and multiply by 360
			setTetherBearing(tetherBearing);	//set the tether bearing to use for flyovers
			
			openFlyover();	//open the flyover
		}

		/**Called when the mouse exits the source.
		This implementation closes any open flyover.
		@param mouseEvent The event providing mouse information
		@see #closeFlyover()
		*/
		public void mouseExited(final MouseExitEvent mouseEvent)
		{
			closeFlyover();	//close the flyover if it is open
		}
	}	

	/**An abstract flyover strategy that uses flyover frames.
	@param <S> The type of component for which this object is to control flyovers.
	@author Garret Wilson
	*/
	public static abstract class AbstractFlyoverFrameStrategy<S extends Component> extends AbstractFlyoverStrategy<S>
	{
		/**The frame used for displaying flyovers.*/
		private FlyoverFrame flyoverFrame=null;

		/**Component constructor.
		@param component The component for which this object will control flyovers.
		@exception NullPointerException if the given component is <code>null</code>.
		*/
		public AbstractFlyoverFrameStrategy(final S component)
		{
			super(component);	//construct the parent class
//TODO del			setOpenEffect(new OpacityFadeEffect(component.getSession(), 500));	//create a default open effect TODO use a constant
		}

		/**Shows a flyover for the component.
		This implementation creates a flyover frame if necessary and then opens the frame.
		@see #createFrame()
		*/
		public void openFlyover()
		{
			if(flyoverFrame==null)	//if no flyover frame has been created
			{
//TODO del Debug.trace("no frame; created");
				flyoverFrame=createFrame();	//create a new frame
				final String styleID=getStyleID();	//get the styld ID
				if(styleID!=null)	//if there is a style ID
				{
					flyoverFrame.setStyleID(styleID);	//set the style ID of the flyover
				}
				final Extent lineExtent=getLineExtent();	//get the requested width
				if(lineExtent!=null)	//if there is a requested width
				{
					flyoverFrame.setLineExtent(lineExtent);	//set the flyover width
				}
				final Extent pageExtent=getPageExtent();	//get the requested height
				if(pageExtent!=null)	//if there is a requested height
				{
					flyoverFrame.setPageExtent(pageExtent);	//set the flyover height
				}
				flyoverFrame.setTetherBearing(getTetherBearing());	//set the bearing of the tether
//TODO fix				frame.getModel().setLabel("Flyover");
				flyoverFrame.setOpenEffect(getOpenEffect());	//set the effect for opening, if any
				flyoverFrame.open();				
			}			
		}

		/**Closes the flyover for the component.
		This implementation closes any open flyover frame.
		*/
		public void closeFlyover()
		{
			if(flyoverFrame!=null)	//if there is a flyover frame
			{
				flyoverFrame.close();	//close the frame
				flyoverFrame=null;	//release our reference to the frame
			}			
		}

		/**@return A new frame for displaying flyover information.*/
		protected abstract FlyoverFrame createFrame();
	}

	/**The default strategy for showing and hiding flyovers in response to mouse events.
//TODO del	This implementation uses flyover frames to represent flyovers.
//TODO del	This implementation defaults to an opacity fade effect for opening with a 500 millisecond delay.
	@param <S> The type of component for which this object is to control flyovers.
	@author Garret Wilson
	*/
	public static class DefaultFlyoverStrategy<S extends Component> extends AbstractFlyoverFrameStrategy<S>
	{
		/**Component constructor.
		@param component The component for which this object will control flyovers.
		@exception NullPointerException if the given component is <code>null</code>.
		*/
		public DefaultFlyoverStrategy(final S component)
		{
			super(component);	//construct the parent class
//TODO del			setOpenEffect(new OpacityFadeEffect(component.getSession(), 500));	//create a default open effect TODO use a constant
		}

		/**@return A new frame for displaying flyover information.*/
		protected FlyoverFrame createFrame()
		{
			final S component=getComponent();	//get the component
			final FlyoverFrame frame=new DefaultFlyoverFrame();	//create a default frame
			frame.setRelatedComponent(getComponent());	//tell the flyover frame with which component it is related
			final Message message=new Message();	//create a new message
			message.setMessageContentType(component.getDescriptionContentType());	//set the appropriate message content
			message.setMessage(component.getDescription());	//set the appropriate message text
			frame.setContent(message);	//put the message in the frame
			return frame;	//return the frame we created
		}
	}

}
