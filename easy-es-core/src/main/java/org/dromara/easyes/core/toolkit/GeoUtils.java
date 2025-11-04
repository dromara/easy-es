package org.dromara.easyes.core.toolkit;

import co.elastic.clients.elasticsearch._types.DistanceUnit;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import org.dromara.easyes.common.exception.EasyEsException;
import org.elasticsearch.geometry.*;
import org.elasticsearch.geometry.utils.BitUtil;
import org.elasticsearch.geometry.utils.GeographyValidator;
import org.elasticsearch.geometry.utils.Geohash;
import org.elasticsearch.geometry.utils.WellKnownText;

import java.util.*;

/**
 * geo工具类
 *
 * @author jaime
 * @version 1.0
 * @since 2025/2/18
 */
public class GeoUtils {

    /**
     * 读取geohash或lat-lon组合
     *
     * @param value 此 String 必须是 geohash 或 lat-lon组合(以逗号间隔)
     * @return GeoLocation
     */
    public static GeoLocation create(String value) {
        return create(value, false, EffectivePoint.BOTTOM_LEFT);
    }

    public static GeoLocation create(String value, final boolean ignoreZValue, EffectivePoint effectivePoint) {
        if (value.toLowerCase(Locale.ROOT).contains("point")) {
            return createFromWKT(value, ignoreZValue);
        } else if (value.contains(",")) {
            return createFromCoordinates(value, ignoreZValue);
        }
        return parseGeoHash(value, effectivePoint);
    }

    public static GeoLocation createFromCoordinates(String value, final boolean ignoreZValue) {
        String[] vals = value.split(",");
        if (vals.length > 3) {
            throw new EasyEsException(String.format("failed to parse [%s], expected 2 or 3 coordinates but found: [%s]", value, vals.length));
        }
        final double lat;
        final double lon;
        try {
            lat = Double.parseDouble(vals[0].trim());
        } catch (NumberFormatException ex) {
            throw new EasyEsException("latitude must be a number");
        }
        try {
            lon = Double.parseDouble(vals[1].trim());
        } catch (NumberFormatException ex) {
            throw new EasyEsException("longitude must be a number");
        }
        if (vals.length > 2 && !ignoreZValue) {
            throw new EasyEsException(String.format(
                    "Exception parsing coordinates: found Z value [%s] but [ignore_z_value] parameter is [%s]",
                    Double.parseDouble(vals[2].trim()), ignoreZValue));
        }
        return create(lat, lon);
    }

    private static GeoLocation createFromWKT(String value, boolean ignoreZValue) {
        Geometry geometry;
        try {
            geometry = WellKnownText.fromWKT(GeographyValidator.instance(ignoreZValue), false, value);
        } catch (Exception e) {
            throw new EasyEsException("Invalid WKT format", e);
        }
        if (geometry.type() != ShapeType.POINT) {
            throw new EasyEsException(
                    "[geo_point] supports only POINT among WKT primitives, " + "but found " + geometry.type()
            );
        }
        Point point = (Point) geometry;
        return create(point.getY(), point.getX());
    }

    public static GeoLocation parseGeoHash(String geohash, EffectivePoint effectivePoint) {
        if (effectivePoint == EffectivePoint.BOTTOM_LEFT) {
            return createFromGeoHash(geohash);
        } else {
            Rectangle rectangle = Geohash.toBoundingBox(geohash);
            switch (effectivePoint) {
                case TOP_LEFT:
                    return create(rectangle.getMaxY(), rectangle.getMinX());
                case TOP_RIGHT:
                    return create(rectangle.getMaxY(), rectangle.getMaxX());
                case BOTTOM_RIGHT:
                    return create(rectangle.getMinY(), rectangle.getMaxX());
                default:
                    throw new IllegalArgumentException("Unsupported effective point " + effectivePoint);
            }
        }
    }

    public static GeoLocation createFromIndexHash(long hash) {
        return create(Geohash.decodeLatitude(hash), Geohash.decodeLongitude(hash));
    }

    public static GeoLocation createFromGeoHash(String geohash) {
        return createFromIndexHash(Geohash.mortonEncode(geohash));
    }

    public static GeoLocation createFromGeoHash(long geohashLong) {
        final int level = (int) (12 - (geohashLong & 15));
        return createFromIndexHash(BitUtil.flipFlop((geohashLong >>> 4) << ((level * 5) + 2)));
    }

    public static GeoLocation create(double lat, double lon) {
        return GeoLocation.of(b -> b.latlon(x -> x.lat(lat).lon(lon)));
    }

    /**
     * 表示 geohash 单元格中应用作 geohash 值的点
     */
    public enum EffectivePoint {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    /**
     * geoJson 转换
     * @param geometry geometry
     * @return map
     */
    public static Map<String, Object> toMap(Geometry geometry) {
        String geoJsonName = null;
        try {
            geoJsonName = getGeoJsonName(geometry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> root = new HashMap<>();
        root.put("type", geoJsonName);

        geometry.visit(new GeometryVisitor<Void, RuntimeException>() {
            @Override
            public Void visit(Circle circle) {
                root.put("radius", circle.getRadiusMeters() + DistanceUnit.Meters.jsonValue());
                root.put("coordinates", coordinatesToList(circle.getY(), circle.getX(), circle.getZ()));
                return null;
            }

            @Override
            public Void visit(GeometryCollection<?> collection) {
                List<Object> geometries = new ArrayList<>(collection.size());

                for (Geometry g : collection) {
                    geometries.add(toMap(g));
                }
                root.put("geometries", geometries);
                return null;
            }

            @Override
            public Void visit(Line line) {
                root.put("coordinates", coordinatesToList(line));
                return null;
            }

            @Override
            public Void visit(LinearRing ring) {
                throw new UnsupportedOperationException("linearRing cannot be serialized using GeoJson");
            }

            @Override
            public Void visit(MultiLine multiLine) {
                List<Object> lines = new ArrayList<>(multiLine.size());
                for (int i = 0; i < multiLine.size(); i++) {
                    lines.add(coordinatesToList(multiLine.get(i)));
                }
                root.put("coordinates", lines);
                return null;
            }

            @Override
            public Void visit(MultiPoint multiPoint) {
                List<Object> points = new ArrayList<>(multiPoint.size());
                for (int i = 0; i < multiPoint.size(); i++) {
                    Point p = multiPoint.get(i);
                    List<Object> point = new ArrayList<>();
                    point.add(p.getX());
                    point.add(p.getY());
                    if (p.hasZ()) {
                        point.add(p.getZ());
                    }
                    points.add(point);
                }
                root.put("coordinates", points);
                return null;
            }

            @Override
            public Void visit(MultiPolygon multiPolygon) {
                List<Object> polygons = new ArrayList<>();
                for (int i = 0; i < multiPolygon.size(); i++) {
                    polygons.add(coordinatesToList(multiPolygon.get(i)));
                }
                root.put("coordinates", polygons);
                return null;
            }

            @Override
            public Void visit(Point point) {
                root.put("coordinates", coordinatesToList(point.getY(), point.getX(), point.getZ()));
                return null;
            }

            @Override
            public Void visit(Polygon polygon) {
                List<Object> coords = new ArrayList<>(polygon.getNumberOfHoles() + 1);
                coords.add(coordinatesToList(polygon.getPolygon()));
                for (int i = 0; i < polygon.getNumberOfHoles(); i++) {
                    coords.add(coordinatesToList(polygon.getHole(i)));
                }
                root.put("coordinates", coords);
                return null;
            }

            @Override
            public Void visit(Rectangle rectangle) {
                List<Object> coords = new ArrayList<>(2);
                coords.add(coordinatesToList(rectangle.getMaxY(), rectangle.getMinX(), rectangle.getMinZ())); // top left
                coords.add(coordinatesToList(rectangle.getMinY(), rectangle.getMaxX(), rectangle.getMaxZ())); // bottom right
                root.put("coordinates", coords);
                return null;
            }

            private List<Object> coordinatesToList(double lat, double lon, double alt) {
                List<Object> coords = new ArrayList<>(3);
                coords.add(lon);
                coords.add(lat);
                if (!Double.isNaN(alt)) {
                    coords.add(alt);
                }
                return coords;
            }

            private List<Object> coordinatesToList(Line line) {
                List<Object> lines = new ArrayList<>(line.length());
                for (int i = 0; i < line.length(); i++) {
                    List<Object> coords = new ArrayList<>(3);
                    coords.add(line.getX(i));
                    coords.add(line.getY(i));
                    if (line.hasZ()) {
                        coords.add(line.getZ(i));
                    }
                    lines.add(coords);
                }
                return lines;
            }

            private List<Object> coordinatesToList(Polygon polygon) {
                List<Object> coords = new ArrayList<>(polygon.getNumberOfHoles() + 1);
                coords.add(coordinatesToList(polygon.getPolygon()));
                for (int i = 0; i < polygon.getNumberOfHoles(); i++) {
                    coords.add(coordinatesToList(polygon.getHole(i)));
                }
                return coords;
            }

        });
        return root;
    }

    /**
     * 获取geometry名称
     *
     * @param geometry geometry
     * @return String
     * @throws Exception RuntimeException
     */
    public static String getGeoJsonName(Geometry geometry) throws Exception {
        return geometry.visit(new GeometryVisitor<String, Exception>() {
            @Override
            public String visit(Circle circle) {
                return "Circle";
            }

            @Override
            public String visit(GeometryCollection<?> collection) {
                return "GeometryCollection";
            }

            @Override
            public String visit(Line line) {
                return "LineString";
            }

            @Override
            public String visit(LinearRing ring) {
                throw new UnsupportedOperationException("line ring cannot be serialized using GeoJson");
            }

            @Override
            public String visit(MultiLine multiLine) {
                return "MultiLineString";
            }

            @Override
            public String visit(MultiPoint multiPoint) {
                return "MultiPoint";
            }

            @Override
            public String visit(MultiPolygon multiPolygon) {
                return "MultiPolygon";
            }

            @Override
            public String visit(Point point) {
                return "Point";
            }

            @Override
            public String visit(Polygon polygon) {
                return "Polygon";
            }

            @Override
            public String visit(Rectangle rectangle) {
                return "Envelope";
            }
        });
    }
}
