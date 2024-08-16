package by.ladyka.poputka.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class PoputkaUser extends Auditable implements UserDetails {

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
