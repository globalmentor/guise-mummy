/*
 * Copyright © 2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.mesh;

import java.util.*;

import javax.annotation.*;

/**
 * A scope of variables. <code>null</code> variable values are not permitted.
 * @apiNote Variable lookup in a nested scope does not automatically look up variables in parent scopes. Scope resolution requires external coordination,
 *          usually in {@link MeshContext}.
 * @author Garret Wilson
 */
public interface MeshScope {

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

	/**
	 * Creates a new scope with the given variables.
	 * @implSpec A {@link MapMeshScope} will be created.
	 * @param map The map of variables with which to initialize the scope.
	 * @return A new scope with the given variables.
	 */
	public static MeshScope create(@Nonnull final Map<String, Object> map) {
		return new MapMeshScope(new HashMap<>(map)); //make a defensive copy of the variables map
	}

}
