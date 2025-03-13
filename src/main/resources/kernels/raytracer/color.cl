#ifndef COLOR_CL
#define COLOR_CL

#include "geometry.cl"
#include "scene.cl"
#include "interval.cl"
#include "util.cl"
#include "scatter.cl"

// Convert linear color component to gamma corrected value
inline float linear_to_gamma(float linear_component) {
    return (linear_component > 0) ? sqrt(linear_component) : 0.0f;
}

// Converts a float3 color to an integer RGB value with gamma correction.
int write_color(float3 pixel_color) {
    const interval intensity = interval_create(0.000f, 0.999f);
    pixel_color.x = linear_to_gamma(clamp_interval(intensity, pixel_color.x));
    pixel_color.y = linear_to_gamma(clamp_interval(intensity, pixel_color.y));
    pixel_color.z = linear_to_gamma(clamp_interval(intensity, pixel_color.z));

    int r = (int)(256.0f * pixel_color.x);
    int g = (int)(256.0f * pixel_color.y);
    int b = (int)(256.0f * pixel_color.z);
    return (r << 16) | (g << 8) | b;
}

float3 ray_color(ray r,
                 __constant float* centerX,
                 __constant float* centerY,
                 __constant float* centerZ,
                 __constant float* radii,
                 __constant int* materialIndices,
                 __constant float* albedoR,
                 __constant float* albedoG,
                 __constant float* albedoB,
                 __constant float* fuzz,
                 __constant float* refIdx,
                 int num_spheres, int max_depth, __private uint2 *rng_state) {

    float3 accumulated_color = (float3)(1.0f, 1.0f, 1.0f);
    float3 attenuation = (float3)(1.0f, 1.0f, 1.0f);

    for (int depth = 0; depth < max_depth; depth++) {
        hit_record rec;

        if (hit_scene(centerX, centerY, centerZ, radii, materialIndices, albedoR, albedoG, albedoB, fuzz, refIdx, num_spheres,
                      r, interval_create(0.001f, infinity), &rec)) {

            ray scattered;
            float3 new_attenuation;
            if (scatter(r, rec, rec.mat, &new_attenuation, &scattered, rng_state)) {
                attenuation *= new_attenuation;
                r = scattered;
            } else {
                return (float3)(0.0f, 0.0f, 0.0f);
            }
        } else {
            float3 unit_direction = normalize(r.dir);
            float t = 0.5f * (unit_direction.y + 1.0f);
            return attenuation * ((1.0f - t) * (float3)(1.0f, 1.0f, 1.0f) + t * (float3)(0.5f, 0.7f, 1.0f));
        }
    }
    return (float3)(0.0f, 0.0f, 0.0f);
}

#endif // COLOR_CL