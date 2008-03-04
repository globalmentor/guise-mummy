package com.guiseframework.platform.web;

import com.guiseframework.component.*;

/**Strategy for rendering a panel as an XHTML <code>&lt;div&gt;</code> element.
Changes to {@link Component#LABEL_PROPERTY} are ignored.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebPanelDepictor<C extends Panel> extends WebLayoutComponentDepictor<C>
{

	/**Default constructor.*/
	public WebPanelDepictor()
	{
		getIgnoredProperties().add(Panel.LABEL_PROPERTY);	//ignore Panel.label by default, because panels are large objects with many children but most do not show labels
	}

}
