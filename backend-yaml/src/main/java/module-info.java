module space.arim.dazzleconf.backend.yaml {
	exports space.arim.dazzleconf.backend.yaml;
	requires transitive space.arim.dazzleconf;
	requires static org.checkerframework.checker.qual;
	requires org.yaml.snakeyaml; // Not included in moditect source
	opens space.arim.dazzleconf.backend.yaml; // Used for testing purposes
}