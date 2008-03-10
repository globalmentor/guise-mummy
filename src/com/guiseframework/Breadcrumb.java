package com.guiseframework;

import java.net.URI;

import static com.globalmentor.java.Objects.*;
import com.globalmentor.net.URIPath;

import com.guiseframework.model.DefaultLabelModel;

/**Encapsulates navigation information for particular location, such as the segment of a navigation URI.
@author Garret Wilson
*/
public class Breadcrumb extends DefaultLabelModel
{

	/**The navigation path this breadcrumb represents.*/
	private final URIPath navigationPath;

		/**@return The navigation path this breadcrumb represents.*/
		public URIPath getNavigationPath() {return navigationPath;}

	/**Navigation path constructor.
	@param navigationPath The navigation path this breadcrumb represents.
	@throws NullPointerException if the given navigation path is <code>null</code>.
	*/
	public Breadcrumb(final URIPath navigationPath)
	{
		this(navigationPath, null);	//construct the class with no label
	}

	/**Navigation path and Label constructor.
	@param navigationPath The navigation path this breadcrumb represents.
	@param labelText The text of the label, or <code>null</code> if there should be no label.
	*/
	public Breadcrumb(final URIPath navigationPath, final String labelText)
	{
		this(navigationPath, labelText, null);	//construct the label model with no glyph
	}

	/**Navigation path, label, and glyph URI constructor.
	@param navigationPath The navigation path this breadcrumb represents.
	@param labelText The text of the label, or <code>null</code> if there should be no label.
	@param glyphURI The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI.
	*/
	public Breadcrumb(final URIPath navigationPath, final String labelText, final URI glyphURI)
	{
		super(labelText, glyphURI);	//construct the parent class
		this.navigationPath=checkInstance(navigationPath, "navigation path cannot be null.");
	}

}
