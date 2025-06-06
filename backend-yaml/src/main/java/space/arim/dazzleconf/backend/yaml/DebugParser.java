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

import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.parser.Parser;

import java.util.ArrayList;
import java.util.List;

final class DebugParser implements Parser {

    private final Parser inner;
    private boolean record = true;
    private final List<Event> recordedEvents = new ArrayList<>();

    DebugParser(Parser inner) {
        this.inner = inner;
    }

    @Override
    public boolean checkEvent(Event.ID choice) {
        return inner.checkEvent(choice);
    }

    @Override
    public Event peekEvent() {
        Event innerEvent = inner.peekEvent();
        if (record) {
            recordedEvents.add(innerEvent);
            record = false;
        }
        return innerEvent;
    }

    @Override
    public boolean hasNext() {
        return inner.hasNext();
    }

    @Override
    public Event next() {
        Event innerEvent = inner.next();
        if (record) {
            recordedEvents.add(innerEvent);
        }
        record = true;
        return innerEvent;
    }
}
