package jpabook.jpashop;

import jpabook.jpashop.domain.item.*;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OrderServiceTest {

    @PersistenceContext
    EntityManager em;
    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {

        //given
        Member member = createMember();
        Book book = createBook("데미안", 10000, 10);

        //when
        int orderCount=2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.ORDER, getOrder.getStatus());
        assertEquals(1,getOrder.getOrderItems().size());
        assertEquals(10000*orderCount, getOrder.getTotalPrice());
        assertEquals(10, book.getStockQuantity());
    }

    // Junit5에서는 expected가 없어진 대신, 람다로 해당 클래스를 넣고 예외처리를 직접 돌려줘야한다.
    @Test
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        Item item = createBook("데미안", 10000, 10);

        int orderCount = 10;
        //when
        // orderService.order(member.getId(), item.getId(), orderCount)

        //then
        assertThrows(NotEnoughStockException.class, ()-> {
            orderService.order(member.getId(), item.getId(), orderCount);
        });
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Book item = createBook("JPA", 10000, 10);
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals(10, item.getStockQuantity());
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원");
        member.setAddress(new Address("서울", "강가", "01234"));
        em.persist(member);
        return member;
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }



}
