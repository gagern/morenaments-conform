import java.util.Arrays;
import java.util.List;

class Face {

    private List<Vertex> vs;

    public Face(Vertex... vs) {
	this.vs = Arrays.asList(vs);
    }

    public Face(List<Vertex> vs) {
	this.vs = vs;
    }

    public List<Vertex> getVertices() {
	return vs;
    }

}
