package Common.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@ToString
@Getter
@Setter
@Builder
public class Order{
	private Long orderId;
	private Long userId;
	private Long orderDate;
	private Map<Long, Integer> products;
}