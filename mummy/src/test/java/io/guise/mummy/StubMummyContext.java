/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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
import java.util.*;

import javax.annotation.*;

import io.confound.config.Configuration;
import io.guise.mummy.deploy.*;

/**
 * Stub implementation a mummification context with minimal functionality for testing.
 * @author Garret Wilson
 */
public class StubMummyContext extends BaseMummyContext {

	/**
	 * Constructor.
	 * @param project The Guise project.
	 */
	public StubMummyContext(@Nonnull final GuiseProject project) {
		super(project);
	}

	@Override
	public Configuration getConfiguration() {
		return getProject().getConfiguration();
	}

	@Override
	public boolean isFull() {
		return true;
	}

	@Override
	public SourcePathMummifier getDefaultSourceFileMummifier() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SourcePathMummifier getDefaultSourceDirectoryMummifier() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<SourcePathMummifier> findRegisteredMummifierForSourceFile(Path sourceFile) {
		return Optional.empty();
	}

	@Override
	public Optional<SourcePathMummifier> findRegisteredMummifierForSourceDirectory(Path sourceDirectory) {
		return Optional.empty();
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
