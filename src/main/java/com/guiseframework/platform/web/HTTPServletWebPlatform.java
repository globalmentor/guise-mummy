/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.platform.web;

import java.net.URI;
import java.util.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.servlet.http.HTTPServlets.*;

import com.globalmentor.beans.*;
import com.globalmentor.event.ProgressListener;
import com.globalmentor.net.ContentType;
import com.globalmentor.net.URIPath;
import com.guiseframework.Bookmark;
import com.guiseframework.GuiseApplication;
import com.guiseframework.audio.Audio;
import com.guiseframework.component.*;
import com.guiseframework.component.facebook.LikeButton;
import com.guiseframework.event.ValueEvent;
import com.guiseframework.event.ValueSelectListener;
import com.guiseframework.platform.*;
import com.guiseframework.platform.web.facebook.WebIFrameLikeButtonDepictor;

/**
 * A web platform based upon an HTTP servlet.
 * @author Garret Wilson
 */
public class HTTPServletWebPlatform extends AbstractWebPlatform implements WebPlatform {

	/** The HTTP servlet session with which this platform is associated. */
	private final HttpSession httpSession;

	/** @return The HTTP servlet session with which this platform is associated. */
	public HttpSession getHTTPSession() {
		return httpSession;
	}

	/** The user agent client, such as a browser, used to access Guise on this platform. */
	private final WebUserAgentProduct clientProduct;

	@Override
	public WebUserAgentProduct getClientProduct() {
		return clientProduct;
	}

	/** The JavaScript supported on this platform, or <code>null</code> if JavaScript is not supported or the JavaScript support is not yet known. */
	private Product javascriptProduct;

	/** @return The JavaScript supported on this platform, or <code>null</code> if JavaScript is not supported or the JavaScript support is not yet known. */
	public Product getJavaScriptProduct() {
		return javascriptProduct;
	}

	/**
	 * Sets the JavaScript supported on this platform This method has package access so that JavaScript support may be updated when known.
	 * @param javascriptTroduct the JavaScript supported on this platform, or <code>null</code> if JavaScript is not supported or the JavaScript support is not
	 *          yet known.
	 */
	void setJavaScriptProduct(final Product product) {
		javascriptProduct = product;
	}

	/**
	 * Application and request constructor. This implementation updates the environment from the initial request cookies
	 * @param application The Guise application running on this platform.
	 * @param httpSession The HTTP servlet session with which this platform is associated.
	 * @param httpRequest The HTTP servlet request.
	 * @throws NullPointerException if the given application, HTTP session, and/or HTTP request is <code>null</code>.
	 */
	public HTTPServletWebPlatform(final GuiseApplication application, final HttpSession httpSession, final HttpServletRequest httpRequest) {
		super(application); //construct the parent class
		this.httpSession = requireNonNull(httpSession, "HTTP session cannot be null.");
		//update the client
		final String userAgentID = getUserAgent(httpRequest); //get the user agent identification, if any
		final Map<String, Object> userAgentProperties = getUserAgentProperties(httpRequest); //get other information about the user agent from the request
		final String userAgentName = asInstance(userAgentProperties.get(USER_AGENT_NAME_PROPERTY), String.class); //get the user agent name, if any
		final String userAgentVersion = asInstance(userAgentProperties.get(USER_AGENT_VERSION_PROPERTY), String.class); //get the user string, if any
		final Number userAgentVersionNumber = asInstance(userAgentProperties.get(USER_AGENT_VERSION_NUMBER_PROPERTY), Number.class); //get the user agent version number, if any
		final int[] userAgentVersionNumbers = (int[])userAgentProperties.get(USER_AGENT_VERSION_NUMBERS_PROPERTY); //get the user agent version numbers, if any TODO check why we can't use asArrayInstance() with Integer.TYPE
		final WebUserAgentProduct.Brand userAgentBrand = WebUserAgentProduct.Brand.getBrand(userAgentName); //get the specific brand of user agent if it's one we recognize
		final ContentType[] clientAcceptedContentTypes = getAcceptedContentTypes(httpRequest); //return the user agent accepted content types from the request
		final Locale[] clientAcceptedLanguages = getAcceptedLanguages(httpRequest); //return the user agent accepted languages from the request
		this.clientProduct = new DefaultWebUserAgentProduct(userAgentID, userAgentBrand, userAgentName, userAgentVersion,
				userAgentVersionNumber != null ? userAgentVersionNumber.doubleValue() : Double.NaN, userAgentVersionNumbers,
				unmodifiableList(asList(clientAcceptedContentTypes)), unmodifiableList(asList(clientAcceptedLanguages))); //save the client information
		javascriptProduct = null; //we don't yet know the JavaScript product
		final Environment environment = getEnvironment(); //get the environment
		final Cookie[] cookies = httpRequest.getCookies(); //get the cookies in the request
		if(cookies != null) { //if a cookie array was returned
			for(final Cookie cookie : cookies) { //for each cookie in the request
				final String cookieName = cookie.getName(); //get the name of this cookie
				//			TODO del Log.trace("Looking at cookie", cookieName, "with value", cookie.getValue());
				if(!SESSION_ID_COOKIE_NAME.equals(cookieName)) { //ignore the session ID
					environment.setProperty(cookieName, decode(cookie.getValue())); //put this cookie's decoded value into the session's environment
				}
			}
		}
		//register depictors
		//audio
		registerDepictorClass(Audio.class, WebAudioDepictor.class);
		//file upload
		registerDepictorClass(PlatformFileCollector.class, DefaultWebPlatformFileCollectorDepictor.class); //this depictor supports Flash/Google Gears		
		//components
		registerDepictorClass(AccordionMenu.class, WebAccordionMenuDepictor.class);
		registerDepictorClass(ApplicationFrame.class, WebApplicationFrameDepictor.class);
		//TODO fix custom button		registerView(Button.class, XHTMLButtonView.class);
		//TODO fix		registerView(ButtonControl.class, XHTMLCustomButtonView.class);
		registerDepictorClass(BooleanSelectToolButton.class, WebToolButtonDepictor.class);
		registerDepictorClass(ButtonControl.class, WebButtonDepictor.class);
		registerDepictorClass(CardPanel.class, WebCardPanelDepictor.class);
		registerDepictorClass(CheckControl.class, WebCheckControlDepictor.class);
		registerDepictorClass(DropMenu.class, WebDropMenuDepictor.class);
		registerDepictorClass(Flash.class, WebFlashDepictor.class);
		registerDepictorClass(Frame.class, WebFrameDepictor.class);
		registerDepictorClass(GroupPanel.class, WebFieldsetDepictor.class);
		registerDepictorClass(Heading.class, WebHeadingDepictor.class);
		registerDepictorClass(HeadingLink.class, WebHeadingLinkDepictor.class);
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
		registerDepictorClass(TextBox.class, WebTextBoxDepictor.class);
		registerDepictorClass(TextControl.class, WebTextControlDepictor.class);
		registerDepictorClass(ToolButton.class, WebToolButtonDepictor.class);
		registerDepictorClass(TreeControl.class, WebTreeControlDepictor.class);
		registerDepictorClass(ValueSelectLink.class, WebValueSelectLinkDepictor.class);
		//Facebook
		registerDepictorClass(LikeButton.class, WebIFrameLikeButtonDepictor.class);
	}

	/** The current depict context. */
	private WebDepictContext depictContext = null;

	/**
	 * Sets the depict context. This method has package access so that the depict context can be set when necessary by the appropriate platform classes.
	 * @param depictContext The new depict context, or <code>null</code> if there should be no depict context.
	 * @throws IllegalStateException if a depict context was given and there already exists a depict context.
	 */
	void setDepictContext(final WebDepictContext depictContext) {
		if(depictContext != null && this.depictContext != null) { //if a non-null depict context is being used to replace another depict context
			throw new IllegalStateException("Depict context cannot be replaced unless previous depict context is first removed.");
		}
		this.depictContext = depictContext; //change the depict context
	}

	@Override
	public WebDepictContext getDepictContext() {
		final WebDepictContext depictContext = this.depictContext; //get the depict context
		if(depictContext == null) { //if there is no depict context
			throw new IllegalStateException("No depict context is available.");
		}
		return depictContext; //return the depict context
	}

	private PlatformFileCollector fileReferenceList = null; //TODO finish; comment

	@Override
	public void selectPlatformFiles(final boolean multiple, final ValueSelectListener<Collection<PlatformFile>> platformFileSelectListener) {
		fileReferenceList = new PlatformFileCollector(); //create a new platform file collector
		requireNonNull(platformFileSelectListener, "Platform file select listener cannot be null.");
		fileReferenceList.addPropertyChangeListener(PlatformFileCollector.PLATFORM_FILES_PROPERTY, new AbstractGenericPropertyChangeListener<List<PlatformFile>>() { //listen for the files changing

			@Override
			public void propertyChange(final GenericPropertyChangeEvent<List<PlatformFile>> genericPropertyChangeEvent) { //if the user selects platform files
				fileReferenceList.removePropertyChangeListener(PlatformFileCollector.PLATFORM_FILES_PROPERTY, this); //we don't need to listen for the files anymore
				platformFileSelectListener
						.valueSelected(new ValueEvent<Collection<PlatformFile>>(HTTPServletWebPlatform.this, genericPropertyChangeEvent.getNewValue())); //report the new value to the listener
			}

		});
		fileReferenceList.browse(); //tell the file reference list to start browsing
	}

	/**
	 * Uploads files from the platform.
	 * @param destinationPath The path representing the destination of the platform files, relative to the application.
	 * @param destinationBookmark The bookmark to be used in uploading the platform files to the destination path, or <code>null</code> if no bookmark should be
	 *          used.
	 * @param progressListener The listener that will be notified when progress is made for a particular platform file upload.
	 * @param platformFiles That platform files to upload.
	 * @throws NullPointerException if the given destination path and/or listener is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	 * @throws IllegalArgumentException if the provided path is absolute.
	 * @throws IllegalStateException if one or more of the specified platform files can no longer be uploaded because, for example, other platform files have
	 *           since been selected.
	 */
	public void uploadPlatformFiles(final String destinationPath, final Bookmark destinationBookmark, final ProgressListener progressListener,
			final PlatformFile... platformFiles) { //TODO del if not needed
		if(fileReferenceList == null) { //if no Flash file reference list has been created
			throw new IllegalStateException("No Flash file reference list exists.");
		}
		/*TODO fix
					this.destinationPath=checkRelativePath(destinationPath);	//save the path
					this.destinationBookmark=destinationBookmark;	//save the bookmark
				
				fileReferenceList=new FlashFileReferenceList();	//create a new flash file reference list
				requireNonNull(platformFileSelectListener, "Platform file select listener cannot be null.");
				fileReferenceList.addPropertyChangeListener(FlashFileReferenceList.PLATFORM_FILES_PROPERTY, new AbstractGenericPropertyChangeListener<List<PlatformFile>>() {	//listen for the files changing
							public void propertyChange(final GenericPropertyChangeEvent<List<PlatformFile>> genericPropertyChangeEvent) {	//if the user selects platform files
								fileReferenceList.removePropertyChangeListener(FlashFileReferenceList.PLATFORM_FILES_PROPERTY, this);	//we don't need to listen for the files anymore
								platformFileSelectListener.valueSelected(new ValueEvent<Collection<PlatformFile>>(HTTPServletWebPlatform.this, genericPropertyChangeEvent.getNewValue()));	//report the new value to the listener
							}
						});
				fileReferenceList.browse();	//tell the file reference list to start browsing
		*/
	}

	/** The absolute or application-relative URI of the resource to sent to the platform, or <code>null</code> if there is no resource to be sent. */
	private URI sendResourceURI = null;

	/** @return The absolute or application-relative URI of the resource to sent to the platform, or <code>null</code> if there is no resource to be sent. */
	URI getSendResourceURI() {
		return sendResourceURI;
	}

	/** Clears the record of the resource to be sent. */
	void clearSendResourceURI() {
		sendResourceURI = null;
	}

	@Override
	public void sendResource(final URIPath resourcePath) {
		sendResource(resourcePath, null); //send the resource with no bookmark
	}

	@Override
	public void sendResource(final URI resourceURI) {
		sendResource(resourceURI, null); //send the resource with no bookmark
	}

	@Override
	public void sendResource(final URIPath resourcePath, final Bookmark bookmark) {
		sendResource(resourcePath.toURI(), bookmark); //send the requested URI, converting the path to a URI
	}

	/**
	 * Sends a resource to the platform.
	 * @param resourceURI The URI of the resource to send, relative to the application.
	 * @param bookmark The bookmark at the given path, or <code>null</code> if there is no bookmark.
	 * @throws NullPointerException if the given URI is <code>null</code>.
	 */
	public void sendResource(final URI resourceURI, final Bookmark bookmark) { //TODO resolve whether resource URI can have bookmark information; currently some code relies on allowing bookmark information in the URI
		sendResourceURI = requireNonNull(resourceURI, "Resource URI cannot be null."); //save the resource; it will be resolved when depicted
		if(bookmark != null) { //if a bookmark was provided
			sendResourceURI = URI.create(sendResourceURI.toString() + bookmark.toString()); //append the bookmark query
		}
	}
}
