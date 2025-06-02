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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

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
    public void loadAny() {
        PrintStream originalSysOut = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        try {
            for (Locale locale : Locale.getAvailableLocales()) {
                LibraryLang loaded = assertDoesNotThrow(() -> LibraryLang.loadLang(locale), () -> "threw for " + locale);
                assertNotNull(loaded, () -> "loaded null for " + locale);
            }
        } finally {
            System.setOut(originalSysOut);
        }
    }

    @Test
    public void preserveDialect() {
        assertEquals(Locale.CANADA, LibraryLang.loadLang(Locale.CANADA).getLocale());
        Locale arJo = new Locale("ar", "JO");
        assertEquals(arJo, LibraryLang.loadLang(arJo).getLocale());
    }
}
