#ifndef GEOMETRY_CL
#define GEOMETRY_CL

#include "material.cl"

// Using float3 for both points and vectors
typedef float3 point3;
typedef float3 vec3;

// Define a simple ray structure
typedef struct {
    point3 orig;
    vec3 dir;
} ray;

// Compute a point along the ray at parameter t
inline point3 ray_at(ray r, float t) {
    return r.orig + t * r.dir;
}

// Hit record structure
typedef struct {
    point3 p;
    vec3 normal;
    float t;
    bool front_face;
    Material mat;
} hit_record;

// Set the face normal in a hit_record.
// The "outward_normal" is assumed to be normalized.
inline void set_face_normal(ray r, float3 outward_normal, __private hit_record* rec) {
    int front = (dot(r.dir, outward_normal) < 0.0f) ? 1 : 0;
    rec->front_face = front;
    rec->normal = front ? outward_normal : -outward_normal;
}

#endif // GEOMETRY_CL