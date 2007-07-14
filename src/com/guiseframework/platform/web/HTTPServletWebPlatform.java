package com.guiseframework.platform.web;

import java.net.URI;
import java.util.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import javax.mail.internet.ContentType;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.servlet.http.HttpServletConstants.*;

import com.garretwilson.beans.*;
import com.garretwilson.event.ProgressListener;
import com.guiseframework.Bookmark;
import com.guiseframework.GuiseApplication;
import com.guiseframework.audio.Audio;
import com.guiseframework.component.*;
import com.guiseframework.event.ValueEvent;
import com.guiseframework.event.ValueSelectListener;
import com.guiseframework.platform.*;

/**A web platform based upon an HTTP servlet.
@author Garret Wilson
*/
public class HTTPServletWebPlatform extends AbstractWebPlatform implements WebPlatform
{

	/**The user agent client, such as a browser, used to access Guise on this platform.*/
	private final WebUserAgentProduct clientProduct;

		/**@return The user agent client, such as a browser, used to access Guise on this platform.*/
		public WebUserAgentProduct getClientProduct() {return clientProduct;}

	/**The JavaScript supported on this platform, or <code>null</code> if JavaScript is not supported or the JavaScript support is not yet known.*/
	private Product javascriptProduct;

		/**@return The JavaScript supported on this platform, or <code>null</code> if JavaScript is not supported or the JavaScript support is not yet known.*/
		public Product getJavaScriptProduct() {return javascriptProduct;}

		/**Sets the JavaScript supported on this platform
		This method has package access so that JavaScript support may be updated when known.
		@param javascriptTroduct the JavaScript supported on this platform, or <code>null</code> if JavaScript is not supported or the JavaScript support is not yet known.
		*/
		void setJavaScriptProduct(final Product product) {javascriptProduct=product;}

	/**Application and request constructor.
	This implementation updates the environment from the initial request cookies
	@param application The Guise application running on this platform.
	@param request The HTTP servlet request.
	@exception NullPointerException if the given application and/or environment is <code>null</code>.
	*/
	public HTTPServletWebPlatform(final GuiseApplication application, final HttpServletRequest request)
	{
		super(application);	//construct the parent class
			//update the client
		final String userAgentID=getUserAgent(request);	//get the user agent identification, if any
		final Map<String, Object> userAgentProperties=getUserAgentProperties(request);	//get other information about the user agent from the request
		final String userAgentName=asInstance(userAgentProperties.get(USER_AGENT_NAME_PROPERTY), String.class);	//get the user agent name, if any
		final String userAgentVersion=asInstance(userAgentProperties.get(USER_AGENT_VERSION_PROPERTY), String.class);	//get the user string, if any
		final Number userAgentVersionNumber=asInstance(userAgentProperties.get(USER_AGENT_VERSION_NUMBER_PROPERTY), Number.class);	//get the user agent version number, if any
		final int[] userAgentVersionNumbers=(int[])userAgentProperties.get(USER_AGENT_VERSION_NUMBERS_PROPERTY);	//get the user agent version numbers, if any TODO check why we can't use asArrayInstance() with Integer.TYPE
		final WebUserAgentProduct.Brand userAgentBrand=WebUserAgentProduct.Brand.getBrand(userAgentName);	//get the specific brand of user agent if it's one we recognize
		final ContentType[] clientAcceptedContentTypes=getAcceptedContentTypes(request);	//return the user agent accepted content types from the request
		final Locale[] clientAcceptedLanguages=getAcceptedLanguages(request);	//return the user agent accepted languages from the request
		this.clientProduct=new DefaultWebUserAgentProduct(userAgentID, userAgentBrand, userAgentName, userAgentVersion, userAgentVersionNumber!=null ? userAgentVersionNumber.doubleValue() : Double.NaN, userAgentVersionNumbers, unmodifiableList(asList(clientAcceptedContentTypes)), unmodifiableList(asList(clientAcceptedLanguages)));	//save the client information
		javascriptProduct=null;	//we don't yet know the JavaScript product
		final Environment environment=getEnvironment();	//get the environment
		final Cookie[] cookies=request.getCookies();	//get the cookies in the request
		if(cookies!=null)	//if a cookie array was returned
		{
			for(final Cookie cookie:cookies)	//for each cookie in the request
			{
				final String cookieName=cookie.getName();	//get the name of this cookie
//			TODO del Debug.trace("Looking at cookie", cookieName, "with value", cookie.getValue());
				if(!SESSION_ID_COOKIE_NAME.equals(cookieName))	//ignore the session ID
				{
					environment.setProperty(cookieName, decode(cookie.getValue()));	//put this cookie's decoded value into the session's environment
				}
			}
		}
			//register depictors
				//audio
		registerDepictorClass(Audio.class, WebAudioDepictor.class);		
				//Flash
		registerDepictorClass(FlashFileReferenceList.class, WebFlashFileReferenceListDepictor.class);		
				//components
		registerDepictorClass(AccordionMenu.class, WebAccordionMenuDepictor.class);
		registerDepictorClass(ApplicationFrame.class, WebApplicationFrameDepictor.class);
//TODO fix custom button		registerView(Button.class, XHTMLButtonView.class);
//TODO fix		registerView(ButtonControl.class, XHTMLCustomButtonView.class);
		registerDepictorClass(ButtonControl.class, WebButtonDepictor.class);
		registerDepictorClass(CardPanel.class, WebCardPanelDepictor.class);
		registerDepictorClass(CheckControl.class, WebCheckControlDepictor.class);
		registerDepictorClass(DropMenu.class, WebDropMenuDepictor.class);
		registerDepictorClass(Flash.class, WebFlashDepictor.class);
		registerDepictorClass(Frame.class, WebFrameDepictor.class);
		registerDepictorClass(GroupPanel.class, WebFieldsetDepictor.class);
		registerDepictorClass(Heading.class, WebHeadingDepictor.class);
		registerDepictorClass(ImageComponent.class, WebImageDepictor.class);
		registerDepictorClass(ImageActionControl.class, WebImageActionControlDepictor.class);
		registerDepictorClass(ImageBooleanSelectActionControl.class, WebImageBooleanSelectActionControlViewer.class);
		registerDepictorClass(LabelComponent.class, WebLabelDepictor.class);
		registerDepictorClass(LayoutComponent.class, WebLayoutComponentDepictor.class);
		registerDepictorClass(ListControl.class, WebSelectDepictor.class);
		registerDepictorClass(LinkControl.class, WebLinkDepictor.class);
		registerDepictorClass(Message.class, WebMessageDepictor.class);
		registerDepictorClass(Panel.class, WebPanelDepictor.class);
		registerDepictorClass(Picture.class, WebPictureDepictor.class);
		registerDepictorClass(ResourceCollectControl.class, WebResourceCollectDepictor.class);		
		registerDepictorClass(ResourceImportControl.class, WebFileInputDepictor.class);		
		registerDepictorClass(ScrollControl.class, WebScrollControlDepictor.class);
		registerDepictorClass(SelectLinkControl.class, WebSelectLinkDepictor.class);
		registerDepictorClass(SelectableLabel.class, WebSelectableLabelDepictor.class);
		registerDepictorClass(SliderControl.class, WebSliderDepictor.class);
		registerDepictorClass(TabbedPanel.class, WebTabbedPanelDepictor.class);
		registerDepictorClass(TabContainerControl.class, WebTabDepictor.class);
		registerDepictorClass(TabControl.class, WebTabDepictor.class);
		registerDepictorClass(Table.class, WebTableDepictor.class);
		registerDepictorClass(Text.class, WebTextDepictor.class);
		registerDepictorClass(TextControl.class, WebTextControlDepictor.class);
		registerDepictorClass(TreeControl.class, WebTreeControlDepictor.class);
		registerDepictorClass(ValueSelectLink.class, WebValueSelectLinkDepictor.class);		
	}

	/**The current depict context.*/
	private WebDepictContext depictContext=null;

	/**Sets the depict context.
	This method has package access so that the depict context can be set when necessary by the appropriate platform classes.
	@param depictContext The new depict context, or <code>null</code> if there should be no depict context.
	@exception IllegalStateException if a depict context was given and there already exists a depict context.
	*/
	void setDepictContext(final WebDepictContext depictContext)
	{
		if(depictContext!=null && this.depictContext!=null)	//if a non-null depict context is being used to replace another depict context
		{
			throw new IllegalStateException("Depict context cannot be replaced unless previous depict context is first removed.");
		}
		this.depictContext=depictContext;	//change the depict context
	}

	/**Retrieves information and functionality related to the current depiction.
	@return A context for the current depiction.
	@exception IllegalStateException if no depict context can be returned in the current depiction state.
	*/
	public WebDepictContext getDepictContext()
	{
		final WebDepictContext depictContext=this.depictContext;	//get the depict context
		if(depictContext==null)	//if there is no depict context
		{
			throw new IllegalStateException("No depict context is available.");
		}
		return depictContext;	//return the depict context
	}


	private FlashFileReferenceList fileReferenceList=null;	//TODO finish; comment
	

	/**Selects one or more files on the platform, using the appropriate selection functionality for the platform.
	@param multiple Whether multiple files should be allowed to be selected.
	@param platformFileSelectListener The listener that will be notified when platform files are selected.
	@exception NullPointerException if the given listener is <code>null</code>.
	*/
	public void selectPlatformFiles(final boolean multiple, final ValueSelectListener<Collection<PlatformFile>> platformFileSelectListener)
	{
		fileReferenceList=new FlashFileReferenceList();	//create a new flash file reference list
		checkInstance(platformFileSelectListener, "Platform file select listener cannot be null.");
		fileReferenceList.addPropertyChangeListener(FlashFileReferenceList.PLATFORM_FILES_PROPERTY, new AbstractGenericPropertyChangeListener<List<PlatformFile>>()	//listen for the files changing
				{
					public void propertyChange(final GenericPropertyChangeEvent<List<PlatformFile>> genericPropertyChangeEvent)	//if the user selects platform files
					{
						fileReferenceList.removePropertyChangeListener(FlashFileReferenceList.PLATFORM_FILES_PROPERTY, this);	//we don't need to listen for the files anymore
						platformFileSelectListener.valueSelected(new ValueEvent<Collection<PlatformFile>>(HTTPServletWebPlatform.this, genericPropertyChangeEvent.getNewValue()));	//report the new value to the listener
					}
				});
		fileReferenceList.browse();	//tell the file reference list to start browsing
	}

	/**Uploads files from the platform.
	@param destinationPath The path representing the destination of the platform files, relative to the application.
	@param destinationBookmark The bookmark to be used in uploading the platform files to the destination path, or <code>null</code> if no bookmark should be used.
	@param progressListener The listener that will be notified when progress is made for a particular platform file upload.
	@param platformFiles Thet platform files to upload.
	@exception NullPointerException if the given destination path and/or listener is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalStateException if one or more of the specified platform files can no longer be uploaded because, for example, other platform files have since been selected.	
	*/
	public void uploadPlatformFiles(final String destinationPath, final Bookmark destinationBookmark, final ProgressListener progressListener, final PlatformFile... platformFiles)
	{
		if(fileReferenceList==null)	//if no Flash file reference list has been created
		{
			throw new IllegalStateException("No Flash file reference list exists.");
		}
/*TODO fix
			this.destinationPath=checkRelativePath(destinationPath);	//save the path
			this.destinationBookmark=destinationBookmark;	//save the bookmark
		
		fileReferenceList=new FlashFileReferenceList();	//create a new flash file reference list
		checkInstance(platformFileSelectListener, "Platform file select listener cannot be null.");
		fileReferenceList.addPropertyChangeListener(FlashFileReferenceList.PLATFORM_FILES_PROPERTY, new AbstractGenericPropertyChangeListener<List<PlatformFile>>()	//listen for the files changing
				{
					public void propertyChange(final GenericPropertyChangeEvent<List<PlatformFile>> genericPropertyChangeEvent)	//if the user selects platform files
					{
						fileReferenceList.removePropertyChangeListener(FlashFileReferenceList.PLATFORM_FILES_PROPERTY, this);	//we don't need to listen for the files anymore
						platformFileSelectListener.valueSelected(new ValueEvent<Collection<PlatformFile>>(HTTPServletWebPlatform.this, genericPropertyChangeEvent.getNewValue()));	//report the new value to the listener
					}
				});
		fileReferenceList.browse();	//tell the file reference list to start browsing
*/
	}

	/**The URI of the resource to sent to the platform, or <code>null</code> if there is no resource to be sent.*/
	private URI sendResourceURI=null;

		/**@return The URI of the resource to sent to the platform, or <code>null</code> if there is no resource to be sent.*/
		URI getSendResourceURI() {return sendResourceURI;}

		/**Clears the record of the resource to be sent.*/
		void clearSendResourceURI() {sendResourceURI=null;}

	/**Sends a resource to the platform.
	@param resourcePath The path of the resource to send, relative to the application.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the given string is not a path.
	*/
	public void sendResource(final String resourcePath)
	{
		sendResource(resourcePath, null);	//send the resource with no bookmark
	}

	/**Sends a resource to the platform.
	@param resourceURI The URI of the resource to send, relative to the application.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public void sendResource(final URI resourceURI)
	{
		sendResource(resourceURI, null);	//send the resource with no bookmark
	}

	/**Sends a resource to the platform with the specified bookmark.
	@param resourcePath The path of the resource to send, relative to the application.
	@param bookmark The bookmark at the given path, or <code>null</code> if there is no bookmark.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the given string is not a path.
	*/
	public void sendResource(final String resourcePath, final Bookmark bookmark)
	{
		sendResource(createPathURI(resourcePath), bookmark);	//send the requested URI, converting the path to a URI and verifying that it is only a path
	}

	/**Sends a resource to the platform.
	@param resourceURI The URI of the resource to send, relative to the application.
	@param bookmark The bookmark at the given path, or <code>null</code> if there is no bookmark.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public void sendResource(final URI resourceURI, final Bookmark bookmark)
	{
		sendResourceURI=getApplication().resolveURI(checkInstance(resourceURI, "Resource URI cannot be null."));	//resolve the URI and save the resource
		if(bookmark!=null)	//if a bookmark was provided
		{
			sendResourceURI=URI.create(sendResourceURI.toString()+bookmark.toString());	//append the bookmark query
		}
	}
}
