#ifndef UTIL_CL
#define UTIL_CL

const float infinity = FLT_MAX;
const float pi = 3.1415926535897932385;

// Utility function: Convert degrees to radians.
inline float degrees_to_radians(float degrees) {
    return degrees * pi / 180.0f;
}

// MWC64X Random Number Generator
inline uint MWC64X(__private uint2 *state) {
    enum { A = 4294883355U };
    uint x = (*state).x, c = (*state).y;  // Unpack the state
    uint res = x ^ c;                     // Compute the next random value
    uint hi = mul_hi(x, A);                // Perform multiplication
    x = x * A + c;                         // Update x
    c = hi + (x < c);                      // Update carry
    *state = (uint2)(x, c);                 // Store new state
    return res;
}

// Convert a 32-bit integer to a floating-point value in [0,1)
inline float random_float(__private uint2 *state) {
    return (float)MWC64X(state) / 4294967296.0f;  // Use full 32-bit range
}

// Returns a random float in the range [min, max) using the updated state.
inline float random_float_range(float min, float max, __private uint2 *state) {
    return min + (max - min) * random_float(state);
}

// Generate a random unit vector
inline float3 random_unit_vector(__private uint2 *state) {
    float3 p;
    float lensq;
    do {
        p = (float3)(random_float_range(-1.0f, 1.0f, state),
                     random_float_range(-1.0f, 1.0f, state),
                     random_float_range(-1.0f, 1.0f, state));
        lensq = dot(p, p);
    } while (lensq <= 1e-6f || lensq > 1.0f);
    return p / sqrt(lensq);
}

// Generate a random unit vector on a hemisphere
inline float3 random_on_hemisphere(float3 normal, __private uint2 *state) {
    float3 on_unit_sphere = random_unit_vector(state);
    return (dot(on_unit_sphere, normal) > 0.0f) ? on_unit_sphere : -on_unit_sphere;
}

#endif // UTIL_CL
