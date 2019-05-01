package assignment;

import java.util.*;
import java.util.concurrent.CyclicBarrier;

public class Game {

	private static List<Player> players;
	private static Map<String, Integer> gameStocks;
	private static Map<String,Deck> decks;
	private static Map<String,Integer> votes = new HashMap<>();
	public static CyclicBarrier turnEnd;

	public Game() {
		players = new ArrayList<>();
		gameStocks = new HashMap<>();
		decks = new HashMap<>();
		//stocks
		for (Stock value: Stock.values()) {
			gameStocks.put(value.toString(), 100);
		}
		//cards
		for (Stock value : Stock.values()){
			decks.put(value.toString(), new Deck(value));
		}
    }

    public Game(Map<String, Deck> decksTest , List<Player> testPlayer){
    	decks = decksTest;
		players = testPlayer;
		gameStocks = new HashMap<>();
    	for (Stock s: Stock.values())
    	{
    		gameStocks.put(s.name(),100);
			votes.put(s.name(),100);
    	}
	}

    public  String[] getPlayers(){
    	return Constants.PLAYERS;
	}

	public String getPrice(){
		String result = "";
		for (Map.Entry<String, Integer> entry: gameStocks.entrySet()) {
			result += "|" + entry.getKey() + " : " + entry.getValue() + "|  ";
		}
		result += System.lineSeparator();

		return result;
	}

	public void createPlayers(int total){
		if (players.isEmpty()){
			for (int i = 0; i < total; i++) {
				players.add(new Player(Constants.PLAYERS[i]));
			}
		}
		turnEnd = new CyclicBarrier(players.size());
	}

	public int getCash(int playerId) {
		return players.get(playerId).cash;
	}

	public Map<String, Integer> getShares(int playerId) {
		return players.get(playerId).playerStocks;
	}

	public String getPlayersPrint(){
		String result = "";
		for (int i = 0; i < players.size(); i++) {
			result += "|Player " + (i+1) + ", cash : " + getCash(i) + ",shares : " + getShares(i) +"|";
			result += "\r\n";
		}
		return result;
	}

	public String getCards() {
		String result ="";
		for (Map.Entry<String, Deck> entry: decks.entrySet()){
			result += "|" + entry.getKey() + " : " + entry.getValue().topCard() + "|  ";
		}
		result += System.lineSeparator();

		return result;
	}


	public void executeVotes() {
		synchronized (this) {
			for (Map.Entry<String, Integer> entry : votes.entrySet()) {
				if (entry.getValue() > 0) {
					for (Map.Entry<String, Deck> entry2 : decks.entrySet()) {
						if (entry.getKey().equals(entry2.getKey())) {
							int value = Integer.parseInt(entry2.getValue().topCard());
							gameStocks.put(entry.getKey(), ((gameStocks.get(entry.getKey())) + value));
							entry2.getValue().removeTopCard();
						}
					}
				}
				if (entry.getValue() < 0){
					for (Map.Entry<String, Deck> entry2 : decks.entrySet()) {
						if (entry.getKey().equals(entry2.getKey())) {
							entry2.getValue().removeTopCard();
						}
					}

				}
			}
			for (Map.Entry<String, Integer> entry : votes.entrySet()) {
				votes.put(entry.getKey(), 0);
			}
		}
	}

	public Boolean buy(String username, Stock s, int amount) {
			int spent = ((gameStocks.get(s.name()) + 3) * amount);
			for (Player person : players) {
				if (Objects.equals(person.getUsername(), username)) {
					if (person.cash >= spent) {
						person.cash -= spent;
						person.playerStocks.put(s.name(), person.playerStocks.get(s.name()) + amount);
						return Boolean.TRUE;
					}
				}
			}
		return Boolean.FALSE;
	}

	public Boolean sell(String username, Stock s, int amount) {
			int earnt = gameStocks.get(s.name()) * amount;
			for (Player person : players) {
				if (Objects.equals(person.getUsername(), username)) {
					if (person.playerStocks.get(s.name()) >= amount) {
						person.cash += earnt;
						person.playerStocks.put(s.name(), person.playerStocks.get(s.name()) - amount);
						return Boolean.TRUE;
					}
				}
			}
			return Boolean.FALSE;
}

	public Boolean vote(Stock s, boolean yes) {
		synchronized (votes) {
			if (!votes.containsKey(s.name())) {
				votes.put(s.name(), 0);
			}
			if (yes) {
				votes.put(s.name(), votes.get(s.name()) + 1);
			}
			if (!yes) {
				votes.put(s.name(), votes.get(s.name()) - 1);
			}
			return Boolean.TRUE;
		}
	}

	public String getWinner(){
		int max = 0;
		String result = "Winner :";
		for (Player person: players) {
			sellAll(person);
		}
		for (Player person2: players) {
			if (person2.cash > max){
				max = person2.cash;
			}
		}
		for (Player personResult: players){
			if (personResult.cash == max){
				result += personResult.getUsername() + ", Score : " + personResult.cash;
			}
		}
	return result;
	}

	private void sellAll(Player person){
		for (Stock stocks: Stock.values()) {
			sell(person.getUsername(),stocks, person.playerStocks.get(stocks.name()));
		}
	}

}
