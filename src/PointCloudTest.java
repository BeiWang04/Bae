import peasy.PeasyCam;
import processing.core.PApplet;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
import wblut.geom.WB_RandomInSphere;
import wblut.geom.WB_RandomPoint;
import wblut.hemesh.*;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

public class PointCloudTest extends PApplet {
    PeasyCam peasyCam;
    WB_Render render;
    HE_Mesh container;
    WB_Point[] points;
    int numpoints;

    HE_MeshCollection cells;
    int numcells;
    HE_Mesh fusedcells;
    HE_Selection sel;
    HE_Mesh mesh;

    public static void main(String[] args) {
        PApplet.main("PointCloudTest");
    }

    public void setup() {
        initialize();
        numpoints = 10;
        createContainer();
        createMesh();
        for(HE_Mesh cell:cells)HET_MeshOp.fuseCoplanarFaces(cell, 0.1);
        mesh = subdivideCellMid(cells.getMesh(0));
        System.out.println(mesh.getHalfedges().size());
    }

    public void draw() {
        background(0);
        directionalLight(255, 255, 255, 1, 1, -1);
        directionalLight(127, 127, 127, -1, -1, 1);

        strokeWeight(1);
        stroke(255);
        //render.drawEdges(fusedcells);
        noStroke();
        stroke(255);
        fill(200,200);
        //noFill();
        //for(HE_Mesh cell:cells)render.drawFaces(cell);
        //render.drawFaces(cells.getMesh(0));
        //render.drawFaces(fusedcells);
        fill(255, 0, 0);
        stroke(255,0,0);
        strokeWeight(1);
        render.drawEdges(mesh);
        render.drawFaces(mesh);
        //render.drawVertices(sel, 10);


    }

    void initialize() {
        size(1000, 1000, P3D);
        peasyCam = new PeasyCam(this, 2000);
        render = new WB_Render(this);
    }


    void createContainer() {
        container = new HE_Mesh(new HEC_Geodesic().setB(2).setC(0).setRadius(420));
        HE_FaceIterator fitr = container.fItr();
        while (fitr.hasNext()) {
            fitr.next().setColor(color(0, 200, 50));
        }
    }

    HE_Mesh subdivideCellMid(HE_Mesh mesh) {
        List<Integer>check=new ArrayList<>();
        HE_Mesh newMesh=new HE_Mesh();
        List<HE_Vertex>pts=new ArrayList<>();
            List<HE_Face> faces = mesh.getFaces();
            for(int i=0;i<faces.size();i++) {
                HE_Face f = faces.get(i);
                f.setUserLabel(i);
                HE_Vertex v = new HE_Vertex(f.getFaceCenter());
                newMesh.add(v);
                pts.add(v);
                check.add(i);
            }
               for(int i=0;i<faces.size();i++) {
                   HE_Face f = faces.get(i);
                   HE_Vertex v = pts.get(i);
                   List<HE_Face> neighborFaces = new ArrayList<>();
                   List<HE_Halfedge> faceHalfedges = f.getFaceHalfedges();
                   for(HE_Halfedge h:faceHalfedges){
                       neighborFaces.add(h.getPair().getFace());
                   }



                   for (HE_Face neigh : neighborFaces) {
                       if(check.contains(f.getUserLabel())&&check.contains(neigh.getUserLabel())) {
                           HE_Vertex nei = pts.get(neigh.getUserLabel());
                           HE_Halfedge h = new HE_Halfedge();
                           HE_Halfedge he = new HE_Halfedge();
                           newMesh.add(h);
                           newMesh.add(he);
                           newMesh.setVertex(h, v);
                           newMesh.setVertex(he, nei);
                           newMesh.setPair(h,he);
                       }
                   }
                   check.remove(check.indexOf(f.getUserLabel()));
               }
               HEM_Lattice modifier=new HEM_Lattice().setDepth(10).setWidth(5);
               newMesh.modify(modifier);
        //List<HE_Halfedge> halfedges = newMesh.getHalfedges();

               return newMesh;
    }

    void createMesh() {

        // generate points
        points = new WB_Point[numpoints];
        WB_RandomPoint generator = new WB_RandomInSphere().setRadius(400);
        for (int i = 0; i < numpoints; i++) {
            points[i] = generator.nextPoint();
        }

        // generate voronoi cells
        HEMC_VoronoiCells multiCreator = new HEMC_VoronoiCells().setPoints(points).setContainer(container).setOffset(0);
        cells = multiCreator.create();

        //color the cells
        int counter = 0;
        HE_MeshIterator mItr = cells.mItr();
        HE_Mesh m;
        while (mItr.hasNext()) {
            m = mItr.next();
            m.setFaceColorWithOtherInternalLabel(color(255 - 2 * counter, 220, 2 * counter), -1);
            counter++;
        }

        numcells = cells.size();
        boolean[] isCellOn = new boolean[numcells];
        for (int i = 0; i < numcells; i++) {
            isCellOn[i] = isActive(i);
            //isCellOn[i] = true;
        }
        mesh = cells.getMesh(0);



        //build new mesh from active cells

        HEC_FromVoronoiCells creator = new HEC_FromVoronoiCells().setCells(cells).setActive(isCellOn);

        fusedcells = new HE_Mesh(creator);
        /**熔断共面*/
        //HET_MeshOp.fuseCoplanarFaces(fusedcells, 0.1);
        sel = HET_Fixer.selectVerticesWithDegree(2, fusedcells);
        //fusedcells.validate();

/*
  //clean-up mesh by joining fragmented faces back together. This does not always work
  HE_Mesh tmp=fusedcells.get();

  try {
    fusedcells.fuseCoplanarFaces(0.1);
  }
  catch(final Exception ex) {
    //oops HE_Mesh messed up, retreat!
    ex.printStackTrace();
    fusedcells=tmp;
  }
fusedcells.triangulate(fusedcells.selectFacesWithOtherInternalLabel("inner", -1));
fusedcells.getSelection("inner").subdivide(new HES_CatmullClark(),2);
*/
    }

    boolean isActive(int i) {
        WB_Coord point = points[i];

        float r2 = (float) WB_Point.getSqLength(point);
        float zcutoff = 50 + 0.0025f * r2;

        return abs(point.xf()) < 50 || (abs(point.xf()) > 150 && abs(point.xf()) < 250) || (abs(point.xf()) > 350);

    }

}
