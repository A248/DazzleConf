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

package space.arim.dazzleconf2.reflect;

import java.util.Arrays;
import java.util.Objects;

/**
 * A potentially generic type, with its arguments fully specified
 *
 */
public final class ReifiedType {

    private final Class<?> rawType;
    private final ReifiedType[] arguments;

    /**
     * Builds from nonnull input arguments.
     *
     * @param rawType the raw type
     * @param arguments the arguments, which are copied to ensure immutability
     */
    public ReifiedType(Class<?> rawType, ReifiedType[] arguments) {
        this.rawType = Objects.requireNonNull(rawType, "rawType");
        this.arguments = arguments.clone();
    }

    /**
     * Gets the raw type, unparameterized
     *
     * @return the raw type
     */
    public Class<?> rawType() {
        return rawType;
    }

    /**
     * Gets the argument at a certain index
     *
     * @param index the index
     * @return the argument at it
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public ReifiedType argumentAt(int index) {
        return arguments[index];
    }

    /**
     * The argument count
     * @return the argument count
     */
    public int argumentCount() {
        return arguments.length;
    }

    /**
     * Gets all the arguments
     *
     * @return a copy of the arguments
     */
    public ReifiedType[] arguments() {
        return arguments.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReifiedType)) return false;

        ReifiedType that = (ReifiedType) o;
        return rawType.equals(that.rawType) && Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = rawType.hashCode();
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    @Override
    public String toString() {
        return "ReifiedType{" +
                "rawType=" + rawType +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }
}
