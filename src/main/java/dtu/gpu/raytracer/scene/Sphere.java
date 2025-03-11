package dtu.gpu.raytracer.scene;

import dtu.gpu.raytracer.Vector3;

public class Sphere {
    public Vector3 center;
    public double radius;
    public int materialIndex;

    public Sphere(Vector3 center, double radius, int materialIndex) {
        this.center = center;
        this.radius = radius;
        this.materialIndex = materialIndex;
    }
}

