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

/**
 * Abstract prototype information that is a proxy for another prototype.
 * @param <P> The type of prototype being proxied.
 * @author Garret Wilson
 */
public class AbstractProxyPrototype<P extends Prototype & InfoModel> extends AbstractModel implements Prototype, InfoModel {

	/** The prototype proxied by this prototype. */
	private P proxiedPrototype;

	/** @return The prototype proxied by this prototype. */
	public P getProxiedPrototype() {
		return proxiedPrototype;
	}

	/**
	 * Changes the prototype being proxied. Although this is not a bound property, changing its value will cause all the relevant bound properties of the proxied
	 * prototype to fire property change events.
	 * @param newProxiedPrototype The new prototype to be proxied by this prototype.
	 * @see #uninstallListeners(Prototype)
	 * @see #installListeners(Prototype)
	 * @see #fireProxiedPrototypeBoundPropertyChanges(Prototype, Prototype)
	 */
	public void setProxiedPrototype(final P newProxiedPrototype) {
		if(proxiedPrototype != checkInstance(newProxiedPrototype, "Proxied prototype cannot be null.")) { //if the proxied prototype is really changing
			final P oldProxiedPrototype = proxiedPrototype; //get the current proxied prototype
			uninstallListeners(oldProxiedPrototype);
			proxiedPrototype = newProxiedPrototype; //actually change the proxied prototype
			installListeners(newProxiedPrototype);
			fireProxiedPrototypeBoundPropertyChanges(oldProxiedPrototype, newProxiedPrototype); //fire property change events for all the bound properties of the proxied prototype
		}
	}

	/**
	 * Uninstalls listeners from a proxied prototype.
	 * @param oldProxiedPrototype The old proxied prototype.
	 */
	protected void uninstallListeners(final P oldProxiedPrototype) {
		oldProxiedPrototype.removePropertyChangeListener(getRepeatPropertyChangeListener()); //stop repeating all property changes of the proxied prototype
		oldProxiedPrototype.removeVetoableChangeListener(getRepeatVetoableChangeListener()); //stop repeating all vetoable changes of the proxied prototype
	}

	/**
	 * Installs listeners to a proxied prototype.
	 * @param newProxiedPrototype The new proxied prototype.
	 */
	protected void installListeners(final P newProxiedPrototype) {
		newProxiedPrototype.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the proxied prototype
		newProxiedPrototype.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the proxied prototype
	}

	/**
	 * Fires appropriate property change events for the bound properties of the proxied prototype This implementation fires property change events for the
	 * following properties:
	 * <ul>
	 * <li>{@link #LABEL_PROPERTY}</li>
	 * <li>{@link #LABEL_CONTENT_TYPE_PROPERTY}</li>
	 * <li>{@link #GLYPH_URI_PROPERTY}</li>
	 * <li>{@link #DESCRIPTION_PROPERTY}</li>
	 * <li>{@link #DESCRIPTION_CONTENT_TYPE_PROPERTY}</li>
	 * <li>{@link #INFO_PROPERTY}</li>
	 * <li>{@link #INFO_CONTENT_TYPE_PROPERTY}</li>
	 * </ul>
	 * @param oldProxiedPrototype The old proxied prototype.
	 * @param newProxiedPrototype The new proxied prototype.
	 * @throws NullPointerException if the given old proxied prototype and/or new proxied prototype is <code>null</code>.
	 */
	protected void fireProxiedPrototypeBoundPropertyChanges(final P oldProxiedPrototype, final P newProxiedPrototype) {
		firePropertyChange(LABEL_PROPERTY, oldProxiedPrototype.getLabel(), newProxiedPrototype.getLabel());
		firePropertyChange(LABEL_CONTENT_TYPE_PROPERTY, oldProxiedPrototype.getLabelContentType(), newProxiedPrototype.getLabelContentType());
		firePropertyChange(GLYPH_URI_PROPERTY, oldProxiedPrototype.getGlyphURI(), newProxiedPrototype.getGlyphURI());
		firePropertyChange(DESCRIPTION_PROPERTY, oldProxiedPrototype.getDescription(), newProxiedPrototype.getDescription());
		firePropertyChange(DESCRIPTION_CONTENT_TYPE_PROPERTY, oldProxiedPrototype.getDescriptionContentType(), newProxiedPrototype.getDescriptionContentType());
		firePropertyChange(INFO_PROPERTY, oldProxiedPrototype.getInfo(), newProxiedPrototype.getInfo());
		firePropertyChange(INFO_CONTENT_TYPE_PROPERTY, oldProxiedPrototype.getInfoContentType(), newProxiedPrototype.getInfoContentType());
	}

	@Override
	public URI getGlyphURI() {
		return getProxiedPrototype().getGlyphURI();
	}

	@Override
	public void setGlyphURI(final URI newLabelIcon) {
		getProxiedPrototype().setGlyphURI(newLabelIcon);
	}

	@Override
	public String getLabel() {
		return getProxiedPrototype().getLabel();
	}

	@Override
	public void setLabel(final String newLabelText) {
		getProxiedPrototype().setLabel(newLabelText);
	}

	@Override
	public ContentType getLabelContentType() {
		return getProxiedPrototype().getLabelContentType();
	}

	@Override
	public void setLabelContentType(final ContentType newLabelTextContentType) {
		getProxiedPrototype().setLabelContentType(newLabelTextContentType);
	}

	@Override
	public String getDescription() {
		return getProxiedPrototype().getDescription();
	}

	@Override
	public void setDescription(final String newDescription) {
		getProxiedPrototype().setDescription(newDescription);
	}

	@Override
	public ContentType getDescriptionContentType() {
		return getProxiedPrototype().getDescriptionContentType();
	}

	@Override
	public void setDescriptionContentType(final ContentType newDescriptionContentType) {
		getProxiedPrototype().setDescriptionContentType(newDescriptionContentType);
	}

	@Override
	public String getInfo() {
		return getProxiedPrototype().getInfo();
	}

	@Override
	public void setInfo(final String newInfo) {
		getProxiedPrototype().setInfo(newInfo);
	}

	@Override
	public ContentType getInfoContentType() {
		return getProxiedPrototype().getInfoContentType();
	}

	@Override
	public void setInfoContentType(final ContentType newInfoContentType) {
		getProxiedPrototype().setInfoContentType(newInfoContentType);
	}

	/**
	 * Proxied prototype constructor.
	 * @param proxiedPrototype The prototype proxied by this prototype.
	 * @throws NullPointerException if the given proxied prototype is <code>null</code> is <code>null</code>.
	 */
	public AbstractProxyPrototype(final P proxiedPrototype) {
		this.proxiedPrototype = checkInstance(proxiedPrototype, "Proxied prototype cannot be null.");
		installListeners(proxiedPrototype);
	}

}
