package hu.aestallon.jsmol.json;

/**
 * Marker interface for value types interpreted in JavaScript Object Notation (JSON).
 *
 * @author Szabolcs Bazil Papp
 */
public sealed interface JsonValue permits WrappedValue, JsonNull {}
