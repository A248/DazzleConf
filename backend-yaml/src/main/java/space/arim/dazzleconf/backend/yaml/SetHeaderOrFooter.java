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

import space.arim.dazzleconf2.engine.CommentLocation;

import java.util.List;

final class SetHeaderOrFooter implements CommentSink {

    private final ReadYamlProduct product;

    SetHeaderOrFooter(ReadYamlProduct product) {
        this.product = product;
    }

    @Override
    public int indentLevel() {
        return 0;
    }

    @Override
    public void setBlockComments(boolean comingFromBelow, List<String> comments) {
        CommentLocation location = comingFromBelow ? CommentLocation.ABOVE : CommentLocation.BELOW;
        product.headerFooter = product.headerFooter.setAt(location, comments);
    }

    @Override
    public void finish() {
    }
}
