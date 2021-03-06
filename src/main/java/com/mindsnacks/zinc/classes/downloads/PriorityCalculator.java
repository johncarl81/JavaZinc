package com.mindsnacks.zinc.classes.downloads;

/**
 * User: NachoSoto
 * Date: 9/27/13
 */
public interface PriorityCalculator <V> {
    DownloadPriority getPriorityForObject(final V object);
}
