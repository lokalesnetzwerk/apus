/*
 * Apus - A social wall for conferences with additional features.
 * Copyright (C) Marcus Fihlon and the individual contributors to Apus.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package swiss.fihlon.apus.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpeakerTest {

    @Test
    void compareTo() {
        assertEquals(-1, new Speaker("Speaker 1").compareTo(new Speaker("Speaker 2")));
        assertEquals(0, new Speaker("Speaker 1").compareTo(new Speaker("Speaker 1")));
        assertEquals(1, new Speaker("Speaker 2").compareTo(new Speaker("Speaker 1")));
    }

    @Test
    void fullName() {
        assertEquals("Speaker 1", new Speaker("Speaker 1").fullName());
    }

}
