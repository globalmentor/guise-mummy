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

import static java.util.Objects.*;

import java.util.*;

import javax.annotation.*;

/**
 * A simple mesh context using a {@link Map} to back variable storage.
 * @author Garret Wilson
 */
public class MapMeshContext implements MeshContext {

	private final Map<String, Object> map;

	/**
	 * Default map constructor
	 * @implSpec A default {@link HashMap} is used.
	 */
	public MapMeshContext() {
		this(new HashMap<>());
	}

	/**
	 * Map constructor.
	 * @param map The map to be used to back variable storage.
	 */
	public MapMeshContext(@Nonnull final Map<String, Object> map) {
		this.map = requireNonNull(map);
	}

	@Override
	public boolean hasVariable(final String name) {
		return map.containsKey(requireNonNull(name));
	}

	@Override
	public Optional<Object> findVariable(final String name) {
		return Optional.ofNullable(map.get(requireNonNull(name)));
	}

	@Override
	public void setVariable(String name, Object value) {
		map.put(requireNonNull(name), requireNonNull(value));
	}

}
