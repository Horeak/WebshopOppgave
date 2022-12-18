package Server;

import Common.Objects.Order;
import Common.Objects.User;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

class Users {
	private static Cache<Long, User> userCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(1, TimeUnit.MINUTES).build();

	public static User getUserById(Long id){
		return getUserCache().stream().filter(s -> Objects.equals(s.getUserId(), id)).findFirst().orElse(null);
	}

	public static User getUserByUsername(String username){
		return getUserCache().stream().filter(s -> Objects.equals(s.getUserName(), username)).findFirst().orElse(null);
	}

	public static void deleteUser(Long id){
		userCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("DELETE FROM USERS WHERE ID = ?");
					statement.setLong(1, id);
					statement.executeUpdate();

				} catch (SQLException e) {
					e.printStackTrace();
				}
			} finally {
				statement.close();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateUser(User user){
		userCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("UPDATE USERS SET USERNAME = ?, DISPLAYNAME = ?, EMAIL = ?, ORDERS = ? WHERE ID = ?");
					statement.setString(1, user.getUserName());
					statement.setString(2, user.getDisplayName());
					statement.setString(3, user.getEmail());
					statement.setString(4, Database.getGson().toJson(user.getUserOrders()));
					statement.setLong(5, user.getUserId());

					statement.executeUpdate();

				} catch (SQLException e) {
					e.printStackTrace();
				}
			} finally {
				statement.close();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addUser(User user){
		userCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("INSERT INTO USERS VALUES(?,?,?,?,?)");
					statement.setLong(1, user.getUserId());
					statement.setString(2, user.getUserName());
					statement.setString(3, user.getDisplayName());
					statement.setString(4, user.getEmail());
					statement.setString(5, Database.getGson().toJson(user.getUserOrders()));

					statement.executeUpdate();

				} catch (SQLException e) {
					e.printStackTrace();
				}
			} finally {
				statement.close();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<User> getUserCache(){
		if(userCache.size() <= 0){
			fetchUsers();
		}

		return userCache.asMap().values().stream().toList();
	}


	private static void fetchUsers(){
		ArrayList<User> users = new ArrayList<>();
		userCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("select * from USERS");
					rs = statement.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(1);
						String username = rs.getString(2);
						String displayName = rs.getString(3);
						String email = rs.getString(4);
						String orderString = rs.getString(5);

						Order[] orders = orderString == null ? new Order[0] : Database.getGson().fromJson(orderString, Order[].class);

						users.add(User.builder()
							          .userId(id)
							          .userName(username)
							          .displayName(displayName)
							          .email(email)
							          .userOrders(orders)
							          .build());
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		users.forEach(user -> userCache.put(user.getUserId(), user));
	}
}