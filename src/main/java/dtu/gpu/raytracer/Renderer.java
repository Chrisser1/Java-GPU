package dtu.gpu.raytracer;

import static org.jocl.CL.*;
import java.awt.image.BufferedImage;
import dtu.gpu.raytracer.scene.Scene;
import dtu.gpu.raytracer.scene.Sphere;
import java.util.List;
import org.jocl.*;

public class Renderer {
    private boolean debug = false;
    private int samplesPrPixel = 50;
    private int maxDepth = 10;
    private int width, height;
    private BufferedImage image;
    private OpenCLManager openCLManager;

    public Renderer(int width, int height) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.openCLManager = new OpenCLManager(width, height);
    }

    public void render(Scene scene) {
        Camera camera = scene.getCamera();
        sendCameraDataToOpenCL(camera);

        cl_mem[] sphereBuffers = createSphereBuffers(scene);
        int numSpheres = scene.getSpheres().size();
        cl_kernel kernel = openCLManager.getKernel();

        clSetKernelArg(kernel, 7, Sizeof.cl_mem, Pointer.to(sphereBuffers[0])); // centerX
        clSetKernelArg(kernel, 8, Sizeof.cl_mem, Pointer.to(sphereBuffers[1])); // centerY
        clSetKernelArg(kernel, 9, Sizeof.cl_mem, Pointer.to(sphereBuffers[2])); // centerZ
        clSetKernelArg(kernel, 10, Sizeof.cl_mem, Pointer.to(sphereBuffers[3])); // radii
        clSetKernelArg(kernel, 11, Sizeof.cl_mem, Pointer.to(sphereBuffers[4])); // materialIndices

        clSetKernelArg(kernel, 12, Sizeof.cl_mem, Pointer.to(sphereBuffers[5])); // albedoR
        clSetKernelArg(kernel, 13, Sizeof.cl_mem, Pointer.to(sphereBuffers[6])); // albedoG
        clSetKernelArg(kernel, 14, Sizeof.cl_mem, Pointer.to(sphereBuffers[7])); // albedoB
        clSetKernelArg(kernel, 15, Sizeof.cl_mem, Pointer.to(sphereBuffers[8])); // fuzz
        clSetKernelArg(kernel, 16, Sizeof.cl_mem, Pointer.to(sphereBuffers[9])); // refIdx
        clSetKernelArg(kernel, 17, Sizeof.cl_int, Pointer.to(new int[]{ numSpheres }));


        // Convert the boolean to an int: 1 for true, 0 for false
        int debugFlag = debug ? 1 : 0;
        clSetKernelArg(kernel, 18, Sizeof.cl_int, Pointer.to(new int[] { debugFlag }));
        clSetKernelArg(kernel, 19, Sizeof.cl_int, Pointer.to(new int[] { samplesPrPixel }));
        clSetKernelArg(kernel, 20, Sizeof.cl_int, Pointer.to(new int[] { maxDepth }));


        long[] globalWorkSize = { width, height };
        clEnqueueNDRangeKernel(openCLManager.getCommandQueue(),
                kernel, 2, null, globalWorkSize, null, 0, null, null);

        int[] pixelData = new int[width * height];
        clEnqueueReadBuffer(openCLManager.getCommandQueue(), openCLManager.getPixelMem(),
                CL_TRUE, 0, (long) Sizeof.cl_int * width * height, Pointer.to(pixelData), 0, null, null);
        image.setRGB(0, 0, width, height, pixelData, 0, width);

        for (cl_mem buffer : sphereBuffers) {
            clReleaseMemObject(buffer);
        }
    }

    /**
     * Prepares and sends camera data to the kernel.
     * Kernel expects:
     * arg0: pixelMem (__global int*)
     * arg1: imageWidth (int)
     * arg2: imageHeight (int)
     * arg3: camera_center (float3, passed as 4 floats for alignment)
     * arg4: lowerLeftCorner (float3)
     * arg5: horizontal (float3)
     * arg6: vertical (float3)
     */
    private void sendCameraDataToOpenCL(Camera camera) {
        cl_kernel kernel = openCLManager.getKernel();

        // Set arg0: pixel memory buffer
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(openCLManager.getPixelMem()));
        // Set arg1: imageWidth (as int)
        clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[]{ width }));
        // Set arg2: imageHeight (as int)
        clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[]{ height }));

        // Get camera parameters from the Camera instance.
        Vector3 origin = camera.getOrigin();
        Vector3 lowerLeftCorner = camera.getLowerLeftCorner();
        Vector3 horizontal = camera.getHorizontal();
        Vector3 vertical = camera.getVertical();

        // Set arg3: camera_center (float3)
        clSetKernelArg(kernel, 3, 4 * Sizeof.cl_float, Pointer.to(new float[]{
                (float)origin.getX(), (float)origin.getY(), (float)origin.getZ(), 0.0f
        }));
        // Set arg4: lowerLeftCorner (float3)
        clSetKernelArg(kernel, 4, 4 * Sizeof.cl_float, Pointer.to(new float[]{
                (float)lowerLeftCorner.getX(), (float)lowerLeftCorner.getY(), (float)lowerLeftCorner.getZ(), 0.0f
        }));
        // Set arg5: horizontal (float3)
        clSetKernelArg(kernel, 5, 4 * Sizeof.cl_float, Pointer.to(new float[]{
                (float)horizontal.getX(), (float)horizontal.getY(), (float)horizontal.getZ(), 0.0f
        }));
        // Set arg6: vertical (float3)
        clSetKernelArg(kernel, 6, 4 * Sizeof.cl_float, Pointer.to(new float[]{
                (float)vertical.getX(), (float)vertical.getY(), (float)vertical.getZ(), 0.0f
        }));
    }

    /**
     * Converts the list of spheres in the scene into a flat float array and creates
     * an OpenCL buffer for them. Each sphere is stored as 4 floats: center.x, center.y, center.z, radius.
     * This buffer is then passed to the kernel as argument 7, and the number of spheres as argument 8.
     */
    private cl_mem[] createSphereBuffers(Scene scene) {
        List<Sphere> sphereList = scene.getSpheres();
        int numSpheres = sphereList.size();
        if (numSpheres == 0) {
            numSpheres = 1;
            sphereList.add(new Sphere(
                    new Vector3(0, -100.5, -1), 100, 0,
                    new Vector3(0.8, 0.8, 0.0)));  // Ground (lambertian)
        }

        float[] centerX = new float[numSpheres];
        float[] centerY = new float[numSpheres];
        float[] centerZ = new float[numSpheres];
        float[] radii = new float[numSpheres];
        int[] materialIndices = new int[numSpheres];

        float[] albedoR = new float[numSpheres];
        float[] albedoG = new float[numSpheres];
        float[] albedoB = new float[numSpheres];
        float[] fuzz = new float[numSpheres];
        float[] refIdx = new float[numSpheres];

        for (int i = 0; i < numSpheres; i++) {
            Sphere s = sphereList.get(i);
            centerX[i] = (float) s.center.getX();
            centerY[i] = (float) s.center.getY();
            centerZ[i] = (float) s.center.getZ();
            radii[i] = (float) s.radius;
            materialIndices[i] = s.materialIndex;

            albedoR[i] = (float) s.albedo.getX();
            albedoG[i] = (float) s.albedo.getY();
            albedoB[i] = (float) s.albedo.getZ();
            fuzz[i] = (float) s.fuzz;
            refIdx[i] = (float) s.ref_idx;
        }

        cl_context context = openCLManager.getContext();
        cl_mem centerXBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) Sizeof.cl_float * centerX.length, Pointer.to(centerX), null);
        cl_mem centerYBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) Sizeof.cl_float * centerY.length, Pointer.to(centerY), null);
        cl_mem centerZBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) Sizeof.cl_float * centerZ.length, Pointer.to(centerZ), null);
        cl_mem radiiBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) Sizeof.cl_float * radii.length, Pointer.to(radii), null);
        cl_mem materialIndexBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) Sizeof.cl_int * materialIndices.length, Pointer.to(materialIndices), null);

        cl_mem albedoRBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) Sizeof.cl_float * albedoR.length, Pointer.to(albedoR), null);
        cl_mem albedoGBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) Sizeof.cl_float * albedoG.length, Pointer.to(albedoG), null);
        cl_mem albedoBBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) Sizeof.cl_float * albedoB.length, Pointer.to(albedoB), null);
        cl_mem fuzzBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) Sizeof.cl_float * fuzz.length, Pointer.to(fuzz), null);
        cl_mem refIdxBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) Sizeof.cl_float * refIdx.length, Pointer.to(refIdx), null);

        return new cl_mem[]{
                centerXBuffer, centerYBuffer, centerZBuffer, radiiBuffer, materialIndexBuffer,
                albedoRBuffer, albedoGBuffer, albedoBBuffer, fuzzBuffer, refIdxBuffer
        };
    }

    // This method updates the image size and re-allocates the OpenCL buffer.
    public void updateImageSize(int newWidth, int newHeight) {
        this.width = newWidth;
        this.height = newHeight;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        openCLManager.recreatePixelBuffer(width, height);
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getSamplesPrPixel() {
        return samplesPrPixel;
    }

    public void setSamplesPrPixel(int samplesPrPixel) {
        this.samplesPrPixel = samplesPrPixel;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}
