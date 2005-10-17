package com.javaguise.component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.event.EventListenerManager;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.Debug;
import com.javaguise.component.layout.Orientation;
import com.javaguise.component.transfer.*;
import com.javaguise.context.GuiseContext;
import com.javaguise.controller.ControlEvent;
import com.javaguise.controller.Controller;
import com.javaguise.event.GuiseBoundPropertyObject;
import com.javaguise.geometry.Dimensions;
import com.javaguise.geometry.Extent;
import com.javaguise.model.Model;
import com.javaguise.session.GuiseSession;
import com.javaguise.style.Color;
import com.javaguise.style.RGBColor;
import com.javaguise.view.View;

import static com.garretwilson.lang.CharSequenceUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

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

	/**The data model used by this component.*/
	private final Model model;

		/**@return The data model used by this component.*/
		public Model getModel() {return model;}

	/**The foreground color of the component, or <code>null</code> if no foreground color is specified for this component.*/
	private Color<?> color=null;

		/**@return The foreground color of the component, or <code>null</code> if no foreground color is specified for this component.
		@see #determineColor()
		*/
		public Color<?> getColor() {return color;}

		/**Sets the foreground color of the component.
		This is a bound property.
		@param newColor The foreground color of the component, or <code>null</code> if the default foreground color should be used.
		@see Component#COLOR_PROPERTY 
		*/
		public void setColor(final Color<?> newColor)
		{
			if(color!=newColor)	//if the value is really changing
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

		/**Sets the preferred extent of the component.
		This is a bound property.
		@param newPreferredWidth The new preferred extent of the component, or <code>null</code> there is no width preference.
		@see Component#PREFERRED_WIDTH_PROPERTY 
		*/
		public void setPreferredWidth(final Extent newPreferredWidth)
		{
			if(preferredWidth!=newPreferredWidth)	//if the value is really changing
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

		/**Sets the preferred extent of the component.
		This is a bound property.
		@param newPreferredHeight The new preferred extent of the component, or <code>null</code> there is no height preference.
		@see Component#PREFERRED_HEIGHT_PROPERTY 
		*/
		public void setPreferredHeight(final Extent newPreferredHeight)
		{
			if(preferredHeight!=newPreferredHeight)	//if the value is really changing
			{
				final Extent oldPreferredHeight=preferredHeight;	//get the old value
				preferredHeight=newPreferredHeight;	//actually change the value
				firePropertyChange(PREFERRED_HEIGHT_PROPERTY, oldPreferredHeight, newPreferredHeight);	//indicate that the value changed
			}			
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
				controller=checkNull(newController, "Controller cannot be null.");	//actually change values
				firePropertyChange(CONTROLLER_PROPERTY, oldController, newController);	//indicate that the value changed				
			}
		}

	/**The view installed in this component.*/
	private View<? extends GuiseContext, ? super C> view;

		/**@return The view installed in this component.*/
		public View<? extends GuiseContext, ? super C> getView() {return view;}

		/**Sets the view used by this component.
		This is a bound property.
		@param newView The new view to use.
		@see Component#VIEW_PROPERTY
		@exception NullPointerException if the given view is <code>null</code>.
		*/
		public void setView(final View<? extends GuiseContext, ? super C> newView)
		{
			if(newView!=checkNull(view, "View cannot be null"))	//if the value is really changing
			{
				final View<? extends GuiseContext, ? super C> oldView=view;	//get a reference to the old value
				oldView.uninstalled(getThis());	//tell the old view it's being uninstalled
				view=newView;	//actually change values
				oldView.installed(getThis());	//tell the new view it's being installed
				firePropertyChange(VIEW_PROPERTY, oldView, newView);	//indicate that the value changed				
			}
		}

	/**The thread-safe list of errors.*/
	private final List<Throwable> errorList=new CopyOnWriteArrayList<Throwable>();

		/**@return An iterable interface to all errors associated with this component.*/
		public Iterable<Throwable> getErrors() {return errorList;}

		/**@return <code>true</code> if there is at least one error associated with this component.*/
		public boolean hasErrors() {return !errorList.isEmpty();}

		/**Adds an error to the component.
		@param error The error to add.
		*/
		public void addError(final Throwable error) {errorList.add(error);
		
getView().setUpdated(false);	//TODO fix hack; make the view listen for error changes		
		
		}

		/**Adds errors to the component.
		@param errors The errors to add.
		*/
		public void addErrors(final Collection<? extends Throwable> errors) {errorList.addAll(errors);
		
getView().setUpdated(false);	//TODO fix hack; make the view listen for error changes		
		
		}

		/**Removes a specific error from this component.
		@param error The error to remove.
		*/
		public void removeError(final Throwable error) {errorList.remove(error);
		
		getView().setUpdated(false);	//TODO fix hack; make the view listen for error changes		
		
		}

		/**Clears all errors associated with this component.*/
		public void clearErrors() {errorList.clear();
		
		getView().setUpdated(false);	//TODO fix hack; make the view listen for error changes		
		
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
		public Orientation getOrientation() {return orientation=null;}

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
			if(styleID!=newStyleID)	//if the value is really changing
			{
				final String oldStyleID=styleID;	//get the current value
				styleID=newStyleID;	//update the value
				firePropertyChange(STYLE_ID_PROPERTY, oldStyleID, newStyleID);
			}
		}

	/**Whether the component is visible.*/
	private boolean visible=true;

		/**@return Whether the component is visible.*/
		public boolean isVisible() {return visible;}

		/**Sets whether the component is visible.
		This is a bound property of type <code>Boolean</code>.
		@param newVisible <code>true</code> if the component should be visible, else <code>false</code>.
		@see Component#VISIBLE_PROPERTY
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

	/**Whether the component has dragging enabled.*/
	private boolean dragEnabled=false;

		/**@return Whether the component has dragging enabled.*/
		public boolean isDragEnabled() {return dragEnabled;}

		/**Sets whether the component is has dragging enabled.
		This is a bound property of type <code>Boolean</code>.
		@param newDragEnabled <code>true</code> if the component should allow dragging, else false, else <code>false</code>.
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
		@param newDropEnabled <code>true</code> if the component should allow dropping, else false, else <code>false</code>.
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

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalStateException if no controller is registered for this component type.
	@exception IllegalStateException if no view is registered for this component type.
	*/
	public AbstractComponent(final GuiseSession session, final String id, final Model model)
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
		this.model=checkNull(model, "Model cannot be null.");	//save the model
		controller=session.getApplication().getController(getThis());	//ask the application for a controller
		if(controller==null)	//if we couldn't find a controller
		{
			throw new IllegalStateException("No registered controller for "+getClass().getName());	//TODO use a better error
		}
		view=session.getApplication().getView(getThis());	//ask the application for a view
		if(view==null)	//if we couldn't find a view
		{
			throw new IllegalStateException("No registered view for "+getClass().getName());	//TODO use a better error
		}
		view.installed(getThis());	//tell the view it's being installed
	}

	/**Determines whether the models of this component and all of its child components are valid.
	This version checks to ensure the component's model is valid.
	@return Whether the models of this component and all of its child components are valid.
	*/
	public boolean isValid()
	{
/*TODO decide whether this is needed, now that we've refactored information into the component
		if(!getController().isValid())	//if the controller isn't valid
		{
			return false;	//although the model may be valid, its view representation is not
		}
*/
//TODO del		return true;	//indicate that this component is valid
//TODO del Debug.trace("###checking to see if model is valid for", getID(), getModel().isValid());
		return getModel().isValid();	//return whether the model is valid
	}

	/**Validates the model of this component and all child components.
	The component will be updated with error information.
	This version validates the associated model.
	@exception ComponentExceptions if there was one or more validation error.
	*/
	public void validate() throws ComponentExceptions
	{
		try
		{
			clearErrors();	//clear all errors TODO check
			getModel().validate();	//validate the model
		}
		catch(final ComponentException componentException)	//if there is a component error
		{
			componentException.setComponent(this);	//make sure the exception knows to which component it relates
			addError(componentException);	//add this error to the component
			throw new ComponentExceptions(componentException);	//throw a new component exception list exception
		}
	}

	/**Processes an event for the component.
	This method should not normally be called directly by applications.
	This method delegates to the installed controller.
	@param event The event to be processed.
	@exception ComponentExceptions if there was a component-related error processing the event.
	@see #getController()
	@see GuiseContext.State#PROCESS_EVENT
	*/
	public void processEvent(final ControlEvent event) throws ComponentExceptions
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

	/**Retrieves all components that have views needing updated.
	This method checks the given component and all descendant components.
	If a given component is dirty, its child views will not be checked.
	@param component The component that should be checked, along with its descendants, for out-of-date views.
	@return The components with views needing to be updated. 
	*/
	public static Collection<Component<?>> getDirtyComponents(final Component<?> component)
	{
		return getDirtyComponents(component, new ArrayList<Component<?>>());	//gather dirty components and put them in a list
	}

	/**Retrieves all components that have views needing updated.
	This method checks the given component and all descendant components.
	If a given component is dirty, its child views will not be checked.
	@param component The component that should be checked, along with its descendants, for out-of-date views.
	@param dirtyComponents The collection that will be updated with more dirty components if any are found.
	@return The components with views needing to be updated. 
	*/
	public static Collection<Component<?>> getDirtyComponents(final Component<?> component, final Collection<Component<?>> dirtyComponents)
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
}
