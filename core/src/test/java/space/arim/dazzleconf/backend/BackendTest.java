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

package space.arim.dazzleconf.backend;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import space.arim.dazzleconf.TestingErrorSource;
import space.arim.dazzleconf2.ErrorContext;
import space.arim.dazzleconf2.LoadResult;
import space.arim.dazzleconf2.backend.Backend;
import space.arim.dazzleconf2.backend.DataTree;
import space.arim.dazzleconf2.backend.ReadableRoot;
import space.arim.dazzleconf2.backend.StringRoot;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BackendTest {

    protected abstract Backend createBackend(ReadableRoot dataRoot);

    @TestFactory
    public Stream<DynamicTest> readWriteRandomData() {
        ErrorContext.Source errorSource  = new TestingErrorSource().makeErrorSource();
        AtomicInteger sizeRecord = new AtomicInteger();
        AtomicLong ratioRecord = new AtomicLong(Double.doubleToRawLongBits(0L));
        return Stream.generate(DataTree.Mut::new)
                .limit(100L)
                .map(dataTree -> {

                    StringRoot stringRoot = new StringRoot("");
                    Backend backend = createBackend(stringRoot);
                    Backend.Document subject;
                    CountEntries countEntries = new CountEntries();
                    {
                        RandomGen randomGen = new RandomGen(backend.meta(), ThreadLocalRandom.current(), countEntries);
                        randomGen.fillDataTree(dataTree, 1, 0);
                        subject = new SimpleDocument(randomGen.generateDocumentComments(), dataTree.intoImmut());
                    }
                    return DynamicTest.dynamicTest("Using document " + subject, () -> {
                        assertDoesNotThrow(() -> backend.write(subject));
                        int dataSize = stringRoot.readString().length();

                        LoadResult<Backend.Document> reloadResult = assertDoesNotThrow(() -> backend.read(errorSource));
                        assertTrue(reloadResult.isSuccess(), () ->
                                "Failed to re-read data \n" + subject + ". Document looks like:\n---\n" + stringRoot.readString()
                        );
                        Backend.Document reloaded = reloadResult.getOrThrow();
                        if (reloaded == null) {
                            fail("Reloaded document is null for input " + subject + ". Document looks like:\n---\n" + stringRoot.readString());
                        }
                        Comparison comparison = new Comparison(subject, reloaded, backend.meta());
                        comparison.headerFooterEqual(subject, reloaded);
                        comparison.treesEqual(subject.data(), reloaded.data());

                        double dataRatio = ((double) dataSize) / countEntries.count;
                        int compareSizeRecord;
                        do {
                            compareSizeRecord = sizeRecord.get();
                        } while (dataSize > compareSizeRecord && !sizeRecord.compareAndSet(compareSizeRecord, dataSize));
                        long compareRatioRecord;
                        do {
                            compareRatioRecord = ratioRecord.get();
                        } while (dataRatio > Double.longBitsToDouble(compareRatioRecord)
                                && !ratioRecord.compareAndSet(compareRatioRecord, Double.doubleToRawLongBits(dataRatio)));
                        if (dataRatio > 1000) {
                            fail("DATA RATIO ABOVE 1000!");
                        }
                    });
                })
                .onClose(() -> {
                    System.out.println("Size record of " + sizeRecord.get() + " established.");
                    System.out.println("Ratio record of " + Double.longBitsToDouble(ratioRecord.get()) + " established.");
                });
    }

}
