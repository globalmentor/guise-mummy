/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mesh;

import java.util.Optional;

import javax.annotation.*;

/**
 * Provides information about context of DOM meshing, including variable lookup. <code>null</code> variable values are not permitted.
 * @author Garret Wilson
 */
public interface MeshContext {

	/**
	 * Looks up a named variable in the current context.
	 * @param name The name of the variable to find.
	 * @return The value of the variable if present.
	 */
	public Optional<Object> findVariable(@Nonnull String name);

	/**
	 * Determines whether a variable with the given name exists in the given context.
	 * @implSpec The default implementation delegates to {@link #findVariable(String)}.
	 * @param name The name of the variable to check for.
	 * @return <code>true</code> if the named variable exists.
	 */
	public default boolean hasVariable(@Nonnull final String name) {
		return findVariable(name).isPresent();
	}

	/**
	 * Sets a variable in the context. If the variable already exists, its previous value will be lost.
	 * @param name The name of the variable to set.
	 * @param value The new variable value, which must not be <code>null</code>.
	 */
	public void setVariable(@Nonnull String name, @Nonnull Object value);

}
