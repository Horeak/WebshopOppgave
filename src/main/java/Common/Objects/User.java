package Common.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
public class User {
	private Long userId;
	private String userName, displayName;
	private String email;
	private Order[] userOrders;
}