package kitchenpos.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.List;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderTable;
import kitchenpos.order.domain.OrderTableRepository;
import kitchenpos.order.domain.TableGroup;
import kitchenpos.order.presentation.dto.ChangeOrderStatusRequest;
import kitchenpos.order.presentation.dto.CreateTableGroupRequest;
import kitchenpos.order.presentation.dto.OrderTableRequest;
import kitchenpos.support.TestSupporter;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(ReplaceUnderscores.class)
@Sql("/truncate.sql")
@SpringBootTest
class TableGroupServiceTest {

    @Autowired
    private TableGroupService tableGroupService;

    @Autowired
    private TestSupporter testSupporter;

    @Autowired
    private OrderTableRepository orderTableRepository;

    @Autowired
    private OrderService orderService;

    @Test
    void 테이블_그룹을_생성한다() {
        // given
        final OrderTable orderTable1 = testSupporter.createOrderTable(true);
        final OrderTable orderTable2 = testSupporter.createOrderTable(true);
        final OrderTableRequest orderTableRequest1 = new OrderTableRequest(orderTable1.getId());
        final OrderTableRequest orderTableRequest2 = new OrderTableRequest(orderTable2.getId());
        final CreateTableGroupRequest createTableGroupRequest = new CreateTableGroupRequest(List.of(orderTableRequest1,
                                                                                                    orderTableRequest2));

        // when
        final TableGroup tableGroup = tableGroupService.create(createTableGroupRequest);

        // then
        assertThat(tableGroup).isNotNull();
    }

    @Test
    void 테이블_그룹을_해제한다() {
        // given
        final TableGroup tableGroup = testSupporter.createTableGroup();

        // when
        tableGroupService.ungroup(tableGroup.getId());

        // then
        final List<OrderTable> orderTables = orderTableRepository.findAllByTableGroupId(tableGroup.getId());
        final boolean actual = orderTables.stream()
                                          .allMatch(orderTable -> !orderTable.isGrouped() && !orderTable.isEmpty());
        assertThat(actual).isTrue();
    }

    @Test
    void 테이블_그룹을_생성할_때_주문_테이블이_2_테이블_미만이라면_예외가_발생한다() {
        // given
        final OrderTable orderTable = testSupporter.createOrderTable();
        final OrderTableRequest orderTableRequest = new OrderTableRequest(orderTable.getId());
        final CreateTableGroupRequest createTableGroupRequest = new CreateTableGroupRequest(List.of(orderTableRequest));

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(createTableGroupRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 테이블_그룹을_생성할_때_주문_테이블들_중_하나라도_비어있지_않다면_예외가_발생한다() {
        // given
        final OrderTable orderTable1 = testSupporter.createOrderTable(true);
        final OrderTable orderTable2 = testSupporter.createOrderTable(false);
        final OrderTableRequest orderTableRequest1 = new OrderTableRequest(orderTable1.getId());
        final OrderTableRequest orderTableRequest2 = new OrderTableRequest(orderTable2.getId());
        final CreateTableGroupRequest createTableGroupRequest = new CreateTableGroupRequest(List.of(orderTableRequest1,
                                                                                                    orderTableRequest2));

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(createTableGroupRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "주문 상태가 {0}일때 예외가 발생한다.")
    @ValueSource(strings = {"COOKING", "MEAL"})
    void 테이블_그룹을_해제할_때_주문의_상태가_하나라도_COMPLETION_이_아니라면_예외가_발생한다(final String orderStatus) {
        // given
        final TableGroup tableGroup = testSupporter.createTableGroup();

        final List<OrderTable> orderTables = tableGroup.getOrderTables();

        final Order order1 = testSupporter.createOrder(orderTables.get(0));
        final Order order2 = testSupporter.createOrder(orderTables.get(1));

        final ChangeOrderStatusRequest changeOrderStatusRequest1 = new ChangeOrderStatusRequest("COMPLETION");
        orderService.changeOrderStatus(order1.getId(), changeOrderStatusRequest1);

        final ChangeOrderStatusRequest changeOrderStatusRequest2 = new ChangeOrderStatusRequest(orderStatus);
        orderService.changeOrderStatus(order2.getId(), changeOrderStatusRequest2);

        // when & then
        assertThatThrownBy(() -> tableGroupService.ungroup(tableGroup.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
