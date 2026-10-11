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

package space.arim.dazzleconf.backend.toml;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.comment.Comment;
import io.github.wasabithumb.jtoml.comment.CommentPosition;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.option.prop.IndentationPolicy;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TomlLibTest {

    private JToml jToml() {
        JTomlOptions jTomlOptions = JTomlOptions.builder()
                .set(JTomlOption.READ_COMMENTS, false)
                .set(JTomlOption.WRITE_EMPTY_TABLES, true)
                .set(JTomlOption.WRITE_COMMENTS, true)
                .set(JTomlOption.INDENTATION, IndentationPolicy.NONE)
                .build();
        return JToml.jToml(jTomlOptions);
    }

    private TomlDocument writeAndReload(JToml jToml, TomlTable table) {
        StringWriter contentWriter = new StringWriter();
        jToml.write(contentWriter, table);
        String content = contentWriter.toString();
        try {
            return jToml.read(new StringReader(content));
        } catch (TomlException e) {
            throw new IllegalStateException("Failed to re-read. Content is \n" + content + '\n', e);
        }
    }

    @Test
    public void readUnindentedComments() {
/*
# UTB
# viP5Yil
# 9aPzH41D
# j3a
D = [
# Umn3a
# t6B
# rG
26 # 7glnquyI
# GubtrGj
# y
# R3tiGZ
]
# 5mODwp
# l
# 8B2O

simplified to

D = [
 # ABOVE ELEM
 26 # INLINE ELEM
 # BELOW ELEM
]
 */
        JToml jToml = jToml();

        TomlPrimitive element = TomlPrimitive.of(26);
        element.comments().add(Comment.of(CommentPosition.PRE, "ABOVE ELEM"));
        element.comments().add(Comment.of(CommentPosition.INLINE, "INLINE ELEM"));
        element.comments().add(Comment.of(CommentPosition.POST, "BELOW ELEM"));

        TomlArray array = TomlArray.create();
        array.add(element);
        TomlTable table = TomlTable.create();
        table.put("D", array);
        //table.comments().add(Comment.of(CommentPosition.PRE, "ABOVE TABLE"));
        //table.comments().add(Comment.of(CommentPosition.INLINE, "INLINE TABLE"));
        //table.comments().add(Comment.of(CommentPosition.POST, "BELOW TABLE"));

        writeAndReload(jToml, table);
    }

    @Test
    public void readUnindentedArray() {
        /*
# y
# 6
# zV1
# sUIGq
-845656543 = [
[]
]
# Gua1mmD6
# 9hM0e
# aRrUcbA

simplified to

-845656543 = [
[]
]
         */
        JToml jToml = jToml();

        TomlArray array = TomlArray.create();
        array.add(TomlArray.create());
        TomlTable table = TomlTable.create();

        // Adding comments is not needed to trigger this bug, but it may be valuable to test their interaction too
        //array.comments().add(Comment.of(CommentPosition.PRE, "ABOVE ARRAY"));
        //array.comments().add(Comment.of(CommentPosition.INLINE, "INLINE ARRAY"));
        //array.comments().add(Comment.of(CommentPosition.POST, "BELOW ARRAY"));
        //table.comments().add(Comment.of(CommentPosition.PRE, "ABOVE TABLE"));
        //table.comments().add(Comment.of(CommentPosition.INLINE, "INLINE TABLE"));
        //table.comments().add(Comment.of(CommentPosition.POST, "BELOW TABLE"));

        table.put("-845656543", array);
        writeAndReload(jToml, table);
    }

    @Test
    public void consistentIndent() {
        JTomlOptions jTomlOptions = JTomlOptions.builder()
                .set(JTomlOption.INDENTATION, IndentationPolicy.builder().variableIndent(1).build())
                .build();
        JToml jToml = JToml.jToml(jTomlOptions);
        TomlTable superNested = TomlTable.create();
        superNested.put("y-char", 121);
        TomlTable nested = TomlTable.create();
        nested.put("hello", true);
        TomlTable table = TomlTable.create();
        table.put("main", "yes");
        TomlTable document = TomlTable.create();

        nested.put("super-nested", superNested);
        table.put("nested", nested);
        document.put("main", table);

        StringWriter contentWriter = new StringWriter();
        jToml.write(contentWriter, document);
        String content = contentWriter.toString();
        assertEquals("""
                [main]
                main = "yes"
                
                \t[main.nested]
                \thello = true
                
                \t\t[main.nested.super-nested]
                \t\ty-char = 121
                """.trim(), content.trim());
    }
}
