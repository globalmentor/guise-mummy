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

/**
 * Commands for reacting to a decision to be made during a process. A process command is semantically more general than a sequence command. While a "next"
 * command is similar to "continue" in the context of a wizard and either could reasonably cause a progresion to the next stage of the sequence, selecting an OK
 * button for a confirmation dialog would not be appropriate in response to the "next" command, although it would be for the "continue" command.
 * @author Garret Wilson
 */
public enum ProcessCommand implements Command {

	/** The command for continuing a process. This command is usually bound to the Enter key and signifies that some step in a process should be approved. */
	CONTINUE,
	/** The command for aborting a process. This command is usually bound to the Escape key and signifies that the process should be canceled. */
	ABORT;

}
