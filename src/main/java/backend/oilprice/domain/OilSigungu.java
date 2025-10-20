package backend.oilprice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "oil_avg_sigungu")
@Getter
@Setter
public class OilSigungu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String sigunCd;
    private String sigunNm;
    private String prodcd;
    private Double price;
    private Double diff;
}
