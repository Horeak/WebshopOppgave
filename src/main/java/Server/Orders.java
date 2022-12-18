package Server;

import Common.Objects.Order;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.reflect.TypeToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

class Orders{
	private static Cache<Long, Order> orderCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(1, TimeUnit.MINUTES).build();

	public static Order getOrderById(Long id){
		return getOrderCache().stream().filter(s -> Objects.equals(s.getOrderId(), id)).findFirst().orElse(null);
	}

	public static void addOrder(Order product){
		orderCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("INSERT INTO ORDERS VALUES(?,?,?,?)");
					statement.setLong(1, product.getOrderId());
					statement.setLong(2, product.getUserId());
					statement.setString(3, Database.getGson().toJson(product.getProducts()));
					statement.setLong(4, product.getOrderDate());

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

	public static void updateOrder(Order order){
		orderCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("UPDATE ORDERS SET USER_ID = ?, PRODUCTS = ?, ORDER_TIME = ? WHERE ID = ?");
					statement.setDouble(1, order.getUserId());
					statement.setString(2, Database.getGson().toJson(order.getProducts()));
					statement.setLong(3, order.getOrderDate());
					statement.setLong(4, order.getOrderId());

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

	private static List<Order> getOrderCache(){
		if(orderCache.size() <= 0){
			fetchOrders();
		}

		return orderCache.asMap().values().stream().toList();
	}

	public static void deleteOrder(Long id){
		orderCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("DELETE FROM ORDERS WHERE ID = ?");
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

	private static void fetchOrders(){
		ArrayList<Order> users = new ArrayList<>();
		orderCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("select * from ORDERS");
					rs = statement.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(1);
						Long userId = rs.getLong(2);

						String productsString = rs.getString(3);
						Map<Long, Integer> products = productsString == null ? new HashMap<>() : Database.getGson().fromJson(productsString, new TypeToken<Map<Long, Integer>>(){}.getType());

						Long orderDate = rs.getLong(4);


						users.add(Order.builder()
							          .orderId(id)
							          .userId(userId)
							          .products(products)
							          .orderDate(orderDate)
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

		users.forEach(user -> orderCache.put(user.getOrderId(), user));
	}
}