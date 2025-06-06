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

package space.arim.dazzleconf.backend.yaml;

import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.TestingErrorSource;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.StringRoot;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GeneratedDataTest {

    private final StringRoot stringRoot = new StringRoot("");
    private final YamlBackend yamlBackend = new YamlBackend(stringRoot);
    private final ErrorContext.Source errorSource = new TestingErrorSource().makeErrorSource();

    @Test
    public void floats() {
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set(false, new DataEntry(new DataTree.Mut()));
        dataTree.set(96.0, new DataEntry(new DataTree.Mut()));
        yamlBackend.write(Backend.Document.simple(dataTree));
        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();
        assertEquals(dataTree, reloaded);
    }

    @Test
    public void inlineCommentOnMultiLineString() {
        /*
[ERROR]   YamlBackendTest>BackendTest.lambda$readWriteRandomData$3:73 Wrote data tree
Mut{{PRX=DataEntry{value=r󱭆�*.�A
, lineNumber=null, comments=CommentData{{ABOVE=[J��, V+�W�)�, ܡʜ�E~՛Hl��], INLINE=[[�], BELOW=[��ٯ�, ���, X[\\��f�]}}}, pI5=DataEntry{value=\\h�3��3�, lineNumber=null, comments=CommentData{{ABOVE=[ԉ$Y, Rd�鐧y#H, N�p����], INLINE=[D��؈�[]], BELOW=[/ఆ, (��v��, �;��(]}}}, MLcZFA=DataEntry{value=0.43604767, lineNumber=null, comments=CommentData{{}}}, 9J4J=DataEntry{value=1377459125, lineNumber=null, comments=CommentData{{ABOVE=[H�����C;J�yK-�ȵ!�>U�, , 8�3��B̹`M�I��v�����ҽ`��n*], INLINE=[�], BELOW=[����Fd�ͺZz�, �6���)�٥�cԣ, +�D�ח�RR��2*�B]}}}, c2sm6op=DataEntry{value=[], lineNumber=null, comments=CommentData{{ABOVE=[, , �], INLINE=[E�{d���jig(���], BELOW=[x�G, sX�QZz���<�/, ���C{j�$�Xԧ5���`��X�]}}}, 98ked=DataEntry{value=2307, lineNumber=null, comments=CommentData{{ABOVE=[��v�%��JjF B�*n, , ��], INLINE=[s2]�], BELOW=[�?z�, aB\\u, �4�0��Uff���]}}}, s=DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}}, KKdw=DataEntry{value=-49, lineNumber=null, comments=CommentData{{}}}, o2Z=DataEntry{value=0.10960555, lineNumber=null, comments=CommentData{{}}}}}
 and reloaded it to
Mut{{PRX=DataEntry{value=r󱭆�*.�A
, lineNumber=null, comments=CommentData{{ABOVE=[J��, V+�W�)�, ܡʜ�E~՛Hl��], BELOW=[[�, ��ٯ�, ���, X[\\��f�]}}}, pI5=DataEntry{value=\\h�3��3�, lineNumber=null, comments=CommentData{{ABOVE=[ԉ$Y, Rd�鐧y#H, N�p����], INLINE=[D��؈�[]], BELOW=[/ఆ, (��v��, �;��(]}}}, MLcZFA=DataEntry{value=0.43604767, lineNumber=null, comments=CommentData{{}}}, 9J4J=DataEntry{value=1377459125, lineNumber=null, comments=CommentData{{ABOVE=[H�����C;J�yK-�ȵ!�>U�, , 8�3��B̹`M�I��v�����ҽ`��n*], INLINE=[�], BELOW=[����Fd�ͺZz�, �6���)�٥�cԣ, +�D�ח�RR��2*�B]}}}, c2sm6op=DataEntry{value=[], lineNumber=null, comments=CommentData{{ABOVE=[, , �], INLINE=[E�{d���jig(���], BELOW=[x�G, sX�QZz���<�/, ���C{j�$�Xԧ5���`��X�]}}}, 98ked=DataEntry{value=2307, lineNumber=null, comments=CommentData{{ABOVE=[��v�%��JjF B�*n, , ��], INLINE=[s2]�], BELOW=[�?z�, aB\\u, �4�0��Uff���]}}}, s=DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}}, KKdw=DataEntry{value=-49, lineNumber=null, comments=CommentData{{}}}, o2Z=DataEntry{value=0.10960555, lineNumber=null, comments=CommentData{{}}}}}.
 But failed because of Failed at key PRX
         */
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("PRX", new DataEntry("r45as\nhi\n")
                .withComments(CommentLocation.INLINE, List.of("�"))
                .withComments(CommentLocation.BELOW, List.of("below multiliner"))
        );
        dataTree.set("pI5", new DataEntry("\\h�3��3�").withComments(CommentLocation.ABOVE, List.of("ԉ$Y", "Rd�鐧y#H", "N�p����")));
        yamlBackend.write(Backend.Document.simple(dataTree));
        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();
        assertEquals(dataTree, reloaded);
        DataEntry prxEntry = reloaded.get("PRX");
        assertNotNull(prxEntry);
        assertEquals(List.of("�"), prxEntry.getComments(CommentLocation.INLINE));
        assertEquals(List.of("below multiliner"), prxEntry.getComments(CommentLocation.BELOW));
    }

    @Test
    public void inlineCommentOnStringWithCarriageReturn() {
        /*
[ERROR]   YamlBackendTest>BackendTest.lambda$readWriteRandomData$3:73 Wrote data tree
Mut{{T4q5GM=DataEntry{value=-34190314, lineNumber=null, comments=CommentData{{ABOVE=[gXd������C�w��}�c���, ��;��, J�;e�], INLINE=[�R��l], BELOW=[\���'�A�IF+�h, ����e��`�, �]}}},
o2gZz=DataEntry{value=0.3669793, lineNumber=null, comments=CommentData{{}}}, LymQpJDB=DataEntry{value=�~�
+���P�mɒq���5��, lineNumber=null, comments=CommentData{{ABOVE=[, t�, ], INLINE=[�蝾�{te], BELOW=[?9, ���J���&���O�y, d�R"աW�)'@E0]}}},
G34q=DataEntry{value=[], lineNumber=null, comments=CommentData{{ABOVE=[, @���P�S<, �#�], INLINE=[��], BELOW=[%?�9��, ��, ]}}},
exCJA=DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}},
mN17=DataEntry{value=1380079938714299445, lineNumber=null, comments=CommentData{{}}},
0FnG8=DataEntry{value=0.94797635, lineNumber=null, comments=CommentData{{ABOVE=[;�Y����]|d�@, I;F�����, m], INLINE=[Ak�V�], BELOW=[E��׽�, �, ��]}}},
SNG=DataEntry{value=[], lineNumber=null, comments=CommentData{{}}}}}
 and reloaded it to
Mut{{T4q5GM=DataEntry{value=-34190314, lineNumber=null, comments=CommentData{{ABOVE=[gXd������C�w��}�c���, ��;��, J�;e�], INLINE=[�R��l], BELOW=[\���'�A�IF+�h, ����e��`�, �]}}},
o2gZz=DataEntry{value=0.3669793, lineNumber=null, comments=CommentData{{}}}, LymQpJDB=DataEntry{value=�~�
+���P�mɒq���5��, lineNumber=null, comments=CommentData{{ABOVE=[, t�, ], BELOW=[?9, ���J���&���O�y, d�R"աW�)'@E0]}}},
G34q=DataEntry{value=[], lineNumber=null, comments=CommentData{{ABOVE=[, @���P�S<, �#�], INLINE=[��], BELOW=[%?�9��, ��, ]}}},
exCJA=DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}},
mN17=DataEntry{value=1380079938714299445, lineNumber=null, comments=CommentData{{}}},
0FnG8=DataEntry{value=0.94797635, lineNumber=null, comments=CommentData{{ABOVE=[;�Y����]|d�@, I;F�����, m], INLINE=[Ak�V�], BELOW=[E��׽�, �, ��]}}},
SNG=DataEntry{value=[], lineNumber=null, comments=CommentData{{}}}}}.
 But failed because of Failed at key LymQpJDB
         */
        DataTree.Mut dataTree = new DataTree.Mut();
        dataTree.set("T4q5GM", new DataEntry(-34190314).withComments(CommentData.empty()
                .setAt(CommentLocation.ABOVE, "gXd������C�w��}�c���", "��;��", "J�;e�")
                .setAt(CommentLocation.INLINE, "�R��l")
                .setAt(CommentLocation.BELOW, "\\���'�A�IF+�h", "����e��`�", "�")
        ));
        dataTree.set("o2gZz", new DataEntry(0.3669793));
        DataEntry originalLymEntry = new DataEntry("�~�\r\n+���P�mɒq���5��").withComments(CommentData.empty()
                .setAt(CommentLocation.ABOVE, "", "t�", "")
                .setAt(CommentLocation.INLINE, "�蝾�{te")
                .setAt(CommentLocation.BELOW, "?9", "���J���&���O�y", "d�R\"աW�)'@E0")
        );
        dataTree.set("LymQpJDB", originalLymEntry);
        dataTree.set("G34q", new DataEntry(List.of()).withComments(CommentData.empty()
                .setAt(CommentLocation.ABOVE, "", "@���P�S<", "�#�")
                .setAt(CommentLocation.INLINE, "��")
                .setAt(CommentLocation.BELOW, "%?�9��", "��", "")
        ));
        yamlBackend.write(Backend.Document.simple(dataTree));
        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();
        DataEntry lymEntry = reloaded.get("LymQpJDB");
        assertNotNull(lymEntry);
        assertEquals(originalLymEntry.getComments(), lymEntry.getComments(), stringRoot.readString());
        assertEquals(List.of("�蝾�{te"), lymEntry.getComments(CommentLocation.INLINE));
    }

    @Test
    public void commentsOnListEntry() {
/*
Mut{{
H4j=DataEntry{value=笆, lineNumber=null, comments=CommentData{{ABOVE=[\ը-�L�, j�?�Cg�#Y+`, #�k���D�[�V�a� /], INLINE=[[?���8�&�b���s\�pp��2��], BELOW=[)�ĳo#/4� �fX���, -L, �Z]}}},
lz=DataEntry{value=[
  DataEntry{value=1522, lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=-975416927, lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=[], lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}}
], lineNumber=null, comments=CommentData{{}}},
4P3hgrR=DataEntry{value=[
  DataEntry{value=4585, lineNumber=null, comments=CommentData{{ABOVE=[�Ŕ����PW�, , �9], INLINE=[�], BELOW=[S��!, خ�+��, c��u�D]}}},
  DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=0.82580996, lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=[], lineNumber=null, comments=CommentData{{}}}
], lineNumber=null, comments=CommentData{{}}}}}
 and reloaded it to
Mut{{
H4j=DataEntry{value=笆, lineNumber=null, comments=CommentData{{ABOVE=[\ը-�L�, j�?�Cg�#Y+`, #�k���D�[�V�a� /], INLINE=[[?���8�&�b���s\�pp��2��], BELOW=[)�ĳo#/4� �fX���, -L, �Z]}}},
lz=DataEntry{value=[
  DataEntry{value=1522, lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=-975416927, lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=[], lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}}], lineNumber=null, comments=CommentData{{}}},
4P3hgrR=DataEntry{value=[
  DataEntry{value=4585, lineNumber=null, comments=CommentData{{ABOVE=[�Ŕ����PW�, , �9], INLINE=[�], BELOW=[S��!, خ�+��, c��u�D]}}},
  DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=0.82580996, lineNumber=null, comments=CommentData{{ABOVE=[S��!, خ�+��, c��u�D]}}},
  DataEntry{value=[], lineNumber=null, comments=CommentData{{}}}
], lineNumber=null, comments=CommentData{{}}}}}.
 But failed because of Failed at key 4P3hgrR
 */
        DataTree.Mut dataTree = new DataTree.Mut();
        DataEntry h4jEntry = new DataEntry("笆").withComments(CommentData.empty()
                .setAt(CommentLocation.ABOVE, "\\ը-�L�", "j�?�Cg�#Y+`", "#�k���D�[�V�a� /")
                .setAt(CommentLocation.INLINE, "[?���8�&�b���s\\�pp��2��")
                .setAt(CommentLocation.BELOW, ")�ĳo#/4� �fX���", "-L", "�Z")
        );
        dataTree.set("H4j", h4jEntry);
        DataEntry dataList1 = new DataEntry(4585).withComments(CommentData.empty()
                .setAt(CommentLocation.ABOVE, "�Ŕ����PW�", "", "�9")
                .setAt(CommentLocation.INLINE, "�")
                .setAt(CommentLocation.BELOW, "S��!, خ�+��, c��u�D")
        );
        DataEntry dataList2 = new DataEntry(new DataTree.Immut());
        DataEntry dataList3 = new DataEntry(82580996); // No comments
        DataEntry dataList4 = new DataEntry(List.of());
        dataTree.set("4P3hgrR", new DataEntry(List.of(dataList1, dataList2, dataList3, dataList4)));

        yamlBackend.write(Backend.Document.simple(dataTree));
        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();

        DataEntry reloadedH4j = reloaded.get("H4j");
        assertNotNull(reloadedH4j);
        assertEquals(h4jEntry.getComments(), reloadedH4j.getComments());

        DataEntry reloaded4P3 = reloaded.get("4P3hgrR");
        assertNotNull(reloaded4P3);
        assertEquals(dataTree.get("4P3hgrR"), reloaded4P3); // Checks the values
        @SuppressWarnings("unchecked")
        List<DataEntry> reloadedList = (List<DataEntry>) reloaded4P3.getValue();
        DataEntry reloadedList1 = reloadedList.get(0);
        DataEntry reloadedList2 = reloadedList.get(1);
        DataEntry reloadedList3 = reloadedList.get(2);
        DataEntry reloadedList4 = reloadedList.get(3);
        assertEquals(dataList1.getComments(), reloadedList1.getComments());
        assertEquals(dataList2.getComments(), reloadedList2.getComments());
        assertEquals(dataList3.getComments(), reloadedList3.getComments());
        assertEquals(dataList4.getComments(), reloadedList4.getComments());
    }

    @Test
    public void emptyFlowSubtree() {
/*
java.lang.AssertionError: Wrote data tree
Mut{{7WFE3=DataEntry{value=Mut{{px=DataEntry{value=0.6403029978265398, lineNumber=null, comments=CommentData{{}}}, pX=DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}}}}, lineNumber=null, comments=CommentData{{}}}, wvW6t=DataEntry{value=傓, lineNumber=null, comments=CommentData{{ABOVE=[, �!�d3t,�, {#��], INLINE=[OM�&�], BELOW=[, g, ��li��n��K^�γ��]}}}, U=DataEntry{value=true, lineNumber=null, comments=CommentData{{}}}, dW=DataEntry{value=0.06337135740149635, lineNumber=null, comments=CommentData{{ABOVE=[, �"eT�ىE�S^L��, Z�y], INLINE=["B�], BELOW=[��7, dM֊�ԃ�}�, t�*]}}}}}
 and reloaded it to
Mut{{7WFE3=DataEntry{value=Mut{{px=DataEntry{value=0.6403029978265398, lineNumber=null, comments=CommentData{{}}}, pX=DataEntry{value=Mut{{}}, lineNumber=null, comments=CommentData{{}}}}}, lineNumber=null, comments=CommentData{{BELOW=[, �!�d3t,�, {#��]}}}, wvW6t=DataEntry{value=傓, lineNumber=null, comments=CommentData{{INLINE=[OM�&�], BELOW=[, g, ��li��n��K^�γ��]}}}, U=DataEntry{value=true, lineNumber=null, comments=CommentData{{}}}, dW=DataEntry{value=0.06337135740149635, lineNumber=null, comments=CommentData{{ABOVE=[, �"eT�ىE�S^L��, Z�y], INLINE=["B�], BELOW=[��7, dM֊�ԃ�}�, t�*]}}}}}.
 But failed because of Failed at key wvW6t
 */
        CommentData commentData = CommentData.empty().setAt(CommentLocation.ABOVE, "", "�!�d3t,�", "{#��");

        DataTree.Mut dataTree = new DataTree.Mut();
        DataTree.Mut subTree = new DataTree.Mut();
        dataTree.set("7WFE3", new DataEntry(subTree));
        subTree.set("px", new DataEntry(0.6403029978265398));
        subTree.set("pX", new DataEntry(new DataTree.Mut()));
        dataTree.set("wvW6t", new DataEntry("傓").withComments(commentData));
        yamlBackend.write(Backend.Document.simple(dataTree));
        /*
7WFE3:
  px: 0.6403029978265398
  pX: {}
#
# �!�d3t,�
# {#��
wvW6t: 傓
         */
        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();
        DataEntry reloadedEntry = reloaded.get("wvW6t");
        assertNotNull(reloadedEntry);
        assertEquals(commentData, reloadedEntry.getComments());
    }

    @Test
    public void belowCommentsOnMMultilineString() {
        /*
Mut{{8iHh=DataEntry{value=ᷯ, lineNumber=null, comments=CommentData{{}}}, oCifae0=DataEntry{value=Mut{{APWNF=DataEntry{value=o �]i
�ߘ㧡�\Ҕ�>���, lineNumber=null, comments=CommentData{{ABOVE=[, ��, �], INLINE=[�3�], BELOW=[\�w�R����5�l���ƪARP�=��Ⱥ]��, k��,X�p|�, i\h�?�W�]}}}}}, lineNumber=null, comments=CommentData{{}}}}}
 and reloaded it to
Mut{{8iHh=DataEntry{value=ᷯ, lineNumber=null, comments=CommentData{{}}}, oCifae0=DataEntry{value=Mut{{APWNF=DataEntry{value=o �]i
�ߘ㧡�\Ҕ�>���, lineNumber=null, comments=CommentData{{ABOVE=[, ��, �], INLINE=[\�w�R����5�l���ƪARP�=��Ⱥ]��, k��,X�p|�, i\h�?�W�]}}}}}, lineNumber=null, comments=CommentData{{}}}}}.
 But failed because of Failed at key APWNF
         */
        CommentData commentData = CommentData.empty()
                .setAt(CommentLocation.ABOVE, "", "��", "�")
                .setAt(CommentLocation.INLINE, "�3�")
                .setAt(CommentLocation.BELOW, "\\�w�R����5�l���ƪARP�=��Ⱥ]��", "k��,X�p|�", "i\\h�?�W�");
        DataTree.Mut dataTree = new DataTree.Mut();
        DataTree.Mut subTree = new DataTree.Mut();
        dataTree.set("8iHh", new DataEntry(""));
        dataTree.set("oCifae0", new DataEntry(subTree));
        subTree.set("APWNF", new DataEntry("o �]i\n" + "�ߘ㧡�\\Ҕ�>���").withComments(commentData));
        yamlBackend.write(Backend.Document.simple(dataTree));
        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();
        DataEntry reloadedEntry = reloaded.get("oCifae0");
        assertNotNull(reloadedEntry);
        DataEntry reloadedSubEntry = ((DataTree) reloadedEntry.getValue()).get("APWNF");
        assertNotNull(reloadedSubEntry);
        assertEquals(commentData, reloadedSubEntry.getComments());
    }

    @Test
    public void commentedEmptyStringInsideList() {
/*
org.opentest4j.AssertionFailedError: Failed to re-read data
Immut{{
oTenACiz=DataEntry{value=Mut{{HV8u=DataEntry{value=[], lineNumber=null, comments=CommentData{{}}}}
}, lineNumber=null, comments=CommentData{{}}},
lwM=DataEntry{value=[
  DataEntry{value=[], lineNumber=null, comments=CommentData{{}}},
  DataEntry{value=, lineNumber=null, comments=CommentData{{ABOVE=[�і����2?u�$��Ȋ�V�g�3t, �, �˽], INLINE=[̮{], BELOW=[�ɗvvĕh, m, �oPE�ܴ��&y�F�]}}}
], lineNumber=null, comments=CommentData{{}}}}}. Document looks like:
---

oTenACiz:
  HV8u: []
lwM:
- []
- # �і����2?u�$��Ȋ�V�g�3t
  # �
  # �˽
'' # ̮{
# �ɗvvĕh
# m
# �oPE�ܴ��&y�F�
 */
        CommentData emptyStringCommentData = CommentData.empty()
                .setAt(CommentLocation.ABOVE, "�і����2?u�$��Ȋ�V�g�3t, �, �˽]")
                .setAt(CommentLocation.INLINE, "̮")
                .setAt(CommentLocation.BELOW, "�ɗvvĕh, m, �oPE�ܴ��&y�F�");
        DataTree.Mut dataTree = new DataTree.Mut();
        List<DataEntry> dataList = new ArrayList<>();
        dataTree.set("lwM", new DataEntry(dataList));
        dataList.add(new DataEntry(List.of()));
        dataList.add(new DataEntry("").withComments(emptyStringCommentData));
        yamlBackend.write(Backend.Document.simple(dataTree));

        /*stringRoot.writeString("""
                lwM:
                - []
                - # �і����2?u�$��Ȋ�V�g�3t, �, �˽]
                  '' # ̮
                  # �ɗvvĕh, m, �oPE�ܴ��&y�F�
                """);*/
        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();
        DataEntry reloadedList = reloaded.get("lwM");
        assertNotNull(reloadedList);
        DataEntry reloadedEntry = (DataEntry) ((List<?>) reloadedList.getValue()).get(1);
        assertEquals("", reloadedEntry.getValue());
        assertEquals(emptyStringCommentData, reloadedEntry.getComments());
    }

    @Test
    public void commentedMultilineStringInsideList() {
        /*
Mut{{WQNs688=DataEntry{value=[DataEntry{value=[], lineNumber=null, comments=CommentData{{}}}, DataEntry{value=��wy���3S��惷��{s�x��
q@�WI, lineNumber=null, comments=CommentData{{ABOVE=[.)L�, [�#c��o�|�, ��], INLINE=[�wk:n+���ˍ�G�����}�����t�], BELOW=[�WX�, S�3��, ���Z;�����^-]}}}], lineNumber=null, comments=CommentData{{}}}}}
 and reloaded it to
Mut{{WQNs688=DataEntry{value=[DataEntry{value=[], lineNumber=null, comments=CommentData{{}}}, DataEntry{value=��wy���3S��惷��{s�x��
q@�WI, lineNumber=null, comments=CommentData{{ABOVE=[.)L�, [�#c��o�|�, ��], BELOW=[�wk:n+���ˍ�G�����}�����t�, �WX�, S�3��, ���Z;�����^-]}}}], lineNumber=null, comments=CommentData{{}}}}}.
 But failed because of Failed at key WQNs688
         */
        CommentData multilineStringCommentData = CommentData.empty()
                .setAt(CommentLocation.ABOVE, ".)L�", "[�#c��o�|�", "��")
                .setAt(CommentLocation.INLINE, "�wk:n+���ˍ�G�����}�����t�")
                .setAt(CommentLocation.BELOW, "�WX�", "S�3��", "���Z;�����^-");
        DataTree.Mut dataTree = new DataTree.Mut();
        List<DataEntry> dataList = new ArrayList<>();
        dataTree.set("WQNs688", new DataEntry(dataList));
        dataList.add(new DataEntry(List.of()));
        dataList.add(new DataEntry("��wy���3S��惷��{s�x��\nq@�WI").withComments(multilineStringCommentData));
        dataList.add(new DataEntry("final").withComments(CommentLocation.ABOVE, List.of("comment on next item")));
        yamlBackend.write(Backend.Document.simple(dataTree));
        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();
        DataEntry reloadedList = reloaded.get("WQNs688");
        assertNotNull(reloadedList);
        DataEntry reloadedEntry = (DataEntry) ((List<?>) reloadedList.getValue()).get(1);
        assertEquals(multilineStringCommentData, reloadedEntry.getComments());
    }

    @Test
    public void threeLayerFlowKeepsComments() {
        /*
Immut{{hgXURRq=DataEntry{value=[
  DataEntry{value=Mut{{
    PwpPlDpu=DataEntry{value=12532, lineNumber=null, comments=CommentData{{ABOVE=[O�ǝ��m��, �, ۉN����], INLINE=[�9Eq�-���Y�>�e6��qU���], BELOW=[, �, �^{=�G�U]}}}
  }}, lineNumber=null, comments=CommentData{{}}}
], lineNumber=null, comments=CommentData{{}}}}}
 and reloaded it to
Mut{{hgXURRq=DataEntry{value=[
  DataEntry{value=Mut{{
    PwpPlDpu=DataEntry{value=12532, lineNumber=null, comments=CommentData{{INLINE=[�9Eq�-���Y�>�e6��qU���], BELOW=[, �, �^{=�G�U]}}}
  }}, lineNumber=null, comments=CommentData{{ABOVE=[O�ǝ��m��, �, ۉN����]}}}], lineNumber=null, comments=CommentData{{}}}}}.
 But failed because of Failed at key hgXURRq. Note that document comments were added: CommentData{{ABOVE=[F�pM�8�,  v"E����?m, �C��], BELOW=[�2�]�RF�g�����0�, ���,,W�0+, ��^۬�]}}
         */
        DataTree.Mut dataTree = new DataTree.Mut();
        List<DataEntry> dataList = new ArrayList<>();
        DataTree.Mut treeElement = new DataTree.Mut();
        dataTree.set("hgXURRq", new DataEntry(dataList));
        dataList.add(new DataEntry(treeElement));

        CommentData deepComments = CommentData.empty()
                .setAt(CommentLocation.ABOVE, "O�ǝ��m��", "�", "ۉN����")
                .setAt(CommentLocation.INLINE, "�9Eq�-���Y�>�e6��qU���")
                .setAt(CommentLocation.BELOW, "", "�", "�^{=�G�U");
        treeElement.set("PwpPlDpu", new DataEntry(12532).withComments(deepComments));
        yamlBackend.write(Backend.Document.simple(dataTree));

        DataTree reloaded = yamlBackend.read(errorSource).getOrThrow().data();
        DataEntry reloadedList = reloaded.get("hgXURRq");
        assertNotNull(reloadedList);
        DataEntry reloadedTreeElement = (DataEntry) ((List<?>) reloadedList.getValue()).getFirst();
        DataEntry reloadedNumeral = ((DataTree) reloadedTreeElement.getValue()).get("PwpPlDpu");
        assertNotNull(reloadedNumeral);
        assertEquals(deepComments, reloadedNumeral.getComments());
    }
}
