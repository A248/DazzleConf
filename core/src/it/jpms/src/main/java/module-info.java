module space.arim.dazzleconf.core.it.jpms {
	requires space.arim.dazzleconf;
    requires org.checkerframework.checker.qual;
	opens space.arim.dazzleconf.core.it.jpms to space.arim.dazzleconf;
	exports space.arim.dazzleconf.core.it.jpms.exported to space.arim.dazzleconf;
}