package backend.location.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDefaultLocationResponse {

    private Long id;
    private Long userId;
    private String address;
    private Double latitude;
    private Double longitude;
}
