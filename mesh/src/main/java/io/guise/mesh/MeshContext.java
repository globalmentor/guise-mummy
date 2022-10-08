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

import java.util.*;

import javax.annotation.*;

/**
 * Provides information about context of DOM meshing, including variable lookup.
 * <p>
 * As a Mesh scope, a Mesh context allows lookup of variables all the way up the scope chain, resolving variables across nested scopes.
 * </p>
 * @author Garret Wilson
 */
public interface MeshContext extends MeshScope {

	/**
	 * {@inheritDoc} The context will search for the variable up the nested scope chain, returning the first variable it finds.
	 */
	@Override
	public Optional<Object> findVariable(@Nonnull String name);

	/**
	 * {@inheritDoc} The context will search for the variable up the nested scope chain.
	 */
	@Override
	public boolean hasVariable(@Nonnull final String name);

	/**
	 * {@inheritDoc} The context sets the variable in the current context scope in the nested scope chain.
	 */
	@Override
	public void setVariable(@Nonnull String name, @Nonnull Object value);

	/**
	 * Creates a new default context with the given variables.
	 * @implSpec A {@link DefaultMeshContext} will be created.
	 * @param map The map of variables with which to initialize the root scope.
	 * @return A new context with the given variables in the root scope.
	 * @see MeshScope#create(Map)
	 */
	public static MeshContext create(@Nonnull final Map<String, Object> map) {
		return new DefaultMeshContext(MeshScope.create(map));
	}

	/**
	 * Creates and adds a new nested scope to the scope chain.
	 * @return An encapsulation of the nested scope, allowing scope closure and removal.
	 */
	public ScopeNesting nestScope();

	/** A nested level of scope. */
	public interface ScopeNesting extends AutoCloseable {

		/** @return The nested scope. */
		public MeshScope getScope();

		/**
		 * Closes and discards the scope and all its nested scopes. The context scope will revert to the parent scope.
		 * @throws IllegalStateException if the scope one of its parents has already been closed.
		 */
		@Override
		public void close();

	}

}
