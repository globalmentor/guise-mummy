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
import static com.globalmentor.java.Conditions.*;
import static java.nio.file.Files.*;
import static java.util.Objects.*;

import java.io.*;
import java.nio.file.Path;
import java.util.Set;

import javax.annotation.*;

import io.guise.mummy.mummify.Mummifier;
import io.urf.model.UrfResourceDescription;

/**
 * A source file artifact that retrieves its contents from some actual file on the file system.
 * @author Garret Wilson
 */
public class CorporealSourceFileArtifact extends AbstractSourceFileArtifact {

	private final Path corporealSourceFile;

	/** @return The file containing the actual source contents of the artifact. */
	public Path getCorporealSourceFile() {
		return corporealSourceFile;
	}

	/**
	 * Source file and target file constructor.
	 * @apiNote For more detailed definition, use {@link #builder(Mummifier, Path, Path)}.
	 * @implSpec The source file is used as the corporeal file with the contents of the artifact.
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourceFile The location of the artifact in the site source tree.
	 * @param targetFile The file where the artifact will be generated.
	 * @param description The description of the artifact.
	 * @throws IllegalArgumentException if the source file does not exist or is not a regular file.
	 */
	public CorporealSourceFileArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path targetFile,
			@Nonnull final UrfResourceDescription description) {
		super(mummifier, sourceFile, targetFile, description);
		this.corporealSourceFile = checkArgumentRegularFile(sourceFile);
	}

	/**
	 * Builder constructor.
	 * @implSpec This implementation uses the source file as the corporeal file with the contents of the artifact.
	 * @param builder The builder specifying the construction parameters.
	 * @throws IllegalArgumentException if the corporeal source file does not exist or is not a regular file.
	 */
	protected CorporealSourceFileArtifact(@Nonnull final Builder<?> builder) {
		super(builder);
		this.corporealSourceFile = checkArgumentRegularFile(builder.corporealSourceFile != null ? builder.corporealSourceFile : getSourcePath());
	}

	/**
	 * Creates a builder for further definition of an artifact.
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourceFile The location of the artifact in the site source tree.
	 * @param targetFile The file where the artifact will be generated.
	 * @return A builder for further defining an artifact before construction.
	 */
	public static Builder<?> builder(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path targetFile) {
		return new Builder<>(mummifier, sourceFile, targetFile);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation opens a simple, unbuffered input stream to {@link #getCorporealSourceFile()}.
	 */
	@Override
	public InputStream openSource(final MummyContext context) throws IOException {
		return newInputStream(getCorporealSourceFile());
	}

	/**
	 * Factory for creating an artifact with optional parameters.
	 * @param <B> The concrete type of builder subclass.
	 */
	public static class Builder<B extends Builder<B>> extends AbstractSourceFileArtifact.Builder<B> {

		/**
		 * Constructor.
		 * @param mummifier The mummifier responsible for generating this artifact.
		 * @param sourceFile The location of the artifact in the site source tree.
		 * @param targetFile The file where the artifact will be generated.
		 */
		public Builder(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path targetFile) {
			super(mummifier, sourceFile, targetFile);
		}

		private Path corporealSourceFile = null; //the source file will be used as the corporeal file if none other is specified

		/**
		 * Sets the file containing the actual source contents of the artifact.
		 * @apiNote If this method is not called, the source file will be used as the corporeal file when building.
		 * @param corporealSourceFile The file from which to retrieve the artifact contents.
		 * @return This builder.
		 * @throws IllegalStateException if this method is called twice on a builder.
		 * @see CorporealSourceFileArtifact#getCorporealSourceFile()
		 */
		public B setCorporealSourceFile(@Nonnull Path corporealSourceFile) {
			checkState(this.corporealSourceFile == null, "Corporeal source file already set.");
			this.corporealSourceFile = requireNonNull(corporealSourceFile);
			return self();
		}

		private Set<String> aspectIds = null;

		/**
		 * Indicates that an aspectual artifact should be created with the identified aspects.
		 * @apiNote If no aspect IDs are provided, an aspectual artifact will be created but with no aspects.
		 * @param aspectIds The IDs of the aspects that should be added.
		 * @return This builder.
		 * @throws IllegalStateException if this method is called twice on a builder.
		 * @see AspectualArtifact
		 */
		public B withAspects(@Nonnull String... aspectIds) {
			checkState(this.aspectIds == null, "Aspects already set.");
			this.aspectIds = Set.of(aspectIds);
			return self();
		}

		/**
		 * {@inheritDoc} This implementation creates an {@link AspectualCorporealSourceFileArtifact} if aspects are indicated; otherwise it creates a
		 * {@link CorporealSourceFileArtifact}.
		 */
		@Override
		public CorporealSourceFileArtifact build() {
			validate();
			return aspectIds != null ? new AspectualCorporealSourceFileArtifact(this, aspectIds) : new CorporealSourceFileArtifact(this);
		}

	}

}
