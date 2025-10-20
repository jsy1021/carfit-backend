package backend.oilprice.service;

import backend.oilprice.repository.OilSidoRepository;
import backend.oilprice.repository.OilSigunguRepository;
import backend.oilprice.service.OilDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OilDataServiceTest {

    @InjectMocks
    private OilDataService oilDataService;

    @Mock
    private OilSidoRepository sidoRepository;

    @Mock
    private OilSigunguRepository sigunguRepository;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // @Value apiKey 주입
        ReflectionTestUtils.setField(oilDataService, "apiKey", "TEST_KEY");
    }

    @Test
    @DisplayName("전국/시도 유가 데이터 정상 저장")
    void testSaveSidoAveragePrices_Success() throws Exception {
        String mockResponse = """
            {
              "RESULT": {
                "OIL": [
                  {"SIDOCD": "01", "SIDONM": "서울", "PRODCD": "B027", "PRICE": 1750, "DIFF": 10},
                  {"SIDOCD": "02", "SIDONM": "부산", "PRODCD": "B027", "PRICE": 1760, "DIFF": -5}
                ]
              }
            }
        """;

        Mockito.when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockResponse);

        Mockito.when(sidoRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0)); // 그대로 반환

        // fetchAndSaveOilData() 내부에서 saveSidoAveragePrices 호출
        oilDataService.fetchAndSaveOilData();

        // 저장 호출 검증
        Mockito.verify(sidoRepository, Mockito.atLeastOnce()).saveAll(anyList());
    }

    @Test
    @DisplayName("시군구 API HTML 응답 시 로그만 기록하고 저장하지 않음")
    void testSaveSigunguAveragePrices_HtmlResponse() throws Exception {
        String htmlResponse = "<html><body>API Error</body></html>";

        Mockito.when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(htmlResponse);

        // 시군구 데이터 저장 시도
        oilDataService.fetchAndSaveOilData();

        // DB 저장 호출되지 않음
        Mockito.verify(sigunguRepository, Mockito.never()).saveAll(anyList());
    }

    @Test
    @DisplayName("예외 발생 시 로그만 기록")
    void testFetchAndSaveOilData_ExceptionHandling() throws Exception {
        Mockito.when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API 호출 실패"));

        oilDataService.fetchAndSaveOilData();

        // 저장 호출되지 않음
        Mockito.verify(sidoRepository, Mockito.never()).saveAll(anyList());
        Mockito.verify(sigunguRepository, Mockito.never()).saveAll(anyList());
    }
}