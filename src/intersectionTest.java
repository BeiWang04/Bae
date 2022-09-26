import peasy.PeasyCam;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.processing.WB_Render;

import java.awt.*;

public class intersectionTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("intersectionTest");
    }

    PeasyCam cam;
    WB_Render render;
    WB_Segment s;
    WB_Polygon t;
    WB_Polygon t2;
    WB_Point point = new WB_Point();
    Color color=new Color(201, 201, 201);
    boolean is;
    WB_IntersectionResult intersection3D;

    public void setup() {
        size(800, 800, P3D);
        this.cam = new PeasyCam(this, 200);
        this.render = new WB_Render(this);
        s = new WB_Segment(new WB_Point(0, 50, 0), new WB_Point(150, 50, 0));
        t = new WB_Polygon(new WB_Point(0, 0), new WB_Point(100, 0), new WB_Point(50, 100,0));
        t2 = new WB_Polygon(new WB_Point(0, 100), new WB_Point(100, 100), new WB_Point(50, 0,50));
        is=intersection(s, t);

        WB_Triangle t1=new WB_Triangle(new WB_Point(0, 0), new WB_Point(100, 0), new WB_Point(50, 100,0));
        WB_Triangle t2=new WB_Triangle(new WB_Point(0, 100), new WB_Point(100, 100), new WB_Point(50, 0,50));
        intersection3D = WB_GeometryOp.getIntersection3D(t1,t2);

        System.out.println(intersection3D.intersection);
        System.out.println(intersection3D.object);
        //is=intersectionTri(t,t2);
    }

    public void draw() {
        background(255);
        stroke(0);
        fill(color.getRGB());
        render.drawSegment(s);
        render.drawPolygon(t);
        render.drawPolygon(t2);
        fill(0);
        stroke(255,0,0);
        strokeWeight(5);
        render.drawSegment((WB_Segment) intersection3D.object);
        //textSize(50);
        if(is){
         text("Intersection : True",0,-20);}else{
         text("Intersection : False",0,-20);}
    }

    public static boolean intersection(WB_Segment s, WB_Polygon t) {
        boolean intersection = false;
        WB_Plane p = t.getPlane();
        //若边和平面不平行
        if (WB_GeometryOp.getDistance3D(s.getCenter(), p) != WB_GeometryOp.getDistance3D(s.getEndpoint(), p)) {
            WB_Line l = new WB_Line(s.getCenter(), s.getEndpoint());

            WB_IntersectionResult intersection3D = WB_GeometryOp.getIntersection3D(l, p);
            WB_Point point = (WB_Point) intersection3D.object;

            if (point != null && WB_GeometryOp.getDistance3D(point, t) == 0) {
                intersection = true;
            }

            //若边和面平行
        } else {
            //每一条边是否相交
            int numberSegments = t.getNumberSegments();
            for(int i=0;i<numberSegments;i++){
                WB_Segment segment = t.getSegment(i);

                //System.out.println(WB_GeometryOp.getDistance3D(s,segment));
                if(WB_GeometryOp.getDistance3D(s,segment)==0){
                    //System.out.println("1"+intersection);
                    intersection=true;break;}
            }
            //若都不相交，判断端点是否在多边形内
            if(!intersection){
                if(WB_GeometryOp.getDistance3D(s.getEndpoint(),t)==0){
                    //System.out.println("2"+intersection);
                    intersection=true;}
            }

        }
        return intersection;
    }

    //如何使得共顶点不算相交
    public static boolean intersectionTri(WB_Polygon t1, WB_Polygon t2) {
        boolean intersection = false;
        //System.out.println(t1.getNumberSegments());
       for(int i=0;i<t1.getNumberSegments()+1;i++){
           WB_Segment segment = t1.getSegment(i);
           if(intersection(segment,t2)){intersection=true;break;}
       }
        for(int i=0;i<t2.getNumberSegments()+1;i++){
            WB_Segment segment = t2.getSegment(i);
            if(intersection(segment,t1)){intersection=true;break;}
        }
        return intersection;

        //return
    }
}
