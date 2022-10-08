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

import java.io.*;
import java.nio.file.Path;

import javax.annotation.*;

import io.guise.mummy.mummify.Mummifier;
import io.urf.model.UrfResourceDescription;

/**
 * A dummy artifact used for testing.
 * @author Garret Wilson
 */
public class DummyArtifact extends AbstractSourceFileArtifact {

	/**
	 * Constructor with no description.
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourceFile The location of the artifact in the site source tree.
	 * @param outputFile The file where the artifact will be generated.
	 */
	public DummyArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path outputFile) {
		this(mummifier, sourceFile, outputFile, UrfResourceDescription.EMPTY);
	}

	/**
	 * Description constructor.
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourceFile The location of the artifact in the site source tree.
	 * @param outputFile The file where the artifact will be generated.
	 */
	public DummyArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path outputFile,
			@Nonnull final UrfResourceDescription description) {
		super(mummifier, sourceFile, outputFile, description);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This method is not implemented.
	 * @throws UnsupportedOperationException invariably.
	 */
	@Override
	public long getSourceSize(final MummyContext context) throws IOException {
		throw new UnsupportedOperationException("Cannot get the source size of a dummy artifact.");
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This method is not implemented.
	 * @throws UnsupportedOperationException invariably.
	 */
	@Override
	public InputStream openSource(final MummyContext context) throws IOException {
		throw new UnsupportedOperationException("Cannot open source of dummy artifact.");
	}

	@Override
	public boolean isNavigable() {
		return true;
	}

}
