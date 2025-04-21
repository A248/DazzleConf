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

import space.arim.dazzleconf.internal.util.ImmutableCollections;

import java.util.List;
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
    List<ErrorContext> if an error occured. R otherwise.
     */
    private final Object value;
    private final boolean success;

    private LoadResult(Object value, boolean success) {
        this.value = Objects.requireNonNull(value);
        this.success = success;
    }

    /**
     * Makes a successful load result
     * @param success the success value
     * @return the load result
     */
    public static <R> LoadResult<R> of(R success) {
        return new LoadResult<>(success, true);
    }

    /**
     * Creates a failed load result with the given error contexts
     * @param reasons the error contexts
     * @return the load result
     */
    public static <R> LoadResult<R> failure(ErrorContext...reasons) {
        return new LoadResult<>(ImmutableCollections.listOf(reasons), false);
    }

    /**
     * Creates a failed load result with the given error contexts
     * @param reasons the error contexts
     * @return the load result
     */
    public static <R> LoadResult<R> failure(List<ErrorContext> reasons) {
        return new LoadResult<>(ImmutableCollections.listOf(reasons), false);
    }

    @SuppressWarnings("unchecked")
    private R successValue() {
        return success ? (R) value : null;
    }

    /**
     * Checks if succeeded. If true, a success value will be present
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Checks if failed. Opposite of {@link #isSuccess()}
     *
     * @return true if failed and an error is present
     */
    public boolean isFailure() {
        return !success;
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
     * Gets the error contexts.
     * <p>
     * Note that this list may be empty if either the load result was a success, or an empty list of error contexts
     * were passed.
     *
     * @return the error contexts, nonnull and immutable
     */
    public List<ErrorContext> getErrorContexts() {
        if (success) {
            return ImmutableCollections.emptyList();
        }
        @SuppressWarnings("unchecked")
        List<ErrorContext> contexts = (List<ErrorContext>) value;
        return contexts;
    }

    /**
     * Maps from one load result to another. If this load result is in error, nothing happens.
     *
     * @param mapper the mapping function
     * @return the new result
     * @param <R_NEW> the type of the new returnable value
     */
    public <R_NEW> LoadResult<R_NEW> map(Function<? super R, R_NEW> mapper) {
        R current = successValue();
        if (current == null) {
            // We can re-use our own object to pass on the error context
            @SuppressWarnings("unchecked")
            LoadResult<R_NEW> casted = (LoadResult<R_NEW>) this;
            return casted;
        }
        R_NEW updated = mapper.apply(current);
        return new LoadResult<>(updated, true);
    }

    /**
     * Maps from one load result to another. If this load result is in error, nothing happens.
     *
     * @param mapper the mapping function
     * @return the new result
     * @param <R_NEW> the type of the new returnable value
     */
    public <R_NEW> LoadResult<R_NEW> flatMap(Function<? super R, ? extends LoadResult<R_NEW>> mapper) {
        R current = successValue();
        if (current == null) {
            // We can re-use our own object to pass on the error context
            @SuppressWarnings("unchecked")
            LoadResult<R_NEW> casted = (LoadResult<R_NEW>) this;
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
     * @throws NoSuchElementException if the success value is not present
     */
    public R getOrThrow() {
        R current = successValue();
        if (current == null) {
            throw new NoSuchElementException("Success value not present");
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
