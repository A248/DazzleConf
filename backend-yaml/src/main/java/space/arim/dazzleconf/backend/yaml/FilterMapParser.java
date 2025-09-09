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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.parser.Parser;

abstract class FilterMapParser implements Parser {

    private final Parser inner;
    private Event mappedEvent;

    FilterMapParser(Parser inner) {
        this.inner = inner;
    }

    /// Returns null to skip the event, returns non-null to map
    abstract @Nullable Event filterMap(@NonNull Event event);

    @Override
    public boolean checkEvent(Event.ID choice) {
        if (mappedEvent != null) {
            return mappedEvent.getEventId().equals(choice);
        }
        return inner.checkEvent(choice);
    }

    @Override
    public Event peekEvent() {
        while (mappedEvent == null) {
            mappedEvent = filterMap(inner.next());
        }
        return mappedEvent;
    }

    @Override
    public boolean hasNext() {
        while (mappedEvent == null) {
            if (!inner.hasNext()) {
                return false;
            }
            mappedEvent = filterMap(inner.next());
        }
        return true;
    }

    @Override
    public Event next() {
        while (mappedEvent == null) {
            mappedEvent = filterMap(inner.next());
        }
        Event next = mappedEvent;
        mappedEvent = null;
        return next;
    }
}
