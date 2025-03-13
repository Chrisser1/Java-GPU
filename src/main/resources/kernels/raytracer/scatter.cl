#ifndef SCATTER_CL
#define SCATTER_CL

#include "util.cl"
#include "geometry.cl"
#include "material.cl"

// Computes reflection for metal materials
inline float3 reflect(float3 v, float3 n) {
    return v - 2.0f * dot(v, n) * n;
}

// Computes refraction for dielectric materials
bool refract(float3 v, float3 n, float eta, __private float3 *refracted) {
    float cos_theta = min(dot(-v, n), 1.0f);
    float3 r_out_perp = eta * (v + cos_theta * n);
    float3 r_out_parallel = -sqrt(fabs(1.0f - dot(r_out_perp, r_out_perp))) * n;
    *refracted = r_out_perp + r_out_parallel;
    return true;
}

// Computes reflectance using Schlick's approximation
static float reflectance(float cosine, float refraction_index) {
    float r0 = (1.0f - refraction_index) / (1.0f + refraction_index);
    r0 = r0 * r0;
    return r0 + (1.0f - r0) * pow((1.0f - cosine), 5.0f);
}

// Scatter function for materials
bool scatter(ray r_in, hit_record rec, Material mat, __private float3 *attenuation, __private ray *scattered, __private uint2 *state) {
    if (mat.type == LAMBERTIAN) {
        // Diffuse scattering
        float3 scatter_direction = rec.normal + random_unit_vector(state);
        if (length(scatter_direction) < 1e-6) scatter_direction = rec.normal; // Avoid zero direction
        *scattered = (ray){rec.p, scatter_direction};
        *attenuation = mat.albedo;
        return true;
    }
    else if (mat.type == METAL) {
        // Reflective scattering
        float3 reflected = reflect(normalize(r_in.dir), rec.normal);
        *scattered = (ray){rec.p, reflected + mat.fuzz * random_unit_vector(state)};
        *attenuation = mat.albedo;
        return dot(scattered->dir, rec.normal) > 0;
    }
    else if (mat.type == DIELECTRIC) {
        // Glass-like refraction
        *attenuation = (float3)(1.0f, 1.0f, 1.0f);
        float eta = rec.front_face ? (1.0f / mat.ref_idx) : mat.ref_idx;
        float3 unit_direction = normalize(r_in.dir);
        float cos_theta = min(dot(-unit_direction, rec.normal), 1.0f);
        float sin_theta = sqrt(1.0f - cos_theta * cos_theta);

        bool cannot_refract = eta * sin_theta > 1.0f;
        float3 direction;

        if (cannot_refract || reflectance(cos_theta, eta) > random_float(state)) {
            direction = reflect(unit_direction, rec.normal);
        } else {
            refract(unit_direction, rec.normal, eta, &direction);
        }

        *scattered = (ray){rec.p, direction};
        return true;
    }
    return false;
}

#endif // SCATTER_CL
