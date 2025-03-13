package dtu.gpu.raytracer;

import static org.jocl.CL.*;

import org.jocl.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class OpenCLManager {
    private cl_context context;
    private cl_command_queue commandQueue;
    private cl_kernel kernel;
    private cl_mem pixelMem;

    /**
     * Width and height of the window
     */
    private int width, height;

    /**
     * Create the OpenCLManager via a given width and height
     */
    public OpenCLManager(int width, int height)
    {
        this.width = width;
        this.height = height;
        initCL();
    }

    /**
     * Initialize the openCL context and set everything up
     */
    private void initCL()
    {
        CL.setExceptionsEnabled(true);

        // Platform and device selection
        cl_platform_id[]  platforms = new cl_platform_id[1];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[0];

        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        cl_device_id[] devices = new cl_device_id[1];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, devices.length, devices, null);
        cl_device_id device = devices[0];

        context = clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);

        cl_queue_properties properties = new cl_queue_properties();
        commandQueue = clCreateCommandQueueWithProperties(context, device, properties, null);

        // Load kernel source
        String source = readFile("src/main/resources/kernels/raytracer/raytracer.cl");
        cl_program program = clCreateProgramWithSource(context, 1, new String[]{source}, null, null);
        clBuildProgram(program, 0, null, "-I src/main/resources/kernels/raytracer -cl-fast-relaxed-math", null, null);

        kernel = clCreateKernel(program, "raytrace", null);

        // Allocate memory for pixels
        pixelMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY, (long) width * height * Sizeof.cl_int, null, null);
    }

    public cl_kernel getKernel() { return kernel; }
    public cl_mem getPixelMem() { return pixelMem; }
    public cl_command_queue getCommandQueue() { return commandQueue; }
    public cl_context getContext() { return context; }

    public void recreatePixelBuffer(int newWidth, int newHeight)
    {
        this.width = newWidth;
        this.height = newHeight;

        // Release the old buffer
        clReleaseMemObject(pixelMem);

        // Create a new pixel buffer with updated dimensions
        pixelMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY, (long) newWidth * newHeight * Sizeof.cl_int, null, null);
    }

    private String readFile(String fileName) {
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileName)));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while (true)
            {
                line = br.readLine();
                if (line == null)
                {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void release() {
        clReleaseMemObject(pixelMem);
        clReleaseKernel(kernel);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }
}

