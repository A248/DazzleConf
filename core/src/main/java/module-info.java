module space.arim.dazzleconf {
	requires static org.apiguardian.api;
    requires static org.checkerframework.checker.qual;
	exports space.arim.dazzleconf;
	exports space.arim.dazzleconf.backend;
	exports space.arim.dazzleconf.engine;
	exports space.arim.dazzleconf.engine.liaison;
	exports space.arim.dazzleconf.internals.lang to space.arim.dazzleconf.backend.hocon,
			space.arim.dazzleconf.backend.toml, space.arim.dazzleconf.backend.yaml;
	exports space.arim.dazzleconf.migration;
	exports space.arim.dazzleconf.reflect;
}