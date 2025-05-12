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

import java.util.Locale;

public final class LibraryLangAr implements LibraryLang {

    LibraryLangAr() {}

    @Override
    public @NonNull Locale getLocale() {
        return Locale.forLanguageTag("ar");
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
        return "لا يوجد إعداد هنا";
    }

    @Override
    public @NonNull String wrongTypeForValue(Object value, String expectedType, String actualType) {
        return "الإعداد < " + value + " > ليس من نوع " + expectedType + " بل " + actualType + " فعلاً";
    }

    @Override
    public @NonNull String mustBeBetween(String value, Number min, Number max) {
        return "يجب ان < " + value + " > يكون بين " + min + " و" + max;
    }

    @Override
    public @NonNull String malformattedValue(String reason) {
        return "هذا الإعداد ليس مرتباً جيداً لانه " + reason;
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
    public @NonNull String moreErrors(int howMany) {
        String number, singularOrPlural;
        if (howMany == 1) {
            number = "";
            singularOrPlural = "خطأ";
        } else if (howMany <= 10) {
            number = " " + howMany;
            singularOrPlural = "اخطاء";
        } else {
            number = " " + howMany;
            singularOrPlural = "خطأ";
        }
        return "بالإضافة الى" + number + " " + singularOrPlural + " كمان...";
    }

    @Override
    public @NonNull String text() {
        return "نص";
    }

    @Override
    public @NonNull String smallInteger() {
        return "عدد صحيح صغير";
    }

    @Override
    public @NonNull String integer() {
        return "عدد صحيح";
    }

    @Override
    public @NonNull String character() {
        return "حرف";
    }

    @Override
    public @NonNull String decimal() {
        return "عدد عشري";
    }

    @Override
    public @NonNull String list() {
        return "قائمة";
    }

    @Override
    public @NonNull String configurationSection() {
        return "مجموعة إعدادات";
    }
}
