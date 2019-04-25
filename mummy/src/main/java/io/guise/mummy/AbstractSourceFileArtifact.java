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

import javax.annotation.*;

/**
 * An abstract base class for an artifact generated from a source file.
 * @author Garret Wilson
 */
public abstract class AbstractSourceFileArtifact extends AbstractArtifact implements SourceFileArtifact {

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the parent directory of the source path.
	 */
	@Override
	public Path getSourceDirectory() {
		final Path sourceDirectory = getSourcePath().getParent();
		assert sourceDirectory != null : "There should be no way for an artifact not to have a parent directory.";
		return sourceDirectory;
	}

	/**
	 * Constructor.
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourceFile The location of the artifact in the site source tree.
	 * @param outputFile The file where the artifact will be generated.
	 */
	public AbstractSourceFileArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path outputFile) {
		super(mummifier, sourceFile, outputFile);
	}

}
