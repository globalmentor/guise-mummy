package com.javaguise.model;

import java.util.*;

import com.garretwilson.beans.PropertyValueChangeEvent;
import com.javaguise.validator.ValidationException;

/**A group that ensures that only one boolean model in the group is set to <code>true</code> at the same time.
This class is declared final because it represents a particular defined semantics, no more and no less.
This allows controllers to make assumptions about models with only this listener, and offload mutual exclusion to client that have this capability built in.
@author Garret Wilson.
*/
public final class MutualExclusionModelGroup extends ValuePolicyModelGroup<Boolean>
{

	/**Model constructor.
	@param models Zero or more models with which to initially place in the group.
	@exception NullPointerException if one of the models is <code>null</code>.
	*/
	public MutualExclusionModelGroup(final ValueModel<Boolean>... models)
	{
		super(models);	//construct the parent class
	}

	/**Called when the boolean model value is changed.
	@param propertyValueChangeEvent An event object describing the event source, the property that has changed, and its old and new values.
	*/
	public void propertyValueChange(final PropertyValueChangeEvent<Boolean> propertyValueChangeEvent)
	{
		if(Boolean.TRUE.equals(propertyValueChangeEvent.getNewValue()))	//if this model is changing to true, change the other models to false
		{
			final Object source=propertyValueChangeEvent.getSource();	//see which model changed
			final Set<ValueModel<Boolean>> modelSet=getModelSet();	//get the set of models in the group
			for(final ValueModel<Boolean> valueModel:modelSet)	//for each model in the group
			{
				if(valueModel!=source)	//if this is not the source (the source model should keep the value of true)
				{
					try
					{
						valueModel.setValue(Boolean.FALSE);	//set the values of the other value models to false (which will fire other events, but will be ignored by this class because the value is false)
					}
					catch(final ValidationException validationException)	//if this model can't be set to false
					{
						throw new AssertionError(validationException);
					}
				}
			}
		}
	}

}
