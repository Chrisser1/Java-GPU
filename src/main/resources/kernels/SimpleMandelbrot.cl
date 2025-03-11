#pragma OPENCL EXTENSION cl_khr_fp64 : enable

__kernel void computeMandelbrot(
    __global uint *output,
    int sizeX, int sizeY,
    double x0, double y0,
    double x1, double y1,
    int maxIterations,
    __global uint *colorMap,
    int colorMapSize)
{
    unsigned int ix = get_global_id(0);
    unsigned int iy = get_global_id(1);

    double r = x0 + (double)ix * (x1 - x0) / (double)sizeX;
    double i = y0 + (double)iy * (y1 - y0) / (double)sizeY;

    double x = 0.0;
    double y = 0.0;
    double magnitudeSquared = 0.0;

    int iteration = 0;
    while (iteration < maxIterations && magnitudeSquared < 4.0)
    {
        double xx = x * x;
        double yy = y * y;
        y = 2.0 * x * y + i;
        x = xx - yy + r;
        magnitudeSquared = xx + yy;
        iteration++;
    }
    if (iteration == maxIterations)
    {
        output[iy * sizeX + ix] = 0;
    }
    else
    {
        float alpha = (float)iteration / (float)maxIterations;
        int colorIndex = (int)(alpha * colorMapSize);
        output[iy * sizeX + ix] = colorMap[colorIndex];
    }
}
