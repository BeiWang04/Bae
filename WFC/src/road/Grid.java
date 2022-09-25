package road;

import processing.core.PApplet;
import processing.core.PImage;
import wblut.geom.WB_Point;

import java.util.ArrayList;
import java.util.Arrays;

public class Grid {
    int cols;
    int rows;

    //所有细胞
    Cell[] cells;
    //贴图及规则
    public static PImage[] tiles;
    public ArrayList[][] rules;
    int blank = 0;
    int up = 1;
    int right = 2;
    int down = 3;
    int left = 4;

    //格网尺寸
    int size = 100;
    PApplet app;

    //设置贴图的规则
    public static ArrayList[][] getRules() {
        ArrayList[][] rules = new ArrayList[tiles.length][4];
        rules[0][0] = new ArrayList<Integer>(Arrays.asList(0, 1));
        rules[0][1] = new ArrayList<Integer>(Arrays.asList(0, 2));
        rules[0][2] = new ArrayList<Integer>(Arrays.asList(0, 3));
        rules[0][3] = new ArrayList<Integer>(Arrays.asList(0, 4));



        rules[1][0] = new ArrayList<Integer>(Arrays.asList(2, 3, 4));
        rules[1][1] = new ArrayList<Integer>(Arrays.asList(1, 3, 4));
        rules[1][2] = new ArrayList<Integer>(Arrays.asList(0, 3));
        rules[1][3] = new ArrayList<Integer>(Arrays.asList(2, 3));

        rules[2][0] = new ArrayList<Integer>(Arrays.asList(3, 4));
        rules[2][1] = new ArrayList<Integer>(Arrays.asList(1, 3, 4));
        rules[2][2] = new ArrayList<Integer>(Arrays.asList(1, 2, 4));
        rules[2][3] = new ArrayList<Integer>(Arrays.asList(0, 4));

        rules[3][0] = new ArrayList<Integer>(Arrays.asList(0, 1));
        rules[3][1] = new ArrayList<Integer>(Arrays.asList(1, 3, 4));
        rules[3][2] = new ArrayList<Integer>(Arrays.asList(1, 2, 4));
        rules[3][3] = new ArrayList<Integer>(Arrays.asList(1, 2));

        rules[4][0] = new ArrayList<Integer>(Arrays.asList(2, 3, 4));
        rules[4][1] = new ArrayList<Integer>(Arrays.asList(0, 2));
        rules[4][2] = new ArrayList<Integer>(Arrays.asList(1, 2, 4));
        rules[4][3] = new ArrayList<Integer>(Arrays.asList(2, 3));
        return rules;
    }

    public Grid(int cols, int rows, PApplet app) {
        this.cols = cols;
        this.rows = rows;
        this.cells = new Cell[cols * rows];
        this.app = app;

        preLoad();
        rules = getRules();

        //创建新cell
        for (int i = 0; i < cols * rows; i++) {
            cells[i] = new Cell(new WB_Point((i % rows) * size, Math.floor(i / rows) * size), false, Arrays.asList(blank, up, right, down, left));
            cells[i].setIndex(i);
        }

        //设置邻接关系
        for (int i = 0; i < cols * rows; i++) {
            if ((i + 1) % cols != 0 && i + 1 < cols * rows) cells[i].setRight(cells[i + 1]);
            if ((i + cols) < cols * rows) cells[i].setDown(cells[i + cols]);
        }
    }

    public void draw(PApplet app) {
        for (Cell cell : cells) cell.draw(app);
    }

    //加载贴图
    private void preLoad() {
        tiles = new PImage[6];
        tiles[0] = app.loadImage("road/0.png");
        tiles[1] = app.loadImage("road/1.png");
        tiles[2] = app.loadImage("road/2.png");
        tiles[3] = app.loadImage("road/3.png");
        tiles[4] = app.loadImage("road/4.png");
        tiles[5] = app.loadImage("road/5.png");
    }

    private void startOver() {
        for (int i = 0; i < cells.length; i++) {
            cells[i].collapsed = false;
            cells[i].pick = 0;
        }
        getOptis();
    }

    public void collapse() {
        ArrayList<Cell> split = split();
        if (split.size() > 0) {
            int index = (int) Math.floor(Math.random() * split.size());
            split.get(index).collapsed = true;
            if (split.get(index).optis.size() > 0) {
                int pick = (int) Math.floor(Math.random() * split.get(index).optis.size());
                split.get(index).pick = split.get(index).optis.get(pick);
                split.get(index).optis = Arrays.asList(split.get(index).optis.get(pick));
            }else{
                split.get(index).pick = 5;
                split.get(index).optis = Arrays.asList(5);
            }
        }
        getOptis();
    }

    private void getOptis() {
        for (int i = 0; i < cells.length; i++) {
            ArrayList<Integer> optis = new ArrayList<>();
            Cell cell = cells[i];
            int count = 0;
            int[] counts = new int[5];


            if (cell.up != null && cell.up.collapsed && cell.up.pick!=5) {
                optis.addAll(rules[cell.up.pick][2]);
                count++;
            }
            if (cell.right != null && cell.right.collapsed&& cell.right.pick!=5) {
                optis.addAll(rules[cell.right.pick][3]);
                count++;
            }
            if (cell.down != null && cell.down.collapsed&& cell.down.pick!=5) {
                optis.addAll(rules[cell.down.pick][0]);
                count++;
            }
            if (cell.left != null && cell.left.collapsed&& cell.left.pick!=5) {
                optis.addAll(rules[cell.left.pick][1]);
                count++;
            }

            for (int j = 0; j < optis.size(); j++) {
                if (optis.get(j) == 0) {
                    counts[0]++;
                }
                if (optis.get(j) == 1) {
                    counts[1]++;
                }
                if (optis.get(j) == 2) {
                    counts[2]++;
                }
                if (optis.get(j) == 3) {
                    counts[3]++;
                }
                if (optis.get(j) == 4) {
                    counts[4]++;
                }
            }

            if (count > 0) {
                optis.clear();
                for (int k = 0; k < counts.length; k++) {
                    if (counts[k] == count) optis.add(k);
                }
                    cell.optis = optis;

            }
        }
    }

    public void sort() {
        cells = bubbleSort(cells);
    }

    //获得坍缩数量最少的List
    private ArrayList<Cell> split() {
        cells = bubbleSort(cells);
        int min = 10;
        ArrayList<Cell> minist = new ArrayList<>();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i].collapsed == false) {
                min = cells[i].optis.size();
                break;
            }
        }

        for (int i = 0; i < cells.length; i++) {
            if (cells[i].optis.size() > min) break;
            if (cells[i].collapsed == false) minist.add(cells[i]);
        }
        return minist;
    }

    //排序
    private Cell[] bubbleSort(Cell[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {//外层循环控制排序趟数
            for (int j = 0; j < arr.length - 1 - i; j++) {//内层循环控制每一趟排序多少次
                if (arr[j].compareTo(arr[j + 1]) > 0) {
                    Cell temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
        return arr;
    }
}