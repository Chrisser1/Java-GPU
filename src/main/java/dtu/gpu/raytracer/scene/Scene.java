package dtu.gpu.raytracer.scene;

import dtu.gpu.raytracer.Camera;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private Camera camera;
    private List<Sphere> spheres;

    public Scene(Camera camera) {
        this.camera = camera;
        this.spheres = new ArrayList<>();
    }

    public Camera getCamera() {
        return camera;
    }

    public List<Sphere> getSpheres() {
        return spheres;
    }

    public void addSphere(Sphere sphere) {
        spheres.add(sphere);
    }
}
