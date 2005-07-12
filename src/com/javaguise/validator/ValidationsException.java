package com.javaguise.validator;

import java.util.*;

/**Validation exception indicating that multiple validation errors occured.
This exception can be caught and updated with new validation exceptions.
@author Garret Wilson
*/
public class ValidationsException extends Exception implements Collection<ValidationException>
{

	/**The mutable list of validation exceptions.*/
	private final List<ValidationException> validationExceptionList=new ArrayList<ValidationException>();

	/**Validation exceptions constructor.
	The provided validation exceptions will be added to the list of exceptions.
	*/
	public ValidationsException(final ValidationException... validationExceptions)
	{
		Collections.addAll(validationExceptionList, validationExceptions);	//add all the provided exceptions to the list
	}

  /**
   * Returns the number of elements in this collection.  If this collection
   * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
   * <tt>Integer.MAX_VALUE</tt>.
   * 
   * @return the number of elements in this collection
   */
  public int size() {return validationExceptionList.size();}

  /**
   * Returns <tt>true</tt> if this collection contains no elements.
   *
   * @return <tt>true</tt> if this collection contains no elements
   */
  public boolean isEmpty() {return validationExceptionList.isEmpty();}

  /**
   * Returns <tt>true</tt> if this collection contains the specified
   * element.  More formally, returns <tt>true</tt> if and only if this
   * collection contains at least one element <tt>e</tt> such that
   * <tt>(o==null ? e==null : o.equals(e))</tt>.
   *
   * @param o element whose presence in this collection is to be tested.
   * @return <tt>true</tt> if this collection contains the specified
   *         element
   * @throws ClassCastException if the type of the specified element
   * 	       is incompatible with this collection (optional).
   * @throws NullPointerException if the specified element is null and this
   *         collection does not support null elements (optional).
   */
  public boolean contains(Object o) {return validationExceptionList.contains(o);}

  /**
   * Returns an iterator over the elements in this collection.  There are no
   * guarantees concerning the order in which the elements are returned
   * (unless this collection is an instance of some class that provides a
   * guarantee).
   * 
   * @return an <tt>Iterator</tt> over the elements in this collection
   */
  public Iterator<ValidationException> iterator() {return validationExceptionList.iterator();}

  /**
   * Returns an array containing all of the elements in this collection.  If
   * the collection makes any guarantees as to what order its elements are
   * returned by its iterator, this method must return the elements in the
   * same order.<p>
   *
   * The returned array will be "safe" in that no references to it are
   * maintained by this collection.  (In other words, this method must
   * allocate a new array even if this collection is backed by an array).
   * The caller is thus free to modify the returned array.<p>
   *
   * This method acts as bridge between array-based and collection-based
   * APIs.
   *
   * @return an array containing all of the elements in this collection
   */
  public Object[] toArray() {return validationExceptionList.toArray();}

  /**
   * Returns an array containing all of the elements in this collection; 
   * the runtime type of the returned array is that of the specified array.  
   * If the collection fits in the specified array, it is returned therein.  
   * Otherwise, a new array is allocated with the runtime type of the 
   * specified array and the size of this collection.<p>
   *
   * If this collection fits in the specified array with room to spare
   * (i.e., the array has more elements than this collection), the element
   * in the array immediately following the end of the collection is set to
   * <tt>null</tt>.  This is useful in determining the length of this
   * collection <i>only</i> if the caller knows that this collection does
   * not contain any <tt>null</tt> elements.)<p>
   *
   * If this collection makes any guarantees as to what order its elements
   * are returned by its iterator, this method must return the elements in
   * the same order.<p>
   *
   * Like the <tt>toArray</tt> method, this method acts as bridge between
   * array-based and collection-based APIs.  Further, this method allows
   * precise control over the runtime type of the output array, and may,
   * under certain circumstances, be used to save allocation costs<p>
   *
   * Suppose <tt>l</tt> is a <tt>List</tt> known to contain only strings.
   * The following code can be used to dump the list into a newly allocated
   * array of <tt>String</tt>:
   *
   * <pre>
   *     String[] x = (String[]) v.toArray(new String[0]);
   * </pre><p>
   *
   * Note that <tt>toArray(new Object[0])</tt> is identical in function to
   * <tt>toArray()</tt>.
   *
   * @param a the array into which the elements of this collection are to be
   *        stored, if it is big enough; otherwise, a new array of the same
   *        runtime type is allocated for this purpose.
   * @return an array containing the elements of this collection
   * 
   * @throws ArrayStoreException the runtime type of the specified array is
   *         not a supertype of the runtime type of every element in this
   *         collection.
   * @throws NullPointerException if the specified array is <tt>null</tt>.
   */
  public <T> T[] toArray(T[] a) {return validationExceptionList.toArray(a);}

  // Modification Operations

  /**
   * Ensures that this collection contains the specified element (optional
   * operation).  Returns <tt>true</tt> if this collection changed as a
   * result of the call.  (Returns <tt>false</tt> if this collection does
   * not permit duplicates and already contains the specified element.)<p>
   *
   * Collections that support this operation may place limitations on what
   * elements may be added to this collection.  In particular, some
   * collections will refuse to add <tt>null</tt> elements, and others will
   * impose restrictions on the type of elements that may be added.
   * Collection classes should clearly specify in their documentation any
   * restrictions on what elements may be added.<p>
   *
   * If a collection refuses to add a particular element for any reason
   * other than that it already contains the element, it <i>must</i> throw
   * an exception (rather than returning <tt>false</tt>).  This preserves
   * the invariant that a collection always contains the specified element
   * after this call returns.
   *
   * @param o element whose presence in this collection is to be ensured.
   * @return <tt>true</tt> if this collection changed as a result of the
   *         call
   * 
   * @throws UnsupportedOperationException <tt>add</tt> is not supported by
   *         this collection.
   * @throws ClassCastException class of the specified element prevents it
   *         from being added to this collection.
   * @throws NullPointerException if the specified element is null and this
   *         collection does not support null elements.
   * @throws IllegalArgumentException some aspect of this element prevents
   *         it from being added to this collection.
   */
  public boolean add(ValidationException o) {return validationExceptionList.add(o);}

  /**
   * Removes a single instance of the specified element from this
   * collection, if it is present (optional operation).  More formally,
   * removes an element <tt>e</tt> such that <tt>(o==null ?  e==null :
   * o.equals(e))</tt>, if this collection contains one or more such
   * elements.  Returns true if this collection contained the specified
   * element (or equivalently, if this collection changed as a result of the
   * call).
   *
   * @param o element to be removed from this collection, if present.
   * @return <tt>true</tt> if this collection changed as a result of the
   *         call
   * 
   * @throws ClassCastException if the type of the specified element
   * 	       is incompatible with this collection (optional).
   * @throws NullPointerException if the specified element is null and this
   *         collection does not support null elements (optional).
   * @throws UnsupportedOperationException remove is not supported by this
   *         collection.
   */
  public boolean remove(Object o) {return validationExceptionList.remove(o);}


  // Bulk Operations

  /**
   * Returns <tt>true</tt> if this collection contains all of the elements
   * in the specified collection.
   *
   * @param  c collection to be checked for containment in this collection.
   * @return <tt>true</tt> if this collection contains all of the elements
   *	       in the specified collection
   * @throws ClassCastException if the types of one or more elements
   *         in the specified collection are incompatible with this
   *         collection (optional).
   * @throws NullPointerException if the specified collection contains one
   *         or more null elements and this collection does not support null
   *         elements (optional).
   * @throws NullPointerException if the specified collection is
   *         <tt>null</tt>.
   * @see    #contains(Object)
   */
  public boolean containsAll(Collection<?> c) {return validationExceptionList.containsAll(c);}

  /**
   * Adds all of the elements in the specified collection to this collection
   * (optional operation).  The behavior of this operation is undefined if
   * the specified collection is modified while the operation is in progress.
   * (This implies that the behavior of this call is undefined if the
   * specified collection is this collection, and this collection is
   * nonempty.)
   *
   * @param c elements to be inserted into this collection.
   * @return <tt>true</tt> if this collection changed as a result of the
   *         call
   * 
   * @throws UnsupportedOperationException if this collection does not
   *         support the <tt>addAll</tt> method.
   * @throws ClassCastException if the class of an element of the specified
   * 	       collection prevents it from being added to this collection.
   * @throws NullPointerException if the specified collection contains one
   *         or more null elements and this collection does not support null
   *         elements, or if the specified collection is <tt>null</tt>.
   * @throws IllegalArgumentException some aspect of an element of the
   *	       specified collection prevents it from being added to this
   *	       collection.
   * @see #add(ValidationException)
   */
  public boolean addAll(Collection<? extends ValidationException> c) {return validationExceptionList.addAll(c);}

  /**
   * 
   * Removes all this collection's elements that are also contained in the
   * specified collection (optional operation).  After this call returns,
   * this collection will contain no elements in common with the specified
   * collection.
   *
   * @param c elements to be removed from this collection.
   * @return <tt>true</tt> if this collection changed as a result of the
   *         call
   * 
   * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
   * 	       is not supported by this collection.
   * @throws ClassCastException if the types of one or more elements
   *         in this collection are incompatible with the specified
   *         collection (optional).
   * @throws NullPointerException if this collection contains one or more
   *         null elements and the specified collection does not support
   *         null elements (optional).
   * @throws NullPointerException if the specified collection is
   *         <tt>null</tt>.
   * @see #remove(Object)
   * @see #contains(Object)
   */
  public boolean removeAll(Collection<?> c) {return validationExceptionList.removeAll(c);}

  /**
   * Retains only the elements in this collection that are contained in the
   * specified collection (optional operation).  In other words, removes from
   * this collection all of its elements that are not contained in the
   * specified collection.
   *
   * @param c elements to be retained in this collection.
   * @return <tt>true</tt> if this collection changed as a result of the
   *         call
   * 
   * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
   * 	       is not supported by this Collection.
   * @throws ClassCastException if the types of one or more elements
   *         in this collection are incompatible with the specified
   *         collection (optional).
   * @throws NullPointerException if this collection contains one or more
   *         null elements and the specified collection does not support null 
   *         elements (optional).
   * @throws NullPointerException if the specified collection is
   *         <tt>null</tt>.
   * @see #remove(Object)
   * @see #contains(Object)
   */
  public boolean retainAll(Collection<?> c) {return validationExceptionList.retainAll(c);}

  /**
   * Removes all of the elements from this collection (optional operation).
   * This collection will be empty after this method returns unless it
   * throws an exception.
   *
   * @throws UnsupportedOperationException if the <tt>clear</tt> method is
   *         not supported by this collection.
   */
  public void clear() {validationExceptionList.clear();}

}
