#pragma OPENCL EXTENSION cl_khr_fp64 : enable
#include "geometry.cl"
#include "sphere.cl"
#include "scene.cl"
#include "color.cl"
#include "util.cl"

__kernel void raytrace(__global int* pixels,
                       int imageWidth, int imageHeight,
                       // Camera parameters passed from host:
                       point3 camera_center,
                       point3 lowerLeftCorner,
                       vec3 horizontal,
                       vec3 vertical,
                       // Sphere data passed as __constant arrays:
                       __constant float* sphereCenterX,
                       __constant float* sphereCenterY,
                       __constant float* sphereCenterZ,
                       __constant float* sphereRadii,
                       __constant int* sphereMaterialIndices,
                       __constant float* sphereAlbedoR,
                       __constant float* sphereAlbedoG,
                       __constant float* sphereAlbedoB,
                       __constant float* sphereFuzz,
                       __constant float* sphereRefIdx,
                       int num_spheres,
                       int debugging,
                       int samples_per_pixel,
                       int max_depth)
{
    int x = get_global_id(0);
    int y = get_global_id(1);
    if (x >= imageWidth || y >= imageHeight)
        return;

    // Optional: Debug printing can still be done by one work-item.
    bool debugByte = (debugging != 0);
    if (debugByte && (x == 0 && y == 0)) {
        printf("Received %d spheres\n", num_spheres);
        // Optionally, print sphere data by reconstructing a few spheres:
        for (int i = 0; i < num_spheres && i < 5; i++) {
            sphere s = reconstruct_sphere(i, sphereCenterX, sphereCenterY, sphereCenterZ, sphereRadii, sphereMaterialIndices,
                                          sphereAlbedoR, sphereAlbedoG, sphereAlbedoB, sphereFuzz, sphereRefIdx);
            printf("Sphere[%d]: Center=(%f, %f, %f), Radius=%f, MatIdx=%d\n",
                   i,
                   s.center.x, s.center.y, s.center.z,
                   s.radius,
                   s.materialIndex);
        }
    }

    // Initialize MWC64X state per pixel
    __private uint2 rng_state = (uint2)(x + y * imageWidth + 1, 0xCAFEBABE);

    float3 pixel_color = (float3)(0.0f, 0.0f, 0.0f);
    for (int s = 0; s < samples_per_pixel; s++) {
        float offset_u = random_float(&rng_state) - 0.5f;
        float offset_v = random_float(&rng_state) - 0.5f;
        float u = ((float)x + offset_u) / (imageWidth - 1);
        float v = (((float)imageHeight - 1.0f - y) + offset_v) / (imageHeight - 1);
        ray r;
        r.orig = camera_center;
        r.dir = lowerLeftCorner + u * horizontal + v * vertical - camera_center;
        pixel_color += ray_color(r, sphereCenterX, sphereCenterY, sphereCenterZ, sphereRadii, sphereMaterialIndices,
                                          sphereAlbedoR, sphereAlbedoG, sphereAlbedoB, sphereFuzz, sphereRefIdx,
                                          num_spheres, max_depth, &rng_state);
    }
    pixel_color = pixel_color / (float)samples_per_pixel;
    int pixelIndex = y * imageWidth + x;
    pixels[pixelIndex] = write_color(pixel_color);
}