package elements;

import java.util.HashMap;

public class User {
	private String occupation;
	private String age;
	private String location;
	private String favorite_genre;
	private HashMap<String, Integer> ratingVector; /* A rating vector for user, key is movie title and value is rating*/
	
	public String getOccupation() {return this.occupation;}
	public String getAge() {return this.age;}
	public String getLocation() {return this.location;}
	public String getFavoriteGenre() {return this.favorite_genre;}
	public HashMap<String, Integer> getRatings() {return this.ratingVector;}
}
