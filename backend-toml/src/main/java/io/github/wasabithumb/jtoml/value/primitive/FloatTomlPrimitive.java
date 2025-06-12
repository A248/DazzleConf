package io.github.wasabithumb.jtoml.value.primitive;

import java.text.NumberFormat;
import java.util.Locale;

final class FloatTomlPrimitive extends AbstractTomlPrimitive<Double> {

    private static String autoChars(double value) {
        if (value == Double.POSITIVE_INFINITY) return "inf";
        if (value == Double.NEGATIVE_INFINITY) return "-inf";
        if (Double.isNaN(value)) return "nan";
        if ((value % 1) == 0) {
            if (Double.doubleToLongBits(value) == -9223372036854775808L) return "-0";
            return Long.toString((long) value);
        }
        NumberFormat decimalFormat = NumberFormat.getInstance(Locale.ROOT);
        decimalFormat.setMaximumFractionDigits(15);
        decimalFormat.setMinimumFractionDigits(1);
        return decimalFormat.format(value);
    }

    //

    private final double value;
    private final String chars;

    /** Called by {@code UnsafePrimitives} in {@code jtoml-internals} */
    public FloatTomlPrimitive(double value, String chars) {
        this.value = value;
        this.chars = chars;
    }

    public FloatTomlPrimitive(double value) {
        this(value, autoChars(value));
    }

    //

    @Override
    public TomlPrimitiveType type() {
        return TomlPrimitiveType.FLOAT;
    }

    @Override
    public Double value() {
        return this.value;
    }

    @Override
    public String asString() {
        return this.chars;
    }

    @Override
    public boolean asBoolean() {
        return this.value != 0d;
    }

    @Override
    public long asLong() {
        return (long) this.value;
    }

    @Override
    public double asDouble() {
        return this.value;
    }

}