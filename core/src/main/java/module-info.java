module space.arim.dazzleconf {
	exports space.arim.dazzleconf;
	exports space.arim.dazzleconf.annote;
	exports space.arim.dazzleconf.error;
	exports space.arim.dazzleconf.factory;
	exports space.arim.dazzleconf.validator;
	exports space.arim.dazzleconf.serialiser;
	exports space.arim.dazzleconf.sorter;

	exports space.arim.dazzleconf.internal to space.arim.dazzleconf.ext.gson;
	exports space.arim.dazzleconf.internal.deprocessor to space.arim.dazzleconf.ext.gson;
}