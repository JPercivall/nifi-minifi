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

package org.apache.nifi.minifi.commons.status.processor;

public class ProcessorStats {

    private String activeThreads;
    private String flowfilesReceived;
    private String bytesRead;
    private String bytesWritten;
    private String flowfilesOut;
    private String tasks;
    private String processingNanos;

    public ProcessorStats() {
    }

    public String getActiveThreads() {
        return activeThreads;
    }

    public void setActiveThreads(String activeThreads) {
        this.activeThreads = activeThreads;
    }

    public String getFlowfilesReceived() {
        return flowfilesReceived;
    }

    public void setFlowfilesReceived(String flowfilesReceived) {
        this.flowfilesReceived = flowfilesReceived;
    }

    public String getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(String bytesRead) {
        this.bytesRead = bytesRead;
    }

    public String getBytesWritten() {
        return bytesWritten;
    }

    public void setBytesWritten(String bytesWritten) {
        this.bytesWritten = bytesWritten;
    }

    public String getFlowfilesOut() {
        return flowfilesOut;
    }

    public void setFlowfilesOut(String flowfilesOut) {
        this.flowfilesOut = flowfilesOut;
    }

    public String getTasks() {
        return tasks;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    public String getProcessingNanos() {
        return processingNanos;
    }

    public void setProcessingNanos(String processingNanos) {
        this.processingNanos = processingNanos;
    }
}
