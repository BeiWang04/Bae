import igeo.*;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.hemesh.HEC_FromFacelist;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

public class ImportObj extends PApplet {

    HE_Mesh importedmMesh;
    ArrayList<HE_Mesh> volumnmeshes =new ArrayList<>();

    public ImportObj(String filePath){
        importedmMesh=new HE_Mesh();
        importSth(filePath);
        for(HE_Mesh mesh:volumnmeshes){
            importedmMesh.add(mesh);
        }

    }
    public HE_Mesh getObj(){
        return importedmMesh;
    }

    void drawOriginalMesh(PApplet app,WB_Render render){
        app.fill(255);
        render.drawFaces(volumnmeshes);
        render.drawEdges(volumnmeshes);
    }

    void importSth(String file){
        IG.init();
        IG.open(file);
        System.out.println("IG OPEN FILE");

        if (IG.layer("Volumn").meshes().length > 0) {
            IMesh[] volumns = IG.layer("Volumn").meshes();
            System.out.println("--------------" + volumns.length);
            volumnmeshes.add(toHE_Mesh(volumns[0]));
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
}
