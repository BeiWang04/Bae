import peasy.PeasyCam;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.hemesh.*;
import wblut.processing.WB_Render3D;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DelaunayTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("DelaunayTest");
    }

    double acceptableVariation = 5;

    WB_Render3D render;
    PeasyCam peasyCam;
    WB_Point[] points;
    int numPoints;
    boolean[][]intersection;

    int count = 0;
    HE_Mesh mesh;

    HE_Mesh newMesh;
    int kind;

    Color intersect=new Color(143, 119, 182);
    public void setup() {
        size(1200, 1200, P3D);

        this.peasyCam = new PeasyCam(this, 200);

        render = new WB_Render3D(this);

        numPoints = 20;
        points = new WB_Point[numPoints];
        for (int i = 0; i < numPoints; i++) {
            points[i] = new WB_Point(random(500), random(500), 0);
        }

        mesh = getVoronoi(points);
        newMesh = getDelaunay(points);
        kind = getType(newMesh, acceptableVariation);
        intersection=new boolean[newMesh.getFaces().size()][newMesh.getFaces().size()];

        /*
        WB_Polygon tri1 = new WB_Polygon(new WB_Point(0, 0, 0), new WB_Point(100, 0, 0), new WB_Point(0, 100, 0));
        WB_Polygon tri2 = new WB_Polygon(new WB_Point(0, 0, 0), new WB_Point(100, 0, 0), new WB_Point(50, -20, 0));
        test=new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{tri1,tri2}));
        bounce(test);
        //test.add(new HE_Mesh(new HEC_Polygon(tri1)));
        System.out.println(test.getFaces().get(0).getNeighborFaces().size());
        */


    }

    public void draw() {
        background(255);
        stroke(0);
        strokeWeight(0.5f);


        render.drawEdges(mesh);

        strokeWeight(2);
        render.drawEdges(newMesh);
        disPlayHeMesh(newMesh, 5);

        fill(0);


        //for(HE_Halfedge he_halfedge:newMesh.getEdgesAsArray())text(he_halfedge.getUserLabel(),he_halfedge.getCenter().xf(),he_halfedge.getCenter().yf());

        text("Count : " + count + "   Num : " + newMesh.getEdgesAsArray().length + "   acceptableVariation : " + acceptableVariation + "   Type : " + kind, 0, 520);

        for(int i=0;i<intersection.length;i++){
            for(int j=i+1;j<intersection[i].length;j++){
                if(intersection[i][j]==true){
                    noStroke();
                    fill(intersect.getRGB(),100);
                    render.drawFace(newMesh.getFaceWithIndex(i));
                    render.drawFace(newMesh.getFaceWithIndex(j));
                }
            }
        }

        strokeWeight(0.5f);
        Color color = new Color(236, 135, 167);
        stroke(color.getRGB());
        noFill();
        render.drawAABB(newMesh.getAABB());
        for (int i = 0; i < newMesh.getFaces().size(); i++) {
            text(i, newMesh.getFaces().get(i).getFaceCenter().xf(), newMesh.getFaces().get(i).getFaceCenter().yf());
        }




    }

    public void keyReleased() {
        if (key == 'A') {
            count++;
            newMesh = getLaplacian(newMesh, true);
            kind = getType(newMesh, acceptableVariation);
            bounce(newMesh);
        }
        if (key == 'B') {
            count++;
            WB_Point[] wb_points = updateVoronoi(mesh);
            mesh = getVoronoi(wb_points);
            newMesh = getDelaunay(wb_points);
            kind = getType(newMesh, acceptableVariation);
        }
        if (key == 'C') {
            count++;
            newMesh = getTaubin(newMesh);
            kind = getType(newMesh, acceptableVariation);

        }
        //if(key=='C')HET_Export.saveAsHemesh(mesh);
    }

    private void bounce(HE_Mesh mesh) {
        for(int i=0;i<intersection.length;i++){
            for(int j=0;j<intersection.length;j++)intersection[i][j]=false;
        }

        List<HE_Face> faces = mesh.getFaces();
        for(int i=0;i<faces.size();i++){faces.get(i).setUserLabel(i);}
        for (int i = 0; i < faces.size(); i++) {


            List<HE_Vertex> faceVertices = faces.get(i).getFaceVertices();
            List<HE_Face> neighborFaces = new ArrayList<>();

            List<HE_Face>coEdgeFaces=faces.get(i).getNeighborFaces();
            List<HE_Face>coVertexFaces=new ArrayList<>();
            neighborFaces.addAll(coEdgeFaces);
            for(HE_Vertex v:faceVertices){
                neighborFaces.addAll(v.getFaceStar());
                coVertexFaces.addAll(v.getFaceStar());
            }

            //???????????????????????????
            for (int j = 0; j < faces.size(); j++) {
                boolean is = false;

                if (j != i) {
                    //?????????????????????
                    if (!neighborFaces.contains(faces.get(j))) {
                        if (intersectionTest.intersectionTri(faces.get(i).getPolygon(), faces.get(j).getPolygon())) {
                            is = true;

                        }

                    }
                }
                if(is){
                    intersection[i][j]=true;
                    //System.out.println("Face"+i+"???Face"+j+"??????");}
                }}

            //?????????
            for(HE_Halfedge e:faces.get(i).getFaceHalfedges()) {
                if (e.getPair().getFace() != null) {
                    boolean is = false;
                    HE_Face face = e.getPair().getFace();

                    List<HE_Vertex> faceVertices1 = face.getFaceVertices();


                    List<HE_Vertex> vjs = new ArrayList<>();
                    vjs.addAll(faceVertices1);
                    vjs.remove(e.getStartVertex());
                    vjs.remove(e.getEndVertex());
                    HE_Vertex vj = vjs.get(0);

                    List<HE_Vertex> vis = new ArrayList<>();
                    vis.addAll(faceVertices);
                    vis.remove(e.getStartVertex());
                    vis.remove(e.getEndVertex());
                    HE_Vertex vi = vis.get(0);

                    //?????????
                    if (WB_GeometryOp.getDistance3D(vj.getPosition(), faces.get(i).getPlane()) == 0) {
                        //System.out.println("VV"+face.getUserLabel());
                        //j???i??????i???j???
                        if (WB_GeometryOp.getDistance3D(vj.getPosition(), faces.get(i).getPolygon()) == 0 ||
                                WB_GeometryOp.getDistance3D(vi.getPosition(), face.getPolygon()) == 0
                        ) {
                            is = true;
                        } else {
                            //i1???j2?????????
                            //System.out.println(vi.getPosition());
                            //System.out.println(vj.getPosition());
                            //System.out.println(e.getStartPosition());
                            //System.out.println(e.getEndPosition());
                            WB_Segment i1 = new WB_Segment(vi.getPosition(), e.getStartPosition());
                            WB_Segment j1 = new WB_Segment(vj.getPosition(), e.getStartPosition());
                            WB_Segment i2 = new WB_Segment(vi.getPosition(), e.getEndPosition());
                            WB_Segment j2 = new WB_Segment(vj.getPosition(), e.getEndPosition());
                            //System.out.println((WB_GeometryOp.getDistance3D(i1,j2)));
                            //System.out.println((WB_GeometryOp.getDistance3D(i2,j1)));
                            if (WB_GeometryOp.getDistance3D(i1, j2) < 0.5f || WB_GeometryOp.getDistance3D(i2, j1) < 0.5f) {
                                //System.out.println("aaaaaaa");
                                is = true;
                            }
                        }
                    }
                    if (is) {
                        intersection[i][face.getUserLabel()] = true;
                        //System.out.println("Face"+i+"???Face"+face.getUserLabel()+"??????");}
                    }
                }}

            //?????????????????????
            for (HE_Vertex v : faceVertices) {
                List<HE_Face> faceStar = v.getFaceStar();
                for (HE_Face face : faceStar) {
                    boolean is = false;
                    //??????????????????
                    if (face != faces.get(i) && !coEdgeFaces.contains(face)) {
                        List<HE_Vertex> faceVertices1 = face.getFaceVertices();
                        List<HE_Vertex> seg1 = new ArrayList<>();

                        seg1.addAll(faceVertices);
                        seg1.remove(v);
                        WB_Segment si = new WB_Segment(seg1.get(0), seg1.get(1));
                        seg1 = new ArrayList<>();
                        seg1.addAll(faceVertices1);
                        seg1.remove(v);
                        WB_Segment sj = new WB_Segment(seg1.get(0), seg1.get(1));

                        //????????????????????????????????????
                        for (HE_Vertex vertex : faceVertices1) {
                            if (!faceVertices.contains(vertex)) {
                                if (WB_GeometryOp.getDistance3D(vertex.getPosition(), faces.get(i).getPolygon()) == 0)
                                    is = true;
                            }
                        }
                        //???????????????????????????????????????
                        for (HE_Vertex vertex : faceVertices) {
                            if (!faceVertices1.contains(vertex)) {
                                if (WB_GeometryOp.getDistance3D(vertex.getPosition(), face.getPolygon()) == 0) {
                                    is = true;
                                }

                            }
                        }
                        //?????????????????????????????????????????????
                        if (intersectionTest.intersection(si, face.getPolygon()) || intersectionTest.intersection(sj, faces.get(i).getPolygon())) {
                            is = true;
                        }

                    }
                    if (is) {
                        intersection[i][face.getUserLabel()] = true;
                        //System.out.println("Face"+i+"???Face"+face.getUserLabel()+"??????");}
                    }
                }


            }


        }

        for(int i=0;i<intersection.length;i++){
            for(int j=i+1;j<intersection.length;j++){
                if(intersection[i][j]) System.out.println("Face"+i+"???Face"+j+"??????");}
        }
    }





    /**
     * ????????? ??????
     */
    private WB_Point[] updateVoronoi(HE_Mesh voronoi) {
        for (int i = 0; i < points.length; i++) {
            for (HE_Face f : voronoi.getFaces()) {
                if (WB_GeometryOp.contains2D(points[i], f.getPolygon())) {
                    f.setUserLabel(i);
                    break;
                }
            }
        }
        WB_Point[] newPoints = points.clone();
        for (HE_Face f : voronoi.getFaces()) {
            newPoints[f.getUserLabel()] = new WB_Point(f.getFaceCenter());
        }
        points = newPoints.clone();
        return points;
    }


    private HE_Mesh getTaubin(HE_Mesh delaunay) {
        WB_AABB aabb = delaunay.getAABB();
        //WB_AABB
        HEM_TaubinSmooth modifier = new HEM_TaubinSmooth();
        //modifier.setAutoRescale(true);
        //modifier.setKeepBoundary(true);
        delaunay.modify(modifier);
        fitInAABB2D(delaunay, aabb);
        //System.out.println("AAAA"+delaunay.getVertices().size());
        //System.out.println("AAAA"+delaunay.getMeanEdgeLength());
        return delaunay;
    }

    private HE_Mesh getCenter(HE_Mesh voronoi) {

        for (int i = 0; i < points.length; i++) {
            for (HE_Face f : voronoi.getFaces()) {
                if (WB_GeometryOp.contains2D(points[i], f.getPolygon())) {
                    f.setUserLabel(i);
                    break;
                }
            }
        }
        WB_Point[] newPoints = points.clone();
        for (HE_Face f : voronoi.getFaces()) {
            newPoints[f.getUserLabel()] = new WB_Point(f.getFaceCenter());
        }
        //HE_Mesh voronoi1 = getVoronoi(newPoints);
        return getDelaunay(newPoints);

        //return delaunay;
    }

    /**
     * ??????????????? ????????????
     */
    private HE_Mesh getLaplacian(HE_Mesh delaunay, boolean autoRescale) {
        WB_AABB aabb = delaunay.getAABB();
        List<HE_Halfedge> edges = delaunay.getEdges();
        double averageLength = 0;
        for (HE_Halfedge he_halfedge : edges) averageLength += he_halfedge.getLength();
        averageLength /= edges.size();
        System.out.println("averageLength" + averageLength);
        List<WB_Vector> moves = new ArrayList<>();
        List<HE_Vertex> vertices = delaunay.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            WB_Vector move = new WB_Vector();
            HE_Vertex vertex = vertices.get(i);
            List<HE_Halfedge> halfedgeStar = vertex.getHalfedgeStar();
            for (HE_Halfedge edge : halfedgeStar) {
                WB_Coord center = edge.getCenter();
                WB_Vector force = new WB_Vector(new WB_Point(vertex.xf() - center.xf(), vertex.yf() - center.yf(),
                        vertex.zf() - center.zf()));
                double length = force.getLength();

                force.normalizeSelf();

                //vertex.getVertexNormal()
                force.mulSelf((averageLength - length * 2) / (halfedgeStar.size()));
                move.addSelf(force);
            }
            moves.add(move);
        }
        WB_Transform3D transform3D;

        for (int i = 0; i < vertices.size(); i++) {
            transform3D = new WB_Transform3D().addTranslate(moves.get(i));
            //System.out.println(moves.get(i).getLength());
            WB_Point apply = vertices.get(i).getPosition().apply(transform3D);
            //if(!vertices.get(i).isBoundary())
            vertices.get(i).set(apply);
            //System.out.println(111);
        }

        if (autoRescale) {
            fitInAABB2D(delaunay, aabb);
        }
        return delaunay;
    }

    public void fitInAABB2D(HE_Mesh mesh, final WB_AABB AABB) {
        final WB_AABB self = HE_MeshOp.getAABB(mesh);
        mesh.moveSelf(new WB_Vector(self.getMin(), AABB.getMin()));
        mesh.scaleSelf(AABB.getWidth() / self.getWidth(),
                AABB.getHeight() / self.getHeight(),
                1, new WB_Point(AABB.getMin()));
    }

    /**
     * ???????????????????????????
     * <p>
     * He SiYuan
     */
    public void disPlayHeMesh(HE_Mesh mesh, double r) {
        displayHalfEdges(mesh);
        displayHeVertices(mesh, r);
    }


    public void displayHeVertices(HE_Mesh mesh, double r) {
        pushStyle();
        Color color = new Color(205, 237, 159);
        noStroke();
        fill(color.getRGB());
        for (HE_Vertex v : mesh.getVertices()) {
            pushMatrix();
            translate(v.xf(), v.yf(), v.zf());
            sphere((float) r);
            popMatrix();
        }

        popStyle();
    }

    public void displaySingleHeVertex(HE_Vertex v, Color color) {
        pushStyle();
        fill(color.getRGB());
        pushMatrix();
        translate(v.xf(), v.yf(), v.zf());
        sphere(50);
        popMatrix();
        popStyle();
    }

    public void displayHalfEdges(HE_Mesh mesh) {
        pushStyle();
        Color color = new Color(95, 199, 196);
        stroke(color.getRGB());
        strokeWeight(2);
        for (HE_Halfedge he : mesh.getHalfedges()) {
            double offsetDis = he.getLength() / 100;
            stroke(color.getRGB());
            strokeWeight(2);
            HE_Face referFace = he.isOuterBoundary() ? he.getPair().getFace() : he.getFace();

            WB_Vector vec = new WB_Vector(he.getVertex(), he.getEndVertex());
            WB_Vector v = (WB_Vector) he.getHalfedgeDirection();
            WB_Vector v_ortho = v.rotateAboutAxis(90 * DEG_TO_RAD, new WB_Point(0, 0, 0), referFace.getFaceNormal()).mul(offsetDis);
            WB_Point ps = he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.25));
            WB_Point pe = he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.75));
            WB_Vector v_arrow = v.rotateAboutAxis(150 * DEG_TO_RAD, new WB_Point(0, 0, 0), referFace.getFaceNormal()).mul(he.getLength() / 50);

            WB_Point p_arrow = pe.add(v_arrow);
            line(ps.xf(), ps.yf(), ps.zf(), pe.xf(), pe.yf(), pe.zf());
            line(pe.xf(), pe.yf(), pe.zf(), p_arrow.xf(), p_arrow.yf(), p_arrow.zf());
        }

        popStyle();
    }

    public void displaySingleHalfEdge(HE_Halfedge he, Color color) {
        double offsetDis = he.getLength() / 100;
        pushStyle();
        stroke(color.getRGB());
        strokeWeight(2);
        HE_Face referFace = he.isOuterBoundary() ? he.getPair().getFace() : he.getFace();

        WB_Vector vec = new WB_Vector(he.getVertex(), he.getEndVertex());
        WB_Vector v = (WB_Vector) he.getHalfedgeDirection();
        WB_Vector v_ortho = v.rotateAboutAxis(90 * DEG_TO_RAD, new WB_Point(0, 0, 0), referFace.getFaceNormal()).mul(offsetDis);
        WB_Point ps = he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.25));
        WB_Point pe = he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.75));
        WB_Vector v_arrow = v.rotateAboutAxis(150 * DEG_TO_RAD, new WB_Point(0, 0, 0), referFace.getFaceNormal()).mul(he.getLength() / 50);

        WB_Point p_arrow = pe.add(v_arrow);
        line(ps.xf(), ps.yf(), ps.zf(), pe.xf(), pe.yf(), pe.zf());
        line(pe.xf(), pe.yf(), pe.zf(), p_arrow.xf(), p_arrow.yf(), p_arrow.zf());
        popStyle();
    }

    private HE_Mesh getDelaunay(WB_Point[] points) {
        /**
         for(int i=0;i<points.length;i++){
         for(HE_Face f:voronoi.getFaces()){
         if(WB_GeometryOp.contains2D(points[i],f.getPolygon())){f.setUserLabel(i);break;}
         }
         }
         HE_Mesh delaunay=new HE_Mesh();
         List<WB_Polygon>tris=new ArrayList<>();
         List<HE_Face> faces = voronoi.getFaces();
         for(int m=0;m<faces.size();m++) {
         HE_Face f=faces.get(m);
         List<HE_Face> neighborFaces = f.getNeighborFaces();
         if (neighborFaces.size() > 1) {
         for (int i = 0; i < neighborFaces.size(); i++) {
         HE_Face faceI = neighborFaces.get(i);
         List<HE_Face> neighborFacesI = faceI.getNeighborFaces();
         for (int j = i + 1; j < neighborFaces.size();j++){
         HE_Face faceJ = neighborFaces.get(j);
         if(neighborFacesI.contains(faceJ)){
         tris.add(new WB_Polygon(points[f.getUserLabel()],points[faceI.getUserLabel()],
         points[faceJ.getUserLabel()]));
         }
         }
         }
         }
         }
         HEC_FromPolygons ps=new HEC_FromPolygons().setPolygons(tris);
         delaunay=new HE_Mesh(ps);
         //return delaunay;
         */
        WB_Triangulation2D triangulation = WB_Triangulate.triangulate2D(points);
        HEC_FromTriangulation TRI = new HEC_FromTriangulation().setTriangulation(triangulation).setPoints(points);
        return new HE_Mesh(TRI);
    }

    private HE_Mesh getVoronoi(WB_Point[] points) {

//        public HE_Mesh setVoronoi(List<WB_Point> centers, WB_Polygon poly) {
//        HE_Mesh meshp;

        WB_Polygon boundary = new WB_Polygon(new WB_Point(0, 0), new WB_Point(500, 0), new WB_Point(500, 500), new WB_Point(0, 500));
        List<WB_VoronoiCell2D> Vor = WB_VoronoiCreator.getClippedVoronoi2D(points, boundary).getCells();

        List<WB_Polygon> lp = new ArrayList<WB_Polygon>();
        for (WB_VoronoiCell2D vor : Vor) {
            lp.add(vor.getPolygon());

        }

        HEC_FromPolygons hecp = new HEC_FromPolygons();
        hecp.setPolygons(lp);

        return new HE_Mesh(hecp);
    }

    private HE_Mesh getVoronoiCells(WB_Point[] points) {

//        public HE_Mesh setVoronoi(List<WB_Point> centers, WB_Polygon poly) {
//        HE_Mesh meshp;

        WB_Polygon boundary = new WB_Polygon(new WB_Point(0, 0), new WB_Point(500, 0), new WB_Point(500, 500), new WB_Point(0, 500));
        List<WB_VoronoiCell2D> Vor = WB_VoronoiCreator.getClippedVoronoi2D(points, boundary).getCells();


        for (int i = 0; i < points.length; i++) {
            if (!points[i].equals(Vor.get(i).getCentroid())) {
                points[i] = Vor.get(i).getCentroid();
            }
        }

        Vor = WB_VoronoiCreator.getClippedVoronoi2D(points, boundary).getCells();

        List<WB_Polygon> lp = new ArrayList<WB_Polygon>();
        for (WB_VoronoiCell2D vor : Vor) {
            lp.add(vor.getPolygon());
            //vor.getPolygon().g
        }

        HEC_FromPolygons hecp = new HEC_FromPolygons();
        hecp.setPolygons(lp);
        this.points = points;

        return new HE_Mesh(hecp);
    }


    public static int getType(HE_Mesh mesh, double acceptableVariation) {
        double minLength = Double.MAX_VALUE;
        double maxLength = Double.MIN_VALUE;

        HE_Halfedge[] edges = mesh.getEdgesAsArray();
        for (int i = 0; i < edges.length; i++) {
            double length = edges[i].getLength();
            if (length < minLength) minLength = length;
            if (length > maxLength) maxLength = length;
        }

        ArrayList<Double> types = new ArrayList<>();
        double floor = (maxLength - minLength) / acceptableVariation;
        for (int i = 0; i < floor; i++) {
            types.add(minLength + i * acceptableVariation);
        }
        types.add(maxLength);


        Set<Integer> finalTypes = new HashSet<>();
        for (int i = 0; i < edges.length; i++) {
            double length = edges[i].getLength();
            for (int j = 0; j < types.size(); j++) {
                if (Math.abs(types.get(j) - length) <= acceptableVariation / 2) {
                    edges[i].setUserLabel(j);
                    finalTypes.add(j);
                    break;
                }
            }
        }


        System.out.println("??????" + finalTypes.size() + "?????????");
        ArrayList<Integer> T = new ArrayList<>();
        T.addAll(finalTypes);
        for (int i = 0; i < edges.length; i++) {

            // System.out.println("??????" + i + "????????????" + edges[i].getUserLabel() + ",?????????" + types.get(edges[i].getUserLabel()));
        }
        return finalTypes.size();
    }
}
