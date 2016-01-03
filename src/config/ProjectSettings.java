package config;

/* Written settings for rapid development, I'd better to apply config file approach */
public class ProjectSettings {
	public final static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	public final static String DB_URL = "jdbc:mysql://127.0.0.1/rcbcf";
	public final static String MOVIELENS_100K_PATH = "/home/yigit/Doktora/KnowledgeEngineering/20151/project/dataset/ml-100k/";
	public final static String MOVIELENS_1M_PATH = "/home/yigit/Doktora/KnowledgeEngineering/20151/project/dataset/ml-1m/";
	public final static String MOVIELENS_10M_PATH = "/home/yigit/Doktora/KnowledgeEngineering/20151/project/dataset/ml-10m/";
	public final static String MOVIELENS_RATING_INPUT_FILE = "u.data";
	public final static String MOVIELENS_USER_INPUT_FILE = "u.user";
	public final static String MOVIELENS_MOVIE_INPUT_FILE = "u.item";
	public final static String MOVIELENS_GENRE_INPUT_FILE = "u.genre";
	public final static String MOVIELENS_OCCUPATION_INPUT_FILE = "u.occupation";
	public final static String MOVIELENS_COLUMN_SEPARATOR = "\\|";
	public final static String GENRE_TABLE_NAME = "genre";
	public final static String MOVIE_TABLE_NAME = "movies";
	public final static String RATING_TABLE_NAME = "ratings";
	public final static String USER_TABLE_NAME = "users";
}
