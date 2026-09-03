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
import space.arim.dazzleconf.engine.DefiningContext;
import space.arim.dazzleconf.engine.Interprocessor;
import space.arim.dazzleconf.engine.TranslationResolve;

import java.util.Objects;

class DefiningContextImpl implements DefiningContext {

    private final Interprocessor definingInterprocessor;
    private final TranslationResolve translationResolve;

    DefiningContextImpl(Interprocessor definingInterprocessor, TranslationResolve translationResolve) {
        this.definingInterprocessor = Objects.requireNonNull(definingInterprocessor, "definingInterprocessor");
        this.translationResolve = Objects.requireNonNull(translationResolve, "translationResolve");
    }

    DefiningContextImpl(DefiningContext context) {
        this(context.getDefiningInterprocessor(), context.getTranslationResolve());
    }

    @Override
    public @NonNull Interprocessor getDefiningInterprocessor() {
        return definingInterprocessor;
    }

    @Override
    public @NonNull TranslationResolve getTranslationResolve() {
        return translationResolve;
    }
}
