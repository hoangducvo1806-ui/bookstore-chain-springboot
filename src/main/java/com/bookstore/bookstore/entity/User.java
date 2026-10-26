package com.bookstore.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String avatarUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = false; // Phải kích hoạt qua OTP Email mới = true

    @Builder.Default
    @Column(nullable = false)
    private Boolean locked = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean hasRole(String roleName) {
        if (roles == null) return false;
        return roles.stream().anyMatch(r -> r.getName() != null && r.getName().name().equalsIgnoreCase(roleName));
    }

    public boolean isVendor() { return hasRole("VENDOR"); }
    public boolean isAdmin() { return hasRole("ADMIN"); }
    public boolean isManager() { return hasRole("MANAGER"); }
    public boolean isShipper() { return hasRole("SHIPPER"); }
}
