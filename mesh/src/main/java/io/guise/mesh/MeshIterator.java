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

import static java.lang.String.format;
import static java.util.Arrays.*;
import static java.util.Objects.*;

import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

/**
 * The current state of an ongoing iteration.
 * @apiNote In usage by Guise Mesh, the iteration should never be exposed to the rest of the system in a state before iteration. That is, Guise Mesh consumers
 *          can always assume that iteration is occurring and that {@link #getIndex()}, {@link #getCurrent()}, and the like will always provide valid values
 *          without throwing an exception.
 * @implSpec This iterator is immutable; it does not allow removal.
 * @implSpec This class is not thread safe.
 * @author Garret Wilson
 */
public class MeshIterator implements Iterator<Object> {

	private final Iterator<?> iterator;

	private boolean hasCurrent = false;

	private Object current = null;

	private int index = -1;

	/**
	 * Iterator constructor.
	 * @param iterator The iterator to provide state for.
	 */
	private MeshIterator(@Nonnull final Iterator<?> iterator) {
		this.iterator = requireNonNull(iterator);
	}

	/**
	 * Indicates whether the iterator is on the first item.
	 * @apiNote This implementation is a convenience method that returns whether iteration has started and the iterator is on the first index.
	 * @return <code>true</code> if the current item is the first item.
	 * @see #hasCurrent()
	 * @see #getIndex()
	 */
	public boolean isFirst() {
		return hasCurrent() && getIndex() == 0;
	}

	/**
	 * Indicates whether the iterator is on the last item.
	 * @apiNote This implementation is a convenience method that returns the opposite of {@link #hasNext()}.
	 * @return <code>true</code> if the current item is the last time (i.e. there are no more items).
	 * @see #hasNext()
	 */
	public boolean isLast() {
		return !hasNext();
	}

	/**
	 * Indicates whether iteration has started and there is a current object.
	 * @return <code>true</code> if there is a current object being iterated.
	 */
	public boolean hasCurrent() {
		return hasCurrent;
	}

	/**
	 * Returns the current item. This will be the result of the last successful call to {@link #next()}.
	 * @throws NoSuchElementException if iteration has not yet started.
	 * @return The current item.
	 */
	public Object getCurrent() {
		if(!hasCurrent()) {
			throw new NoSuchElementException("Current element not available; iteration not yet started.");
		}
		return current;
	}

	/** @return The current index of iteration, or <code>-1</code> if iteration has not started. */
	public int getIndex() {
		return index;
	}

	/**
	 * {@inheritDoc}
	 * @see #isLast()
	 */
	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation updates the current item and increments the index.
	 */
	@Override
	public Object next() {
		current = iterator.next();
		hasCurrent = true;
		index++;
		return current;
	}

	/**
	 * Creates a new iteration state from the given source.
	 * @implSpec This implementation supports the following source types:
	 *           <ul>
	 *           <li><code>Object[]</code> (of any non-primitive type)</li>
	 *           <li><code>int[]</code></li>
	 *           <li><code>long[]</code></li>
	 *           <li><code>double[]</code></li>
	 *           <li><code>{@link Iterable}</code></li>
	 *           <li><code>{@link Iterator}</code></li>
	 *           <li><code>{@link Enumeration}</code></li>
	 *           <li><code>{@link Stream}</code></li>
	 *           <li><code>{@link Map}</code> (returns iteration of {@link Map.Entry})</li>
	 *           <li><code>{@link Object}</code> (returns iteration of single object)</li>
	 *           </ul>
	 * @param source The source of iteration.
	 * @return A new state ready for iteration.
	 * @throws IllegalArgumentException if the given object is not supported as a source of iteration.
	 */
	public static MeshIterator fromIterationSource(@Nonnull final Object source) {
		return new MeshIterator(toIterator(source));
	}

	/**
	 * Converts an iteration source object to an {@link Iterator}.
	 * @implSpec This implementation supports the following source types:
	 *           <ul>
	 *           <li><code>Object[]</code> (of any non-primitive type)</li>
	 *           <li><code>int[]</code></li>
	 *           <li><code>long[]</code></li>
	 *           <li><code>double[]</code></li>
	 *           <li><code>{@link Iterable}</code></li>
	 *           <li><code>{@link Iterator}</code></li>
	 *           <li><code>{@link Enumeration}</code></li>
	 *           <li><code>{@link Stream}</code></li>
	 *           <li><code>{@link Map}</code> (returns iteration of {@link Map.Entry})</li>
	 *           <li><code>{@link Object}</code> (returns iteration of single object)</li>
	 *           </ul>
	 * @param object The iteration source object.
	 * @return An iterator to the items in the iteration source.
	 * @throws IllegalArgumentException if the object is an array to an unsupported primitive type.
	 */
	protected static Iterator<?> toIterator(@Nonnull final Object object) {
		if(object.getClass().isArray()) {
			if(object instanceof Object[]) {
				return asList((Object[])object).iterator();
			} else if(object instanceof int[]) {
				return stream((int[])object).iterator();
			} else if(object instanceof long[]) {
				return stream((long[])object).iterator();
			} else if(object instanceof double[]) {
				return stream((double[])object).iterator();
			} else {
				throw new IllegalArgumentException(format("Iteration not supported on array of type %s: %s.", object.getClass().getComponentType().getName(), object));
			}
		} else if(object instanceof Iterable) {
			return ((Iterable<?>)object).iterator();
		} else if(object instanceof Iterator) {
			return (Iterator<?>)object;
		} else if(object instanceof Enumeration) {
			return ((Enumeration<?>)object).asIterator();
		} else if(object instanceof Stream) {
			return ((Stream<?>)object).iterator();
		} else if(object instanceof Map) {
			return ((Map<?, ?>)object).entrySet().iterator();
		} else {
			return Set.of(object).iterator();
		}
	}

}
