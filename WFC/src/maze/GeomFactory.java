package maze;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import wblut.geom.*;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Halfedge;
import wblut.hemesh.HE_Mesh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class GeomFactory {
    public static WB_GeometryFactory gf_hemesh = WB_GeometryFactory.instance();
    public static GeometryFactory gf_jts = new GeometryFactory();

    public GeomFactory() {
    }


    public static Polygon toJTSPolygon(WB_Polygon poly) {
        int num = poly.getNumberOfContours();
        if (num == 1) {
            return toJTSPolygonSimple(poly);
        } else {
            WB_Coord[] shell_points = getShellPts(poly);
            LinearRing outRing = gf_jts.createLinearRing(toLinearRing(shell_points));
            WB_Coord[][] holes_points = getInnerPts(poly);
            LinearRing[] holeRings = new LinearRing[holes_points.length];

            for(int i = 0; i < holes_points.length; ++i) {
                holeRings[i] = gf_jts.createLinearRing(toLinearRing(holes_points[i]));
            }

            return gf_jts.createPolygon(outRing, holeRings);
        }
    }

    public static Polygon toJTSPolygon(HE_Face face) {
        WB_Polygon wb_polygon = face.getPolygon();
        return toJTSPolygon(wb_polygon);
    }

    public static Coordinate toJTSCoord(WB_Point p) {
        return new Coordinate(p.xf(),p.yf());
    }

    public static List<LineString> toJTSLineString(WB_Polygon poly) {
        List<LineString> lss = new ArrayList();
        int num = poly.getNumberOfContours();
        WB_Coord[] shell_points = getShellPts(poly);
        List<LineString> shell = toLineStrings(shell_points);
        if (num == 1) {
            return shell;
        } else {
            lss.addAll(shell);
            WB_Coord[][] holes_points = getInnerPts(poly);

            for(int i = 0; i < holes_points.length; ++i) {
                lss.addAll(toLineStrings(holes_points[i]));
            }

            return lss;
        }
    }

    public static MultiLineString toJTSLineString(Polygon poly) {
        List<LineString> lss = new ArrayList();
        LineString shell = poly.getExteriorRing();
        Coordinate[] shell_coords = shell.getCoordinates();

        int holesNum;
        for(holesNum = 0; holesNum < shell_coords.length - 1; ++holesNum) {
            lss.add(gf_jts.createLineString(new Coordinate[]{shell_coords[holesNum], shell_coords[holesNum + 1]}));
        }

        holesNum = poly.getNumInteriorRing();

        for(int i = 0; i < holesNum; ++i) {
            LineString ls = poly.getInteriorRingN(i);
            Coordinate[] holes = ls.getCoordinates();

            for(int j = 0; j < holes.length - 1; ++j) {
                lss.add(gf_jts.createLineString(new Coordinate[]{holes[j], holes[j + 1]}));
            }
        }

        return gf_jts.createMultiLineString((LineString[])lss.toArray(new LineString[lss.size()]));
    }

    private static List<LineString> toLineStrings(WB_Coord[] points) {
        List<LineString> lss = new ArrayList();
        int leng = points.length;

        for(int i = 0; i < points.length; ++i) {
            Coordinate sp = new Coordinate(points[i].xd(), points[i].yd());
            Coordinate ep = new Coordinate(points[(i + 1) % leng].xd(), points[(i + 1) % leng].yd());
            lss.add(gf_jts.createLineString(new Coordinate[]{sp, ep}));
        }

        return lss;
    }

    public static Polygon toJTSPolygonSimple(WB_Polygon poly) {
        WB_Coord[] wb_coord = getShellPts(poly);
        Coordinate[] coords = new Coordinate[wb_coord.length + 1];

        for(int i = 0; i < wb_coord.length; ++i) {
            coords[i] = new Coordinate(wb_coord[i].xd(), wb_coord[i].yd());
        }

        coords[wb_coord.length] = coords[0];
        return gf_jts.createPolygon(coords);
    }

    public static Coordinate[] toLinearRing(WB_Coord[] wb_points) {
        int leng = wb_points.length;
        Coordinate[] coords = new Coordinate[leng + 1];

        for(int i = 0; i < leng; ++i) {
            coords[i] = new Coordinate(wb_points[i].xd(), wb_points[i].yd());
        }

        coords[leng] = coords[0];
        return coords;
    }

    public static WB_Coord[] getShellPts(WB_Polygon poly) {
        if (poly.getNumberOfContours() == 1) {
            return poly.getPoints().toArray();
        } else {
            int numOut = poly.getNumberOfShellPoints();
            WB_Coord[] out = new WB_Point[numOut];

            for(int i = 0; i < numOut; ++i) {
                out[i] = poly.getPoint(i);
            }

            return out;
        }
    }

    public static WB_Point[][] getInnerPts(WB_Polygon poly) {
        if (poly.getNumberOfContours() == 1) {
            return (WB_Point[][])null;
        } else {
            WB_Point[][] in = new WB_Point[poly.getNumberOfHoles()][];
            int[] num = poly.getNumberOfPointsPerContour();
            int count = num[0];

            for(int i = 0; i < in.length; ++i) {
                WB_Point[] pts = new WB_Point[num[i + 1]];

                for(int j = 0; j < pts.length; ++j) {
                    pts[j] = poly.getPoint(count + j);
                }

                in[i] = pts;
                count += pts.length;
            }

            return in;
        }
    }

    public static WB_Polygon jtsPolygon2WB_Polygon2D(Geometry g) {
        if (!g.getGeometryType().equalsIgnoreCase("Polygon")) {
            System.out.println("this Geometry is not a Polygon!");
            return null;
        } else {
            Polygon p = (Polygon)g;
            Coordinate[] coordOut = p.getExteriorRing().getCoordinates();
            coordOut = subLast(coordOut);
            WB_Point[] outPt = new WB_Point[coordOut.length];

            int num;
            for(num = 0; num < coordOut.length; ++num) {
                outPt[num] = new WB_Point(coordOut[num].x, coordOut[num].y);
            }

            num = p.getNumInteriorRing();
            if (num == 0) {
                return new WB_Polygon(outPt);
            } else {
                WB_Point[][] ptsIn = new WB_Point[num][];

                for(int i = 0; i < num; ++i) {
                    Coordinate[] coords = p.getInteriorRingN(i).getCoordinates();
                    WB_Point[] pts = new WB_Point[coords.length - 1];

                    for(int j = 0; j < coords.length - 1; ++j) {
                        pts[j] = new WB_Point(coords[j].x, coords[j].y);
                    }

                    ptsIn[i] = pts;
                }

                return new WB_Polygon(outPt, ptsIn);
            }
        }
    }

    public static Coordinate[] subLast(Coordinate[] ori_coord) {
        int leng = ori_coord.length - 1;
        if (leng < 1) {
            return ori_coord;
        } else {
            Coordinate[] wb_poly = new Coordinate[leng];

            for(int i = 0; i < leng; ++i) {
                wb_poly[i] = ori_coord[i];
            }

            return wb_poly;
        }
    }

    public static HE_Mesh jtsSimplePolygons2He_mesh(List<Polygon> jtsPolygons) {
        int leng = jtsPolygons.size();
        WB_Polygon[] polygons = new WB_Polygon[leng];

        for(int i = 0; i < leng; ++i) {
            Coordinate[] coords = ((Polygon)jtsPolygons.get(i)).getCoordinates();
            WB_Coord[] wb_coords = new WB_Point[coords.length - 1];

            for(int j = 0; j < coords.length - 1; ++j) {
                wb_coords[j] = new WB_Point(coords[j].x, coords[j].y);
            }

            polygons[i] = gf_hemesh.createSimplePolygon(wb_coords);
        }

        HEC_FromPolygons creator = new HEC_FromPolygons();
        creator.setPolygons(polygons);
        return new HE_Mesh(creator);
    }

    public static LineString simplify(WB_PolyLine polyline, double tolerance) {
        int pntNum = polyline.getNumberOfPoints();
        Coordinate[] coords = new Coordinate[pntNum];

        for(int i = 0; i < pntNum; ++i) {
            WB_Point sb_pnt = polyline.getPoint(i);
            coords[i] = new Coordinate(sb_pnt.xd(), sb_pnt.yd());
        }

        LineString lineString = gf_jts.createLineString(coords);
        return (LineString)DouglasPeuckerSimplifier.simplify(lineString, tolerance);
    }

    public static LineString simplify(LineString lineString, double tolerance) {
        return (LineString)DouglasPeuckerSimplifier.simplify(lineString, tolerance);
    }


    public static List<Polygon> extractLines2JTSPolygon(List<Geometry> lineStringCollection) {
        List<LineString> lsss = new ArrayList();

        for(int i = 0; i < lineStringCollection.size(); ++i) {
            Geometry c_g = (Geometry)lineStringCollection.get(i);
            if (c_g.getGeometryType() == "LineString") {
                lsss.add((LineString)c_g);
            } else if (c_g.getGeometryType() == "MultiLineString") {
                MultiLineString ms = (MultiLineString)c_g;
                int g_num = ms.getNumGeometries();

                for(int j = 0; j < g_num; ++j) {
                    lsss.add((LineString)ms.getGeometryN(j));
                }
            }
        }

        Geometry nodedLineStrings = (LineString)lsss.get(0);

        for(int i = 1; i < lsss.size(); ++i) {
            nodedLineStrings = ((Geometry)nodedLineStrings).union((LineString)lsss.get(i));
        }

        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add((Geometry)nodedLineStrings);
        Collection polys = polygonizer.getPolygons();
        List<Polygon> newPolygons = new ArrayList();
        Iterator it = polys.iterator();

        while(it.hasNext()) {
            Polygon c_p = (Polygon)it.next();
            newPolygons.add(c_p);
        }

        return newPolygons;
    }

    public static HE_Mesh extractLines2JTSPolygon(HE_Mesh mesh) {
        List<Geometry> lss = new ArrayList();
        List<HE_Halfedge> es = mesh.getEdges();
        Iterator var3 = es.iterator();

        while(var3.hasNext()) {
            HE_Halfedge e = (HE_Halfedge)var3.next();
            WB_Point st = e.getStartPosition();
            WB_Point et = e.getEndPosition();
            LineString ls = gf_jts.createLineString(new Coordinate[]{new Coordinate(st.xd(), st.yd()), new Coordinate(et.xd(), et.yd())});
            lss.add(ls);
        }

        List<Polygon> ploygons = extractLines2JTSPolygon((List)lss);
        return jtsSimplePolygons2He_mesh(ploygons);
    }

}
