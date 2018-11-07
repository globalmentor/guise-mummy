/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.framework.validator;

import java.util.*;

/**
 * Validation exception indicating that multiple validation errors occured. This exception can be caught and updated with new validation exceptions.
 * @author Garret Wilson
 */
public class ValidationsException extends Exception implements Collection<ValidationException> {

	/** The mutable list of validation exceptions. */
	private final List<ValidationException> validationExceptionList = new ArrayList<ValidationException>();

	/**
	 * Validation exceptions constructor. The provided validation exceptions will be added to the list of exceptions.
	 * @param validationExceptions The validation exceptions to be added to the list.
	 */
	public ValidationsException(final ValidationException... validationExceptions) {
		Collections.addAll(validationExceptionList, validationExceptions); //add all the provided exceptions to the list
	}

	@Override
	public int size() {
		return validationExceptionList.size();
	}

	@Override
	public boolean isEmpty() {
		return validationExceptionList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return validationExceptionList.contains(o);
	}

	@Override
	public Iterator<ValidationException> iterator() {
		return validationExceptionList.iterator();
	}

	@Override
	public Object[] toArray() {
		return validationExceptionList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return validationExceptionList.toArray(a);
	}

	// Modification Operations

	@Override
	public boolean add(ValidationException o) {
		return validationExceptionList.add(o);
	}

	@Override
	public boolean remove(Object o) {
		return validationExceptionList.remove(o);
	}

	// Bulk Operations

	@Override
	public boolean containsAll(Collection<?> c) {
		return validationExceptionList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends ValidationException> c) {
		return validationExceptionList.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return validationExceptionList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return validationExceptionList.retainAll(c);
	}

	@Override
	public void clear() {
		validationExceptionList.clear();
	}

}
