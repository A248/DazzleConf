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

package space.arim.dazzleconf2;

import java.util.Optional;
import java.util.function.Function;

/**
 * General purpose result class for configuration loading errors.
 * @param <R> the type of the yielded value
 */
public final class LoadResult<R> {

    /*
    ErrorContext if an error occured. R otherwise.
     */
    private final Object value;

    private LoadResult(Object value) {
        this.value = value;
    }

    /**
     * Makes a successful load result
     * @param success the success value
     * @return the load result
     */
    public LoadResult<R> of(R success) {
        return new LoadResult<>(success);
    }

    /**
     * Creates a failed load result with the given error coontext
     * @param reason the error context
     * @return the load result
     */
    public LoadResult<R> failure(ErrorContext reason) {
        return new LoadResult<>(reason);
    }

    @SuppressWarnings("unchecked")
    private R successValue() {
        if (value instanceof ErrorContext) {
            return null;
        }
        return (R) value;
    }

    /**
     * Unwraps this result to get at the value
     *
     * @return the inner value, or an empty optional if unset
     */
    public Optional<R> unwrap() {
        return Optional.ofNullable(successValue());
    }

    /**
     * Unwraps this result to get at the error
     *
     * @return the inner error, or an empty optional if unset
     */
    public Optional<ErrorContext> unwrapError() {
        if (value instanceof ErrorContext) {
            return Optional.of((ErrorContext) value);
        }
        return Optional.empty();
    }

    /**
     * Maps from one load result to another. If this load result is in error, nothing happens.
     *
     * @param mapper the mapping function
     * @return the new result
     * @param <R2> the type of the new returnable value
     */
    @SuppressWarnings("unchecked")
    public <R2> LoadResult<R2> map(Function<R, R2> mapper) {
        R current = successValue();
        if (current == null) {
            // We can re-use our own object to pass on the error context
            return (LoadResult<R2>) this;
        }
        R2 updated = mapper.apply(current);
        return new LoadResult<>(updated);
    }
}
