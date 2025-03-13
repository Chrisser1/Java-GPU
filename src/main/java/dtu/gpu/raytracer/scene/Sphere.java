package dtu.gpu.raytracer.scene;

import dtu.gpu.raytracer.Vector3;

public class Sphere {
    public Vector3 center;
    public double radius;
    public int materialIndex;
    public Vector3 albedo;  // Color
    public double fuzz;      // Metal roughness
    public double ref_idx;   // Dielectric refraction

    // **Constructor for Lambertian**
    public Sphere(Vector3 center, double radius, int materialIndex, Vector3 albedo) {
        this.center = center;
        this.radius = radius;
        this.materialIndex = materialIndex;
        this.albedo = albedo;
        this.fuzz = 0.0;
        this.ref_idx = 0.0;
    }

    // **Constructor for Metal**
    public Sphere(Vector3 center, double radius, int materialIndex, Vector3 albedo, double fuzz) {
        this.center = center;
        this.radius = radius;
        this.materialIndex = materialIndex;
        this.albedo = albedo;
        this.fuzz = fuzz;
        this.ref_idx = 0.0;
    }

    // **Constructor for Dielectric**
    public Sphere(Vector3 center, double radius, int materialIndex, double ref_idx) {
        this.center = center;
        this.radius = radius;
        this.materialIndex = materialIndex;
        this.albedo = new Vector3(1.0, 1.0, 1.0);  // Default white for dielectric
        this.fuzz = 0.0;
        this.ref_idx = ref_idx;
    }
}
