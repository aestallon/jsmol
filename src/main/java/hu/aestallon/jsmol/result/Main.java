package hu.aestallon.jsmol.result;

import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.parser.JsmolParser;

public class Main {
  public static void main(String[] args) {
    JsmolParser jSmol = new JsmolParser();
    String str = """
        {
        	"id": "0001",
        	"type": "donut",
        	"name": "Cake",
        	"ppu": 0.55,
        	"batters":
        		{
        			"batter":
        				[
        					{ "id": "1001", "type": "Regular" },
        					{ "id": "1002", "type": "Chocolate" },
        					{ "id": "1003", "type": "Blueberry" },
        					{ "id": "1004", "type": "Devil's Food" }
        				]
        		},
        	"topping":
        		[
        			{ "id": "5001", "type": "None" },
        			{ "id": "5002", "type": "Glazed" },
        			{ "id": "5005", "type": "Sugar" },
        			{ "id": "5007", "type": "Powdered Sugar" },
        			{ "id": "5006", "type": "Chocolate with Sprinkles" },
        			{ "id": "5003", "type": "Chocolate" },
        			{ "id": "5004", "type": "Maple" }
        		]
        }""";
    System.out.println(str);
    Result<JsonValue> res = jSmol.external(str);
    if (res.isOk()) {
      System.out.println(res.unwrap());
    } else {
      System.out.println("ERROR");
    }
  }
}