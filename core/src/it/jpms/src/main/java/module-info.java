module space.arim.dazzleconf.core.it.jpms {

	requires space.arim.dazzleconf;
	opens space.arim.dazzleconf.core.it.jpms to space.arim.dazzleconf;
	exports space.arim.dazzleconf.core.it.jpms.exported;
}