package com.tenpo.apirest.cache;

public interface PercentageCache {
    void savePercentageInCache(Double percentage);
    Double getPercentageCachedValue();
    void invalidateCache();
}
