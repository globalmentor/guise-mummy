package com.guiseframework.platform;

import java.beans.*;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.garretwilson.beans.PropertyBindable;
import com.guiseframework.Guise;
import com.guiseframework.GuiseSession;
import com.guiseframework.event.*;

/**An abstract strategy for depicting objects on some platform.
@param <O> The type of object being depicted.
@author Garret Wilson
*/
public abstract class AbstractDepictor<O extends DepictedObject> implements Depictor<O>
{

	/**The Guise session that owns this object.*/
	private final GuiseSession session;

		/**@return The Guise session that owns this object.*/
		public GuiseSession getSession() {return session;}

		/**@return The platform on which this depictor is depicting ojects.*/
		public GuisePlatform getPlatform() {return getSession().getPlatform();}
		
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
	protected final ChangeListener CHANGE_LISTENER=new ChangeListener();

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
	}
		
	/**Called when the depictor is installed in a depicted object.
	This version listens for property changes of a {@link PropertyBindable} object.
	This version listens for list changes of a {@link ListListenable} object.
	@param depictedObject The depictedObject into which this depictor is being installed.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	@exception IllegalStateException if this depictor is already installed in a depicted object.
	@see #CHANGE_LISTENER
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
			((PropertyBindable)depictedObject).addPropertyChangeListener(CHANGE_LISTENER);	//listen for property changes
		}
		if(depictedObject instanceof ListListenable)	//if the depicted object notifies of list changes
		{
			((ListListenable<Object>)depictedObject).addListListener(CHANGE_LISTENER);	//listen for list changes
		}
	}

	/**Called when the depictor is uninstalled from a depicted object.
	This version stop listening for property changes of a {@link PropertyBindable} object.
	This version stops listening for list changes of a {@link ListListenable} object.
	@param depictedObject The depicted object from which this depictor is being uninstalled.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	@exception IllegalStateException if this depictor is not installed in a depicted object.
	@see #CHANGE_LISTENER
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
			((PropertyBindable)depictedObject).removePropertyChangeListener(CHANGE_LISTENER);	//stop listening for property changes
		}
		if(depictedObject instanceof ListListenable)	//if the depicted object notifies of list changes
		{
			((ListListenable<Object>)depictedObject).removeListListener(CHANGE_LISTENER);	//stop listening for list changes
		}
	}

	/**Depicts the depicted object.
	This implementation marks the depiction as depicted.
	@exception IOException if there is an error updating the depiction.
	*/
	public void update() throws IOException
	{
		setDepicted(true);	//show that the depiction has been updated
	}

	/**A listener that marks this depiction as dirty if changes occur.
	This class implements various event listeners and marks the depiction as dirty accordingly.
	@author Garret Wilson
	*/
	protected class ChangeListener implements PropertyChangeListener, ListListener<Object>
	{

		/**Called when a bound property is changed.
		@param propertyChangeEvent An event object describing the event source and the property that has changed.
		*/ 
		public void propertyChange(final PropertyChangeEvent propertyChangeEvent)
		{
			final Object source=propertyChangeEvent.getSource();	//get the source of the event
			final String propertyName=propertyChangeEvent.getPropertyName();	//get the name of the changing property
			final Object oldValue=propertyChangeEvent.getOldValue();	//get the old value
			final Object newValue=propertyChangeEvent.getOldValue();	//get the new value
			if(getIgnoredProperties().contains(propertyName))	//if this is an ignored property
			{
				return;	//ignore this property change
			}
			setPropertyModified(propertyName, true);	//show that a property has been modified
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
