package elements;

import java.util.ArrayList;
import java.util.List;

public class Movie {
	private String title;
	private List<String> players = new ArrayList<String>();
	
	public String getTitle() {return this.title;}
	public List<String >getPlayers() {return this.players;}
	public boolean isPlayerTakeParticipated(String full_name) {
		boolean isFound = false;
		for(String player : this.players) {
			if(full_name.equals(player)) {
				isFound = true;
				break;
			}				
		}
		return isFound;
	}
}
