package backend.geolocation.util;

import org.locationtech.proj4j.*;

public class CoordinateConverter {

    private static final CoordinateTransformFactory transformFactory = new CoordinateTransformFactory();
    private static final CRSFactory crsFactory = new CRSFactory();

    private static final CoordinateReferenceSystem katecCRS = crsFactory.createFromName("EPSG:5178"); // KATEC
    private static final CoordinateReferenceSystem wgs84CRS = crsFactory.createFromName("EPSG:4326"); // 위도, 경도

    // TM128 좌표계 직접 정의
    private static final CoordinateReferenceSystem tm128CRS = crsFactory.createFromParameters(
            "TM128",
            "+proj=tmerc +lat_0=38 +lon_0=128 +ellps=bessel +x_0=400000 +y_0=600000 +k=0.9999 +towgs84=-146.43,507.89,681.46"
    );

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

        return new double[] { wgs84Coord.x, wgs84Coord.y }; // [longitude, latitude]
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

        return new double[] { katecCoord.x, katecCoord.y };
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

        return new double[] { tm128Coord.x, tm128Coord.y }; // [x, y]
    }
}