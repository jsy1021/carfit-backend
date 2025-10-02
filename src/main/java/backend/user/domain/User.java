package backend.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

@Table(name="users")
@NoArgsConstructor
@Getter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id",updatable = false)
    private Long id;

    @Column(name="user_id", updatable = false,unique = true)
    private String userId; //아이디

    @Column(name="password")
    private String password; //비밀번호

    @Column(name="name")
    private String name; //이름

    @Column(name="email")
    private String email; //이메일

    @Column(name="address")
    private String address; //주소

    @Column(name="birth_date")
    private LocalDate birthDate; //생년월일

    @Column(name="agreed_terms")
    private boolean termsAgreed;

    @Column(name="agreed_privacy")
    private boolean privacyAgreed;

    @Column(name="agreed_marketing")
    private boolean marketingAgreed;

    @Column(name="created_at")
    private Date createdAt; //생성시점

    @Column(name="role")
    private String role; //user로 저장

    public User(String userId, String password, String name, String email, String address,
                LocalDate birthDate, boolean agreedTerms, boolean privacyAgreed, boolean marketingAgreed) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.address = address;
        this.birthDate = birthDate;
        this.termsAgreed = agreedTerms;
        this.privacyAgreed = privacyAgreed;
        this.marketingAgreed = marketingAgreed;
        this.createdAt = new Date();
        this.role = "USER";
    }
}

