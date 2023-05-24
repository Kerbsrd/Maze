import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//represnets a vertex
class Vertex {
  int size;
  int x;
  int y;
  int num;
  ArrayList<Edge> edges;
  Color col;
  Edge prevEdge;

  // ArrayList<Edge> edges;
  Vertex(int size, int x, int y) {
    this.size = size;
    this.x = x;
    this.y = y;
    this.num = 0;
    this.edges = new ArrayList<Edge>();
    this.col = Color.GRAY;
  }

  // draws a vertex
  public WorldScene printVertex(WorldScene scene) {
    RectangleImage vertex = new RectangleImage(this.size, this.size, OutlineMode.SOLID, this.col);
    scene.placeImageXY(vertex, this.x, this.y);
    return scene;
  }

  // overrides equals
  public boolean equals(Object o) {
    if (o instanceof Vertex) {
      Vertex ver = (Vertex) o;
      return this.num == ver.num;
    }
    else {
      return false;
    }
  }

  // overrides hashCode
  public int hashCode() {
    return Objects.hash(this.x, this.y, this.num);
  }

}

// represents an edge
class Edge {
  ArrayList<Vertex> vertices;
  int x;
  int y;
  int val;
  int width;
  int height;
  Color col;

  Edge(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.vertices = new ArrayList<Vertex>();
    Random rand = new Random();
    this.val = rand.nextInt(60);
    this.width = width;
    this.height = height;
    this.col = Color.RED;
  }

  Edge(int x, int y, int width, int height, Random rand) {
    this.x = x;
    this.y = y;
    this.vertices = new ArrayList<Vertex>();
    this.val = rand.nextInt(60);
    this.width = width;
    this.height = height;
    this.col = Color.RED;
  }

  // draws an edge
  public WorldScene printEdge(WorldScene scene) {
    RectangleImage edge = new RectangleImage(this.width, this.height, OutlineMode.SOLID, this.col);
    scene.placeImageXY(edge, this.x, this.y);
    return scene;
  }

}

// represents a maze
class Maze extends World {
  int width;
  int height;
  ArrayList<Vertex> vertices;
  ArrayList<Edge> print = new ArrayList<Edge>();
  ArrayList<Edge> orderedEdges;
  ArrayList<Edge> connected = new ArrayList<Edge>();
  HashMap<Vertex, Vertex> reps = new HashMap<Vertex, Vertex>();
  Random rand;
  ArrayList<Vertex> workList = new ArrayList<Vertex>();
  ArrayList<Vertex> visited = new ArrayList<Vertex>();
  boolean bfst = false;
  boolean dsft = false;
  HashMap<Vertex, Edge> path = new HashMap<Vertex, Edge>();

  Maze(int width, int height) {
    this.width = width;
    this.height = height;
    this.vertices = this.makeVertices();
    this.rand = new Random();
    this.orderedEdges = new ArrayList<Edge>();
    this.makeEdges();
    Comparator<Edge> comparator = ((e1, e2) -> e1.val - e2.val);
    this.orderedEdges.sort(comparator);
    for (Vertex v : this.vertices) {
      reps.put(v, v);
    }
    this.krask();
    for (Edge ed : this.connected) {
      ed.col = Color.GRAY;
    }
    this.initVert();
    this.workList.add(this.vertices.get(0));
    for (Vertex v : this.vertices) {
      Edge spot = new Edge(0, 0, 0, 0);
      this.path.put(v, spot);
    }
  }

  Maze(int width, int height, int rand) {
    this.width = width;
    this.height = height;
    this.vertices = this.makeVertices();
    this.rand = new Random(rand);
    this.orderedEdges = new ArrayList<Edge>();
    this.makeEdges();
    Comparator<Edge> comparator = ((e1, e2) -> e1.val - e2.val);
    this.orderedEdges.sort(comparator);
    for (Vertex v : this.vertices) {
      reps.put(v, v);
    }
    this.krask();
    for (Edge ed : this.connected) {
      ed.col = Color.GRAY;
    }
    this.initVert();
    this.workList.add(this.vertices.get(0));
    for (Vertex v : this.vertices) {
      Edge spot = new Edge(0, 0, 0, 0);
      this.path.put(v, spot);
    }
  }

  // creates a maze
  ArrayList<Vertex> makeVertices() {
    ArrayList<Vertex> vert = new ArrayList<Vertex>();
    int size;
    if (this.height < this.width) {
      size = this.width;
    }
    else {
      size = this.height;
    }

    size = (int) (800 / size);
    int x = 0;
    int y = 0;
    for (int i = 0; i < this.height; i++) {
      y = (int) (size / 2) + size * i;
      for (int j = 0; j < this.width; j++) {
        x = (int) (size / 2) + size * j;
        Vertex vrtx = new Vertex(size, x, y);
        vert.add(vrtx);
      }
    }
    for (int h = 0; h < vert.size(); h++) {
      vert.get(h).num = h;
    }
    return vert;
  }

  // creates an Array with all the Edges
  void makeEdges() {
    int size;
    if (this.height < this.width) {
      size = this.width;
    }
    else {
      size = this.height;
    }
    size = (int) (800 / size);
    int sizeHeight = (int) (size / 10) + 1;

    int x = 0;
    int y = 0;

    int indx = 0;
    for (int i = 0; i < this.height; i++) {
      indx++;
      y = (int) (size / 2) + size * i;
      for (int j = 1; j < this.width; j++) {
        x = size * j;
        Vertex v1 = this.vertices.get(indx - 1);
        Vertex v2 = this.vertices.get(indx);
        Edge insert = new Edge(x, y, sizeHeight, size, this.rand);
        insert.vertices.add(v1);
        insert.vertices.add(v2);
        indx++;
        this.print.add(insert);
        this.orderedEdges.add(insert);
      }
    }

    int ex = 0;
    int why = 0;
    int ids = this.width;
    for (int m = 1; m < this.height; m++) {
      why = size * m;
      for (int h = 0; h < this.width; h++) {
        ex = (int) (size / 2) + size * h;
        Vertex ver1 = this.vertices.get(ids);
        Vertex ver2 = this.vertices.get(ids - this.width);
        Edge edgy = new Edge(ex, why, size, sizeHeight, this.rand);
        edgy.vertices.add(ver1);
        edgy.vertices.add(ver2);
        ids++;
        this.print.add(edgy);
        this.orderedEdges.add(edgy);
      }
    }

  }

  // prints all vertices
  public WorldScene printV(WorldScene scene) {
    for (Vertex v : this.vertices) {
      v.printVertex(scene);
    }
    return scene;
  }

  // prints all edges
  public WorldScene printE(WorldScene scene) {
    for (Edge e : this.print) {
      e.printEdge(scene);
    }
    return scene;
  }

  // draws a scene
  @Override
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(800, 800);
    scene = this.printV(scene);
    scene = this.printE(scene);

    return scene;
  }

  @Override
  public void onTick() {
    if (this.dsft) {
      this.dsf();
    }
    if (this.bfst) {
      for (int i = 0; i < this.workList.size(); i++) {
        this.bfs();
      }
    }
  }

  // detects if the user presses "b" or "d" for dfs or bfs
  public void onKeyEvent(String key) {
    if (key.equals("b")) {
      this.bfst = true;
    }
    if (key.equals("d")) {
      this.dsft = true;
    }
    if (key.equals("r")) {
      Maze nm = new Maze(this.width, this.height);
      this.width = nm.width;
      this.height = nm.height;
      this.rand = nm.rand;
      this.vertices = nm.vertices;
      this.orderedEdges = nm.orderedEdges;
      this.connected = nm.connected;
      this.reps = nm.reps;
      this.print = nm.print;
      this.visited = nm.visited;
      this.workList = nm.workList;
      this.path = nm.path;
      this.bfst = false;
      this.dsft = false;
    }
  }

  // breadth first search
  void bfs() {
    ArrayList<Vertex> queue = new ArrayList<Vertex>();
    if (this.workList.get(0).equals(this.vertices.get(this.vertices.size() - 1))) {
      this.workList.get(0).col = Color.yellow;
      this.finalPath(this.vertices.get(this.vertices.size() - 1));
    }
    else {
      for (Edge edg : this.workList.get(0).edges) {
        Vertex me = this.workList.get(0);
        me.col = Color.BLUE;
        edg.col = Color.blue;
        Vertex comp = this.findVer(edg, me);

        if (!this.visited.contains(comp)) {
          queue.add(this.findVer(edg, this.workList.get(0)));
          this.visited.add(this.workList.get(0));
          this.path.replace(comp, edg);
        }
      }
      this.workList.remove(0);
      for (Vertex v : queue) {
        this.workList.add(v);
      }
    }

  }

  // draws the final path
  void finalPath(Vertex sumn) {

    sumn.col = Color.YELLOW;
    Edge edg = this.path.get(sumn);
    edg.col = Color.YELLOW;
    if (!sumn.equals(this.vertices.get(0))) {
      Vertex next = new Vertex(0, 0, 0);
      for (Vertex ver : edg.vertices) {
        if (!ver.equals(sumn)) {
          next = ver;
        }
      }
      next.col = Color.YELLOW;
      this.finalPath(next);
    }
  }

  // Depth First Search
  void dsf() {
    ArrayList<Vertex> queue = new ArrayList<Vertex>();
    if (this.workList.get(0).equals(this.vertices.get(this.vertices.size() - 1))) {
      this.workList.get(0).col = Color.YELLOW;
      this.finalPath(this.vertices.get(this.vertices.size() - 1));
    }
    else {
      for (Edge edg : this.workList.get(0).edges) {
        Vertex me = this.workList.get(0);
        me.col = Color.BLUE;
        edg.col = Color.BLUE;
        Vertex comp = this.findVer(edg, me);
        if (!this.visited.contains(comp)) {
          queue.add(this.findVer(edg, this.workList.get(0)));
          this.visited.add(this.workList.get(0));

          this.path.replace(comp, edg);
        }
      }
      this.workList.remove(0);
      for (Vertex v : queue) {
        this.workList.add(0, v);
      }
    }

  }

  // Kraskals
  void krask() {
    while (this.notDone()) {
      Vertex a = this.orderedEdges.get(0).vertices.get(0);
      Vertex b = this.orderedEdges.get(0).vertices.get(1);
      Vertex arep = this.find(a);
      Vertex brep = this.find(b);
      if (!(arep.equals(brep))) {
        this.connected.add(this.orderedEdges.get(0));
        this.union(arep, brep);
        this.orderedEdges.remove(0);

      }
      else {

        this.orderedEdges.remove(0);

      }
    }

  }

  // finds the other vertex of an Edge
  Vertex findVer(Edge edgy, Vertex from) {
    Vertex to = edgy.vertices.get(0);
    for (Vertex v : edgy.vertices) {
      if (!v.equals(from) && !this.visited.contains(v)) {
        to = v;
      }
    }
    return to;
  }

  // link all the vertices to the connected edges (path).
  void initVert() {
    for (Edge edg : this.connected) {
      Vertex ver1 = edg.vertices.get(0);
      Vertex ver2 = edg.vertices.get(1);
      ver1.edges.add(edg);
      ver2.edges.add(edg);
    }
  }

  // finds the representative
  // x always the representative.
  Vertex find(Vertex edgy) {
    Vertex x = this.reps.get(edgy);
    Vertex y = edgy;
    while (!(y.equals(x))) {

      y = x;
      x = this.reps.get(y);
    }
    return x;
  }

  // gives ver1 the same rep as ver2
  void union(Vertex ver1, Vertex ver2) {
    Vertex finding = this.reps.get(this.find(ver2));
    this.reps.replace(ver1, finding);

  }

  // determines when it should stop
  boolean notDone() {
    Vertex star = this.find(this.vertices.get(0));
    for (Vertex v : this.reps.keySet()) {
      if (!this.find(v).equals(star)) {
        return true;
      }
    }
    return false;
  }

}

//maze tester
class MazeTest {

  WorldScene scene1;
  WorldScene scene2;
  WorldScene scene3;
  WorldScene scene4;
  WorldScene scene5;
  WorldScene scene6;
  WorldScene scene7;
  WorldScene scene8;
  Vertex v1;
  Vertex v2;
  Vertex v3;
  Vertex v4;
  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;
  Maze m1;
  Maze m2;

  Maze seeded;
  Vertex sV1;
  Vertex sV2;
  Vertex sV3;
  Vertex sV4;
  ArrayList<Vertex> sVerts;
  Edge sE1;
  Edge sE2;
  Edge sE3;
  ArrayList<Edge> sEdges;

  void init() {
    this.v1 = new Vertex(40, 0, 0);
    this.v2 = new Vertex(40, 20, 0);
    this.v3 = new Vertex(40, 0, 20);
    this.v4 = new Vertex(40, 20, 20);
    this.scene1 = new WorldScene(800, 800);
    this.scene2 = new WorldScene(800, 800);
    this.scene3 = new WorldScene(800, 800);
    this.scene4 = new WorldScene(800, 800);
    this.scene5 = new WorldScene(800, 800);
    this.scene6 = new WorldScene(800, 800);
    this.scene7 = new WorldScene(800, 800);
    this.scene8 = new WorldScene(800, 800);
    this.e1 = new Edge(20, 40, 40, 40);
    this.e2 = new Edge(40, 20, 40, 40);
    this.e3 = new Edge(40, 40, 40, 40);
    this.e4 = new Edge(60, 40, 40, 40);
    this.m1 = new Maze(2, 2);
    this.m2 = new Maze(3, 2);

    this.seeded = new Maze(2, 2, 49);
    this.sV1 = new Vertex(200, 200, 400);
    this.sV2 = new Vertex(600, 200, 400);
    this.sV3 = new Vertex(200, 600, 400);
    this.sV4 = new Vertex(600, 600, 400);
    this.sVerts = new ArrayList<Vertex>(Arrays.asList(sV1, sV2, sV3, sV4));

    this.sE1 = new Edge(400, 600, 400, 41);
    sE1.val = 3;
    sE1.vertices.add(sV3);
    sE1.vertices.add(sV4);
    this.sE2 = new Edge(600, 400, 400, 41);
    sE2.val = 10;
    sE2.vertices.add(sV4);
    sE2.vertices.add(sV2);
    this.sE3 = new Edge(200, 400, 400, 41);
    sE3.val = 41;
    sE3.vertices.add(sV2);
    sE3.vertices.add(sV1);
    this.sEdges = new ArrayList<Edge>(Arrays.asList(sE1, sE2, sE3));

  }

  boolean testBigBang(Tester t) {
    Maze world = new Maze(50, 30, 50); // this are the dimensions of the maze.
    world.bigBang(800, 800, .02);
    ArrayList<Integer> nums = new ArrayList<Integer>();
    for (Vertex v : world.vertices) {
      nums.add(v.num);
    }
    return true;
  }

  @SuppressWarnings("unlikely-arg-type")
  void testVertex(Tester t) {
    init();
    this.v1.printVertex(this.scene1);
    this.scene2.placeImageXY(
        new RectangleImage(this.v1.size, this.v1.size, OutlineMode.SOLID, Color.GRAY), this.v1.x,
        this.v1.y);
    t.checkExpect(this.scene1, this.scene2); // Testing printVertex
    this.v2.printVertex(this.scene1);
    this.scene2.placeImageXY(
        new RectangleImage(this.v2.size, this.v2.size, OutlineMode.SOLID, Color.GRAY), this.v2.x,
        this.v2.y);
    t.checkExpect(this.scene1, this.scene2); // Testing printVertex

    t.checkExpect(v1.equals(v2), true); // Testing overridden equals method
    t.checkExpect(v1.equals(scene1), false); // testing a false

    t.checkExpect(v1.hashCode(), 29791); // Testing hashCode()
    t.checkExpect(v2.hashCode(), 49011);
  }

  void testEdge(Tester t) {
    init(); // testing print edge
    this.e1.printEdge(this.scene1);
    this.scene2.placeImageXY(
        new RectangleImage(this.e1.width, this.e1.height, OutlineMode.SOLID, this.e1.col),
        this.e1.x, this.e1.y);
    t.checkExpect(this.scene1, this.scene2);
    this.e2.printEdge(this.scene1);
    this.scene2.placeImageXY(
        new RectangleImage(this.e2.width, this.e2.height, OutlineMode.SOLID, this.e2.col),
        this.e2.x, this.e2.y);
    t.checkExpect(this.scene1, this.scene2);
  }

  void testMaze1(Tester t) { // test makeVertices, makeEdges, printV, printE
    init();
    t.checkExpect(this.m1.vertices.size(), 4); // a list of 4 vertices are being created
    t.checkExpect(this.m2.vertices.size(), 6);// makeVertices its called inside maze constructor
    t.checkExpect(this.m1.print.size(), 4);// makeEdges
    t.checkExpect(this.m2.print.size(), 7);// makeEdges is called inside maze constructor

    this.m2.vertices.get(0).printVertex(this.scene2);
    this.m2.vertices.get(1).printVertex(this.scene2);
    this.m2.vertices.get(2).printVertex(this.scene2);
    this.m2.vertices.get(3).printVertex(this.scene2);
    this.m2.vertices.get(4).printVertex(this.scene2);
    this.m2.vertices.get(5).printVertex(this.scene2);
    t.checkExpect(this.m2.printV(this.scene1), this.scene2); // Testing print vertices
    this.scene3.placeImageXY(new RectangleImage(400, 400, OutlineMode.SOLID, Color.GRAY),
        this.m1.vertices.get(0).x, this.m1.vertices.get(0).y);
    this.scene3.placeImageXY(new RectangleImage(400, 400, OutlineMode.SOLID, Color.GRAY),
        this.m1.vertices.get(1).x, this.m1.vertices.get(1).y);
    this.scene3.placeImageXY(new RectangleImage(400, 400, OutlineMode.SOLID, Color.GRAY),
        this.m1.vertices.get(2).x, this.m1.vertices.get(2).y);
    this.scene3.placeImageXY(new RectangleImage(400, 400, OutlineMode.SOLID, Color.GRAY),
        this.m1.vertices.get(3).x, this.m1.vertices.get(3).y);
    t.checkExpect(this.m1.printV(this.scene4), this.scene3); // more manual print

    this.m2.print.get(0).printEdge(scene5);
    this.m2.print.get(1).printEdge(scene5);
    this.m2.print.get(2).printEdge(scene5);
    this.m2.print.get(3).printEdge(scene5);
    this.m2.print.get(4).printEdge(scene5);
    this.m2.print.get(5).printEdge(scene5);
    this.m2.print.get(6).printEdge(scene5);
    t.checkExpect(this.m2.printE(this.scene6), this.scene5); // Testing print edges

    this.scene7.placeImageXY(
        new RectangleImage(this.m1.print.get(0).width, this.m1.print.get(0).height,
            OutlineMode.SOLID, this.m1.print.get(0).col),
        this.m1.print.get(0).x, this.m1.print.get(0).y);
    this.scene7.placeImageXY(
        new RectangleImage(this.m1.print.get(1).width, this.m1.print.get(1).height,
            OutlineMode.SOLID, this.m1.print.get(1).col),
        this.m1.print.get(1).x, this.m1.print.get(1).y);
    this.scene7.placeImageXY(
        new RectangleImage(this.m1.print.get(2).width, this.m1.print.get(2).height,
            OutlineMode.SOLID, this.m1.print.get(2).col),
        this.m1.print.get(2).x, this.m1.print.get(2).y);
    this.scene7.placeImageXY(
        new RectangleImage(this.m1.print.get(3).width, this.m1.print.get(3).height,
            OutlineMode.SOLID, this.m1.print.get(3).col),
        this.m1.print.get(3).x, this.m1.print.get(3).y);
    t.checkExpect(this.m1.printE(this.scene8), this.scene7); // Another way of inputs

  }

  void testMazeMakeScene(Tester t) {
    init();
    this.m2.printV(this.scene1);
    this.m2.printE(this.scene1);
    t.checkExpect(this.m2.makeScene(), this.scene1);

    this.scene3.placeImageXY(new RectangleImage(400, 400, OutlineMode.SOLID, Color.GRAY),
        this.m1.vertices.get(0).x, this.m1.vertices.get(0).y);
    this.scene3.placeImageXY(new RectangleImage(400, 400, OutlineMode.SOLID, Color.GRAY),
        this.m1.vertices.get(1).x, this.m1.vertices.get(1).y);
    this.scene3.placeImageXY(new RectangleImage(400, 400, OutlineMode.SOLID, Color.GRAY),
        this.m1.vertices.get(2).x, this.m1.vertices.get(2).y);
    this.scene3.placeImageXY(new RectangleImage(400, 400, OutlineMode.SOLID, Color.GRAY),
        this.m1.vertices.get(3).x, this.m1.vertices.get(3).y);

    this.scene3.placeImageXY(
        new RectangleImage(this.m1.print.get(0).width, this.m1.print.get(0).height,
            OutlineMode.SOLID, this.m1.print.get(0).col),
        this.m1.print.get(0).x, this.m1.print.get(0).y);
    this.scene3.placeImageXY(
        new RectangleImage(this.m1.print.get(1).width, this.m1.print.get(1).height,
            OutlineMode.SOLID, this.m1.print.get(1).col),
        this.m1.print.get(1).x, this.m1.print.get(1).y);
    this.scene3.placeImageXY(
        new RectangleImage(this.m1.print.get(2).width, this.m1.print.get(2).height,
            OutlineMode.SOLID, this.m1.print.get(2).col),
        this.m1.print.get(2).x, this.m1.print.get(2).y);
    this.scene3.placeImageXY(
        new RectangleImage(this.m1.print.get(3).width, this.m1.print.get(3).height,
            OutlineMode.SOLID, this.m1.print.get(3).col),
        this.m1.print.get(3).x, this.m1.print.get(3).y);
    t.checkExpect(this.m1.makeScene(), this.scene3);
  }

  void testKrask(Tester t) {
    Maze krask = new Maze(2, 2, 45);
    t.checkExpect(krask.connected.size(), 3); // A 2 by 2 maze should be connected through 3
    Maze krask1 = new Maze(3, 2, 45); // edges always.
    t.checkExpect(krask1.connected.size(), 5); // A 3 by 2 maze should be connected through 5
  }

  void testUnion(Tester t) {
    Maze uni = new Maze(3, 3, 49);
    t.checkExpect(uni.reps.get(uni.vertices.get(0)), uni.vertices.get(2));
    t.checkExpect(uni.reps.get(uni.vertices.get(1)), uni.vertices.get(2));

  }

  void testFind(Tester t) {
    Maze findie1 = new Maze(2, 2, 49);
    t.checkExpect(findie1.find(findie1.vertices.get(3)), findie1.vertices.get(0));
    Maze findie2 = new Maze(2, 2, 13);
    t.checkExpect(findie2.find(findie2.vertices.get(3)), findie2.vertices.get(1));
  }

  void testEdgesconnect(Tester t) {
    Maze dume = new Maze(3, 3, 78);
    t.checkExpect(dume.vertices.get(4).edges.size(), 3);
    t.checkExpect(dume.vertices.get(0).edges.size(), 1);
    t.checkExpect(dume.vertices.get(0).edges.get(0), dume.connected.get(2));

  }

  void testFindVer(Tester t) {
    Maze sumn = new Maze(2, 2, 13);
    t.checkExpect(sumn.findVer(sumn.connected.get(0), sumn.vertices.get(0)), sumn.vertices.get(2));
    t.checkExpect(sumn.findVer(sumn.connected.get(2), sumn.vertices.get(0)), sumn.vertices.get(1));
  }

  void testbfs(Tester t) {
    Maze sumn = new Maze(2, 2, 13);
    t.checkExpect(sumn.vertices.get(0).col, Color.GRAY);
    sumn.bfs();
    t.checkExpect(sumn.vertices.get(0).col, Color.BLUE);
    sumn.bfs();
    sumn.bfs();
    sumn.bfs();
    sumn.bfs(); // this one calls final path
    t.checkExpect(sumn.vertices.get(0).col, Color.YELLOW);
    t.checkExpect(sumn.vertices.get(3).col, Color.YELLOW); // last vertex
  }

  void testdfs(Tester t) {
    Maze sumn = new Maze(2, 2, 13);
    t.checkExpect(sumn.vertices.get(0).col, Color.GRAY);
    sumn.dsf();
    t.checkExpect(sumn.vertices.get(0).col, Color.BLUE);
    sumn.dsf();
    sumn.dsf();
    sumn.dsf(); // this one calls final path
    t.checkExpect(sumn.vertices.get(0).col, Color.YELLOW);
    t.checkExpect(sumn.vertices.get(3).col, Color.YELLOW); // last vertex
  }

  void testfinalPath(Tester t) {
    Maze sumn = new Maze(2, 2, 13);

    t.checkExpect(sumn.path.get(sumn.vertices.get(1)).x, 0);
    t.checkExpect(sumn.path.get(sumn.vertices.get(3)).y, 0);
    sumn.bfs();
    sumn.bfs();
    sumn.bfs();
    sumn.bfs();
    sumn.bfs();
    sumn.finalPath(sumn.vertices.get(3));
    t.checkExpect(sumn.path.get(sumn.vertices.get(1)).x, 400);
    t.checkExpect(sumn.path.get(sumn.vertices.get(3)).y, 600);
  }
}