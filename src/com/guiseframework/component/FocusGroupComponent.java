package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.beans.PropertyVetoException;

import com.guiseframework.event.FocusStrategy;

/**A focusable Guise component that serves as a parent of other components that can be focused.
@author Garret Wilson
*/
public interface FocusGroupComponent<C extends FocusGroupComponent<C>> extends FocusableComponent<C>
{

	/**The focus strategy bound property.*/
	public final static String FOCUS_STRATEGY_PROPERTY=getPropertyName(FocusGroupComponent.class, "focusStrategy");
	/**The focused component bound property.*/
	public final static String FOCUSED_COMPONENT_PROPERTY=getPropertyName(FocusGroupComponent.class, "focusedComponent");

	/**@return The focus strategy for this focus group.*/
	public FocusStrategy getFocusStrategy();

	/**Sets the focus strategy.
	This is a bound property
	@param newFocusStrategy The focus strategy for this group.
	@exception NullPointerException if the given focus strategy is <code>null</code>.
	@see #FOCUS_STRATEGY_PROPERTY
	*/
	public void setFocusStrategy(final FocusStrategy newFocusStrategy);

	/**Indicates the component within this group that has the focus.
	The focused component may be another {@link FocusGroupComponent}, which in turn will have its own focus component.
	@return The component within this group that has the focus, or <code>null</code> if no component currently has the focus.
	*/ 
	public FocusableComponent<?> getFocusedComponent();

	/**Sets the focused component within this group.
	This is a bound property.
	@param newFocusableComponent The component to receive the focus.
	@exception PropertyVetoException if the given component is not a focusable component within this group, the component cannot receive the focus, or the focus change has otherwise been vetoed.
	@see #getFocusStrategy()
	@see #FOCUSED_COMPONENT_PROPERTY
	*/
	public void setFocusedComponent(final FocusableComponent<?> newFocusedComponent) throws PropertyVetoException;

	/**Indicates the leaf component within this group that has the focus.
	If this group's focused component is another {@link FocusGroupComponent}, the leaf focus component component is recursively retrieved from that component.
	This method will return a {@link FocusGroupComponent} if that focus group has no focused comonent.
	If there is no focused component within this focus group, this method returns <code>null</code>.
	@return The leaf component within this group that has the focus, or <code>null</code> if no leaf focusable component within this group currently has the focus.
	*/ 
//TODO move to ApplicationFrame	public FocusableComponent<?> getLeafFocusedComponent();

}