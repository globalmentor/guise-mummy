package com.guiseframework.event;

/**An object that listens for components being added to or removed from a composite component.
@author Garret Wilson
*/
public interface CompositeComponentListener extends GuiseEventListener
{

	/**Called when a child component is added to a composite component.
	@param childComponentEvent The event indicating the added child component and the target parent composite component.
	*/
	public void childComponentAdded(final ComponentEvent childComponentEvent);

	/**Called when a child component is removed from a composite component.
	@param childComponentEvent The event indicating the removed child component and the target parent composite component.
	*/
	public void childComponentRemoved(final ComponentEvent childComponentEvent);

}
