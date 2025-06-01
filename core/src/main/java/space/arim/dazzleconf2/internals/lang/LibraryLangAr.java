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

import java.util.Arrays;
import java.util.Locale;

import static space.arim.dazzleconf2.backend.Printable.preBuilt;

public final class LibraryLangAr extends LibraryLang.Base {

    LibraryLangAr(@Nullable Locale overrideLocale) {
        super(overrideLocale);
    }

    @Override
    protected Locale getActualLocale() {
        return Locale.forLanguageTag("ar");
    }

    @Override
    public @NonNull LibraryLang pretendToUseLocale(@NonNull Locale usingLocale) {
        return new LibraryLangAr(usingLocale);
    }

    @Override
    public @NonNull String location() {
        return "موقع";
    }

    @Override
    public @NonNull String line() {
        return "خط";
    }

    @Override
    public @NonNull String backendMessage() {
        return "";
    }

    @Override
    public @NonNull String missingValue() {
        return "لا يوجد إختيار هنا ولكنه مفروض.";
    }

    @Override
    public @NonNull Printable wrongTypeForValue(Object value, String expectedType, String actualType) {
        return preBuilt("الإختيار < " + value + " > من نوع " + actualType + " ولكنه مفروض من " + expectedType + '.');
    }

    @Override
    public @NonNull Printable mustBeBetween(String value, Number min, Number max) {
        return preBuilt("يجب ان يكون الإختيار بين " + min + " و" + max + " ولكنه بالفعل " + value + ".");
    }

    @Override
    public @NonNull Printable notAccepted(@NonNull String value, @NonNull String[] permitted) {
        return preBuilt("الإختيار < " + value + " > غير مقبول. لزاماً يكون بين " + Arrays.toString(permitted));
    }

    @Override
    public @NonNull String forExample() {
        return "مثلاً";
    }

    @Override
    public @NonNull String badValue() {
        return "الإعداد هنا لا يناسب.";
    }

    @Override
    public @NonNull String errorIntro() {
        return "لقينا عقبات تمنع تحميل الإادادات";
    }

    @Override
    public @NonNull String errorContext() {
        return "اين تمت الاخطاء او كيفيتها";
    }

    @Override
    public @NonNull String more(int howMany) {
        return formatNumber(howMany) + " كمان...";
    }

    @Override
    public @NonNull String trueFalse() {
        return "'true/false'";
    }

    @Override
    public @NonNull String text() {
        return "النص";
    }

    @Override
    public @NonNull String smallInteger() {
        return "العدد الصحيح الصغير";
    }

    @Override
    public @NonNull String integer() {
        return "العدد الصحيح";
    }

    @Override
    public @NonNull String character() {
        return "الحرف";
    }

    @Override
    public @NonNull String decimal() {
        return "العدد عشري";
    }

    @Override
    public @NonNull String list() {
        return "القائمة";
    }

    @Override
    public @NonNull String configurationSection() {
        return "مجموعة إلاعدادات";
    }
}
