#ifndef MATERIAL_CL
#define MATERIAL_CL

enum MaterialType {
    LAMBERTIAN = 0,  // Diffuse
    METAL = 1,       // Reflective
    DIELECTRIC = 2   // Glass-like refraction
};

typedef struct {
    enum MaterialType type;
    float3 albedo;  // Base color
    float fuzz;     // Used for metal roughness
    float ref_idx;  // Refractive index for dielectric materials
} Material;

// Function to get material from material index and color
Material materialFromIndex(int materialIndex, float3 color, float fuzz, float ref_idx) {
    Material mat;
    if (materialIndex == LAMBERTIAN) {
        mat.type = LAMBERTIAN;
        mat.albedo = color;
        mat.fuzz = 0.0f;
        mat.ref_idx = 0.0f;
    } else if (materialIndex == METAL) {
        mat.type = METAL;
        mat.albedo = color;
        mat.fuzz = fuzz;
        mat.ref_idx = 0.0f;
    } else if (materialIndex == DIELECTRIC) {
        mat.type = DIELECTRIC;
        mat.albedo = (float3)(1.0f, 1.0f, 1.0f);
        mat.fuzz = 0.0f;
        mat.ref_idx = ref_idx;
    } else {
        mat.type = LAMBERTIAN;
        mat.albedo = color;
        mat.fuzz = 0.0f;
        mat.ref_idx = 0.0f;
    }
    return mat;
}

#endif // MATERIAL_CL
