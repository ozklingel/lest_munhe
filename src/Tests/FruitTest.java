package Tests;

import Server.Game_Server;
import Server.game_service;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import GameElements.*;

import static org.junit.jupiter.api.Assertions.*;

class FruitTest {

    @Test
    void getValue() throws JSONException {
        game_service game = Game_Server.getServer(7);
        int value;
        for (int i = 0; i < game.getFruits().size(); i++) {
            String fruit_json = game.getFruits().get(i);
            JSONObject line = new JSONObject(fruit_json);
            JSONObject ttt = line.getJSONObject("Fruit");
            value = ttt.getInt("value");
            fruit_data f=new Fruit(game.getFruits(),i);
            System.out.println(value+" "+f.getValue());
            assertEquals(value,f.getValue());
        }
    }

    @Test
    void getType() throws JSONException {
        game_service game = Game_Server.getServer(10);
        int Type;
        for (int i = 0; i < game.getFruits().size(); i++) {
            String fruit_json = game.getFruits().get(i);
            JSONObject line = new JSONObject(fruit_json);
            JSONObject ttt = line.getJSONObject("Fruit");
            Type = ttt.getInt("type");
            fruit_data f=new Fruit(game.getFruits(),i);
            System.out.println(Type+" "+f.getType());
            assertEquals(Type,f.getType());
        }
    }

    @Test
    void getLocation() {
        game_service game = Game_Server.getServer(10);
        String location="";
        for (int i = 0; i < game.getFruits().size(); i++) {
            String fruit_json = game.getFruits().get(i);
            try {
                JSONObject line = new JSONObject(fruit_json);
                JSONObject ttt = line.getJSONObject("Fruit");
                location = ttt.getString("pos");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            fruit_data f=new Fruit(game.getFruits(),i);
            System.out.println(location.substring(0,location.indexOf(','))+" "+f.getLocation().x());
            System.out.println(location.substring(location.indexOf(',')+1,location.lastIndexOf(','))+" "+f.getLocation().y());
            assertEquals(Double.parseDouble(location.substring(0,location.indexOf(','))),f.getLocation().x());
            assertEquals(Double.parseDouble(location.substring(location.indexOf(',')+1,location.lastIndexOf(','))),f.getLocation().y());
        }
    }
}