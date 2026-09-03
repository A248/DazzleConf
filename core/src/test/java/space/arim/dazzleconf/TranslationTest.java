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

package space.arim.dazzleconf;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.KeyPath;
import space.arim.dazzleconf.engine.CommentLocation;
import space.arim.dazzleconf.engine.Comments;
import space.arim.dazzleconf.engine.DefinedLayout;
import space.arim.dazzleconf.engine.DefinedNode;
import space.arim.dazzleconf.engine.LangComments;
import space.arim.dazzleconf.engine.LangDefault;
import space.arim.dazzleconf.engine.TranslationResolve;

import java.util.Locale;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class TranslationTest {

    public interface Config {

        @LangDefault("hello.key")
        String hello();

        @LangComments("wow.now")
        @Comments(value = "backup", location = CommentLocation.INLINE)
        default boolean yes() {
            return true;
        }

        @LangComments("wow.more")
        default String more() {
            return "more";
        }
    }

    private void assertComments(Configuration<?> configuration, String label, CommentData comments) {
        for (DefinedLayout.Branch<?> branch : configuration.getDefinedLayout().getBranches()) {
            for (DefinedNode<?, ?> node : branch.getNodes()) {
                if (node instanceof DefinedNode.Value<?, ?> valNode && node.methodId().name().equals(label)) {
                    assertEquals(comments, valNode.comments());
                    return;
                }
            }
        }
        fail("Expected to find method: " + label + " in layout " + configuration.getDefinedLayout());
    }

    @Test
    public void loadLangDefault() {
        Configuration<Config> configuration = Configuration.defaultBuilder(Config.class)
                .translation(locale -> new TranslationResolve() {
                    @Override
                    public @Nullable CommentData resolveComments(@NonNull KeyPath key, boolean auto) {
                        return null;
                    }

                    @Override
                    public @Nullable String resolveStringValue(@NonNull KeyPath valueKey) {
                        if (valueKey.toString().equals("hello.key")) {
                            return "hi";
                        }
                        return null;
                    }
                })
                .build();
        assertEquals("hi", configuration.loadDefaults().hello());
        assertComments(configuration, "yes", CommentData.empty().setAt(CommentLocation.INLINE, "backup"));
        assertComments(configuration, "more", CommentData.empty());
    }

    @Test
    public void dependOnLang() {
        CommentData engComments1 = CommentData.empty().setAt(CommentLocation.ABOVE, "assent");
        CommentData zhComments = CommentData.empty().setAt(CommentLocation.ABOVE, "同意");
        CommentData engComments2 = CommentData.empty().setAt(CommentLocation.INLINE, "yes");
        CommentData frComments = CommentData.empty().setAt(CommentLocation.ABOVE, "wi");
        Function<Locale, TranslationResolve> resolveFunction = locale -> new TranslationResolve() {
            @Override
            public @Nullable CommentData resolveComments(@NonNull KeyPath key, boolean auto) {
                if (key.toString().equals("wow.now")) {
                    assert !auto : "wow.now is an explicit key";
                    switch (locale.getLanguage()) {
                        case "en":
                            return engComments1;
                        case "zh":
                            return zhComments;
                        case "fr":
                            return CommentData.empty();
                        default:
                            break;
                    }
                }
                if (key.toString().equals("wow.more")) {
                    assert !auto : "wow.more is an explicit key";
                    if (locale.getLanguage().equals("en")) {
                        return engComments2;
                    }
                }
                if (key.toString().equals("hello")) {
                    assert auto : "hello is an automatic key";
                    if (locale.getLanguage().equals("fr")) {
                        return frComments;
                    }
                }
                return null;
            }

            @Override
            public @Nullable String resolveStringValue(@NonNull KeyPath valueKey) {
                if (valueKey.toString().equals("hello.key")) {
                    return "hi there";
                }
                return null;
            }
        };
        Configuration<Config> english = Configuration.defaultBuilder(Config.class)
                .locale(Locale.ENGLISH)
                .translation(resolveFunction)
                .build();
        assertComments(english, "hello", CommentData.empty());
        assertComments(english, "yes", engComments1);
        assertComments(english, "more", engComments2);
        assertEquals("hi there", english.loadDefaults().hello());

        Configuration<Config> chinese = Configuration.defaultBuilder(Config.class)
                .locale(Locale.SIMPLIFIED_CHINESE)
                .translation(resolveFunction)
                .build();
        assertComments(chinese, "hello", CommentData.empty());
        assertComments(chinese, "yes", zhComments);
        assertComments(chinese, "more", CommentData.empty());
        assertEquals("hi there", chinese.loadDefaults().hello());

        Configuration<Config> french = Configuration.defaultBuilder(Config.class)
                .locale(Locale.FRENCH)
                .translation(resolveFunction)
                .build();
        assertComments(french, "hello", frComments);
        assertComments(french, "yes", CommentData.empty());
        assertComments(french, "more", CommentData.empty());
    }

    public interface BadKey1 {
        @LangDefault("hello...")
        String hello();
    }

    public interface BadKey2 {
        @LangComments("no..no")
        default boolean no() {
            return false;
        }
    }

    @Test
    public void badTranslationKeys() {
        assertThrows(DeveloperMistakeException.class, () -> Configuration.defaultBuilder(BadKey1.class).build());
        assertThrows(DeveloperMistakeException.class, () -> Configuration.defaultBuilder(BadKey2.class).build());
    }
}
