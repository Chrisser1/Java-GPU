#ifndef SPHERE_CL
#define SPHERE_CL

#include "geometry.cl"
#include "interval.cl"
#include "material.cl"

// A sphere structure
typedef struct {
    point3 center;
    float radius;
    int materialIndex;
    float3 albedo;
    float fuzz;
    float ref_idx;
} sphere;

// Reconstruct a sphere from separate __constant arrays
sphere reconstruct_sphere(int i,
    __constant float* centerX, __constant float* centerY, __constant float* centerZ,
    __constant float* radii, __constant int* materialIndices,
    __constant float* albedoR, __constant float* albedoG, __constant float* albedoB,
    __constant float* fuzz, __constant float* refIdx) {
    sphere s;
    s.center = (point3)(centerX[i], centerY[i], centerZ[i]);
    s.radius = radii[i];
    s.materialIndex = materialIndices[i];
    s.albedo = (float3)(albedoR[i], albedoG[i], albedoB[i]);
    s.fuzz = fuzz[i];
    s.ref_idx = refIdx[i];
    return s;
}

// Ray-sphere intersection remains the same:
bool hit_sphere(sphere s, ray r, interval ray_t, __private hit_record *rec) {
    vec3 oc = s.center - r.orig;
    float a = dot(r.dir, r.dir);
    float h = dot(r.dir, oc);
    float c = dot(oc, oc) - s.radius * s.radius;
    float discriminant = h * h - a * c;
    if (discriminant < 0.0f)
        return false;
    float sqrtd = sqrt(discriminant);
    float root = (h - sqrtd) / a;
    if (!interval_surrounds(ray_t, root)) {
        root = (h + sqrtd) / a;
        if (!interval_surrounds(ray_t, root))
            return false;
    }
    rec->t = root;
    rec->p = ray_at(r, root);
    rec->mat = materialFromIndex(s.materialIndex, s.albedo, s.fuzz, s.ref_idx);
    vec3 outward_normal = (rec->p - s.center) / s.radius;
    set_face_normal(r, outward_normal, rec);
    return true;
}
#endif // SPHERE_CL
