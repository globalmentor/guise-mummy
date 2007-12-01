package com.guiseframework.platform;

import java.beans.*;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.garretwilson.beans.PropertyBindable;
import com.guiseframework.*;
import com.guiseframework.event.*;

/**An abstract strategy for depicting objects on some platform.
The {@link Depictor#GENERAL_PROPERTY} is used to indicate that some general property has changed.
@param <O> The type of object being depicted.
@author Garret Wilson
*/
public abstract class AbstractDepictor<O extends DepictedObject> implements Depictor<O>
{

	/**The Guise session that owns this object.*/
	private final GuiseSession session;

		/**@return The Guise session that owns this object.*/
		public GuiseSession getSession() {return session;}

	/**The platform on which this depictor is depicting ojects.*/
	private final Platform platform;		
		
		/**@return The platform on which this depictor is depicting ojects.*/
		public Platform getPlatform() {return platform;}
		
	/**Retrieves information and functionality related to the current depiction on the platform.
	This method delegates to {@link Platform#getDepictContext()}.
	@return A context for the current depiction.
	@exception IllegalStateException if no depict context can be returned in the current depiction state.
	*/
	public DepictContext getDepictContext() {return getPlatform().getDepictContext();}

	/**The thread-safe list of properties that are to be ignored.*/
	private final Set<String> ignoredProperties=new CopyOnWriteArraySet<String>(); 

		/**@return The depicted object properties that are to be ignored.*/
		protected Set<String> getIgnoredProperties() {return ignoredProperties;}

	/**The thread-safe list of modifed properties.*/
	private final Set<String> modifiedProperties=new CopyOnWriteArraySet<String>(); 

		/**@return The depicted object properties that have been modified.*/
		protected Set<String> getModifiedProperties() {return modifiedProperties;}
	
		/**Calls when a property has been modified to sets whether a property has been modified.
		If the property's modified status is set to <code>true</code>, the depictor's {@link #isDepicted()} status is changed to <code>false</code>.
		If the property's modified status is set to <code>false</code> and there are no other modified properties, the depictor's {@link #isDepicted()} status is set to <code>true</code>.
		@param property The property that has been modified.
		@see #setDepicted(boolean)
		*/
		protected void setPropertyModified(final String property, final boolean modified)
		{
			if(modified)	//if the property is modified
			{
				modifiedProperties.add(property);	//add this property to the list of modified properties
				depicted=false;	//note that the depiction is not updated
			}
			else	//if the property is not modified
			{
				if(modifiedProperties.remove(property))	//remove the property from the set of modified properties; if the property was in the set
				{
					if(modifiedProperties.isEmpty())	//if there are no modified properties
					{
						depicted=true;	//count the depiction as updated
					}
				}
			}
		}
	
	/**The listener that marks this depiction as dirty if a change occurs.*/
	private final DepictedPropertyChangeListener depictedPropertyChangeListener=new DepictedPropertyChangeListener();

		/**@return The listener that marks this depiction as dirty if a change occurs.*/
		protected DepictedPropertyChangeListener getDepictedPropertyChangeListener() {return depictedPropertyChangeListener;}

	/**The object being depicted.*/
	private O depictedObject=null;

		/**@return The object being depicted, or <code>null</code> if this depictor is not installed in a depicted object.*/
		public O getDepictedObject() {return depictedObject;}

	/**Whether this depictor's representation of the depicted object is up to date.*/
	private boolean depicted=false;

		/**@return Whether this depictor's representation of the depicted object is up to date.*/
		public boolean isDepicted() {return depicted;}

		/**Changes the depictor's updated status.
		If the new depicted status is <code>true</code>, all modified properties are removed.
		If the new depicted status is <code>false</code>, the {@link Depictor#GENERAL_PROPERTY} property is set as modified.
		@param newDepicted Whether this depictor's representation of the depicted object is up to date.
		*/
		public void setDepicted(final boolean newDepicted)
		{
			if(newDepicted)	//if the depiction is being marked as updated 
			{
				modifiedProperties.clear();	//remove all modified properties
			}
			else	//if the depiction is being marked as not updated
			{
				modifiedProperties.add(GENERAL_PROPERTY);	//add the general property to the list of modified properties				
			}
			depicted=newDepicted;	//update the depicted status
		}

	/**Default constructor.*/
	public AbstractDepictor()
	{
		this.session=Guise.getInstance().getGuiseSession();	//store a reference to the current Guise session
		this.platform=this.session.getPlatform();	//store a reference to the platform
	}

	/**Called when the depictor is installed in a depicted object.
	This version listens for property changes of a {@link PropertyBindable} object.
	This version listens for list changes of a {@link ListListenable} object.
	@param depictedObject The depictedObject into which this depictor is being installed.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	@exception IllegalStateException if this depictor is already installed in a depicted object.
	@see #depictedPropertyChangeListener
	*/
	public void installed(final O depictedObject)
	{
		if(this.depictedObject!=null)	//if this depictor is already installed
		{
			throw new IllegalStateException("Depictor is already installed in a depicted object.");
		}
		this.depictedObject=depictedObject;	//change depicted objects
		if(depictedObject instanceof PropertyBindable)	//if the depicted object allows bound properties
		{
			((PropertyBindable)depictedObject).addPropertyChangeListener(getDepictedPropertyChangeListener());	//listen for property changes
		}
		if(depictedObject instanceof ListListenable)	//if the depicted object notifies of list changes
		{
			((ListListenable<Object>)depictedObject).addListListener(getDepictedPropertyChangeListener());	//listen for list changes
		}
	}

	/**Called when the depictor is uninstalled from a depicted object.
	This version stop listening for property changes of a {@link PropertyBindable} object.
	This version stops listening for list changes of a {@link ListListenable} object.
	@param depictedObject The depicted object from which this depictor is being uninstalled.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	@exception IllegalStateException if this depictor is not installed in a depicted object.
	@see #depictedPropertyChangeListener
	*/
	public void uninstalled(final O depictedObject)
	{
		if(this.depictedObject==null)	//if this depictor is not installed
		{
			throw new IllegalStateException("Depictor is not installed in a depicted object.");
		}
		this.depictedObject=null;	//remove the depicted object
		if(depictedObject instanceof PropertyBindable)	//if the depicted object allows bound properties
		{
			((PropertyBindable)depictedObject).removePropertyChangeListener(depictedPropertyChangeListener);	//stop listening for property changes
		}
		if(depictedObject instanceof ListListenable)	//if the depicted object notifies of list changes
		{
			((ListListenable<Object>)depictedObject).removeListListener(depictedPropertyChangeListener);	//stop listening for list changes
		}
	}

	/**Processes an event from the platform.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	*/
	public void processEvent(final PlatformEvent event)
	{
	}

	/**Updates the depiction of the object.
	This implementation marks the depiction as depicted.
	@exception IOException if there is an error updating the depiction.
	*/
	public void depict() throws IOException
	{
		setDepicted(true);	//show that the depiction has been updated
	}

	/**Called when a depicted object bound property is changed.
	This method may also be called for objects related to the depicted object, so if specific properties are checked the event source should be verified to be the depicted object.
	This implementation marks the property as being modified if the property is not an ignored property.
	@param propertyChangeEvent An event object describing the event source and the property that has changed.
	@see #getIgnoredProperties()
	@see #setPropertyModified(String, boolean)
	*/ 
	protected void depictedObjectPropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		final String propertyName=propertyChangeEvent.getPropertyName();	//get the name of the changing property
		if(getIgnoredProperties().contains(propertyName))	//if this is an ignored property
		{
			return;	//ignore this property change
		}
		setPropertyModified(propertyName, true);	//show that a property has been modified
	}

	/**A listener that marks this depiction as dirty if changes occur.
	Property changes are delegated to {@link AbstractDepictor#depictedObjectPropertyChange(PropertyChangeEvent)}.
	@author Garret Wilson
	*/
	protected class DepictedPropertyChangeListener implements PropertyChangeListener, ListListener<Object>
	{

		/**Called when a bound property is changed.
		@param propertyChangeEvent An event object describing the event source and the property that has changed.
		*/ 
		public void propertyChange(final PropertyChangeEvent propertyChangeEvent)
		{
			depictedObjectPropertyChange(propertyChangeEvent);	//delegate to the outer class method
		}

		/**Called when a list is modified.
		@param listEvent The event indicating the source of the event and the list modifications.
		*/
		public void listModified(final ListEvent<Object> listEvent)
		{
			setDepicted(false);	//show that we need general updates			
		}
	};

}
