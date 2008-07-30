package net.von_gagern.martin.morenaments.conformal;

import java.io.IOException;

interface MeshWriter {

    public void name(String n) throws IOException;

    public void vertex(Vertex v) throws IOException;

    public void face(Face f) throws IOException;

    public void close() throws IOException;

}
