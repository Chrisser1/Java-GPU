#ifndef SCENE_CL
#define SCENE_CL
#include "sphere.cl"
#include "interval.cl"

// Checks for intersections with any sphere in the scene.
bool hit_scene(__constant float* centerX, __constant float* centerY, __constant float* centerZ,
               __constant float* radii, __constant int* materialIndices, __constant float* albedoR, __constant float* albedoG, __constant float* albedoB,
               __constant float* fuzz, __constant float* refIdx,
               int num_spheres, ray r, interval ray_t, __private hit_record *rec) {
    hit_record temp_rec;
    bool hit_anything = false;
    float closest_so_far = ray_t.max;
    for (int i = 0; i < num_spheres; i++) {
        sphere s = reconstruct_sphere(i, centerX, centerY, centerZ, radii, materialIndices, albedoR, albedoG, albedoB, fuzz, refIdx);
        if (hit_sphere(s, r, interval_create(ray_t.min, closest_so_far), &temp_rec)) {
            hit_anything = true;
            closest_so_far = temp_rec.t;
            *rec = temp_rec;
        }
    }
    return hit_anything;
}

#endif // SCENE_CL