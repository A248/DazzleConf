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

package space.arim.dazzleconf.engine;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import space.arim.dazzleconf.backend.CommentData;
import space.arim.dazzleconf.backend.KeyPath;

import java.lang.reflect.AnnotatedElement;

/**
 * Provides resources while the configuration is being built.
 */
public interface DefiningContext {

    /**
     * Interprocessor key for {@link LoadNodeComments}.
     * <p>
     * The default value of this hook resolves comments from the following sources:
     * <ol>
     *     <li>If {@link LangComments} is set, using the translation key to look up a translated comment value from the {@link TranslationResolve}</li>
     *     <li>If {@code LangComments} is not set, using the label path as a translation key likewise.</li>
     *     <li>Otherwise, {@link Comments} if it exists.</li>
     * </ol>
     */
    Interprocessor.@NonNull HookKey<LoadNodeComments> LOAD_NODE_COMMENTS = new Interprocessor.HookKey<LoadNodeComments>() {
        @Override
        public @NonNull LoadNodeComments defaultValue() {
            return new LoadNodeComments() {
                @Override
                public @NonNull CommentData loadComments(TypeLiaison.@NonNull CommentInit commentInit) {
                    AnnotatedElement methodAnnotations = commentInit.methodAnnotations();
                    TranslationResolve translationResolve = commentInit.getTranslationResolve();
                    LangComments langComments = methodAnnotations.getAnnotation(LangComments.class);
                    if (langComments == null) {
                        CommentData fromLabelAuto = translationResolve.resolveComments(commentInit.labelPath(), true);
                        if (fromLabelAuto != null) {
                            return fromLabelAuto;
                        }
                    } else {
                        KeyPath translationKey = KeyPath.parse(langComments.value());
                        CommentData fromLangComments = translationResolve.resolveComments(translationKey, false);
                        if (fromLangComments != null) {
                            return fromLangComments;
                        }
                    }
                    return CommentData.buildFrom(methodAnnotations.getAnnotationsByType(Comments.class));
                }
            };
        }
    };

    /**
     * Gets the interprocessor passed to {@link space.arim.dazzleconf.ConfigurationBuilder#definingInterprocessor(Interprocessor)},
     * or else the default interprocessor.
     *
     * @return the interprocessor used to help define the configuration. Serializers should not store this
     * object, but rather make use of {@link OperationContext#getInterprocessor()} during reading and writing.
     */
    @Pure
    @NonNull Interprocessor getDefiningInterprocessor();

    /**
     * Gets the translation resolver provided by the library user
     *
     * @return the translation resolve
     */
    @Pure
    @NonNull TranslationResolve getTranslationResolve();

    /**
     * A hook that is used while building the configuration definition, to provide a node's comments. Typically added
     * to the defining interprocessor ({@link #getDefiningInterprocessor()}).
     * <p>
     * This hook is called by the default implementation of {@link TypeLiaison.Agent#loadComments(TypeLiaison.CommentInit)}
     */
    interface LoadNodeComments {

        /**
         * Loads comments on a configuration node.
         *
         * @param commentInit the comment init
         * @return the comments
         */
        @NonNull CommentData loadComments(TypeLiaison.@NonNull CommentInit commentInit);

    }

}
