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

import java.util.List;

final class FlowFrameOutside implements CommentSink {

    private final CommentSink setOn;
    private final int indentLevel;
    private final int obscureAfterIndentLevel;

    FlowFrameOutside(CommentSink setOn, int indentLevel, int obscureAfterIndentLevel) {
        this.setOn = setOn;
        this.indentLevel = indentLevel;
        this.obscureAfterIndentLevel = obscureAfterIndentLevel;
    }

    @Override
    public int indentLevel() {
        return indentLevel;
    }

    @Override
    public int obscureAfterIndentLevel() {
        return obscureAfterIndentLevel;
    }

    @Override
    public void setBlockComments(boolean comingFromBelow, List<String> comments) {
        setOn.setBlockComments(comingFromBelow, comments);
    }

    @Override
    public void finish() {

    }
}
