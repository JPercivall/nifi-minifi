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

package org.apache.nifi.minifi.status;

import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.controller.ReportingTaskNode;
import org.apache.nifi.controller.service.ControllerServiceNode;
import org.apache.nifi.controller.status.ConnectionStatus;
import org.apache.nifi.controller.status.ProcessGroupStatus;
import org.apache.nifi.controller.status.ProcessorStatus;
import org.apache.nifi.controller.status.RemoteProcessGroupStatus;
import org.apache.nifi.diagnostics.GarbageCollection;
import org.apache.nifi.diagnostics.StorageUsage;
import org.apache.nifi.diagnostics.SystemDiagnostics;
import org.apache.nifi.groups.RemoteProcessGroup;
import org.apache.nifi.remote.RemoteGroupPort;
import org.apache.nifi.reporting.Bulletin;
import org.apache.nifi.reporting.BulletinQuery;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StatusRequestParser {
    private StatusRequestParser() {
    }

    static StringBuilder parseProcessorStatusRequest(ProcessorStatus processorStatus, String statusTypes, FlowController flowController, Collection<ValidationResult> validationResults) {
        StringBuilder statusBuilder = new StringBuilder();

        statusBuilder.append("Processor '");
        statusBuilder.append(processorStatus.getId());
        statusBuilder.append("':");

        String[] statusSplits = statusTypes.split(",");
        List<Bulletin> bulletinList = flowController.getBulletinRepository().findBulletins(
                new BulletinQuery.Builder()
                        .sourceIdMatches(processorStatus.getId())
                        .build());

        for (String statusType : statusSplits) {
            switch (statusType.toLowerCase().trim()) {
                case "health":
                    statusBuilder.append("<Health {Run Status:");
                    statusBuilder.append(processorStatus.getRunStatus());
                    statusBuilder.append(", Has Bulletin(s):");
                    statusBuilder.append(!bulletinList.isEmpty());

                    appendValidationResults(statusBuilder, validationResults);
                    statusBuilder.append("}>");
                    break;
                case "bulletins":
                    printBulletins(statusBuilder, bulletinList);
                    break;
                case "stats":
                    statusBuilder.append("<Stats {Active Threads:");
                    statusBuilder.append(processorStatus.getActiveThreadCount());
                    statusBuilder.append(", FlowFiles Received:");
                    statusBuilder.append(processorStatus.getFlowFilesReceived());
                    statusBuilder.append(", Bytes Read:");
                    statusBuilder.append(processorStatus.getBytesRead());
                    statusBuilder.append(", Bytes Written:");
                    statusBuilder.append(processorStatus.getBytesWritten());
                    statusBuilder.append(", FlowFiles Out:");
                    statusBuilder.append(processorStatus.getFlowFilesSent());
                    statusBuilder.append(", Tasks:");
                    statusBuilder.append(processorStatus.getInvocations());
                    statusBuilder.append(", Processing Nanos:");
                    statusBuilder.append(processorStatus.getProcessingNanos());
                    statusBuilder.append("}>");
                    break;
            }
        }
        return statusBuilder;
    }

    static StringBuilder parseRemoteProcessingGroupStatusRequest(RemoteProcessGroupStatus remoteProcessGroupStatus, String statusTypes, FlowController flowController) {
        StringBuilder statusBuilder = new StringBuilder();

        statusBuilder.append("Remote Processing Group '");
        statusBuilder.append(remoteProcessGroupStatus.getId());
        statusBuilder.append("':");

        String rootGroupId = flowController.getRootGroupId();
        String[] statusSplits = statusTypes.split(",");

        List<Bulletin> bulletinList = flowController.getBulletinRepository().findBulletins(
                new BulletinQuery.Builder()
                        .sourceIdMatches(remoteProcessGroupStatus.getId())
                        .build());
        List<String> authorizationIssues = remoteProcessGroupStatus.getAuthorizationIssues();

        for (String statusType : statusSplits) {
            switch (statusType.toLowerCase().trim()) {
                case "health":
                    statusBuilder.append("<Health {Transmission Status:");
                    statusBuilder.append(remoteProcessGroupStatus.getTransmissionStatus());
                    statusBuilder.append(", Has Bulletin(s):");
                    statusBuilder.append(!bulletinList.isEmpty());
                    statusBuilder.append(", Has Authorization Issue(s):");
                    statusBuilder.append(!authorizationIssues.isEmpty());
                    statusBuilder.append(", Active Port Count:");
                    statusBuilder.append(remoteProcessGroupStatus.getActiveRemotePortCount());
                    statusBuilder.append(", Inactive Port Count:");
                    statusBuilder.append(remoteProcessGroupStatus.getInactiveRemotePortCount());
                    statusBuilder.append("}>");
                    break;
                case "bulletins":
                    printBulletins(statusBuilder, bulletinList);
                    break;
                case "authorizationissues":
                    statusBuilder.append("<Authorization Issue(s) ");
                    boolean first = true;
                    for (String authorizationIssue : authorizationIssues) {
                        if (!first) {
                            statusBuilder.append(", ");
                        }
                        statusBuilder.append("'");
                        statusBuilder.append(authorizationIssue);
                        statusBuilder.append("'");
                        first = false;
                    }
                    statusBuilder.append(">");
                    break;
                case "inputPorts":
                    RemoteProcessGroup remoteProcessGroup = flowController.getGroup(rootGroupId).getRemoteProcessGroup(remoteProcessGroupStatus.getId());
                    Set<RemoteGroupPort> inputPorts = remoteProcessGroup.getInputPorts();
                    statusBuilder.append("<Ports {");
                    first = true;
                    for (RemoteGroupPort inputPort : inputPorts) {
                        if (!first) {
                            statusBuilder.append(", ");
                        }
                        statusBuilder.append(inputPort.getName());
                        statusBuilder.append(":'");
                        if (!inputPort.getTargetExists()){
                            statusBuilder.append("'target does not exist'");
                        } else if (!inputPort.isTargetRunning()) {
                            statusBuilder.append("'target exists but is not running'");
                        } else {
                            statusBuilder.append("'target exists and is running'");
                        }
                        statusBuilder.append("'}");
                        first = false;
                    }
                    break;
                case "stats":
                    statusBuilder.append("<Stats {Active Threads:");
                    statusBuilder.append(remoteProcessGroupStatus.getActiveThreadCount());
                    statusBuilder.append(", Sent Count:");
                    statusBuilder.append(remoteProcessGroupStatus.getSentCount());
                    statusBuilder.append(", Sent Content Size:");
                    statusBuilder.append(remoteProcessGroupStatus.getSentContentSize());
                    statusBuilder.append("}>");
                    break;
            }
        }
        return statusBuilder;
    }

    static StringBuilder parseConnectionStatusRequest(ConnectionStatus connectionStatus, String statusTypes, Logger logger) {
        StringBuilder statusBuilder = new StringBuilder();

        statusBuilder.append("Connection '");
        statusBuilder.append(connectionStatus.getId());
        statusBuilder.append("':");

        String[] statusSplits = statusTypes.split(",");
        for (String statusType : statusSplits) {
            switch (statusType.toLowerCase().trim()) {
                case "health":
                    statusBuilder.append("<Health {Queued Count:");
                    statusBuilder.append(connectionStatus.getQueuedCount());
                    statusBuilder.append(", Queued Size:");
                    statusBuilder.append(connectionStatus.getQueuedBytes());
                    statusBuilder.append("}>");
                    break;
                case "bulletins":
                    statusBuilder.append("<Bulletins {'bulletins' is not a valid request for connections}>");
                    logger.warn("Status request is asking for bulletins for connection " + connectionStatus.getId() + " but bulletins are not possible for connections");
                    break;
                case "stats":
                    statusBuilder.append("<Stats {Input Count:");
                    statusBuilder.append(connectionStatus.getInputCount());
                    statusBuilder.append(", Input Bytes:");
                    statusBuilder.append(connectionStatus.getInputBytes());
                    statusBuilder.append(", Output Count:");
                    statusBuilder.append(connectionStatus.getOutputCount());
                    statusBuilder.append(", Output Bytes:");
                    statusBuilder.append(connectionStatus.getOutputBytes());
                    statusBuilder.append("}>");
                    break;
            }
        }
        return statusBuilder;
    }

    static StringBuilder parseReportingTaskStatusRequest(String id, ReportingTaskNode reportingTaskNode, String statusTypes, FlowController flowController, Logger logger) {
        StringBuilder statusBuilder = new StringBuilder();

        statusBuilder.append("Reporting Task '");
        statusBuilder.append(id);
        statusBuilder.append("':");

        String[] statusSplits = statusTypes.split(",");
        List<Bulletin> bulletinList = flowController.getBulletinRepository().findBulletins(
                new BulletinQuery.Builder()
                        .sourceIdMatches(id)
                        .build());
        for (String statusType : statusSplits) {
            switch (statusType.toLowerCase().trim()) {
                case "health":
                    statusBuilder.append("<Health {Scheduled State:");
                    statusBuilder.append(reportingTaskNode.getScheduledState());
                    statusBuilder.append(", Has Bulletin(s):");
                    statusBuilder.append(!bulletinList.isEmpty());
                    statusBuilder.append(", Active Threads:");
                    statusBuilder.append(reportingTaskNode.getActiveThreadCount());

                    Collection<ValidationResult> validationResults = reportingTaskNode.getValidationErrors();
                    appendValidationResults(statusBuilder, validationResults);

                    statusBuilder.append("}>");
                    break;
                case "bulletins":
                    printBulletins(statusBuilder, bulletinList);
                    break;
                case "stats":
                    statusBuilder.append("<Stats {'stats' is not a valid request for reporting tasks}>");
                    logger.warn("Status request is asking for stats for reporting task " + id + " but stats are not possible for reporting tasks");
                    break;
            }
        }
        return statusBuilder;
    }

    static StringBuilder parseControllerServiceStatusRequest(ControllerServiceNode controllerServiceNode, String statusTypes, FlowController flowController, Logger logger) {
        StringBuilder statusBuilder = new StringBuilder();
        String id = controllerServiceNode.getIdentifier();

        statusBuilder.append("Controller Service '");
        statusBuilder.append(id);
        statusBuilder.append("':");

        String[] statusSplits = statusTypes.split(",");
        List<Bulletin> bulletinList = flowController.getBulletinRepository().findBulletins(
                new BulletinQuery.Builder()
                        .sourceIdMatches(id)
                        .build());
        for (String statusType : statusSplits) {
            switch (statusType.toLowerCase().trim()) {
                case "health":
                    statusBuilder.append("<Health {State:");
                    statusBuilder.append(controllerServiceNode.getState());
                    statusBuilder.append(", Has Bulletin(s):");
                    statusBuilder.append(!bulletinList.isEmpty());

                    Collection<ValidationResult> validationResults = controllerServiceNode.getValidationErrors();
                    appendValidationResults(statusBuilder, validationResults);

                    statusBuilder.append("}>");
                    break;
                case "bulletins":
                    printBulletins(statusBuilder, bulletinList);
                    break;
                case "stats":
                    statusBuilder.append("<Stats {'stats' is not a valid request for controller services}>");
                    logger.warn("Status request is asking for stats for controller service " + id + " but stats are not possible for controller services");
                    break;
            }
        }
        return statusBuilder;
    }

    static StringBuilder parseSystemDiagnosticsRequest(SystemDiagnostics systemDiagnostics, String statusTypes, Logger logger) {
        StringBuilder statusBuilder = new StringBuilder();
        statusBuilder.append("System Diagnostics :");
        String[] statusSplits = statusTypes.split(",");

        if (systemDiagnostics == null) {
            statusBuilder.append("Unable to get system diagnostics");
            return statusBuilder;
        }

        for (String statusType : statusSplits) {
            switch (statusType.toLowerCase().trim()) {
                case "heap":
                    statusBuilder.append("<Heap {Total Heap:");
                    statusBuilder.append(systemDiagnostics.getTotalHeap());
                    statusBuilder.append(", Max Heap:");
                    statusBuilder.append(systemDiagnostics.getMaxHeap());
                    statusBuilder.append(", Free Heap:");
                    statusBuilder.append(systemDiagnostics.getFreeHeap());
                    statusBuilder.append(", Used Heap:");
                    statusBuilder.append(systemDiagnostics.getUsedHeap());
                    statusBuilder.append(", Heap Utilization:");
                    statusBuilder.append(systemDiagnostics.getHeapUtilization());
                    statusBuilder.append(", Total NonHeap:");
                    statusBuilder.append(systemDiagnostics.getTotalNonHeap());
                    statusBuilder.append(", Max NonHeap:");
                    statusBuilder.append(systemDiagnostics.getMaxNonHeap());
                    statusBuilder.append(", Free NonHeap:");
                    statusBuilder.append(systemDiagnostics.getFreeNonHeap());
                    statusBuilder.append(", Used NonHeap:");
                    statusBuilder.append(systemDiagnostics.getUsedNonHeap());
                    statusBuilder.append(", NonHeap Utilization:");
                    statusBuilder.append(systemDiagnostics.getNonHeapUtilization());
                    statusBuilder.append("}>");
                    break;
                case "processorstats":
                    statusBuilder.append("<Processor Stats {Processor Load Average:");
                    statusBuilder.append(systemDiagnostics.getProcessorLoadAverage());
                    statusBuilder.append(", Available Processors:");
                    statusBuilder.append(systemDiagnostics.getAvailableProcessors());
                    statusBuilder.append("}>");
                    break;
                case "contentrepositoryusage":
                    Map<String, StorageUsage> contentRepoStorage = systemDiagnostics.getContentRepositoryStorageUsage();
                    statusBuilder.append("<Content Repository Usage {");
                    boolean first = true;
                    for (Map.Entry<String, StorageUsage> stringStorageUsageEntry : contentRepoStorage.entrySet()) {
                        if (!first) {
                            statusBuilder.append(", ");
                        }
                        statusBuilder.append("'");
                        StorageUsage storageUsage = stringStorageUsageEntry.getValue();
                        statusBuilder.append(stringStorageUsageEntry.getKey());
                        statusBuilder.append("':[Free Space:");
                        statusBuilder.append(storageUsage.getFreeSpace());
                        statusBuilder.append(", Total Space:");
                        statusBuilder.append(storageUsage.getTotalSpace());
                        statusBuilder.append(", Disk Utilization:");
                        statusBuilder.append(storageUsage.getDiskUtilization());
                        statusBuilder.append(", Used Space:");
                        statusBuilder.append(storageUsage.getUsedSpace());
                        statusBuilder.append("]");
                        first = false;
                    }
                    statusBuilder.append("}>");
                    break;
                case "flowfilerepositoryusage":
                    StorageUsage flowFileRepoStorage = systemDiagnostics.getFlowFileRepositoryStorageUsage();
                    statusBuilder.append("<FlowFile Repository Usage {");
                    statusBuilder.append("Free Space:");
                    statusBuilder.append(flowFileRepoStorage.getFreeSpace());
                    statusBuilder.append(", Total Space:");
                    statusBuilder.append(flowFileRepoStorage.getTotalSpace());
                    statusBuilder.append(", Disk Utilization:");
                    statusBuilder.append(flowFileRepoStorage.getDiskUtilization());
                    statusBuilder.append(", Used Space:");
                    statusBuilder.append(flowFileRepoStorage.getUsedSpace());
                    statusBuilder.append("}>");
                    break;
                case "garbagecollection":
                    Map<String, GarbageCollection> garbageCollectionMap = systemDiagnostics.getGarbageCollection();
                    statusBuilder.append("<Garbage Collection {");
                    first = true;
                    for (Map.Entry<String, GarbageCollection> stringGarbageCollectionEntry : garbageCollectionMap.entrySet()) {
                        if (!first) {
                            statusBuilder.append(", ");
                        }
                        GarbageCollection garbageCollection = stringGarbageCollectionEntry.getValue();
                        statusBuilder.append("'");
                        statusBuilder.append(stringGarbageCollectionEntry.getKey());
                        statusBuilder.append("':[Collection Count:");
                        statusBuilder.append(garbageCollection.getCollectionCount());
                        statusBuilder.append(", Collection Time:");
                        statusBuilder.append(garbageCollection.getCollectionTime());
                        statusBuilder.append("]");
                        first = false;
                    }
                    statusBuilder.append("}>");
                    break;
            }
        }
        return statusBuilder;
    }

    static StringBuilder parseInstanceRequest(String statusTypes, FlowController flowController, ProcessGroupStatus rootGroupStatus) {
        StringBuilder statusBuilder = new StringBuilder();

        statusBuilder.append("Instance :");
        flowController.getAllControllerServices();

        List<Bulletin> bulletinList = flowController.getBulletinRepository().findBulletinsForController();
        String[] statusSplits = statusTypes.split(",");

        for (String statusType : statusSplits) {
            switch (statusType.toLowerCase().trim()) {
                case "health":
                    statusBuilder.append("<Health {Queued Count:");
                    statusBuilder.append(rootGroupStatus.getQueuedCount());
                    statusBuilder.append(", Queued Content Size:");
                    statusBuilder.append(rootGroupStatus.getQueuedContentSize());
                    statusBuilder.append(", Has Bulletin(s):");
                    statusBuilder.append(!bulletinList.isEmpty());
                    statusBuilder.append(", Active Threads:");
                    statusBuilder.append(rootGroupStatus.getActiveThreadCount());
                    statusBuilder.append("}>");
                    break;
                case "bulletins":
                    printBulletins(statusBuilder, flowController.getBulletinRepository().findBulletinsForController());
                    break;
                case "stats":
                    statusBuilder.append("<Stats {Bytes Read:");
                    statusBuilder.append(rootGroupStatus.getBytesRead());
                    statusBuilder.append(", Bytes Written:");
                    statusBuilder.append(rootGroupStatus.getBytesWritten());
                    statusBuilder.append(", Bytes Sent:");
                    statusBuilder.append(rootGroupStatus.getBytesSent());
                    statusBuilder.append(", FlowFiles Sent:");
                    statusBuilder.append(rootGroupStatus.getFlowFilesSent());
                    statusBuilder.append(", Bytes Transferred:");
                    statusBuilder.append(rootGroupStatus.getBytesTransferred());
                    statusBuilder.append(", FlowFiles Transferred:");
                    statusBuilder.append(rootGroupStatus.getFlowFilesTransferred());
                    statusBuilder.append(", Bytes Received:");
                    statusBuilder.append(rootGroupStatus.getBytesReceived());
                    statusBuilder.append(", FlowFiles Received:");
                    statusBuilder.append(rootGroupStatus.getFlowFilesReceived());
                    statusBuilder.append("}>");
                    break;
            }
        }
        return statusBuilder;
    }

    private static void appendValidationResults(StringBuilder statusBuilder, Collection<ValidationResult> validationResults) {
        boolean printedValidationResults = false;
        for (ValidationResult validationResult : validationResults) {
            if (!validationResult.isValid()) {
                if (!printedValidationResults) {
                    statusBuilder.append(", Validation Error(s):[");
                    printedValidationResults = true;
                } else {
                    statusBuilder.append(", ");
                }

                statusBuilder.append("'");
                statusBuilder.append(validationResult.getSubject());
                statusBuilder.append("' is invalid with input '");
                statusBuilder.append(validationResult.getInput());
                statusBuilder.append("' because '");
                statusBuilder.append(validationResult.getExplanation());
                statusBuilder.append("'");
            }
        }
        if (printedValidationResults) {
            statusBuilder.append("]");
        }
    }

    private static void printBulletins(StringBuilder statusBuilder, List<Bulletin> bulletinList){
        statusBuilder.append("<Bulletins ");
        boolean first = true;

        if (bulletinList.isEmpty()){
            statusBuilder.append("'No bulletins'");
        } else {
            for (Bulletin bulletin : bulletinList) {
                if (!first) {
                    statusBuilder.append(", ");
                }
                statusBuilder.append("{'");
                statusBuilder.append(bulletin.getTimestamp());
                statusBuilder.append("':'");
                statusBuilder.append(bulletin.getMessage());
                statusBuilder.append("'}");
                first = false;
            }
        }
        statusBuilder.append(">");
    }
}
