package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;

import javax.mail.internet.ContentType;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.Flash;

/**Strategy for rendering a Flash component as an XHTML <code>&lt;object&gt;</code> element.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebFlashDepictor<C extends Flash> extends AbstractSimpleWebComponentDepictor<C>
{

	/**The content type for Flash objects.*/
	public final static ContentType FLASH_CONTENT_TYPE=new ContentType(APPLICATION, X_SHOCKWAVE_FLASH_SUBTYPE, null);

	/**The "movie" parameter.*/
	public final static String MOVIE_PARAMETER="movie";
	/**The "quality" parameter.*/
	public final static String QUALITY_PARAMETER="quality";
		/**The "quality" parameter "high" value.*/
		public final static String QUALITY_PARAMETER_HIGH="high";
	/**The "wmode" parameter.*/
	public final static String WMODE_PARAMETER="wmode";

	/**The Shockwave Flash player class ID.*/
	public final static String FLASH_CLASS_ID="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000";

	/**Default constructor using the XHTML <code>&lt;object&gt;</code> element.*/
	public WebFlashDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_OBJECT);	//represent <xhtml:object>
	}

	/**Renders the body of the component.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBody() throws IOException
	{
		super.depictBody();	//render the default main part of the component
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final GuiseSession session=getSession();	//get the session
		final C component=getDepictedObject();	//get the component
		final URI flashURI=component.getFlashURI();	//get the Flash URI
		if(getPlatform().getClientProduct().getBrand()==WebUserAgentProduct.Brand.INTERNET_EXPLORER)	//if the user agent is IE, use the special attributes
		{
			depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_CLASSID, FLASH_CLASS_ID);	//classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"; only write the classid attributes for IE, because it will prevent the Flash from being loaded in Firefox
				//create a codebase URI in the form "http[s]://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0", making sure we use the same scheme so that IE6 won't complain if a secure page references a non-secure codebase
			final URI codebaseURI=createURI(depictContext.getDepictURI().getScheme(), "//download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab", "version=6,0,40,0");	//TODO use constant; allow version to be specified
			depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_CODEBASE, codebaseURI.toString());	//codebase="http[s]://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0"
		}
		else	//if the user agent is not IE, specify the object content type
		{
			depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_TYPE, FLASH_CONTENT_TYPE.toString());	//type="application/x-shockwave-flash"; don't write the type Type attribute in IE, because this will prevent IE from loading the Flash movie 			
			if(flashURI!=null)	//if there is a flash URI
			{
				depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_DATA, session.resolveURI(flashURI).toString());	//data="flashURI"; don't write the data attribute in IE, because it will prevent the Flash movie from showing its preloader
			}
		}
/*TODO fix
		context.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_WIDTH, "760px");	//width="760px"
		context.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_HEIGHT, "955px");	//height="955px"
*/
		if(flashURI!=null)	//if there is a flash URI
		{
				//param movie="flashURI" (necessary for IE)
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//<xhtml:param>
			depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_NAME, MOVIE_PARAMETER);	//name="movie"
			depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_VALUE, session.resolveURI(flashURI).toString());	//value="flashURI"
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//</xhtml:param>
		}
			//param quality="high" TODO allow customization
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//<xhtml:param>
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_NAME, QUALITY_PARAMETER);	//name="quality"
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_VALUE, QUALITY_PARAMETER_HIGH);	//value="high"
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//</xhtml:param>		
		//param wmode="opaque" TODO allow customization
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//<xhtml:param>
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_NAME, WMODE_PARAMETER);	//name="wmode"
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_VALUE, "opaque");	//value="opaque"
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_PARAM);	//</xhtml:param>		
	}
}
