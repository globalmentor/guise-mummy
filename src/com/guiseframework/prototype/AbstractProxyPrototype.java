/*
 * Copyright © 2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.prototype;

import java.net.URI;

import com.globalmentor.net.ContentType;

import static com.globalmentor.java.Objects.*;

import com.guiseframework.model.*;

/**Abstract prototype information that is a proxy for another prototype.
@param <P> The type of prototype being proxied.
@author Garret Wilson
*/
public class AbstractProxyPrototype<P extends Prototype & InfoModel> extends AbstractModel implements Prototype, InfoModel 
{

	/**The prototype proxied by this prototype.*/
	private P proxiedPrototype;

		/**@return The prototype proxied by this prototype.*/
		public P getProxiedPrototype() {return proxiedPrototype;}

		/**Changes the prototype being proxied.
		Although this is not a bound property, changing its value
		will cause all the relevant bound properties of the proxied
		prototype to fire property change events.
		@param newProxiedPrototype The new prototype to be proxied by this prototype.
		@see #uninstallListeners(Prototype)
		@see #installListeners(Prototype)
		@see #fireProxiedPrototypeBoundPropertyChanges(Prototype, Prototype)
		*/
		public void setProxiedPrototype(final P newProxiedPrototype)
		{
			if(proxiedPrototype!=checkInstance(newProxiedPrototype, "Proxied prototype cannot be null."))	//if the proxied prototype is really changing
			{
				final P oldProxiedPrototype=proxiedPrototype;	//get the current proxied prototype
				uninstallListeners(oldProxiedPrototype);
				proxiedPrototype=newProxiedPrototype;	//actually change the proxied prototype
				installListeners(newProxiedPrototype);
				fireProxiedPrototypeBoundPropertyChanges(oldProxiedPrototype, newProxiedPrototype);	//fire property change events for all the bound properties of the proxied prototype
			}
		}

		/**Uninstalls listeners from a proxied prototype.
		@param oldProxiedPrototype The old proxied prototype.
		*/
		protected void uninstallListeners(final P oldProxiedPrototype)
		{
			oldProxiedPrototype.removePropertyChangeListener(getRepeatPropertyChangeListener());	//stop repeating all property changes of the proxied prototype
			oldProxiedPrototype.removeVetoableChangeListener(getRepeatVetoableChangeListener());	//stop repeating all vetoable changes of the proxied prototype
		}

		/**Installs listeners to a proxied prototype.
		@param newProxiedPrototype The new proxied prototype.
		*/
		protected void installListeners(final P newProxiedPrototype)
		{
			newProxiedPrototype.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the proxied prototype
			newProxiedPrototype.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the proxied prototype
		}

		/**Fires appropriate property change events for the bound properties of the proxied prototype
		This implementation fires property change events for the following properties:
		<ul>
			<li>{@link #LABEL_PROPERTY}</li>
			<li>{@link #LABEL_CONTENT_TYPE_PROPERTY}</li>
			<li>{@link #GLYPH_URI}</li>
			<li>{@link #DESCRIPTION_PROPERTY}</li>
			<li>{@link #DESCRIPTION_CONTENT_TYPE_PROPERTY}</li>
			<li>{@link #INFO_PROPERTY}</li>
			<li>{@link #INFO_CONTENT_TYPE_PROPERTY}</li>
		</ul>
		@param oldProxiedPrototype The old proxied prototype.
		@param newProxiedPrototype The new proxied prototype.
		@throws NullPointerException if the given old proxied prototype and/or new proxied prototype is <code>null</code>.
		*/
		protected void fireProxiedPrototypeBoundPropertyChanges(final P oldProxiedPrototype, final P newProxiedPrototype)
		{
			firePropertyChange(LABEL_PROPERTY, oldProxiedPrototype.getLabel(), newProxiedPrototype.getLabel());
			firePropertyChange(LABEL_CONTENT_TYPE_PROPERTY, oldProxiedPrototype.getLabelContentType(), newProxiedPrototype.getLabelContentType());
			firePropertyChange(GLYPH_URI_PROPERTY, oldProxiedPrototype.getGlyphURI(), newProxiedPrototype.getGlyphURI());
			firePropertyChange(DESCRIPTION_PROPERTY, oldProxiedPrototype.getDescription(), newProxiedPrototype.getDescription());
			firePropertyChange(DESCRIPTION_CONTENT_TYPE_PROPERTY, oldProxiedPrototype.getDescriptionContentType(), newProxiedPrototype.getDescriptionContentType());
			firePropertyChange(INFO_PROPERTY, oldProxiedPrototype.getInfo(), newProxiedPrototype.getInfo());
			firePropertyChange(INFO_CONTENT_TYPE_PROPERTY, oldProxiedPrototype.getInfoContentType(), newProxiedPrototype.getInfoContentType());
		}

	/**@return The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.*/
	public URI getGlyphURI() {return getProxiedPrototype().getGlyphURI();}

	/**Sets the URI of the icon.
	This is a bound property of type <code>URI</code>.
	@param newLabelIcon The new URI of the icon, which may be a resource URI.
	@see #GLYPH_URI_PROPERTY
	*/
	public void setGlyphURI(final URI newLabelIcon) {getProxiedPrototype().setGlyphURI(newLabelIcon);}

	/**@return The label text, which may include a resource reference, or <code>null</code> if there is no label text.*/
	public String getLabel() {return getProxiedPrototype().getLabel();}

	/**Sets the text of the label.
	This is a bound property.
	@param newLabelText The new text of the label, which may include a resource reference.
	@see #LABEL_PROPERTY
	*/
	public void setLabel(final String newLabelText) {getProxiedPrototype().setLabel(newLabelText);}

	/**@return The content type of the label text.*/
	public ContentType getLabelContentType() {return getProxiedPrototype().getLabelContentType();}

	/**Sets the content type of the label text.
	This is a bound property.
	@param newLabelTextContentType The new label text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #LABEL_CONTENT_TYPE_PROPERTY
	*/
	public void setLabelContentType(final ContentType newLabelTextContentType) {getProxiedPrototype().setLabelContentType(newLabelTextContentType);}

	/**@return The description text, such as might appear in a flyover, or <code>null</code> if there is no description.*/
	public String getDescription() {return getProxiedPrototype().getDescription();}

	/**Sets the description text, such as might appear in a flyover.
	This is a bound property.
	@param newDescription The new text of the description, such as might appear in a flyover.
	@see #DESCRIPTION_PROPERTY
	*/
	public void setDescription(final String newDescription) {getProxiedPrototype().setDescription(newDescription);}

	/**@return The content type of the description text.*/
	public ContentType getDescriptionContentType() {return getProxiedPrototype().getDescriptionContentType();}

	/**Sets the content type of the description text.
	This is a bound property.
	@param newDescriptionContentType The new description text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #DESCRIPTION_CONTENT_TYPE_PROPERTY
	*/
	public void setDescriptionContentType(final ContentType newDescriptionContentType) {getProxiedPrototype().setDescriptionContentType(newDescriptionContentType);}

	/**@return The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.*/
	public String getInfo() {return getProxiedPrototype().getInfo();} 

	/**Sets the advisory information text, such as might appear in a tooltip.
	This is a bound property.
	@param newInfo The new text of the advisory information, such as might appear in a tooltip.
	@see #INFO_PROPERTY
	*/
	public void setInfo(final String newInfo) {getProxiedPrototype().setInfo(newInfo);}

	/**@return The content type of the advisory information text.*/
	public ContentType getInfoContentType() {return getProxiedPrototype().getInfoContentType();}

	/**Sets the content type of the advisory information text.
	This is a bound property.
	@param newInfoContentType The new advisory information text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #INFO_CONTENT_TYPE_PROPERTY
	*/
	public void setInfoContentType(final ContentType newInfoContentType) {getProxiedPrototype().setInfoContentType(newInfoContentType);}

	/**Proxied prototype constructor.
	@param proxiedPrototype The prototype proxied by this prototype.
	@exception NullPointerException if the given proxied prototype is <code>null</code> is <code>null</code>.
	*/
	public AbstractProxyPrototype(final P proxiedPrototype)
	{
		this.proxiedPrototype=checkInstance(proxiedPrototype, "Proxied prototype cannot be null.");
		installListeners(proxiedPrototype);
	}

}
