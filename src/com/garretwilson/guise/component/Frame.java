package com.garretwilson.guise.component;

import static com.garretwilson.lang.ClassUtilities.*;
import com.garretwilson.guise.model.LabelModel;

/**A root-level component such as a window or an HTML page.
The title is specified by the frame model's label.
@author Garret Wilson
*/
public interface Frame<C extends Frame<C>> extends ModelComponent<LabelModel, C>
{
	/**The content bound property.*/
	public final static String CONTENT_PROPERTY=getPropertyName(Frame.class, "content");

	/**@return The component representing the frame's content, or <code>null</code> if there is no content component.*/
	public Component<?> getContent();

	/**Sets the content component.
	This is a bound property
	@param newContent The component representing this frame's content, <code>null</code> if this frame has no content.
	@see Frame#CONTENT_PROPERTY
	*/
	public void setContent(final Component<?> newContent);

}
