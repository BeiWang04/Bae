import gzf.gui.CameraController;
import igeo.IFace;
import igeo.IG;
import igeo.IMesh;
import igeo.IVertex;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.hemesh.HEC_FromFacelist;
import wblut.hemesh.HEM_Extrude;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

public class ImportObj_Test extends PApplet{
    public static void main(String[] args) { PApplet.main("ImportObj_Test"); }
    CameraController cam;
    String path="D:\\0902-PolyTest.3dm";
    ArrayList<HE_Mesh> volumnmeshes =new ArrayList<>();
    ArrayList<HE_Mesh> floormeshes=new ArrayList<>();
    ArrayList<HE_Mesh> groundmeshes=new ArrayList<>();
    ArrayList<HE_Mesh> archmeshes=new ArrayList<>();
    ArrayList<HE_Mesh> detailsmeshes=new ArrayList<>();
    ArrayList<HE_Mesh> woodmeshes=new ArrayList<>();

    HE_Mesh meshcopy;
    WB_Render render;

    public void setup() {
        size(1000,1000,P3D);
        render=new WB_Render(this);
        setcam();
        importblabla(path);

        for(HE_Mesh mesh: volumnmeshes){
            meshcopy=mesh.copy();
            HEM_Extrude modi=new HEM_Extrude().setDistance(100);
            mesh.modify(modi);
        }
        System.out.println("finish");
    }

    public void draw(){
        background(255);
        cam.drawSystem(500);
        pushStyle();
        fill(0,0,255,20);
        for(HE_Mesh mesh: volumnmeshes){
            render.drawFaces(mesh);
        }
        popStyle();

        pushStyle();
        fill(255,0,0);
        render.drawFaces(meshcopy);
        popStyle();

        for(HE_Mesh mesh:floormeshes){
            render.drawFaces(mesh);
        }
        for(HE_Mesh mesh:groundmeshes){
            render.drawFaces(mesh);
        }
        for(HE_Mesh mesh:archmeshes){
            render.drawFaces(mesh);
        }
        for(HE_Mesh mesh:detailsmeshes){
            render.drawFaces(mesh);
        }
        for(HE_Mesh mesh:woodmeshes){
            render.drawFaces(mesh);
        }
    }

    void importblabla(String file){
        IG.init();
        IG.open(file);
        System.out.println("IG OPEN FILE");

        if (IG.layer("Volumn").meshes().length > 0) {
            IMesh[] details = IG.layer("Volumn").meshes();
            System.out.println("--------------" + details.length);
            volumnmeshes.add(toHE_Mesh(details[0]));
        }

        if (IG.layer("Floor").breps().length > 0){
            IMesh[] details = IG.layer("Floor").meshes();
            System.out.println("--------------"+details.length);
            detailsmeshes.add(toHE_Mesh(details[0]));
        }

        if (IG.layer("Arch").breps().length > 0){
            IMesh[] details = IG.layer("Arch").meshes();
            System.out.println("--------------"+details.length);
            detailsmeshes.add(toHE_Mesh(details[0]));
        }


        if (IG.layer("Details").meshes().length > 0) {
            IMesh[] details = IG.layer("Details").meshes();
            System.out.println("--------------"+details.length);
            detailsmeshes.add(toHE_Mesh(details[0]));
        }

        System.out.println("finish other");
        if (IG.layer("Wood").meshes().length > 0) {
            IMesh[] details = IG.layer("Wood").meshes();
            System.out.println("--------------"+details.length);
            woodmeshes.add(toHE_Mesh(details[0]));
        }

    }

    public static HE_Mesh toHE_Mesh(IMesh iMesh) {
        List<IVertex> vertices = iMesh.vertices();
        List<WB_Point> pts = new ArrayList<>();

        for (IVertex vertex : vertices) {
            WB_Point pt = new WB_Point(vertex.x(), vertex.y(), vertex.z());
            pts.add(pt);
        }

        ArrayList<IFace> faces = iMesh.faces();
        List<int[]> faceList = new ArrayList<>();

        for (IFace face : faces) {
            IVertex[] iVers = face.vertices;

            int[]faceVers = new int[iVers.length];
            for (int i = 0; i < iVers.length; i++) {
                int id = vertices.indexOf(iVers[i]);
                faceVers[i] = id;
            }

            faceList.add(faceVers);
        }

        HEC_FromFacelist creator = new HEC_FromFacelist();
        creator.setVertices(pts);
        creator.setFaces(faceList);

        return creator.create();
    }

    private void setcam() {
        cam = new CameraController(this);
    }

}
