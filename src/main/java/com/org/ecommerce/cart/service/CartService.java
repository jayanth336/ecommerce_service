package com.org.ecommerce.cart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.ecommerce.cart.dto.CartRequestDto;
import com.org.ecommerce.cart.dto.CartResponseDto;
import com.org.ecommerce.cart.entity.Cart;
import com.org.ecommerce.cart.exception.CartItemNotFoundException;
import com.org.ecommerce.cart.exception.CartNotFoundException;
import com.org.ecommerce.cart.exception.EmptyCartException;
import com.org.ecommerce.cart.exception.InvalidQuantityException;
import com.org.ecommerce.cart.repository.CartRepository;
import com.org.ecommerce.cartItem.dto.CartItemResponseDto;
import com.org.ecommerce.cartItem.entity.CartItem;
import com.org.ecommerce.cartItem.repository.CartItemRepository;
import com.org.ecommerce.common.enums.OrderStatus;
import com.org.ecommerce.common.event.OrderCreatedEvent;
import com.org.ecommerce.common.event.OrderItemEvent;
import com.org.ecommerce.common.kafka.OrderEventProducer;
import com.org.ecommerce.common.outbox.OutboxEventRepository;
import com.org.ecommerce.common.outbox.OutboxEvent;
import com.org.ecommerce.order.dto.OrderResponseDto;
import com.org.ecommerce.order.entity.Order;
import com.org.ecommerce.order.repository.OrderRepository;
import com.org.ecommerce.orderItem.dto.OrderItemResponseDto;
import com.org.ecommerce.orderItem.entity.OrderItem;
import com.org.ecommerce.payment.service.PaymentService;
import com.org.ecommerce.product.entity.Product;
import com.org.ecommerce.product.exception.ProductNotFoundException;
import com.org.ecommerce.product.repository.ProductRepository;
import com.org.ecommerce.user.entity.User;
import com.org.ecommerce.user.exception.UserNotFoundException;
import com.org.ecommerce.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OutboxEventRepository outBoxEventRepository;
    private final ObjectMapper objectMapper;

    public CartService(CartRepository cartRepository, UserRepository userRepository,
                       ProductRepository productRepository, CartItemRepository cartItemRepository,
                       OrderRepository orderRepository, OutboxEventRepository outBoxEventRepository, ObjectMapper objectMapper) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.outBoxEventRepository = outBoxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CartResponseDto addToCart(CartRequestDto requestDto) {
        // GET THE AUTHENTICATED USER
        User user = getAuthenticatedUser();

        // GET THE EXISTING CART (OR) CREATE A NEW CART FOR THE USER
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // GET THE PRODUCT
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(requestDto.getProductId()));

        // CHECK IF THE PRODUCT IS ALREADY PRESENT IN THE CART AS PART OF A CART_ITEM
        CartItem cartItem;
        Optional<CartItem> optionalCartItem = cartItemRepository.findByCartAndProduct(cart, product);
        if (optionalCartItem.isPresent()) {
            cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + requestDto.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(requestDto.getQuantity());
        }

        // SAVE THE CART_ITEM AND GET THE UPDATED CART
        // AS PER OUR CODE LOGIC, WE NEED TO SAVE IN REPO FOR ELSE CASE, BUT FOR IF CASE IT'S NOT REQUIRED
        // BECAUSE CART_ITEM_REPO IS ALREADY PART OF JPA MANAGED ENTITY
        cartItemRepository.save(cartItem);
        cart = cartRepository.findByUser(user).orElseThrow();
        return mapToCartResponseDto(cart);
    }

    public CartResponseDto getCart() {
        // GET THE AUTHENTICATED USER
        User user = getAuthenticatedUser();

        // GET THE CART
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new CartNotFoundException(user.getId()));

        return mapToCartResponseDto(cart);
    }

    @Transactional
    public OrderResponseDto processOrder() {
        // GET THE AUTHENTICATED USER
        User user = getAuthenticatedUser();

        // GET THE CART
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new CartNotFoundException(user.getId()));

        //CHECK IF THE CART IS EMPTY
        if(cart.getCartItems().isEmpty()) throw new EmptyCartException(user.getId());

        // CREATE ORDER - MAP THE CART DATA TO ORDER AND CALCULATE TOTAL AMOUNT
        Order order = new Order();
        order.setUser(user);

        BigDecimal amount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderItemEvent> eventItems = new ArrayList<>();

        List<CartItem> cartItems = cart.getCartItems();
        for(CartItem item : cartItems) {
            Product product = item.getProduct();
            BigDecimal price =  product.getPrice();
            int quantity = item.getQuantity();

            orderItems.add(new OrderItem(order, product, quantity, price));

            // CALCULATE THE AMOUNT FOR THE PRODUCT
            amount = amount.add(price.multiply(BigDecimal.valueOf(quantity)));

            // ADD TO THE EVENT LIST - TO UPDATE THE INVENTORY, WE JUST NEED THE PRODUCT AND THE QUANTITY
            eventItems.add(new OrderItemEvent(product.getId(), quantity));
        }

        order.setAmount(amount);
        order.setOrderItems(orderItems);
        order.setStatus(OrderStatus.PENDING);

        // SAVE THE ORDER - WE NEED TO SAVE THIS BECAUSE ORDER IS NOT PART OF JPA MANAGED ENTITY YET
        orderRepository.save(order);

        // CREATE THE EVENT - TO UPDATE THE INVENTORY
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(order.getId(), eventItems);

        // INSERT THE EVENT IN OUTBOX TABLE
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType("ORDER_CREATED");
        try {
            outboxEvent.setPayload(objectMapper.writeValueAsString(orderCreatedEvent));
        } catch (JsonProcessingException e) {
            // THIS EXCEPTION ROLLS BACK THE PREVIOUS OPERATIONS WE PERFORMED - BECAUSE THIS METHOD HAS @TRANSACTIONAL
            throw new RuntimeException("Failed to serialize orderCreatedEvent", e);
        }
        outboxEvent.setCreatedAt(LocalDateTime.now());
        outboxEvent.setPublished(false);

        // SAVE THE OUTBOX EVENT
        outBoxEventRepository.save(outboxEvent);

        // CLEAR CART
        cart.getCartItems().clear();

        return mapToOrderResponseDto(order);
    }

    @Transactional
    public CartResponseDto updateQuantity(Long cartItemId, int quantity) {
        // GET THE USER
        User user =  getAuthenticatedUser();

        if(quantity <= 0) {
            throw new InvalidQuantityException();
        }

        // GET THE CART_ITEM - THEN THE CART - THEN THE USER
        CartItem cartItem = cartItemRepository.findByIdAndCartUserId(cartItemId, user.getId())
                .orElseThrow(() -> new CartItemNotFoundException(cartItemId));
        cartItem.setQuantity(quantity);

        // NO NEED OF JPA SAVE METHOD CALL - BECAUSE FROM THE LINES 2 AND 3,
        //      CART AND CART_ITEM HAVE ALREADY BECOME PART OF JPA MANAGED ENTITY
        // SO THEY ARE TAKEN CARE DURING JPA'S DIRTY CHECKING

        return mapToCartResponseDto(cartItem.getCart());
    }

    @Transactional
    public void  removeCartItem(Long cartItemId) {
        // GET THE USER
        User user =  getAuthenticatedUser();

        // GET THE USER'S CART
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new CartNotFoundException(user.getId()));

        // GET THE CART_ITEMS
        List<CartItem> cartItems = cart.getCartItems();

        // ITERATE THE CART_ITEMS TO FIND THE REQUIRED CART_ITEM
        // WE ARE MODIFYING THE LIST'S STRUCTURE WHILE ITERATING - SO USE ITERATOR INSTEAD OF FOR-EACH
        Iterator<CartItem> iterator = cartItems.iterator();
        while(iterator.hasNext()) {
            CartItem item = iterator.next();
            if(item.getId().equals(cartItemId)) {
                iterator.remove();
                return;
            }
        }

        throw new CartItemNotFoundException(cartItemId);
        /**
         * USER AND CART ARE PART OF MANAGED ENTITY.
         * ORPHAN_REMOVAL PLAYS A ROLE HERE.
         * IT IS NOTHING BUT AUTOMATICALLY REMOVING CHILD ENTITY FROM DATABASE WHEN IT IS REMOVED FROM PARENT'S COLLECTION.
         * SO WE REMOVE CART_ITEM FROM LIST OF CART_ITEMS - GIVEN THAT LIST OF CART_ITEMS IS A COLLECTION PRESENT IN CART.
         * SO AUTOMATICALLY THAT PARTICULAR CART_ITEM GETS REMOVED FROM DATABASE - CART ENTITY IS ALSO UPDATED.
         * SO NO SEPARATE REPO SAVING OR DELETING IS REQUIRED.
         */
    }

    @Transactional
    public void deleteCart() {
        // GET THE USER
        User user = getAuthenticatedUser();

        // GET USER'S CART
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new CartNotFoundException(user.getId()));

        // GET CART_ITEMS
        List<CartItem> cartItems = cart.getCartItems();

        // CLEAR THE CART_ITEMS
        cartItems.clear();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    private OrderResponseDto mapToOrderResponseDto(Order order) {
        OrderResponseDto responseDto = new OrderResponseDto();

        responseDto.setUser_id(order.getUser().getId());
        responseDto.setOrder_id(order.getId());
        responseDto.setAmount(order.getAmount());
        responseDto.setStatus(order.getStatus());

        List<OrderItemResponseDto> ordersItems = order.getOrderItems()
                .stream()
                .map(orderItem -> mapToOrderItemResponseDto(orderItem))
                .toList();

        responseDto.setOrderItems(ordersItems);

        return responseDto;
    }

    private OrderItemResponseDto mapToOrderItemResponseDto(OrderItem orderItem) {
        OrderItemResponseDto responseDto = new OrderItemResponseDto();
        responseDto.setProductId(orderItem.getProduct().getId());
        responseDto.setProductName(orderItem.getProduct().getProductName());
        responseDto.setQuantity(orderItem.getQuantity());
        responseDto.setPriceAtPurchase(orderItem.getPriceAtPurchase());
        return responseDto;
    }

    private CartResponseDto mapToCartResponseDto(Cart cart) {
        CartResponseDto dto = new CartResponseDto();

        dto.setCartId(cart.getId());
        dto.setUserId(cart.getUser().getId());

        List<CartItemResponseDto> items = cart.getCartItems()
                .stream()
                .map(item -> mapToCartItemResponseDto(item))
                .toList();

        dto.setCartItems(items);

        return dto;
    }

    private CartItemResponseDto mapToCartItemResponseDto(CartItem item) {
        CartItemResponseDto dto = new CartItemResponseDto();

        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getProduct().getPrice());

        return dto;
    }
}
