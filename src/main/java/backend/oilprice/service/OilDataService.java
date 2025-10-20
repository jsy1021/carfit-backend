package backend.oilprice.service;

import backend.oilprice.domain.OilSido;
import backend.oilprice.domain.OilSigungu;
import backend.oilprice.dto.SidoApiResponse;
import backend.oilprice.dto.SigunguApiResponse;
import backend.oilprice.repository.OilSidoRepository;
import backend.oilprice.repository.OilSigunguRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import io.micrometer.core.annotation.Timed;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OilDataService {

    private final OilSidoRepository sidoRepository;
    private final OilSigunguRepository sigunguRepository;
    private final RestTemplate restTemplate;

    @Value("${opinet.api.key}")
    private String apiKey;

    // 전국 시도 코드 목록 (오피넷 표준 코드)
    private static final List<String> SIDO_CODES = List.of(
            "01", "02", "03", "04", "05", "06", "07", "08", "09",
            "10", "11", "14", "15", "16", "17", "18", "19"
    );

    /**
     * 전국/시도별 & 시군구별 유가 데이터 수집 및 저장
     */
    @Timed(value = "oil.data.collection", description = "Time taken to collect and save oil data")
    public void fetchAndSaveOilData() {
        LocalDate today = LocalDate.now();
        long startTime = System.currentTimeMillis();

        log.info("유가 데이터 수집 시작: {}", today);

        // 전국/시도별 평균 가격 저장
        long sidoStartTime = System.currentTimeMillis();
        saveSidoAveragePrices(today);
        long sidoEndTime = System.currentTimeMillis();
        log.info("시도별 데이터 저장 시간: {}ms", sidoEndTime - sidoStartTime);

        // 시군구별 평균 가격 저장
        long sigunguStartTime = System.currentTimeMillis();
        for (String sidoCode : SIDO_CODES) {
            saveSigunguAveragePrices(today, sidoCode);
        }
        long sigunguEndTime = System.currentTimeMillis();
        log.info("시군구별 데이터 저장 시간: {}ms", sigunguEndTime - sigunguStartTime);

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("유가 데이터 저장 완료: {} (총 소요시간: {}ms)", today, totalTime);
    }

    /**
     * 전국/시도별 평균가격 수집 및 저장
     */
    private void saveSidoAveragePrices(LocalDate today) {
        try {
            String sidoUrl = "http://www.opinet.co.kr/api/avgSidoPrice.do?out=json&code=" + apiKey;
            log.info("전국/시도 API 요청: {}", sidoUrl);

            //먼저 원본 응답을 String으로 받아서 확인
            String rawResponse = restTemplate.getForObject(sidoUrl, String.class);
            log.info("전국/시도 API 원본 응답: {}", rawResponse);

            //HTML 응답인지 확인 (에러 페이지일 가능성)
            if (rawResponse.contains("<html>") || rawResponse.contains("<!DOCTYPE")) {
                log.error("API가 HTML을 반환했습니다. API 키나 URL을 확인해주세요.");
                log.error("응답 내용: {}", rawResponse);
                return;
            }

            //JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            SidoApiResponse response = mapper.readValue(rawResponse, SidoApiResponse.class);

            if (response == null || response.getResult() == null || response.getResult().getOil() == null) {
                log.warn("전국/시도별 API 응답이 비어있습니다.");
                return;
            }

            List<SidoApiResponse.SidoOilData> oilDataList = response.getResult().getOil();
            
            // 배치 처리를 위한 엔티티 리스트 생성
            List<OilSido> entities = new ArrayList<>();
            for (SidoApiResponse.SidoOilData oilData : oilDataList) {
                try {
                    OilSido entity = new OilSido();
                    entity.setDate(today);
                    entity.setSidoCd(oilData.getSidoCd());
                    entity.setSidoNm(oilData.getSidoNm());
                    entity.setProdcd(oilData.getProdcd());
                    entity.setPrice(oilData.getPrice());
                    entity.setDiff(oilData.getDiff());
                    entities.add(entity);
                } catch (Exception e) {
                    log.error("시도 데이터 변환 실패 (sidoCd={}): {}", oilData.getSidoCd(), e.getMessage());
                }
            }

            // 배치로 저장
            if (!entities.isEmpty()) {
                long dbStartTime = System.currentTimeMillis();
                sidoRepository.saveAll(entities);
                long dbEndTime = System.currentTimeMillis();
                log.info(" 전국/시도별 유가 데이터 저장 완료 ({}건, DB저장시간: {}ms)", 
                        entities.size(), dbEndTime - dbStartTime);
            }

        } catch (HttpClientErrorException e) {
            log.error("전국/시도별 API 클라이언트 에러 ({}): {}", e.getStatusCode(), e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("전국/시도별 API 서버 에러 ({}): {}", e.getStatusCode(), e.getMessage());
        } catch (RestClientException e) {
            log.error("전국/시도별 API 네트워크 에러: {}", e.getMessage());
        } catch (Exception e) {
            log.error("전국/시도별 유가 데이터 저장 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 특정 시도의 시군구별 평균가격 수집 및 저장
     */
    private void saveSigunguAveragePrices(LocalDate today, String sidoCode) {
        try {
            String sigunguUrl = String.format(
                    "http://www.opinet.co.kr/api/avgSigunPrice.do?out=json&sido=%s&code=%s",
                    sidoCode, apiKey
            );
            log.info("시군구 API 요청 (sido={}): {}", sidoCode, sigunguUrl);

            //먼저 원본 응답을 String으로 받아서 확인
            String rawResponse = restTemplate.getForObject(sigunguUrl, String.class);
            log.info("시군구 API 원본 응답 (sido={}): {}", sidoCode, rawResponse);

            // HTML 응답인지 확인 (에러 페이지일 가능성)
            if (rawResponse.contains("<html>") || rawResponse.contains("<!DOCTYPE")) {
                log.error("API가 HTML을 반환했습니다. API 키나 URL을 확인해주세요. (sido={})", sidoCode);
                log.error("응답 내용: {}", rawResponse);
                return;
            }

            //JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            SigunguApiResponse response = mapper.readValue(rawResponse, SigunguApiResponse.class);

            if (response == null || response.getResult() == null || response.getResult().getOil() == null) {
                log.warn("시군구 API 응답이 비어있습니다. (sido={})", sidoCode);
                return;
            }

            List<SigunguApiResponse.SigunguOilData> oilDataList = response.getResult().getOil();
            log.info("시군구 API 응답 데이터 수: {}건 (시도코드: {})", oilDataList.size(), sidoCode);

            // 배치 처리를 위한 엔티티 리스트 생성
            List<OilSigungu> entities = new ArrayList<>();
            for (SigunguApiResponse.SigunguOilData oilData : oilDataList) {
                try {
                    OilSigungu entity = new OilSigungu();
                    entity.setDate(today);
                    entity.setSigunCd(oilData.getSigunCd());
                    entity.setSigunNm(oilData.getSigunNm());
                    entity.setProdcd(oilData.getProdcd());
                    entity.setPrice(oilData.getPrice());
                    entity.setDiff(oilData.getDiff());
                    entities.add(entity);
                } catch (Exception e) {
                    log.error("시군구 데이터 변환 실패 (sigunCd={}): {}", oilData.getSigunCd(), e.getMessage());
                }
            }

            // 배치로 저장 (안전한 청크 단위 처리)
            if (!entities.isEmpty()) {
                long dbStartTime = System.currentTimeMillis();
                int totalSaved = 0;
                int chunkSize = 500; // 더 작은 청크 크기로 안전하게 처리
                
                log.info("시군구 데이터 저장 시작 (시도코드: {}, 총 {}건, 청크크기: {})", 
                        sidoCode, entities.size(), chunkSize);
                
                // 500건씩 나누어서 저장
                for (int i = 0; i < entities.size(); i += chunkSize) {
                    int endIndex = Math.min(i + chunkSize, entities.size());
                    List<OilSigungu> chunk = entities.subList(i, endIndex);
                    
                    try {
                        List<OilSigungu> savedChunk = sigunguRepository.saveAll(chunk);
                        totalSaved += savedChunk.size();
                    } catch (Exception e) {
                        log.error("시군구 데이터 청크 저장 실패 (시도코드: {}, 청크: {}-{}): {}", 
                                sidoCode, i, endIndex-1, e.getMessage());
                        // 실패한 청크는 건너뛰고 계속 진행
                    }
                }
                
                long dbEndTime = System.currentTimeMillis();
                log.info("시군구 데이터 저장 완료 (시도코드: {}, 요청: {}건, 실제저장: {}건, DB저장시간: {}ms)", 
                        sidoCode, entities.size(), totalSaved, dbEndTime - dbStartTime);
                
                // 저장된 데이터 수가 요청한 데이터 수와 다른 경우 경고
                if (entities.size() != totalSaved) {
                    log.warn("데이터 저장 불일치! 요청: {}건, 실제저장: {}건 (시도코드: {})",
                            entities.size(), totalSaved, sidoCode);
                }
            }

        } catch (HttpClientErrorException e) {
            log.error("시군구 API 클라이언트 에러 (sido={}, {}): {}", sidoCode, e.getStatusCode(), e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("시군구 API 서버 에러 (sido={}, {}): {}", sidoCode, e.getStatusCode(), e.getMessage());
        } catch (RestClientException e) {
            log.error("시군구 API 네트워크 에러 (sido={}): {}", sidoCode, e.getMessage());
        } catch (Exception e) {
            log.error("시군구 데이터 수집 실패 (sido={}): {}", sidoCode, e.getMessage(), e);
        }
    }

}
