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

import static com.globalmentor.io.Files.*;
import static java.nio.file.Files.*;

import java.io.*;
import java.nio.file.Path;

import javax.annotation.*;

import io.guise.mummy.mummify.Mummifier;
import io.urf.model.UrfResourceDescription;

/**
 * A base source file artifact that retrieves its contents from some actual file on the file system.
 * @author Garret Wilson
 */
public abstract class BaseCorporealSourceFileArtifact extends AbstractSourceFileArtifact {

	private final Path corporealSourceFile;

	/** @return The file containing the actual source contents of the artifact. */
	public Path getCorporealSourceFile() {
		return corporealSourceFile;
	}

	/**
	 * Source file and output file constructor.
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourceFile The location of the artifact in the site source tree.
	 * @param outputFile The file where the artifact will be generated.
	 * @param description The description of the artifact.
	 * @throws IllegalArgumentException if the source file does not exist or is not a regular file.
	 */
	public BaseCorporealSourceFileArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path outputFile,
			@Nonnull final UrfResourceDescription description) {
		this(mummifier, sourceFile, sourceFile, outputFile, description);
	}

	/**
	 * Source file and output file constructor.
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourceFile The location of the artifact in the site source tree.
	 * @param corporealSourceFile The file containing the actual source contents of the artifact.
	 * @param outputFile The file where the artifact will be generated.
	 * @param description The description of the artifact.
	 * @throws IllegalArgumentException if the corporeal source file does not exist or is not a regular file.
	 */
	public BaseCorporealSourceFileArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path corporealSourceFile,
			@Nonnull final Path outputFile, @Nonnull final UrfResourceDescription description) {
		super(mummifier, sourceFile, outputFile, description);
		checkArgumentRegularFile(corporealSourceFile);
		this.corporealSourceFile = corporealSourceFile;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation opens a simple, unbuffered input stream to {@link #getCorporealSourceFile()}.
	 */
	@Override
	public InputStream openSource(final MummyContext context) throws IOException {
		return newInputStream(getCorporealSourceFile());
	}

}
