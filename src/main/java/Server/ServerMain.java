package Server;

import Common.Objects.Order;
import Common.Objects.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;

public class ServerMain{
	public static void main(String[] args) throws Exception{
		Server server = new Server(8000);
		server.setHandler(new AbstractHandler(){
			@Override
			public void handle(String s, Request baseRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException{
				System.out.println(baseRequest.getRequestURI());

				if(baseRequest.getRequestURI().equals("/products")){
					String response = Database.getGson().toJson(Products.getProducts());

					httpResponse.setContentType("application/json");
					httpResponse.setStatus(HttpServletResponse.SC_OK);
					baseRequest.setHandled(true);
					httpResponse.getWriter().write(response);

				}else if(baseRequest.getRequestURI().equals("/update_user")){
					String request = IOUtils.toString(httpRequest.getReader());

					try{
						User user = Database.getGson().fromJson(request, User.class);
						Users.updateUser(user);
						System.out.println("Update user: " + user);
					}catch(Exception e){
						e.printStackTrace();
					}

					httpResponse.setContentType("text/plain");
					httpResponse.setStatus(HttpServletResponse.SC_OK);
					baseRequest.setHandled(true);
					httpResponse.getWriter().write("SUCCESS");

				}else if(baseRequest.getRequestURI().equals("/add_user")){
					String request = IOUtils.toString(httpRequest.getReader());
					try{
						User user = Database.getGson().fromJson(request, User.class);
						Users.addUser(user);
						System.out.println("Add user: " + user);
					}catch(Exception e){
						e.printStackTrace();
					}

					httpResponse.setContentType("text/plain");
					httpResponse.setStatus(HttpServletResponse.SC_OK);
					baseRequest.setHandled(true);
					httpResponse.getWriter().write("SUCCESS");

				}else if(baseRequest.getRequestURI().equals("/order")){
					String request = IOUtils.toString(httpRequest.getReader());
					try{
						Order order = Database.getGson().fromJson(request, Order.class);
						Orders.addOrder(order);
						System.out.println("Add order: " + order);
					}catch(Exception e){
						e.printStackTrace();
					}

					httpResponse.setContentType("text/plain");
					httpResponse.setStatus(HttpServletResponse.SC_OK);
					baseRequest.setHandled(true);
					httpResponse.getWriter().write("SUCCESS");
				}
			}
		});

		server.start();
		server.join();
	}
}