package net.von_gagern.martin.morenaments.conformal.triangulate;

import java.io.*;

class ObjWriter implements MeshWriter {

    private int vertexType = -1;

    private PrintStream out;

    public ObjWriter(String fileName) throws IOException {
        this(new File(fileName));
    }

    public ObjWriter(File file) throws IOException {
        this(new FileOutputStream(file));
    }

    public ObjWriter(OutputStream out) {
        this(new PrintStream(out));
    }

    public ObjWriter(PrintStream out) {
        this.out = out;
    }

    public void name(String name) {
        if (name != null) {
            out.print("g ");
            out.println(name);
        }
    }

    public void vertex(Vertex v) {
        if (v.getType() != vertexType) {
            vertexType = v.getType();
            out.print("# vertices of type ");
            out.println(vertexType);
        }
        out.print("v ");
        out.print(v.getX());
        out.print(" ");
        out.print(v.getY());
        out.println(" 0");
    }

    public void face(Face f) {
        out.print("f");
        for (Vertex v: f.getVertices()) {
            out.print(" ");
            out.print(v.getIndex());
            // out.print("//");
        }
        out.println();
    }

    public void close() throws IOException {
        out.close();
        if (out.checkError())
            throw new IOException("There were errors writing the mesh.");
    }

}
