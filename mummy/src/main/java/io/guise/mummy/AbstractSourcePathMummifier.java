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
 * Abstract mummifier for generating artifacts based upon a single source file or directory.
 * @author Garret Wilson
 */
public abstract class AbstractSourcePathMummifier implements Mummifier {

	/**
	 * Determines the output path for an artifact in the site target directory based upon the source path in the site source directory.
	 * @implSpec This version delegates to {@link MummyContext#getTargetPath(Path)}.
	 * @param context The context of static site generation.
	 * @param sourcePath The path in the site source directory.
	 * @return The path in the site target directory to which the given source path should be generated.
	 * @throws IllegalArgumentException if the given source file is not in the site source tree.
	 * @see MummyContext#getTargetPath(Path)
	 */
	protected Path getArtifactTargetPath(@Nonnull MummyContext context, @Nonnull final Path sourcePath) {
		return context.getTargetPath(sourcePath);
	}

}
