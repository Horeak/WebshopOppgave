package Common.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
public class Product {
	private Long productId;
	private String name, description;
	private String imageURL;
	private double price;
}