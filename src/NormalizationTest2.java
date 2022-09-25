import gzf.gui.CameraController;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.hemesh.*;
import wblut.processing.WB_Render;

public class NormalizationTest2 extends PApplet {
    public static void main(String[] args) {
        PApplet.main("NormalizationTest2");
    }


    HE_Mesh mesh;
    HE_Mesh copy;


    WB_Render render;
    CameraController cam;
    Normalization nm;
    boolean is=false;

    public void setup() {
        size(1200, 800, P3D);
        render = new WB_Render(this);
        cam=new CameraController(this,200);

        createMesh();
        copy=mesh.copy();
        WB_Transform3D transform3D=new WB_Transform3D();
        transform3D.addTranslate(new WB_Point(50000,0,0));
        copy.applySelf(transform3D);
        nm=new Normalization(this.mesh,30,5000,10,1);

    }

    public void keyReleased() {
        if (key == 'A') nm.optimize();
        if (key == 'B') {

            is=!is;
            //nm.optimize2();
        }
        if(key=='C'){
            HET_Export.saveAsOBJ(nm.mesh,"E://","newOBJ0920-2");
            HET_Export.saveAsHemesh(nm.mesh,"E://","newMesh0920-2");
        }
    }

    public void draw() {
        cam.top();
        background(255);
        lights();
        directionalLight(255, 255, 255, 1, 1, -1);
        directionalLight(127, 127, 127, -1, -1, 1);
        if(is)nm.optimize2();
        //nm.draw(this,render);
        stroke(0);
        //fill()
        fill(200);
        render.drawFaces(mesh);
        render.drawFaces(copy);
        fill(0);


    }

    void createMesh() {
        ImportObj obj=new ImportObj("E:\\gz.3dm");
        this.mesh=obj.getObj();
    }

}
