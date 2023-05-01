package hu.aestallon.jsmol.parser;

import hu.aestallon.jsmol.parser.JsonValue.*;
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

import static java.util.stream.Collectors.toMap;

public final class JsmolParser {

  @FunctionalInterface
  interface Parser {
    Result<ParseCursor> parse(String str, int idx);
  }

  record ParseCursor(String str, int idx, JsonValue res) {}

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
  // Static parsing algorithms
  private static Result<ParseCursor> parseLiteral(String lit, JsonValue res, String str, int idx) {
    for (int i = 0; i < lit.length(); i++) {
      if (lit.charAt(i) != str.charAt(i + idx)) {
        return new Err<>(new ParseCursor(str, idx, null));
      }
    }
    return new Ok<>(new ParseCursor(str, idx + lit.length(), res));
  }

  private static Result<ParseCursor> parseWithPattern(Pattern ptn,
                                                      Function<String, JsonValue> resultProcessor,
                                                      String str,
                                                      int idx) {
    final Matcher m = ptn.matcher(str);
    return (m.find(idx) && m.start() == idx)
        ? new Ok<>(new ParseCursor(
            str,
            idx + m.group().length(),
            resultProcessor.apply(m.group())))
        : new Err<>(new ParseCursor(str, idx, null));
  }

  // -----------------------------------------------------------------------------------------------
  // Type specific parser implementations
  private final Parser NULL_PARSER   =
      (str, idx) -> parseLiteral(NULL_LITERAL, new JsonNull(), str, idx);
  private final Parser BOOL_PARSER   =
      (str, idx) -> parseLiteral(TRUE_LITERAL, new JsonBoolean(true), str, idx)
          .or(() -> parseLiteral(FALSE_LITERAL, new JsonBoolean(false), str, idx));
  private final Parser NUMBER_PARSER =
      (str, idx) -> parseWithPattern(
          NUM_PTRN,
          s -> s.contains(".")
              ? new JsonNumber(Double.parseDouble(s))
              : new JsonNumber(Long.parseLong(s)),
          str, idx);
  private final Parser STRING_PARSER =
      (str, idx) -> parseWithPattern(
          STR_PTRN,
          s -> s.length() == 2
              ? new JsonString("")
              : new JsonString(s.substring(1, s.length() - 1)),
          str, idx);
  private final Parser ARRAY_PARSER  = (str, idx) -> {
    Result<ParseCursor> prefix = parseWithPattern(ARRAY_START_PTRN, DISCARD, str, idx);
    if (!prefix.isOk()) {
      return prefix;
    }
    List<Result<ParseCursor>> r = new ArrayList<>();
    int i = prefix.unwrap().idx();
    while (true) {
      var endParse = parseWithPattern(ARRAY_END_PTRN, DISCARD, str, i);
      if (endParse.isOk()) {
        return new Ok<>(new ParseCursor(str, endParse.unwrap().idx(), new JsonArray(r.stream()
            .map(Result::unwrap)
            .map(ParseCursor::res)
            .toList())));
      }
      if (!r.isEmpty()) {
        var sepParse = parseWithPattern(ELEMENT_SEP_PTRN, DISCARD, str, i);
        if (sepParse.isOk()) {
          i = sepParse.unwrap().idx();
        } else {
          return sepParse;
        }
      }
      var e = parseInternal(str, i);
      if (e.isOk()) {
        r.add(e);
        i = e.unwrap().idx();
      } else {
        return e;
      }
    }
  };
  private final Parser OBJECT_PARSER = (str, idx) -> {
    Result<ParseCursor> prefix = parseWithPattern(OBJ_START_PTRN, DISCARD, str, idx);
    if (!prefix.isOk()) {
      return prefix;
    }
    Map<String, Result<ParseCursor>> r = new LinkedHashMap<>();
    int i = prefix.unwrap().idx();
    while (true) {
      var endParse = parseWithPattern(OBJ_END_PTRN, DISCARD, str, i);
      if (endParse.isOk()) {
        return new Ok<>(new ParseCursor(str, endParse.unwrap().idx(),
            new JsonObject(r.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().unwrap().res()))
                .collect(toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (o1, o2) -> o1,
                    LinkedHashMap::new)))));
      }
      if (!r.isEmpty()) {
        var sepParse = parseWithPattern(ELEMENT_SEP_PTRN, DISCARD, str, i);
        if (!sepParse.isOk()) {
          return sepParse;
        }
        i = sepParse.unwrap().idx();
      }

      var keyParse = STRING_PARSER.parse(str, i);
      if (!keyParse.isOk()) {
        return keyParse;
      }
      i = keyParse.unwrap().idx();

      final String key = ((JsonString) keyParse.unwrap().res()).value();
      if (r.containsKey(key)) {
        return new ExErr<>(new IllegalStateException("duplicate key: " + key + " at idx: " + i));
      }

      var sepParse = parseWithPattern(ENTRY_SEP_PTRN, DISCARD, str, i);
      if (!sepParse.isOk()) {
        return sepParse;
      }
      i = sepParse.unwrap().idx();

      var valParse = parseInternal(str, i);
      if (!valParse.isOk()) {
        return valParse;
      }
      i = valParse.unwrap().idx();
      r.put(key, valParse);
    }
  };

  private static final Function<String, JsonValue> DISCARD = s -> null;


  public Result<JsonValue> external(String s) {
    return parseInternal(s).map(ParseCursor::res);
  }

  private Result<ParseCursor> parseInternal(String str) {
    return this.parseInternal(str, 0);
  }

  private Result<ParseCursor> parseInternal(String str, int idx) {
    return NULL_PARSER.parse(str, idx)
        .or(() -> BOOL_PARSER.parse(str, idx))
        .or(() -> NUMBER_PARSER.parse(str, idx))
        .or(() -> STRING_PARSER.parse(str, idx))
        .or(() -> ARRAY_PARSER.parse(str, idx))
        .or(() -> OBJECT_PARSER.parse(str, idx));
  }

}
