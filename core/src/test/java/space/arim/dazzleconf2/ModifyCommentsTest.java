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

package space.arim.dazzleconf2;import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.dazzleconf2.backend.CommentData;
import space.arim.dazzleconf2.backend.DataEntry;
import space.arim.dazzleconf2.engine.CommentLocation;
import space.arim.dazzleconf2.engine.Comments;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ModifyCommentsTest {

    private DataEntry dataEntry;
    private final ConfigurationDefinition.WriteOptions writeOptions;

    public ModifyCommentsTest(@Mock ConfigurationDefinition.WriteOptions writeOptions) {
        this.writeOptions = writeOptions;
    }

    @BeforeEach
    public void setupValue() {
        dataEntry = new DataEntry("initial value");
    }

    private static CommentData getComments(String methodName) {
        Method method;
        try {
            method = ModifyCommentsTest.class.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
        return CommentData.buildFrom(method.getAnnotationsByType(Comments.class));
    }

    @Test
    @Comments({"Hi there", "Another on top"})
    @Comments(value = "From below", location = CommentLocation.BELOW)
    public void copyAllComments() {
        when(writeOptions.writeEntryComments(any())).thenReturn(true);
        ModifyComments modifyComments = new ModifyComments(writeOptions);
        assertTrue(modifyComments.writeEntryComments(CommentLocation.ABOVE));
        assertTrue(modifyComments.writeEntryComments(CommentLocation.INLINE));
        assertTrue(modifyComments.writeEntryComments(CommentLocation.BELOW));
        assertTrue(modifyComments.isAnyLocationEnabled());
        CommentData copyFrom = getComments("copyAllComments");
        dataEntry = modifyComments.applyTo(dataEntry, copyFrom);
        assertEquals(copyFrom, dataEntry.getComments());
    }

    @Test
    public void dontCopyAnything() {
        ModifyComments modifyComments = new ModifyComments(writeOptions);
        assertFalse(modifyComments.writeEntryComments(CommentLocation.ABOVE));
        assertFalse(modifyComments.writeEntryComments(CommentLocation.INLINE));
        assertFalse(modifyComments.writeEntryComments(CommentLocation.BELOW));
        assertFalse(modifyComments.isAnyLocationEnabled());
        dataEntry = modifyComments.applyTo(dataEntry, getComments("copyAllComments"));
        assertEquals(CommentData.empty(), dataEntry.getComments());
    }

    @Test
    @Comments("Existing comments not erased")
    public void dontCopyDontOverwrite() {
        ModifyComments modifyComments = new ModifyComments(writeOptions);
        assertFalse(modifyComments.writeEntryComments(CommentLocation.ABOVE));
        assertFalse(modifyComments.writeEntryComments(CommentLocation.INLINE));
        assertFalse(modifyComments.writeEntryComments(CommentLocation.BELOW));
        assertFalse(modifyComments.isAnyLocationEnabled());
        CommentData existingData = getComments("dontCopyDontOverwrite");
        dataEntry = dataEntry.withComments(existingData);
        dataEntry = modifyComments.applyTo(dataEntry, getComments("copyAllComments"));
        assertEquals(existingData, dataEntry.getComments());
    }

    @Test
    @Comments("Hi there")
    @Comments(value = "Inline should not be copied", location = CommentLocation.INLINE)
    @Comments(value = "From below", location = CommentLocation.BELOW)
    public void setOnlyRequested() {
        when(writeOptions.writeEntryComments(CommentLocation.ABOVE)).thenReturn(true);
        when(writeOptions.writeEntryComments(CommentLocation.BELOW)).thenReturn(true);
        ModifyComments modifyComments = new ModifyComments(writeOptions);
        assertTrue(modifyComments.writeEntryComments(CommentLocation.ABOVE));
        assertFalse(modifyComments.writeEntryComments(CommentLocation.INLINE));
        assertTrue(modifyComments.writeEntryComments(CommentLocation.BELOW));
        assertTrue(modifyComments.isAnyLocationEnabled());
        CommentData copyFrom = getComments("setOnlyRequested");
        dataEntry = modifyComments.applyTo(dataEntry, copyFrom);
        assertEquals(
                CommentData.empty()
                        .setAt(CommentLocation.ABOVE, "Hi there")
                        .setAt(CommentLocation.BELOW, "From below"),
                dataEntry.getComments()
        );
    }

    @Test
    @Comments({"Please overwrite me", "another line"})
    @Comments(value = "But not me", location = CommentLocation.INLINE)
    public void overwriteExisting() {
        when(writeOptions.writeEntryComments(CommentLocation.ABOVE)).thenReturn(true);
        when(writeOptions.writeEntryComments(CommentLocation.BELOW)).thenReturn(true);
        ModifyComments modifyComments = new ModifyComments(writeOptions);
        assertTrue(modifyComments.writeEntryComments(CommentLocation.ABOVE));
        assertFalse(modifyComments.writeEntryComments(CommentLocation.INLINE));
        assertTrue(modifyComments.writeEntryComments(CommentLocation.BELOW));
        assertTrue(modifyComments.isAnyLocationEnabled());
        CommentData existingData = getComments("overwriteExisting");
        dataEntry = dataEntry.withComments(existingData);
        CommentData overwriteWith = CommentData.empty()
                .setAt(CommentLocation.ABOVE, "Hi there")
                .setAt(CommentLocation.BELOW, "From below");
        dataEntry = modifyComments.applyTo(dataEntry, overwriteWith);
        assertEquals(
                CommentData.empty()
                        .setAt(CommentLocation.ABOVE, "Hi there")
                        .setAt(CommentLocation.INLINE, "But not me")
                        .setAt(CommentLocation.BELOW, "From below"),
                dataEntry.getComments()
        );
    }

    @AfterEach
    public void valueUnchanged() {
        assertEquals("initial value", dataEntry.getValue());
    }
}
