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

import static java.util.Objects.*;

import java.nio.file.Path;
import java.util.Set;

import javax.annotation.*;

import io.guise.mummy.mummify.Mummifier;

/**
 * Abstract implementation of an artifact.
 * @implSpec This class implements artifact equality.
 * @author Garret Wilson
 */
public abstract class AbstractArtifact implements Artifact {

	private final Mummifier mummifier;

	@Override
	public Mummifier getMummifier() {
		return mummifier;
	}

	private final Path sourcePath;

	@Override
	public Path getSourcePath() {
		return sourcePath;
	}

	private final Path targetPath;

	@Override
	public Path getTargetPath() {
		return targetPath;
	}

	/**
	 * Constructor
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourcePath The file containing the source of this artifact.
	 * @param outputPath The file where the artifact will be generated.
	 */
	public AbstractArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourcePath, @Nonnull final Path outputPath) {
		this.mummifier = requireNonNull(mummifier);
		this.sourcePath = requireNonNull(sourcePath);
		this.targetPath = requireNonNull(outputPath);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version simply returns the source file, equivalent to {@link #getSourcePath()}.
	 */
	@Override
	public Set<Path> getReferentSourcePaths() {
		return Set.of(getSourcePath());
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version prints the resource context path.
	 */
	@Override
	public String toString() {
		return "(`" + getSourcePath() + "` -> `" + getTargetPath() + "`)";
	}

	@Override
	public int hashCode() {
		return getTargetPath().hashCode();
	}

	@Override
	public boolean equals(final Object object) {
		if(object == this) {
			return true;
		}
		if(!(object instanceof Artifact)) {
			return false;
		}
		return getTargetPath().equals(((Artifact)object).getTargetPath());
	}

}
