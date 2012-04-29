package pet.hp.info;

import java.util.*;
import pet.hp.*;

/**
 * Analyses players and hands
 */
public class Info implements HistoryListener {

	/**
	 * players seen
	 */
	private final Map<String, PlayerInfo> playerMap = new TreeMap<String, PlayerInfo>();
	/**
	 * the player info representing the whole population
	 */
	private final PlayerInfo population = new PlayerInfo("*");
	
	private final Map<Long,TournInfo> tournInfos = new TreeMap<Long,TournInfo>();
	private final History history;
	
	public PlayerInfo getPopulation() {
		return population;
	}

	public Info(History history) {
		this.history = history;
		history.addListener(this);
		playerMap.put("*", population);
	}

	/**
	 * Get the list of players matching the given pattern
	 */
	public synchronized List<PlayerInfo> getPlayers(String pattern) {
		pattern = pattern.toLowerCase();
		System.out.println("get players " + pattern);
		List<PlayerInfo> players = new ArrayList<PlayerInfo>();
		for (Map.Entry<String,PlayerInfo> e : this.playerMap.entrySet()) {
			if (e.getKey().toLowerCase().contains(pattern)) {
				players.add(e.getValue());
			}
		}
		System.out.println("got " + players.size() + " players");
		return players;
	}

	public synchronized List<PlayerGameInfo> getGameInfos(String gameid) {
		System.out.println("get game infos for " + gameid);
		List<PlayerGameInfo> gameinfos = new ArrayList<PlayerGameInfo>();
		for (PlayerInfo pi : playerMap.values()) {
			PlayerGameInfo pgi = pi.games.get(gameid);
			if (pgi != null) {
				gameinfos.add(pgi);
			}
		}
		System.out.println("got " + gameinfos.size() + " game infos");
		return gameinfos;
	}

	public synchronized PlayerInfo getPlayerInfo(String player) {
		return playerMap.get(player);
	}

	private synchronized PlayerInfo getPlayerInfo(String player, boolean create) {
		PlayerInfo pi = playerMap.get(player);
		if (pi == null && create) {
			playerMap.put(player, pi = new PlayerInfo(player));
		}
		return pi;
	}
	
	/**
	 * get hand infos for the player and game.
	 * always returns new list
	 */
	public synchronized List<HandInfo> getHandInfos(String player, String gameid) {
		return HandInfo.getHandInfos(history.getHands(player, gameid));
	}
	
	/**
	 * Get hand infos for the tournament.
	 * always returns new list
	 */
	public synchronized List<HandInfo> getHandInfos(long tournid) {
		return HandInfo.getHandInfos(history.getHands(tournid));
	}
	
	/**
	 * get list of tourn infos.
	 * always returns new list
	 */
	public synchronized List<TournInfo> getTournInfos() {
		ArrayList<TournInfo> l = new ArrayList<TournInfo>(tournInfos.values());
		return l;
	}

	/**
	 * Add just one more hand to player info map
	 */
	@Override
	public synchronized void handAdded(Hand hand) {
		// update player info with seat
		for (Seat s : hand.seats) {
			PlayerInfo pi = getPlayerInfo(s.name, true);
			pi.add(hand, s);

			// add to population, but XXX careful not to over count hand stuff
			population.add(hand, s);
			
			// XXX if recent, fire pgi updated?
		}
		
		Tourn t = hand.tourn;
		if (t != null) {
			TournInfo ti = tournInfos.get(t.id);
			if (ti == null) {
				tournInfos.put(t.id, ti = new TournInfo(t));
			}
			ti.addHand(hand);
			
			// XXX if recent, fire tourn info updated?
		}
	}

	@Override
	public void gameAdded(Game game) {
		// yawn
	}

}