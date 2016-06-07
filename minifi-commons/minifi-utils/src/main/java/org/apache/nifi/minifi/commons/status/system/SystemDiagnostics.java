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

import org.apache.nifi.minifi.commons.status.processor.ProcessorStatus;

import java.util.List;

public class SystemDiagnostics {
    private List<GarbageCollectionStatus> garbageCollectionStatusList;
    private HeapStatus heapStatus;
    private ProcessorStatus processorStatus;
    private List<ContentRepositoryUsage> contentRepositoryUsageList;
    private FlowfileRepositoryUsage flowfileRepositoryUsage;

    public SystemDiagnostics() {
    }

    public List<GarbageCollectionStatus> getGarbageCollectionStatusList() {
        return garbageCollectionStatusList;
    }

    public void setGarbageCollectionStatusList(List<GarbageCollectionStatus> garbageCollectionStatusList) {
        this.garbageCollectionStatusList = garbageCollectionStatusList;
    }

    public HeapStatus getHeapStatus() {
        return heapStatus;
    }

    public void setHeapStatus(HeapStatus heapStatus) {
        this.heapStatus = heapStatus;
    }

    public ProcessorStatus getProcessorStatus() {
        return processorStatus;
    }

    public void setProcessorStatus(ProcessorStatus processorStatus) {
        this.processorStatus = processorStatus;
    }

    public List<ContentRepositoryUsage> getContentRepositoryUsageList() {
        return contentRepositoryUsageList;
    }

    public void setContentRepositoryUsageList(List<ContentRepositoryUsage> contentRepositoryUsageList) {
        this.contentRepositoryUsageList = contentRepositoryUsageList;
    }

    public FlowfileRepositoryUsage getFlowfileRepositoryUsage() {
        return flowfileRepositoryUsage;
    }

    public void setFlowfileRepositoryUsage(FlowfileRepositoryUsage flowfileRepositoryUsage) {
        this.flowfileRepositoryUsage = flowfileRepositoryUsage;
    }
}
