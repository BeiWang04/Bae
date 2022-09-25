import gurobi.*;
import peasy.PeasyCam;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.hemesh.HEC_FromTriangulation;
import wblut.hemesh.HE_Halfedge;
import wblut.hemesh.HE_Mesh;
import wblut.hemesh.HE_Vertex;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

public class GurobiTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("GurobiTest");
    }

    double moveRange = 50;//可移动阈值


    HE_Mesh mesh;
    WB_Render render;
    PeasyCam peasyCam;

    int pointsNum;//顶点数量
    int[][] edgesAsInt;//边对应的顶点index
    HE_Halfedge[] edges;//边
    double[] edgeLength;//储存边长
    List<Integer> fixedPointsIndex;//锚固点
    double[] targetLength;//目标值

    public void setup() {
        size(1000, 1000, P3D);
        render = new WB_Render(this);
        peasyCam=new PeasyCam(this,200);

        createMesh();
        setFixedPointsIndex();
        initialize(20);

    }

    public void keyReleased() {
        if (key == 'A') optimize();
    }

    public void draw() {
        background(55);
        directionalLight(255, 255, 255, 1, 1, -1);
        directionalLight(127, 127, 127, -1, -1, 1);
        translate(width / 2, height / 2);
        stroke(0);
        render.drawEdges(mesh);
        noStroke();
        render.drawFaces(mesh);
    }

    void setFixedPointsIndex() {
        fixedPointsIndex=new ArrayList<>();
        for(int i=0;i<20;i++){
        fixedPointsIndex.add((int)random(pointsNum));
        }
    }

    void optimize() {
        double[][] moveLength = new double[pointsNum][3];
        try {
            GRBEnv env = new GRBEnv();
            GRBModel model = new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, "Triangles");

            GRBQuadExpr qxp;
            GRBQuadExpr qxp2;
            GRBLinExpr lxp;

            //对每个顶点V的x,y,z坐标,的移动值建立变量 0：x 1:y 2:z ; 并设置锚固点
            GRBVar[][] Vmove = new GRBVar[pointsNum][3];
            for (int i = 0; i < pointsNum; i++) {
                for (int j = 0; j < 3; j++) {
                    Vmove[i][j] = model.addVar(-moveRange, moveRange, 0, GRB.CONTINUOUS, "move");
                    //设置锚固点
                    if (fixedPointsIndex.contains(i)) {
                        lxp = new GRBLinExpr();
                        lxp.addTerm(1, Vmove[i][j]);
                        model.addConstr(lxp, GRB.EQUAL, 0, "CONS");
                    }
                }
            }

            //目标：计算每条边的新长度，与给定值比较(50)，总差最小
            qxp = new GRBQuadExpr();
            for (int i = 0; i < edges.length; i++) {
                qxp2=new GRBQuadExpr();

                int idA = edgesAsInt[i][0];
                int idB = edgesAsInt[i][1];
                HE_Vertex a = mesh.getVertexWithIndex(idA);
                HE_Vertex b = mesh.getVertexWithIndex(idB);

                qxp2.addConstant(-targetLength[edges[i].getUserLabel()]*targetLength[edges[i].getUserLabel()]);
                qxp2.addConstant(Math.pow(a.xf(), 2) + Math.pow(b.xf(), 2) - 2 * a.xf() * b.xf() );
                qxp2.addTerm(1, Vmove[idB][0], Vmove[idB][0]);
                qxp2.addTerm(1, Vmove[idA][0], Vmove[idA][0]);
                qxp2.addTerm(2 * b.xf(), Vmove[idB][0]);
                qxp2.addTerm(-2 * b.xf(), Vmove[idA][0]);
                qxp2.addTerm(-2 * a.xf(), Vmove[idB][0]);
                qxp2.addTerm(-2, Vmove[idA][0], Vmove[idB][0]);
                qxp2.addTerm(2 * a.xf(), Vmove[idA][0]);

                qxp2.addConstant(Math.pow(a.yf(), 2) + Math.pow(b.yf(), 2) - 2 * a.yf() * b.yf());
                qxp2.addTerm(1, Vmove[idB][1], Vmove[idB][1]);
                qxp2.addTerm(1, Vmove[idA][1], Vmove[idA][1]);
                qxp2.addTerm(2 * b.yf(), Vmove[idB][1]);
                qxp2.addTerm(-2 * b.yf(), Vmove[idA][1]);
                qxp2.addTerm(-2 * a.yf(), Vmove[idB][1]);
                qxp2.addTerm(-2, Vmove[idA][1], Vmove[idB][1]);
                qxp2.addTerm(2 * a.yf(), Vmove[idA][1]);

                qxp2.addConstant(Math.pow(a.zf(), 2) + Math.pow(b.zf(), 2) - 2 * a.zf() * b.zf());
                qxp2.addTerm(1, Vmove[idB][2], Vmove[idB][2]);
                qxp2.addTerm(1, Vmove[idA][2], Vmove[idA][2]);
                qxp2.addTerm(2 * b.zf(), Vmove[idB][2]);
                qxp2.addTerm(-2 * b.zf(), Vmove[idA][2]);
                qxp2.addTerm(-2 * a.zf(), Vmove[idB][2]);
                qxp2.addTerm(-2, Vmove[idA][2], Vmove[idB][2]);
                qxp2.addTerm(2 * a.zf(), Vmove[idA][2]);

                qxp.add(qxp2);

                model.addQConstr(qxp2,GRB.GREATER_EQUAL,0,"cons");

            }

            model.setObjective(qxp, GRB.MINIMIZE);
            model.set(GRB.DoubleParam.TimeLimit, 200);
            //非凸二次问题求解
            model.set(GRB.IntParam.NonConvex, 2);
            model.optimize();

            //保存移动结果
            for (int i = 0; i < pointsNum; i++) {
                for (int j = 0; j < 3; j++) {
                    moveLength[i][j] = Vmove[i][j].get(GRB.DoubleAttr.X);
                }
            }

            model.dispose();
            env.dispose();

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }

        //改变mesh
        for (int i = 0; i < pointsNum; i++) {
            WB_Transform3D transform3D = new WB_Transform3D();
            transform3D.addTranslate(new WB_Point(moveLength[i][0], moveLength[i][1], moveLength[i][2]));
            HE_Vertex vertex = mesh.getVertexWithIndex(i);
            WB_Point apply = vertex.getPosition().apply(transform3D);
            vertex.set(apply);
        }

        for (int i=0;i<edges.length;i++) {
            double v = targetLength[edges[i].getUserLabel()] - edges[i].getLength();
            System.out.println("边"+i+"的原来长度是"+edgeLength[i]+",现在长度是"+edges[i].getLength()+",与目标边长相差"+ v);}
    }

    void createMesh() {
        WB_RandomPoint source;
        WB_Point[] points;
        int numPoints;

        source=new WB_RandomRectangle().setSize(800, 800);
        numPoints=20;
        points=new WB_Point[numPoints];
        for (int i=0; i<numPoints; i++) {
            points[i]=source.nextPoint();
        }
        WB_Triangulation2D triangulation= WB_Triangulate.triangulate2D(points);

        HEC_FromTriangulation creator= new HEC_FromTriangulation();
        creator.setTriangulation(triangulation);
        creator.setPoints(points);
        mesh=new HE_Mesh(creator);

        //mesh = new HE_Mesh(creator);
        pointsNum = mesh.getVertices().size();
    }

    void initialize(int segMent) {
        edgesAsInt = mesh.getEdgesAsInt();
        edges = mesh.getEdgesAsArray();
        fixedPointsIndex = new ArrayList<>();
        edgeLength=new double[edges.length];
        for (int i=0;i<edges.length;i++) {edges[i].setUserLabel(-1);edgeLength[i]=edges[i].getLength();}
        //getLengthSegmentation(segMent);
        getNumSegmentation(segMent);
    }

    //测试，按照长度来平均分段
    void getLengthSegmentation(int segment) {
        double[] evaluation = new double[segment];
        double minLength = Double.MAX_VALUE;
        double maxLength = Double.MIN_VALUE;

        for (int i = 0; i < edges.length; i++) {
            double length = edges[i].getLength();
            if (length < minLength) minLength = length;
            if (length > maxLength) maxLength = length;
        }

        evaluation[0] = minLength;
        for (int i = 1; i < evaluation.length; i++) {
            evaluation[i] = evaluation[i - 1] + (maxLength - minLength) / segment;
        }
        evaluation[segment - 1] = maxLength + 1;

        for (int i = 0; i < edges.length; i++) {
            double length = edges[i].getLength();
            for (int j = 0; j < segment - 1; j++) {
                if (evaluation[j] <= length && length < evaluation[j + 1]) edges[i].setUserLabel(j);
            }
        }
        targetLength=new double[segment-1];
        for(int i=0;i<targetLength.length;i++){
            targetLength[i]=evaluation[i];
        }
    }

    //排序
    void bubbleSort(int segment) {
        HE_Halfedge[]arr=edges.clone();
        for (int i = 0; i < arr.length - 1; i++) {//外层循环控制排序趟数
            for (int j = 0; j < arr.length - 1 - i; j++) {//内层循环控制每一趟排序多少次
                if (arr[j].getLength()-(arr[j + 1].getLength()) > 0) {
                    HE_Halfedge temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }

        int seg = (int)Math.floor(arr.length / (segment - 1));

        for(int i=0;i<arr.length;i++){
            for(int j=0;j<segment-1;j++){

            if((int)Math.floor(i/seg)==j)
                arr[i].setUserLabel(j);
            }
            if(i>=seg*(segment-1))arr[i].setUserLabel(segment-1);
        }

        for(HE_Halfedge he_halfedge:arr) System.out.println(he_halfedge.getUserLabel());
    }

    //测试，按照数量来平均分段
    void getNumSegmentation(int segment) {
        bubbleSort(segment);
        targetLength=new double[segment];
        for(int i=0;i<segment;i++)targetLength[i]=Double.MAX_VALUE;
        for(int i=0;i<targetLength.length;i++){
            for(int j=0;j<edges.length;j++){
                if(edges[j].getLength()<targetLength[edges[j].getUserLabel()]){
                targetLength[edges[j].getUserLabel()]=edges[j].getLength();
                }
            }
        }
    }

}
