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

package space.arim.dazzleconf.internals.lang;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Locale;

/**
 * This interface should be implemented for every language used.
 *
 */
public interface LanguageCandidate extends LanguageMessages {

    /**
     * Gets the locale this translation represents.
     * <p>
     * This should be the locale that best describes the messages provided in this translation. Often, this translation
     * will represent a language that covers more than one region (e.g., there are many countries that speak English),
     * so this method should return the locale at the broad level (e.g., {@code Locale.ENGLISH}).
     *
     * @return the actual locale this translation represents
     */
    Locale getActualLocale();

    /**
     * Whether this translation can work for the specified locale. As in, someone using the specified locale
     * can read messages using this translation.
     * <p>
     * For example, let's say this translation represents {@link Locale#FRENCH}. In that case, it would also support
     * {@link Locale#FRANCE}, {@link Locale#CANADA_FRENCH}, Burkina Faso French, Algerian French, and the French spoken
     * in French Guiana, as well as basically all the other former colonies that won their independence from the cruel
     * and extremely bloody French empire.
     *
     * @param usingLocale the locale being used, usually the system locale
     * @return true if this translation is usable for the argument locale, false otherwise
     */
    default boolean supportsLocale(@NonNull Locale usingLocale) {
        return getActualLocale().getLanguage().equals(usingLocale.getLanguage());
    }

}
