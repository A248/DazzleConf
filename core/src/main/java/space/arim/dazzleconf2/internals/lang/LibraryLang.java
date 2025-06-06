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

package space.arim.dazzleconf2.internals.lang;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.dazzleconf2.backend.Printable;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Function;

import static space.arim.dazzleconf2.backend.Printable.join;
import static space.arim.dazzleconf2.backend.Printable.preBuilt;

/**
 * Language translation for the library itself. If you want to implement this and provide a translation for your
 * locale, <b>please</b> PR it back to the main repository.
 */
public interface LibraryLang {

    interface Accessor {

        @NonNull LibraryLang getLibraryLang();

        static <S> LibraryLang access(S source, Function<S, Locale> localeFallback) {
            if (source instanceof Accessor) {
                return ((Accessor) source).getLibraryLang();
            } else {
                return LibraryLang.loadLang(localeFallback.apply(source));
            }
        }
    }

    /**
     * Gets the locale this {@code LibraryLang} represents
     *
     * @return the locale
     */
    @NonNull Locale getLocale();

    /**
     * Changes the output of {@link #getLocale()}, without actually changing the implementation of this
     * {@code LibraryLang} instance.
     *
     * @param usingLocale the locale to return instead
     * @return a new {@code LibraryLang} which is the same as this one, but reports its locale as the given argument
     */
    @NonNull LibraryLang pretendToUseLocale(@NonNull Locale usingLocale);

    /**
     * Whether this {@code LibraryLang} can work for the specified locale. As in, someone using the specified locale
     * can read messages using this {@code LibraryLang}.
     *
     * @param usingLocale the locale being used, usually the system locale
     * @return true if usable, false otherwise
     */
    boolean supportsLocale(@NonNull Locale usingLocale);

    @NonNull String location();

    @NonNull String line();

    @NonNull String backendMessage();

    @NonNull String syntaxLinter();

    @NonNull String failed();

    @NonNull String missingValue();

    @NonNull Printable wrongTypeForValue(Object value, String expectedType, String actualType);

    @NonNull Printable mustBeBetween(String value, Number min, Number max);

    @NonNull Printable notAccepted(@NonNull String value, @NonNull String[] permitted);

    @NonNull String forExample();

    @NonNull String badValue();

    @NonNull String more(int howMany);

    @NonNull String errorIntro();

    @NonNull String errorContext();

    default @NonNull Printable wrongTypeForValue(@NonNull Object value, @NonNull Class<?> expectedType) {
        return join(preBuilt(badValue()), preBuilt(" "), wrongTypeForValue(
                value,
                ReadMe.displayCanonicalType(this, expectedType),
                ReadMe.displayCanonicalType(this, value.getClass())
        ));
    }

    default @NonNull Printable outOfRange(@NonNull Object value, @NonNull Number min, @NonNull Number max) {
        return join(preBuilt(badValue()), preBuilt(" "), mustBeBetween(
                ReadMe.displayCanonicalType(this, value.getClass()), min, max
        ));
    }

    default @NonNull String formatNumber(@NonNull Number value) {
        return NumberFormat.getInstance(getLocale()).format(value);
    }

    default @NonNull Printable notANumber(@NonNull Number value) {
        return join(preBuilt(badValue()), preBuilt(" "), wrongTypeForValue(
                value,
                ReadMe.displayCanonicalType(this, value.getClass()),
                NumberFormat.getInstance(getLocale()).format(value)
        ));
    }

    @NonNull String trueFalse();

    @NonNull String text();

    @NonNull String smallInteger();

    @NonNull String integer();

    @NonNull String character();

    @NonNull String decimal();

    @NonNull String list();

    @NonNull String configurationSection();

    @NonNull String syntaxInvalidPleaseTryAt(@NonNull URL url);

    @NonNull String syntax();

    @NonNull String otherReason();

    @NonNull String yamlNotAMap();

    @NonNull String tomlDateType();

    static @NonNull LibraryLang loadLang(@NonNull Locale usingLocale) {

        for (LibraryLang candidate : ReadMe.availableLanguages()) {

            if (candidate.supportsLocale(usingLocale)) {
                // Locale supported! But is it exactly the same?
                if (candidate.getLocale().equals(usingLocale)) {
                    // Exactly the same
                    return candidate;
                }
                // Not exactly the same: so use pretendToUseLocale(), and make sure it is implemented correctly
                LibraryLang altered = candidate.pretendToUseLocale(usingLocale);
                assert candidate.getClass().equals(altered.getClass()) : "pretendToUseLocale() returned wrong impl";
                return altered;
            }
        }
        // Non-matched locale. If you've arrived here, this is your opportunity to contribute a PR
        System.out.println(
                "Message from DazzleConf 2 (preview): The requested language " + usingLocale.getLanguage() +
                        " does not have a translation yet. If you would like to contribute one, please make a PR."
        );
        return new LibraryLangEn(usingLocale);
    }

    abstract class Base implements LibraryLang {

        private final Locale locale;

        protected Base(@Nullable Locale overrideLocale) {
            this.locale = overrideLocale != null ? overrideLocale : getActualLocale();
        }

        protected abstract Locale getActualLocale();

        @Override
        public final @NonNull Locale getLocale() {
            return locale;
        }

        @Override
        public boolean supportsLocale(@NonNull Locale usingLocale) {
            return getActualLocale().getLanguage().equals(usingLocale.getLanguage());
        }
    }
}
