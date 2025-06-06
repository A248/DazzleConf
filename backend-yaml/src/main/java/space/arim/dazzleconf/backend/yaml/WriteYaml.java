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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.ArrayList;
import java.util.List;

final class WriteYaml {

    private final Yaml yaml;

    WriteYaml() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setProcessComments(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(dumperOptions);
    }

    private Node dataTreeToNode(DataTree dataTree) {
        List<NodeTuple> keyValuePairs = new ArrayList<>(dataTree.size());
        dataTree.forEach((key, entry) -> {
            Node keyNode = yaml.represent(key);
            Node valueNode = entryToNode(entry);

            CommentData comments = entry.getComments();
            // Block comments can be converted easily, 1:1
            setComments1to1(keyNode, comments.getAt(CommentLocation.ABOVE), Node::setBlockComments);
            setInlineAndBelowComments(valueNode, comments);

            keyValuePairs.add(new NodeTuple(keyNode, valueNode));
        });
        return new MappingNode(Tag.MAP, keyValuePairs, DumperOptions.FlowStyle.AUTO);
    }

    private Node entryToNode(DataEntry entry) {
        Object value = entry.getValue();
        if (value instanceof DataTree) {
            return dataTreeToNode((DataTree) value);
        }
        return yaml.represent(value);
    }


}
