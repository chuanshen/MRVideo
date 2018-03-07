package com.seu.magicfilter.utils;

import com.seu.magicfilter.base.gpuimage.GPUImageFilter;

public class MagicFilterFactory {

    public static GPUImageFilter initFilters(MagicFilterType type) {
        return new GPUImageFilter();
    }
}
