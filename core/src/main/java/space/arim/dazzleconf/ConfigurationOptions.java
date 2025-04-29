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
package space.arim.dazzleconf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import space.arim.dazzleconf.annote.ConfSerialisers;
import space.arim.dazzleconf.annote.ConfValidator;
import space.arim.dazzleconf2.internals.ImmutableCollections;
import space.arim.dazzleconf.serialiser.ValueSerialiser;
import space.arim.dazzleconf.serialiser.ValueSerialiserMap;
import space.arim.dazzleconf.sorter.ConfigurationSorter;
import space.arim.dazzleconf.validator.ValueValidator;

/**
 * Options for loading, validating, and writing configuration values
 * 
 * @author A248
 *
 */
public final class ConfigurationOptions {

	private final ValueSerialiserMap serialisers;
	private final Map<String, ValueValidator> validators;
	private final ConfigurationSorter sorter;
	private final boolean strictParseEnums;
	private final boolean createSingleElementCollections;
	
	private static final ConfigurationOptions DEFAULTS = new ConfigurationOptions.Builder().build();

	ConfigurationOptions(ValueSerialiserMap serialisers, Map<String, ValueValidator> validators,
								ConfigurationSorter sorter, boolean strictParseEnums,
								boolean createSingleElementCollections) {
		this.serialisers = serialisers;
		this.validators = validators;
		this.sorter = sorter;
		this.strictParseEnums = strictParseEnums;
		this.createSingleElementCollections = createSingleElementCollections;
	}
	
	/**
	 * Returns the default configuration options
	 * 
	 * @return the default config options
	 */
	public static ConfigurationOptions defaults() {
		return DEFAULTS;
	}
	
	/**
	 * Gets the configuration value serialisers. Serialisers can also be specified by {@link ConfSerialisers},
	 * which are not included here
	 * 
	 * @return the value serialisers, never {@code null}
	 */
	public ValueSerialiserMap getSerialisers() {
		return serialisers;
	}
	
	/**
	 * Gets an immutable map of value validators, keyed by the configuration key to which they apply.
	 * Validators can also be specified {@link ConfValidator}, which are not included here
	 * 
	 * @return the map of value validators, never {@code null}
	 */
	public Map<String, ValueValidator> getValidators() {
		return validators;
	}

	/**
	 * Gets the {@link ConfigurationSorter}, or {@code null} for none
	 *
	 * @return the sorter or {@code null} if there is none
	 * @deprecated Use {@link #getConfigurationSorter()} instead
	 */
	@Deprecated
	public ConfigurationSorter getSorter() {
		return getConfigurationSorter().orElse(null);
	}

	/**
	 * Gets the configuration sorter
	 *
	 * @return the sorter, or an empty optional if there is none
	 */
	public Optional<ConfigurationSorter> getConfigurationSorter() {
		return Optional.ofNullable(sorter);
	}
	
	/**
	 * Whether enums are strictly parsed. See {@link Builder#setStrictParseEnums(boolean)}
	 * 
	 * @return true if strictly parsed, false otherwise
	 */
	public boolean strictParseEnums() {
		return strictParseEnums;
	}
	
	/**
	 * Whether single element collections are created for config values which were not originally collections
	 * 
	 * @return true to create single element collections, false otherwise
	 */
	public boolean createSingleElementCollections() {
		return createSingleElementCollections;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (strictParseEnums ? 1231 : 1237);
		result = prime * result + System.identityHashCode(sorter);
		result = prime * result + serialisers.hashCode();
		result = prime * result + validators.hashCode();
		result = prime * result + (createSingleElementCollections ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof ConfigurationOptions)) {
			return false;
		}
		ConfigurationOptions other = (ConfigurationOptions) object;
		return strictParseEnums == other.strictParseEnums
				&& ((sorter == null) ? other.sorter == null : sorter == other.sorter)
				&& serialisers.equals(other.serialisers)
				&& validators.equals(other.validators)
				&& createSingleElementCollections == other.createSingleElementCollections;
	}

	@Override
	public String toString() {
		return "ConfigurationOptions [serialisers=" + serialisers + ", validators=" + validators + ", sorter=" + sorter
				+ ", strictParseEnums=" + strictParseEnums + ", createSingleElementCollections="
				+ createSingleElementCollections + "]";
	}

	/**
	 * Builder of {@link ConfigurationOptions}. <b>Not thread safe</b>
	 * 
	 * @author A248
	 *
	 */
	public static class Builder {
		
		private final Map<Class<?>, ValueSerialiser<?>> serialisers = new HashMap<>();
		private final Map<String, ValueValidator> validators = new HashMap<>();
		private ConfigurationSorter sorter;
		private boolean strictParseEnums;
		private boolean createSingleElementCollections;

		/**
		 * Creates the builder. <br>
		 * <br>
		 * <b>Subclassing this builder is deprecated, and will be removed in a later release.</b>
		 */
		public Builder() {

		}

		/**
		 * Adds the specified value serialiser to this builder
		 * 
		 * @param serialiser the value serialiser
		 * @return this builder
		 * @throws NullPointerException if {@code serialiser} is null
		 * @throws IllegalArgumentException if the serialiser conflicts with an existing one
		 */
		public Builder addSerialiser(ValueSerialiser<?> serialiser) {
			Objects.requireNonNull(serialiser, "serialiser");
			ValueSerialiser<?> previous = serialisers.putIfAbsent(serialiser.getTargetClass(), serialiser);
			if (previous != null) {
				throw new IllegalArgumentException("ValueSerialiser " + serialiser + " conflicts with " + previous);
			}
			return this;
		}
		
		/**
		 * Adds the specified value serialisers to this builder
		 * 
		 * @param serialisers the value serialisers
		 * @return this builder
		 * @throws NullPointerException if {@code serialisers} or an element in it is null
		 * @throws IllegalArgumentException if any serialiser conflicts with an existing one
		 */
		public Builder addSerialisers(ValueSerialiser<?>...serialisers) {
			Objects.requireNonNull(serialisers, "serialisers");
			for (ValueSerialiser<?> serialiser : serialisers) {
				addSerialiser(serialiser);
			}
			return this;
		}
		
		/**
		 * Adds the specified value serialisers to this builder
		 * 
		 * @param serialisers the value serialisers
		 * @return this builder
		 * @throws NullPointerException if {@code serialisers} or an element in it is null
		 * @throws IllegalArgumentException if any serialiser conflicts with an existing one
		 */
		public Builder addSerialisers(Collection<ValueSerialiser<?>> serialisers) {
			Objects.requireNonNull(serialisers, "serialisers");
			for (ValueSerialiser<?> serialiser : serialisers) {
				addSerialiser(serialiser);
			}
			return this;
		}
		
		/**
		 * Clears the serialisers of this builder
		 * 
		 * @return this builder
		 */
		public Builder clearSerialisers() {
			serialisers.clear();
			return this;
		}
		
		/**
		 * Adds the specified value validator to this builder
		 * 
		 * @param key the config key at which to place the validator
		 * @param validator the value validator
		 * @return this builder
		 * @throws NullPointerException if either parameter is null
		 */
		public Builder addValidator(String key, ValueValidator validator) {
			validators.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(validator, "validator"));
			return this;
		}
		
		/**
		 * Adds the specified value validators to this builder
		 * 
		 * @param validators the map of keys at which the validators are placed to the validators themselves
		 * @return this builder
		 * @throws NullPointerException if the map, any key, or any value is null
		 */
		public Builder addValidators(Map<String, ? extends ValueValidator> validators) {
			Objects.requireNonNull(validators, "validators");
			for (Map.Entry<String, ? extends ValueValidator> entry : validators.entrySet()) {
				addValidator(entry.getKey(), entry.getValue());
			}
			return this;
		}
		
		/**
		 * Clears the validators of this builder
		 * 
		 * @return this builder
		 */
		public Builder clearValidators() {
			validators.clear();
			return this;
		}
		
		/**
		 * Sets the {@link ConfigurationSorter} to use when writing the configuration to a stream or channel. <br>
		 * By default there is no sorter (null)
		 * 
		 * @param sorter the configuration sorter to use, or {@code null} for none
		 * @return this builder
		 */
		public Builder sorter(ConfigurationSorter sorter) {
			this.sorter = sorter;
			return this;
		}
		
		/**
		 * Specifies whether enum values should be strictly parsed. By default this is {@code false}. <br>
		 * <br>
		 * When {@code false}, enum values are parsed ignoring case. Otherwise, they must have correct case.
		 * 
		 * @param strictParseEnums whether to strictly parse enums
		 * @return this builder
		 */
		public Builder setStrictParseEnums(boolean strictParseEnums) {
			this.strictParseEnums = strictParseEnums;
			return this;
		}
		
		/**
		 * Specifies whether, when a configuration value is desired as some kind of collection, but the config
		 * value is not a collection, a single element collection should be created from the value and used.
		 * By default this is {@code false}
		 * 
		 * @param createSingleElementCollections whether to create single element collections
		 * @return this builder
		 */
		public Builder setCreateSingleElementCollections(boolean createSingleElementCollections) {
			this.createSingleElementCollections = createSingleElementCollections;
			return this;
		}
		
		/**
		 * Builds a {@code ValidationOptions} from the contents of this builder. <br>
		 * <br>
		 * May be used repeatedly without side effects.
		 * 
		 * @return built options
		 */
		public ConfigurationOptions build() {
			return new ConfigurationOptions(
					ValueSerialiserMap.of(serialisers), ImmutableCollections.mapOf(validators),
					sorter, strictParseEnums, createSingleElementCollections);
		}

		@Override
		public String toString() {
			return "ConfigurationOptions.Builder [serialisers=" + serialisers + ", validators=" + validators
					+ ", sorter=" + sorter + ", strictParseEnums=" + strictParseEnums
					+ ", createSingleElementCollections="+ createSingleElementCollections + "]";
		}
		
	}
	
}
