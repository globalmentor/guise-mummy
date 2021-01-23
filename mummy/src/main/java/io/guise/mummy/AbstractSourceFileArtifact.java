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

import static com.globalmentor.java.Conditions.*;
import static java.util.Objects.*;

import java.nio.file.Path;

import javax.annotation.*;

import io.guise.mummy.mummify.Mummifier;
import io.urf.model.*;

/**
 * An abstract base class for an artifact generated from a source file.
 * @author Garret Wilson
 */
public abstract class AbstractSourceFileArtifact extends AbstractDescribedArtifact implements SourceFileArtifact {

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
	 * {@inheritDoc}
	 * @implSpec This implementation always returns <code>true</code>.
	 */
	@Override
	public final boolean isSourcePathFile() {
		return true;
	}

	private final boolean navigable;

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns <code>false</code> by default unless it is configured in the builder.
	 */
	@Override
	public boolean isNavigable() {
		return navigable;
	}

	/**
	 * Constructor.
	 * @implSpec The artifact is created to be non-navigable.
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourceFile The location of the artifact in the site source tree.
	 * @param targetFile The file where the artifact will be generated.
	 * @param description The description of the artifact.
	 */
	public AbstractSourceFileArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path targetFile,
			@Nonnull final UrfResourceDescription description) {
		super(mummifier, sourceFile, targetFile, description);
		this.navigable = false;
	}

	/**
	 * Builder constructor.
	 * @param builder The builder specifying the construction parameters.
	 */
	protected AbstractSourceFileArtifact(@Nonnull final Builder<?> builder) {
		super(builder.mummifier, builder.sourceFile, builder.targetFile, builder.description != null ? builder.description : new UrfObject());
		this.navigable = builder.navigable;
	}

	/**
	 * Factory for creating an artifact with optional parameters.
	 * @param <B> The concrete type of builder subclass.
	 */
	protected abstract static class Builder<B extends Builder<B>> {

		private final Mummifier mummifier;
		private final Path sourceFile;
		private final Path targetFile;

		/**
		 * Constructor.
		 * @param mummifier The mummifier responsible for generating this artifact.
		 * @param sourceFile The location of the artifact in the site source tree.
		 * @param targetFile The file where the artifact will be generated.
		 */
		public Builder(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path targetFile) {
			this.mummifier = requireNonNull(mummifier);
			this.sourceFile = requireNonNull(sourceFile);
			this.targetFile = requireNonNull(targetFile);

		}

		/** @return This builder itself. */
		@SuppressWarnings("unchecked")
		protected B self() {
			return (B)this;
		}

		/**
		 * Builds a new source file artifact from the current builder state.
		 * @return A new source file artifact.
		 */
		protected abstract SourceFileArtifact build();

		private UrfResourceDescription description = null; //a default description will be created if none is specified

		/**
		 * Sets the description for the artifact.
		 * @apiNote If this method is not called, a default empty description will be used when building.
		 * @param description The artifact description.
		 * @return This builder.
		 * @throws IllegalStateException if this method is called twice on a builder.
		 * @see Artifact#getResourceDescription()
		 */
		public B withDescription(@Nonnull final UrfResourceDescription description) {
			checkState(this.description == null, "Description already set.");
			this.description = requireNonNull(description);
			return self();
		}

		private boolean navigable = false;

		/**
		 * Sets whether the artifact would normally be part of site navigation.
		 * @implSpec The default setting is <code>false</code>.
		 * @param navigable <code>true</code> if the artifact should be part of default navigation.
		 * @return This builder.
		 * @see Artifact#isNavigable()
		 */
		public B setNavigable(final boolean navigable) {
			this.navigable = navigable;
			return self();
		}

	}

}
