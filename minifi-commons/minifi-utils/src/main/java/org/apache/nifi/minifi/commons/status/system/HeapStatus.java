/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nifi.minifi.commons.status.system;

public class HeapStatus {

    private long totalHeap;
    private long maxHeap;
    private long freeHeap;
    private long usedHeap;
    private int heapUtilization;
    private long totalNonHeap;
    private long maxNonHeap;
    private long freeNonHeap;
    private long usedNonHeap;
    private int nonHeapUtilization;

    public HeapStatus() {
    }

    public long getTotalHeap() {
        return totalHeap;
    }

    public void setTotalHeap(long totalHeap) {
        this.totalHeap = totalHeap;
    }

    public long getMaxHeap() {
        return maxHeap;
    }

    public void setMaxHeap(long maxHeap) {
        this.maxHeap = maxHeap;
    }

    public long getFreeHeap() {
        return freeHeap;
    }

    public void setFreeHeap(long freeHeap) {
        this.freeHeap = freeHeap;
    }

    public long getUsedHeap() {
        return usedHeap;
    }

    public void setUsedHeap(long usedHeap) {
        this.usedHeap = usedHeap;
    }

    public int getHeapUtilization() {
        return heapUtilization;
    }

    public void setHeapUtilization(int heapUtilization) {
        this.heapUtilization = heapUtilization;
    }

    public long getTotalNonHeap() {
        return totalNonHeap;
    }

    public void setTotalNonHeap(long totalNonHeap) {
        this.totalNonHeap = totalNonHeap;
    }

    public long getMaxNonHeap() {
        return maxNonHeap;
    }

    public void setMaxNonHeap(long maxNonHeap) {
        this.maxNonHeap = maxNonHeap;
    }

    public long getFreeNonHeap() {
        return freeNonHeap;
    }

    public void setFreeNonHeap(long freeNonHeap) {
        this.freeNonHeap = freeNonHeap;
    }

    public long getUsedNonHeap() {
        return usedNonHeap;
    }

    public void setUsedNonHeap(long usedNonHeap) {
        this.usedNonHeap = usedNonHeap;
    }

    public int getNonHeapUtilization() {
        return nonHeapUtilization;
    }

    public void setNonHeapUtilization(int nonHeapUtilization) {
        this.nonHeapUtilization = nonHeapUtilization;
    }
}
