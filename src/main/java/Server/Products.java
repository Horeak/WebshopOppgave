package Server;

import Common.Objects.Product;
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

class Products {
	private static Cache<Long, Product> productCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(1, TimeUnit.MINUTES).build();

	public static Product getProductById(Long id){
		return getProductCache().stream().filter(s -> Objects.equals(s.getProductId(), id)).findFirst().orElse(null);
	}

	public static List<Product> getProducts(){
		return getProductCache();
	}

	public static void updateProduct(Product product){
		productCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("UPDATE PRODUCTS SET PRICE = ?, NAME = ?, DESCRIPTION = ?, ICON = ? WHERE ID = ?");
					statement.setDouble(1, product.getPrice());
					statement.setString(2, product.getName());
					statement.setString(3, product.getDescription());
					statement.setString(4, product.getImageURL());
					statement.setLong(5, product.getProductId());

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

	public static void deleteProduct(Long id){
		productCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("DELETE FROM PRODUCTS WHERE ID = ?");
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

	public static void addProduct(Product product){
		productCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("INSERT INTO PRODUCTS VALUES(?,?,?,?,?)");
					statement.setLong(1, product.getProductId());
					statement.setDouble(2, product.getPrice());
					statement.setString(3, product.getName());
					statement.setString(4, product.getDescription());
					statement.setString(5, product.getImageURL());

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

	private static List<Product> getProductCache(){
		if(productCache.size() <= 0){
			fetchProducts();
		}

		return productCache.asMap().values().stream().toList();
	}


	private static void fetchProducts(){
		ArrayList<Product> users = new ArrayList<>();
		productCache.invalidateAll();

		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = Database.getConnection();

			try {
				try {
					statement = con.prepareStatement("select * from PRODUCTS");
					rs = statement.executeQuery();

					while (rs.next()) {
						Long id = rs.getLong(1);
						Double price = rs.getDouble(2);
						String name = rs.getString(3);
						String description = rs.getString(4);
						String icon = rs.getString(5);

						users.add(Product.builder()
							          .productId(id)
							          .price(price)
							          .name(name)
							          .description(description)
							          .imageURL(icon)
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

		users.forEach(user -> productCache.put(user.getProductId(), user));
	}
}