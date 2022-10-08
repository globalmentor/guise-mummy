/*
 * Copyright © 2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.mummy;

import static java.util.Objects.*;

import java.nio.file.Path;
import java.util.*;

import javax.annotation.*;
import javax.xml.parsers.DocumentBuilder;

import io.confound.config.Configuration;
import io.guise.mummy.deploy.*;
import io.guise.mummy.mummify.OpaqueFileMummifier;
import io.guise.mummy.mummify.SourcePathMummifier;
import io.guise.mummy.mummify.collection.DirectoryMummifier;

/**
 * Stub implementation of a mummification context with minimal functionality for testing.
 * @implSpec This implementation does not support {@link #getPlan()}.
 * @author Garret Wilson
 */
public class StubMummyContext implements MummyContext {

	/**
	 * {@inheritDoc}
	 * @implSpec This version returns {@link GuiseMummy#LABEL}, usually a string in the form <code>Guise Mummy <var>version</var></code>.
	 */
	@Override
	public String getMummifierIdentification() {
		return GuiseMummy.LABEL;
	}

	private final SourcePathMummifier fileMummifier = new OpaqueFileMummifier();
	private final SourcePathMummifier directoryMummifier = new DirectoryMummifier();

	private final GuiseProject project;

	@Override
	public GuiseProject getProject() {
		return project;
	}

	/**
	 * Constructor.
	 * @param project The Guise project.
	 */
	public StubMummyContext(@Nonnull final GuiseProject project) {
		this.project = requireNonNull(project);
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
	public boolean isIgnore(Path sourcePath) {
		return false;
	}

	@Override
	public boolean isFull() {
		return true;
	}

	@Override
	public SourcePathMummifier getDefaultSourceFileMummifier() {
		return fileMummifier;
	}

	@Override
	public SourcePathMummifier getDefaultSourceDirectoryMummifier() {
		return directoryMummifier;
	}

	@Override
	public Optional<SourcePathMummifier> findRegisteredMummifierForSourceFile(Path sourceFile) {
		return Optional.of(getDefaultSourceFileMummifier());
	}

	@Override
	public Optional<SourcePathMummifier> findRegisteredMummifierForSourceDirectory(Path sourceDirectory) {
		return Optional.of(getDefaultSourceDirectoryMummifier());
	}

	@Override
	public DocumentBuilder newPageDocumentBuilder() {
		throw new UnsupportedOperationException();
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
