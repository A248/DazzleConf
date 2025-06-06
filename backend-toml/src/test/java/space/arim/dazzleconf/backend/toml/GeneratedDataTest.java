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

package space.arim.dazzleconf.backend.toml;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.TestingErrorSource;
import space.arim.dazzleconf.backend.SimpleDocument;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneratedDataTest {

    private final StringRoot stringRoot = new StringRoot("");
    private final TomlBackend tomlBackend = new TomlBackend(stringRoot);
    private final ErrorContext.Source errorSource = new TestingErrorSource().makeErrorSource();

    @Test
    public void writeFloat() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("1r1G", new DataEntry(true));
        dataTree.set("Hxh", new DataEntry(0.99000555));
        tomlBackend.write(Backend.Document.simple(dataTree));
        assertDoesNotThrow(tomlBackend.read(errorSource)::getOrThrow, stringRoot.readString());
    }

    @Test
    public void writeKanjiKey() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("漢", new DataEntry(true));
        assertDoesNotThrow(() -> tomlBackend.write(Backend.Document.simple(dataTree)));
    }

    @Test
    public void writeBlurgeoningData() {
        /*
Immut{{
-29=DataEntry{value=8296509441357041660, lineNumber=null, comments=CommentData{{}}},
똊=DataEntry{value=false, lineNumber=null, comments=CommentData{{ABOVE=[QZSbcItn, 4JSs92nR, H], INLINE=[MLMCEJd], BELOW=[GgUOMZK, vy5, U5ERu3]}}},
false=DataEntry{value=0.17445159, lineNumber=null, comments=CommentData{{ABOVE=[Q3, HWuZBe2, r5rBRt6W], INLINE=[k], BELOW=[h, BzROM, rvRXWMX]}}},
AI0ons2K=DataEntry{value=-1130343348, lineNumber=null, comments=CommentData{{}}},
-2074373901=DataEntry{value=false, lineNumber=null, comments=CommentData{{}}},
42=DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}}}}
         */
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set(-29, new DataEntry(8296509441357041660L));
        dataTree.set("똊", new DataEntry(false).withComments(CommentData.empty()
                .setAt(CommentLocation.ABOVE, "QZSbcItn", "4JSs92nR", "H")
                .setAt(CommentLocation.INLINE, "MLMCEJd")
                .setAt(CommentLocation.BELOW, "GgUOMZK", "vy5", "U5ERu3")
        ));
        dataTree.set(false, new DataEntry(0.17445159).withComments(CommentData.empty()
                .setAt(CommentLocation.ABOVE, "Q3", "HWuZBe2", "r5rBRt6W")
                .setAt(CommentLocation.INLINE, "k")
                .setAt(CommentLocation.BELOW, "h", "BzROM", "rvRXWMX")
        ));
        dataTree.set("AI0ons2K", new DataEntry(-1130343348));
        dataTree.set(-2074373901, new DataEntry(false));
        dataTree.set(42, new DataEntry(new DataTree.Immut()));
        tomlBackend.write(Backend.Document.simple(dataTree));
        assertEquals("""
                -2074373901 = false
                -29 = 8296509441357041660
                AI0ons2K = -1130343348
                # Q3
                # HWuZBe2
                # r5rBRt6W
                false = 0.17445159 # k
                # h
                # BzROM
                # rvRXWMX
                # QZSbcItn
                # 4JSs92nR
                # H
                "똊" = false # MLMCEJd
                # GgUOMZK
                # vy5
                # U5ERu3
                
                [42]""", stringRoot.readString().trim());
    }

    @Test
    @Disabled("TODO; We still don't know what is going on here")
    public void rereadExtremelyWeird() {
/*
Failed to re-read data
SimpleDocument[comments=CommentData{{ABOVE=[�, , �mi�ý], INLINE=[�r��I�u���o:T�6�], BELOW=[]d,�, , �9�]}},
data=Immut{{
26=DataEntry{value=[
  DataEntry{value=[
    DataEntry{value=[
      DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}},
      DataEntry{value=[], lineNumber=null, comments=CommentData{{}}}
    ], lineNumber=null, comments=CommentData{{}}},
    DataEntry{value=[
      DataEntry{value=��9,:ש5`y���y|2���
                                                                                                      8%,
      lineNumber=null, comments=CommentData{{ABOVE=[_��0A, �, ], INLINE=[�X��`+<], BELOW=[��ܗ[��Yɽ"��, �, �}:���m�]}}},
      DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}},
      DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}},
      DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}}
    ], lineNumber=null, comments=CommentData{{}}},
    DataEntry{value=false, lineNumber=null, comments=CommentData{{ABOVE=[, ��, ��4], INLINE=[��eHò��c���%�], BELOW=[{�x�, �, ]}}}
  ], lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=true, lineNumber=null, comments=CommentData{{ABOVE=[, �BE�U�H'�, ��&J��>], INLINE=[N�)D�q^Z̡-�V��], BELOW=[�kFùIPm�B�, , )T�+]}}
}], lineNumber=null, comments=CommentData{{}}},
-11360=DataEntry{value=-825108576, lineNumber=null, comments=CommentData{{ABOVE=[<®7, 3dl��E�%%��AQ`�́��v\�1Z, D�], INLINE=[��� �i�Oj��=E], BELOW=[���[^, �qI�G. n@, a�]}}},
true=DataEntry{value=Mut{{Ĝ=DataEntry{value
 */ // Yes, the assertion message cut off here... maybe an extremely weird string value pre-consumed what came after
        DataTree.Mut dataTree = new DataTree.Mut();
        List<DataEntry> dataList = new ArrayList<>();
        List<DataEntry> subList = new ArrayList<>();
        List<DataEntry> subSubList1 = new ArrayList<>();
        List<DataEntry> subSubList2 = new ArrayList<>();
        dataTree.set("26", new DataEntry(dataList));
        dataList.add(new DataEntry(subList));
        subList.add(new DataEntry(subSubList1));
        subList.add(new DataEntry(subSubList2));

        subSubList1.add(new DataEntry(new DataTree.Mut()));
        subSubList1.add(new DataEntry(new ArrayList<>()));
        List<String> weirdLines = new ArrayList<>();
        weirdLines.add("��ܗ[��Yɽ\"��");
        weirdLines.add("�");
        weirdLines.add("�}:���m�");
        subSubList2.add(new DataEntry("��9,:ש5`y\u0017��\b��y|2���\f8%").withComments(CommentData.empty()
                .setAt(CommentLocation.ABOVE, "_��0A", "�", "")
                .setAt(CommentLocation.INLINE, "�X��`+<")
                .setAt(CommentLocation.BELOW, weirdLines)
        ));
        subSubList2.add(new DataEntry(new DataTree.Mut()));
        subSubList2.add(new DataEntry(new DataTree.Mut()));
        subSubList2.add(new DataEntry(new DataTree.Mut()));

        subList.add(new DataEntry(false).withComments(CommentData.empty()
                .setAt(CommentLocation.ABOVE, "", "��", "��4")
                .setAt(CommentLocation.INLINE, "��eHò��c���%�")
                .setAt(CommentLocation.BELOW, "{�x�", "�, ")
        ));

        // STILL NOT SURE HOW TO REPRODUCE THIS BUG

        CommentData headerFooter = CommentData.empty()
                .setAt(CommentLocation.ABOVE, "�", "", "�mi�ý")
                .setAt(CommentLocation.INLINE, "�r��I�u���o:T�6�")
                .setAt(CommentLocation.BELOW, "]d,�", "", "�9�");

        tomlBackend.write(new SimpleDocument(headerFooter, dataTree.intoImmut()));

        Backend.Document reloaded = tomlBackend.read(errorSource).getOrThrow();
        assertEquals(dataTree, reloaded.data());
    }
}
