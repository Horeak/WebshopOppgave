package Server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class Database {
	@Getter
	private static final Gson gson = new GsonBuilder().serializeNulls().create();

	private static Connection c = null;

	public static Connection getConnection() throws SQLException, ClassNotFoundException{
		if(c != null){
			c.close();
		}

		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir").replace("\\", "/") + "/webshop.sqllite.db");

		return c;
	}
}