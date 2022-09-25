import gzf.gui.CameraController;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.hemesh.*;
import wblut.processing.WB_Render;

import java.awt.*;

public class NormalizationTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("NormalizationTest");
    }


    HE_Mesh mesh;
    HE_Mesh copy;
    HE_Mesh dual;
    HE_Mesh tri_mesh;

    WB_Render render;
    CameraController cam;
    Normalization nm;

    public void setup() {
        size(1000, 1000, P3D);
        render = new WB_Render(this);
        cam=new CameraController(this,200);

        createMesh();
        copy=mesh.copy();
        WB_Transform3D transform3D=new WB_Transform3D();
        transform3D.addTranslate(new WB_Point(250,0,0));
        copy.applySelf(transform3D);

        nm=new Normalization(this.mesh,2,30,5);


    }

    public void keyReleased() {

        if (key == 'A') nm.optimize();
        if(key=='B'){
            HET_Export.saveAsOBJ(nm.mesh,"E://","newOBJ0917-2");
            HET_Export.saveAsHemesh(nm.mesh,"E://","newMesh0917-2");
        }
    }

    public void draw() {
        cam.top();
        background(255);
        directionalLight(255, 255, 255, 1, 1, -1);
        directionalLight(127, 127, 127, -1, -1, 1);
       //nm.draw(this,render);
       stroke(0);
       render.drawEdges(mesh);
       disPlayHeMesh(mesh,1);
       render.drawFaces(copy);
    }


    /**
     * 半边数据结构的显示
     *
     * He SiYuan
     * */
    public void disPlayHeMesh(HE_Mesh mesh,double r){
        displayHalfEdges(mesh);
        displayHeVertices(mesh,r);
    }


    public void displayHeVertices(HE_Mesh mesh,double r){
        pushStyle();
        Color color=new Color(205, 237, 159);
        noStroke();
        fill(color.getRGB());
        for(HE_Vertex v:mesh.getVertices()){
            pushMatrix();
            translate(v.xf(),v.yf(),v.zf());
            sphere((float) r);
            popMatrix();
        }

        popStyle();
    }
    public void displaySingleHeVertex(HE_Vertex v,Color color){
        pushStyle();
        fill(color.getRGB());
        pushMatrix();
        translate(v.xf(),v.yf(),v.zf());
        sphere(50);
        popMatrix();
        popStyle();
    }
    public void displayHalfEdges(HE_Mesh mesh){
        pushStyle();
        Color color=new Color(95, 199, 196);
        stroke(color.getRGB());
        strokeWeight(2);
        for(HE_Halfedge he:mesh.getHalfedges()){
            double offsetDis=he.getLength()/100;
            stroke(color.getRGB());
            strokeWeight(2);
            HE_Face referFace=he.isOuterBoundary()?he.getPair().getFace():he.getFace();

            WB_Vector vec=new WB_Vector(he.getVertex(),he.getEndVertex());
            WB_Vector v= (WB_Vector) he.getHalfedgeDirection();
            WB_Vector v_ortho=v.rotateAboutAxis(90*DEG_TO_RAD,new WB_Point(0,0,0), referFace.getFaceNormal()).mul(offsetDis);
            WB_Point ps=he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.25));
            WB_Point pe=he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.75));
            WB_Vector v_arrow=v.rotateAboutAxis(150*DEG_TO_RAD, new WB_Point(0,0,0),referFace.getFaceNormal()).mul(he.getLength()/50);

            WB_Point p_arrow=pe.add(v_arrow);
            line(ps.xf(),ps.yf(),ps.zf(),pe.xf(),pe.yf(),pe.zf());
            line(pe.xf(),pe.yf(),pe.zf(),p_arrow.xf(),p_arrow.yf(),p_arrow.zf());
        }

        popStyle();
    }
    public void displaySingleHalfEdge(HE_Halfedge he, Color color){
        double offsetDis=he.getLength()/100;
        pushStyle();
        stroke(color.getRGB());
        strokeWeight(2);
        HE_Face referFace=he.isOuterBoundary()?he.getPair().getFace():he.getFace();

        WB_Vector vec=new WB_Vector(he.getVertex(),he.getEndVertex());
        WB_Vector v= (WB_Vector) he.getHalfedgeDirection();
        WB_Vector v_ortho=v.rotateAboutAxis(90*DEG_TO_RAD,new WB_Point(0,0,0), referFace.getFaceNormal()).mul(offsetDis);
        WB_Point ps=he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.25));
        WB_Point pe=he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.75));
        WB_Vector v_arrow=v.rotateAboutAxis(150*DEG_TO_RAD, new WB_Point(0,0,0),referFace.getFaceNormal()).mul(he.getLength()/50);

        WB_Point p_arrow=pe.add(v_arrow);
        line(ps.xf(),ps.yf(),ps.zf(),pe.xf(),pe.yf(),pe.zf());
        line(pe.xf(),pe.yf(),pe.zf(),p_arrow.xf(),p_arrow.yf(),p_arrow.zf());
        popStyle();
    }
    void createMesh() {

        int num=4;
        mesh = new HE_Mesh(new HEC_Cube().setEdge(4000));
        //mesh.smooth();
        HEC_Dual creator = new HEC_Dual();
        creator.setSource(mesh);
        dual = new HE_Mesh(creator);

        WB_Transform3D tran = new WB_Transform3D();
        tran.addRotateAboutAxis(PI/6,new WB_Point(0,0,0),new WB_Point(0,0,1));
        tran.addTranslate(new WB_Vector(-4000,2000,4000));

        mesh.applySelf(tran);
        dual.applySelf(tran);

        WB_Point[] points = new WB_Point[num*num];
        int index = 0;
        for (int j = 0; j < num; j++) {
            for (int i = 0; i < num; i++) {
                points[index] = new WB_Point((int)(-200 + i * 40 + (((i != 0) && (i != 10)) ? random(-20, 20) : 0)),
                        (int)(-200 + j * 40 + (((j != 0) && (j != 10)) ? random(-20, 20) : 0)),
                        0);
                index++;
            }
        }

        // create triangles from point grid
        WB_Triangle[] tris = new WB_Triangle[(num-1) *(num-1) *2];

        for (int i = 0; i < num-1; i++) {
            for (int j = 0; j < num-1; j++) {
                tris[2 * (i + (num-1) * j)] = new WB_Triangle(points[i + num * j], points[i + 1 + num * j],
                        points[i + num * j + num]);
                tris[2 * (i + (num-1) * j) + 1] = new WB_Triangle(points[i + 1 + num * j], points[i + num * j + 1+num],
                        points[i + num * j + num]);
            }
        }

        HEC_FromTriangles tri_creator = new HEC_FromTriangles();
        tri_creator.setTriangles(tris);
        // alternatively tris can be any Collection<WB_Triangle>
        mesh = new HE_Mesh(tri_creator);


//        HEC_Polygon creator=new HEC_Polygon();
//        creator.setPolygon(new WB_Polygon(new WB_Point(0,0,10),
//                new WB_Point(30,0,10),new WB_Point(0,30,0),new WB_Point(-50,15,10)));
//        mesh=new HE_Mesh(creator);


        ImportObj obj=new ImportObj("E:\\0917.3dm");
        //this.mesh=obj.getObj();

    }

}
