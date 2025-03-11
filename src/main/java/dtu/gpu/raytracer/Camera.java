package dtu.gpu.raytracer;

public class Camera {
    private final double aspectRatio;
    private final double viewportHeight;
    private final double viewportWidth;
    private final double focalLength;

    private final Vector3 origin;
    private final Vector3 horizontal;
    private final Vector3 vertical;
    private final Vector3 lowerLeftCorner;

    public Camera(double aspectRatio, double viewportHeight, double focalLength, Vector3 origin) {
        this.aspectRatio = aspectRatio;
        this.viewportHeight = viewportHeight;
        this.viewportWidth = aspectRatio * viewportHeight;
        this.focalLength = focalLength;
        this.origin = origin;
        // Horizontal span of the viewport
        this.horizontal = new Vector3(viewportWidth, 0, 0);
        // Vertical span (using positive y)
        this.vertical = new Vector3(0, viewportHeight, 0);
        // Lower-left corner of the viewport:
        this.lowerLeftCorner = origin
                .subtract(horizontal.multiply(0.5))
                .subtract(vertical.multiply(0.5))
                .subtract(new Vector3(0, 0, focalLength));
    }

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
