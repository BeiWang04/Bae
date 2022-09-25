import gurobi.*;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.hemesh.HE_Halfedge;
import wblut.hemesh.HE_Mesh;
import wblut.hemesh.HE_Vertex;
import wblut.processing.WB_Render;

import java.util.*;

public class Normalization {
    HE_Mesh mesh;
    int segment;
    double acceptableVariation;
    double moveRange;

    int num;
    double[] evaluation;

    int pointsNum;//顶点数量
    int[][] edgesAsInt;//边对应的顶点index
    HE_Halfedge[] edges;//边
    double[] edgeLength;//储存边长
    List<Integer> fixedPointsIndex;//锚固点
    double[] targetLength;//目标值
    boolean[] whether;
    List<Integer> ints;
    //所有种类的长度
    ArrayList<Double> alltypes;
    ArrayList<Double> finalTypes;
    List<WB_Point> initial;
    double currentValue = 0;
    //double[]
    int kind;
    List<Integer> TYPE;
    List<List<Integer>> types;
    int aaa;

    public Normalization(HE_Mesh mesh, int segment, double moveRange, double acceptableVariation) {
        this.mesh = mesh;
        this.segment = segment;
        this.moveRange = moveRange;
        this.acceptableVariation = acceptableVariation;
        pointsNum = mesh.getVertices().size();
        setFixedPointsIndex();
        initialize(segment);
    }

    public Normalization(HE_Mesh mesh, int segment, double moveRange, double acceptableVariation, int num) {
        this.mesh = mesh;
        this.segment = segment;
        this.moveRange = moveRange;
        this.acceptableVariation = acceptableVariation;
        pointsNum = mesh.getVertices().size();
        this.num = num;
        //System.out.println("num"+num);
        this.aaa=num+1;
        //System.out.println("aaa"+aaa);
        setFixedPointsIndex();
        initialize2();

    }

    public void optimize() {
        double[][] moveLength = new double[pointsNum][3];
        try {
            GRBEnv env = new GRBEnv();
            GRBModel model = new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, "Triangles");

            GRBQuadExpr qxp;
            GRBQuadExpr qxp2;
            GRBQuadExpr qxp3;
            GRBLinExpr lxp;

            //对每个顶点V的x,y,z坐标,的移动值建立变量 0：x 1:y 2:z ; 并设置锚固点
            GRBVar[][] Vmove = new GRBVar[pointsNum][3];
            for (int i = 0; i < pointsNum; i++) {
                for (int j = 0; j < 3; j++) {
                    Vmove[i][j] = model.addVar(-moveRange, moveRange, 0, GRB.INTEGER, "move");
                }
                //设置锚固点(z)不动
                //if (fixedPointsIndex.contains(i)) {
                    lxp = new GRBLinExpr();
                    lxp.addTerm(1, Vmove[i][2]);
                    model.addConstr(lxp, GRB.EQUAL, 0, "CONS");
                //}
            }

            //是不是呢
            GRBVar[][] normalize = new GRBVar[edges.length][this.segment - 1];
            for (int i = 0; i < edges.length; i++) {
                for (int j = 0; j < this.segment - 1; j++) {
                    normalize[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "a");
                }
            }

            //至少有一种成立
            for (int i = 0; i < edges.length; i++) {
                lxp = new GRBLinExpr();
                for (int j = 0; j < this.segment - 1; j++) {
                    lxp.addTerm(1, normalize[i][j]);
                }
                model.addConstr(lxp, GRB.GREATER_EQUAL, 1, "a");
            }


            double M = Integer.MAX_VALUE;
            //限制必须是其中一种
            //目标：计算每条边的新长度，与给定值比较，总差最小
            qxp = new GRBQuadExpr();
            for (int i = 0; i < edges.length; i++) {
                qxp2 = new GRBQuadExpr();

                int idA = edgesAsInt[i][0];
                int idB = edgesAsInt[i][1];
                HE_Vertex a = mesh.getVertexWithIndex(idA);
                HE_Vertex b = mesh.getVertexWithIndex(idB);

                int id = edges[i].getUserLabel() % 2 == 0 ? -1 : 1;

                /***/
                qxp2.addConstant(-targetLength[(int) Math.floor(edges[i].getUserLabel() / 2)] * targetLength[(int) Math.floor(edges[i].getUserLabel() / 2)]);

                qxp2.addConstant(Math.pow(a.xf(), 2) + Math.pow(b.xf(), 2) - 2 * a.xf() * b.xf());
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

                /***/
                qxp.multAdd(id, qxp2);

                /***/
                if (id == 1) {
                    model.addQConstr(qxp2, GRB.GREATER_EQUAL, 0, "cons");
                } else {
                    model.addQConstr(qxp2, GRB.LESS_EQUAL, 0, "cons");
                }

                for (int j = 0; j < segment - 1; j++) {
                    qxp3 = new GRBQuadExpr();
                    qxp3.add(qxp2);
                    qxp3.addConstant(-targetLength[j] * targetLength[j] - acceptableVariation * acceptableVariation - M);
                    qxp3.addTerm(M, normalize[i][j]);
                    /***/
                    //model.addQConstr(qxp3,GRB.LESS_EQUAL,0,"cons");

                    qxp3 = new GRBQuadExpr();
                    qxp3.add(qxp2);
                    qxp3.addConstant(-targetLength[j] * targetLength[j] + acceptableVariation * acceptableVariation + M);
                    qxp3.addTerm(-M, normalize[i][j]);
                    /***/
                    //model.addQConstr(qxp3,GRB.GREATER_EQUAL,0,"cons");
                }

            }

            /***/
//            qxp=new GRBQuadExpr();
//            for(int i=0;i<pointsNum;i++){
//                qxp.addTerm(1,Vmove[i][0],Vmove[i][0]);
//                qxp.addTerm(1,Vmove[i][1],Vmove[i][1]);
//                qxp.addTerm(1,Vmove[i][2],Vmove[i][2]);
//                qxp.addTerm(2,Vmove[i][0],Vmove[i][1]);
//                qxp.addTerm(2,Vmove[i][0],Vmove[i][2]);
//                qxp.addTerm(2,Vmove[i][1],Vmove[i][2]);
//            }


            model.setObjective(qxp, GRB.MINIMIZE);
            model.set(GRB.DoubleParam.TimeLimit, 50);
            //非凸二次问题求解
            model.set(GRB.IntParam.NonConvex, 2);
            model.set(GRB.DoubleParam.PoolGap, 0.1);
            model.optimize();
            System.out.println("SolCount:" + model.get(GRB.IntAttr.SolCount));

            //WB_GeometryOp.getIntersection3D()
            //保存移动结果
            for (int i = 0; i < pointsNum; i++) {
                for (int j = 0; j < 3; j++) {
                    moveLength[i][j] = Vmove[i][j].get(GRB.DoubleAttr.X);
                }
            }

//            for (int i=0;i<edges.length;i++) {
//                int count=0;
//                for(int j=0;j<segment-1;j++){
//                    count+=normalize[i][j].get(GRB.DoubleAttr.X);
//                }
//                System.out.println("count"+i+" = "+count);
//            }

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

        for (int i = 0; i < edges.length; i++) {
            System.out.println("边" + i + "的原来长度是" + edgeLength[i] + ",现在长度是" + edges[i].getLength());
        }


        getType(acceptableVariation);
    }

    void getType(double acceptableVariation) {
        double minLength = Double.MAX_VALUE;
        double maxLength = Double.MIN_VALUE;


        for (int i = 0; i < edges.length; i++) {
            double length = edges[i].getLength();
            System.out.println(length);
            if (length < minLength) minLength = length;
            if (length > maxLength) maxLength = length;
        }
        //System.out.println(maxLength);
        //System.out.println(minLength);

        ArrayList<Double> types = new ArrayList<>();
        double floor = (int) Math.floor((maxLength - minLength) / acceptableVariation);
        for (int i = 0; i < floor; i++) {
            types.add(minLength + i * acceptableVariation);
        }
        types.add(maxLength + 1);


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

        System.out.println("种类共有" + finalTypes.size());
        ArrayList<Integer> T = new ArrayList<>();
        T.addAll(finalTypes);
        for (int i = 0; i < T.size(); i++) System.out.println("种类" + i + "的长度是" + types.get(T.get(i)));
        for (int i = 0; i < edges.length; i++) {
            System.out.println("杆件" + i + "的种类是" + edges[i].getUserLabel());
        }
    }

    void getType2(double acceptableVariation) {
        double minLength = 1000;
        double maxLength = Double.MIN_VALUE;

        for (int i = 0; i < edges.length; i++) {
            double length = edges[i].getLength();
            if (length > maxLength) maxLength = length;
        }

        //共划分为多少区域
        alltypes = new ArrayList<>();
        TYPE = new ArrayList<>();
        double length = minLength;
        int l = 0;
        //所有种类
        alltypes.add(length);
        //所有种类对应的编号
        TYPE.add(l);
        while (length + acceptableVariation <maxLength) {
            length += acceptableVariation;
            l++;
            alltypes.add(length);
            TYPE.add(l);
        }
        TYPE.add(l+1);
        alltypes.add(maxLength+1);
        //System.out.println("TYPE"+TYPE.size());

        //分类
        Set<Double> finalType = new HashSet<>();
        Set<Integer> T = new HashSet<>();
        for (int i = 0; i < edges.length; i++) {
            length = edges[i].getLength();
            for (int j = 0; j < alltypes.size(); j++) {
                if (Math.abs(alltypes.get(j) - length) <= acceptableVariation / 2) {
                    edges[i].setUserLabel(j);
                    finalType.add(alltypes.get(j));
                    T.add(j);
                    break;
                }
            }
        }

        //最后所有种的编号
        List<Integer> typeID = new ArrayList<>();
        typeID.addAll(T);
        ints = bubbleSort(typeID);
        //最后所有种的长度
        finalTypes = new ArrayList<>();
        finalTypes.addAll(finalType);

        //每一种有多少根
        types = new ArrayList<>();
        for (int i = 0; i < ints.size(); i++) {
            List<Integer> edgeIndex = new ArrayList<>();
            for (int j = 0; j < edges.length; j++) {
                if (edges[j].getUserLabel() == ints.get(i)) {
                    edgeIndex.add(j);
                }
            }
            types.add(edgeIndex);
        }


        kind = finalType.size();
        System.out.println("种类共有" + kind);
        for (int i = 0; i < types.size(); i++) {
            System.out.println("种类:" + ints.get(i) + " 数量:" + types.get(i).size());
        }
//        System.out.println(minLength);
//        System.out.println(maxLength);
//
//        for(HE_Halfedge he_halfedge:edges) {
//            if(he_halfedge.getUserLabel()==-1) System.out.println(he_halfedge.getLength());
//           }

        targetLength = new double[edges.length];
        for (int i = 0; i < edges.length; i++) {
            int ind = TYPE.indexOf(edges[i].getUserLabel());
            targetLength[i] = edges[i].getLength();
            //System.out.println(types.get(ints.indexOf(edges[i].getUserLabel())).size());
            //System.out.println("a"+aaa);
            if (types.get(ints.indexOf(edges[i].getUserLabel())).size() < aaa) {
                //System.out.println("change?");
                for (int j = 1; j < TYPE.size(); j++) {

                    if (0 <= ind + j && ind + j < TYPE.size() && ints.contains(TYPE.get(ind + j)) && types.get(ints.indexOf(TYPE.get(ind + j))).size() >=aaa) {
                        targetLength[i] = finalTypes.get(ints.indexOf(TYPE.get(ind + j)));
                        System.out.println("change" + i);
                        break;
                    }
                    if (0 <= ind - j && ind - j < TYPE.size() && ints.contains(TYPE.get(ind - j)) && types.get(ints.indexOf(TYPE.get(ind - j))).size() >=aaa) {
                        targetLength[i] = finalTypes.get(ints.indexOf(TYPE.get(ind - j)));
                        System.out.println("change" + i);
                        break;
                    }
                }
                System.out.println("test");
            }
            System.out.println("长度:" + edges[i].getLength() + " 目标:" + targetLength[i]);
        }

        //getLengthSegmentation(segment);

        whether = new boolean[edges.length];

        /**i-edge.getuserlabel*/
        for (int i = 0; i < edges.length; i++) {
            if (Math.abs(edges[i].getLength() - targetLength[i]) < 8000) {
                whether[i] = true;
            } else {
                whether[i] = false;
            }
            //System.out.println(whether[i]);
        }

        currentValue = 0;
        for (int i = 0; i < edges.length; i++) {
            if (whether[i] == true) {
                currentValue += Math.abs(edges[i].getLength() - targetLength[i]) >
                        acceptableVariation ? Math.pow(Math.abs(edges[i].getLength() - targetLength[i]), 2) : 0;
            }
        }
        System.out.println("currentValue" + currentValue);

    }


    private void mapValue(PApplet app) {
        ArrayList<Float>term=new ArrayList<>();

        for (HE_Halfedge he_halfedge:edges) {
            term.add((float)he_halfedge.getUserLabel());
        }
       // term=mapValues(term);

        for(int i=0;i<term.size();i++){
            //System.out.println(term.get(i));
           // values.add(new GradientMap(app, ColorMap.JET).getColor(term.get(i)));
        }
    }

    public static float map(double value, double min, double max, double a, double b) {
        return (float) (a + (value - min) * (b - a) / (max - min));
    }

    public static ArrayList<Float> mapValues(ArrayList<Integer> values) {
        ArrayList<Float> mapValues = new ArrayList<>();
        float min = 5000;
        float max = -5000;
        for (float v : values) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        for (int i = 0; i < values.size(); i++) {
            mapValues.add(map(values.get(i), min, max, 0, 1));

        }
        return mapValues;
    }


    boolean upDateType2(double acceptableVariation) {
        double termValue = 0;
        for (int i = 0; i < edges.length; i++) {
            if (whether[i] == true) {
                /***/
                termValue += Math.abs(edges[i].getLength() - targetLength[i]) > acceptableVariation ? Math.pow(Math.abs(edges[i].getLength() - targetLength[i]), 2) : 0;
            }
        }

        double length;

        Set<Double> finalType = new HashSet<>();
        for (int i = 0; i < edges.length; i++) {
            length = edges[i].getLength();
            for (int j = 0; j < alltypes.size(); j++) {
                if (Math.abs(alltypes.get(j) - length) <= acceptableVariation / 2) {
                    finalType.add(alltypes.get(j));
                    break;
                }
            }
        }
        //System.out.println("type"+finalType.size());
        double term = currentValue;
        int termKind=kind;
        if(finalType.size()<kind){kind=finalType.size();}
        if (termValue < currentValue) {
            currentValue = termValue;
            /***/
            //kind = finalType.size();

            targetLength = new double[edges.length];
            for (int i = 0; i < edges.length; i++) {
                int ind = TYPE.indexOf(edges[i].getUserLabel());
                targetLength[i] = edges[i].getLength();

                if (types.get(ints.indexOf(edges[i].getUserLabel())).size() < aaa) {
                    //System.out.println(edges[i].getUserLabel());
                    for (int j = 1; j < TYPE.size(); j++) {

                        if (0 <= ind + j && ind + j < TYPE.size() && ints.contains(TYPE.get(ind + j)) && types.get(ints.indexOf(TYPE.get(ind + j))).size() >= aaa) {
                            targetLength[i] = finalTypes.get(ints.indexOf(TYPE.get(ind + j)));
                            //System.out.println("change" + i);
                            break;
                        }
                        if (0 <= ind - j && ind - j < TYPE.size() && ints.contains(TYPE.get(ind - j)) && types.get(ints.indexOf(TYPE.get(ind - j))).size() >= aaa) {
                            targetLength[i] = finalTypes.get(ints.indexOf(TYPE.get(ind - j)));
                            //System.out.println("change" + i);
                            break;
                        }
                    }
                    //System.out.println("test");
                }
                //System.out.println("长度:" + edges[i].getLength() + " 目标:" + targetLength[i]);
            }
        }


        return finalType.size()<termKind;
        //return termValue < term;
    }


    public void draw(PApplet app, WB_Render render) {
        app.stroke(0);
        render.drawEdges(mesh);
        app.fill(255);
        app.noStroke();
        render.drawFaces(mesh);

    }

    private void initialize(int segMent) {
        edgesAsInt = mesh.getEdgesAsInt();
        edges = mesh.getEdgesAsArray();
        //System.out.println("aaa"+edges.length);
        WB_AABB aabb = mesh.getAABB();
        //去除底面
        /**
        ArrayList<HE_Halfedge> termEdges = new ArrayList<>();
        ArrayList<int[]> termEdgesAsInt = new ArrayList<>();
        for (int i = 0; i < edges.length; i++) {
            if (!(edges[i].getStartPosition().zf() == aabb.getMinZ() && edges[i].getEndPosition().zf() == aabb.getMinZ())) {
                termEdges.add(edges[i]);
                termEdgesAsInt.add(edgesAsInt[i]);
            }
        }
        edges = new HE_Halfedge[termEdges.size()];
        for (int i = 0; i < termEdges.size(); i++) edges[i] = termEdges.get(i);
        edgesAsInt = new int[termEdges.size()][2];
        for (int i = 0; i < termEdges.size(); i++) edgesAsInt[i] = termEdgesAsInt.get(i);
*/
        fixedPointsIndex = new ArrayList<>();
        edgeLength = new double[edges.length];
        for (int i = 0; i < edges.length; i++) {
            edges[i].setUserLabel(-1);
            edgeLength[i] = edges[i].getLength();
        }
        getType(acceptableVariation);
        getLengthSegmentation(segMent);
        //getNumSegmentation(segMent);

    }

    private void initialize2() {
        edgesAsInt = mesh.getEdgesAsInt();
        edges = mesh.getEdgesAsArray();
        WB_AABB aabb = mesh.getAABB();
        //去除底面
        ArrayList<HE_Halfedge> termEdges = new ArrayList<>();
        ArrayList<int[]> termEdgesAsInt = new ArrayList<>();
        for (int i = 0; i < edges.length; i++) {
            if (!(edges[i].getStartPosition().zf() == aabb.getMinZ() && edges[i].getEndPosition().zf() == aabb.getMinZ())) {
                termEdges.add(edges[i]);
                termEdgesAsInt.add(edgesAsInt[i]);
            }
        }
        edges = new HE_Halfedge[termEdges.size()];
        for (int i = 0; i < termEdges.size(); i++) edges[i] = termEdges.get(i);
        edgesAsInt = new int[termEdges.size()][2];
        for (int i = 0; i < termEdges.size(); i++) edgesAsInt[i] = termEdgesAsInt.get(i);

        fixedPointsIndex = new ArrayList<>();
        edgeLength = new double[edges.length];
        for (int i = 0; i < edges.length; i++) {
            edges[i].setUserLabel(-1);
            edgeLength[i] = edges[i].getLength();
        }
        //getLengthSegmentation(segMent);

        getType2(acceptableVariation);

        //getNumSegmentation(segMent);
        initial=new ArrayList<>();
        List<HE_Vertex> vertices = mesh.getVertices();
        for(HE_Vertex v:vertices){initial.add(new WB_Point(v));}

    }

    void optimize2() {

        List<HE_Vertex> vertices = mesh.getVertices();

        List<Integer> vertexID = new ArrayList<>();
        int index;
//        while (vertexID.size() < num) {
//            do {
//                index = (int) (Math.random() * vertices.size());
//            } while (vertexID.contains(index));
//            vertexID.add(index);
//        }
        //System.out.println("111111");

        List<WB_Point> termPos = new ArrayList<>();

        HE_Halfedge he_halfedge;

        HE_Vertex vertex;

        //记录原先
        List<HE_Halfedge> edgeStar = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            do {
                index = (int) (Math.random() * vertices.size());
                vertex = vertices.get(index);
                edgeStar = vertex.getEdgeStar();
                he_halfedge = edgeStar.get((int) (Math.random() * edgeStar.size()));
                //System.out.println("BBBB");
            }
            while (vertexID.contains(index) || he_halfedge.getLength() == targetLength[Arrays.asList(edges).indexOf(he_halfedge)]
                    );
            //System.out.println("aaaaaaa");
            termPos.add(new WB_Point(vertex));
            vertexID.add(index);
        }
        //System.out.println(111111);
        for (int i = 0; i < vertexID.size(); i++) {

            vertex = vertices.get(vertexID.get(i));
            edgeStar = vertex.getEdgeStar();
            he_halfedge = edgeStar.get((int) (Math.random() * edgeStar.size()));

            HE_Vertex vertex1 = he_halfedge.getEndVertex();

            WB_Transform3D transform3D;
            WB_Point apply = new WB_Point();
            int d = mesh.getVertices().indexOf(vertex);

            int moveStep = 50;

            //System.out.println("33333");
            do {
                // TODO: 2022/9/18
                //List<HE_Halfedge> he_halfedges = Arrays.asList(edges);
                int index1 = Arrays.asList(edges).indexOf(he_halfedge);

                /***/
                double m = -(targetLength[index1] - edges[index1].getLength());

                transform3D = new WB_Transform3D();
                double alpha = Math.random() * Math.PI;
                double beta = Math.random() * 2 * Math.PI;
                double scale = Math.random();
//                WB_Vector vector=new WB_Vector(new WB_Point(scale*moveStep*Math.sin(alpha)*Math.cos(beta),
//                        scale*moveStep*Math.sin(alpha)*Math.sin(beta),scale*moveStep*Math.cos(alpha)
//                ));
//                vector.normalizeSelf();
//        transform3D.addTranslate(new WB_Point(scale*moveStep*Math.sin(alpha)*Math.cos(beta),
//                scale*moveStep*Math.sin(alpha)*Math.sin(beta),scale*moveStep*Math.cos(alpha)
//        ));

                // TODO: 2022/9/18
                WB_Vector vector = new WB_Vector(new WB_Point(vertex1.xf() - vertex.xf(),
                        vertex1.yf() - vertex.yf(), vertex1.zf() - vertex.zf()));
                vector.normalizeSelf();

                transform3D.addTranslate(m, vector);

                //现在
                apply = vertex.getPosition().apply(transform3D);
            } while (apply.getDistance3D(initial.get(d)) > moveRange);

            //System.out.println("44444");
            vertex.set(apply);

        //System.out.println("22222");

    }
        //System.out.println(22222);
        if (!upDateType2(acceptableVariation)) {
            for (int j = 0; j < num; j++) {
                vertices.get(vertexID.get(j)).set(termPos.get(j));
            }


        } else {
            //
            System.out.println("do it!");
            System.out.println("优化后" + kind + "种");
        }

//        for (int i = 0; i < edges.length; i++) {
//            double length = edges[i].getLength();
//            for (int j = 0; j < segment - 1; j++) {
//                if (evaluation[j] <= length && length < evaluation[j + 1]) {
//                    if (length < targetLength[j]) {
//                        //System.out.println("t"+targetLength[j]+" e"+length+" "+evaluation[j]+" "+evaluation[j+1]);
//                        edges[i].setUserLabel(2 * j);
//                    } else {
//                        edges[i].setUserLabel(2 * j + 1);
//                    }
//                }
//            }
//        }
    }

    private void setFixedPointsIndex() {
        fixedPointsIndex = new ArrayList<>();
        //fixedPointsIndex.add(0);
        WB_AABB aabb = mesh.getAABB();
        List<HE_Vertex> vertices = mesh.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            //if (vertices.get(i).zf() == aabb.getMinZ()) fixedPointsIndex.add(i);
        }
    }

    //测试，按照长度来平均分段
    private void getLengthSegmentation(int segment) {
        evaluation = new double[segment];
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

        targetLength = new double[segment - 1];
        for (int i = 0; i < targetLength.length; i++) {
            targetLength[i] = Math.floor((evaluation[i] + evaluation[i + 1]) / 2);
            //System.out.println(targetLength[i]);
        }

        for (int i = 0; i < edges.length; i++) {
            double length = edges[i].getLength();
            for (int j = 0; j < segment - 1; j++) {
                if (evaluation[j] <= length && length < evaluation[j + 1]) {
                    if (length < targetLength[j]) {
                        System.out.println("t" + targetLength[j] + " e" + length + " " + evaluation[j] + " " + evaluation[j + 1]);
                        edges[i].setUserLabel(2 * j);
                    } else {
                        edges[i].setUserLabel(2 * j + 1);
                    }
                }
            }
        }

        for (HE_Halfedge edge : edges) {
            System.out.println("id" + edge.getUserLabel() + " " + targetLength[(int) Math.floor((edge.getUserLabel() / 2))] + " " + (edge.getLength() - targetLength[(int) Math.floor((edge.getUserLabel() / 2))]));
        }
    }

    //排序
    private void bubbleSort(int segment) {
        HE_Halfedge[] arr = edges.clone();
        for (int i = 0; i < arr.length - 1; i++) {//外层循环控制排序趟数
            for (int j = 0; j < arr.length - 1 - i; j++) {//内层循环控制每一趟排序多少次
                if (arr[j].getLength() - (arr[j + 1].getLength()) > 0) {
                    HE_Halfedge temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }

        int seg = (int) Math.floor(arr.length / (segment - 1));

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < segment - 1; j++) {

                if ((int) Math.floor(i / seg) == j)
                    arr[i].setUserLabel(j);
            }
            if (i >= seg * (segment - 1)) arr[i].setUserLabel(segment - 1);
        }

    }


    //排序2
    private List<Integer> bubbleSort(List<Integer> t) {
        int[] arr = new int[t.size()];
        for (int i = 0; i < t.size(); i++) arr[i] = t.get(i);
        for (int i = 0; i < arr.length - 1; i++) {//外层循环控制排序趟数
            for (int j = 0; j < arr.length - 1 - i; j++) {//内层循环控制每一趟排序多少次
                if (arr[j] - (arr[j + 1]) > 0) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
        List<Integer> arr2 = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) arr2.add(arr[i]);
        return arr2;

    }

    //测试，按照数量来平均分段
    private void getNumSegmentation(int segment) {
        bubbleSort(segment);
        targetLength = new double[segment];
        for (int i = 0; i < segment; i++) targetLength[i] = Double.MAX_VALUE;
        for (int i = 0; i < targetLength.length; i++) {
            for (int j = 0; j < edges.length; j++) {
                if (edges[j].getLength() < targetLength[edges[j].getUserLabel()]) {
                    targetLength[edges[j].getUserLabel()] = Math.floor(edges[j].getLength());
                }
            }
        }
    }

}
