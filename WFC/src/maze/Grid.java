package maze;

import processing.core.PApplet;
import processing.core.PImage;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Transform3D;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.Arrays;

public class Grid {
    int cols;
    int rows;
    boolean first = false;
    boolean second = false;
    HE_Mesh walls;

    //所有细胞
    Cell[] cells;
    //贴图及规则
    public static PImage[] tiles;
    int white = 0;
    int black = 1;
    int alpha = 2;

    //格网尺寸
    int size = 50;
    PApplet app;


    public Grid(int cols, int rows, PApplet app) {
        this.cols = cols;
        this.rows = rows;
        this.cells = new Cell[cols * rows];
        this.app = app;

        preLoad();


        //创建新cell,设定起点
        for (int i = 0; i < cols * rows; i++) {
            cells[i] = new Cell(new WB_Point((i % rows) * size, Math.floor(i / rows) * size), false, Arrays.asList(white,black,alpha));
            cells[i].setIndex(i);
            if(i!=0){cells[i].pick=1;}else{cells[i].pick=0;}
        }

        //设置邻接关系
        for (int i = 0; i < cols * rows; i++) {
            if ((i + 1) % cols != 0 && i + 1 < cols * rows) cells[i].setRight(cells[i + 1]);
            if ((i + cols) < cols * rows) cells[i].setDown(cells[i + cols]);
        }
    }

    public void draw(PApplet app, WB_Render render) {
        for (Cell cell : cells) cell.draw(app);
        render.drawFaces(walls);
    }

    //加载贴图
    private void preLoad() {
        tiles = new PImage[3];
        tiles[0] = app.loadImage("maze/white.png");
        tiles[1] = app.loadImage("maze/black.png");
        tiles[2] = app.loadImage("maze/alpha.png");
    }


    public void firstCollapse() {
        ArrayList<Cell[]> optis = getOptis();
        if(optis.size()>0) {
            Cell[] cells = optis.get((int) (Math.random() * optis.size()));
            cells[0].pick = 0;
            cells[1].pick = 2;
            cells[2].pick = 0;
        }else{first=false;second=true;}
    }

    public void secondCollapse() {
        for(Cell cell:cells){
            if(cell.pick==2){cell.pick=0;}
        }
        generateWalls();
        second=false;
    }

    private ArrayList<Cell[]>getOptis(){
        ArrayList<Cell[]>optis=new ArrayList<>();
        for(int i=0;i<cells.length;i++){
            Cell cell = cells[i];
            if(cell.pick==0&&cell.up!=null&&cell.up.up!=null&&cell.up.pick==1&&cell.up.up.pick==1){optis.add(new Cell[]{cell,cell.up,cell.up.up});}
            if(cell.pick==0&&cell.down!=null&&cell.down.down!=null&&cell.down.pick==1&&cell.down.down.pick==1){optis.add(new Cell[]{cell,cell.down,cell.down.down});}
            if(cell.pick==0&&cell.left!=null&&cell.left.left!=null&&cell.left.pick==1&&cell.left.left.pick==1){optis.add(new Cell[]{cell,cell.left,cell.left.left});}
            if(cell.pick==0&&cell.right!=null&&cell.right.right!=null&&cell.right.pick==1&&cell.right.right.pick==1){optis.add(new Cell[]{cell,cell.right,cell.right.right});}
        }
        return optis;
    }

    void generateWalls(){

        for(Cell cell:cells){
            if(cell.pick==1){
                if(cell.up!=null&&cell.up.pick==1&&cell.left!=null&&cell.left.pick==1&&cell.right!=null&&cell.right.pick==1&&
                        (cell.down== null||cell.down.pick!=1)){cell.wallType=1;}

                if(cell.up!=null&&cell.up.pick==1&&cell.down!=null&&cell.down.pick==1&&cell.right!=null&&cell.right.pick==1&&
                        (cell.left== null||cell.left.pick!=1)){cell.wallType=2;}

                if(cell.down!=null&&cell.down.pick==1&&cell.left!=null&&cell.left.pick==1&&cell.right!=null&&cell.right.pick==1&&
                        (cell.up== null||cell.up.pick!=1)){cell.wallType=3;}

                if(cell.up!=null&&cell.up.pick==1&&cell.left!=null&&cell.left.pick==1&&cell.down!=null&&cell.down.pick==1&&
                        (cell.right== null||cell.right.pick!=1)){cell.wallType=4;}

                if(cell.up!=null&&cell.up.pick==1&&cell.left!=null&&cell.left.pick==1&&(cell.right== null||cell.right.pick!=1)&&
                        (cell.down== null||cell.down.pick!=1)){cell.wallType=5;}

                if(cell.up!=null&&cell.up.pick==1&&cell.right!=null&&cell.right.pick==1&&(cell.left== null||cell.left.pick!=1)&&
                        (cell.down== null||cell.down.pick!=1)){cell.wallType=6;}

                if(cell.down!=null&&cell.down.pick==1&&cell.right!=null&&cell.right.pick==1&&(cell.left== null||cell.left.pick!=1)&&
                        (cell.up== null||cell.up.pick!=1)){cell.wallType=7;}

                if(cell.down!=null&&cell.down.pick==1&&cell.left!=null&&cell.left.pick==1&&(cell.right== null||cell.right.pick!=1)&&
                        (cell.up== null||cell.up.pick!=1)){cell.wallType=8;}

                if((cell.up== null||cell.up.pick!=1)&&
                        (cell.down== null||cell.down.pick!=1)){cell.wallType=9;}

                if((cell.left== null||cell.left.pick!=1)&&
                        (cell.right== null||cell.right.pick!=1)){cell.wallType=10;}
            }
        }
        HE_Mesh mesh=new HE_Mesh();
        WB_Transform3D transform3D=new WB_Transform3D();
        transform3D.addTranslate(new WB_Point(0,0,size));

     for(Cell cell:cells){
         if(cell.pick==1){
             WB_Point leftMid=new WB_Point(cell.loc.xf(),cell.loc.yf()+this.size/2);
             WB_Point upMid=new WB_Point(cell.loc.xf()+this.size/2,cell.loc.yf());
             WB_Point rightMid=new WB_Point(cell.loc.xf()+this.size,cell.loc.yf()+this.size/2);
             WB_Point downMid=new WB_Point(cell.loc.xf()+this.size/2,cell.loc.yf()+this.size);
             WB_Point mid=new WB_Point(cell.loc.xf()+this.size/2,cell.loc.yf()+this.size/2);
             switch (cell.wallType){
                 case 1:
                     HEC_FromPolygons hec_fromPolygons = new HEC_FromPolygons().setPolygons(new WB_Polygon[]{new WB_Polygon(leftMid, rightMid, rightMid.apply(transform3D), leftMid.apply(transform3D)),
                             new WB_Polygon(upMid, mid, mid.apply(transform3D), upMid.apply(transform3D))});
                     mesh.add(new HE_Mesh(hec_fromPolygons));
                     break;

                 case 2:
                     hec_fromPolygons = new HEC_FromPolygons().setPolygons(new WB_Polygon[]{new WB_Polygon(upMid, downMid, downMid.apply(transform3D), upMid.apply(transform3D)),
                             new WB_Polygon(rightMid, mid, mid.apply(transform3D), rightMid.apply(transform3D))});
                     mesh.add(new HE_Mesh(hec_fromPolygons));
                     break;

                 case 3:
                     hec_fromPolygons = new HEC_FromPolygons().setPolygons(new WB_Polygon[]{new WB_Polygon(leftMid, rightMid, rightMid.apply(transform3D), leftMid.apply(transform3D)),
                             new WB_Polygon(downMid, mid, mid.apply(transform3D), downMid.apply(transform3D))});
                     mesh.add(new HE_Mesh(hec_fromPolygons));
                     break;

                 case 4:
                     hec_fromPolygons = new HEC_FromPolygons().setPolygons(new WB_Polygon[]{new WB_Polygon(upMid, downMid, downMid.apply(transform3D), upMid.apply(transform3D)),
                             new WB_Polygon(leftMid, mid, mid.apply(transform3D), leftMid.apply(transform3D))});
                     mesh.add(new HE_Mesh(hec_fromPolygons));
                     break;

                 case 5:
                     hec_fromPolygons = new HEC_FromPolygons().setPolygons(new WB_Polygon[]{new WB_Polygon(upMid, mid, mid.apply(transform3D), upMid.apply(transform3D)),
                             new WB_Polygon(leftMid, mid, mid.apply(transform3D), leftMid.apply(transform3D))});
                     mesh.add(new HE_Mesh(hec_fromPolygons));
                     break;

                 case 6:
                     hec_fromPolygons = new HEC_FromPolygons().setPolygons(new WB_Polygon[]{new WB_Polygon(upMid, mid, mid.apply(transform3D), upMid.apply(transform3D)),
                             new WB_Polygon(rightMid, mid, mid.apply(transform3D), rightMid.apply(transform3D))});
                     mesh.add(new HE_Mesh(hec_fromPolygons));
                     break;

                 case 7:
                     hec_fromPolygons = new HEC_FromPolygons().setPolygons(new WB_Polygon[]{new WB_Polygon(mid, downMid, downMid.apply(transform3D), mid.apply(transform3D)),
                             new WB_Polygon(rightMid, mid, mid.apply(transform3D), rightMid.apply(transform3D))});
                     mesh.add(new HE_Mesh(hec_fromPolygons));
                     break;
                 case 8:
                     hec_fromPolygons = new HEC_FromPolygons().setPolygons(new WB_Polygon[]{new WB_Polygon(mid, downMid, downMid.apply(transform3D), mid.apply(transform3D)),
                             new WB_Polygon(leftMid, mid, mid.apply(transform3D), leftMid.apply(transform3D))});
                     mesh.add(new HE_Mesh(hec_fromPolygons));
                     break;
                 case 9:
                     hec_fromPolygons = new HEC_FromPolygons().setPolygons(new WB_Polygon[]{new WB_Polygon(leftMid, rightMid, rightMid.apply(transform3D), leftMid.apply(transform3D)),
                            });
                     mesh.add(new HE_Mesh(hec_fromPolygons));
                     break;

                 case 10:
                     hec_fromPolygons = new HEC_FromPolygons().setPolygons(new WB_Polygon[]{new WB_Polygon(upMid, downMid, downMid.apply(transform3D), upMid.apply(transform3D)),
                             });
                     mesh.add(new HE_Mesh(hec_fromPolygons));
                     break;
             }
         }
     }
     this.walls=mesh;
    }

}