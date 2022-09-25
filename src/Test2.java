import gzf.gui.CameraController;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.hemesh.*;
import wblut.processing.WB_Render;

public class Test2 extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Test2");
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
        transform3D.addTranslate(new WB_Point(50000,0,0));
        copy.applySelf(transform3D);

        nm=new Normalization(this.mesh,30,100,5);


    }

    public void keyReleased() {

        if (key == 'A') nm.optimize();
        if(key=='B'){
            HET_Export.saveAsOBJ(nm.mesh,"E://","newOBJ0920");
            HET_Export.saveAsHemesh(nm.mesh,"E://","newMesh0920");
        }
    }

    public void draw() {
        background(55);
        directionalLight(255, 255, 255, 1, 1, -1);
        directionalLight(127, 127, 127, -1, -1, 1);
        //nm.draw(this,render);
        stroke(0);
        render.drawFaces(mesh);
        render.drawFaces(copy);
    }

    void createMesh() {

//        int num=3;
//        mesh = new HE_Mesh(new HEC_Cube().setEdge(4000));
//        //mesh.smooth();
//        HEC_Dual creator = new HEC_Dual();
//        creator.setSource(mesh);
//        dual = new HE_Mesh(creator);
//
//        WB_Transform3D tran = new WB_Transform3D();
//        tran.addRotateAboutAxis(PI/6,new WB_Point(0,0,0),new WB_Point(0,0,1));
//        tran.addTranslate(new WB_Vector(-4000,2000,4000));
//
//        mesh.applySelf(tran);
//        dual.applySelf(tran);
//
//        WB_Point[] points = new WB_Point[num*num];
//        int index = 0;
//        for (int j = 0; j < num; j++) {
//            for (int i = 0; i < num; i++) {
//                points[index] = new WB_Point((int)(-200 + i * 40 + (((i != 0) && (i != 10)) ? random(-20, 20) : 0)),
//                        (int)(-200 + j * 40 + (((j != 0) && (j != 10)) ? random(-20, 20) : 0)),
//                        (int)(sin(TWO_PI / 20 * i) * 40 + cos(TWO_PI / 10 * j) * 40));
//                index++;
//            }
//        }
//
//        // create triangles from point grid
//        WB_Triangle[] tris = new WB_Triangle[(num-1) *(num-1) *2];
//
//        for (int i = 0; i < num-1; i++) {
//            for (int j = 0; j < num-1; j++) {
//                tris[2 * (i + (num-1) * j)] = new WB_Triangle(points[i + num * j], points[i + 1 + num * j],
//                        points[i + num * j + num]);
//                tris[2 * (i + (num-1) * j) + 1] = new WB_Triangle(points[i + 1 + num * j], points[i + num * j + 1+num],
//                        points[i + num * j + num]);
//            }
//        }
//
//        HEC_FromTriangles tri_creator = new HEC_FromTriangles();
//        tri_creator.setTriangles(tris);
//        // alternatively tris can be any Collection<WB_Triangle>
//        mesh = new HE_Mesh(tri_creator);


//        HEC_Polygon creator=new HEC_Polygon();
//        creator.setPolygon(new WB_Polygon(new WB_Point(0,0,10),
//                new WB_Point(30,0,10),new WB_Point(0,30,0),new WB_Point(-50,15,10)));
//        mesh=new HE_Mesh(creator);


        ImportObj obj=new ImportObj("E:\\0917.3dm");
        this.mesh=obj.getObj();

    }

}
