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

import com.globalmentor.net.URIPath;

/**
 * Abstract implementation of an artifact.
 * @author Garret Wilson
 */
public abstract class AbstractArtifact implements Artifact {
	//TODO fix
	//	private final URIPath resourceContextPath;

	private final Path sourceFile;

	private final Path outputFile;

	/**
	 * Source resource context path constructor.
	 * @param resourceContextPath The absolute path of the resource, relative to the site context.
	 * @param sourceFile The file containing the source of this artifact.
	 * @param outputFile The file where the artifact will be generated.
	 * @throws IllegalArgumentException if the given context path is not absolute.
	 */
	public AbstractArtifact(/*TODO fix @Nonnull final URIPath resourceContextPath, */@Nonnull final Path sourceFile, @Nonnull final Path outputFile) {
		//TODO fix		this.resourceContextPath = resourceContextPath.checkAbsolute();
		this.sourceFile = requireNonNull(sourceFile);
		this.outputFile = requireNonNull(outputFile);
	}
	//TODO fix
	//	@Override
	//	public URIPath getResourceContextPath() {
	//		return resourceContextPath;
	//	}

	@Override
	public Path getSourceFile() {
		return sourceFile;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version simply returns the source file, equivalent to {@link #getSourceFile()}.
	 */
	@Override
	public Set<Path> getReferentSourceFiles() {
		return Set.of(getSourceFile());
	}

	@Override
	public Path getOutputFile() {
		return outputFile;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version prints the resource context path.
	 */
	@Override
	public String toString() {
		return /*TODO fix resourceContextPath.toString()*/"" + '(' + getSourceFile() + " -> " + getOutputFile() + ')';
	}
}
