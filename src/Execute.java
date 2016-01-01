import crawler.HtmlCrawler;



public class Execute {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int number_of_args = args.length; 
		if( number_of_args < 1) {
			System.out.println("You have to specify at least one url");
			return;
		}
		for(int i=0; i<number_of_args; i++)
			System.out.print(args[i].concat(" "));
		System.out.println();
		
		HtmlCrawler hc = new HtmlCrawler();
		hc.crawlMovie(args[0]);
	}

}
