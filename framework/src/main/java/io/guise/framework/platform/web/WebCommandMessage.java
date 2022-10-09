/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform.web;

import java.util.*;

import static java.util.Collections.*;
import static java.util.Objects.*;

import com.globalmentor.model.NameValuePair;

import static com.globalmentor.collections.Maps.*;

/**
 * A command message to or from the web platform.
 * @param <C> The type of command.
 * @author Garret Wilson
 */
public class WebCommandMessage<C extends Enum<C> & WebPlatformCommand> extends AbstractWebMessage implements WebPlatformCommandMessage<C> {

	/** The command. */
	private final WebPlatformCommand command;

	@SuppressWarnings("unchecked")
	@Override
	public C getCommand() {
		return (C)command;
	}

	/** The read-only map of parameters, which will be encoded in JavaScript Object Notation (JSON). */
	private final Map<String, Object> parameters;

	@Override
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * Command and parameters constructor.
	 * @param command The command.
	 * @param parameters The parameters of the command; parameters with duplicate names replace earlier parameters of the same name.
	 * @throws NullPointerException if the given command and/or parameters is <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public WebCommandMessage(final C command, final NameValuePair<String, Object>... parameters) {
		this.command = requireNonNull(command, "Command cannot be null.");
		this.parameters = unmodifiableMap(addAll(new HashMap<String, Object>(parameters.length), parameters)); //add all the parameters to a new map
	}

	/**
	 * Command and parameters map constructor.
	 * @param command The command.
	 * @param parameters The map representing the parameters of the command.
	 * @throws NullPointerException if the given command and/or parameters is <code>null</code>.
	 */
	public WebCommandMessage(final C command, final Map<String, Object> parameters) {
		this.command = requireNonNull(command, "Command cannot be null.");
		this.parameters = unmodifiableMap(new HashMap<String, Object>(requireNonNull(parameters, "Parameters cannot be null.")));
	}

}
