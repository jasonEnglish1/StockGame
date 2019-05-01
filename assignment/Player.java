package assignment;

/**
 * Created by jengli on 29/11/2017.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Player {

     int cash = 0;
     Map<String, Integer> playerStocks;
     private String username;


    Player(String username){
        playerStocks = new HashMap<>();
        cash += 500;
        this.username = username;
        for (Stock value: Stock.values()) {
            playerStocks.put(value.toString(), 0);
        }
        produceStock();
    }

    private void produceStock(){
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            Stock value = Stock.values()[rand.nextInt(playerStocks.size())];
            playerStocks.put(value.toString() , playerStocks.get(value.toString())+1);
        }

    }

    public String getUsername(){
        return username;
    }


}
