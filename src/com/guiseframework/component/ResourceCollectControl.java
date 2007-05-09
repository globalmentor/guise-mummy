package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

import com.guiseframework.Bookmark;
import com.guiseframework.ResourceWriteDestination;
import com.guiseframework.model.*;

/**Control that allows resources to be collected and writes them to a given path. 
The destination path should reference a registered {@link ResourceWriteDestination} of the application.
@author Garret Wilson
*/
public class ResourceCollectControl extends AbstractControl<ResourceCollectControl>
{
	
	/**The destination path relative to the application context path, of <code>null</code> if no resources are currently being sent.*/
	private String destinationPath=null;

		/**Indicates the destination path relative to the application context path.
		@return The path representing the destination of the collected resources, or <code>null</code> if no resources are currently being sent.
		*/
		public String getDestinationPath() {return destinationPath;}

	/**The bookmark being used in sending the resources to the destination path, or <code>null</code> if there is no bookmark specified and/or no resources are currently being sent.*/
	private Bookmark bookmark=null;

		/**Reports the bookmark relative to the current destination path.
		@return The bookmark being used in sending the resources to the destination path, or <code>null</code> if there is no bookmark specified and/or no resources are currently being sent.
		*/
		public Bookmark getBookmark() {return bookmark;}	
	
	/**The bound property of whether the selected resources are being sent.*/
	public final static String SENDING_PROPERTY=getPropertyName(ResourceCollectControl.class, "sending");

	/**Whether the selected resources are being sent.*/
	private boolean sending=false;

		/**@return Whether the selected resources are being sent.*/
		public boolean isSending() {return sending;}

		/**Sets whether the selected resources are being sent.
		This is a bound property of type <code>Boolean</code>.
		@param newSending <code>true</code> if the resources are being sent, else <code>false</code>.
		@see #SENDING_PROPERTY
		*/
		protected void setSending(final boolean newSending)
		{
			if(sending!=newSending)	//if the value is really changing
			{
				final boolean oldSending=sending;	//get the current value
				sending=newSending;	//update the value
				firePropertyChange(SENDING_PROPERTY, Boolean.valueOf(oldSending), Boolean.valueOf(newSending));
			}
		}

	/**Default constructor with a default models.*/
	public ResourceCollectControl()
	{
		this(new DefaultLabelModel(), new DefaultEnableable());	//construct the class with default models
	}

	/**Label model and enableable object constructor.
	@param labelModel The component label model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model and/or enableable object is <code>null</code>.
	*/
	public ResourceCollectControl(final LabelModel labelModel, final Enableable enableable)
	{
		super(labelModel, enableable);	//construct the parent class
	}

	/**Sends collected resources to the given destination path with no bookmark.
	@param destinationPath The path representing the destination of the collected resources, or <code>null</code> if no resources are currently being sent.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	*/
	public void sendResources(final String destinationPath)
	{
		sendResources(destinationPath, null);	//send the resources with no bookmark
	}

	/**Sends collected resources to the given destination path using the given bookmark.
	@param destinationPath The path representing the destination of the collected resources, or <code>null</code> if no resources are currently being sent.
	@param bookmark The bookmark to be used in sending the resources to the destination path, or <code>null</code> if no bookmark should be used.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	*/
	public void sendResources(final String destinationPath, final Bookmark bookmark)
	{
		this.destinationPath=checkPath(destinationPath);	//save the path
		this.bookmark=bookmark;	//save the bookmark
		setSending(true);	//initiate sending
	}
}
