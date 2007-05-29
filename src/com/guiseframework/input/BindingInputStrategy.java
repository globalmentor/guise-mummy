package com.guiseframework.input;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.guiseframework.Guise;
import com.guiseframework.GuiseSession;
import com.guiseframework.model.ActionModel;

/**An input strategy based upon input bindings between input and other input or actions.
Typical uses include binding {@link CommandInput} to {@link KeyInput}, or binding an {@link ActionModel} to {@link CommandInput}. 
There must be a {@link GuiseSession} in effect when this {@link #input(Input)} is called for this input strategy.
@author Garret Wilson
*/
public class BindingInputStrategy extends AbstractInputStrategy
{

	/**The thread-safe map of bindings between input and other input or actions. The bound object is either of type {@link Input} or of type {@link ActionModel}.*/
	private final Map<Input, Object> bindings=new ConcurrentHashMap<Input, Object>();

	/**Binds the given input to other input.
	If the given input is already bound, the old binding will be replaced.
	@param input The input to be bound, such as {@link KeyInput}.
	@param targetInput The target input, such as {@link CommandInput}.
	*/
	public void bind(final Input input, final Input targetInput)
	{
		bindings.put(input, targetInput);	//bind the target input to the input
	}

	/**Binds the given input to an action.
	If the given input is already bound, the old binding will be replaced.
	@param input The input to be bound, such as {@link CommandInput}.
	@param targetAction The target action that should be performed.
	*/
	public void bind(final Input input, final ActionModel targetAction)
	{
		bindings.put(input, targetAction);	//bind the target action to the input
	}

	/**Unbinds the given input from any other input or action.
	If there is no binding with the given input, no action is taken. 
	@param input The input to be unbound.
	*/
	public void unbind(final Input input)
	{
		bindings.remove(input);	//remove any bindings to the input
	}

	/**Default constructor with no parent.*/
	public BindingInputStrategy()
	{
		this(null);	//construct the class with no parent
	}

	/**Parent constructor.
	@param parent The parent input strategy, or <code>null</code> if there is no parent input strategy.
	*/
	public BindingInputStrategy(final InputStrategy parent)
	{
		super(parent);	//construct the parent class
	}

	/**Processes input, returning whether the input was consumed.
	If the input is not consumed by this input strategy, it is sent to the parent input strategy, if any, for processing.
	If input is bound to the given input, the input is delegated to {@link GuiseSession#input(Input)} and considered to be consumed.
	If an action is bound to the given input, the action is performed and the input is considered to be consumed.
	@param input The input to process.
	@return <code>true</code> if the input was consumed and should not be processed further.
	@exception NullPointerException if the given input is <code>null</code>.
	@see GuiseSession#input(Input)
	@see ActionModel#performAction()
	*/
	public boolean input(final Input input)
	{
		final Object targetObject=bindings.get(input);	//get the target object bound to this input
		if(targetObject!=null)	//if there is a target object
		{
			if(targetObject instanceof Input)	//if the target is more input
			{
				Guise.getInstance().getGuiseSession().input((Input)targetObject);	//send the input to the Guise session for further processing
			}
			else if(targetObject instanceof ActionModel)	//if the target is an action
			{
				((ActionModel)targetObject).performAction();	//perform the action
			}
			else	//if we don't recognize the target object, something's wrong, because we control everything that's stored in the map
			{
				throw new AssertionError("Unrecognized input binding: "+targetObject);
			}
			return true;	//indicate that we consumed the input
		}
		else	//if there is nothing bound to the given input
		{
			return super.input(input);	//perform the default processing, which includes delegation to any parent input strategy
		}
	}
}
