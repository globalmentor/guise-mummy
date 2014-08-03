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

package com.guiseframework.input;

import java.net.URI;

import com.globalmentor.util.StringTemplate;

import static com.guiseframework.Resources.*;

/**
 * Commands for controlling media.
 * @author Garret Wilson
 */
public enum MediaCommand implements Command {

	/** The command for advancing in a media stream from the current location. */
	ADVANCE,
	/** The command for moving to the next media resource in a series. */
	NEXT,
	/** The command for pausing a media stream, with the capability to continue the media stream later. */
	PAUSE,
	/** The command for starting media. */
	PLAY,
	/** The command for moving to the previous media resource in a series. */
	PREVIOUS,
	/** The command for receding in a media stream from the current location. */
	RECEDE,
	/** The command for recording input to media. */
	RECORD,
	/** The command for stopping media. */
	STOP;

	/** The resource key template for each label. */
	private final static StringTemplate LABEL_RESOURCE_KEY_TEMPLATE = new StringTemplate("command.media.", StringTemplate.STRING_PARAMETER, ".label");
	/** The resource key template for each glyph. */
	private final static StringTemplate GLYPH_RESOURCE_KEY_TEMPLATE = new StringTemplate("command.media.", StringTemplate.STRING_PARAMETER, ".glyph");

	/** @return The resource reference for the label. */
	public String getLabel() {
		return createStringResourceReference(LABEL_RESOURCE_KEY_TEMPLATE.apply(getResourceKeyName(this))); //create a resource reference using the resource key name of this enum value
	}

	/** @return The resource reference for the glyph. */
	public URI getGlyph() {
		return createURIResourceReference(GLYPH_RESOURCE_KEY_TEMPLATE.apply(getResourceKeyName(this))); //create a resource reference using the resource key name of this enum value
	}

	/**
	 * Returns a string representation of the command. This implementation delegates to {@link #getLabel()}.
	 * @return A string representation of the object.
	 */
	public String toString() {
		return getLabel();
	}

}
