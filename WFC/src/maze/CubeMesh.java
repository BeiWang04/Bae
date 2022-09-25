package maze;

import edu.duke.geo4.colormapper.ColorMap;
import edu.duke.geo4.colormapper.GradientMap;
import processing.core.PApplet;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Transform3D;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CubeMesh {
    int cols;
    int rows;
    int depth;
    double divide;
    PApplet app;

    int innersize;
    boolean first = false;
    boolean second = false;
    HE_Mesh walls;

    //所有细胞
    Cube[] cells;
    //贴图及规则

    int white = 0;
    int black = 1;
    int alpha = 2;

    //格网尺寸
    int size ;
    ArrayList<Integer> values;

    public CubeMesh(int cols, int rows, int depth, double divide,int size,PApplet app) {
        this.cols = cols;
        this.rows = rows;
        this.depth = depth;
        this.cells = new Cube[cols * rows * depth];
        this.innersize = cols * rows * depth - (cols - 1) * (rows - 1) * (depth - 1);
        this.divide = divide;
        values=new ArrayList<>();
        walls = new HE_Mesh();
        this.app=app;
        this.size=size;
        //System.out.println(innersize);


        //创建新cell,设定起点
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < cols * rows; i++) {
                cells[j * (cols * rows) + i] = new Cube(new WB_Point((i % rows) * size, Math.floor(i / rows) * size, j * size),
                        false, Arrays.asList(white, black, alpha),size);
                cells[j * (cols * rows) + i].setIndex(j * (cols * rows) + i);
                cells[j * (cols * rows) + i].pick = 1;
            }
        }
        cells[0].pick = 0;
        //设置邻接关系
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < cols * rows; i++) {
                if ((i + 1) % cols != 0 && i + 1 < cols * rows)
                    cells[j * (cols * rows) + i].setRight(cells[j * (cols * rows) + i + 1]);
                if ((i + cols) < cols * rows)
                    cells[j * (cols * rows) + i].setBehind(cells[j * (cols * rows) + i + cols]);
                if (j > 0) cells[j * (cols * rows) + i].setDown(cells[(j - 1) * (cols * rows) + i]);
            }
        }

        for (Cube cube : cells) {
            //if(cube.up!=null&&cube.down!=null&&cube.left!=null&&cube.right!=null&&cube.front!=null&&cube.behind!=null)cube.inner=true;

            int count = 0;
            if (cube.up == null) {
                count++;
            }
            if (cube.down == null) {
                count++;
            }
            if (cube.behind == null) {
                count++;
            }
            if (cube.front == null) {
                count++;
            }
            if (cube.left == null) {
                count++;
            }
            if (cube.right == null) {
                count++;
            }
            if (count == 0) {
                cube.inner = true;
                //innersize--;
            }
            if (count == 2) {
                cube.boundary = true;
                cube.type = 0;
            }
            if (count > 2) {
                cube.corner = true;
                cube.type = 0;
            }
            if (count == 1) {
                if (cube.up == null) {
                    cube.type = 1;
                }
                if (cube.down == null) {
                    cube.type = 2;
                }
                if (cube.behind == null) {
                    cube.type = 3;
                }
                if (cube.front == null) {
                    cube.type = 4;
                }
                if (cube.left == null) {
                    cube.type = 5;
                }
                if (cube.right == null) {
                    cube.type = 6;
                }
            }
        }


    }

    public void draw(PApplet app, WB_Render render) {
        for (Cube cube : cells) {
            if(first&&!second){cube.draw(app);}else{
            if(cube.inner){ cube.draw(app);}
        }}
        List<HE_Face> faces = walls.getFaces();

        if(faces.size()>0){
        for (int i=0;i<faces.size();i++) {
            app.fill(values.get(i),170);
            app.noStroke();
            render.drawFace(faces.get(i));
        }
//        app.fill(255,0,255);
//        app.noStroke();
//        render.drawFaces(walls);
    }}

    private void mapValue() {
        ArrayList<Float>term=new ArrayList<>();
        WB_Point min = walls.getAABB().getMin();
        for (HE_Face f : walls.getFaces()) {
            term.add((float) WB_GeometryOp.getDistance3D(f.getFaceCenter(), min));
        }
        term=mapValues(term);

        for(int i=0;i<term.size();i++){
            //System.out.println(term.get(i));
            values.add(new GradientMap(app, ColorMap.COOL).getColor(term.get(i)));
        }
    }

    public static float map(double value, double min, double max, double a, double b) {
        return (float) (a + (value - min) * (b - a) / (max - min));
    }

    public static ArrayList<Float> mapValues(ArrayList<Float> values) {
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

    public void firstCollapse() {
        ArrayList<ArrayList<Cube>> optis = getOptis(this.innersize / divide);
        if (optis.size() > 0) {
            ArrayList<Cube> cells = optis.get((int) (Math.random() * optis.size()));
            cells.get(0).pick = 0;
            cells.get(1).pick = 2;
            cells.get(2).pick = 0;
        } else {
            first = false;
            second = true;
        }
    }

    //0:面的序号 1:剩下的黑色边的数量
    public int[][] calculate() {
        int[][] sum = new int[7][2];
        for (Cube cube : cells) {
            if (cube.pick == 1) {
                switch (cube.type) {
                    case 0:
                        sum[0][1]++;
                        break;
                    case 1:
                        sum[1][1]++;
                        break;
                    case 2:
                        sum[2][1]++;
                        break;
                    case 3:
                        sum[3][1]++;
                        break;
                    case 4:
                        sum[4][1]++;
                        break;
                    case 5:
                        sum[5][1]++;
                        break;
                    case 6:
                        sum[6][1]++;
                        break;
                }
            }
        }

        return sum;
    }

    //排序
    private int[][] bubbleSort(int[][] arr) {
        for (int i = 0; i < arr.length - 1; i++) {//外层循环控制排序趟数
            for (int j = 0; j < arr.length - 1 - i; j++) {//内层循环控制每一趟排序多少次
                if (arr[j][1] - (arr[j + 1][1]) > 0) {
                    int temp = arr[j][1];
                    arr[j][1] = arr[j + 1][1];
                    arr[j + 1][1] = temp;
                    temp = arr[j][0];
                    arr[j][0] = arr[j + 1][0];
                    arr[j + 1][0] = temp;
                }
            }
        }
        return arr;
    }

    public void secondCollapse() {
        for (Cube cell : cells) {
            if (cell.pick == 2) {
                cell.pick = 0;
            }
        }
        generateWalls();
        second = false;
    }

    private ArrayList<ArrayList<Cube>> getOptis(double divide) {
        ArrayList<ArrayList<Cube>> optis = new ArrayList<>();
        ArrayList<ArrayList<Cube>> finalOptis = new ArrayList<>();
        int[][] calculate = calculate();

        for (Cube cube : cells) {
            if (!cube.inner) {
                if (calculate[cube.type][1] > divide) {
                    optis.addAll(option(cube));
                }
            }
        }

        ArrayList<int[]> ints = new ArrayList<>();
        if (optis.size() > 0) {
            ints = sortOptis(optis);
        }

        if (ints.size() > 0) {
            for (int i = 0; i < ints.size(); i++) {
                finalOptis.add(optis.get(ints.get(i)[0]));
            }
        }

        //System.out.println(finalOptis.size());
        return finalOptis;
    }

    private ArrayList<int[]> sortOptis(ArrayList<ArrayList<Cube>> optis) {
        int[][] sortOptis = new int[optis.size()][2];
        for (int i = 0; i < optis.size(); i++) {
            ArrayList<Cube> cubes = optis.get(i);
            int count = 0;
            for (Cube cube : cubes) {
                if (cube.up != null && !cube.up.inner && cube.up.pick == 1) count++;
                if (cube.down != null && !cube.down.inner && cube.down.pick == 1) count++;
                if (cube.left != null && !cube.left.inner && cube.left.pick == 1) count++;
                if (cube.right != null && !cube.right.inner && cube.right.pick == 1) count++;
                if (cube.front != null && !cube.front.inner && cube.front.pick == 1) count++;
                if (cube.behind != null && !cube.behind.inner && cube.behind.pick == 1) count++;
            }
            //if(cubes.get(0).type==cubes.get(1).type&&cubes.get(1).type==cubes.get(2).type)count++;
            sortOptis[i][0] = i;
            sortOptis[i][1] = count;
        }
        int[][] ints = bubbleSort(sortOptis);

        ArrayList<int[]> opti = new ArrayList<>();
        for (int i = 0; i < ints.length; i++) {
            if (ints[ints.length - 1][1] > 3) {
                if (ints[i][1] == ints[ints.length - 1][1]) opti.add(new int[]{ints[i][0], ints[i][1]});
            }
        }
        return opti;

    }

    private ArrayList<ArrayList<Cube>> option(Cube cube) {
        ArrayList<ArrayList<Cube>> optis = new ArrayList<>();
        ArrayList<Cube> opti;

        if (cube.pick == 0 && cube.up != null && cube.up.pick == 1 && !cube.up.inner) {
            opti = test(cube, cube.up, cube.up.up);
            if (opti.size() > 0) optis.add(opti);
            if (cube.up.boundary == true) {
                opti = test(cube, cube.up, cube.up.left);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.up, cube.up.right);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.up, cube.up.behind);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.up, cube.up.front);
                if (opti.size() > 0) optis.add(opti);
            }
        }

        if (cube.pick == 0 && cube.down != null && cube.down.pick == 1 && !cube.down.inner) {
            opti = test(cube, cube.down, cube.down.down);
            if (opti.size() > 0) optis.add(opti);

            if (cube.down.boundary == true) {
                opti = test(cube, cube.down, cube.down.left);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.down, cube.down.right);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.down, cube.down.behind);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.down, cube.down.front);
                if (opti.size() > 0) optis.add(opti);
            }
        }

        if (cube.pick == 0 && cube.left != null && cube.left.pick == 1 && !cube.left.inner) {
            opti = test(cube, cube.left, cube.left.left);
            if (opti.size() > 0) optis.add(opti);

            if (cube.left.boundary == true) {
                opti = test(cube, cube.left, cube.left.up);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.left, cube.left.down);
                if (opti.size() > 0) optis.add(opti);


                opti = test(cube, cube.left, cube.left.behind);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.left, cube.left.front);
                if (opti.size() > 0) optis.add(opti);
            }
        }

        if (cube.pick == 0 && cube.right != null && cube.right.pick == 1 && !cube.right.inner) {
            opti = test(cube, cube.right, cube.right.right);
            if (opti.size() > 0) optis.add(opti);
            if (cube.right.boundary == true) {
                opti = test(cube, cube.right, cube.right.up);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.right, cube.right.down);
                if (opti.size() > 0) optis.add(opti);


                opti = test(cube, cube.right, cube.right.behind);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.right, cube.right.front);
                if (opti.size() > 0) optis.add(opti);
            }
        }
        if (cube.pick == 0 && cube.front != null && cube.front.pick == 1 && !cube.front.inner) {
            opti = test(cube, cube.front, cube.front.front);
            if (opti.size() > 0) optis.add(opti);
            if (cube.front.boundary == true) {
                opti = test(cube, cube.front, cube.front.up);
                if (opti.size() > 0) optis.add(opti);


                opti = test(cube, cube.front, cube.front.left);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.front, cube.front.behind);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.front, cube.front.right);
                if (opti.size() > 0) optis.add(opti);
            }
        }
        if (cube.pick == 0 && cube.behind != null && cube.behind.pick == 1 && !cube.behind.inner) {
            opti = test(cube, cube.behind, cube.behind.behind);
            if (opti.size() > 0) optis.add(opti);
            if (cube.behind.boundary == true) {
                opti = test(cube, cube.behind, cube.behind.up);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.behind, cube.behind.down);
                if (opti.size() > 0) optis.add(opti);

                opti = test(cube, cube.behind, cube.behind.left);
                if (opti.size() > 0) optis.add(opti);


                opti = test(cube, cube.behind, cube.behind.right);
                if (opti.size() > 0) optis.add(opti);
            }
        }

        return optis;
    }

    private ArrayList<Cube> test(Cube cube, Cube a, Cube b) {
        ArrayList<Cube> opti = new ArrayList<>();
        if (b != null && b.pick == 1 && !b.inner) {
            opti.add(cube);
            opti.add(a);
            opti.add(b);
        }
        return opti;
    }


    void generateWalls() {

        /***/
        ArrayList<WB_Transform3D> trans = new ArrayList<>();
        HE_Mesh mesh = new HE_Mesh();
        WB_Transform3D t0 = new WB_Transform3D();
        t0.addTranslate(new WB_Point(0, 0, size));
        trans.add(t0);
        //up
        WB_Transform3D t1 = new WB_Transform3D();
        t1.addTranslate(new WB_Point(0, 0, size));
        trans.add(t1);
        //down
        WB_Transform3D t2 = new WB_Transform3D();
        t2.addTranslate(new WB_Point(0, 0, -size));
        trans.add(t2);
        //behind
        WB_Transform3D t3 = new WB_Transform3D();
        t3.addTranslate(new WB_Point(0, size, 0));
        trans.add(t3);
        //front
        WB_Transform3D t4 = new WB_Transform3D();
        t4.addTranslate(new WB_Point(0, -size, 0));
        trans.add(t4);
        //left
        WB_Transform3D t5 = new WB_Transform3D();
        t5.addTranslate(new WB_Point(-size, 0, 0));
        trans.add(t5);
        //right
        WB_Transform3D t6 = new WB_Transform3D();
        t6.addTranslate(new WB_Point(size, 0, 0));
        trans.add(t6);

        HEC_FromPolygons hec_fromPolygons;
        for (Cube cell : cells) {
            if (cell.pick == 1 && !cell.inner) {
                int count = 0;
                String term = null;
                String current=null;
                Cube next = null;

                if ((cell.left != null && !cell.left.inner && cell.left.pick == 1)) {
                    hec_fromPolygons = new HEC_FromPolygons().setPolygons(
                            new WB_Polygon[]{new WB_Polygon(cell.left.loc, cell.loc, cell.loc.apply(trans.get(cell.type)),
                                    cell.left.loc.apply(trans.get(cell.type)))}
                    );
                    mesh.add(new HE_Mesh(hec_fromPolygons));
                    count++;
                    term = "left";
                    current="right";
                    next = cell.left;
                }

                if ((cell.right != null && !cell.right.inner&&cell.right.pick == 1)) {
//                    hec_fromPolygons = new HEC_FromPolygons().setPolygons(
//                            new WB_Polygon[]{new WB_Polygon(cell.right.loc, cell.loc,cell.loc.apply(trans.get(cell.type)),
//                                    cell.right.loc.apply(trans.get(cell.type)))}
//                    );
//                    mesh.add(new HE_Mesh(hec_fromPolygons));
                    count++;term="right";next=cell.right;
                }

                if ((cell.up != null && !cell.up.inner && cell.up.pick == 1)) {
                    hec_fromPolygons = new HEC_FromPolygons().setPolygons(
                            new WB_Polygon[]{new WB_Polygon(cell.up.loc, cell.loc, cell.loc.apply(trans.get(cell.type)),
                                    cell.up.loc.apply(trans.get(cell.type)))}
                    );
                    mesh.add(new HE_Mesh(hec_fromPolygons));
                    count++;
                    term = "up";
                    current="down";
                    next = cell.up;
                }

                if ((cell.down != null && !cell.down.inner&&cell.down.pick == 1)) {
//                    hec_fromPolygons = new HEC_FromPolygons().setPolygons(
//                            new WB_Polygon[]{new WB_Polygon(cell.down.loc, cell.loc,cell.loc.apply(trans.get(cell.type)),
//                                    cell.down.loc.apply(trans.get(cell.type)))}
//                    );
//                    mesh.add(new HE_Mesh(hec_fromPolygons));
                    count++;term="down";next=cell.down;
                }

                if ((cell.front != null && !cell.front.inner && cell.front.pick == 1)) {
                    hec_fromPolygons = new HEC_FromPolygons().setPolygons(
                            new WB_Polygon[]{new WB_Polygon(cell.front.loc, cell.loc, cell.loc.apply(trans.get(cell.type)),
                                    cell.front.loc.apply(trans.get(cell.type)))}
                    );
                    mesh.add(new HE_Mesh(hec_fromPolygons));
                    count++;
                    term = "front";
                    current="behind";
                    next = cell.front;
                }

                if ((cell.behind != null && !cell.behind.inner&&cell.behind.pick == 1)) {
//                    hec_fromPolygons = new HEC_FromPolygons().setPolygons(
//                            new WB_Polygon[]{new WB_Polygon(cell.behind.loc, cell.loc,cell.loc.apply(trans.get(cell.type)),
//                                    cell.behind.loc.apply(trans.get(cell.type)))}
//                    );
//                    mesh.add(new HE_Mesh(hec_fromPolygons));
                    count++;term="behind";next=cell.behind;
                }

                WB_Transform3D transform3D = new WB_Transform3D();
                WB_Transform3D transform3D2 = new WB_Transform3D();


                if (count == 0 || cell.corner) {

                    hec_fromPolygons = new HEC_FromPolygons().setPolygons(
                            new WB_Polygon[]{new WB_Polygon(cell.loc.apply(trans.get(4)).apply(trans.get(5)),
                                    cell.loc.apply(trans.get(4)).apply(trans.get(6)),
                                    cell.loc.apply(trans.get(3)).apply(trans.get(6)),
                                    cell.loc.apply(trans.get(3)).apply(trans.get(5))
                            )}
                    );
                    mesh.add(new HE_Mesh(hec_fromPolygons));
                }


                if (count == 1 ) {
                    switch (term) {
                        case "up":
                            transform3D = new WB_Transform3D().addTranslate(new WB_Point(0, 0, -size / 2));
                            break;


                        case "down":
                            transform3D = new WB_Transform3D().addTranslate(new WB_Point(0, 0, size / 2));
                            break;

                        case "left":
                            transform3D = new WB_Transform3D().addTranslate(new WB_Point(size / 2, 0, 0));
                            break;

                        case "right":
                            transform3D = new WB_Transform3D().addTranslate(new WB_Point(-size / 2, 0, 0));
                            break;

                        case "front":
                            transform3D = new WB_Transform3D().addTranslate(new WB_Point(0, size / 2, 0));
                            break;

                        case "behind":
                            transform3D = new WB_Transform3D().addTranslate(new WB_Point(0, -size / 2, 0));
                            break;


                    }


                    hec_fromPolygons = new HEC_FromPolygons().setPolygons(
                            new WB_Polygon[]{new WB_Polygon(cell.loc.apply(transform3D), cell.loc, cell.loc.apply(trans.get(cell.type)),
                                    cell.loc.apply(transform3D).apply(trans.get(cell.type)))}
                    );
                    mesh.add(new HE_Mesh(hec_fromPolygons));
                }


                if (!cell.corner && cell.boundary) {

                    if ((cell.up == null && cell.left == null) || (cell.up == null && cell.right == null) ||
                            (cell.down == null && cell.left == null) || (cell.down == null && cell.right == null)) {
                        hec_fromPolygons = new HEC_FromPolygons().setPolygons(
                                new WB_Polygon[]{new WB_Polygon(cell.loc.apply(trans.get(5)).apply(trans.get(1)),
                                        cell.loc.apply(trans.get(5)).apply(trans.get(2)),
                                        cell.loc.apply(trans.get(6)).apply(trans.get(2)),
                                        cell.loc.apply(trans.get(6)).apply(trans.get(1))
                                )}
                        );
                        mesh.add(new HE_Mesh(hec_fromPolygons));
                    }

                    if ((cell.up == null && cell.front == null) || (cell.up == null && cell.behind == null) ||
                            (cell.down == null && cell.front == null) || (cell.down == null && cell.behind == null)) {
                        hec_fromPolygons = new HEC_FromPolygons().setPolygons(
                                new WB_Polygon[]{new WB_Polygon(cell.loc.apply(trans.get(1)).apply(trans.get(4)),
                                        cell.loc.apply(trans.get(1)).apply(trans.get(3)),
                                        cell.loc.apply(trans.get(2)).apply(trans.get(3)),
                                        cell.loc.apply(trans.get(2)).apply(trans.get(4))
                                )}
                        );
                        mesh.add(new HE_Mesh(hec_fromPolygons));
                    }

                    if ((cell.front == null && cell.left == null) || (cell.front == null && cell.right == null) ||
                            (cell.behind == null && cell.left == null) || (cell.behind == null && cell.right == null)) {
                        hec_fromPolygons = new HEC_FromPolygons().setPolygons(
                                new WB_Polygon[]{new WB_Polygon(cell.loc.apply(trans.get(4)).apply(trans.get(5)),
                                        cell.loc.apply(trans.get(4)).apply(trans.get(6)),
                                        cell.loc.apply(trans.get(3)).apply(trans.get(6)),
                                        cell.loc.apply(trans.get(3)).apply(trans.get(5))
                                )}
                        );
                        mesh.add(new HE_Mesh(hec_fromPolygons));
                    }
                }

            }
        }
        //设置墙面

        this.walls = mesh;
        mapValue();

    }


}
