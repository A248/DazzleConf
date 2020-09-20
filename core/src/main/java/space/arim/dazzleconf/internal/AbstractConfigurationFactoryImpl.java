/* 
 * DazzleConf-core
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DazzleConf-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DazzleConf-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DazzleConf-core. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package space.arim.dazzleconf.internal;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.IllDefinedConfigException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.internal.deprocessor.MapDeprocessor;
import space.arim.dazzleconf.internal.processor.DefaultsProcessor;
import space.arim.dazzleconf.internal.processor.ProcessorBase;
import space.arim.dazzleconf.internal.processor.MapProcessor;

public abstract class AbstractConfigurationFactoryImpl<C> extends BaseConfigurationFactoryImpl<C> {

	private final ConfigurationInfo<C> definition;
	
	protected AbstractConfigurationFactoryImpl(Class<C> configClass, ConfigurationOptions options) {
		super(configClass, options);
		definition = new DefinitionReader<>(configClass, options).read();
	}
	
	protected ConfigurationInfo<C> getDefinition() {
		return definition;
	}
	
	/*
	 * Reading
	 */

	@Override
	public C loadDefaults() {
		ProcessorBase processor = new DefaultsProcessor(getOptions(), definition.getEntries());
		try {
			return fromProcessor(processor);
		} catch (InvalidConfigException ex) {
			throw new IllDefinedConfigException(ex);
		}
	}
	
	protected abstract Map<String, Object> loadMapFromReader(Reader reader) throws IOException, ConfigFormatSyntaxException;

	@Override
	protected C loadFromReader(Reader reader) throws IOException, InvalidConfigException {
		return fromRawMap(loadMapFromReader(reader));
	}
	
	/**
	 * For use by testing (SerialisationFactory)
	 * 
	 * @param rawMap the raw map of nested values
	 * @return the config data
	 * @throws InvalidConfigException if the config is invalid
	 */
	/*private*/ C fromRawMap(Map<String, Object> rawMap) throws InvalidConfigException {
		return fromProcessor(new MapProcessor(getOptions(), definition.getEntries(), rawMap));
	}
	
	private C fromProcessor(ProcessorBase processor) throws InvalidConfigException {
		return ProcessorBase.createConfig(definition, processor);
	}
	
	/*
	 * Writing
	 */
	
	protected abstract void writeMapToWriter(Map<String, Object> config, Writer writer) throws IOException;

	@Override
	protected void writeToWriter(C configData, Writer writer) throws IOException {
		writeMapToWriter(toRawMap(configData), writer);
	}
	
	/**
	 * For use by testing (SerialisationFactory)
	 * 
	 * @param configData the config data
	 * @return the raw map of nested values
	 */
	/*private*/ Map<String, Object> toRawMap(C configData) {
		MapDeprocessor<C> simpleDeprocessor = createMapDeprocessor(configData);
		return simpleDeprocessor.deprocessAndGetResult();
	}
	
	protected MapDeprocessor<C> createMapDeprocessor(C configData) {
		return new MapDeprocessor<>(getOptions(), getDefinition().getEntries(), configData);
	}

}
