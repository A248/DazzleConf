module space.arim.dazzleconf {
    requires static org.checkerframework.checker.qual;
	exports space.arim.dazzleconf;
	exports space.arim.dazzleconf.annote;
	exports space.arim.dazzleconf.error;
	exports space.arim.dazzleconf.factory;
	exports space.arim.dazzleconf.helper;
	exports space.arim.dazzleconf.validator;
	exports space.arim.dazzleconf.serialiser;
	exports space.arim.dazzleconf.sorter;
	exports space.arim.dazzleconf2;
	exports space.arim.dazzleconf2.backend;
	exports space.arim.dazzleconf2.engine;
	exports space.arim.dazzleconf2.engine.liaison;
	exports space.arim.dazzleconf2.internals.lang to space.arim.dazzleconf.backend.yaml;
	exports space.arim.dazzleconf2.migration;
	exports space.arim.dazzleconf2.reflect;
}