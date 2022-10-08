/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.framework.component;

/**
 * Base interface for boxes within a frame.
 * <p>
 * There are several types of often-used panels, in increasing order of complexity and functionality:
 * </p>
 * <dl>
 * <dt>{@link LayoutPanel}</dt>
 * <dd>A class only used for laying out child components without providing extra arrangement such as inter-child-component spacing.</dd>
 * <dt>{@link ArrangePanel}</dt>
 * <dd>A panel that semantically demarcates an area that arranges other components. This panel is usually styled to arrange the spacing between child
 * components.</dd>
 * <dt>{@link SectionPanel}</dt>
 * <dd>A panel that demarcates a semantically significant area of the a parent component with arranged child components.</dd>
 * </dl>
 * @author Garret Wilson
 */
public interface Panel extends Box, Container {
}
