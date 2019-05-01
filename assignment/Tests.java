package assignment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tests {
	
	// Sample round, see FAQ at https://orb.essex.ac.uk/ce/ce303/restricted/assign/ce303assignFaq2017.html 

	public Game game;


	public static int[][] sampleShares() {
		return new int[][] { 
			{ 3, 0, 1, 4, 2 }, 
			{ 2, 2, 5, 0, 1 }, 
			{ 4, 1, 0, 1, 4 } 
		};
	}

	@Before
	public void setup() {
		Map<String, Deck> map = new HashMap<>();
		map.put("Apple", new Deck(Stock.Apple, -20, 5, 10, -5, -10, 20));
		map.put("BP", new Deck(Stock.BP, 20, 5, 10, -5, -10, -20));
		map.put("Cisco", new Deck(Stock.Cisco, 20, -5, 10, -20, -10, 5));
		map.put("Dell", new Deck(Stock.Dell, 5, -20, 10, 20, -10, -5));
		map.put("Ericsson", new Deck(Stock.Ericsson, -10, 10, 20, 5, -20, -5));

		List<Player> player = new ArrayList<>();
		player.add(new Player("Player1"));
		player.add(new Player("Player2"));
		player.add(new Player("Player3"));


		for (Stock stock: Stock.values()) {
			player.get(0).playerStocks.put(stock.name(),sampleShares()[0][stock.ordinal()]);
			player.get(1).playerStocks.put(stock.name(),sampleShares()[1][stock.ordinal()]);
			player.get(2).playerStocks.put(stock.name(),sampleShares()[2][stock.ordinal()]);
		}


		game = new Game(map,player);
		}





	@Test
	public void tradeP0() {
		game.sell("Player1", Stock.Apple, 3);
		game.buy("Player1", Stock.Cisco, 6);
		Map<String, Integer> map2 = new HashMap<>();
		map2.put("Apple", 0);
		map2.put("BP",0);
		map2.put("Cisco",7);
		map2.put("Dell",4);
		map2.put("Ericsson",2);
		Assert.assertEquals(game.getShares(0), map2);
		Assert.assertEquals(game.getCash(0), 182);
	}

	@Test
	public void tradeP1() {
		game.buy("Player2", Stock.BP, 4);
		Map<String, Integer> map2 = new HashMap<>();
		map2.put("Apple", 2);
		map2.put("BP",6);
		map2.put("Cisco",5);
		map2.put("Dell",0);
		map2.put("Ericsson",1);
		Assert.assertEquals(game.getShares(1), map2);
		Assert.assertEquals(game.getCash(1), 88);
	}

	@Test
	public void tradeP2() {
		game.sell("Player3", Stock.Ericsson, 4);
		game.sell("Player3", Stock.Apple, 4);
		Map<String, Integer> map2 = new HashMap<>();
		map2.put("Apple", 0);
		map2.put("BP",1);
		map2.put("Cisco",0);
		map2.put("Dell",1);
		map2.put("Ericsson",0);
		Assert.assertEquals(game.getShares(2), map2);
		Assert.assertEquals(game.getCash(2), 1300);
	}

}
