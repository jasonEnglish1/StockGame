package assignment.socket;

// Socket-based bank service 
// Reads and writes ONE line at a time
// Print writer output is set to auto-flush

import assignment.Constants;
import assignment.Game;
import assignment.Stock;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.atomic.AtomicInteger;


public class GameService implements Runnable {
	private Scanner in;
	private PrintWriter out;
	private String players;
	private boolean login;
	private Game game;
	private Boolean turnEnded;
	private int turnsTaken ;
	private int votesMade;
	private String firstVote = "";
	private static int playerCount;
	private static ArrayList<String> users = new ArrayList<>();
	private AtomicInteger roundsAtom;

	public GameService(Game game, Socket socket) {
		this.game = game;
		players = null;
		login = false;
		try {
			in = new Scanner(socket.getInputStream());
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override 
	public void run() {

		turnEnded = false;
		if (users.isEmpty()) {
			out.println("Enter the amount of players");
			playerCount = Integer.parseInt(in.nextLine().trim());
			game.createPlayers(playerCount);
		}
		roundsAtom = new AtomicInteger(1);

		login();
		while (login) {
			turnsTaken = 0;
			votesMade = 0;
			out.println("Round:" + roundsAtom.toString());
			out.println("Stock Prices");
			out.println(game.getPrice());
			out.println("Top cards");
			out.println(game.getCards());
			out.println("Players");
			out.println(game.getPlayersPrint());
			try {
				while (!turnEnded) {
					Request request = Request.parse(in.nextLine());
					String response = execute(game, request);
					// note use of \r\n for CRLF
					out.println(response + "\r\n");
					turnEnded = (turnsTaken == 2 && votesMade == 2);
				}
				try {
					Game.turnEnd.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
				game.executeVotes();
				turnEnded = false;
				firstVote = "";
				if (roundsAtom.intValue() == 5){
					out.println("-------------- GAME FINISHED --------------");
					out.println(game.getWinner());
					break;
				}
				roundsAtom.getAndIncrement();

			} catch (NoSuchElementException e) {
				login = false;
			}
		}
		logout();
	}

	public void login() {

		try {
			String input = Constants.PLAYERS[users.size()];
			if ((users.size() < playerCount)) {
			users.add(users.size(),Constants.PLAYERS[users.size()]);
				if (Arrays.asList(game.getPlayers()).contains(input)) {
					players = input;
					out.println("Welcome " + players + "!");
					System.out.println("Login: " + players);
					login = true;
			}
			} else {
				out.println("Invalid login attempt!");
			}
			out.println(); // don't forget empty line terminator!
		} catch (NoSuchElementException e) {
		}
	}

	public void logout() {
		if (players != null) {
			System.out.println("Logout: " + players);
		}
		try {
			Thread.sleep(2000);
			in.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String execute(Game game, Request request) {
		System.out.println(request);

		int amount;
		Stock stock = null;
		if (request.params.length > 1) {
			for (Stock value : Stock.values()) {
				if (value.name().toUpperCase().equals(request.params[0].toUpperCase())) {
					stock = value;
				}
			}
		}
		try {
			switch (request.type) {
				case BUY:
					if (stock == null){return "Invalid stock";}
					amount = Integer.parseInt(request.params[1]);
					if (game.buy(players, stock, amount)) {
						turnsTaken++;
						return "Successful";
					}
					return "Unsuccessful";

				case SELL:
					if (stock == null){return "Invalid stock";}
					amount = Integer.parseInt(request.params[1]);
					if (game.sell(players, stock, amount)) {
						turnsTaken++;
						return "Successful";
					}
					return "Unsuccessful";

				case INVALID:
					return "Command invalid or failed!";

				case DONE:
					turnsTaken = 2;
					votesMade = 2;
					return "Round skipped";

				case VOTE:
					if (stock == null){return "Invalid stock";}
					if (!(firstVote.equals(stock.name()))) {
						if (request.params[1].equals("yes")) {
							if (game.vote(stock, true)) {
								votesMade++;
								firstVote = stock.name();
								return "Successful";
							}
						} else {
							if (game.vote(stock, false)) {
								votesMade++;
								firstVote = stock.name();
								return "Successful";
							}
						}
					}
					else {
						return "Can only vote on a stock once per round";
					}
			default:
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
