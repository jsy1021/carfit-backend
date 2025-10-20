package backend.oilprice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "oil_avg_sido")
@Getter
@Setter
public class OilSido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String sidoCd;
    private String sidoNm;
    private String prodcd;
    private Double price;
    private Double diff;
}


