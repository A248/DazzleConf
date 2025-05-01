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

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf2.internals.lang.LibraryLang;
import space.arim.dazzleconf2.internals.lang.LibraryLangAr;
import space.arim.dazzleconf2.internals.lang.LibraryLangEn;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LibraryLangTest {

    @Test
    public void loadEnglish() {
        assert LibraryLang.loadLang(Locale.ENGLISH) instanceof LibraryLangEn;
        assert LibraryLang.loadLang(Locale.US) instanceof LibraryLangEn;
        assert LibraryLang.loadLang(Locale.UK) instanceof LibraryLangEn;
        assert LibraryLang.loadLang(Locale.CANADA) instanceof LibraryLangEn;
    }

    @Test
    public void loadArabic() {
        assert LibraryLang.loadLang(new Locale("ar")) instanceof LibraryLangAr;
        assert LibraryLang.loadLang(new Locale("ar", "JO")) instanceof LibraryLangAr;
        assert LibraryLang.loadLang(new Locale("ar", "EG")) instanceof LibraryLangAr;
        assert LibraryLang.loadLang(new Locale("ar", "DZ")) instanceof LibraryLangAr;
    }

    @Test
    public void arabicMessages() {
        LibraryLang arabic = LibraryLang.loadLang(new Locale("ar"));
        assertEquals("بالإضافة الى خطأ كمان...", arabic.moreErrors(1));
        assertEquals("بالإضافة الى 3 اخطاء كمان...", arabic.moreErrors(3));
        assertEquals("بالإضافة الى 12 خطأ كمان...", arabic.moreErrors(12));
    }
}
