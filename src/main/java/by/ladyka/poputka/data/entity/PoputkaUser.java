package by.ladyka.poputka.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class PoputkaUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String password;
    private String name;
    private String surname;
    private LocalDate birthday;
    private String music;
    private String businessActivity;
    private String description;
    private String car;
    private long telegramId;
    private String telegramUsername;

    // Поля для модуля помощи на дороге
    private Boolean readyToHelp = false;
    private Integer helpRadius = 10;
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "trip_rating", nullable = false)
    private BigDecimal tripRating = BigDecimal.ZERO;

    private Boolean requiresModeration = false;
    private Boolean isBlocked = false;


    // AUDIT
    @Column(name = "created_datetime", nullable = false)
    @CreatedDate
    private Long createdDatetime;

    @Column(name = "modified_datetime", nullable = false)
    @LastModifiedDate
    private Long modifiedDatetime;

    @Column(name = "version", nullable = false)
    @Version
    private short version;

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> "USER");
    }

    public String getEmail() {
        return getUsername();
    }

    public void setEmail(String email) {
        setUsername(email);
    }

    public String getUUID() {
        return String.valueOf(getId());
    }
}
