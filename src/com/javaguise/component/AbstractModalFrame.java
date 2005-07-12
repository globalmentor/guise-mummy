package com.javaguise.component;

import com.javaguise.model.DefaultLabelModel;
import com.javaguise.model.LabelModel;
import com.javaguise.session.GuiseSession;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.Debug;

/**Abstract implementation of a modal frame.
@param <R> The type of modal result this modal frame produces.
@author Garret Wilson
*/
public abstract class AbstractModalFrame<R, C extends ModalFrame<R, C>> extends AbstractFrame<C> implements ModalFrame<R, C>
{

	/**The result of this frame's modal interaction, or <code>null</code> if no result is given.*/
	private R result=null;

		/**@return The result of this frame's modal interaction, or <code>null</code> if no result is given.*/
		public R getResult() {return result;}

		/**Sets the modal result.
		This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
		@param newResult The new result of this frame's modal interaction, or <code>null</code> if no result is given.
		@see ModalFrame#RESULT_PROPERTY
		*/
		public void setResult(final R newResult)
		{
			if(!ObjectUtilities.equals(result, newResult))	//if the value is really changing (compare their values, rather than identity)
			{
				final R oldResult=result;	//get the old value
				result=newResult;	//actually change the value
				firePropertyChange(RESULT_PROPERTY, oldResult, newResult);	//indicate that the value changed
			}
		}

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractModalFrame(final GuiseSession<?> session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractModalFrame(final GuiseSession<?> session, final LabelModel model)
	{
		this(session, (String)null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractModalFrame(final GuiseSession<?> session, final String id)
	{
		this(session, id, (Component<?>)null);	//default to no content component
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractModalFrame(final GuiseSession<?> session, final String id, final LabelModel model)
	{
		this(session, id, null, model);	//default to flowing vertically
	}

	/**Session and content component constructor.
	@param session The Guise session that owns this component.
	@param content The component representing this frame's content, <code>null</code> if this frame has no content.
	@exception NullPointerException if the given session and/or content component is <code>null</code>.
	*/
	public AbstractModalFrame(final GuiseSession<?> session, final Component<?> content)
	{
		this(session, null, content);	//construct the component with the content, indicating that a default ID should be used
	}

	/**Session, content component, and model constructor.
	@param session The Guise session that owns this component.
	@param content The component representing this frame's content, <code>null</code> if this frame has no content.
	@param model The component data model.
	@exception NullPointerException if the given session, content component, and/or model is <code>null</code>.
	*/
	public AbstractModalFrame(final GuiseSession<?> session, final Component<?> content, final LabelModel model)
	{
		this(session, null, content, model);	//construct the component with the content, indicating that a default ID should be used
	}

	/**Session, ID, and content component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param content The component representing this frame's content, <code>null</code> if this frame has no content.
	@exception NullPointerException if the given session and/or content component is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractModalFrame(final GuiseSession<?> session, final String id, final Component<?> content)
	{
		this(session, id, content, new DefaultLabelModel(session));	//construct the class with a default model
	}

	/**Session, ID, content component, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param content The component representing this frame's content, <code>null</code> if this frame has no content.
	@param model The component data model.
	@exception NullPointerException if the given session, content component, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractModalFrame(final GuiseSession<?> session, final String id, final Component<?> content, final LabelModel model)
	{
		super(session, id, content, model);	//construct the parent class
	}

	/**Ends this frame's modal interaction and navigates either to the previous modal navigation or to this frame's referring URI, if any.
	@param result The result of this frame's modal interaction, or <code>null</code> if no result is given.
	@see #setResult(R)
	@see GuiseSession#endModalNavigation(ModalFrame)
	*/
	public void endModal(final R result)
	{
Debug.trace("ready to set modal result");
		setResult(result);	//update the result
Debug.trace("ready to end navigation");
		getSession().endModalNavigation(this);	//end modal navigation for this modal frame
	}
}
