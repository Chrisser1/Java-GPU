package dtu.gpu.raytracer;

public class Vector3 {
    private final double x;
    private final double y;
    private final double z;

    public Vector3(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }
    public Vector3 subtract(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }
    public Vector3 multiply(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }
    public double dot(Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }
    public Vector3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        return new Vector3(x / length, y / length, z / length);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
