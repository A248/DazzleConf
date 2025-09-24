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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryLangTest {

    @Test
    public void loadEnglish() {
        assert LibraryLang.loadLang(Locale.ENGLISH).getChosen() instanceof LangEn;
        assert LibraryLang.loadLang(Locale.US).getChosen() instanceof LangEn;
        assert LibraryLang.loadLang(Locale.UK).getChosen() instanceof LangEn;
        assert LibraryLang.loadLang(Locale.CANADA).getChosen() instanceof LangEn;
    }

    @Test
    public void loadArabic() {
        assert LibraryLang.loadLang(Locale.of("ar")).getChosen() instanceof LangAr;
        assert LibraryLang.loadLang(Locale.of("ar", "JO")).getChosen() instanceof LangAr;
        assert LibraryLang.loadLang(Locale.of("ar", "EG")).getChosen() instanceof LangAr;
        assert LibraryLang.loadLang(Locale.of("ar", "DZ")).getChosen() instanceof LangAr;
    }

    @Test
    public void loadAny() {
        // See LibraryLang for when to remove this System.out check
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
        Locale arJo = Locale.of("ar", "JO");
        assertEquals(arJo, LibraryLang.loadLang(arJo).getLocale());
    }
}
