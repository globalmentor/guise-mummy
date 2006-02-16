package com.guiseframework.component;

import com.guiseframework.GuiseSession;

/**An abstract implementation of a composite component.
@author Garret Wilson
*/
public abstract class AbstractCompositeComponent<C extends CompositeComponent<C>> extends AbstractComponent<C> implements CompositeComponent<C>
{

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalStateException if no controller is registered for this component type.
	@exception IllegalStateException if no view is registered for this component type.
	*/
	public AbstractCompositeComponent(final GuiseSession session, final String id)
	{
		super(session, id);	//construct the parent class
	}

	/**Determines whether this component and all of its child components are valid.
	This version returns <code>true</code> if all its child components are valid.
	@return Whether this component and all of its child components are valid.
	*/
	public boolean isValid()	//TODO reconcile this design with the new control isValid() semantics
	{
		if(!super.isValid())	//if the component doesn't pass the default checks
		{
			return false;	//this component isn't valid
		}
		for(final Component<?> childComponent:this)	//for each child component
		{
			if(!childComponent.isValid())	//if this child component isn't valid
			{
				return false;	//indicate that this component is consequently not valid
			}
		}
		return true;	//indicate that all child components are valid
	}

	/**Validates the this component and all child components.
	The component will be updated with error information.
	This version validates the this component and all child components.
	@exception ComponentExceptions if there was one or more validation error.
	*/
	public void validate() throws ComponentExceptions
	{
		ComponentExceptions componentExceptions=null;	//we'll store any component exceptions here and keep going
		try
		{
			super.validate();	//validate the component normally
		}
		catch(final ComponentExceptions superComponentExceptions)	//if the super version returns an error
		{
			if(componentExceptions==null)	//if this is our first component exception
			{
				componentExceptions=superComponentExceptions;	//store the exception and continue processing events with other child components
			}
			else	//if we already have component exceptions
			{
				componentExceptions.addAll(superComponentExceptions);	//add all the exceptions to the exception we already have
			}
		}
		for(final Component<?> childComponent:this)	//for each child component
		{
			try
			{
				childComponent.validate();	//validate the child
			}
			catch(final ComponentExceptions childComponentExceptions)	//if a child returns an error
			{
				if(componentExceptions==null)	//if this is our first component exception
				{
					componentExceptions=childComponentExceptions;	//store the exception and continue processing events with other child components
				}
				else	//if we already have component exceptions
				{
					componentExceptions.addAll(childComponentExceptions);	//add all the child component exceptions to the exception we already have
				}
			}
		}
		if(componentExceptions!=null)	//if we encountered one or more component exceptions
		{
			throw componentExceptions;	//throw the exception, which may contain multiple exceptions
		}
	}

}
