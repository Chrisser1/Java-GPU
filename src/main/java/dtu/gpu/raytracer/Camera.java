package dtu.gpu.raytracer;

public class Camera {
    private final double aspectRatio;
    private final double vfov;  // Vertical Field of View
    private final double focalLength;

    private Vector3 origin;
    private Vector3 horizontal;
    private Vector3 vertical;
    private Vector3 lowerLeftCorner;

    // Fields for camera orientation
    private double yaw;      // rotation around the vertical axis (in degrees)
    private double pitch;    // rotation around the horizontal axis (in degrees)
    private Vector3 lookDirection;
    private final Vector3 worldUp;
    private Vector3 right;
    private Vector3 up;

    /**
     * Constructs a camera with the given parameters and an initial "look at" point.
     *
     * @param aspectRatio the aspect ratio of the viewport
     * @param vfov the vertical field of view (in degrees)
     * @param focalLength the focal length (distance to the viewport)
     * @param origin the camera position in world space
     * @param lookAt the point in space the camera should initially look at
     */
    public Camera(double aspectRatio, double vfov, double focalLength, Vector3 origin, Vector3 lookAt) {
        this.aspectRatio = aspectRatio;
        this.vfov = vfov;
        this.focalLength = focalLength;
        this.origin = origin;
        this.worldUp = new Vector3(0, 1, 0);

        // Calculate the initial look direction from origin to lookAt.
        Vector3 initialDirection = lookAt.subtract(origin).normalize();

        // Calculate pitch from the y component.
        this.pitch = Math.toDegrees(Math.asin(initialDirection.getY()));

        // Calculate yaw using the x and z components.
        // Note: atan2 returns the angle relative to the positive x-axis.
        this.yaw = Math.toDegrees(Math.atan2(initialDirection.getZ(), initialDirection.getX()));

        updateCameraVectors();
        computeCameraParameters();
    }

    // Recalculate the camera’s basis vectors based on the current yaw and pitch.
    private void updateCameraVectors() {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        // Compute new look direction using spherical coordinates.
        lookDirection = new Vector3(
                Math.cos(pitchRad) * Math.cos(yawRad),
                Math.sin(pitchRad),
                Math.cos(pitchRad) * Math.sin(yawRad)
        ).normalize();

        // Compute the right and up vectors.
        right = lookDirection.cross(worldUp).normalize();
        up = right.cross(lookDirection).normalize();
    }

    // Recalculate viewport parameters based on the current orientation.
    private void computeCameraParameters() {
        double theta = Math.toRadians(vfov);
        double h = Math.tan(theta / 2);
        double viewportHeight = 2.0 * h * focalLength;
        double viewportWidth = viewportHeight * aspectRatio;

        horizontal = right.multiply(viewportWidth);
        vertical = up.multiply(viewportHeight);

        // The viewport center is at origin + lookDirection * focalLength.
        // Lower left corner is the center minus half the horizontal and vertical spans.
        lowerLeftCorner = origin.add(lookDirection.multiply(focalLength))
                .subtract(horizontal.multiply(0.5))
                .subtract(vertical.multiply(0.5));
    }

    /**
     * Rotates the camera by the given yaw (left/right) and pitch (up/down) deltas.
     */
    public void rotate(double yawDelta, double pitchDelta) {
        yaw += yawDelta;
        pitch += pitchDelta;
        // Clamp pitch to avoid extreme angles (and potential gimbal lock)
        if (pitch > 89.0) pitch = 89.0;
        if (pitch < -89.0) pitch = -89.0;

        updateCameraVectors();
        computeCameraParameters();
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
    public double getFocalLength() {
        return focalLength;
    }
    public double getVfov() {
        return vfov;
    }
    public double getYaw() {
        return yaw;
    }
    public double getPitch() {
        return pitch;
    }
    public Vector3 getLookDirection() {
        return lookDirection;
    }
    public Vector3 getWorldUp() {
        return worldUp;
    }
    public Vector3 getRight() {
        return right;
    }
    public Vector3 getUp() {
        return up;
    }
}
