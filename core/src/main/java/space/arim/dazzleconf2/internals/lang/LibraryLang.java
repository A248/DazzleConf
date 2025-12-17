/*
 * DazzleConf
 * Copyright © 2026 Anand Beh
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
import space.arim.dazzleconf2.backend.DataList;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.Printable;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * Language translation for the library itself. If you want to provide a translation for your locale, implement a
 * {@link LanguageCandidate}, and <b>please</b> PR it back to the main repository.
 */
public final class LibraryLang implements LanguageMessages {

    private final Locale usingLocale;
    private final LanguageCandidate chosen;

    private LibraryLang(Locale usingLocale, LanguageCandidate chosen) {
        this.usingLocale = usingLocale;
        this.chosen = chosen;
    }

    public interface Accessor {

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
     * Gets the locale this {@code LibraryLang} is servicing
     *
     * @return the locale being serviced
     */
    public @NonNull Locale getLocale() {
        return usingLocale;
    }

    /**
     * Gets the actual locale used to provide translated messages
     *
     * @return the locale used for translations
     */
    public @NonNull Locale getActualLocale() {
        return chosen.getActualLocale();
    }

    public LanguageCandidate getChosen() {
        return chosen;
    }

    private static @Nullable LanguageCandidate bestCandidate(@NonNull Locale usingLocale) {
        LanguageCandidate bestGuess = null;
        for (LanguageCandidate candidate : ReadMe.availableLanguages()) {

            if (candidate.supportsLocale(usingLocale)) {
                // Locale supported! But is it exactly the same?
                if (candidate.getActualLocale().equals(usingLocale)) {
                    // Exactly the same
                    return candidate;
                }
                bestGuess = candidate;
            }
        }
        return bestGuess;
    }

    public static @NonNull LibraryLang loadLang(@NonNull Locale usingLocale) {
        LanguageCandidate chosen = bestCandidate(usingLocale);
        if (chosen == null) {
            // 2.0.0-RC1: Remove this System.out message
            // When DazzleConf 2.0 has exited the preview phase, we will remove this message. Right now, it exists
            // to help identify missing translations and encourage contributions.
            //
            // Codebase: upon exiting the preview phase, make the following changes:
            // 1. Make Locale configurable using ConfigurationBuilder#locale, and re-enable the test for it
            // 2. Remove this System.out message
            // 3. Remove the proofing for System.out usage in LibraryLangTest.java


            // Non-matched locale. If you've arrived here, this is your opportunity to contribute a PR
            System.out.println(
                    "Message from DazzleConf 2 (preview): The requested language " + usingLocale.getLanguage() +
                            " does not have a translation yet. If you would like to contribute one, please make a PR."
            );
            chosen = new LangEn();
        }
        return new LibraryLang(usingLocale, chosen);
    }

    /**
     * Simple functional interface for dealing with {@link LibraryLang}
     */
    public interface Key {
    
        /**
         * Gets the desired message
         * @param libraryLang the library language chosen
         * @return the message selected
         */
        String getMessage(LibraryLang libraryLang);
    
    }

    // Formatting-related items

    public @NonNull Printable wrongTypeForValue(@NonNull Object value, @NonNull Class<?> expectedType) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(expectedType, "expectedType");
        return new Printable.Abstract() {
            @Override
            public void printTo(@NonNull Appendable output) throws IOException {
                output.append(badValue());
                output.append(' ');
                wrongTypeForValue(
                        value,
                        displayCanonicalType(expectedType),
                        displayCanonicalType(value.getClass())
                ).printTo(output);
            }
        };
    }

    public @NonNull Printable outOfRange(@NonNull Object value, @NonNull Number min, @NonNull Number max) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        return new Printable.Abstract() {
            @Override
            public void printTo(@NonNull Appendable output) throws IOException {
                output.append(badValue());
                output.append(' ');
                mustBeBetween(
                        displayCanonicalType(value.getClass()), min, max
                ).printTo(output);
            }
        };
    }

    public @NonNull String formatNumber(@NonNull Number value) {
        return NumberFormat.getInstance(usingLocale).format(value);
    }

    public @NonNull Printable notANumber(@NonNull Number value) {
        Objects.requireNonNull(value, "value");
        return new Printable.Abstract() {
            @Override
            public void printTo(@NonNull Appendable output) throws IOException {
                output.append(badValue());
                output.append(' ');
                wrongTypeForValue(
                        value,
                        displayCanonicalType(value.getClass()),
                        formatNumber(value)
                ).printTo(output);
            }
        };
    }

    private String displayCanonicalType(Class<?> type) {
        assert !type.isPrimitive() : "Use wrapper type";

        if (type.equals(String.class)) {
            return text();
        }
        if (type.equals(Boolean.class)) {
            return trueFalse();
        }
        if (type.equals(Byte.class)) {
            return smallInteger();
        }
        if (type.equals(Short.class) || type.equals(Integer.class) || type.equals(Long.class)) {
            return integer();
        }
        if (type.equals(Character.class)) {
            return character();
        }
        if (type.equals(Float.class) || type.equals(Double.class)) {
            return decimal();
        }
        if (DataList.class.isAssignableFrom(type)) {
            return list();
        }
        if (DataTree.class.isAssignableFrom(type)) {
            return configurationSection();
        }
        throw new IllegalArgumentException("Not a canonical type " + type);
    }

    public @NonNull String more(int howMany) {
        return chosen.more(formatNumber(howMany));
    }

    // BEGIN DELEGATION

    @Override
    public @NonNull String location() {
        return chosen.location();
    }

    @Override
    public @NonNull String line() {
        return chosen.line();
    }

    @Override
    public @NonNull String backendMessage() {
        return chosen.backendMessage();
    }

    @Override
    public @NonNull String syntaxLinter() {
        return chosen.syntaxLinter();
    }

    @Override
    public @NonNull String failed() {
        return chosen.failed();
    }

    @Override
    public @NonNull String missingValue() {
        return chosen.missingValue();
    }

    @Override
    public @NonNull Printable wrongTypeForValue(Object value, String expectedType, String actualType) {
        return chosen.wrongTypeForValue(value, expectedType, actualType);
    }

    @Override
    public @NonNull Printable mustBeBetween(String value, Number min, Number max) {
        return chosen.mustBeBetween(value, min, max);
    }

    @Override
    public @NonNull Printable notAccepted(@NonNull String value, @NonNull String[] permitted) {
        return chosen.notAccepted(value, permitted);
    }

    @Override
    public @NonNull String forExample() {
        return chosen.forExample();
    }

    @Override
    public @NonNull String badValue() {
        return chosen.badValue();
    }

    @Override
    public @NonNull String more(String what) {
        return chosen.more(what);
    }

    @Override
    public @NonNull String errorIntro() {
        return chosen.errorIntro();
    }

    @Override
    public @NonNull String errorContext() {
        return chosen.errorContext();
    }

    @Override
    public @NonNull String trueFalse() {
        return chosen.trueFalse();
    }

    @Override
    public @NonNull String text() {
        return chosen.text();
    }

    @Override
    public @NonNull String smallInteger() {
        return chosen.smallInteger();
    }

    @Override
    public @NonNull String integer() {
        return chosen.integer();
    }

    @Override
    public @NonNull String character() {
        return chosen.character();
    }

    @Override
    public @NonNull String decimal() {
        return chosen.decimal();
    }

    @Override
    public @NonNull String list() {
        return chosen.list();
    }

    @Override
    public @NonNull String configurationSection() {
        return chosen.configurationSection();
    }

    @Override
    public @NonNull String syntaxInvalidPleaseTryAt(@NonNull URL url) {
        return chosen.syntaxInvalidPleaseTryAt(url);
    }

    @Override
    public @NonNull String syntax() {
        return chosen.syntax();
    }

    @Override
    public @NonNull String otherReason() {
        return chosen.otherReason();
    }

    @Override
    public @NonNull String yamlNotAMap() {
        return chosen.yamlNotAMap();
    }

    @Override
    public @NonNull String tomlDateType() {
        return chosen.tomlDateType();
    }

    // END DELEGATION

}
