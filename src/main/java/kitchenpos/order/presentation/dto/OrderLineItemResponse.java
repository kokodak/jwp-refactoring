package kitchenpos.order.presentation.dto;

import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.menu.presentation.dto.MenuResponse;
import kitchenpos.order.domain.OrderLineItem;

public class OrderLineItemResponse {

    private final Long id;

    private final MenuResponse menu;

    private final long quantity;

    public OrderLineItemResponse(final Long id,
                                 final MenuResponse menu,
                                 final long quantity) {
        this.id = id;
        this.menu = menu;
        this.quantity = quantity;
    }

    public static OrderLineItemResponse from(final OrderLineItem orderLineItem) {
        return new OrderLineItemResponse(orderLineItem.getId(),
                                         MenuResponse.from(orderLineItem.getMenu()),
                                         orderLineItem.getQuantity());
    }

    public static List<OrderLineItemResponse> convertToList(final List<OrderLineItem> orderLineItems) {
        return orderLineItems.stream()
                             .map(OrderLineItemResponse::from)
                             .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public MenuResponse getMenu() {
        return menu;
    }

    public long getQuantity() {
        return quantity;
    }
}