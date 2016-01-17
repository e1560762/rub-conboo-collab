package elements;

public class LevelOfAgreement {
	public int total_rating;
	public int number_of_votes;
	public double average_rating;
	
	public LevelOfAgreement() {
		this.total_rating = 0;
		this.number_of_votes = 0;
		this.average_rating = 0d;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("Total rating: ").append(Integer.toString(this.total_rating))
				.append(" Number of votes: ").append(this.number_of_votes)
				.append(" Average rating: ").append(Double.toString(this.average_rating)).toString();
	}
}
