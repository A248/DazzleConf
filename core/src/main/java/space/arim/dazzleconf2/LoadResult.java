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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.internals.ImmutableCollections;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
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
    public static <R> @NonNull LoadResult<R> of(R success) {
        return new LoadResult<>(success, true);
    }

    /**
     * Creates a failed load result with the given error contexts
     * @param reasons the error contexts
     * @return the load result
     */
    public static <R> @NonNull LoadResult<R> failure(@NonNull ErrorContext @NonNull...reasons) {
        return new LoadResult<>(ImmutableCollections.listOf(reasons), false);
    }

    /**
     * Creates a failed load result with the given error contexts
     * @param reasons the error contexts
     * @return the load result
     */
    public static <R> @NonNull LoadResult<R> failure(@NonNull List<@NonNull ErrorContext> reasons) {
        return new LoadResult<>(ImmutableCollections.listOf(reasons), false);
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
     * Tries to get at the success value.
     * <p>
     * This might be null either if the result is a failure, or the success value itself is null.
     *
     * @return the success value, or null if failed
     */
    public @Nullable R getValue() {
        @SuppressWarnings("unchecked")
        R casted = success ? (R) value : null;
        return casted;
    }

    /**
     * Gets the error contexts.
     * <p>
     * Note that this list may be empty if either the load result was a success, or an empty list of error contexts
     * were passed.
     *
     * @return the error contexts, immutable
     */
    public @NonNull List<@NonNull ErrorContext> getErrorContexts() {
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
    public <R_NEW> @NonNull LoadResult<R_NEW> map(Function<? super R, R_NEW> mapper) {
        if (success) {
            @SuppressWarnings("unchecked")
            R original = (R) value;
            R_NEW updated = mapper.apply(original);
            return new LoadResult<>(updated, true);
        }  else {
            // We can re-use our own object to pass on the error context
            @SuppressWarnings("unchecked")
            LoadResult<R_NEW> casted = (LoadResult<R_NEW>) this;
            return casted;
        }
    }

    /**
     * Maps from one load result to another. If this load result is in error, nothing happens.
     *
     * @param mapper the mapping function
     * @return the new result
     * @param <R_NEW> the type of the new returnable value
     */
    public <R_NEW> @NonNull LoadResult<R_NEW> flatMap(Function<? super R, ? extends LoadResult<R_NEW>> mapper) {
        if (success) {
            @SuppressWarnings("unchecked")
            R original = (R) value;
            return mapper.apply(original);
        } else {
            // We can re-use our own object to pass on the error context
            @SuppressWarnings("unchecked")
            LoadResult<R_NEW> casted = (LoadResult<R_NEW>) this;
            return casted;
        }
    }

    /**
     * Runs an action on the success value. If this load result is in error, nothing happens.
     *
     * @param action the action
     */
    public void ifSuccess(Consumer<? super R> action) {
        if (success) {
            action.accept(getValue());
        }
    }

    /**
     * Unwraps the success value or throws an exception
     *
     * @return the success value
     * @throws NoSuchElementException if the success value is not present
     */
    public R getOrThrow() {
        if (!success) {
            throw new NoSuchElementException("Success value not present");
        }
        return getValue();
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
