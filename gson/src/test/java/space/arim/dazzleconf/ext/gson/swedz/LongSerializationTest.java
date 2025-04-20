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

package space.arim.dazzleconf.ext.gson.swedz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Test;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.gson.GsonConfigurationFactory;
import space.arim.dazzleconf.sorter.AnnotationBasedSorter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongSerializationTest {

    @Test
    public void loadJsonNoCorruptLongValue() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setLenient()
                .disableHtmlEscaping()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create();
        String input = "{\n  \"channel\": 1184252858728726529\n}";
        Map<String, Object> loadedMap;
        {
            TypeAdapter<Map<String, Object>> adapter = gson.getAdapter(new TypeToken<Map<String, Object>>() {});
            JsonReader jsonReader = gson.newJsonReader(new StringReader(input));
            loadedMap = adapter.read(jsonReader);
        }
        assertEquals( 1184252858728726529L, loadedMap.get("channel"));
    }

    @Test
    public void testRoundTrip() throws IOException, InvalidConfigException {
        ConfigurationFactory<HexCrawlerConfig> factory = GsonConfigurationFactory.create(
                HexCrawlerConfig.class,
                new ConfigurationOptions.Builder().sorter(new AnnotationBasedSorter()).build()
        );
        String input = "{\n  \"channel\": 1184252858728726529\n}";
        HexCrawlerConfig config = factory.load(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        factory.write(config, outputStream);
        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertEquals(input, output);
    }
}
