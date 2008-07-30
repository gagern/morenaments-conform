package net.von_gagern.martin.morenaments.conformal;

import java.io.IOException;

class CircleTriangulation extends HexagonalMesh {

    public static void main(String[] args) throws IOException {
        CircleTriangulation c = new CircleTriangulation();
        c.write("circ");
    }

    public CircleTriangulation() {
        super();
        addBoundary(new CircleBoundary(0, 0, 1, false));
    }

}
