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

import static com.globalmentor.io.Files.*;
import static com.globalmentor.java.Conditions.*;
import static java.nio.file.Files.*;
import static java.util.Objects.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import org.jspecify.annotations.*;

import dev.guise.mummy.mummify.Mummifier;
import io.urf.model.UrfResourceDescription;

/// A source file artifact that retrieves its contents from some actual file on the file system.
/// @author Garret Wilson
public class DefaultSourceFileArtifact extends AbstractSourceFileArtifact {

	private final Path corporealSourceFile;

	/// Returns the file containing the actual source contents of the artifact.
	/// @return The file containing the actual source contents of the artifact.
	public Path getCorporealSourceFile() {
		return corporealSourceFile;
	}

	/// Source file and target file constructor.
	/// @apiNote For more detailed definition, use [#builder(Mummifier, Path, Path)].
	/// @implSpec The source file is used as the corporeal file with the contents of the artifact.
	/// @param mummifier The mummifier responsible for generating this artifact.
	/// @param sourceFile The location of the artifact in the site source tree.
	/// @param targetFile The file where the artifact will be generated.
	/// @param description The description of the artifact.
	/// @throws IllegalArgumentException if the source file does not exist or is not a regular file.
	public DefaultSourceFileArtifact(@NonNull final Mummifier mummifier, @NonNull final Path sourceFile, @NonNull final Path targetFile,
			@NonNull final UrfResourceDescription description) {
		super(mummifier, sourceFile, targetFile, description);
		this.corporealSourceFile = checkArgumentRegularFile(sourceFile);
	}

	/// Builder constructor.
	/// @implSpec This implementation uses the source file as the corporeal file with the contents of the artifact.
	/// @param builder The builder specifying the construction parameters.
	/// @throws IllegalArgumentException if the corporeal source file does not exist or is not a regular file.
	protected DefaultSourceFileArtifact(@NonNull final Builder<?> builder) {
		super(builder);
		this.corporealSourceFile = checkArgumentRegularFile(builder.determineCorporealSourceFile());
	}

	/// Creates a builder for further definition of an artifact.
	/// @param mummifier The mummifier responsible for generating this artifact.
	/// @param sourceFile The location of the artifact in the site source tree.
	/// @param targetFile The file where the artifact will be generated.
	/// @return A builder for further defining an artifact before construction.
	public static Builder<?> builder(@NonNull final Mummifier mummifier, @NonNull final Path sourceFile, @NonNull final Path targetFile) {
		return new Builder<>(mummifier, sourceFile, targetFile);
	}

	/// {@inheritDoc}
	/// @implSpec This implementation returns the size of [#getCorporealSourceFile()].
	@Override
	public long getSourceSize(final MummyContext context) throws IOException {
		return size(getCorporealSourceFile());
	}

	/// {@inheritDoc}
	/// @implSpec This implementation opens a simple, unbuffered input stream to [#getCorporealSourceFile()].
	@Override
	public InputStream openSource(final MummyContext context) throws IOException {
		return newInputStream(getCorporealSourceFile());
	}

	/// Factory for creating an artifact with optional parameters.
	/// @param <B> The concrete type of builder subclass.
	public static class Builder<B extends Builder<B>> extends AbstractSourceFileArtifact.Builder<B> {

		/// Constructor.
		/// @param mummifier The mummifier responsible for generating this artifact.
		/// @param sourceFile The location of the artifact in the site source tree.
		/// @param targetFile The file where the artifact will be generated.
		public Builder(@NonNull final Mummifier mummifier, @NonNull final Path sourceFile, @NonNull final Path targetFile) {
			super(mummifier, sourceFile, targetFile);
		}

		private Path corporealSourceFile = null;

		/// Determines the corporeal source file, defaulting to the source file path if not explicitly set.
		/// @return The corporeal source file to use for artifact content.
		final Path determineCorporealSourceFile() {
			return corporealSourceFile != null ? corporealSourceFile : getSourceFile();
		}

		/// Sets the file containing the actual source contents of the artifact.
		/// @apiNote If this method is not called, the source file will be used as the corporeal file when building.
		/// @param corporealSourceFile The file from which to retrieve the artifact contents.
		/// @return This builder.
		/// @throws IllegalStateException if this method is called twice on a builder.
		/// @see DefaultSourceFileArtifact#getCorporealSourceFile()
		public B setCorporealSourceFile(@NonNull Path corporealSourceFile) {
			checkState(this.corporealSourceFile == null, "Corporeal source file already set.");
			this.corporealSourceFile = requireNonNull(corporealSourceFile);
			return self();
		}

		private Map<String, Artifact> aspectArtifacts = null;

		/// Returns the aspect artifacts, if any.
		/// @return The aspect artifacts mapped by aspect ID, or `null` if not set.
		Map<String, Artifact> getAspectArtifacts() {
			return aspectArtifacts;
		}

		/// Indicates that an aspectual artifact should be created with the given pre-built aspect artifacts.
		/// @param aspectArtifacts The pre-built aspect artifacts mapped by aspect ID.
		/// @return This builder.
		/// @throws IllegalStateException if this method is called twice on a builder.
		/// @see AspectualArtifact
		public B withAspectArtifacts(@NonNull final Map<String, Artifact> aspectArtifacts) {
			checkState(this.aspectArtifacts == null, "Aspect artifacts already set.");
			this.aspectArtifacts = Map.copyOf(aspectArtifacts);
			return self();
		}

		/// {@inheritDoc} This implementation creates a [DefaultAspectualSourceFileArtifact] if aspect artifacts are present; otherwise it
		/// creates a [DefaultSourceFileArtifact].
		@Override
		public DefaultSourceFileArtifact build() {
			validate();
			return aspectArtifacts != null && !aspectArtifacts.isEmpty() ? new DefaultAspectualSourceFileArtifact(this) : new DefaultSourceFileArtifact(this);
		}

	}

}
