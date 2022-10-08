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

package io.guise.framework.input;

import static java.util.Objects.*;

import com.globalmentor.model.AbstractHashObject;

/**
 * User input in the form of a command.
 * @author Garret Wilson
 */
public class CommandInput extends AbstractHashObject implements Input {

	/** The command. */
	private final Command command;

	/** @return The command. */
	public Command getCommand() {
		return command;
	}

	/**
	 * Command constructor.
	 * @param command The command.
	 * @throws NullPointerException if the given command is <code>null</code>.
	 */
	public CommandInput(final Command command) {
		super(requireNonNull(command, "Command cannot be null.")); //construct the parent class
		this.command = command; //save the command
	}

}
