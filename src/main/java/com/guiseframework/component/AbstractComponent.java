/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

import org.ploop.*;
import org.ploop.graph.PLOOPURFGenerator;
import org.ploop.graph.PLOOPURFProcessor;
import org.urframework.URFResource;

import static com.globalmentor.java.Arrays.*;
import static com.globalmentor.java.Objects.*;

import com.globalmentor.event.*;
import com.globalmentor.java.*;
import com.globalmentor.java.Objects;
import com.globalmentor.net.ContentType;
import com.globalmentor.util.*;
import com.guiseframework.*;
import com.guiseframework.component.effect.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.component.transfer.*;
import com.guiseframework.event.*;
import com.guiseframework.geometry.*;
import com.guiseframework.input.*;
import com.guiseframework.model.*;
import com.guiseframework.model.ui.AbstractPresentationModel;
import com.guiseframework.platform.*;
import com.guiseframework.theme.Theme;

/**
 * An abstract implementation of a component.
 * <p>
 * A component should never fire a property event directly. It should rather create a postponed event and queue that event with the session. This implementation
 * automatically handles postponed property change events when {@link #firePropertyChange(String, Object, Object)} or a related method is called.
 * </p>
 * <p>
 * Property changes to a component's constraints are repeated with the component as the source and the constraints as the target.
 * </p>
 * @author Garret Wilson
 */
public abstract class AbstractComponent extends AbstractPresentationModel implements Component {

	/** The object managing event listeners. */
	private final EventListenerManager eventListenerManager = new EventListenerManager();

	/** @return The object managing event listeners. */
	protected EventListenerManager getEventListenerManager() {
		return eventListenerManager;
	}

	/** The thread-safe set of properties saved and loaded as preferences. */
	private final Set<String> preferenceProperties = new CopyOnWriteArraySet<String>();

	@Override
	public void addPreferenceProperty(final String propertyName) {
		preferenceProperties.add(propertyName);
	}

	@Override
	public boolean isPreferenceProperty(final String propertyName) {
		return preferenceProperties.contains(propertyName);
	}

	@Override
	public Iterable<String> getPreferenceProperties() {
		return preferenceProperties;
	}

	@Override
	public void removePreferenceProperty(final String propertyName) {
		preferenceProperties.remove(propertyName);
	}

	/** The info model decorated by this component. */
	private final InfoModel infoModel;

	/** @return The info model decorated by this component. */
	protected InfoModel getInfoModel() {
		return infoModel;
	}

	/**
	 * The name of the component, not guaranteed to be unique (but guaranteed not to be the empty string) and useful only for searching for components within a
	 * component sub-hierarchy, or <code>null</code> if the component has no name.
	 */
	private String name = null;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String newName) {
		if(!Objects.equals(name, newName)) { //if the value is really changing
			if(newName != null && newName.length() == 0) { //if the empty string was passed
				throw new IllegalArgumentException("Name cannot be the empty string.");
			}
			final String oldName = name; //get the old value
			name = newName; //actually change the value
			firePropertyChange(NAME_PROPERTY, oldName, newName); //indicate that the value changed
		}
	}

	@Override
	public URI getGlyphURI() {
		return getInfoModel().getGlyphURI();
	}

	@Override
	public void setGlyphURI(final URI newLabelIcon) {
		getInfoModel().setGlyphURI(newLabelIcon);
	}

	@Override
	public String getLabel() {
		return getInfoModel().getLabel();
	}

	@Override
	public void setLabel(final String newLabelText) {
		getInfoModel().setLabel(newLabelText);
	}

	@Override
	public ContentType getLabelContentType() {
		return getInfoModel().getLabelContentType();
	}

	@Override
	public void setLabelContentType(final ContentType newLabelTextContentType) {
		getInfoModel().setLabelContentType(newLabelTextContentType);
	}

	@Override
	public String getDescription() {
		return getInfoModel().getDescription();
	}

	@Override
	public void setDescription(final String newDescription) {
		getInfoModel().setDescription(newDescription);
	}

	@Override
	public ContentType getDescriptionContentType() {
		return getInfoModel().getDescriptionContentType();
	}

	@Override
	public void setDescriptionContentType(final ContentType newDescriptionContentType) {
		getInfoModel().setDescriptionContentType(newDescriptionContentType);
	}

	@Override
	public String getInfo() {
		return getInfoModel().getInfo();
	}

	@Override
	public void setInfo(final String newInfo) {
		getInfoModel().setInfo(newInfo);
	}

	@Override
	public ContentType getInfoContentType() {
		return getInfoModel().getInfoContentType();
	}

	@Override
	public void setInfoContentType(final ContentType newInfoContentType) {
		getInfoModel().setInfoContentType(newInfoContentType);
	}

	/**
	 * The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this component.
	 */
	private Constraints constraints = null;

	@Override
	public Constraints getConstraints() {
		return constraints;
	}

	/**
	 * Sets the layout constraints of this component. This is a bound property.
	 * @param newConstraints The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified
	 *          for this component.
	 * @see #CONSTRAINTS_PROPERTY
	 */
	public void setConstraints(final Constraints newConstraints) { //TODO see if any of the specialized components throw constraints property changes as well
		if(constraints != newConstraints) { //if the value is really changing
			final Constraints oldConstraints = constraints; //get the old value
			if(oldConstraints != null) { //if there were old constraints
				oldConstraints.removePropertyChangeListener(getRepeatPropertyChangeListener()); //stop repeating constraints property change events
			}
			constraints = newConstraints; //actually change the value
			if(newConstraints != null) { //if there are new constraints
				newConstraints.addPropertyChangeListener(getRepeatPropertyChangeListener()); //repeat constraints property change events
			}
			firePropertyChange(CONSTRAINTS_PROPERTY, oldConstraints, newConstraints); //indicate that the value changed
		}
	}

	/** The strategy for processing input, or <code>null</code> if this component has no input strategy. */
	private InputStrategy inputStrategy = null;

	@Override
	public InputStrategy getInputStrategy() {
		return inputStrategy;
	}

	@Override
	public void setInputStrategy(final InputStrategy newInputStrategy) {
		if(!Objects.equals(inputStrategy, newInputStrategy)) { //if the value is really changing
			final InputStrategy oldInputStrategy = inputStrategy; //get the current value
			inputStrategy = newInputStrategy; //update the value
			firePropertyChange(INPUT_STRATEGY_PROPERTY, oldInputStrategy, newInputStrategy);
		}
	}

	/** The notification associated with the component, or <code>null</code> if no notification is associated with this component. */
	private Notification notification = null;

	@Override
	public Notification getNotification() {
		return notification;
	}

	@Override
	public void setNotification(final Notification newNotification) {
		if(!Objects.equals(notification, newNotification)) { //if the value is really changing
			final Notification oldNotification = notification; //get the old value
			notification = newNotification; //actually change the value
			//TODO del unless status is promoted to Component				updateStatus();	//update the status before firing the notification event so that the status will already be updated for the listeners to access
			firePropertyChange(NOTIFICATION_PROPERTY, oldNotification, newNotification); //indicate that the value changed
			if(newNotification != null) { //if a new notification is provided
				fireNotified(newNotification); //fire a notification event here and up the hierarchy
			}
		}
	}

	/**
	 * Whether the valid property has been initialized. Updating validity in a super constructor can sometimes call {@link #determineValid()} before subclass
	 * variables are initialized, especially in containers that add and/or remove children in the super constructor before subclasses have a chance to create
	 * class variables in their constructors, so this implementation of valid is lazily-initialized only when needed. The value is always initialized when being
	 * read or being set, and the {@link #updateValid()} method only calls {@link #determineValid()} if the valid property is initialized or there is at least one
	 * listener for the {@link #VALID_PROPERTY}.
	 */
	private boolean validInitialized = false;

	/**
	 * Whether the state of the component and all child component represents valid user input. Updating validity in a super constructor can sometimes call
	 * {@link #determineValid()} before subclass variables are initialized, especially in containers that add and/or remove children in the super constructor
	 * before subclasses have a chance to create class variables in their constructors, so this implementation of valid is lazily-initialized only when needed.
	 * The value is always initialized when being read or being set, and the {@link #updateValid()} method only calls {@link #determineValid()} if the valid
	 * property is initialized or there is at least one listener for the {@link #VALID_PROPERTY}.
	 */
	private Boolean valid = null; //start with an uninitialized valid property

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation initializes the valid property if needed.
	 * </p>
	 */
	@Override
	public boolean isValid() {
		if(valid == null) { //if valid is not yet initialized TODO eliminate race condition
			//TODO del Log.traceStack("ready to call determineValid() for the first time from inside isValid()");
			valid = Boolean.TRUE; //initialize valid to an arbitrary value so that if determineValid() calls isValid() there won't be inifinite recursion
			valid = Boolean.valueOf(determineValid()); //determine validity
		}
		return valid.booleanValue(); //return the valid state
	}

	/*TODO del		
			public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener)
			{
				if(VALID_PROPERTY.equals(propertyName))
				{
					Log.traceStack("we have a new validity listener:", listener);
				}
				super.addPropertyChangeListener(propertyName, listener);
			}
	*/

	/**
	 * Sets whether the state of the component and all child components represents valid user input This is a bound property of type {@link Boolean}. This
	 * implementation initializes the valid property if needed.
	 * @param newValid <code>true</code> if user input of this component and all child components should be considered valid
	 * @see #VALID_PROPERTY
	 */
	protected void setValid(final boolean newValid) {
		final boolean oldValid = isValid(); //get the current value
		if(oldValid != newValid) { //if the value is really changing
			final Boolean booleanNewValid = Boolean.valueOf(newValid); //get the BOolean form of the new value
			valid = booleanNewValid; //update the value
			firePropertyChange(VALID_PROPERTY, Boolean.valueOf(oldValid), booleanNewValid);
		}
	}

	/**
	 * Rechecks user input validity of this component and all child components, and updates the valid state. This implementation only updates the valid property
	 * if the property is already initialized or there is at least one listener to the {@link #VALID_PROPERTY}.
	 * @see #setValid(boolean)
	 */
	protected void updateValid() {
		if(valid != null || hasPropertyChangeListeners(VALID_PROPERTY)) { //if valid is initialized or there is a listener for the valid property
			/*TODO del
			if(valid==null)
			{
				Log.traceStack("ready to call determineValid() for the first time from inside updateValid()");	
			}
			*/
			/*TODO del
							final boolean newValid=determineValid();
			Log.trace("ready to set valid in", this, "to", newValid);
							setValid(newValid);	//update the vailidity after rechecking it
			Log.trace("now valid of", this, "is", isValid());
			*/
			setValid(determineValid()); //update the vailidity after rechecking it
		}
	}

	/**
	 * Checks the state of the component for validity. This version returns <code>true</code>.
	 * @return <code>true</code> if the component and all children passes all validity tests, else <code>false</code>.
	 */
	protected boolean determineValid() {
		return true; //default to being valid
	}

	/** The depictor for this object; the depictor is lazily installed to allow construction to complete before the depictor is installed. */
	private final Depictor<? extends Component> depictor;

	@Override
	public Depictor<? extends Component> getDepictor() {
		if(depictor.getDepictedObject() == null) { //if the depictor has not yet been installed TODO fix the race condition
			notifyDepictorInstalled(depictor); //tell the the depictor it has been installed
		}
		return depictor; //return the depictor
	}

	@Override
	public void processEvent(final PlatformEvent event) {
		getDepictor().processEvent(event); //ask the depictor to process the event
	}

	@Override
	public void depict() throws IOException {
		getDepictor().depict(); //ask the depictor to depict the object
	}

	/** The object depiction identifier */
	private final long depictID;

	@Override
	public long getDepictID() {
		return depictID;
	}

	/** The internationalization orientation of the component's contents, or <code>null</code> if the default orientation should be used. */
	private Orientation orientation = null;

	@Override
	public Orientation getOrientation() {
		return orientation;
	}

	@Override
	public Orientation getComponentOrientation() {
		final Orientation orientation = getOrientation(); //get this component's orientation
		if(orientation != null) { //if an orientation is explicitly set for this component
			return orientation; //return this component's orientation
		} else { //otherwise, try to defer to the parent
			final Component parent = getParent(); //get this component's parent
			if(parent != null) { //if we have a parent
				return parent.getComponentOrientation(); //return the parent's orientation
			} else { //if we don't have a parent
				return getSession().getOrientation(); //return the session's default orientation
			}
		}
	}

	@Override
	public void setOrientation(final Orientation newOrientation) {
		if(!Objects.equals(orientation, newOrientation)) { //if the value is really changing
			final Orientation oldOrientation = orientation; //get the old value
			orientation = newOrientation; //actually change the value
			firePropertyChange(ORIENTATION_PROPERTY, oldOrientation, newOrientation); //indicate that the value changed
		}
	}

	/** The parent of this component, or <code>null</code> if this component does not have a parent. */
	private CompositeComponent parent = null;

	@Override
	public CompositeComponent getParent() {
		return parent;
	}

	@Override
	@SuppressWarnings("unchecked")
	//we check to see if the ancestor is of the correct type before casting, so the cast is logically checked, though not syntactically checked
	public <A extends CompositeComponent> A getAncestor(final Class<A> ancestorClass) {
		final CompositeComponent parent = getParent(); //get this component's parent
		if(parent != null) { //if there is a parent
			return ancestorClass.isInstance(parent) ? (A)parent : parent.getAncestor(ancestorClass); //if the parent is of the correct type, return it; otherwise, ask it to search its own ancestors
		} else { //if there is no parent
			return null; //there is no such ancestor
		}
	}

	@Override
	public void setParent(final CompositeComponent newParent) {
		final CompositeComponent oldParent = parent; //get the old parent
		if(oldParent != newParent) { //if the parent is really changing
			if(newParent != null) { //if a parent is provided
				if(oldParent != null) { //if we already have a parent
					throw new IllegalStateException("Component " + this + " already has parent: " + oldParent);
				}
				if(newParent instanceof Container && !((Container)newParent).contains(this)) { //if the new parent is a container that is not really our parent
					throw new IllegalArgumentException("Provided parent container " + newParent + " is not really parent of component " + this);
				}
			} else { //if no parent is provided
				if(oldParent instanceof Container && ((Container)oldParent).contains(this)) { //if we had a container parent before, and that container still thinks this component is its child
					throw new IllegalStateException("Old parent container " + oldParent + " still thinks this component, " + this + ", is a child.");
				}
			}
			parent = newParent; //this is really our parent; make a note of it
		}
	}

	/** Whether the component has dragging enabled. */
	private boolean dragEnabled = false;

	@Override
	public boolean isDragEnabled() {
		return dragEnabled;
	}

	@Override
	public void setDragEnabled(final boolean newDragEnabled) {
		if(dragEnabled != newDragEnabled) { //if the value is really changing
			final boolean oldDragEnabled = dragEnabled; //get the current value
			dragEnabled = newDragEnabled; //update the value
			firePropertyChange(DRAG_ENABLED_PROPERTY, Boolean.valueOf(oldDragEnabled), Boolean.valueOf(newDragEnabled));
		}
	}

	/** Whether the component has dropping enabled. */
	private boolean dropEnabled = false;

	@Override
	public boolean isDropEnabled() {
		return dropEnabled;
	}

	@Override
	public void setDropEnabled(final boolean newDropEnabled) {
		if(dropEnabled != newDropEnabled) { //if the value is really changing
			final boolean oldDropEnabled = dropEnabled; //get the current value
			dropEnabled = newDropEnabled; //update the value
			firePropertyChange(DRAG_ENABLED_PROPERTY, Boolean.valueOf(oldDropEnabled), Boolean.valueOf(newDropEnabled));
		}
	}

	/** Whether flyovers are enabled for this component. */
	private boolean flyoverEnabled = false;

	@Override
	public boolean isFlyoverEnabled() {
		return flyoverEnabled;
	}

	/** A reference to the default flyover strategy, if we're using one. */
	private FlyoverStrategy<?> defaultFlyoverStrategy = null;

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation adds or removes a default flyover strategy if one is not already installed. This is a bound property of type {@link Boolean}.
	 * </p>
	 */
	@Override
	public void setFlyoverEnabled(final boolean newFlyoverEnabled) {
		if(flyoverEnabled != newFlyoverEnabled) { //if the value is really changing
			final boolean oldFlyoverEnabled = flyoverEnabled; //get the current value
			flyoverEnabled = newFlyoverEnabled; //update the value
			if(newFlyoverEnabled) { //if flyovers are now enabled
				if(getFlyoverStrategy() == null) { //if no flyover strategy is installed
					defaultFlyoverStrategy = new DefaultFlyoverStrategy<Component>(this); //create a default flyover strategy
					setFlyoverStrategy(defaultFlyoverStrategy); //start using our default flyover strategy
				}
			} else { //if flyovers are now disabled
				if(defaultFlyoverStrategy != null) { //if we had created a default flyover strategy
					if(getFlyoverStrategy() == defaultFlyoverStrategy) { //if we were using the default flyover strategy
						setFlyoverStrategy(null); //remove our default flyover strategy
					}
					defaultFlyoverStrategy = null; //release the default flyover strategy
				}
			}
			firePropertyChange(FLYOVER_ENABLED_PROPERTY, Boolean.valueOf(oldFlyoverEnabled), Boolean.valueOf(newFlyoverEnabled));
		}
	}

	/** The installed flyover strategy, or <code>null</code> if there is no flyover strategy installed. */
	private FlyoverStrategy<?> flyoverStrategy = null;

	@Override
	public FlyoverStrategy<?> getFlyoverStrategy() {
		return flyoverStrategy;
	}

	@Override
	public void setFlyoverStrategy(final FlyoverStrategy<?> newFlyoverStrategy) {
		if(flyoverStrategy != newFlyoverStrategy) { //if the value is really changing
			final FlyoverStrategy<?> oldFlyoverStrategy = flyoverStrategy; //get the old value
			if(oldFlyoverStrategy != null) { //if there was a flyover strategy
				removeMouseListener(oldFlyoverStrategy); //let the old flyover strategy stop listening for mouse events
				if(oldFlyoverStrategy == defaultFlyoverStrategy) { //if the default flyover strategy was just uninstalled
					defaultFlyoverStrategy = null; //we don't need to keep around the default flyover strategy
				}
			}
			flyoverStrategy = newFlyoverStrategy; //actually change the value
			if(newFlyoverStrategy != null) { //if there is now a new flyover strategy
				addMouseListener(newFlyoverStrategy); //let the new flyover strategy start listening for mouse events
			}
			firePropertyChange(FLYOVER_STRATEGY_PROPERTY, oldFlyoverStrategy, newFlyoverStrategy); //indicate that the value changed
		}
	}

	/** Whether a theme has been applied to this component. */
	private boolean themeApplied = false;

	@Override
	public boolean isThemeApplied() {
		return themeApplied;
	}

	@Override
	public void setThemeApplied(final boolean newThemeApplied) {
		if(themeApplied != newThemeApplied) { //if the value is really changing
			final boolean oldThemeApplied = themeApplied; //get the current value
			themeApplied = newThemeApplied; //update the value
			firePropertyChange(THEME_APPLIED_PROPERTY, Boolean.valueOf(oldThemeApplied), Boolean.valueOf(newThemeApplied));
		}
	}

	/** The list of installed export strategies, from most recently added to earliest added. */
	private List<ExportStrategy<?>> exportStrategyList = new CopyOnWriteArrayList<ExportStrategy<?>>();

	@Override
	public void addExportStrategy(final ExportStrategy<?> exportStrategy) {
		exportStrategyList.add(0, exportStrategy);
	} //add the export strategy to the beginning of the list

	@Override
	public void removeExportStrategy(final ExportStrategy<?> exportStrategy) {
		exportStrategyList.remove(exportStrategy);
	} //remove the export strategy from the list

	@Override
	public Transferable<?> exportTransfer() {
		for(final ExportStrategy<?> exportStrategy : exportStrategyList) { //for each export strategy
			final Transferable<?> transferable = ((ExportStrategy<Component>)exportStrategy).exportTransfer(this); //ask this export strategy to transfer data
			if(transferable != null) { //if this export succeeded
				return transferable; //return this transferable data
			}
		}
		return null; //indicate that no data could be exported
	}

	/** The list of installed import strategies, from most recently added to earliest added. */
	private List<ImportStrategy<?>> importStrategyList = new CopyOnWriteArrayList<ImportStrategy<?>>();

	@Override
	public void addImportStrategy(final ImportStrategy<?> importStrategy) {
		importStrategyList.add(0, importStrategy);
	} //add the import strategy to the beginning of the list

	@Override
	public void removeImportStrategy(final ImportStrategy<?> importStrategy) {
		importStrategyList.remove(importStrategy);
	} //remove the import strategy from the list

	@Override
	public boolean importTransfer(final Transferable<?> transferable) {
		for(final ImportStrategy<?> importStrategy : importStrategyList) { //for each importstrategy
			if(((ImportStrategy<Component>)importStrategy).canImportTransfer(this, transferable)) { //if this import strategy can import the data
				if(((ImportStrategy<Component>)importStrategy).importTransfer(this, transferable)) { //import the data; if we are successful
					return true; //stop trying to import data, and indicate we were successful
				}
			}
		}
		return false; //indicate that no data could be imported
	}

	/**
	 * Default constructor.
	 * @throws IllegalStateException if no controller is registered for this component type.
	 * @throws IllegalStateException if no view is registered for this component type.
	 */
	public AbstractComponent() {
		this(new DefaultInfoModel()); //construct the component with a default info model
	}

	/**
	 * Info model constructor.
	 * @param infoModel The component info model.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 * @throws IllegalStateException if no depictor is registered for this component type.
	 */
	@SuppressWarnings("unchecked")
	public AbstractComponent(final InfoModel infoModel) {
		final GuiseSession session = getSession(); //get the Guise session
		final Platform platform = session.getPlatform(); //get the Guise platform
		this.depictID = platform.generateDepictID(); //ask the platform to generate a new depict ID
		this.depictor = (Depictor<? extends Component>)platform.getDepictor(this); //ask the platform for a depictor for the object, and assume that it's a component depictor
		if(this.depictor == null) { //if no depictor is registered for this object
			throw new IllegalStateException("No depictor registered for class " + getClass());
		}
		//TODO del; lazily installed		notifyDepictorInstalled(depictor);	//tell the the depictor it has been installed
		platform.registerDepictedObject(this); //register this depicted object with the platform
		this.infoModel = checkInstance(infoModel, "Info model cannot be null."); //save the info model
		this.infoModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the info model
		this.infoModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the info model
	}

	/**
	 * Notifies a depictor that it has been installed in this object.
	 * @param <O> The type of depicted object expected by the depictor.
	 * @param depictor The depictor that has been installed.
	 */
	@SuppressWarnings("unchecked")
	//at this point we have to assume that the correct type of depictor has been registered for this object
	private <O extends DepictedObject> void notifyDepictorInstalled(final Depictor<O> depictor) {
		depictor.installed((O)this); //tell the depictor it has been installed		
	}

	/** Whether this component has been initialized. */
	private boolean initialized = false;

	@Override
	public void initialize() {
		if(initialized) { //if this method has already been called
			throw new IllegalStateException("Component can only be initialized once.");
		}
		initialized = true; //show that this component has been initialized
	}

	/**
	 * Updates the condition of the component based upon the state. This method is a convenience method for complex components that would like to perform
	 * wholesale updates any prototypes, enabled/disabled status, proxied actions, etc. This version does nothing.
	 */
	protected void update() {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version clears all notifications. This version calls {@link #updateValid()}.
	 * </p>
	 */
	@Override
	public boolean validate() {
		setNotification(null); //clear any notification
		updateValid(); //manually update the current component validity
		return isValid(); //return the current valid state
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version fires all events that are not consumed.
	 * </p>
	 */
	@Override
	public void dispatchInputEvent(final InputEvent inputEvent) {
		//Log.trace("in component", this, "ready to do default dispatching of input event", inputEvent);		
		if(!inputEvent.isConsumed()) { //if the input has not been consumed
			//Log.trace("event is not consumed; ready to fire it to listeners");
			fireInputEvent(inputEvent); //fire the event to any listeners
			//Log.trace("firing finised");
			if(!inputEvent.isConsumed()) { //if the input has still not been consumed
				//Log.trace("event is not still not consumed; checking input strategy");
				final InputStrategy inputStrategy = getInputStrategy(); //get our input strategy, if any
				if(inputStrategy != null) { //if we have an input strategy
					//Log.trace("got input strategy");
					final Input input = inputEvent.getInput(); //get the event's input, if any
					if(input != null) { //if the event has input
						//Log.trace("got input for this event:", input);
						if(inputStrategy.input(input)) { //send the input to the input strategy; if the input was consumed
							//Log.trace("our input strategy consumed the input");
							inputEvent.consume(); //mark the event as consumed
						}
					}
				}
			}
		}
	}

	@Override
	public void fireInputEvent(final InputEvent inputEvent) {
		if(inputEvent instanceof TargetedEvent && !this.equals(((TargetedEvent)inputEvent).getTarget())) { //if this is a targeted event that is not bound for this component TODO document, if it works; later allow for registration of pre/target/post bubble listening
			return; //don't fire the event
		}
		if(inputEvent instanceof CommandEvent) { //if this is a command event
			if(hasCommandListeners()) { //if there are command listeners registered
				final CommandEvent commandEvent = new CommandEvent(this, (CommandEvent)inputEvent); //create a new command event copy indicating that this component is the source
				for(final CommandListener commandListener : getCommandListeners()) { //for each command listener
					if(commandEvent.isConsumed()) { //if the event copy has been consumed
						inputEvent.consume(); //consume the original event
						return; //stop further processing
					}
					commandListener.commanded(commandEvent); //fire the command event
				}
			}
		} else if(inputEvent instanceof KeyboardEvent) { //if this is a keyboard event
			if(hasKeyListeners()) { //if there are key listeners registered
				if(inputEvent instanceof KeyPressEvent) { //if this is a key press event
					final KeyPressEvent keyPressEvent = new KeyPressEvent(this, (KeyPressEvent)inputEvent); //create a new key event copy indicating that this component is the source
					for(final KeyboardListener keyListener : getKeyListeners()) { //for each key listener
						if(keyPressEvent.isConsumed()) { //if the event copy has been consumed
							inputEvent.consume(); //consume the original event
							return; //stop further processing
						}
						keyListener.keyPressed(keyPressEvent); //fire the key event
					}
				}
				if(inputEvent instanceof KeyReleaseEvent) { //if this is a key release event
					final KeyReleaseEvent keyReleaseEvent = new KeyReleaseEvent(this, (KeyReleaseEvent)inputEvent); //create a new key event copy indicating that this component is the source
					for(final KeyboardListener keyListener : getKeyListeners()) { //for each key listener
						if(keyReleaseEvent.isConsumed()) { //if the event copy has been consumed
							inputEvent.consume(); //consume the original event
							return; //stop further processing
						}
						keyListener.keyReleased(keyReleaseEvent); //fire the key event
					}
				}
			}
		} else if(inputEvent instanceof MouseEvent) { //if this is a mouse event
			if(hasMouseListeners()) { //if there are mouse listeners registered
				if(inputEvent instanceof MouseClickEvent) { //if this is a mouse click event
					final MouseClickEvent mouseClickEvent = new MouseClickEvent(this, (MouseClickEvent)inputEvent); //create a new mouse event copy indicating that this component is the source
					for(final MouseListener mouseListener : getMouseListeners()) { //for each mouse listener
						if(mouseClickEvent.isConsumed()) { //if the event copy has been consumed
							inputEvent.consume(); //consume the original event
							return; //stop further processing
						}
						mouseListener.mouseClicked(mouseClickEvent); //fire the mouse event
					}
				} else if(inputEvent instanceof MouseEnterEvent) { //if this is a mouse enter event
					final MouseEnterEvent mouseEnterEvent = new MouseEnterEvent(this, (MouseEnterEvent)inputEvent); //create a new mouse event copy indicating that this component is the source
					for(final MouseListener mouseListener : getMouseListeners()) { //for each mouse listener
						if(mouseEnterEvent.isConsumed()) { //if the event copy has been consumed
							inputEvent.consume(); //consume the original event
							return; //stop further processing
						}
						mouseListener.mouseEntered(mouseEnterEvent); //fire the mouse event
					}
				} else if(inputEvent instanceof MouseExitEvent) { //if this is a mouse exit event
					final MouseExitEvent mouseExitEvent = new MouseExitEvent(this, (MouseExitEvent)inputEvent); //create a new mouse event copy indicating that this component is the source
					for(final MouseListener mouseListener : getMouseListeners()) { //for each mouse listener
						if(mouseExitEvent.isConsumed()) { //if the event copy has been consumed
							inputEvent.consume(); //consume the original event
							return; //stop further processing
						}
						mouseListener.mouseExited(mouseExitEvent); //fire the mouse event
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version delegates to the parent version, if there is a parent component; otherwise, the session theme is returned.
	 * </p>
	 */
	@Override
	public Theme getTheme() throws IOException {
		final CompositeComponent parent = getParent(); //get this component's parent, if any
		return parent != null ? parent.getTheme() : getSession().getTheme(); //if there is no parent, return the session theme
	}

	@Override
	public void resetTheme() {
		setThemeApplied(false); //indicate that no theme has been applied
	}

	@Override
	public void updateTheme() throws IOException {
		if(!isThemeApplied()) { //if a theme has not been applied to this component
			applyTheme(); //apply the theme to this component
		}
	}

	@Override
	public void applyTheme() throws IOException {
		if(getSession().getApplication().isThemed()) { //if the application applies themes
			applyTheme(getTheme()); //get the theme and apply it
			setThemeApplied(true); //indicate that the theme was successfully applied
		}
	}

	@Override
	public void applyTheme(final Theme theme) {
		theme.apply(this); //apply the theme to this component
	}

	@Override
	public void loadPreferences(final boolean includeDescendants) throws IOException {
		final Iterator<String> preferencePropertyIterator = getPreferenceProperties().iterator(); //get an iterator to all preferences properties
		if(preferencePropertyIterator.hasNext()) { //if there are preference properties
			final URFResource preferences = getSession().getPreferences(getClass()); //get existing preferences for this class
			//TODO del Log.traceStack("ready to load preferences; view:", ((ResourceChildrenPanel)this).getView(), "thumbnail size:", ((ResourceChildrenPanel)this).getThumbnailSize(), "preferences", RDFUtilities.toString(preferences));
			final PLOOPURFProcessor ploopProcessor = new PLOOPURFProcessor(); //create a new PLOOP processor for retrieving the properties
			do { //for each property
				final String propertyName = preferencePropertyIterator.next(); //get the name of the next property
				try {
					ploopProcessor.setObjectProperty(this, preferences, propertyName); //retrieve this property from the preferences
				} catch(final DataException dataException) { //if there was a data error
					throw new IOException(dataException);
				} catch(final InvocationTargetException invocationTargetException) { //if there was an error accessing a Java class
					throw new IOException(invocationTargetException);
				}
			} while(preferencePropertyIterator.hasNext()); //keep saving properties while there are more preference properties
			//Log.trace("loaded preferences; view:", ((ResourceChildrenPanel)this).getView(), "thumbnail size:", ((ResourceChildrenPanel)this).getThumbnailSize());
		}
	}

	@Override
	public void savePreferences(final boolean includeDescendants) throws IOException {
		//Log.trace("ready to save preferences for component", this, "have preference properties", preferenceProperties);
		final Iterator<String> preferencePropertyIterator = getPreferenceProperties().iterator(); //get an iterator to all preferences properties
		if(preferencePropertyIterator.hasNext()) { //if there are preference properties
			final GuiseSession session = getSession(); //get the current session
			final Class<?> componentClass = getClass(); //get this component's class
			final URFResource preferences = session.getPreferences(componentClass); //get existing preferences for this class
			final PLOOPURFGenerator ploopURFGenerator = new PLOOPURFGenerator(); //create a new PLOOP URF generator for storing the properties
			do { //for each property
				final String propertyName = preferencePropertyIterator.next(); //get the name of the next property
				try {
					ploopURFGenerator.setURFResourceProperty(preferences, this, propertyName); //store this property in the preferences
				} catch(final InvocationTargetException invocationTargetException) { //if there was an error accessing this resource
					throw new IOException(invocationTargetException);
				}
			} while(preferencePropertyIterator.hasNext()); //keep saving properties while there are more preference properties
			//Log.trace("ready to save preferences; view:", ((ResourceChildrenPanel)this).getView(), "thumbnail size:", ((ResourceChildrenPanel)this).getThumbnailSize(), "preferences", URF.toString(preferences));
			session.setPreferences(componentClass, preferences); //set the new preferences
		}
	}

	@Override
	public void addCommandListener(final CommandListener commandListener) {
		getEventListenerManager().add(CommandListener.class, commandListener); //add the listener
	}

	@Override
	public void removeCommandListener(final CommandListener commandListener) {
		getEventListenerManager().remove(CommandListener.class, commandListener); //remove the listener
	}

	@Override
	public boolean hasCommandListeners() {
		return getEventListenerManager().hasListeners(CommandListener.class); //return whether there are command listeners registered
	}

	/** @return all registered command listeners. */
	protected Iterable<CommandListener> getCommandListeners() {
		return getEventListenerManager().getListeners(CommandListener.class); //return the registered listeners
	}

	@Override
	public void addKeyListener(final KeyboardListener keyListener) {
		getEventListenerManager().add(KeyboardListener.class, keyListener); //add the listener
	}

	@Override
	public void removeKeyListener(final KeyboardListener keyListener) {
		getEventListenerManager().remove(KeyboardListener.class, keyListener); //remove the listener
	}

	@Override
	public boolean hasKeyListeners() {
		return getEventListenerManager().hasListeners(KeyboardListener.class); //return whether there are key listeners registered
	}

	/** @return all registered key listeners. */
	protected Iterable<KeyboardListener> getKeyListeners() {
		return getEventListenerManager().getListeners(KeyboardListener.class); //return the registered listeners
	}

	@Override
	public void addMouseListener(final MouseListener mouseListener) {
		getEventListenerManager().add(MouseListener.class, mouseListener); //add the listener
	}

	@Override
	public void removeMouseListener(final MouseListener mouseListener) {
		getEventListenerManager().remove(MouseListener.class, mouseListener); //remove the listener
	}

	@Override
	public boolean hasMouseListeners() {
		return getEventListenerManager().hasListeners(MouseListener.class); //return whether there are mouse listeners registered
	}

	/** @return all registered mouse listeners. */
	protected Iterable<MouseListener> getMouseListeners() {
		return getEventListenerManager().getListeners(MouseListener.class); //return the registered listeners
	}

	/**
	 * Fires a mouse entered event to all registered mouse listeners.
	 * @param componentBounds The absolute bounds of the component.
	 * @param viewportBounds The absolute bounds of the viewport.
	 * @param mousePosition The position of the mouse relative to the viewport.
	 * @throws NullPointerException if one or more of the arguments are <code>null</code>.
	 * @see MouseListener
	 * @see MouseEvent
	 */
	/*TODO del if not needed
		public void fireMouseEntered(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition)
		{
			if(hasMouseListeners()) {	//if there are mouse listeners registered
				final MouseEnterEvent mouseEnterEvent=new MouseEnterEvent(getThis(), componentBounds, viewportBounds, mousePosition);	//create a new mouse event
				for(final MouseListener mouseListener:getMouseListeners()) {	//for each mouse listener
					mouseListener.mouseEntered(mouseEnterEvent);	//fire the mouse entered event
				}
			}
		}
	*/

	/**
	 * Fires a mouse exited event to all registered mouse listeners.
	 * @param componentBounds The absolute bounds of the component.
	 * @param viewportBounds The absolute bounds of the viewport.
	 * @param mousePosition The position of the mouse relative to the viewport.
	 * @throws NullPointerException if one or more of the arguments are <code>null</code>.
	 * @see MouseListener
	 * @see MouseEvent
	 */
	/*TODO del if not needed
		public void fireMouseExited(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition)
		{
			if(hasMouseListeners()) {	//if there are mouse listeners registered
				final MouseExitEvent mouseExitEvent=new MouseExitEvent(getThis(), componentBounds, viewportBounds, mousePosition);	//create a new mouse event
				for(final MouseListener mouseListener:getMouseListeners()) {	//for each mouse listener
					mouseListener.mouseExited(mouseExitEvent);	//fire the mouse entered event
				}
			}
		}
	*/

	/**
	 * Determines the root parent of the given component.
	 * @param component The component for which the root should be found.
	 * @return The root component (the component or ancestor which has no parent).
	 */
	public static Component getRootComponent(Component component) {
		Component parent; //we'll keep track of the parent at each level when finding the root component
		while((parent = component.getParent()) != null) { //get the parent; while there is a parent
			component = parent; //move up the chain
		}
		return component; //return whatever component we ended up with without a parent
	}

	/**
	 * Determines whether a component has a given component as its ancestor, not including the component itself.
	 * @param component The component for which the potential ancestor should be checked.
	 * @param ancestor The component to check as an ancestor.
	 * @return <code>true</code> if the given ancestor component is its parent or one of its parent's parents.
	 * @throws NullPointerException if the given component and/or ancestor is <code>null</code>.
	 */
	public static boolean hasAncestor(Component component, final CompositeComponent ancestor) {
		checkInstance(ancestor, "Ancestor cannot be null.");
		while((component = component.getParent()) != null) { //get the parent; while we're not out of parents
			if(component == ancestor) { //if this is the ancestor
				return true; //indicate that the component has the ancestor
			}
		}
		;
		return false; //indicate that we ran out of ancestors before we found a matching ancestor
	}

	/**
	 * Retrieves a component with the given ID. This method checks the given component and all descendant components.
	 * @param component The component that should be checked, along with its descendants, for the given ID.
	 * @param id The ID of the component.
	 * @return The component with the given ID, or <code>null</code> if this component and all descendant components do not have the given ID.
	 */
	public static Component getComponentByID(final Component component, final long id) {
		if(component.getDepictID() == id) { //if this component has the correct ID
			return component; //return this component
		} else if(component instanceof CompositeComponent) { //if this component doesn't have the correct ID, but it is a composite component
			for(final Component childComponent : ((CompositeComponent)component).getChildComponents()) { //for each child component
				final Component matchingComponent = getComponentByID(childComponent, id); //see if we can find a component in this tree
				if(matchingComponent != null) { //if we found a matching component
					return matchingComponent; //return the matching component
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves a component with the given name. This method checks the given component and all descendant components.
	 * @param component The component that should be checked, along with its descendants, for the given name.
	 * @param name The name of the component.
	 * @return The first component with the given name, or <code>null</code> if this component and all descendant components do not have the given name.
	 */
	public static Component getComponentByName(final Component component, final String name) {
		if(name.equals(component.getName())) { //if this component has the correct name
			return component; //return this component
		} else if(component instanceof CompositeComponent) { //if this component doesn't have the correct name, but it is a composite component
			for(final Component childComponent : ((CompositeComponent)component).getChildComponents()) { //for each child component
				final Component matchingComponent = getComponentByName(childComponent, name); //see if we can find a component in this tree
				if(matchingComponent != null) { //if we found a matching component
					return matchingComponent; //return the matching component
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves all components that have views needing updated. This method checks the given component and all descendant components. If a given component is
	 * dirty, its child views will not be checked.
	 * @param component The component that should be checked, along with its descendants, for out-of-date views.
	 * @return The components with views needing to be updated.
	 */
	public static List<Component> getDirtyComponents(final Component component) {
		return getDirtyComponents(component, new ArrayList<Component>()); //gather dirty components and put them in a list
	}

	/**
	 * Retrieves all components that have views needing updated. This method checks the given component and all descendant components. If a given component is
	 * dirty, its child views will not be checked.
	 * @param component The component that should be checked, along with its descendants, for out-of-date views.
	 * @param dirtyComponents The list that will be updated with more dirty components if any are found.
	 * @return The components with views needing to be updated.
	 */
	public static List<Component> getDirtyComponents(final Component component, final List<Component> dirtyComponents) {
		if(!component.getDepictor().isDepicted()) { //if this component's view isn't updated
			dirtyComponents.add(component); //add this component to the list
		} else if(component instanceof CompositeComponent) { //if the component's view is updated, check its children if it has any
			for(final Component childComponent : ((CompositeComponent)component).getChildComponents()) { //for each child component
				getDirtyComponents(childComponent, dirtyComponents); //gather dirty components in this child hierarchy
			}
		}
		return dirtyComponents;
	}

	/**
	 * Changes the updated status of the views of an entire component descendant hierarchy.
	 * @param component The component from which, along with its descendants, notifications should be retrieved.
	 * @param newUpdated Whether the views of this component and all child components are up to date.
	 */
	public static void setDepicted(final Component component, final boolean newUpdated) {
		component.getDepictor().setDepicted(newUpdated); //change the updated status of this component's view
		if(component instanceof CompositeComponent) { //if the component is a composite component
			for(final Component childComponent : ((CompositeComponent)component).getChildComponents()) { //for each child component
				setDepicted(childComponent, newUpdated); //changed the updated status for this child's hierarchy
			}
		}
	}

	/**
	 * Retrieves the the notifications of all components in a hierarchy. This method checks the given component and all descendant components. Children that are
	 * not visible and/or not displayed are not taken into account.
	 * @param component The component from which, along with its descendants, notifications should be retrieved.
	 * @return The notifications of all components in the hierarchy.
	 */
	public static List<Notification> getNotifications(final Component component) {
		return getNotifications(component, new ArrayList<Notification>()); //gather notifications and put them in a list
	}

	/**
	 * Retrieves the the notifications of all components in a hierarchy. This method checks the given component and all descendant components. Children that are
	 * not visible and/or not displayed are not taken into account.
	 * @param component The component from which, along with its descendants, notifications should be retrieved.
	 * @param notifications The list that will be updated with more dirty components if any are found.
	 * @return The notifications of all components in the hierarchy.
	 */
	protected static List<Notification> getNotifications(final Component component, final List<Notification> notifications) {
		final Notification notification = component.getNotification(); //get the component's notification, if any
		if(notification != null) { //if a notification is available
			notifications.add(notification); //add this notification to the list
		}
		if(component instanceof CompositeComponent) { //if the component is a composite component, check its children
			for(final Component childComponent : ((CompositeComponent)component).getChildComponents()) { //for each child component
				if(childComponent.isDisplayed() && childComponent.isVisible()) { //if this child component is displayed and visible
					getNotifications(childComponent, notifications); //gather notifications from this child hierarchy
				}
			}
		}
		return notifications;
	}

	@Override
	public void addNotificationListener(final NotificationListener notificationListener) {
		getEventListenerManager().add(NotificationListener.class, notificationListener); //add the listener
	}

	@Override
	public void removeNotificationListener(final NotificationListener notificationListener) {
		getEventListenerManager().remove(NotificationListener.class, notificationListener); //remove the listener
	}

	/**
	 * Fires an event to all registered notification listeners with the new notification information. Parents are expected to refire the notification event up the
	 * hierarchy.
	 * @param notification The notification to send to the notification listeners.
	 * @throws NullPointerException if the given notification is <code>null</code>.
	 * @see NotificationListener
	 * @see NotificationEvent
	 */
	protected void fireNotified(final Notification notification) {
		fireNotified(new NotificationEvent(this, notification)); //create and fire a new notification event
	}

	/**
	 * Fires an event to all registered notification listeners with the new notification information. Parents are expected to refire copies of the notification
	 * event up the hierarchy, keeping the original target.
	 * @param notificationEvent The notification event to send to the notification listeners.
	 * @throws NullPointerException if the given notification event is <code>null</code>.
	 * @see NotificationListener
	 */
	protected void fireNotified(final NotificationEvent notificationEvent) {
		for(final NotificationListener notificationListener : getEventListenerManager().getListeners(NotificationListener.class)) { //for each notification listener
			notificationListener.notified(notificationEvent); //dispatch the notification event to the listener
		}
	}

	@Override
	public int hashCode() {
		return Longs.hashCode(getDepictID()); //return the hash code of the ID
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns whether the object is a component with the same ID.
	 * </p>
	 */
	@Override
	public boolean equals(final Object object) {
		return object instanceof Component && getDepictID() == ((Component)object).getDepictID(); //see if the other object is a component with the same ID
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder(super.toString()); //create a string builder for constructing the string
		stringBuilder.append(' ').append('[').append(getDepictID()).append(']'); //append the ID
		return stringBuilder.toString(); //return the string builder
	}

	@Override
	public void notify(final Notification notification) {
		setNotification(notification); //store the notification, firing notification events
		getSession().notify(notification); //notify the user directly
	}

	/**
	 * An abstract implementation of a strategy for showing and hiding flyovers in response to mouse events.
	 * @param <S> The type of component for which this object is to control flyovers.
	 * @author Garret Wilson
	 */
	public static abstract class AbstractFlyoverStrategy<S extends Component> extends MouseAdapter implements FlyoverStrategy<S> {

		/** The component for which this object will control flyovers. */
		private final S component;

		/** @return The component for which this object will control flyovers. */
		public S getComponent() {
			return component;
		}

		/** The array of flyover extents. */
		private Extent[] extents = fill(new Extent[Flow.values().length], null);

		/**
		 * Returns the extent of the indicated flow.
		 * @param flow The flow for which an extent should be returned.
		 * @return The extent of the given flow.
		 */
		public Extent getExtent(final Flow flow) {
			return extents[flow.ordinal()];
		}

		@Override
		public Extent getLineExtent() {
			return getExtent(Flow.LINE);
		}

		@Override
		public Extent getPageExtent() {
			return getExtent(Flow.PAGE);
		}

		/**
		 * Sets the extent of a given flow. The extent of each flow represents a bound property.
		 * @param flow The flow for which the extent should be set.
		 * @param newExtent The new requested extent of the flyover, or <code>null</code> there is no extent preference.
		 * @throws NullPointerException if the given flow is <code>null</code>.
		 */
		public void setExtent(final Flow flow, final Extent newExtent) {
			final int flowOrdinal = checkInstance(flow, "Flow cannot be null").ordinal(); //get the ordinal of the flow
			final Extent oldExtent = extents[flowOrdinal]; //get the old value
			if(!Objects.equals(oldExtent, newExtent)) { //if the value is really changing
				extents[flowOrdinal] = newExtent; //actually change the value
			}
		}

		@Override
		public void setLineExtent(final Extent newExtent) {
			setExtent(Flow.LINE, newExtent);
		}

		@Override
		public void setPageExtent(final Extent newExtent) {
			setExtent(Flow.PAGE, newExtent);
		}

		/** The style identifier of the flyover, or <code>null</code> if there is no style ID. */
		private String styleID = null;

		@Override
		public String getStyleID() {
			return styleID;
		}

		@Override
		public void setStyleID(final String newStyleID) {
			if(Objects.equals(styleID, newStyleID)) { //if the value is really changing
				final String oldStyleID = styleID; //get the current value
				styleID = newStyleID; //update the value
			}
		}

		/** The bearing of the tether in relation to the frame. */
		private BigDecimal tetherBearing = CompassPoint.NORTHWEST_BY_WEST.getBearing();

		/** @return The bearing of the tether in relation to the frame. */
		public BigDecimal getTetherBearing() {
			return tetherBearing;
		}

		/**
		 * Sets the bearing of the tether in relation to the frame.
		 * @param newTetherBearing The new bearing of the tether in relation to the frame.
		 * @throws NullPointerException if the given bearing is <code>null</code>.
		 * @throws IllegalArgumentException if the given bearing is greater than 360.
		 */
		public void setTetherBearing(final BigDecimal newTetherBearing) {
			if(!tetherBearing.equals(checkInstance(newTetherBearing, "Tether bearing cannot be null."))) { //if the value is really changing
				final BigDecimal oldTetherBearing = tetherBearing; //get the current value
				tetherBearing = CompassPoint.checkBearing(newTetherBearing); //update the value
			}
		}

		/** The effect used for opening the flyover, or <code>null</code> if there is no open effect. */
		private Effect openEffect = null;

		@Override
		public Effect getOpenEffect() {
			return openEffect;
		}

		@Override
		public void setOpenEffect(final Effect newOpenEffect) {
			if(openEffect != newOpenEffect) { //if the value is really changing
				final Effect oldOpenEffect = openEffect; //get the old value
				openEffect = newOpenEffect; //actually change the value
				//TODO fix					firePropertyChange(Frame.OPEN_EFFECT_PROPERTY, oldOpenEffect, newOpenEffect);	//indicate that the value changed
			}
		}

		/**
		 * Component constructor.
		 * @param component The component for which this object will control flyovers.
		 * @throws NullPointerException if the given component is <code>null</code>.
		 */
		public AbstractFlyoverStrategy(final S component) {
			this.component = checkInstance(component, "Component cannot be null.");
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation opens the flyover.
		 * </p>
		 * @see #openFlyover()
		 */
		@Override
		public void mouseEntered(final MouseEnterEvent mouseEvent) {
			/*TODO del when works
			Log.trace("source bounds:", mouseEvent.getSourceBounds());
						final Dimensions sourceSize=mouseEvent.getSourceBounds().getSize();	//get the size of the source
						final Point sourceCenter=mouseEvent.getSourceBounds().getPosition().translate(sourceSize.getWidth().getValue()/2, sourceSize.getHeight().getValue()/2);	//determine the center of the source
			Log.trace("source center:", sourceCenter);
			Log.trace("viewport bounds:", mouseEvent.getViewportBounds());
						final Point viewportPosition=mouseEvent.getViewportBounds().getPosition();	//get the position of the viewport
						final Dimensions viewportSize=mouseEvent.getViewportBounds().getSize();	//get the size of the viewport
						final Point viewportSourceCenter=sourceCenter.translate(-viewportPosition.getX().getValue(), -viewportPosition.getY().getValue());	//translate the source center into the viewport
			Log.trace("viewport source center:", viewportSourceCenter);
			*/
			final Rectangle viewportBounds = mouseEvent.getViewportBounds(); //get the bounds of the viewport
			//TODO del Log.trace("viewport bounds:", viewportBounds);
			//TODO del Log.trace("source bounds:", mouseEvent.getSourceBounds());
			final Dimensions viewportSize = viewportBounds.getSize(); //get the size of the viewport
			final Point mousePosition = mouseEvent.getMousePosition(); //get the mouse position
			//TODO del Log.trace("mouse position:", mousePosition);
			//get the mouse position inside the traditional coordinate space with the origin at the center of the viewport
			final Point traditionalMousePosition = new Point(mousePosition.getX().getValue() - (viewportSize.getWidth().getValue() / 2),
					-(mousePosition.getY().getValue() - (viewportSize.getHeight().getValue() / 2)));
			//TODO del Log.trace("traditional mouse position:", traditionalMousePosition);
			//get the angle of the point from the y axis in the range of (-PI, PI)
			final double atan2 = Math.atan2(traditionalMousePosition.getX().getValue(), traditionalMousePosition.getY().getValue());
			final double normalizedAtan2 = atan2 >= 0 ? atan2 : (Math.PI * 2) + atan2; //normalize the angle to the range (0, 2PI) 
			final BigDecimal tetherBearing = CompassPoint.MAX_BEARING.multiply(new BigDecimal(normalizedAtan2 / (Math.PI * 2))); //get the fraction of the range and multiply by 360
			setTetherBearing(tetherBearing); //set the tether bearing to use for flyovers

			openFlyover(); //open the flyover
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation closes any open flyover.
		 * </p>
		 * @see #closeFlyover()
		 */
		@Override
		public void mouseExited(final MouseExitEvent mouseEvent) {
			closeFlyover(); //close the flyover if it is open
		}
	}

	/**
	 * An abstract flyover strategy that uses flyover frames.
	 * @param <S> The type of component for which this object is to control flyovers.
	 * @author Garret Wilson
	 */
	public static abstract class AbstractFlyoverFrameStrategy<S extends Component> extends AbstractFlyoverStrategy<S> {

		/** The frame used for displaying flyovers. */
		private FlyoverFrame flyoverFrame = null;

		/**
		 * Component constructor.
		 * @param component The component for which this object will control flyovers.
		 * @throws NullPointerException if the given component is <code>null</code>.
		 */
		public AbstractFlyoverFrameStrategy(final S component) {
			super(component); //construct the parent class
			//TODO del			setOpenEffect(new OpacityFadeEffect(component.getSession(), 500));	//create a default open effect TODO use a constant
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation creates a flyover frame if necessary and then opens the frame.
		 * </p>
		 * @see #createFrame()
		 */
		@Override
		public void openFlyover() {
			if(flyoverFrame == null) { //if no flyover frame has been created
				//TODO del Log.trace("no frame; created");
				flyoverFrame = createFrame(); //create a new frame
				final String styleID = getStyleID(); //get the styld ID
				if(styleID != null) { //if there is a style ID
					flyoverFrame.setStyleID(styleID); //set the style ID of the flyover
				}
				final Extent lineExtent = getLineExtent(); //get the requested width
				if(lineExtent != null) { //if there is a requested width
					flyoverFrame.setLineExtent(lineExtent); //set the flyover width
				}
				final Extent pageExtent = getPageExtent(); //get the requested height
				if(pageExtent != null) { //if there is a requested height
					flyoverFrame.setPageExtent(pageExtent); //set the flyover height
				}
				flyoverFrame.setTetherBearing(getTetherBearing()); //set the bearing of the tether
				//TODO fix				frame.getModel().setLabel("Flyover");
				flyoverFrame.setOpenEffect(getOpenEffect()); //set the effect for opening, if any
				flyoverFrame.open();
			}
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation closes any open flyover frame.
		 * </p>
		 */
		@Override
		public void closeFlyover() {
			if(flyoverFrame != null) { //if there is a flyover frame
				flyoverFrame.close(); //close the frame
				flyoverFrame = null; //release our reference to the frame
			}
		}

		/** @return A new frame for displaying flyover information. */
		protected abstract FlyoverFrame createFrame();
	}

	/**
	 * The default strategy for showing and hiding flyovers in response to mouse events. //TODO del This implementation uses flyover frames to represent flyovers.
	 * //TODO del This implementation defaults to an opacity fade effect for opening with a 500 millisecond delay.
	 * @param <S> The type of component for which this object is to control flyovers.
	 * @author Garret Wilson
	 */
	public static class DefaultFlyoverStrategy<S extends Component> extends AbstractFlyoverFrameStrategy<S> {

		/**
		 * Component constructor.
		 * @param component The component for which this object will control flyovers.
		 * @throws NullPointerException if the given component is <code>null</code>.
		 */
		public DefaultFlyoverStrategy(final S component) {
			super(component); //construct the parent class
			//TODO del			setOpenEffect(new OpacityFadeEffect(component.getSession(), 500));	//create a default open effect TODO use a constant
		}

		@Override
		protected FlyoverFrame createFrame() {
			final S component = getComponent(); //get the component
			final FlyoverFrame frame = new DefaultFlyoverFrame(); //create a default frame
			frame.setRelatedComponent(getComponent()); //tell the flyover frame with which component it is related
			final Message message = new Message(); //create a new message
			message.setMessageContentType(component.getDescriptionContentType()); //set the appropriate message content
			message.setMessage(component.getDescription()); //set the appropriate message text
			frame.setContent(message); //put the message in the frame
			return frame; //return the frame we created
		}
	}

}
