package net.von_gagern.martin.morenaments.conformal;

import java.util.Collection;

abstract class MeshCreator {

    private int targetCount = 1296;

    abstract protected Collection<Triangle> createMesh(Triangle t);

    public int getTargetTriangleCount() {
        return targetCount;
    }

    public void setTargetTriangleCount(int count) {
        targetCount = count;
    }

}
