package com.guiseframework.prototype;

import java.util.*;

import com.garretwilson.util.*;

import com.guiseframework.component.*;
import static com.guiseframework.component.Components.*;

/**Abstract strategy for keeping track of prototype providers and working with provisioned prototypes, merging them into a menu and/or a toolbar of a frame.
When the prototype providers change provided prototypes, those provided prototypes are processed.
This version monitors the parent composite component children and automatically uses top-level prototype providers added to or removed from the hierarchy.
Prototype provisions are not processed initially; this strategy should be initialized after construction by calling {@link #processPrototypeProvisions()}.
This class is thread safe based upon its exposed read and write locks.
@author Garret Wilson
*/
public abstract class AbstractMenuToolCompositeComponentPrototypeProvisionStrategy extends AbstractCompositeComponentPrototypeProvisionStrategy
{

	/**@return The menu being managed, or <code>null</code> if no menu is being managed.*/
	protected abstract Menu getMenu();

	/**@return The toolbar being managed, or <code>null</code> if no toolbar is being managed.*/
	protected abstract Toolbar getToolbar();

	/**The reverse map of menu components that have been used to represent processed prototypes, keyed to the prototype infos they represent.*/
	private ReadWriteLockReverseMap<PrototypeProvision<?>, Component> prototypeProvisionMenuComponentMap=new DecoratorReadWriteLockReverseMap<PrototypeProvision<?>, Component>(new HashMap<PrototypeProvision<?>, Component>(), new HashMap<Component, PrototypeProvision<?>>(), this);

	/**The reverse map of tool components that have been used to represent processed prototypes, keyed to the prototype infos they represent; this map uses the same locks as the one for menu components.*/
	private ReadWriteLockReverseMap<PrototypeProvision<?>, Component> prototypeProvisionToolComponentMap=new DecoratorReadWriteLockReverseMap<PrototypeProvision<?>, Component>(new HashMap<PrototypeProvision<?>, Component>(), new HashMap<Component, PrototypeProvision<?>>(), this);

	/**Parent component and prototype providers constructor.
	@param parentComponent The composite component the top-level prototype provider children of which will be monitored.
	@param defaultPrototypeProviders The default prototype providers that will provide prototypes for processing, outside the children of the composite component parent.
	@exception NullPointerException if the given parent component, prototype providers, and/or one or more prototype provider is <code>null</code>.
	*/
	public AbstractMenuToolCompositeComponentPrototypeProvisionStrategy(final CompositeComponent parentComponent, final PrototypeProvider... defaultPrototypeProviders)
	{
		super(parentComponent, defaultPrototypeProviders);	//construct the parent class
	}
	
	/**Processes prototype provisions.
	This implementation merges prototype provisions into the given menu and/or toolbar, if any.
	@param prototypeProvisions The mutable set of prototype provisions to be used.
	@see #getMenu()
	@see #getToolbar()
	*/
	protected void processPrototypeProvisions(final Set<PrototypeProvision<?>> prototypeProvisions)
	{
		prototypeProvisionMenuComponentMap.writeLock().lock();	//get a write lock to our prototype component maps
		try
		{
			final Menu menu=getMenu();	//get the menu, if any
			if(menu!=null)	//if there is a menu
			{
				synchronizePrototypeProvisionMap(prototypeProvisionMenuComponentMap, menu, prototypeProvisions);	//synchronize the menu component map
			}
			else	//if there is no menu
			{
				prototypeProvisionMenuComponentMap.clear();	//there can be no menu prototype components
			}
			final Toolbar toolbar=getToolbar();	//get the toolbar, if any
			if(toolbar!=null)	//if there is a toolbar
			{
				synchronizePrototypeProvisionMap(prototypeProvisionToolComponentMap, toolbar, prototypeProvisions);	//synchronize the toolbar component map
			}
			else	//if there is no toolbar
			{
				prototypeProvisionToolComponentMap.clear();	//there can be no tool prototype components
			}
			boolean addAllRemaining=false;	//at some point we'll have to add all remaining prototypes
			do
			{
				int menuPrototypesConsumedCount=0;	//keep track of how many many menu prototypes we consumed
				int toolPrototypesConsumedCount=0;	//keep track of how many many tool prototypes we consumed (this is for consistency; one iteration should always consume all the tool prototypes)
				final Iterator<PrototypeProvision<?>> prototypeProvisionIterator=prototypeProvisions.iterator();	//get an iterator to look at all the prototype provisions
				while(prototypeProvisionIterator.hasNext())	//while there are more prototype provisions
				{
					final PrototypeProvision<?> prototypeProvision=prototypeProvisionIterator.next();	//get the next prototype info
					boolean discardPrototype=!prototypeProvision.isMenu() && !prototypeProvision.isTool();	//we'll see if this prototype was consumed; if this is not a menu or tool prototype, consider it already consumed
					if(!discardPrototype)	//if we haven't yet consumed this prototype
					{
						if(prototypeProvision.isMenu())	//if this is a menu prototype
						{
							if(menu!=null && !prototypeProvisionMenuComponentMap.containsKey(prototypeProvision))	//if we have a menu, but we haven't yet created a menu component for this prototype info
							{
//Debug.trace("no menu yet created for prototype", prototypePublication);
								final PrototypeProvision<?> parentPrototypeProvision=prototypeProvision.getParentPrototypeProvision();	//get the prototype's parent, if any
								final Container parentContainer;	//we'll determine where to add this prototype; if we set this to null, the component shouldn't be added
								if(parentPrototypeProvision!=null)	//if this prototype specifies a parent
								{
//Debug.trace("prototype", prototypePublication, "has parent", parentPrototypePublication);
									final Component parentPrototypeComponent=prototypeProvisionMenuComponentMap.get(parentPrototypeProvision);	//get the component used to represent the parent prototype
									if(parentPrototypeComponent instanceof Container)	//if the parent component is a container
									{
//Debug.trace("found parent component", parentPrototypeComponent, "which is a container");
										parentContainer=(Container)parentPrototypeComponent;	//use the parent component as the container
									}
									else if(parentPrototypeComponent!=null)	//if the parent component is a non-container component
									{
//Debug.trace("found parent component", parentPrototypeComponent, "which is not a container so we'll use the menu as the parent");
										parentContainer=menu;	//add the prototype to the menu

									}
									else	//if there is no component for the parent prototype yet
									{
										parentContainer=addAllRemaining ? menu : null;	//if we should add all remaining prototypes, put it in the menu; otherwise, wait until the next iteration
//Debug.trace("no parent found; add all remaining?", addAllRemaining, "parent container to use", parentContainer);
									}
								}
								else	//if this prototype doesn't specify a parent
								{
									parentContainer=menu;	//add the component to the menu
								}
								if(parentContainer!=null)	//if we know a parent container to which to add the prototype
								{
									final Component component=add(parentContainer, prototypeProvision);	//add this prototype to the parent container
									prototypeProvisionMenuComponentMap.put(prototypeProvision, component);	//note that we created this component to represent this prototype publication
									discardPrototype=true;	//indicate that we consumed this prototype
									++menuPrototypesConsumedCount;	//indicate that we consumed another menu prototype
								}
							}
							else	//if we don't have a menu, or we've already created a component for this prototype info
							{
								discardPrototype=true;	//consider this prototype consumed
							}
						}
						if(prototypeProvision.isTool())	//if this is a tool prototype and we have a toolbar, but we haven't yet created a tool component for this prototype
						{
							if(toolbar!=null && !prototypeProvisionToolComponentMap.containsKey(prototypeProvision))	//if we have a toolbar, but we haven't yet created a tool component for this prototype
							{
								final Component component=add(toolbar, prototypeProvision);	//add this prototype to the toolbar
								prototypeProvisionToolComponentMap.put(prototypeProvision, component);	//note that we created this component to represent this prototype info
								if(!prototypeProvision.isMenu())	//if this wasn't a menu prototype (we'll let menu prototypes be permanently consumed by the menu logic)
								{
									discardPrototype=true;	//consider this prototype consumed
								}
								++toolPrototypesConsumedCount;	//indicate that we consumed another tool prototype
							}
							else	//if we don't have a toolbar, or we've already created a component for this prototype info
							{
								discardPrototype=true;	//consider this prototype consumed
							}
						}
					}
					if(discardPrototype)	//if we should discard this prototype
					{
						prototypeProvisionIterator.remove();	//don't process it in the future
					}
				}
				if(menuPrototypesConsumedCount==0 && toolPrototypesConsumedCount==0)	//if we didn't consume any prototypes in this iteration
				{
					addAllRemaining=true;	//on the next iteration, add all remaining prototypes
				}
			}
			while(!prototypeProvisions.isEmpty());	//keep looping until we've consumed all the prototypes
		}
		finally
		{
			prototypeProvisionMenuComponentMap.writeLock().unlock();	//always release the write lock to our prototype component maps
		}
	}

	/**Synchronizes the map of prototype provision component associations.
	For each prototype provision association, the association is removed if the associated component no longer exists in the component tree, or if the associated prototype provision is not given;
	if the latter, the associated component is removed from its parent.
	@param prototypeProvisionComponentMap The map from which to remove values.
	@param parentComponent The parent of the component tree in which published prototypes are represented.
	@param prototypeProvisionSet The set to indicate which key entries to retain.
	@exception NullPointerException if the given map, parent component, or prototype provision set is <code>null</code>.
	*/
	protected static void synchronizePrototypeProvisionMap(final ReverseMap<PrototypeProvision<?>, Component> prototypeProvisionComponentMap, final CompositeComponent parentComponent, final Set<PrototypeProvision<?>> prototypeProvisionSet)
	{
		final Collection<Component> components=getDescendantComponents(parentComponent);	//get all the descendants of the parent component
		final Iterator<Map.Entry<PrototypeProvision<?>, Component>> prototypeProvisionComponentEntryIterator=prototypeProvisionComponentMap.entrySet().iterator();	//get an iterator to our current prototype/component mappings
		while(prototypeProvisionComponentEntryIterator.hasNext())	//while there are more prototype/component mappings
		{
			final Map.Entry<PrototypeProvision<?>, Component> prototypeProvisionComponentEntry=prototypeProvisionComponentEntryIterator.next();	//get the next mapping
			final Component component=prototypeProvisionComponentEntry.getValue();	//get the component representing the prototype
			if(!components.contains(component))	//if this component is no longer in the tree
			{
				prototypeProvisionComponentEntryIterator.remove();	//remove this mapping; we don't have the corresponding component anymore
			}
			else	//if we still have the component, make sure we'll still have the prototype provision
			{
				final PrototypeProvision<?> prototypeProvision=prototypeProvisionComponentEntry.getKey();	//get the prototype info that was used
				if(!prototypeProvisionSet.contains(prototypeProvision))	//if we no longer have this prototype info
				{
					final CompositeComponent parent=component.getParent();	//get this component's parent
					if(parent instanceof Container)	//if the component is still installed in a container
					{
						((Container)parent).remove(component);	//remove the component from its parent
					}
					prototypeProvisionComponentEntryIterator.remove();	//remove this mapping; we don't have this prototype info or corresponding component anymore
				}
			}
		}
	}

	/**Adds a prototype to a container in the correct order by examining the prototype information of the other components added to the container
	The prototype is inserted before the first component that was created from prototype info with a larger order.
	@param container The container to which the prototype should be added.
	@param prototypeProvision The prototype information to add.
	@return The component that was created to represent the prototype.
	@see PrototypeProvision#getOrder()
	*/
	protected Component add(final Container container, final PrototypeProvision<?> prototypeProvision)
	{
		final double order=prototypeProvision.getOrder();	//get the prototype's order
		int index=0;	//keep track of the child component index
		for(final Component childComponent:container)	//for each child component in the container
		{
			final PrototypeProvision<?> childPrototypeProvision=prototypeProvisionMenuComponentMap.getKey(childComponent);	//get the prototype used to create this component, if any
			if(childPrototypeProvision!=null && childPrototypeProvision.getOrder()>order)	//if this child component was created from a prototype info that has a higher order than the new one
			{
				break;	//use this index
			}
			else	//if this component wasn't created from a prototype, or the prototype info had a lower order than the new one
			{
				++index;	//go to the next index
			}
		}
		return container.add(index, prototypeProvision.getPrototype());	//insert the prototype in front of the existing child component (or at the end, if there was no lower prototype info) and return the created child component
	}

}
