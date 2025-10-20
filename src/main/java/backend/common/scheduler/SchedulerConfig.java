package backend.common.scheduler;

import backend.oilprice.service.OilDataService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {

    private final OilDataService oilDataService;

    @PostConstruct
    public void init() {
        oilDataService.fetchAndSaveOilData(); // 서버 시작 시 즉시 실행
    }

//    @Scheduled(cron = "0 1 2 * * ?") // 매일 새벽 3시(배포시 적용할 스케줄링 메서드)
//    public void collectDailyOilData() {
//        oilDataService.fetchAndSaveOilData();
//    }

}
