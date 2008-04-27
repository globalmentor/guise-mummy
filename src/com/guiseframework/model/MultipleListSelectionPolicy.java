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

package com.guiseframework.model;

/**An implementation of a list selection strategy for a list select model allowing unlimited selections.
This class is marked final because it demarcates certain selection semantics that, if they can be assumed, may be offloaded to a component's view in certain circumstances. 
This class is thread-safe, and assumes that the corresponding select model is thread-safe, synchronized on itself.
@param <V> The type of values contained in the select model.
@author Garret Wilson
@see ListSelectModel
*/
public final class MultipleListSelectionPolicy<V> extends AbstractListSelectionPolicy<V>
{
}
