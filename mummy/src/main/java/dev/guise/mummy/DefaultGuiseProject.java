/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package dev.guise.mummy;

import static com.globalmentor.io.Paths.*;
import static java.nio.file.LinkOption.*;
import static java.util.Objects.*;

import java.io.IOException;
import java.nio.file.Path;

import org.jspecify.annotations.*;

import io.confound.config.Configuration;

/// Default implementation of a Guise project.
/// @author Garret Wilson
public class DefaultGuiseProject implements GuiseProject {

	private final Path directory;

	@Override
	public Path getDirectory() {
		return directory;
	}

	private final Configuration configuration;

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	/// Directory constructor with a default configuration.
	/// @apiNote This constructor is useful for tests; in production code an explicit configuration should usually be given.
	/// @implSpec The default configuration is retrieved from [GuiseMummy#getDefaultConfiguration(Path)].
	/// @param directory The project directory, in real-path form.
	/// @throws IllegalArgumentException if the project directory is not in real-path form.
	/// @throws IOException if an I/O error occurs during real-path validation.
	public DefaultGuiseProject(@NonNull final Path directory) throws IOException {
		this(directory, GuiseMummy.getDefaultConfiguration(directory));
	}

	/// Directory and configuration constructor.
	/// @param directory The project directory, in real-path form.
	/// @param configuration The project configuration.
	/// @throws IllegalArgumentException if the project directory is not in real-path form.
	/// @throws IOException if an I/O error occurs during real-path validation.
	public DefaultGuiseProject(@NonNull final Path directory, @NonNull final Configuration configuration) throws IOException {
		this.directory = checkArgumentRealPath(directory, NOFOLLOW_LINKS);
		this.configuration = requireNonNull(configuration);
	}

}
