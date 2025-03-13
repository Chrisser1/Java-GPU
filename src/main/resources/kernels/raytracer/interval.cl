#ifndef INTERVAL_CL
#define INTERVAL_CL

typedef struct {
    float min;
    float max;
} interval;

// Create an empty interval (default: empty)
static inline interval interval_empty() {
    interval i;
    // INFINITY is available via the math library in OpenCL C (with fp64 enabled)
    i.min = INFINITY;
    i.max = -INFINITY;
    return i;
}

// Create the universe interval (covers all real numbers)
static inline interval interval_universe() {
    interval i;
    i.min = -INFINITY;
    i.max = INFINITY;
    return i;
}

// Create an interval with specified min and max
static inline interval interval_create(double min, double max) {
    interval i;
    i.min = min;
    i.max = max;
    return i;
}

// Returns the size of the interval
static inline double interval_size(interval i) {
    return i.max - i.min;
}

// Check if the interval [min, max] contains x
static inline bool interval_contains(interval i, double x) {
    return (i.min <= x) && (x <= i.max);
}

// Check if the interval (min, max) strictly surrounds x
static inline bool interval_surrounds(interval i, double x) {
    return (i.min < x) && (x < i.max);
}

static inline float clamp_interval(interval i, double x) {
    if (x < i.min) return i.min;
    if (x > i.max) return i.max;
    return x;
}

#endif // INTERVAL_CL