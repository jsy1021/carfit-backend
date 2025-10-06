package backend.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 50, message = "제목은 1~50자 사이여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String content;
}
