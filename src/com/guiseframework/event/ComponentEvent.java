package com.guiseframework.event;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.component.Component;

/**An event relating to a component.
The event target indicates the object that originally fired the event.
@author Garret Wilson
@see CompositeComponentListener
*/
public class ComponentEvent extends AbstractTargetedGuiseEvent
{

	/**The component affected by the event.*/
	private final Component component;

		/**@return The component affected by the event.*/
		public Component getComponent() {return component;}

	/**Source and component constructor.
	The target will be set to be the same as the given source.
	@param source The object on which the event initially occurred.
	@param component The component affected by the event.
	@exception NullPointerException if the given source and/or component is <code>null</code>.
	*/
	public ComponentEvent(final Object source, final Component component)
	{
		this(source, source, component);	//construct the class with the same target as the source
	}

	/**Source, target, and component constructor.
	@param source The object on which the event initially occurred.
	@param target The target of the event.
	@param component The component affected by the event.
	@exception NullPointerException if the given source, target, and/or component is <code>null</code>.
	*/
	public ComponentEvent(final Object source, final Object target, final Component component)
	{
		super(source, target);	//construct the parent class
		this.component=checkInstance(component, "Component cannot be null.");
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@param componentEvent The event the properties of which will be copied.
	@exception NullPointerException if the given source and/or event is <code>null</code>.
	*/
	public ComponentEvent(final Object source, final ComponentEvent componentEvent)
	{
		this(source, componentEvent.getTarget(), componentEvent.getComponent());	//construct the class with the same target		
	}
}
