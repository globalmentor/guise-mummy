package com.guiseframework.component;

import static com.globalmentor.java.Classes.*;

/**A composite component that holds a child content component. 
The component's content is specified using {@link #setContent(Component)}.
@author Garret Wilson
*/
public interface ContentComponent extends CompositeComponent
{
	/**The content bound property.*/
	public final static String CONTENT_PROPERTY=getPropertyName(ContentComponent.class, "content");

	/**@return The content child component, or <code>null</code> if this component does not have a content child component.*/
	public Component getContent();

	/**Sets the content child component.
	This is a bound property
	@param newContent The content child component, or <code>null</code> if this component does not have a content child component.
	@see #CONTENT_PROPERTY
	*/
	public void setContent(final Component newContent);
}
