package Client;

import Common.Objects.Order;
import Common.Objects.Product;
import Common.Objects.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ClientMain {
	private static final String url = "http://localhost:8000";
	private static final HttpClient httpClient = HttpClient.newHttpClient();

	private static final Gson gson = new GsonBuilder().serializeNulls().create();
	public static ArrayList<Product> products = new ArrayList<>();

	public static void main(String[] args) throws IOException, InterruptedException{
		fetchProducts();
		Random random = new Random();

		System.out.println("Found " + products.size() + " products.");
		System.out.println("Press enter to continue...");
		System.in.read();

		User user = User.builder()
			.userId(random.nextLong())
			.userName("user")
			.displayName("Test user")
			.email("test@example.com")
			.build();

		HttpRequest addUserRequest = HttpRequest.newBuilder()
			.uri(URI.create(url + "/add_user"))
			.method("GET", BodyPublishers.ofString(gson.toJson(user)))
			.build();
		httpClient.sendAsync(addUserRequest, BodyHandlers.ofString());

		System.out.println("User added");
		System.out.println("Press enter to continue...");
		System.in.read();

		HashMap<Long, Integer> orderedProducts = new HashMap<>();
		Long product = products.get(random.nextInt(products.size())).getProductId();
		orderedProducts.put(product, 1 + random.nextInt(10));

		Order order = Order.builder()
			.orderId(random.nextLong(0, Integer.MAX_VALUE)) //Int value to keep user id positive
			.userId(user.getUserId())
			.products(orderedProducts)
			.orderDate(System.currentTimeMillis())
			.build();

		HttpRequest addOrderRequest = HttpRequest.newBuilder()
			.uri(URI.create(url + "/order"))
			.method("GET", BodyPublishers.ofString(gson.toJson(order)))
			.build();
		httpClient.sendAsync(addOrderRequest, BodyHandlers.ofString());

		System.out.println("Order added");
		System.out.println("Press enter to continue...");
		System.in.read();

		user.setUserOrders(new Order[]{order});

		HttpRequest updateUserRequest = HttpRequest.newBuilder()
			.uri(URI.create(url + "/update_user"))
			.method("GET", BodyPublishers.ofString(gson.toJson(user)))
			.build();
		httpClient.sendAsync(updateUserRequest, BodyHandlers.ofString());

		System.out.println("User updated");
		System.out.println("Press enter to continue...");
		System.in.read();
	}

	public static void fetchProducts(){
		products.clear();

		HttpRequest request = HttpRequest.newBuilder()
			.timeout(Duration.ofMinutes(1))
			.uri(URI.create(url + "/products"))
			.build();

		try{
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			if(response.statusCode() == 200){
				List<Product> productList = gson.fromJson(response.body(), new TypeToken<List<Product>>(){}.getType());

				if(productList != null){
					products.addAll(productList);
				}
			}
		}catch(IOException | InterruptedException e){
			throw new RuntimeException(e);
		}
	}
}