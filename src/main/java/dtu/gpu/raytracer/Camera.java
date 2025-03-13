package dtu.gpu.raytracer;

public class Camera {
    private final double aspectRatio;
    private final double viewportHeight;
    private final double viewportWidth;
    private final double focalLength;

    private Vector3 origin;
    private Vector3 horizontal;
    private Vector3 vertical;
    private Vector3 lowerLeftCorner;

    public Camera(double aspectRatio, double viewportHeight, double focalLength, Vector3 origin) {
        this.aspectRatio = aspectRatio;
        this.viewportHeight = viewportHeight;
        this.viewportWidth = aspectRatio * viewportHeight;
        this.focalLength = focalLength;
        this.origin = origin;
        computeCameraParameters();
    }

    private void computeCameraParameters() {
        // Calculate horizontal and vertical spans.
        this.horizontal = new Vector3(viewportWidth, 0, 0);
        this.vertical = new Vector3(0, viewportHeight, 0);
        // Compute the lower-left corner from the current origin.
        this.lowerLeftCorner = origin
                .subtract(horizontal.multiply(0.5))
                .subtract(vertical.multiply(0.5))
                .subtract(new Vector3(0, 0, focalLength));
    }

    /**
     * Moves the camera by the given delta and updates the derived parameters.
     */
    public void move(Vector3 delta) {
        this.origin = this.origin.add(delta);
        computeCameraParameters();
    }

    // Getters for camera parameters
    public Vector3 getOrigin() {
        return origin;
    }
    public Vector3 getHorizontal() {
        return horizontal;
    }
    public Vector3 getVertical() {
        return vertical;
    }
    public Vector3 getLowerLeftCorner() {
        return lowerLeftCorner;
    }
    public double getAspectRatio() {
        return aspectRatio;
    }
    public double getViewportHeight() {
        return viewportHeight;
    }
    public double getFocalLength() {
        return focalLength;
    }
}
