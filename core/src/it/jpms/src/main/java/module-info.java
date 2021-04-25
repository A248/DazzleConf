module com.integration.jpms {

	requires space.arim.dazzleconf;
	opens com.integration.jpms to space.arim.dazzleconf;
	exports com.integration.jpms.exported;
}