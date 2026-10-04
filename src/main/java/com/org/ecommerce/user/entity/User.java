package com.org.ecommerce.user.entity;

import com.org.ecommerce.common.enums.Role;
import com.org.ecommerce.order.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "users") // "user" is a keyword wrt to PG DB. So changing the table name to "users".
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user") // WE ARE SAYING ORDER ENTITY ALREADY HAS USER FOREIGN KEY. SO USE IT.
    private List<Order> orders;
}
