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

package com.guiseframework.component.layout;

import static java.util.Objects.*;

import java.net.URI;

import com.globalmentor.net.ContentType;
import com.guiseframework.model.*;

/**
 * Constraints on an individual component in a card layout.
 * @author Garret Wilson
 */
public class CardConstraints extends ControlConstraints implements InfoModel, Displayable, Enableable {

	/** The info model used by this component. */
	private final InfoModel infoModel;

	/** @return The info model used by this component. */
	protected InfoModel getInfoModel() {
		return infoModel;
	}

	/** Default constructor. */
	public CardConstraints() {
		this(true); //construct the class, defaulting to enabled
	}

	/**
	 * Enabled constructor.
	 * @param enabled Whether the card is enabled.
	 */
	public CardConstraints(final boolean enabled) {
		this((String)null, enabled); //construct the class with no label
	}

	/**
	 * Label constructor.
	 * @param label The text of the label.
	 */
	public CardConstraints(final String label) {
		this(label, true); //construct the class, defaulting to enabled
	}

	/**
	 * Label and enabled constructor.
	 * @param label The text of the label.
	 * @param enabled Whether the card is enabled.
	 */
	public CardConstraints(final String label, final boolean enabled) {
		this(new DefaultInfoModel(label), enabled); //construct the class with a default info model
	}

	/**
	 * Info model constructor.
	 * @param infoModel The info model representing the card label.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public CardConstraints(final InfoModel infoModel) {
		this(infoModel, true); //construct the class, defaulting to enabled
	}

	/**
	 * Info model and enabled constructor.
	 * @param infoModel The info model representing the card label.
	 * @param enabled Whether the card is enabled.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public CardConstraints(final InfoModel infoModel, final boolean enabled) {
		super(enabled); //construct the parent class 
		this.infoModel = requireNonNull(infoModel, "Info model cannot be null."); //save the info model
		this.infoModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the info model
		this.infoModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the info model
	}

	//InfoModel delegations

	@Override
	public URI getGlyphURI() {
		return getInfoModel().getGlyphURI();
	}

	@Override
	public void setGlyphURI(final URI newLabelIcon) {
		getInfoModel().setGlyphURI(newLabelIcon);
	}

	@Override
	public String getLabel() {
		return getInfoModel().getLabel();
	}

	@Override
	public void setLabel(final String newLabelText) {
		getInfoModel().setLabel(newLabelText);
	}

	@Override
	public ContentType getLabelContentType() {
		return getInfoModel().getLabelContentType();
	}

	@Override
	public void setLabelContentType(final ContentType newLabelTextContentType) {
		getInfoModel().setLabelContentType(newLabelTextContentType);
	}

	@Override
	public String getDescription() {
		return getInfoModel().getDescription();
	}

	@Override
	public void setDescription(final String newDescription) {
		getInfoModel().setDescription(newDescription);
	}

	@Override
	public ContentType getDescriptionContentType() {
		return getInfoModel().getDescriptionContentType();
	}

	@Override
	public void setDescriptionContentType(final ContentType newDescriptionContentType) {
		getInfoModel().setDescriptionContentType(newDescriptionContentType);
	}

	@Override
	public String getInfo() {
		return getInfoModel().getInfo();
	}

	@Override
	public void setInfo(final String newInfo) {
		getInfoModel().setInfo(newInfo);
	}

	@Override
	public ContentType getInfoContentType() {
		return getInfoModel().getInfoContentType();
	}

	@Override
	public void setInfoContentType(final ContentType newInfoContentType) {
		getInfoModel().setInfoContentType(newInfoContentType);
	}

}
