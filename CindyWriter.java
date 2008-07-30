import java.io.*;
import java.util.zip.GZIPOutputStream;

class CindyWriter implements MeshWriter {

    private PrintStream out;

    private int faceIndex;

    public CindyWriter(String fileName) throws IOException {
	this(new File(fileName));
    }

    public CindyWriter(File file) throws IOException {
	this(new FileOutputStream(file));
    }

    public CindyWriter(OutputStream out) throws IOException {
	out = new GZIPOutputStream(out);
	this.out = new PrintStream(out);
	faceIndex = 0;
	head();
    }

    public void name(String name) {
    }

    public void vertex(Vertex v) {
	String n = "V" + v.getIndex();
	out.print("(\""+n+"\"):=FreePoint([");
	out.print(v.getX());
	out.print("+i*0.0,");
	out.print(v.getY());
	out.println("+i*0.0,4.0+i*0.0]);");
	out.println("\""+n+"\".setAppearance(2,1,1,0,0,10,false,false);");
    }

    public void face(Face f) {
	String n = "F" + (++faceIndex), d = "\"V";
	out.print("(\""+n+"\"):=Poly(");
	for (Vertex v: f.getVertices()) {
	    out.print(d);
	    out.print(v.getIndex());
	    out.print("\"");
	    d = ",\"V";
	}
	out.println(");");
	out.println("\""+n+"\".setAppearance(1,5,1,0,0,10,false,false);");
    }

    public void close() throws IOException {
	tail();
	out.close();
	if (out.checkError())
	    throw new IOException("There were errors writing the mesh.");
    }

    private void head() {
	out.println("//Cindy-2.1 build 984 (2008/05/23 16:45)");
	out.println("//Created on: 16.07.2008 16:03:53");
	out.println("//For: mvg");
	out.println("//DO NOT EDIT --- MACHINE GENERATED CODE");
	out.println("Geometry:=Euclidean;");
    }

    private void tail() {
	out.println("Geometry:=Euclidean;");
	out.println("port EuclideanPort() {");
	out.println("   setAttribute(\"euclideanport.scale\",\"1408\");");
	out.println("   setPolar(false);");
	out.println("   setPortWidth(978);");
	out.println("   setPortHeight(858);");
	out.println("   setScale(1408);");
	out.println("   setOriginY(490);");
	out.println("   setOriginX(429);");
	out.println("   setMesh(false);");
	out.println("   setAxes(false);");
	out.println("   setSnap(false);");
	out.println("}");
    }
}
