/*
 * DazzleConf
 * Copyright © 2025 Anand Beh
 *
 * DazzleConf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DazzleConf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */

package space.arim.dazzleconf2.engine;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies that an interface is a configuration subsection. This is a type level annotation, meaning it can be
 * applied not just to return types, but also generic parameters of return types.
 * <p>
 * The following example is fully functional.
 * <pre>
 * {@code
 * public interface BoredomEvaluationConfig {

 *   default Map<String, @SubSection BoredomDetails> boredomFromMarvelMovies() {
 *     return Map.of(
 *       "Hulk", new BoredomDetails() {
 *           public int minutesOfBoredom() { return 120; }
 *           public boolean doYouWantIt() { return false; }
 *       },
 *       "Avengers", new BoredomDetails() {
 *           public int minutesOfBoredom() { return 220; }
 *           public boolean doYouWantIt() { return true; }
 *       }
 *     );
 *   }
 *
 *   interface BoredomDetails {
 *
 *     @DefaultInteger(3)
 *     int minutesOfBoredom();
 *
 *     @DefaultBoolean(true)
 *     boolean doYouWantIt();
 *
 *   }
 * }
 * }
 * </pre>
 *
 * @author A248
 *
 */
@Retention(RUNTIME)
@Target(TYPE_USE)
public @interface SubSection {

}
