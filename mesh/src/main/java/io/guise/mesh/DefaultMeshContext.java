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

import static com.globalmentor.java.Conditions.*;
import static java.util.Objects.*;

import java.util.*;
import java.util.function.Supplier;

import javax.annotation.*;

/**
 * Default implementation of a Mesh context.
 * @implNote This implementation is not thread safe.
 * @author Garret Wilson
 */
public class DefaultMeshContext implements MeshContext {

	/** The stack of nested scopes. The stack will never to be empty; it will always contain a root scope. */
	private final Deque<MeshScope> scopeStack = new ArrayDeque<>();

	private final Supplier<MeshScope> scopeFactory;

	/**
	 * No-args constructor.
	 * @implSpec A {@link MapMeshScope} will be used for the root scope, and a factory producing instances of {@link MapMeshScope} will be used for creating new
	 *           scopes.
	 */
	public DefaultMeshContext() {
		this(MapMeshScope::new);
	}

	/**
	 * Root scope constructor.
	 * @implSpec A factory producing instances of {@link MapMeshScope} will be used.
	 * @param rootScope The root scope of the context.
	 */
	public DefaultMeshContext(@Nonnull final MeshScope rootScope) {
		this(rootScope, MapMeshScope::new);
	}

	/**
	 * Scope factory constructor.
	 * @implSpec A {@link MapMeshScope} will be used for the root scope.
	 * @param scopeFactory The factory for creating new scopes.
	 */
	public DefaultMeshContext(@Nonnull final Supplier<MeshScope> scopeFactory) {
		this(new MapMeshScope(), scopeFactory);
	}

	/**
	 * Root scope and scope factory constructor.
	 * @param rootScope The root scope of the context.
	 * @param scopeFactory The factory for creating new scopes.
	 */
	public DefaultMeshContext(@Nonnull final MeshScope rootScope, @Nonnull final Supplier<MeshScope> scopeFactory) {
		scopeStack.push(requireNonNull(rootScope));
		this.scopeFactory = requireNonNull(scopeFactory);
	}

	@Override
	public boolean hasVariable(final String name) {
		for(final MeshScope scope : scopeStack) {
			if(scope.hasVariable(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Optional<Object> findVariable(final String name) {
		Optional<Object> foundVariable;
		for(final MeshScope scope : scopeStack) {
			foundVariable = scope.findVariable(name);
			if(foundVariable.isPresent()) {
				return foundVariable;
			}
		}
		return Optional.empty();
	}

	@Override
	public void setVariable(String name, Object value) {
		scopeStack.peek().setVariable(name, value); //set the variable in the current scope
	}

	@Override
	public ScopeNesting nestScope() {
		final MeshScope newScope = scopeFactory.get();
		scopeStack.push(newScope);
		return new DefaultScopeNesting(newScope);
	}

	/** Default implementation of a scope nesting. */
	private class DefaultScopeNesting implements ScopeNesting {

		private final MeshScope scope;

		@Override
		public MeshScope getScope() {
			return scope;
		}

		/**
		 * Scope constructor.
		 * @param scope The scope being nested.
		 */
		public DefaultScopeNesting(@Nonnull final MeshScope scope) {
			this.scope = requireNonNull(scope);
		}

		@Override
		public void close() {
			checkState(scopeStack.contains(scope), "Scope is already closed.");
			while(scopeStack.pop() != scope) //pop all the scopes until we find this scope (it will almost always be the first one)
				;
		}

	}

}
