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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import javax.annotation.*;

import io.clogr.Clogged;

/**
 * Processes a resource for mummification.
 * @author Garret Wilson
 */
public interface Mummifier extends Clogged {

	/**
	 * Retrieves the extensions of files supported by this mummifier.
	 * <p>
	 * An extension may be a simple extension such as <code>foo</code>, or a compound extension such as <code>foo.bar</code>, the latter of which would match a
	 * file ending in <code>.foo.bar</code>, such as <code>example.foo.bar</code>.
	 * </p>
	 * @return The extensions of filenames for file types supported by this mummifier.
	 */
	public @Nonnull Set<String> getSupportedFilenameExtensions();

	/**
	 * Plans mummification of a source path supported by this mummifier.
	 * @param context The context of static site generation.
	 * @param sourcePath The source path to be mummified.
	 * @return An artifact describing the resource to be mummified.
	 * @throws IOException if there is an I/O error during planning.
	 */
	public Artifact plan(@Nonnull MummyContext context, @Nonnull final Path sourcePath) throws IOException;

	/**
	 * Mummifies a resource.
	 * @param context The context of static site generation.
	 * @param artifact The artifact being generated.
	 * @throws IOException if there is an I/O error during static site generation.
	 */
	public void mummify(@Nonnull final MummyContext context, @Nonnull Artifact artifact) throws IOException;

}
