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
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

import java.util.List;
import java.util.Optional;

// https://bitbucket.org/snakeyaml/snakeyaml-engine/issues/64/emitting-an-empty-string-as-a-list-element
public class SnakeYamlEngineIssue64Test {

    @Test
    public void commentsOnEmptyStringInList() {
        DumpSettings dumpSettings = DumpSettings.builder().setDumpComments(true).build();
        StandardRepresenter representer = new StandardRepresenter(dumpSettings);
        Node problemNode = representer.represent("");
        problemNode.setBlockComments(List.of(new CommentLine(
                Optional.empty(), Optional.empty(), " Block comment", CommentType.BLOCK
        )));
        problemNode.setInLineComments(List.of(new CommentLine(
                Optional.empty(), Optional.empty(), " Inline comment", CommentType.IN_LINE
        )));
        problemNode.setEndComments(List.of(new CommentLine(
                Optional.empty(), Optional.empty(), " End comment", CommentType.BLOCK
        )));
        NodeTuple containingTuple = new NodeTuple(representer.represent("my-list"), new SequenceNode(Tag.SEQ, List.of(
                problemNode
        ), FlowStyle.AUTO));
        Node containingMap = new MappingNode(Tag.MAP, List.of(containingTuple), FlowStyle.AUTO);

        Dump dump = new Dump(dumpSettings, representer);
        StringBuilder output = new StringBuilder();
        dump.dumpNode(containingMap, new StreamDataWriter() {
            @Override
            public void write(String str) {
                output.append(str);
            }

            @Override
            public void write(String str, int off, int len) {
                output.append(str, off, off + len);
            }
        });
        // Not valid YAML!
        //System.out.println(output);

        // Indeed, can't read it back
        Compose compose = new Compose(LoadSettings.builder().build());
        compose.composeString(output.toString()); // Throws exception
        /*
while scanning a simple key
 in reader, line 3, column 1:
    ''
    ^
could not find expected ':'
 in reader, line 4, column 1:

    ^

	at org.snakeyaml.engine.v2.scanner.ScannerImpl.stalePossibleSimpleKeys(ScannerImpl.java:475)
	at org.snakeyaml.engine.v2.scanner.ScannerImpl.fetchMoreTokens(ScannerImpl.java:309)
	at org.snakeyaml.engine.v2.scanner.ScannerImpl.checkToken(ScannerImpl.java:193)
	at org.snakeyaml.engine.v2.parser.ParserImpl$ParseIndentlessSequenceEntryValue.produce(ParserImpl.java:685)
	at org.snakeyaml.engine.v2.parser.ParserImpl$ParseIndentlessSequenceEntryKey.produce(ParserImpl.java:667)
	at org.snakeyaml.engine.v2.parser.ParserImpl.lambda$produce$1(ParserImpl.java:232)
	at java.base/java.util.Optional.ifPresent(Optional.java:178)
	at org.snakeyaml.engine.v2.parser.ParserImpl.produce(ParserImpl.java:232)
         */
    }
}
