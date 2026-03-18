/*
 * Copyright © 2021 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import org.jspecify.annotations.*;

import io.confound.config.Configuration;
import dev.guise.mummy.deploy.*;

/// A simple mummy context suitable for planning and mummification with minimal configuration, returning default source source file and source directory
/// mummifiers.
/// @implSpec This implementation does not support [#getPlan()].
/// @author Garret Wilson
public class FakeMummyContext extends BaseMummyContext {

	/// Constructor.
	/// @param project The Guise project.
	/// @param siteSourceDirectory The base directory of the site source, in real-path form.
	/// @param siteTargetDirectory The output directory of the site, in real-path form.
	/// @param siteDescriptionTargetDirectory The output directory of the site description, in real-path form.
	/// @throws IllegalArgumentException if any directory path is not in real-path form.
	/// @throws IOException if an I/O error occurs during real-path validation.
	public FakeMummyContext(@NonNull final GuiseProject project, @NonNull final Path siteSourceDirectory, @NonNull final Path siteTargetDirectory,
			@NonNull final Path siteDescriptionTargetDirectory) throws IOException {
		super(project, siteSourceDirectory, siteTargetDirectory, siteDescriptionTargetDirectory);
	}

	@Override
	public Configuration getConfiguration() {
		return getProject().getConfiguration();
	}

	@Override
	public MummyPlan getPlan() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFull() {
		return true;
	}

	@Override
	public Optional<Dns> getDeployDns() {
		return Optional.empty();
	}

	@Override
	public Optional<List<DeployTarget>> getDeployTargets() {
		return Optional.empty();
	}

};
