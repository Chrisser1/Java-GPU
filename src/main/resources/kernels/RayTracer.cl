#pragma OPENCL EXTENSION cl_khr_fp64 : enable

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
} hit_record;

// Inline function to set the face normal in a hit_record.
// The "outward_normal" is assumed to be normalized.
inline void set_face_normal(ray r, float3 outward_normal, __private hit_record* rec) {
    int front = (dot(r.dir, outward_normal) < 0.0f) ? 1 : 0;
    rec->front_face = front;
    rec->normal = front ? outward_normal : -outward_normal;
}

// A sphere structure
typedef struct {
    point3 center;
    float radius;
    int materialIndex;
} sphere;

// Reconstruct a sphere from separate arrays
sphere reconstruct_sphere(int i,
    __global float* centerX, __global float* centerY, __global float* centerZ,
    __global float* radii, __global int* materialIndices) {
    sphere s;
    s.center = (point3)(centerX[i], centerY[i], centerZ[i]);
    s.radius = radii[i];
    s.materialIndex = materialIndices[i];
    return s;
}

bool hit_sphere(sphere s, ray r, float ray_tmin, float ray_tmax, __private hit_record *rec) {
    vec3 oc = s.center - r.orig;
    float a = dot(r.dir, r.dir);
    float h = dot(r.dir, oc);
    float c = dot(oc, oc) - s.radius * s.radius;
    float discriminant = h * h - a * c;
    if (discriminant < 0.0f)
        return false;
    float sqrtd = sqrt(discriminant);
    float root = (h - sqrtd) / a;
    if (root < ray_tmin || root > ray_tmax) {
        root = (h + sqrtd) / a;
        if (root < ray_tmin || root > ray_tmax)
            return false;
    }
    rec->t = root;
    rec->p = ray_at(r, root);
    vec3 outward_normal = (rec->p - s.center) / s.radius;
    set_face_normal(r, outward_normal, rec);
    return true;
}

bool hit_scene(__private sphere* spheres, int num_spheres, ray r, float t_min, float t_max, __private hit_record *rec) {
    hit_record temp_rec;
    bool hit_anything = false;
    float closest_so_far = t_max;
    for (int i = 0; i < num_spheres; i++) {
        if (hit_sphere(spheres[i], r, t_min, closest_so_far, &temp_rec)) {
            hit_anything = true;
            closest_so_far = temp_rec.t;
            *rec = temp_rec;
        }
    }
    return hit_anything;
}

int write_color(float3 pixel_color) {
    int r = (int)(255.999f * clamp(pixel_color.x, 0.0f, 1.0f));
    int g = (int)(255.999f * clamp(pixel_color.y, 0.0f, 1.0f));
    int b = (int)(255.999f * clamp(pixel_color.z, 0.0f, 1.0f));
    return (r << 16) | (g << 8) | b;
}

float3 ray_color(ray r, __private sphere* spheres, int num_spheres) {
    hit_record rec;
    if (hit_scene(spheres, num_spheres, r, 0.001f, 10000.0f, &rec)) {
        return 0.5f * (rec.normal + (float3)(1.0f, 1.0f, 1.0f));
    }
    float3 unit_direction = normalize(r.dir);
    float t = 0.5f * (unit_direction.y + 1.0f);
    return (1.0f - t) * (float3)(1.0f, 1.0f, 1.0f) + t * (float3)(0.5f, 0.7f, 1.0f);
}

#define MAX_SPHERES 128

__kernel void raytrace(__global int* pixels,
                       int imageWidth, int imageHeight,
                       // Camera parameters passed from host:
                       point3 camera_center,
                       point3 lowerLeftCorner,
                       vec3 horizontal,
                       vec3 vertical,
                       // Scene parameters passed as separate arrays:
                       __global float* sphereCenterX,
                       __global float* sphereCenterY,
                       __global float* sphereCenterZ,
                       __global float* sphereRadii,
                       __global int* sphereMaterialIndices,
                       int num_spheres)
{
    // Reconstruct the sphere array into private memory.
    __private sphere spheres_local[MAX_SPHERES];
    if (num_spheres > MAX_SPHERES)
        num_spheres = MAX_SPHERES;
    for (int i = 0; i < num_spheres; i++) {
        spheres_local[i] = reconstruct_sphere(i, sphereCenterX, sphereCenterY, sphereCenterZ, sphereRadii, sphereMaterialIndices);
    }

    // Debug printing (only one work-item prints)
    if (get_global_id(0) == 0 && get_global_id(1) == 0) {
        printf("Received %d spheres\n", num_spheres);
        for (int i = 0; i < num_spheres; i++) {
            printf("Sphere[%d]: Center=(%f, %f, %f), Radius=%f, MatIdx=%d\n",
                i,
                spheres_local[i].center.x,
                spheres_local[i].center.y,
                spheres_local[i].center.z,
                spheres_local[i].radius,
                spheres_local[i].materialIndex);
        }
    }

    int x = get_global_id(0);
    int y = get_global_id(1);
    if (x >= imageWidth || y >= imageHeight)
        return;

    int inverted_y = imageHeight - 1 - y;
    float u = (float)x / (imageWidth - 1);
    float v = (float)inverted_y / (imageHeight - 1);

    ray r;
    r.orig = camera_center;
    r.dir = lowerLeftCorner + u * horizontal + v * vertical - camera_center;

    float3 pixel_color = ray_color(r, spheres_local, num_spheres);
    int pixelIndex = y * imageWidth + x;
    pixels[pixelIndex] = write_color(pixel_color);
}
