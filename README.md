# Java-GPU Ray Tracer

A GPU-accelerated Ray Tracer in Java, built with [JOCL](http://www.jocl.org/) and inspired by the [*Ray Tracing in One Weekend*](https://raytracing.github.io/books/RayTracingInOneWeekend.html) tutorial series.

![image.png](/images/example.png)
## Overview

This project demonstrates a path-tracing style renderer that uses OpenCL (via JOCL) to accelerate ray tracing computations on the GPU.

**Key features:**
- **GPU Acceleration:** JOCL is used to compile and run OpenCL kernels for fast ray-scene intersection tests.
- **Interactive Camera Controls:** Move around the scene in real time with keyboard inputs (WASD, arrow keys, etc.).
- **Materials:** Includes Lambertian (diffuse), Metal (reflective), and Dielectric (refractive) materials.
- **Randomized World Generation:** Randomly places spheres with different materials, as outlined in *Ray Tracing in One Weekend*.

## Getting Started

### Prerequisites

1. **Java 21+**
2. **Maven** (for building)
3. **OpenCL-compatible GPU** and drivers installed
4. **JOCL** (handled automatically via Maven dependencies)

### Cloning the Repository

```bash
git clone https://github.com/Chrisser1/Java-GPU.git
cd Java-GPU
```

### Building and running
1. Build the project with Maven
```bash
mvn clean install
```
or simply
```bash
mvn package
```
### Keyboard Controls
* **W / S**: Move forward / backward (relative to camera orientation)
* **A / D**: Strafe left / right
* **UP / DOWN**: Pitch camera up / down
* **LEFT / RIGHT**: Yaw camera left / right
* **SPACE / SHIFT**: Move camera up / down
* **ESC**: Quit the application