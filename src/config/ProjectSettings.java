package config;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

/* Written settings for rapid development, I'd better to apply config file approach */
public class ProjectSettings {
	public final static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	public final static String DB_URL = "jdbc:mysql://127.0.0.1/rcbcf";
	public final static String MONGO_IP = "localhost";
	public final static int MONGO_PORT = 27017;
	public final static String MONGO_MOVIES_TABLE_MOVIE_ID_KEY = "mid";
	public final static String MONGO_MOVIES_TABLE_TITLE_KEY = "t";
	public final static String MONGO_MOVIES_TABLE_DIRECTOR_KEY = "d";
	public final static String MONGO_MOVIES_TABLE_PLAYER_KEY = "p";
	public final static String MONGO_MOVIES_TABLE_CONTENT_KEY = "c";
	public final static String MONGO_MOVIES_TABLE_RELATED_TITLE_KEY = "rt";
	public final static String MONGO_MOVIES_TABLE_RELATED_DIRECTOR_KEY = "rd";
	public final static String MONGO_DB_NAME = "rcbcf";
	public final static String MONGO_MOVIE_COLLECTION_NAME = "movies";
	public final static String MONGO_USER_COLLECTION_NAME = "users";
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
	public final static int MOVIELENS_100K_NUM_OF_MOVIES = 1682;
	public final static int MOVIELENS_100K_NUM_OF_GENRES = 19;
	public final static int MOVIELENS_100K_NUM_OF_USERS = 943;
	public final static String USER_AGENT = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36";
	public final static String GENRE_NAMES[] = {"unknown", "Action", "Adventure", "Animation",
	                                             "Childrens", "Comedy", "Crime", "Documentary", "Drama", "Fantasy",
	                                             "Film-Noir", "Horror", "Musical", "Mystery", "Romance", "Sci-Fi",
	                                             "Thriller", "War", "Western"
	                                           };
}
