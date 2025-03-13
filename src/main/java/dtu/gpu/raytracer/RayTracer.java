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
    public static void main(String[] args) {
        int imageWidth = 800;
        double aspectRatio = 16.0 / 9.0;
        int imageHeight = (int) (imageWidth / aspectRatio);

        Camera camera = new Camera(aspectRatio, 2.0, 1.0, new Vector3(0, 0, 1));
        Scene scene = new Scene(camera);
        Renderer renderer = new Renderer(imageWidth, imageHeight);

        // Add spheres
        scene.addSphere(new Sphere(new Vector3(0,0,-1), 0.5, 1));
        scene.addSphere(new Sphere(new Vector3(0,-100.5,-1), 100, 1));

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
                // Move forward/backward, left/right with WASD keys.
                if (key == KeyEvent.VK_W) {
                    delta = new Vector3(0, 0, -0.1);
                } else if (key == KeyEvent.VK_S) {
                    delta = new Vector3(0, 0, 0.1);
                } else if (key == KeyEvent.VK_A) {
                    delta = new Vector3(-0.1, 0, 0);
                } else if (key == KeyEvent.VK_D) {
                    delta = new Vector3(0.1, 0, 0);
                }
                camera.move(delta);
                renderer.render(scene);
                imageLabel.setIcon(new ImageIcon(renderer.getImage()));
                imageLabel.repaint();
            }
        });
    }
}
