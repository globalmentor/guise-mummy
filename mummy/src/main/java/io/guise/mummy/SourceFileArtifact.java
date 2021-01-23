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

import java.io.*;

import javax.annotation.*;

/**
 * Artifact that conceptually originates from some source file (as opposed to a directory) in the site source tree.
 * @apiNote The artifact's source file as returned by {@link #getSourcePath()} is not guaranteed to actually exist in the source tree. The artifact may load
 *          content from another source or even generate content. Thus to access the source content {@link #openSource(MummyContext)} should be called rather
 *          than opening the source file directly.
 * @author Garret Wilson
 */
public interface SourceFileArtifact extends SourcePathArtifact {

	/**
	 * Opens an input stream to the source file of this artifact.
	 * @apiNote The input stream may not necessarily return a stream to file indicated by the source path.
	 * @param context The context of static site generation.
	 * @return An input stream to the source file contents.
	 * @throws IOException if there is an error opening the source file contents.
	 */
	public InputStream openSource(@Nonnull MummyContext context) throws IOException;

}
