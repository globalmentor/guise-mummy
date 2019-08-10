/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy;

import java.nio.file.Path;

import io.confound.config.Configuration;

/**
 * A general configured Guise project.
 * @author Garret Wilson
 */
public interface GuiseProject {

	/**
	 * Returns the project directory.
	 * <p>
	 * This is usually where the project configuration file (if any) is stored.
	 * </p>
	 * @return The project directory,
	 */
	public Path getDirectory();

	/**
	 * Returns the project configuration.
	 * @apiNote Oftentimes the configuration will have one or more fallback configurations with default settings.
	 * @return The project configuration.
	 */
	public Configuration getConfiguration();

}
