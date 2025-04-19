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

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Result container for fallible configuration related operations.
 * <p>
 * This container is immutable and stores either a success value or an error value. Which is stored can be checked
 * with {@link #isSuccess()}
 *
 * @param <R> the type of the yielded value
 */
public final class LoadResult<R> {

    /*
    ErrorContext if an error occured. R otherwise.
     */
    private final Object value;

    private LoadResult(Object value) {
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Makes a successful load result
     * @param success the success value
     * @return the load result
     */
    public static <R> LoadResult<R> of(R success) {
        return new LoadResult<>(success);
    }

    /**
     * Creates a failed load result with the given error coontext
     * @param reason the error context
     * @return the load result
     */
    public static <R> LoadResult<R> failure(ErrorContext reason) {
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
     * Checks if succeeded. If true, a success value will be present
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return !(value instanceof ErrorContext);
    }

    /**
     * Checks if failed. Opposite of {@link #isSuccess()}
     *
     * @return true if failed and an error is present
     */
    public boolean isFailure() {
        return value instanceof ErrorContext;
    }

    /**
     * Tries to get at the success value
     *
     * @return the success value, or an empty optional if failed
     */
    public Optional<R> getValue() {
        return Optional.ofNullable(successValue());
    }

    /**
     * Tries to get at the error value
     *
     * @return the error value, or an empty optional if it does not exist
     */
    public Optional<ErrorContext> getError() {
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
    public <R2> LoadResult<R2> map(Function<? super R, R2> mapper) {
        R current = successValue();
        if (current == null) {
            // We can re-use our own object to pass on the error context
            @SuppressWarnings("unchecked")
            LoadResult<R2> casted = (LoadResult<R2>) this;
            return casted;
        }
        R2 updated = mapper.apply(current);
        return new LoadResult<>(updated);
    }

    /**
     * Maps from one load result to another. If this load result is in error, nothing happens.
     *
     * @param mapper the mapping function
     * @return the new result
     * @param <R2> the type of the new returnable value
     */
    public <R2> LoadResult<R2> flatMap(Function<? super R, ? extends LoadResult<R2>> mapper) {
        R current = successValue();
        if (current == null) {
            // We can re-use our own object to pass on the error context
            @SuppressWarnings("unchecked")
            LoadResult<R2> casted = (LoadResult<R2>) this;
            return casted;
        }
        return mapper.apply(current);
    }

    /**
     * Runs an action on the success value. If this load result is in error, nothing happens.
     *
     * @param action the action
     */
    public void ifSuccess(Consumer<? super R> action) {
        R current = successValue();
        if (current != null) {
            action.accept(current);
        }
    }

    /**
     * Unwraps the success value or throws an exception
     *
     * @return the success value
     * @throws NoSuchElementException if the success value is not present, and the exception message will be set to
     * the error context display ({@link ErrorContext#display()})
     */
    public R getOrThrow() {
        R current = successValue();
        if (current == null) {
            ErrorContext errorContext = (ErrorContext) value;
            throw new NoSuchElementException(errorContext.display());
        }
        return current;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LoadResult)) return false;

        LoadResult<?> that = (LoadResult<?>) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "LoadResult{" + value + '}';
    }
}
