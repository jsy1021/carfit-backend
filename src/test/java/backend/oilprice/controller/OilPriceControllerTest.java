package backend.oilprice.controller;

import backend.auth.security.JwtAuthenticationFilter;
import backend.auth.security.JwtUtil;
import backend.common.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = OilPriceController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class OilPriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate; // 외부 API 호출 Mocking


    @MockBean
    private backend.auth.service.CustomUserDetailsService customUserDetailsService;


    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Autowired
    private ObjectMapper objectMapper; // 실제 ObjectMapper 사용

    @Autowired
    private OilPriceController oilPriceController;

    @Test
    @DisplayName("✅ 정상적으로 유가 정보 조회 성공")
    void testGetAvgSigunPrice_Success() throws Exception {
        // given
        String mockResponse = """
                {
                    "RESULT": {
                        "OIL": [
                            {"SIGUNCD": "0101", "SIGUNNM": "서울 종로구", "PRICE": 1750},
                            {"SIGUNCD": "0102", "SIGUNNM": "서울 중구", "PRICE": 1745}
                        ]
                    }
                }
                """;

        Mockito.when(restTemplate.getForObject(anyString(), Mockito.eq(String.class)))
                .thenReturn(mockResponse);

        ReflectionTestUtils.setField(oilPriceController, "apiKey", "TEST_KEY");

        // when & then
        mockMvc.perform(get("/api/oil/avg-sigun-price")
                        .param("sido", "01")
                        .param("sigun", "0101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sido").value("01"))
                .andExpect(jsonPath("$.oilPrices").isArray())
                .andExpect(jsonPath("$.totalCount").value(2));
    }

    @Test
    @DisplayName("⚠️ Opinet API 응답이 비어있는 경우")
    void testGetAvgSigunPrice_EmptyResponse() throws Exception {
        Mockito.when(restTemplate.getForObject(anyString(), Mockito.eq(String.class)))
                .thenReturn("");

        ReflectionTestUtils.setField(oilPriceController, "apiKey", "TEST_KEY");

        mockMvc.perform(get("/api/oil/avg-sigun-price")
                        .param("sido", "01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("API 응답이 비어있습니다."));
    }

    @Test
    @DisplayName("💥 예외 발생 시 500 반환")
    void testGetAvgSigunPrice_Exception() throws Exception {
        Mockito.when(restTemplate.getForObject(anyString(), Mockito.eq(String.class)))
                .thenThrow(new RuntimeException("API 호출 실패"));

        ReflectionTestUtils.setField(oilPriceController, "apiKey", "TEST_KEY");

        mockMvc.perform(get("/api/oil/avg-sigun-price")
                        .param("sido", "01"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("유가 정보 조회 실패"));
    }
}
