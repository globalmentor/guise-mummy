package com.guiseframework.component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.effect.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.component.transfer.*;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.controller.*;
import com.guiseframework.event.*;
import com.guiseframework.geometry.*;
import com.guiseframework.model.*;
import com.guiseframework.style.Color;
import com.guiseframework.view.View;

import static com.garretwilson.lang.CharSequenceUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;
import static com.garretwilson.util.ArrayUtilities.*;
import static com.guiseframework.GuiseResourceConstants.*;

/**An abstract implementation of a component.
@author Garret Wilson
*/
public abstract class AbstractComponent<C extends Component<C>> extends GuiseBoundPropertyObject implements Component<C>
{

	/**Extra characters allowed in the ID, verified for URI safeness.*/
	protected final static String ID_EXTRA_CHARACTERS="-_.";

	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**@return A reference to this instance, cast to the generic self type.*/
	@SuppressWarnings("unchecked")
	protected final C getThis() {return (C)this;}

	/**The label model used by this component.*/
	private final LabelModel labelModel;

		/**@return The label model used by this component.*/
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

	/**@return The icon URI, or <code>null</code> if there is no icon URI.*/
	public URI getIcon() {return getLabelModel().getIcon();}

	/**Sets the URI of the icon.
	This is a bound property of type <code>URI</code>.
	@param newLabelIcon The new URI of the icon.
	@see #ICON_PROPERTY
	*/
	public void setIcon(final URI newLabelIcon) {getLabelModel().setIcon(newLabelIcon);}

	/**@return The icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
	public String getIconResourceKey() {return getLabelModel().getIconResourceKey();}

	/**Sets the key identifying the URI of the icon in the resources.
	This is a bound property.
	@param newIconResourceKey The new icon URI resource key.
	@see #ICON_RESOURCE_KEY_PROPERTY
	*/
	public void setIconResourceKey(final String newIconResourceKey) {getLabelModel().setIconResourceKey(newIconResourceKey);}

	/**@return The label text, or <code>null</code> if there is no label text.*/
	public String getLabel() {return getLabelModel().getLabel();}

	/**Sets the text of the label.
	This is a bound property.
	@param newLabelText The new text of the label.
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

	/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
	public String getLabelResourceKey() {return getLabelModel().getLabelResourceKey();}

	/**Sets the key identifying the text of the label in the resources.
	This is a bound property.
	@param newLabelTextResourceKey The new label text resource key.
	@see #LABEL_RESOURCE_KEY_PROPERTY
	*/
	public void setLabelResourceKey(final String newLabelTextResourceKey) {getLabelModel().setLabelResourceKey(newLabelTextResourceKey);}

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

	/**The advisory information text resource key, or <code>null</code> if there is no advisory information text resource specified.*/
	private String infoResourceKey=null;

		/**@return The advisory information text resource key, or <code>null</code> if there is no advisory information text resource specified.*/
		public String getInfoResourceKey() {return infoResourceKey;}

		/**Sets the key identifying the text of the advisory information in the resources.
		This is a bound property.
		@param newInfoResourceKey The new advisory information text resource key.
		@see #INFO_RESOURCE_KEY_PROPERTY
		*/
		public void setInfoResourceKey(final String newInfoResourceKey)
		{
			if(!ObjectUtilities.equals(infoResourceKey, newInfoResourceKey))	//if the value is really changing
			{
				final String oldInfoResourceKey=infoResourceKey;	//get the old value
				infoResourceKey=newInfoResourceKey;	//actually change the value
				firePropertyChange(INFO_RESOURCE_KEY_PROPERTY, oldInfoResourceKey, newInfoResourceKey);	//indicate that the value changed
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

	/**The description text resource key, or <code>null</code> if there is no description text resource specified.*/
	private String descriptionResourceKey=null;

		/**@return The description text resource key, or <code>null</code> if there is no description text resource specified.*/
		public String getDescriptionResourceKey() {return descriptionResourceKey;}

		/**Sets the key identifying the text of the description in the resources.
		This is a bound property.
		@param newDescriptionResourceKey The new description text resource key.
		@see #DESCRIPTION_RESOURCE_KEY_PROPERTY
		*/
		public void setDescriptionResourceKey(final String newDescriptionResourceKey)
		{
			if(!ObjectUtilities.equals(descriptionResourceKey, newDescriptionResourceKey))	//if the value is really changing
			{
				final String oldDescriptionResourceKey=descriptionResourceKey;	//get the old value
				descriptionResourceKey=newDescriptionResourceKey;	//actually change the value
				firePropertyChange(DESCRIPTION_RESOURCE_KEY_PROPERTY, oldDescriptionResourceKey, newDescriptionResourceKey);	//indicate that the value changed
			}
		}

	/**The background color of the component, or <code>null</code> if no background color is specified for this component.*/
	private Color<?> backgroundColor=null;

		/**@return The background color of the component, or <code>null</code> if no background color is specified for this component.*/
		public Color<?> getBackgroundColor() {return backgroundColor;}

		/**Sets the background color of the component.
		This is a bound property.
		@param newBackgroundColor The background color of the component, or <code>null</code> if the default background color should be used.
		@see Component#BACKGROUND_COLOR_PROPERTY 
		*/
		public void setBackgroundColor(final Color<?> newBackgroundColor)
		{
			if(!ObjectUtilities.equals(backgroundColor, newBackgroundColor))	//if the value is really changing
			{
				final Color<?> oldBackgroundColor=backgroundColor;	//get the old value
				backgroundColor=newBackgroundColor;	//actually change the value
				firePropertyChange(BACKGROUND_COLOR_PROPERTY, oldBackgroundColor, newBackgroundColor);	//indicate that the value changed
			}			
		}

	/**The foreground color of the component, or <code>null</code> if no foreground color is specified for this component.*/
	private Color<?> color=null;

		/**@return The foreground color of the component, or <code>null</code> if no foreground color is specified for this component.*/
		public Color<?> getColor() {return color;}

		/**Sets the foreground color of the component.
		This is a bound property.
		@param newColor The foreground color of the component, or <code>null</code> if the default foreground color should be used.
		@see Component#COLOR_PROPERTY
		*/
		public void setColor(final Color<?> newColor)
		{
			if(!ObjectUtilities.equals(color, newColor))	//if the value is really changing
			{
				final Color<?> oldColor=color;	//get the old value
				color=newColor;	//actually change the value
				firePropertyChange(COLOR_PROPERTY, oldColor, newColor);	//indicate that the value changed
			}			
		}

		/**Determines the foreground color to use for the component.
		The color is determined by finding the first non-<code>null</code> color up the component hierarchy or the default color.
		@return The foreground color to use for the component.
		@see #getColor()
		*/
/*TODO del if not needed
		public Color<?> determineColor()
		{
			Color<?> color=getColor();	//find this component's color
			if(color==null)	//if we don't have a color, ask the parent
			{
				final CompositeComponent<?> parent=getParent();	//get the parent
				if(parent!=null)	//if there is a parent
				{
					color=parent.determineColor();	//ask the parent to determine the color
				}
			}
			return color!=null ? color : RGBColor.BLACK;	//return the default color if there is no specified color
		}
*/

	/**The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.*/
	private Constraints constraints=null;

		/**@return The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.*/
		public Constraints getConstraints() {return constraints;}

		/**Sets the layout constraints of this component.
		This is a bound property.
		@param newConstraints The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.
		@see #CONSTRAINTS_PROPERTY
		*/
		public void setConstraints(final Constraints newConstraints)
		{
			if(constraints!=newConstraints)	//if the value is really changing
			{
				final Constraints oldConstraints=constraints;	//get the old value
				constraints=newConstraints;	//actually change the value
				firePropertyChange(CONSTRAINTS_PROPERTY, oldConstraints, newConstraints);	//indicate that the value changed
			}			
		}

	/**The array of dimensions each defining a corner arc by two radiuses.*/
	private Dimensions[] cornerArcSizes=fill(new Dimensions[Corner.values().length], Dimensions.ZERO_DIMENSIONS);

	/**The properties corresponding to the corner arc sizes.*/
	private final static String[] CORNER_ARC_SIZE_PROPERTIES=new String[]{CORNER_ARC_SIZE_LINE_NEAR_PAGE_NEAR_PROPERTY, CORNER_ARC_SIZE_LINE_FAR_PAGE_NEAR_PROPERTY, CORNER_ARC_SIZE_LINE_NEAR_PAGE_FAR_PROPERTY, CORNER_ARC_SIZE_LINE_FAR_PAGE_FAR_PROPERTY};

		/**Returns the arc size for the indicated corner.
		@param corner The corner for which an arc size should be returned.
		@return The dimensions indicating the two radiuses of the given corner arc, or dimensions of zero if the corner should not be rounded.
		*/
		public Dimensions getCornerArcSize(final Corner corner) {return cornerArcSizes[corner.ordinal()];}
	
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
		public void setCornerArcSize(final Corner corner, final Dimensions newCornerArcSize)
		{
			final int cornerOrdinal=checkInstance(corner, "Corner cannot be null").ordinal();	//get the ordinal of the corner
			final Dimensions oldCornerArcSize=cornerArcSizes[cornerOrdinal];	//get the old value
			if(!ObjectUtilities.equals(oldCornerArcSize, checkInstance(newCornerArcSize, "Corner arc size cannot be null")))	//if the value is really changing
			{
				cornerArcSizes[cornerOrdinal]=newCornerArcSize;	//actually change the value
				firePropertyChange(CORNER_ARC_SIZE_PROPERTIES[cornerOrdinal], oldCornerArcSize, newCornerArcSize);	//indicate that the value changed
			}			
		}

		/**Sets the arc size of all corners.
		This is a convenience method that calls {@link #setCornerArcSize(Corner, Dimensions)} for each corner.
		@param newCornerArcSize The dimensions indicating the two radiuses of the corners, or dimensions of zero if the corners should not be rounded.
		@exception NullPointerException if the given arc size is <code>null</code>. 
		*/
		public void setCornerArcSize(final Dimensions newCornerArcSize)
		{
			for(final Corner corner:Corner.values())	//for each corner
			{
				setCornerArcSize(corner, newCornerArcSize);	//set this corner arc size
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
		
	/**The opacity of the entire component in the range (0.0-1.0), with a default of 1.0.*/
	private float opacity=1.0f;

		/**@return The opacity of the entire component in the range (0.0-1.0), with a default of 1.0.*/
		public float getOpacity() {return opacity;}

		/**Sets the opacity of the entire component.
		This is a bound property of type <code>Float</code>.
		@param newOpacity The new opacity of the entire component in the range (0.0-1.0).
		@exception IllegalArgumentException if the given opacity is not within the range (0.0-1.0).
		@see Component#OPACITY_PROPERTY 
		*/
		public void setOpacity(final float newOpacity)
		{
			if(newOpacity<0.0f || newOpacity>1.0f)	//if the new opacity is out of range
			{
				throw new IllegalArgumentException("Opacity "+newOpacity+" is not within the allowed range.");
			}
			if(opacity!=newOpacity)	//if the value is really changing
			{
				final float oldOpacity=opacity;	//get the old value
				opacity=newOpacity;	//actually change the value
				firePropertyChange(OPACITY_PROPERTY, new Float(oldOpacity), new Float(newOpacity));	//indicate that the value changed
			}			
		}

	/**The preferred width of the component, or <code>null</code> if no preferred width has been specified.*/
	private Extent preferredWidth=null;

		/**@return The preferred width of the component, or <code>null</code> if no preferred width has been specified.*/
		public Extent getPreferredWidth() {return preferredWidth;}

		/**Sets the preferred width of the component.
		This is a bound property.
		@param newPreferredWidth The new preferred width of the component, or <code>null</code> there is no width preference.
		@see Component#PREFERRED_WIDTH_PROPERTY 
		*/
		public void setPreferredWidth(final Extent newPreferredWidth)
		{
			if(!ObjectUtilities.equals(preferredWidth, newPreferredWidth))	//if the value is really changing
			{
				final Extent oldPreferredWidth=preferredWidth;	//get the old value
				preferredWidth=newPreferredWidth;	//actually change the value
				firePropertyChange(PREFERRED_WIDTH_PROPERTY, oldPreferredWidth, newPreferredWidth);	//indicate that the value changed
			}			
		}

	/**The preferred height of the component, or <code>null</code> if no preferred height has been specified.*/
	private Extent preferredHeight=null;

		/**@return The preferred height of the component, or <code>null</code> if no preferred height has been specified.*/
		public Extent getPreferredHeight() {return preferredHeight;}

		/**Sets the preferred height of the component.
		This is a bound property.
		@param newPreferredHeight The new preferred height of the component, or <code>null</code> there is no height preference.
		@see Component#PREFERRED_HEIGHT_PROPERTY 
		*/
		public void setPreferredHeight(final Extent newPreferredHeight)
		{
			if(!ObjectUtilities.equals(preferredHeight, newPreferredHeight))	//if the value is really changing
			{
				final Extent oldPreferredHeight=preferredHeight;	//get the old value
				preferredHeight=newPreferredHeight;	//actually change the value
				firePropertyChange(PREFERRED_HEIGHT_PROPERTY, oldPreferredHeight, newPreferredHeight);	//indicate that the value changed
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
		This is a bound property of type <code>Boolean</code>.
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
			if(valid!=null || hasListeners(VALID_PROPERTY))	//if valid is initialized or there is a listener for the valid property
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

	/**The controller installed in this component.*/
	private Controller<? extends GuiseContext, ? super C> controller;

		/**@return The controller installed in this component.*/
		public Controller<? extends GuiseContext, ? super C> getController() {return controller;}

		/**Sets the controller used by this component.
		This is a bound property.
		@param newController The new controller to use.
		@see Component#CONTROLLER_PROPERTY
		@exception NullPointerException if the given controller is <code>null</code>.
		*/
		public void setController(final Controller<? extends GuiseContext, ? super C> newController)
		{
			if(newController!=controller)	//if the value is really changing
			{
				final Controller<? extends GuiseContext, ? super C> oldController=controller;	//get a reference to the old value
				controller=checkInstance(newController, "Controller cannot be null.");	//actually change values
				firePropertyChange(CONTROLLER_PROPERTY, oldController, newController);	//indicate that the value changed				
			}
		}

	/**The view installed in this component.*/
	private View<? extends GuiseContext, ? super C> view=null;

		/**@return The view installed in this component.
		This implementation lazily creates a view if one has not yet been created, allowing view creation to be delayed so that appropriate properties such as layout may first be installed.
		*/
		public View<? extends GuiseContext, ? super C> getView()
		{
			if(view==null)	//if a view has not yet been created
			{
				view=getSession().getApplication().getView(getThis());	//ask the application for a view
				if(view==null)	//if we couldn't find a view
				{
					throw new IllegalStateException("No registered view for "+getClass().getName());	//TODO use a better error
				}
				view.installed(getThis());	//tell the view it's being installed
			}
			return view;	//return the view
		}

		/**Sets the view used by this component.
		This is a bound property.
		@param newView The new view to use.
		@see Component#VIEW_PROPERTY
		@exception NullPointerException if the given view is <code>null</code>.
		*/
		public void setView(final View<? extends GuiseContext, ? super C> newView)
		{
			if(newView!=checkInstance(view, "View cannot be null"))	//if the value is really changing
			{
				final View<? extends GuiseContext, ? super C> oldView=view;	//get a reference to the old value
				if(oldView!=null)	//if a view has been installed
				{
					oldView.uninstalled(getThis());	//tell the old view it's being uninstalled
				}
				view=newView;	//actually change values
				oldView.installed(getThis());	//tell the new view it's being installed
				firePropertyChange(VIEW_PROPERTY, oldView, newView);	//indicate that the value changed				
			}
		}

	/**The component identifier*/
	private final String id;

		/**@return The component identifier.*/
		public String getID() {return id;}

		/**Creates an ID by combining this component's ID and the the given ID segment.
		This implementation combines this component's ID with the ID segment using '.' as a delimiter.
		@param idSegment The ID segment, which must itself be a valid ID, to include in the full ID.
		@return An ID appropriate for a child component of this component.
		@exception IllegalArgumentException if the given identifier is not a valid component identifier.
		@see Component#ID_SEGMENT_DELIMITER
		*/
		public String createID(final String idSegment)
		{
			return getID()+ID_SEGMENT_DELIMITER+checkValidComponentID(idSegment);	//make sure the ID segment is a valid ID and combine it with this component's ID
		}
		
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
				final Component<?> parent=getParent();	//get this component's parent
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

		/**Determines if the given string is a valid component ID.
		A valid component ID begins with a letter and is composed only of letters, digits, and/or the characters '-' and '_'.
		@param string The string to check for component identifier compliance.
		@return <code>true</code> if the string is a valid component ID, else <code>false</code>.
		@exception NullPointerException if the given string is <code>null</code>.
		*/ 		
		public static boolean isValidComponentID(final String string)
		{
			return string.length()>0 && Character.isLetter(string.charAt(0)) && isLettersDigitsCharacters(string, ID_EXTRA_CHARACTERS);	//make sure the string has characters; that the first character is a letter; and that the remaining characters are letters, digits, and/or the extra ID characters
		}

		/**Checks to ensure that the given string is a valid component identifier, throwing an exception if not.
		@param string The string to check for component identifier compliance.
		@return The component identifier after being checked for compliance.
		@exception IllegalArgumentException if the given string is not a valid component ID.
		@exception NullPointerException if the given string is <code>null</code>.
		@see #isValidComponentID(String)
		*/
		public static String checkValidComponentID(final String string)
		{
			if(!isValidComponentID(string))	//if the string is not a valid component ID
			{
				throw new IllegalArgumentException("Invalid component ID: \""+string+"\".");
			}
			return string;	//return the string; it passed the test
		}

	/**The parent of this component, or <code>null</code> if this component does not have a parent.*/
	private CompositeComponent<?> parent=null;

		/**@return The parent of this component, or <code>null</code> if this component does not have a parent.*/
		public CompositeComponent<?> getParent() {return parent;}

		/**Retrieves the first ancestor of the given type.
		@param <A> The type of ancestor component requested.
		@param ancestorClass The class of ancestor component requested.
		@return The first ancestor component of the given type, or <code>null</code> if this component has no such ancestor.
		*/
		@SuppressWarnings("unchecked")	//we check to see if the ancestor is of the correct type before casting, so the cast is logically checked, though not syntactically checked
		public <A extends CompositeComponent<?>> A getAncestor(final Class<A> ancestorClass)
		{
			final CompositeComponent<?> parent=getParent();	//get this component's parent
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
		public void setParent(final CompositeComponent<?> newParent)
		{
			final CompositeComponent<?> oldParent=parent;	//get the old parent
			if(oldParent!=newParent)	//if the parent is really changing
			{
				if(newParent!=null)	//if a parent is provided
				{
					if(oldParent!=null)	//if we already have a parent
					{
						throw new IllegalStateException("Component "+this+" already has parent: "+oldParent);
					}
					if(newParent instanceof Container && !((Container<?>)newParent).contains(this))	//if the new parent is a container that is not really our parent
					{
						throw new IllegalArgumentException("Provided parent container "+newParent+" is not really parent of component "+this);
					}
				}
				else	//if no parent is provided
				{
					if(oldParent instanceof Container && ((Container<?>)oldParent).contains(this))	//if we had a container parent before, and that container still thinks this component is its child
					{
						throw new IllegalStateException("Old parent container "+oldParent+" still thinks this component, "+this+", is a child."); 
					}
				}
				parent=newParent;	//this is really our parent; make a note of it
			}
		}

	/**The style identifier, or <code>null</code> if there is no style ID.*/
	private String styleID=null;

		/**@return The style identifier, or <code>null</code> if there is no style ID.*/
		public String getStyleID() {return styleID;}

		/**Identifies the style for the component.
		This is a bound property.
		@param newStyleID The style identifier, or <code>null</code> if there is no style ID.
		@see Component#STYLE_ID_PROPERTY
		*/
		public void setStyleID(final String newStyleID)
		{
			if(!ObjectUtilities.equals(styleID, newStyleID))	//if the value is really changing
			{
				final String oldStyleID=styleID;	//get the current value
				styleID=newStyleID;	//update the value
				firePropertyChange(STYLE_ID_PROPERTY, oldStyleID, newStyleID);
			}
		}

	/**Whether the component is visible.*/
	private boolean visible=true;

		/**@return Whether the component is visible.
		@see #isDisplayed()
		*/
		public boolean isVisible() {return visible;}

		/**Sets whether the component is visible.
		This is a bound property of type <code>Boolean</code>.
		@param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
		@see Component#VISIBLE_PROPERTY
		@see #setDisplayed(boolean)
		*/
		public void setVisible(final boolean newVisible)
		{
			if(visible!=newVisible)	//if the value is really changing
			{
				final boolean oldVisible=visible;	//get the current value
				visible=newVisible;	//update the value
				firePropertyChange(VISIBLE_PROPERTY, Boolean.valueOf(oldVisible), Boolean.valueOf(newVisible));
			}
		}

	/**Whether the component is displayed or has no representation, taking up no space.*/
	private boolean displayed=true;

		/**@return Whether the component is displayed or has no representation, taking up no space.
		@see #isVisible()
		*/
		public boolean isDisplayed() {return displayed;}

		/**Sets whether the component is displayed or has no representation, taking up no space.
		This is a bound property of type <code>Boolean</code>.
		@param newDisplayed <code>true</code> if the component should be displayed, else <code>false</code> if the component should take up no space.
		@see #DISPLAYED_PROPERTY
		@see #setVisible(boolean)
		*/
		public void setDisplayed(final boolean newDisplayed)
		{
			if(displayed!=newDisplayed)	//if the value is really changing
			{
				final boolean oldDisplayed=displayed;	//get the current value
				displayed=newDisplayed;	//update the value
				firePropertyChange(DISPLAYED_PROPERTY, Boolean.valueOf(oldDisplayed), Boolean.valueOf(newDisplayed));
			}
		}

	/**Whether the component has dragging enabled.*/
	private boolean dragEnabled=false;

		/**@return Whether the component has dragging enabled.*/
		public boolean isDragEnabled() {return dragEnabled;}

		/**Sets whether the component is has dragging enabled.
		This is a bound property of type <code>Boolean</code>.
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

		/**Sets whether the component is has dropping enabled.
		This is a bound property of type <code>Boolean</code>.
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
		private FlyoverStrategy<C> defaultFlyoverStrategy=null;
		
		/**Sets whether flyovers are enabled for this component.
		Flyovers contain information from the component model's "description" property.
		This implementation adds or removes a default flyover strategy if one is not already installed.
		This is a bound property of type <code>Boolean</code>.
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
						defaultFlyoverStrategy=new DefaultFlyoverStrategy<C>(getThis());	//create a default flyover strategy
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
		private FlyoverStrategy<? super C> flyoverStrategy=null;

			/**@return The installed flyover strategy, or <code>null</code> if there is no flyover strategy installed.*/
			public FlyoverStrategy<? super C> getFlyoverStrategy() {return flyoverStrategy;}

			/**Sets the strategy for controlling flyovers.
			The flyover strategy will be registered as a mouse listener for this component.
			This is a bound property.
			@param newFlyoverStrategy The new flyover strategy, or <code>null</code> if there is no flyover strategy installed.
			@see Component#FLYOVER_STRATEGY_PROPERTY 
			*/
			public void setFlyoverStrategy(final FlyoverStrategy<? super C> newFlyoverStrategy)
			{
				if(flyoverStrategy!=newFlyoverStrategy)	//if the value is really changing
				{
					final FlyoverStrategy<? super C> oldFlyoverStrategy=flyoverStrategy;	//get the old value
					if(oldFlyoverStrategy!=null)	//if there was a flyover strategy
					{
						removeMouseListener(oldFlyoverStrategy);	//let the old flyover strategy stop listening for mouse events
						if(oldFlyoverStrategy==defaultFlyoverStrategy)	//if the default flyover strategy was just uninstalled
						{
							defaultFlyoverStrategy=null;	//we don't need to keep around default flyover strategy
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

	/**Whether tooltips are enabled for this component.*/
	private boolean tooltipEnabled=true;

		/**@return Whether tooltips are enabled for this component.*/
		public boolean isTooltipEnabled() {return tooltipEnabled;}

		/**Sets whether tooltips are enabled for this component.
		Tooltips contain information from the component model's "info" property.
		This is a bound property of type <code>Boolean</code>.
		@param newTooltipEnabled <code>true</code> if the component should display tooltips, else <code>false</code>.
		@see #getInfo()
		@see Component#TOOLTIP_ENABLED_PROPERTY
		*/
		public void setTooltipEnabled(final boolean newTooltipEnabled)
		{
			if(tooltipEnabled!=newTooltipEnabled)	//if the value is really changing
			{
				final boolean oldTooltipEnabled=tooltipEnabled;	//get the current value
				tooltipEnabled=newTooltipEnabled;	//update the value
				firePropertyChange(TOOLTIP_ENABLED_PROPERTY, Boolean.valueOf(oldTooltipEnabled), Boolean.valueOf(newTooltipEnabled));
			}
		}

	/**The list of installed export strategies, from most recently added to earliest added.*/
	private List<ExportStrategy<? super C>> exportStrategyList=new CopyOnWriteArrayList<ExportStrategy<? super C>>();

		/**Adds an export strategy to the component.
		The export strategy will take prececence over any compatible export strategy previously added.
		@param exportStrategy The export strategy to add.
		*/
		public void addExportStrategy(final ExportStrategy<? super C> exportStrategy) {exportStrategyList.add(0, exportStrategy);}	//add the export strategy to the beginning of the list

		/**Removes an export strategy from the component.
		@param exportStrategy The export strategy to remove.
		*/
		public void removeExportStrategy(final ExportStrategy<? super C> exportStrategy) {exportStrategyList.remove(exportStrategy);}	//remove the export strategy from the list

		/**Exports data from the component.
		Each export strategy, from last to first added, will be asked to export data, until one is successful.
		@return The object to be transferred, or <code>null</code> if no data can be transferred.
		*/
		public Transferable exportTransfer()
		{
			for(final ExportStrategy<? super C> exportStrategy:exportStrategyList)	//for each export strategy
			{
				final Transferable transferable=exportStrategy.exportTransfer(getThis());	//ask this export strategy to transfer data
				if(transferable!=null)	//if this export succeeded
				{
					return transferable;	//return this transferable data
				}
			}
			return null;	//indicate that no data could be exported
		}

	/**The list of installed import strategies, from most recently added to earliest added.*/
	private List<ImportStrategy<? super C>> importStrategyList=new CopyOnWriteArrayList<ImportStrategy<? super C>>();

		/**Adds an import strategy to the component.
		The import strategy will take prececence over any compatible import strategy previously added.
		@param importStrategy The importstrategy to add.
		*/
		public void addImportStrategy(final ImportStrategy<? super C> importStrategy) {importStrategyList.add(0, importStrategy);}	//add the import strategy to the beginning of the list

		/**Removes an import strategy from the component.
		@param importStrategy The import strategy to remove.
		*/
		public void removeImportStrategy(final ImportStrategy<? super C> importStrategy) {importStrategyList.remove(importStrategy);}	//remove the import strategy from the list

		/**Imports data to the component.
		Each import strategy, from last to first added, will be asked to import data, until one is successful.
		@param transferable The object to be transferred.
		@return <code>true</code> if the given object was be imported.
		*/
		public boolean importTransfer(final Transferable transferable)
		{
			for(final ImportStrategy<? super C> importStrategy:importStrategyList)	//for each importstrategy
			{
				if(importStrategy.canImportTransfer(getThis(), transferable))	//if this import strategy can import the data
				{
					if(importStrategy.importTransfer(getThis(), transferable))	//import the data; if we are successful
					{
						return true;	//stop trying to import data, and indicate we were successful
					}
				}
			}
			return false;	//indicate that no data could be imported
		}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalStateException if no controller is registered for this component type.
	@exception IllegalStateException if no view is registered for this component type.
	*/
	public AbstractComponent(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultLabelModel(session));	//construct the component with a default label model
	}

	/**Session, ID, and label model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param labelModel The component label model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalStateException if no controller is registered for this component type.
	@exception IllegalStateException if no view is registered for this component type.
	*/
	public AbstractComponent(final GuiseSession session, final String id, final LabelModel labelModel)
	{
		super(session);	//construct the parent class
		if(id!=null)	//if an ID was provided
		{
			this.id=checkValidComponentID(id);	//save the ID, checking for compliance
		}
		else	//if an ID was not provided
		{
			this.id=getSession().generateComponentID();	//ask the session to generate a new ID
//TODO del when works			this.id=getVariableName(getClass());	//create an ID by transforming the simple class name to a variable name
		}
		this.labelModel=checkInstance(labelModel, "Label model cannot be null.");	//save the label model
		this.labelModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the label model
		controller=session.getApplication().getController(getThis());	//ask the application for a controller
		if(controller==null)	//if we couldn't find a controller
		{
			throw new IllegalStateException("No registered controller for "+getClass().getName());	//TODO use a better error
		}
		assert CORNER_ARC_SIZE_PROPERTIES.length==cornerArcSizes.length : "Number of available corners changed.";
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

	/**Initializes the component with a description in an RDF resource file.
	This method calls {@link #initialize()} after loading.
	@param resourceKey The key to an RDF description resource file.
	@exception MissingResourceException if no resource could be found associated with the given key.
	*/
/*TODO del if not used
	public void initializeFromResource(final String resourceKey) throws MissingResourceException
	{
		final String descriptionResource=getSession().getStringResource(resourceKey);	//get the description resource
	}
*/

	/**Initializes the component with a description in an RDF resource file.
	This method calls {@link #initialize()} after loading.
	@param resourceKey The key to an RDF description resource file.
	@exception MissingResourceException if no resource could be found associated with the given key.
	*/
/*TODO del when works
	public void initializeFromDescription(final String resourceKey) throws MissingResourceException
	{
		final String descriptionResource=getSession().getStringResource(resourceKey);	//get the description resource
	}
*/

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

	/**Processes an event for the component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller.
	@param event The event to be processed.
	@see #getController()
	@see GuiseContext.State#PROCESS_EVENT
	*/
	public void processEvent(final ControlEvent event)
	{
		getController().processEvent(getThis(), event);	//tell the controller to process the event
	}

	/**Updates the view of this component.
	This method should not normally be called directly by applications.
	This method delegates to the installed view
	@param context Guise context information.
	@exception IOException if there is an error updating the view.
	@see #getView()
	@see GuiseContext.State#UPDATE_VIEW
	*/
	public <GC extends GuiseContext> void updateView(final GC context) throws IOException
	{
		final View<? super GC, ? super C> view=(View<? super GC, ? super C>)getView();	//get the view
		view.update(context, getThis());	//tell the view to update
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
	@SuppressWarnings("unchecked")
	public Iterator<MouseListener> getMouseListeners()
	{
		return (Iterator<MouseListener>)getEventListenerManager().getListeners(MouseListener.class);	//remove the listener
	}

	/**Fires a mouse entered event to all registered mouse listeners.
	@param componentBounds The absolute bounds of the component.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@exception NullPointerException if one or more of the arguments are <code>null</code>.
	@see MouseListener
	@see MouseEvent
	*/
	public void fireMouseEntered(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition)
	{
		if(hasMouseListeners())	//if there are mouse listeners registered
		{
			final MouseEvent mouseEvent=new MouseEvent(getSession(), getThis(), componentBounds, viewportBounds, mousePosition);	//create a new mouse event
			getSession().queueEvent(new PostponedMouseEvent(getEventListenerManager(), mouseEvent, PostponedMouseEvent.EventType.ENTERED));	//tell the Guise session to queue the event
		}
	}

	/**Fires a mouse exited event to all registered mouse listeners.
	@param componentBounds The absolute bounds of the component.
	@param viewportBounds The absolute bounds of the viewport.
	@param mousePosition The position of the mouse relative to the viewport.
	@exception NullPointerException if one or more of the arguments are <code>null</code>.
	@see MouseListener
	@see MouseEvent
	*/
	public void fireMouseExited(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition)
	{
		if(hasMouseListeners())	//if there are mouse listeners registered
		{
			final MouseEvent mouseEvent=new MouseEvent(getSession(), getThis(), componentBounds, viewportBounds, mousePosition);	//create a new mouse event
			getSession().queueEvent(new PostponedMouseEvent(getEventListenerManager(), mouseEvent, PostponedMouseEvent.EventType.EXITED));	//tell the Guise session to queue the event
		}
	}

	/**Determines the root parent of the given component.
	@param component The component for which the root should be found.
	@return The root component (the component or ancestor which has no parent).
	*/
	public static Component<?> getRootComponent(Component<?> component)
	{
		Component<?> parent;	//we'll keep track of the parent at each level when finding the root component
		while((parent=component.getParent())!=null)	//get the parent; while there is a parent
		{
			component=parent;	//move up the chain
		}
		return component;	//return whatever component we ended up with without a parent
	}

	/**Retrieves a component with the given ID.
	This method checks the given component and all descendant components.
	@param component The component that should be checked, along with its descendants, for the given ID.
	@return The component with the given ID, or <code>null</code> if this component and all descendant components do not have the given ID. 
	*/
	public static Component<?> getComponentByID(final Component<?> component, final String id)
	{
		if(component.getID().equals(id))	//if this component has the correct ID
		{
			return component;	//return this component
		}
		else if(component instanceof CompositeComponent)	//if this component doesn't have the correct ID, but it is a composite component
		{
			for(final Component<?> childComponent:(CompositeComponent<?>)component)	//for each child component
			{
				final Component<?> matchingComponent=getComponentByID(childComponent, id);	//see if we can find a component in this tree
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
	public static Component<?> getComponentByName(final Component<?> component, final String name)
	{
		if(name.equals(component.getName()))	//if this component has the correct name
		{
			return component;	//return this component
		}
		else if(component instanceof CompositeComponent)	//if this component doesn't have the correct name, but it is a composite component
		{
			for(final Component<?> childComponent:(CompositeComponent<?>)component)	//for each child component
			{
				final Component<?> matchingComponent=getComponentByName(childComponent, name);	//see if we can find a component in this tree
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
	public static List<Component<?>> getDirtyComponents(final Component<?> component)
	{
		return getDirtyComponents(component, new ArrayList<Component<?>>());	//gather dirty components and put them in a list
	}

	/**Retrieves all components that have views needing updated.
	This method checks the given component and all descendant components.
	If a given component is dirty, its child views will not be checked.
	@param component The component that should be checked, along with its descendants, for out-of-date views.
	@param dirtyComponents The list that will be updated with more dirty components if any are found.
	@return The components with views needing to be updated. 
	*/
	public static List<Component<?>> getDirtyComponents(final Component<?> component, final List<Component<?>> dirtyComponents)
	{
		if(!component.getView().isUpdated())	//if this component's view isn't updated
		{
			dirtyComponents.add(component);	//add this component to the list
		}
		else if(component instanceof CompositeComponent)	//if the component's view is updated, check its children if it has any
		{
			for(final Component<?> childComponent:(CompositeComponent<?>)component)	//for each child component
			{
				getDirtyComponents(childComponent, dirtyComponents);	//gather dirty components in this child hierarchy
			}
		}
		return dirtyComponents;
	}

	/**Changes the updated status of the views of an entire component descendant hierarchy.
	@param newUpdated Whether the views of this component and all child components are up to date.
	*/
	public static void setUpdated(final Component<?> component, final boolean newUpdated)
	{
		component.getView().setUpdated(newUpdated);	//change the updated status of this component's view
		if(component instanceof CompositeComponent)	//if the component is a composite component
		{
			for(final Component<?> childComponent:(CompositeComponent<?>)component)	//for each child component
			{
				setUpdated(childComponent, newUpdated);	//changed the updated status for this child's hierarchy
			}
		}
	}

	/**Retrieves the the notifications of all components in a hierarchy.
	This method checks the given component and all descendant components.
	@param component The component from which, along with its descendants, notifications should be retrieved.
	@return The notifications of all components in the hierarchy. 
	*/
	public static List<Notification> getNotifications(final Component<?> component)
	{
		return getNotifications(component, new ArrayList<Notification>());	//gather notifications and put them in a list
	}

	/**Retrieves the the notifications of all components in a hierarchy.
	This method checks the given component and all descendant components.
	@param component The component from which, along with its descendants, notifications should be retrieved.
	@param notifications The list that will be updated with more dirty components if any are found.
	@return The notifications of all components in the hierarchy. 
	*/
	public static List<Notification> getNotifications(final Component<?> component, final List<Notification> notifications)
	{
		final Notification notification=component.getNotification();	//get the component's notification, if any
		if(notification!=null)	//if a notification is available
		{
			notifications.add(notification);	//add this notification to the list
		}
		if(component instanceof CompositeComponent)	//if the component is a composite component, check its children
		{
			for(final Component<?> childComponent:(CompositeComponent<?>)component)	//for each child component
			{
				getNotifications(childComponent, notifications);	//gather notifications from this child hierarchy
			}
		}
		return notifications;
	}
	
	/**Determines a URI value either explicitly set or stored in the resources.
	If a value is explicitly specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	A resource will be retrieved first using an appended physical axis designator (".x" or ".y") based upon the given flow, if any.
	For example, if a thumb image resource key of "<var>image</var>" is requested, first a resource of "<var>image</var>.x" will be retrieved (for Western orientation and a line axis),
	after which a resource for "<var>image</var>" will be retrieved if there is no resource for "<var>image</var>.x".
	@param value The value explicitly set, which will override any resource.
	@param resourceKey The key for looking up a resource if no value is explicitly set.
	@param flow The flow for which a physical axis should be determined, or <code>null</code> if the flow is irrelevant.
	@return The URI value, or <code>null</code> if there is no value available, neither explicitly set nor in the resources.
	@exception MissingResourceException if there was an error loading the value from the resources.
	*/
	protected URI getURI(final URI value, final String resourceKey, final Flow flow) throws MissingResourceException
	{
		if(value!=null)	//if a value is provided
		{
			return value;	//return the specified value
		}
		else if(resourceKey!=null)	//if no value is provided, but if a resource key is provided
		{
			if(flow!=null)	//if a flow is specified
			{
				try
				{
					return getSession().getURIResource(resourceKey+getResourceKeyAxisSuffix(flow));	//get a specialized resource key for the physical axis of the given flow in relation to this component's orientation
				}
				catch(final MissingResourceException missingResourceException)	//ignore a missing axis resource and try the general resource
				{
				}
			}
			return getSession().getURIResource(resourceKey);	//lookup the value from the resources normally
		}
		else	//if neither a value nor a resource key are provided
		{
			return null;	//there is no value available
		}
	}

	/**Determines a URI value either explicitly set or stored in the resources.
	If a value is explicitly specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	A resource will be retrieved first using an appended bearing designator (".W", ".WbS", etc) based upon the given bearing, if any.
	For example, with a bearing of 250 and a resource key of "myTether", a resource key will be requested using "myResource.WSW", "myResource.SWbW", "myResource.SW", etc.
		until all compass points are exhausted, after which a resource key of "myResource" will be requested.
	@param value The value explicitly set, which will override any resource.
	@param resourceKey The key for looking up a resource if no value is explicitly set.
	@param bearing The bearing to use in determining the resource key, or <code>null</code> if the bearing is irrelevant.
	@return The URI value, or <code>null</code> if there is no value available, neither explicitly set nor in the resources.
	@exception IllegalArgumentException if the given bearing is greater than 360.
	@exception MissingResourceException if there was an error loading the value from the resources.
	*/
	protected URI getURI(final URI value, final String resourceKey, final BigDecimal bearing) throws MissingResourceException
	{
		if(value!=null)	//if a value is provided
		{
			return value;	//return the specified value
		}
		else if(resourceKey!=null)	//if no value is provided, but if a resource key is provided
		{
			if(bearing!=null)	//if a bearing was given
			{
				int ordinal=CompassPoint.getCompassPoint(bearing).ordinal();	//get the ordinal of the compass point nearest the bearing
				final CompassPoint[] compassPoints=CompassPoint.values();	//get the compass point values
				//TODO decide if the closest point algorithm should be removed, because it may result in the incorrect image for a particular bearing
				do
				{
					final CompassPoint compassPoint=compassPoints[ordinal];	//get this compass point
					try
					{
						return getSession().getURIResource(resourceKey+'.'+compassPoint.getAbbreviation());	//get a specialized resource key for the compass point abbreviation in the form resourceKey.abbreviation TODO use a constant
					}
					catch(final MissingResourceException missingResourceException)	//ignore all missing bearing-specific resources
					{
						--ordinal;	//try the previous compass point counter-clockwise
					}						
				}
				while(ordinal>=0);	//keep looking for compass points until we reach north
			}
			return getSession().getURIResource(resourceKey);	//lookup the value from the resources normally
		}
		else	//if neither a value nor a resource key are provided
		{
			return null;	//there is no value available
		}
	}

	/**Returns a resource key suffix representing the physical axis based upon the given flow relative to the component's orientation.
	@param flow The flow for which a physical axis should be determined.
	@return The appropriate resource key suffix for the given flow.
	@see #RESOURCE_KEY_X_SUFFIX
	@see #RESOURCE_KEY_Y_SUFFIX
	*/ 
	protected String getResourceKeyAxisSuffix(final Flow flow)
	{
		final Axis axis=getComponentOrientation().getAxis(flow);	//get the physical axis
		switch(axis)
		{
			case X:
				return RESOURCE_KEY_X_SUFFIX;
			case Y:
				return RESOURCE_KEY_Y_SUFFIX;
			case Z:
			default:
				throw new IllegalArgumentException("Unsupported axis: "+axis);
		}	
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
		fireNotified(new NotificationEvent(getSession(), getThis(), notification));	//create and fire a new notification event
	}

	/**Fires an event to all registered notification listeners with the new notification information.
	Parents are expected to refire the notification event up the hierarchy.
	@param notificationEvent The notification event to send to the notification listeners.
	@exception NullPointerException if the given notification event is <code>null</code>.
	@see NotificationListener
	*/
	protected void fireNotified(final NotificationEvent notificationEvent)
	{
		getSession().queueEvent(new PostponedNotificationEvent(getEventListenerManager(), notificationEvent));	//tell the Guise session to queue the event
	}

	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return getID().hashCode();	//return the hash code of the ID
	}

	/**Indicates whether some other object is "equal to" this one.
	This implementation returns whether the object is a component with the same ID.
	@param object The reference object with which to compare.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		return object instanceof Component && getID().equals(((Component<?>)object).getID());	//see if the other object is a component with the same ID
	}

	/**@return A string representation of this component.*/
	public String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder(super.toString());	//create a string builder for constructing the string
		final String id=getID();	//get the component's ID
		if(id!=null)	//if this component has an ID
		{
			stringBuilder.append(' ').append('[').append(id).append(']');	//append the ID
		}
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
	public static abstract class AbstractFlyoverStrategy<S extends Component<?>> implements FlyoverStrategy<S>
	{
		/**The component for which this object will control flyovers.*/
		private final S component;

			/**@return The component for which this object will control flyovers.*/
			public S getComponent() {return component;}
			
		/**The preferred width of the flyover component, or <code>null</code> if no preferred width has been specified.*/
		private Extent preferredWidth=null;

			/**@return The preferred width of the flyover component, or <code>null</code> if no preferred width has been specified.*/
			public Extent getPreferredWidth() {return preferredWidth;}

			/**Sets the preferred width of the flyover component.
			@param newPreferredWidth The new preferred width of the flyover component, or <code>null</code> there is no width preference.
			*/
			public void setPreferredWidth(final Extent newPreferredWidth)
			{
				if(!ObjectUtilities.equals(preferredWidth, newPreferredWidth))	//if the value is really changing
				{
					final Extent oldPreferredWidth=preferredWidth;	//get the old value
					preferredWidth=newPreferredWidth;	//actually change the value
				}			
			}

		/**The preferred height of the flyover component, or <code>null</code> if no preferred height has been specified.*/
		private Extent preferredHeight=null;

			/**@return The preferred height of the flyover component, or <code>null</code> if no preferred height has been specified.*/
			public Extent getPreferredHeight() {return preferredHeight;}

			/**Sets the preferred height of the flyover component.
			@param newPreferredHeight The new preferred height of the flyover component, or <code>null</code> there is no height preference.
			*/
			public void setPreferredHeight(final Extent newPreferredHeight)
			{
				if(!ObjectUtilities.equals(preferredHeight, newPreferredHeight))	//if the value is really changing
				{
					final Extent oldPreferredHeight=preferredHeight;	//get the old value
					preferredHeight=newPreferredHeight;	//actually change the value
				}			
			}
			
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

		/**Called when the mouse enters the source.
		This implementation opens the flyover.
		@param mouseEvent The event providing mouse information
		@see #openFlyover()
		*/
		public void mouseEntered(final MouseEvent mouseEvent)
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
		public void mouseExited(final MouseEvent mouseEvent)
		{
			closeFlyover();	//close the flyover if it is open
		}
	}	
	
	/**The default strategy for showing and hiding flyovers in response to mouse events.
//TODO del	This implementation uses flyover frames to represent flyovers.
//TODO del	This implementation defaults to an opacity fade effect for opening with a 500 millisecond delay.
	@param <S> The type of component for which this object is to control flyovers.
	@author Garret Wilson
	*/
	public static class DefaultFlyoverStrategy<S extends Component<?>> extends AbstractFlyoverStrategy<S>
	{
		/**The frame used for displaying flyovers.*/
		private FlyoverFrame<?> flyoverFrame=null;

		/**Component constructor.
		@param component The component for which this object will control flyovers.
		@exception NullPointerException if the given component is <code>null</code>.
		*/
		public DefaultFlyoverStrategy(final S component)
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
				final Extent preferredWidth=getPreferredWidth();	//get the preferred width
				if(preferredWidth!=null)	//if there is a preferred width
				{
					flyoverFrame.setPreferredWidth(preferredWidth);	//set the flyover preferred width
				}
				final Extent preferredHeight=getPreferredHeight();	//get the preferred height
				if(preferredHeight!=null)	//if there is a preferred height
				{
					flyoverFrame.setPreferredHeight(preferredHeight);	//set the flyover preferred height
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
		protected FlyoverFrame<?> createFrame()
		{
			final S component=getComponent();	//get the component
			final GuiseSession session=component.getSession();	//get the session
			final FlyoverFrame<?> frame=new DefaultFlyoverFrame(session);	//create a default frame
			frame.setRelatedComponent(getComponent());	//tell the flyover frame with which component it is related
			final Message message=new Message(session);	//create a new message
			message.setMessageContentType(component.getDescriptionContentType());	//set the appropriate message content
			message.setMessage(component.getDescription());	//set the appropriate message text
			message.setMessageResourceKey(component.getDescriptionResourceKey());	//set the appropriate message text resource			
			frame.setContent(message);	//put the message in the frame
			return frame;	//return the frame we created
		}
	}

}
