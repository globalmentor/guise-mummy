package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;

import com.guiseframework.component.*;
import com.guiseframework.event.NavigateActionListener;

/**Strategy for rendering an image select action control as an XHTML <code>&lt;img&gt;</code> inside a <code>&lt;a&gt;</code> element.
If a link has a {@link NavigateActionListener} as one of its action listeners, the generated <code>href</code> URI will be that of the listener,
	and a <code>target</code> attribute will be set of the listener specifies a viewport ID.
<p>This view uses the following attributes which are not in XHTML:
	<ul>
		<li><code>guise:originalSrc</code></li>
		<li><code>guise:rolloverSrc</code></li>
	</ul>
</p>
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebImageBooleanSelectActionControlViewer<C extends ImageBooleanSelectActionControl> extends WebImageActionControlDepictor<C>
{

	/**Determines the image to use for this component.
	This implementation returns the selected image if the component is selected and there is a selected image.
	@return The image to use for the component, or <code>null</code> if there should not be an image.
	@see ImageComponent#getImageURI()
	@see ImageBooleanSelectActionControl#isSelected()
	@see ImageBooleanSelectActionControl#getRolloverImage()
	*/
	protected URI getImage()
	{
		final C component=getDepictedObject();	//get the component
		URI image=null;	//we'll determine the image
		final boolean isSelected=component.getValue().booleanValue();	//see if the component is selected
		if(isSelected)	//if the component is selected
		{
			image=component.getSelectedImage();	//use the selected image
			if(image==null)	//if there is no selected image
			{
				image=component.getRolloverImage();	//use the rollover image
			}
		}
		if(image==null)	//if the component is not selected, or there is no selected or rollover image image
		{
			image=component.getImageURI();	//get the normal component image
		}
		return image;	//return the image we determined
	}
	
	/**Determines the rollover image to use for this component.
	This implementation returns the component's rollover image.
	@return The rollover image to use for the component, or <code>null</code> if there should be no rollover image.
	@see ImageBooleanSelectActionControl#getRolloverImage()
	*/
	protected URI getRolloverImage()
	{
		return getDepictedObject().getRolloverImage();	//get the component rollover image
	}
	
	/**Begins the rendering process.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
//TODO del		context.writeAttribute(null, "hidefocus", "true");	//hidefocus="true"	//TODO add to DTD; put in JavaScript to do this dynamically
	}

}
