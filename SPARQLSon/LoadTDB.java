package SPARQLSon;

public class LoadTDB {

	public static void main(String[] args) {

		String TDBdirectory = "/Users/adriansotosuarez/Desktop/distinct25";
		DatabaseWrapper dbw = new DatabaseWrapper(TDBdirectory);
		dbw.createDataset("/Users/adriansotosuarez/Desktop/distinct/db_100_25_100.ttl", "TTL");
		
	}

}
