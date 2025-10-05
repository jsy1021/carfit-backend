package backend.common.util;

import org.locationtech.proj4j.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CoordinateConverter {

    private static final CoordinateTransformFactory transformFactory = new CoordinateTransformFactory();
    private static final CRSFactory crsFactory = new CRSFactory();
    
    // 좌표 정밀도 설정 (미터 단위로 약 1cm 정밀도)
    private static final int COORDINATE_PRECISION = 6;

    private static final CoordinateReferenceSystem katecCRS = crsFactory.createFromName("EPSG:5178"); // KATEC
    private static final CoordinateReferenceSystem wgs84CRS = crsFactory.createFromName("EPSG:4326"); // 위도, 경도

    // TM128 좌표계 직접 정의 (한국 표준 TM128 좌표계)
    private static final CoordinateReferenceSystem tm128CRS = crsFactory.createFromParameters(
            "TM128",
            "+proj=tmerc +lat_0=38 +lon_0=128 +ellps=bessel +x_0=400000 +y_0=600000 +k=0.9999 +towgs84=-146.43,507.89,681.46 +units=m +no_defs"
    );
    
    /**
     * 좌표값의 정밀도를 조정하여 반올림 오차를 최소화
     */
    private static double roundCoordinate(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(COORDINATE_PRECISION, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * KATEC 좌표를 WGS84(경도, 위도)로 변환
     *
     * @param x KATEC x 좌표
     * @param y KATEC y 좌표
     * @return 변환된 [경도, 위도] 배열
     */
    public static double[] katecToWgs84(double x, double y) {
        CoordinateTransform transform = transformFactory.createTransform(katecCRS, wgs84CRS);

        ProjCoordinate katecCoord = new ProjCoordinate(x, y);
        ProjCoordinate wgs84Coord = new ProjCoordinate();

        transform.transform(katecCoord, wgs84Coord);

        // 정밀도 조정
        double longitude = roundCoordinate(wgs84Coord.x);
        double latitude = roundCoordinate(wgs84Coord.y);

        return new double[] { longitude, latitude }; // [longitude, latitude]
    }

    /**
     * WGS84(경도, 위도)를 KATEC 좌표로 변환
     *
     * @param longitude 경도
     * @param latitude 위도
     * @return 변환된 [x, y] 배열
     */
    public static double[] wgs84ToKatec(double longitude, double latitude) {
        CoordinateTransform transform = transformFactory.createTransform(wgs84CRS, katecCRS);

        ProjCoordinate wgs84Coord = new ProjCoordinate(longitude, latitude);
        ProjCoordinate katecCoord = new ProjCoordinate();

        transform.transform(wgs84Coord, katecCoord);

        // 정밀도 조정
        double x = roundCoordinate(katecCoord.x);
        double y = roundCoordinate(katecCoord.y);

        return new double[] { x, y };
    }
    /**
     * WGS84(경도, 위도)를 TM128 좌표로 변환
     *
     * @param longitude 경도
     * @param latitude 위도
     * @return 변환된 [x, y] 배열
     */
    public static double[] wgs84ToTm128(double longitude, double latitude) {
        CoordinateTransform transform = transformFactory.createTransform(wgs84CRS, tm128CRS);

        ProjCoordinate wgs84Coord = new ProjCoordinate(longitude, latitude);
        ProjCoordinate tm128Coord = new ProjCoordinate();

        transform.transform(wgs84Coord, tm128Coord);

        // 정밀도 조정
        double x = roundCoordinate(tm128Coord.x);
        double y = roundCoordinate(tm128Coord.y);

        return new double[] { x, y }; // [x, y]
    }

    /**
     * TM128 좌표를 WGS84(경도, 위도)로 변환
     *
     * @param x TM128 x 좌표
     * @param y TM128 y 좌표
     * @return 변환된 [경도, 위도] 배열
     */
    public static double[] tm128ToWgs84(double x, double y) {
        CoordinateTransform transform = transformFactory.createTransform(tm128CRS, wgs84CRS);

        ProjCoordinate tm128Coord = new ProjCoordinate(x, y);
        ProjCoordinate wgs84Coord = new ProjCoordinate();

        transform.transform(tm128Coord, wgs84Coord);

        // 정밀도 조정
        double longitude = roundCoordinate(wgs84Coord.x);
        double latitude = roundCoordinate(wgs84Coord.y);

        return new double[] { longitude, latitude }; // [longitude, latitude]
    }
    
    /**
     * WGS84 → TM128 → WGS84 변환의 정확도를 검증하는 메서드
     * 
     * @param originalLon 원본 경도 (WGS84)
     * @param originalLat 원본 위도 (WGS84)
     * @param convertedX 변환된 X 좌표 (TM128)
     * @param convertedY 변환된 Y 좌표 (TM128)
     * @param tolerance 허용 오차 (미터 단위)
     * @return 변환이 정확한지 여부
     */
    public static boolean validateWgs84ToTm128Conversion(double originalLon, double originalLat, 
                                                       double convertedX, double convertedY, 
                                                       double tolerance) {
        // TM128 → WGS84 역변환 수행
        double[] reverseConverted = tm128ToWgs84(convertedX, convertedY);
        
        // 오차 계산 (미터 단위로 근사)
        double deltaX = Math.abs(originalLon - reverseConverted[0]) * 111320; // 경도 1도 ≈ 111,320m
        double deltaY = Math.abs(originalLat - reverseConverted[1]) * 111320; // 위도 1도 ≈ 111,320m
        
        double totalError = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        
        return totalError <= tolerance;
    }
    
    /**
     * TM128 → WGS84 변환의 정확도를 검증하는 메서드
     * 
     * @param originalX 원본 X 좌표 (TM128)
     * @param originalY 원본 Y 좌표 (TM128)
     * @param convertedLon 변환된 경도 (WGS84)
     * @param convertedLat 변환된 위도 (WGS84)
     * @param tolerance 허용 오차 (미터 단위)
     * @return 변환이 정확한지 여부
     */
    public static boolean validateTm128ToWgs84Conversion(double originalX, double originalY, 
                                                       double convertedLon, double convertedLat, 
                                                       double tolerance) {
        // WGS84 → TM128 역변환 수행
        double[] reverseConverted = wgs84ToTm128(convertedLon, convertedLat);
        
        // 오차 계산 (미터 단위)
        double deltaX = Math.abs(originalX - reverseConverted[0]);
        double deltaY = Math.abs(originalY - reverseConverted[1]);
        
        double totalError = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        
        // 디버깅을 위한 로그 (오차가 큰 경우만)
        if (totalError > tolerance) {
            System.out.printf("좌표 변환 오차: %.2fm (허용: %.2fm) - 원본: (%.6f, %.6f), 변환: (%.6f, %.6f), 역변환: (%.6f, %.6f)%n",
                            totalError, tolerance, originalX, originalY, convertedLon, convertedLat, 
                            reverseConverted[0], reverseConverted[1]);
        }
        
        return totalError <= tolerance;
    }
    
}