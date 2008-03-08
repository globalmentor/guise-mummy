package com.guiseframework.component;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import static java.util.Collections.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.mail.internet.ContentType;

import static com.globalmentor.java.Objects.*;
import com.globalmentor.beans.*;
import com.guiseframework.input.*;

/**Abstract implementation of an application frame.
<p>This implementation binds the command {@link ProcessCommand#CONTINUE} to the key input {@link Key#ENTER},
as well as the command {@link ProcessCommand#ABORT} to the key input {@link Key#ESCAPE}.</p>
@author Garret Wilson
@see LayoutPanel
*/
public abstract class AbstractApplicationFrame extends AbstractFrame implements ApplicationFrame
{

	/**The list of child frames according to z-order; this will not be initialized with a non-null value until the constructor is finished.*/
	private final List<Frame> frameList;

		/**@return An iterable to all child frames.*/
		public Iterable<Frame> getChildFrames() {return unmodifiableList(frameList);}
	
		/**Adds a frame to the list of child frames.
		This method should usually only be called by the frames themselves.
		@param frame The frame to add.
		@exception NullPointerException if the given frame is <code>null</code>.
		@exception IllegalArgumentException if the given frame is this frame.
		*/
		public void addChildFrame(final Frame frame)
		{
			if(checkInstance(frame, "Frame cannot be null.")==this)
			{
				throw new IllegalArgumentException("A frame cannot be its own child frame.");
			}			
			frameList.add(frame);	//add this frame to our list
			addComponent(frame);	//add the frame as a child of this component
			try
			{
				setInputFocusedComponent(frame);	//set the new frame as the focus component
			}
			catch(final PropertyVetoException propertyVetoException)	//if we can't focus on the new frame, ignore the error
			{
			}
		}

		/**Removes a frame from the list of child frames.
		This method should usually only be called by the frames themselves.
		@param frame The frame to remove.
		@exception NullPointerException if the given frame is <code>null</code>.
		@exception IllegalArgumentException if the given frame is the application frame.
		*/
		public void removeChildFrame(final Frame frame)
		{
			if(checkInstance(frame, "Frame cannot be null.")==this)
			{
				throw new IllegalArgumentException("A frame cannot be its own child frame.");
			}
			frameList.remove(frame);	//remove this frame from the list
			removeComponent(frame);	//remove the frame as a child of this component
			final InputFocusableComponent oldFocusedComponent=getInputFocusedComponent();	//get the focused component
			if(frame==oldFocusedComponent)	//if we just removed the focused component
			{
				final InputFocusableComponent newFocusedComponent=frameList.isEmpty() ? null : frameList.get(frameList.size()-1);	//if we have more frames, set the focus to the last one TODO important fix race condition in finding the last frame
				try
				{
					setInputFocusedComponent(newFocusedComponent);	//set the new frame as the focus component
				}
				catch(final PropertyVetoException propertyVetoException)	//if we can't focus on the new frame
				{
					throw new AssertionError("Cannot focus on remaining frame.");
					//TODO fix to focus on an application frame child component, especially if there are no frames left
				}
			}
		}

	/**Listener that listens for the current content component's label to change, and updates the application frame label in response.
	@see #updateLabel()
	*/
	private final PropertyChangeListener contentLabelChangeUpdateLabelListener=new AbstractGenericPropertyChangeListener<String>() {
		public void propertyChange(final GenericPropertyChangeEvent<String> genericPropertyChangeEvent)	//if the content title changes
		{
			updateLabel();	//update the label
		};
	};

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	*/
	public AbstractApplicationFrame(final Component component)
	{
		super(component);	//construct the parent class
		frameList=new CopyOnWriteArrayList<Frame>();	//initialize the frame list	
		final BindingInputStrategy bindingInputStrategy=new BindingInputStrategy(getInputStrategy());	//create a new input strategy based upon the current input strategy (if any)
		bindingInputStrategy.bind(new KeystrokeInput(Key.ENTER), new CommandInput(ProcessCommand.CONTINUE));	//map the Enter key to the "continue" command
		bindingInputStrategy.bind(new KeystrokeInput(Key.ESCAPE), new CommandInput(ProcessCommand.ABORT));	//map the Escape key to the "abort" command
		setInputStrategy(bindingInputStrategy);	//switch to our new input strategy
	}

	/**Sets the content child component.
	This is a bound property.
	This version updates the frame label by calling {@link #updateLabel()}.
	@param newContent The content child component, or <code>null</code> if this frame does not have a content child component.
	*/
	public void setContent(final Component newContent)
	{
		final Component oldContent=getContent();	//set the previous content
		if(oldContent!=newContent)	//if the content will really change
		{
			if(oldContent!=null)	//if we had a content component
			{
				oldContent.removePropertyChangeListener(Component.LABEL_PROPERTY, contentLabelChangeUpdateLabelListener);	//stop listening for its label to change
			}
			if(newContent!=null)	//if we have a new content component
			{
				newContent.addPropertyChangeListener(Component.LABEL_PROPERTY, contentLabelChangeUpdateLabelListener);	//listen for the content's label changing, and update our label in response
			}
			super.setContent(newContent);	//set the new content
			updateLabel();	//update the label
		}
	}

	/**The delimiter to use when constructing the label from its various segments.*/
	public final static String LABEL_SEPARATOR=" > ";

	/**Retrieves the plain-text base title to use when constructing a label.
	@return A base plain-text string to use when constructing a label, or <code>null</code> if there is no base label.
	@see #updateLabel()
	*/
	protected abstract String getBasePlainLabel();

	/**Called when the content changes so that the label can be updated.
	This version sets the application frame label to match the label of the content, if any,
	prefixed with a base label, if any.
	If the label content type is not plain text, the base label is ignored.
	@see #getContent()
	@see #getBasePlainLabel()
	@see #setLabel(String)
	@see #setLabelContentType(ContentType)
	*/
	protected void updateLabel()
	{
		final Component content=getContent();	//set the content, if any
		final String label;
		final ContentType labelContentType;
		if(content!=null)	//if we have content
		{
			final String contentLabel=content.getLabel();	//get the content label, if any
			final ContentType contentLabelContentType=content.getLabelContentType();	//get the content label content type
			if(contentLabel==null || PLAIN_TEXT_CONTENT_TYPE.match(contentLabelContentType) || contentLabel==null)	//if there is no content label or the content label is plain text, we can use plain text for the entire label
			{
				final String basePlainLabel=getBasePlainLabel();	//see if there is a base label
				if(basePlainLabel!=null || contentLabel!=null)	//if we have a base label or content label
				{
					final StringBuilder labelStringBuilder=new StringBuilder();
					if(basePlainLabel!=null)	//if there is a base label
					{
						labelStringBuilder.append(basePlainLabel);	//start with the base label
					}
					if(contentLabel!=null)	//if the content has a label
					{
						if(labelStringBuilder.length()>0)	//if there is already label content
						{
							labelStringBuilder.append(LABEL_SEPARATOR);	//separate the label components
						}
						labelStringBuilder.append(contentLabel);	//append the content label
					}
					label=labelStringBuilder.toString();	//use the label we constructed
					labelContentType=PLAIN_TEXT_CONTENT_TYPE;	//we'll use plain text for the label
				}
				else	//if we have no label text at all
				{
					label=null;	//don't use a label
					labelContentType=getLabelContentType();	//stay with the current content type
				}
			}
			else	//if we can't use plain text
			{
				label=contentLabel;	//use the content label
				labelContentType=contentLabelContentType;	//use the content label content type
			}
		}
		else	//if we have no content
		{
			label=null;	//don't use a label
			labelContentType=null;
		}
		setLabel(label);	//set the application frame label
		setLabelContentType(labelContentType);	//set the application frame label content type
	}

	/**Determines whether the frame should be allowed to close.
	This implementation returns <code>false</code>.
	This method is called from {@link #close()}.
	@return <code>true</code> if the frame should be allowed to close.
	*/
	public boolean canClose()
	{
		return false;	//don't allow application frames to be closed
	}

	/**Retrieves a list of all child components.
	This version adds all this frame's child frames to the list.
	@return A list of child components.
	@see #getChildFrames()
	*/
	protected List<Component> getChildList()
	{
		final List<Component> childList=super.getChildList();	//get the default list of children
		if(frameList!=null)	//if the frame list has been initialized (getChildList() can be called by an ancestor class during construction, before the frame list has been initialized)
		{
			childList.addAll(frameList);	//add all child frames to the list
		}
		return childList;	//return the list of children, now including child frames
	}
	
	/**Determines whether this component has children.
	This version also checks to see whether there are child frames.
	@return Whether this component has children.
	@see #getChildFrames()
	 */
	public boolean hasChildComponents()
	{
		return super.hasChildComponents() || !frameList.isEmpty();	//see if we have normal children and/or frames
	}

}
