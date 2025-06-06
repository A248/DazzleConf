module space.arim.dazzleconf.backend.yaml {
    exports space.arim.dazzleconf.backend.yaml;
    requires transitive space.arim.dazzleconf;
    requires static org.checkerframework.checker.qual;
    requires org.snakeyaml.engine.v2; // Removed during shading
    opens space.arim.dazzleconf.backend.yaml; // Used for testing, also removed later
}