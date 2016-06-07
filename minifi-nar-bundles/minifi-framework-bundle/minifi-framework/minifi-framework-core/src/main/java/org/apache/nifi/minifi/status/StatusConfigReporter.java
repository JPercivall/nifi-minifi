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
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.nifi.minifi.status.StatusRequestParser.parseConnectionStatusRequest;
import static org.apache.nifi.minifi.status.StatusRequestParser.parseControllerServiceStatusRequest;
import static org.apache.nifi.minifi.status.StatusRequestParser.parseInstanceRequest;
import static org.apache.nifi.minifi.status.StatusRequestParser.parseProcessorStatusRequest;
import static org.apache.nifi.minifi.status.StatusRequestParser.parseRemoteProcessingGroupStatusRequest;
import static org.apache.nifi.minifi.status.StatusRequestParser.parseReportingTaskStatusRequest;
import static org.apache.nifi.minifi.status.StatusRequestParser.parseSystemDiagnosticsRequest;

public final class StatusConfigReporter {

    private StatusConfigReporter() {
    }

    public static String getStatus(FlowController flowController, String statusRequest, Logger logger) {
        // TODO: switch to standard format, durr
        StringBuilder statusToReturn = new StringBuilder();

        if (statusRequest == null) {
            logger.error("Received a status request which was null");
            return "Cannot complete status request because the statusRequest is null";
        }

        if (flowController == null) {
            logger.error("Received a status but the Flow Controller is null");
            return "Cannot complete status request because the Flow Controller is null";
        }

        String[] itemsToReport = statusRequest.split(";");

        ProcessGroupStatus rootGroupStatus = flowController.getControllerStatus();

        Map<String, ProcessorStatus> processorStatusMap = null;
        Map<String, ConnectionStatus> connectionStatusMap = null;
        Map<String, RemoteProcessGroupStatus> remoteProcessGroupStatusMap = null;

        boolean first = true;

        for (String item : itemsToReport) {
            String[] sections = item.split(":");
            if (!first) {
                statusToReturn.append(",");
            }

            try {
                switch (sections[0].toLowerCase().trim()) {
                    case "systemdiagnostics":
                        handleSystemDiagnosticsRequest(sections, flowController, statusToReturn, logger);
                        break;
                    case "instance":
                        handleInstanceRequest(sections, flowController, statusToReturn, rootGroupStatus);
                        break;
                    case "remoteprocessinggroup":
                        handleRemoteProcessingGroupRequest(sections, rootGroupStatus, flowController, statusToReturn, remoteProcessGroupStatusMap, logger);
                        break;
                    case "processor":
                        handleProcessorRequest(sections, rootGroupStatus, flowController, statusToReturn, processorStatusMap, logger);
                        break;
                    case "connection":
                        handleConnectionRequest(sections, rootGroupStatus, statusToReturn, connectionStatusMap, logger);
                        break;
                    case "provenancereporting":
                        handleReportingTaskRequest(sections, flowController, statusToReturn, logger);
                        break;
                    case "controllerservices":
                        handleControllerServices(sections, flowController, statusToReturn, logger);
                        break;
                }
                first = false;
            } catch (Exception e) {
                logger.error("Hit exception while requesting status for item '" + item + "'", e);
                statusToReturn.append("[Unable to get status for request '");
                statusToReturn.append(item);
                statusToReturn.append("' due to:");
                statusToReturn.append(e);
            }
        }
        return statusToReturn.toString();
    }

    private static void handleControllerServices(String[] sections, FlowController flowController, StringBuilder statusToReturn, Logger logger) {

        Collection<ControllerServiceNode> controllerServiceNodeSet = flowController.getAllControllerServices();

        if (controllerServiceNodeSet.isEmpty()) {
            statusToReturn.append("[");
            statusToReturn.append("No controller services to return status for");
            statusToReturn.append("]");
        } else {
            for (ControllerServiceNode controllerServiceNode : controllerServiceNodeSet) {
                statusToReturn.append("[");
                statusToReturn.append(parseControllerServiceStatusRequest(controllerServiceNode, sections[1], flowController, logger));
                statusToReturn.append("]");
            }
        }
    }

    private static void handleSystemDiagnosticsRequest(String[] sections, FlowController flowController, StringBuilder statusToReturn, Logger logger) {
        statusToReturn.append("[");
        statusToReturn.append(parseSystemDiagnosticsRequest(flowController.getSystemDiagnostics(), sections[1], logger));
        statusToReturn.append("]");
    }

    private static void handleInstanceRequest(String[] sections, FlowController flowController, StringBuilder statusToReturn, ProcessGroupStatus rootGroupStatus) {
        statusToReturn.append("[");
        statusToReturn.append(parseInstanceRequest(sections[1], flowController, rootGroupStatus));
        statusToReturn.append("]");
    }

    private static void handleProcessorRequest(String[] sections, ProcessGroupStatus rootGroupStatus, FlowController flowController, StringBuilder statusToReturn,
                                               Map<String, ProcessorStatus> processorStatusMap, Logger logger) {
        String rootGroupId = flowController.getRootGroupId();
        if (sections[1].equalsIgnoreCase("all")) {
            Collection<ProcessorStatus> processorStatusCollection = rootGroupStatus.getProcessorStatus();

            if (processorStatusCollection.isEmpty()) {
                statusToReturn.append("[");
                statusToReturn.append("No processors to return status for");
                statusToReturn.append("]");
            } else {
                for (ProcessorStatus processorStatus : processorStatusCollection) {
                    Collection<ValidationResult> validationResults = flowController.getGroup(rootGroupId).getProcessor(processorStatus.getId()).getValidationErrors();
                    statusToReturn.append("[");
                    statusToReturn.append(parseProcessorStatusRequest(processorStatus, sections[2], flowController, validationResults));
                    statusToReturn.append("]");
                }
            }
        } else {
            if (processorStatusMap == null) {
                processorStatusMap = transformStatusCollection(rootGroupStatus.getProcessorStatus());
            }

            if (processorStatusMap.containsKey(sections[1])) {
                ProcessorStatus processorStatus = processorStatusMap.get(sections[1]);
                Collection<ValidationResult> validationResults = flowController.getGroup(rootGroupId).getProcessor(processorStatus.getId()).getValidationErrors();
                statusToReturn.append("[");
                statusToReturn.append(parseProcessorStatusRequest(processorStatus, sections[2], flowController, validationResults));
                statusToReturn.append("]");
            } else {
                logger.warn("Status for processor with key " + sections[1] + " was requested but one does not exist");
                statusToReturn.append("[");
                statusToReturn.append("No processor with key ");
                statusToReturn.append(sections[1]);
                statusToReturn.append(" to report status on");
                statusToReturn.append("]");
            }
        }
    }

    private static void handleConnectionRequest(String[] sections, ProcessGroupStatus rootGroupStatus, StringBuilder statusToReturn,
                                                Map<String, ConnectionStatus> connectionStatusMap, Logger logger) {
        if (sections[1].equalsIgnoreCase("all")) {
            Collection<ConnectionStatus> connectionStatusCollection = rootGroupStatus.getConnectionStatus();

            if (connectionStatusCollection.isEmpty()) {
                statusToReturn.append("[");
                statusToReturn.append("No connections to return status for");
                statusToReturn.append("]");
            } else {
                for (ConnectionStatus connectionStatus : connectionStatusCollection) {
                    statusToReturn.append("[");
                    statusToReturn.append(parseConnectionStatusRequest(connectionStatus, sections[2], logger));
                    statusToReturn.append("]");
                }
            }
        } else {

            if (connectionStatusMap == null) {
                connectionStatusMap = transformStatusCollection(rootGroupStatus.getConnectionStatus());
            }


            if (connectionStatusMap.containsKey(sections[1])) {
                statusToReturn.append("[");
                statusToReturn.append(parseConnectionStatusRequest(connectionStatusMap.get(sections[1]), sections[2], logger));
                statusToReturn.append("]");
            } else {
                logger.warn("Status for connection with key " + sections[1] + " was requested but one does not exist");
                statusToReturn.append("[");
                statusToReturn.append("No connection with key ");
                statusToReturn.append(sections[1]);
                statusToReturn.append(" to report status on");
                statusToReturn.append("]");
            }
        }

    }

    private static void handleRemoteProcessingGroupRequest(String[] sections, ProcessGroupStatus rootGroupStatus, FlowController flowController, StringBuilder statusToReturn,
                                                           Map<String, RemoteProcessGroupStatus> remoteProcessGroupStatusMap, Logger logger) {
        if (sections[1].equalsIgnoreCase("all")) {
            Collection<RemoteProcessGroupStatus> remoteProcessGroupStatusCollection = rootGroupStatus.getRemoteProcessGroupStatus();

            if (remoteProcessGroupStatusCollection.isEmpty()) {
                statusToReturn.append("[");
                statusToReturn.append("No remote processing groups to return status for");
                statusToReturn.append("]");
            } else {
                for (RemoteProcessGroupStatus remoteProcessGroupStatus : remoteProcessGroupStatusCollection) {
                    statusToReturn.append("[");
                    statusToReturn.append(parseRemoteProcessingGroupStatusRequest(remoteProcessGroupStatus, sections[2], flowController));
                    statusToReturn.append("]");
                }
            }
        } else {
            if (remoteProcessGroupStatusMap == null) {
                remoteProcessGroupStatusMap = transformStatusCollection(rootGroupStatus.getRemoteProcessGroupStatus());
            }

            if (remoteProcessGroupStatusMap.containsKey(sections[1])) {
                RemoteProcessGroupStatus remoteProcessGroupStatus = remoteProcessGroupStatusMap.get(sections[1]);
                statusToReturn.append("[");
                statusToReturn.append(parseRemoteProcessingGroupStatusRequest(remoteProcessGroupStatus, sections[2], flowController));
                statusToReturn.append("]");
            } else {
                logger.warn("Status for Remote Processing Group with key " + sections[1] + " was requested but one does not exist");
                statusToReturn.append("[");
                statusToReturn.append("No Remote Processing Group with key ");
                statusToReturn.append(sections[1]);
                statusToReturn.append(" to report status on");
                statusToReturn.append("]");
            }
        }
    }

    private static void handleReportingTaskRequest(String[] sections, FlowController flowController, StringBuilder statusToReturn, Logger logger) {
        Set<ReportingTaskNode> reportingTaskNodes = flowController.getAllReportingTasks();

        if (reportingTaskNodes.isEmpty()) {
            statusToReturn.append("[");
            statusToReturn.append("No reporting tasks to return status for");
            statusToReturn.append("]");
        } else {
            for (ReportingTaskNode reportingTaskNode : reportingTaskNodes) {
                statusToReturn.append("[");
                statusToReturn.append(parseReportingTaskStatusRequest(reportingTaskNode.getIdentifier(), reportingTaskNode, sections[1], flowController, logger));
                statusToReturn.append("]");
            }
        }
    }

    private static <E> Map<String, E> transformStatusCollection(Collection<E> statusCollection) {
        Map<String, E> statusMap = new HashMap<>();
        for (E status : statusCollection) {
            if (status instanceof ProcessorStatus) {
                statusMap.put(((ProcessorStatus) status).getId(), status);
            } else if (status instanceof ConnectionStatus) {
                statusMap.put(((ConnectionStatus) status).getId(), status);
            } else {
                // TODO
            }
        }
        return statusMap;
    }


}
