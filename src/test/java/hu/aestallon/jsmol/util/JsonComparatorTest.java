/*
 * Copyright 2023 Szabolcs Bazil Papp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hu.aestallon.jsmol.util;

import hu.aestallon.jsmol.json.JsonObject;
import hu.aestallon.jsmol.json.JsonString;
import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.marshaller.JsonValueMapper;
import hu.aestallon.jsmol.parser.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonComparatorTest {

  private final JsonParser jsonParser     = new JsonParser();
  private final JsonComparator comparator = new JsonComparator();


  @Test
  void changingStringYieldsTheSecondString() throws Exception {
    final JsonString left = new JsonString("abc");
    final JsonString right = new JsonString("def");
    assertEquals(new JsonString("def"), comparator.compare(left, right));
  }

  @Test
  void addNewListElementTest() throws Exception {
    final String json1 = """
        {
          "name"           : "John Doe",
          "age"            : 32,
          "isMale"         : true,
          "favouriteCakes" : [
            {
              "name"     : "Feketeerdő",
              "hasChoco" : true
            },
            {
              "name"     : "Rákóczy-krémes",
              "hasChoco" : false
            }
          ]
        }""";
    final String json2 = """
        {
          "name"           : "John Doe",
          "age"            : 32,
          "isMale"         : true,
          "favouriteCakes" : [
            {
              "name"     : "Feketeerdő",
              "hasChoco" : true
            },
            {
              "name"     : "TúróRudi",
              "hasChoco" : true
            },
            {
              "name"     : "Rákóczy-krémes",
              "hasChoco" : false
            }
          ]
        }""";
    JsonValue left = jsonParser.parse(json1).unwrap();
    JsonValue right = jsonParser.parse(json2).unwrap();

    final String expected = """
        {
        "favouriteCakes" : [
          {
            "name"     : "TúróRudi",
            "hasChoco" : true
          }
        ]
        }""";
    final JsonValue expectedJson = jsonParser.parse(expected).unwrap();

    JsonValue result = comparator.compare(left, right);
    assertEquals(expectedJson, result);
    System.out.println("left: " + left);
    System.out.println("right: " + right);
    System.out.println("difference: " + result);
  }

  @Test
  void changingSelectionOfAToDoListWorks() throws Exception {
    final Map<String, Object> m = new HashMap<>();
    m.put("currentItem", Map.of("idx", (Object) Integer.valueOf(12), "title", (Object) "Chores"));
    m.put("items", List.of(
        (Object) Map.of(
            "idx", (Object) 12,
            "title", (Object) "Chores"),
        (Object) Map.of(
            "idx", (Object) 11,
            "title", (Object) "Play the Piano"),
        (Object) Map.of(
            "idx", (Object) 10,
            "title", (Object) "Code something up!")));

    final JsonValueMapper mapper = new JsonValueMapper();

    final JsonValue left = mapper.marshall(m).unwrap();

    m.put("currentItem", (Object) Map.of(
        "idx", (Object) 10,
        "title", (Object) "Code something up!"));

    final JsonValue right = mapper.marshall(m).unwrap();

    Map<String, Object> expected = Map.of("currentItem", (Object) Map.of(
        "idx", (Object) 10,
        "title", (Object) "Code something up!"));
    Object actual = mapper.unmarshall(comparator.compare(left, right)).unwrap();
    assertEquals(expected, actual);
    System.out.println("left: " + left);
    System.out.println("right: " + right);
    System.out.println("difference: " + actual);


  }

}