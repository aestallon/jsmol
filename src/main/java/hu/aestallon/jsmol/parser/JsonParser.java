package hu.aestallon.jsmol.parser;

import hu.aestallon.jsmol.json.*;
import hu.aestallon.jsmol.result.Err;
import hu.aestallon.jsmol.result.ExErr;
import hu.aestallon.jsmol.result.Ok;
import hu.aestallon.jsmol.result.Result;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hu.aestallon.jsmol.parser.JsonParser.RawStringProcessor.DISCARD;

public final class JsonParser {

  @FunctionalInterface
  interface Parser {
    Result<ParseCursor> parse(ParseCursor cursor);
  }

  @FunctionalInterface
  interface RawStringProcessor extends Function<String, JsonValue> {
    RawStringProcessor DISCARD = s -> null;
  }

  record ParseCursor(String str, int idx, JsonValue res) {
    Result<ParseCursor> advanceOnLiteral(String lit, JsonValue res) {
      for (int i = 0; i < lit.length(); i++) {
        if (lit.charAt(i) != str.charAt(i + idx)) {
          return new Err<>(this);
        }
      }
      return new Ok<>(new ParseCursor(str, idx + lit.length(), res));
    }

    Result<ParseCursor> advanceOnPattern(Pattern ptn, RawStringProcessor resultProcessor) {
      final Matcher m = ptn.matcher(str);
      return (m.find(idx) && m.start() == idx)
          ? new Ok<>(new ParseCursor(
          str,
          idx + m.group().length(),
          resultProcessor.apply(m.group())))
          : new Err<>(this);
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Literals and regular expressions
  private static final String  NULL_LITERAL      = "null";
  private static final String  TRUE_LITERAL      = "true";
  private static final String  FALSE_LITERAL     = "false";
  private static final String  NUM_REGEX         = "-?\\d+(?:\\.\\d+)?";
  private static final Pattern NUM_PTRN          = Pattern.compile(NUM_REGEX);
  private static final String  STR_REGEX         = "\\\"(?:(?:\\\\\\\")|[^\\\"])*\\\"";
  private static final Pattern STR_PTRN          = Pattern.compile(STR_REGEX);
  private static final String  ELEMENT_SEP_REGEX = "\\,[\\r\\n\\s]*";
  private static final Pattern ELEMENT_SEP_PTRN  = Pattern.compile(ELEMENT_SEP_REGEX);
  private static final String  ARRAY_START_REGEX = "\\[[\\r\\n\\s]*";
  private static final Pattern ARRAY_START_PTRN  = Pattern.compile(ARRAY_START_REGEX);
  private static final String  ARRAY_END_REGEX   = "[\\r\\n\\s]*\\]";
  private static final Pattern ARRAY_END_PTRN    = Pattern.compile(ARRAY_END_REGEX);
  private static final String  OBJ_START_REGEX   = "\\{[\\r\\n\\s]*";
  private static final Pattern OBJ_START_PTRN    = Pattern.compile(OBJ_START_REGEX);
  private static final String  OBJ_END_REGEX     = "[\\r\\n\\s]*\\}";
  private static final Pattern OBJ_END_PTRN      = Pattern.compile(OBJ_END_REGEX);
  private static final String  ENTRY_SEP_REGEX   = "[\\r\\n\\s]*\\:[\\r\\n\\s]*";
  private static final Pattern ENTRY_SEP_PTRN    = Pattern.compile(ENTRY_SEP_REGEX);

  // -----------------------------------------------------------------------------------------------
  // Type specific parser implementations
  private final Parser NULL_PARSER   = c -> c.advanceOnLiteral(NULL_LITERAL, new JsonNull());
  private final Parser BOOL_PARSER   = c -> c
      .advanceOnLiteral(TRUE_LITERAL, new JsonBoolean(true))
      .or(() -> c.advanceOnLiteral(FALSE_LITERAL, new JsonBoolean(false)));
  private final Parser NUMBER_PARSER = c -> c.advanceOnPattern(
      NUM_PTRN,
      s -> s.contains(".")
          ? new JsonNumber(Double.parseDouble(s))
          : new JsonNumber(Long.parseLong(s)));
  private final Parser STRING_PARSER = c -> c.advanceOnPattern(
      STR_PTRN,
      s -> s.length() == 2
          ? new JsonString("")
          : new JsonString(s.substring(1, s.length() - 1)));
  private final Parser ARRAY_PARSER  = c -> c
      .advanceOnPattern(ARRAY_START_PTRN, DISCARD)
      .flatMap(cursor -> {
        final List<JsonValue> list = new ArrayList<>();
        Result<ParseCursor> result;
        while ((result = cursor.advanceOnPattern(ARRAY_END_PTRN, DISCARD)).isErr()) {
          if (!list.isEmpty()) {
            result = cursor.advanceOnPattern(ELEMENT_SEP_PTRN, DISCARD);
            if (result.isErr()) {return result;} else {cursor = result.unwrap();}
          }
          result = this.parseInternal(cursor);
          if (result.isErr()) {return result;} else {
            cursor = result.unwrap();
            list.add(cursor.res);
          }
        }
        cursor = result.unwrap();
        final JsonArray jsonArray = new JsonArray(list);
        return Ok.of(new ParseCursor(cursor.str, cursor.idx, jsonArray));
      });
  private final Parser OBJECT_PARSER = c -> c
      .advanceOnPattern(OBJ_START_PTRN, DISCARD)
      .flatMap(cursor -> {
        final Map<String, JsonValue> map = new LinkedHashMap<>();
        Result<ParseCursor> result;
        while ((result = cursor.advanceOnPattern(OBJ_END_PTRN, DISCARD)).isErr()) {
          if (!map.isEmpty()) {
            result = cursor.advanceOnPattern(ELEMENT_SEP_PTRN, DISCARD);
            if (result.isErr()) {return result;} else {cursor = result.unwrap();}
          }
          result = STRING_PARSER.parse(cursor);
          if (result.isErr()) {return result;} else {cursor = result.unwrap();}

          final String key = ((JsonString) cursor.res()).value();
          if (map.containsKey(key)) {
            return new ExErr<>(
                new IllegalStateException("duplicate key: " + key + " at idx: " + cursor.idx));
          }

          result = cursor.advanceOnPattern(ENTRY_SEP_PTRN, DISCARD);
          if (result.isErr()) {return result;} else {cursor = result.unwrap();}

          result = this.parseInternal(cursor);
          if (result.isErr()) {return result;} else {
            cursor = result.unwrap();
            map.put(key, cursor.res);
          }
        }
        cursor = result.unwrap();
        final JsonObject jsonObject = new JsonObject(map);
        return Ok.of(new ParseCursor(cursor.str, cursor.idx, jsonObject));
      });

  public Result<JsonValue> parse(String s) {
    return parseInternal(s).map(ParseCursor::res);
  }

  private Result<ParseCursor> parseInternal(String s) {
    return this.parseInternal(new ParseCursor(s, 0, null));
  }

  private Result<ParseCursor> parseInternal(ParseCursor c) {
    return NULL_PARSER.parse(c)
        .or(() -> BOOL_PARSER.parse(c))
        .or(() -> NUMBER_PARSER.parse(c))
        .or(() -> STRING_PARSER.parse(c))
        .or(() -> ARRAY_PARSER.parse(c))
        .or(() -> OBJECT_PARSER.parse(c));
  }

}
