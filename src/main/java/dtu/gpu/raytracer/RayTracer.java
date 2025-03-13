package dtu.gpu.raytracer;

import dtu.gpu.raytracer.scene.Scene;
import dtu.gpu.raytracer.scene.Sphere;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RayTracer {
    private static final boolean DEBUG = false;

    public static void main(String[] args) {
        int imageWidth = 1200;
        double aspectRatio = 16.0 / 9.0;
        double vFov = 20.0;
        double focalLength = 1.0;
        int imageHeight = (int) (imageWidth / aspectRatio);

        Camera camera = new Camera(aspectRatio, vFov, focalLength, new Vector3(13,2,3),  new Vector3(0,0,0));
        Scene scene = new Scene(camera);
        Renderer renderer = new Renderer(imageWidth, imageHeight);
        renderer.setDebug(DEBUG);
        renderer.setSamplesPrPixel(500);
        renderer.setMaxDepth(50);

        // Define material indices
        // 0: Lambertian (diffuse)
        // 1: Metal
        // 2: Dielectric (glass)

        // --- Ground sphere ---
        // A large sphere as the ground with a gray diffuse material
        scene.addSphere(new Sphere(
                new Vector3(0, -1000, 0),
                1000,
                0, // Lambertian
                new Vector3(0.5, 0.5, 0.5)
        ));

        // --- Random small spheres ---
        for (int a = -11; a < 11; a++) {
            for (int b = -11; b < 11; b++) {
                double chooseMat = Math.random();
                Vector3 center = new Vector3(
                        a + 0.9 * Math.random(),
                        0.2,
                        b + 0.9 * Math.random()
                );

                // Prevent spheres from overlapping with the large spheres near (4, 0.2, 0)
                if (center.subtract(new Vector3(4, 0.2, 0)).length() > 0.9) {
                    if (chooseMat < 0.8) {
                        // Diffuse (Lambertian)
                        // albedo = random * random (component-wise)
                        Vector3 albedo = new Vector3(
                                Math.random() * Math.random(),
                                Math.random() * Math.random(),
                                Math.random() * Math.random()
                        );
                        scene.addSphere(new Sphere(center, 0.2, 0, albedo));
                    } else if (chooseMat < 0.95) {
                        // Metal
                        // albedo with each component in [0.5, 1]
                        double r = 0.5 + Math.random() * 0.5;
                        double g = 0.5 + Math.random() * 0.5;
                        double bColor = 0.5 + Math.random() * 0.5;
                        Vector3 albedo = new Vector3(r, g, bColor);
                        double fuzz = Math.random() * 0.5;
                        scene.addSphere(new Sphere(center, 0.2, 1, albedo, fuzz));
                    } else {
                        // Glass (dielectric)
                        scene.addSphere(new Sphere(center, 0.2, 2, 1.5));
                    }
                }
            }
        }

        // --- Three large spheres ---
        // A glass sphere at the center
        scene.addSphere(new Sphere(new Vector3(0, 1, 0), 1.0, 2, 1.5));

        // A diffuse sphere on the left
        scene.addSphere(new Sphere(new Vector3(-4, 1, 0), 1.0, 0, new Vector3(0.4, 0.2, 0.1)));

        // A metal sphere on the right
        scene.addSphere(new Sphere(new Vector3(4, 1, 0), 1.0, 1, new Vector3(0.7, 0.6, 0.5), 0.0));

        // Initial render
        renderer.render(scene);

        // Create and display the image in a JFrame
        JFrame frame = new JFrame("Ray Tracer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel imageLabel = new JLabel(new ImageIcon(renderer.getImage()));
        frame.add(imageLabel);
        frame.pack();
        frame.setVisible(true);

        // Add a component listener to update the renderer when resized
        imageLabel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension newSize = imageLabel.getSize();
                int newWidth = newSize.width;
                int newHeight = newSize.height;
                // Update the renderer with the new dimensions
                renderer.updateImageSize(newWidth, newHeight);
                // Re-render the scene
                renderer.render(scene);
                // Update the display
                imageLabel.setIcon(new ImageIcon(renderer.getImage()));
                imageLabel.repaint();
            }
        });

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                Vector3 delta = new Vector3(0, 0, 0);
                double moveSpeed = 0.1;
                double rotationSpeed = 2.0; // degrees per press

                if (key == KeyEvent.VK_W) {
                    // Forward: use the camera's look direction (projected on the horizontal plane)
                    Vector3 forward = camera.getLookDirection();
                    forward = new Vector3(forward.getX(), 0, forward.getZ()).normalize();
                    delta = forward.multiply(moveSpeed);
                } else if (key == KeyEvent.VK_S) {
                    // Backward
                    Vector3 forward = camera.getLookDirection();
                    forward = new Vector3(forward.getX(), 0, forward.getZ()).normalize();
                    delta = forward.multiply(-moveSpeed);
                } else if (key == KeyEvent.VK_A) {
                    // Strafe left: negative right vector
                    delta = camera.getRight().multiply(-moveSpeed);
                } else if (key == KeyEvent.VK_D) {
                    // Strafe right: use the right vector
                    delta = camera.getRight().multiply(moveSpeed);
                } else if (key == KeyEvent.VK_SPACE) {
                    // Move upward (global up or camera's up if you prefer)
                    delta = new Vector3(0, moveSpeed, 0);
                } else if (key == KeyEvent.VK_SHIFT) {
                    // Move downward
                    delta = new Vector3(0, -moveSpeed, 0);
                } else if (key == KeyEvent.VK_UP) {
                    camera.rotate(0, rotationSpeed); // pitch up
                } else if (key == KeyEvent.VK_DOWN) {
                    camera.rotate(0, -rotationSpeed);  // pitch down
                } else if (key == KeyEvent.VK_LEFT) {
                    camera.rotate(-rotationSpeed, 0); // yaw left
                } else if (key == KeyEvent.VK_RIGHT) {
                    camera.rotate(rotationSpeed, 0);  // yaw right
                } else if (key == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }

                // Apply translation if any delta was set
                if (!delta.equals(new Vector3(0, 0, 0))) {
                    camera.move(delta);
                }

                renderer.render(scene);
                imageLabel.setIcon(new ImageIcon(renderer.getImage()));
                imageLabel.repaint();
            }
        });
    }
}
