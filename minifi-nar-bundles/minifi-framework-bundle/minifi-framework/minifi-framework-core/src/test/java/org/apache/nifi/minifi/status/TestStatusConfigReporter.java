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
import org.apache.nifi.connectable.Connectable;
import org.apache.nifi.controller.ConfiguredComponent;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.controller.ProcessorNode;
import org.apache.nifi.controller.ReportingTaskNode;
import org.apache.nifi.controller.ScheduledState;
import org.apache.nifi.controller.service.ControllerServiceNode;
import org.apache.nifi.controller.service.ControllerServiceState;
import org.apache.nifi.controller.status.ConnectionStatus;
import org.apache.nifi.controller.status.ProcessGroupStatus;
import org.apache.nifi.controller.status.ProcessorStatus;
import org.apache.nifi.controller.status.RunStatus;
import org.apache.nifi.diagnostics.GarbageCollection;
import org.apache.nifi.diagnostics.StorageUsage;
import org.apache.nifi.diagnostics.SystemDiagnostics;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.reporting.Bulletin;
import org.apache.nifi.reporting.BulletinQuery;
import org.apache.nifi.reporting.BulletinRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestStatusConfigReporter {
    private FlowController mockFlowController;
    private ProcessGroupStatus rootGroupStatus;
    private BulletinRepository bulletinRepo;

    @Before
    public void setup() {
        mockFlowController = mock(FlowController.class);
        rootGroupStatus = mock(ProcessGroupStatus.class);
        bulletinRepo = mock(BulletinRepository.class);

        when(mockFlowController.getRootGroupId()).thenReturn("root");
        when(mockFlowController.getGroupStatus("root")).thenReturn(rootGroupStatus);
        when(mockFlowController.getControllerStatus()).thenReturn(rootGroupStatus);
        when(mockFlowController.getBulletinRepository()).thenReturn(bulletinRepo);
    }

    @Test
    public void processorStatusHealth() throws Exception {
        when(bulletinRepo.findBulletins(anyObject())).thenReturn(Collections.emptyList());

        ProcessorStatus processorStatus = new ProcessorStatus();
        processorStatus.setType("org.apache.nifi.processors.attributes.UpdateAttribute");
        processorStatus.setId("UpdateAttributeProcessorId");
        processorStatus.setRunStatus(RunStatus.Stopped);

        Collection<ProcessorStatus> statusCollection = new ArrayList<>();
        statusCollection.add(processorStatus);

        when(rootGroupStatus.getProcessorStatus()).thenReturn(statusCollection);

        mockProcessorEmptyValidation(processorStatus.getId());

        String statusRequest = "processor:all:health";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Processor 'UpdateAttributeProcessorId':<Health {Run Status:Stopped, Has Bulletin(s):false}>]", status);
    }

    @Test
    public void processorStatusWithValidationErrors() throws Exception {
        when(bulletinRepo.findBulletins(anyObject())).thenReturn(Collections.emptyList());

        ProcessorStatus processorStatus = new ProcessorStatus();
        processorStatus.setType("org.apache.nifi.processors.attributes.UpdateAttribute");
        processorStatus.setId("UpdateAttributeProcessorId");
        processorStatus.setRunStatus(RunStatus.Stopped);

        Collection<ProcessorStatus> statusCollection = new ArrayList<>();
        statusCollection.add(processorStatus);

        when(rootGroupStatus.getProcessorStatus()).thenReturn(statusCollection);

        ProcessGroup processGroup = mock(ProcessGroup.class);
        when(mockFlowController.getGroup(mockFlowController.getRootGroupId())).thenReturn(processGroup);

        ProcessorNode processorNode = mock(ProcessorNode.class);
        when(processGroup.getProcessor(processorStatus.getId())).thenReturn(processorNode);
        ValidationResult validationResult = new ValidationResult.Builder()
                .input("input")
                .subject("subject")
                .explanation("is not valid")
                .build();

        ValidationResult validationResult2 = new ValidationResult.Builder()
                .input("input2")
                .subject("subject2")
                .explanation("is not valid too")
                .build();

        List<ValidationResult> validationResultList = new ArrayList<>();
        validationResultList.add(validationResult);
        validationResultList.add(validationResult2);

        when(processorNode.getValidationErrors()).thenReturn(validationResultList);

        String statusRequest = "processor:all:health";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Processor 'UpdateAttributeProcessorId':<Health {Run Status:Stopped, Has Bulletin(s):false, Validation Error(s):['subject' is invalid with input 'input' because " +
                "'is not valid', 'subject2' is invalid with input 'input2' because 'is not valid too']}>]", status);
    }

    @Test
    public void processorStatusAll() throws Exception {

        addBulletins("Bulletin message", "UpdateAttributeProcessorId");

        ProcessorStatus processorStatus = new ProcessorStatus();
        processorStatus.setType("org.apache.nifi.processors.attributes.UpdateAttribute");
        processorStatus.setId("UpdateAttributeProcessorId");
        processorStatus.setRunStatus(RunStatus.Stopped);
        processorStatus.setActiveThreadCount(1);
        processorStatus.setFlowFilesReceived(2);
        processorStatus.setBytesRead(3);
        processorStatus.setBytesWritten(4);
        processorStatus.setFlowFilesSent(5);
        processorStatus.setInvocations(6);
        processorStatus.setProcessingNanos(7);

        Collection<ProcessorStatus> statusCollection = new ArrayList<>();
        statusCollection.add(processorStatus);

        mockProcessorEmptyValidation(processorStatus.getId());
        when(rootGroupStatus.getProcessorStatus()).thenReturn(statusCollection);

        String statusRequest = "processor:all:health, stats, bulletins";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Processor 'UpdateAttributeProcessorId':<Health {Run Status:Stopped, Has Bulletin(s):true}><Stats {Active Threads:1, FlowFiles Received:2, Bytes Read:3, Bytes Written:4, " +
                "FlowFiles Out:5, Tasks:6, Processing Nanos:7}><Bulletins {'Mon May 23 12:00:45 EDT 2016':'Bulletin message'}>]", status);
    }

    @Test
    public void connectionStatusHealth() throws Exception {
        ConnectionStatus connectionStatus = new ConnectionStatus();
        connectionStatus.setQueuedBytes(100);
        connectionStatus.setId("connectionId");
        connectionStatus.setQueuedCount(10);

        Collection<ConnectionStatus> statusCollection = new ArrayList<>();
        statusCollection.add(connectionStatus);

        when(rootGroupStatus.getConnectionStatus()).thenReturn(statusCollection);

        String statusRequest = "connection:all:health";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Connection 'connectionId':<Health {Queued Count:10, Queued Size:100}>]", status);
    }


    @Test
    public void connectionStatusAll() throws Exception {
        ConnectionStatus connectionStatus = new ConnectionStatus();
        connectionStatus.setQueuedBytes(100);
        connectionStatus.setId("connectionId");
        connectionStatus.setQueuedCount(10);
        connectionStatus.setInputCount(1);
        connectionStatus.setInputBytes(2);
        connectionStatus.setOutputCount(3);
        connectionStatus.setOutputBytes(4);

        Collection<ConnectionStatus> statusCollection = new ArrayList<>();
        statusCollection.add(connectionStatus);

        when(rootGroupStatus.getConnectionStatus()).thenReturn(statusCollection);

        String statusRequest = "connection:all:health, stats";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Connection 'connectionId':<Health {Queued Count:10, Queued Size:100}><Stats {Input Count:1, Input Bytes:2, Output Count:3, Output Bytes:4}>]", status);
    }

    @Test
    public void connectionAndProcessorStatusHealth() throws Exception {
        when(bulletinRepo.findBulletins(anyObject())).thenReturn(Collections.emptyList());

        ConnectionStatus connectionStatus = new ConnectionStatus();
        connectionStatus.setQueuedBytes(100);
        connectionStatus.setId("connectionId");
        connectionStatus.setQueuedCount(10);

        Collection<ConnectionStatus> connectionStatusCollection = new ArrayList<>();
        connectionStatusCollection.add(connectionStatus);

        when(rootGroupStatus.getConnectionStatus()).thenReturn(connectionStatusCollection);

        ProcessorStatus processorStatus = new ProcessorStatus();
        processorStatus.setType("org.apache.nifi.processors.attributes.UpdateAttribute");
        processorStatus.setId("UpdateAttributeProcessorId");
        processorStatus.setRunStatus(RunStatus.Stopped);

        Collection<ProcessorStatus> processorStatusCollection = new ArrayList<>();
        processorStatusCollection.add(processorStatus);

        mockProcessorEmptyValidation(processorStatus.getId());
        when(rootGroupStatus.getProcessorStatus()).thenReturn(processorStatusCollection);

        String statusRequest = "connection:connectionId:health; processor:UpdateAttributeProcessorId:health";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Connection 'connectionId':<Health {Queued Count:10, Queued Size:100}>],[Processor 'UpdateAttributeProcessorId':<Health {Run Status:Stopped, Has Bulletin(s):false}>]", status);
    }

    @Test
    public void provenanceReportingTaskStatusHealth() throws Exception {
        when(bulletinRepo.findBulletins(anyObject())).thenReturn(Collections.emptyList());

        ReportingTaskNode reportingTaskNode = mock(ReportingTaskNode.class);
        addReportingTaskNodeVariables(reportingTaskNode);

        HashSet<ReportingTaskNode> reportingTaskNodes = new HashSet<>();
        reportingTaskNodes.add(reportingTaskNode);

        when(mockFlowController.getAllReportingTasks()).thenReturn(reportingTaskNodes);

        String statusRequest = "provenanceReporting:health";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Reporting Task 'ReportProvenance':<Health {Scheduled State:RUNNING, Has Bulletin(s):false, Active Threads:1}>]", status);
    }


    @Test
    public void provenanceReportingTaskStatusBulletins() throws Exception {
        ReportingTaskNode reportingTaskNode = mock(ReportingTaskNode.class);
        addReportingTaskNodeVariables(reportingTaskNode);

        HashSet<ReportingTaskNode> reportingTaskNodes = new HashSet<>();
        reportingTaskNodes.add(reportingTaskNode);

        addBulletins("Bulletin message", "ReportProvenance");

        when(mockFlowController.getAllReportingTasks()).thenReturn(reportingTaskNodes);

        String statusRequest = "provenanceReporting:bulletins";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Reporting Task 'ReportProvenance':<Bulletins {'Mon May 23 12:00:45 EDT 2016':'Bulletin message'}>]", status);
    }

    @Test
    public void provenanceReportingTaskStatusAll() throws Exception {
        addBulletins("Bulletin message", "ReportProvenance");

        ReportingTaskNode reportingTaskNode = mock(ReportingTaskNode.class);
        addReportingTaskNodeVariables(reportingTaskNode);

        HashSet<ReportingTaskNode> reportingTaskNodes = new HashSet<>();
        reportingTaskNodes.add(reportingTaskNode);

        when(mockFlowController.getAllReportingTasks()).thenReturn(reportingTaskNodes);

        String statusRequest = "provenanceReporting:bulletins";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Reporting Task 'ReportProvenance':<Bulletins {'Mon May 23 12:00:45 EDT 2016':'Bulletin message'}>]", status);
    }

    @Test
    public void systemDiagnosticHeap() throws Exception {
        SystemDiagnostics systemDiagnostics = new SystemDiagnostics();
        addHeapSystemDiagnostics(systemDiagnostics);

        when(mockFlowController.getSystemDiagnostics()).thenReturn(systemDiagnostics);

        String statusRequest = "systemDiagnostics:heap";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[System Diagnostics :<Heap {Total Heap:3, Max Heap:5, Free Heap:1, Used Heap:2, Heap Utilization:40, Total NonHeap:8, Max NonHeap:9, Free NonHeap:2, Used NonHeap:6, " +
                "NonHeap Utilization:67}>]", status);
    }

    @Test
    public void systemDiagnosticProcessorStats() throws Exception {
        SystemDiagnostics systemDiagnostics = new SystemDiagnostics();
        addProcessorInfoToSystemDiagnostics(systemDiagnostics);
        when(mockFlowController.getSystemDiagnostics()).thenReturn(systemDiagnostics);

        String statusRequest = "systemDiagnostics:processorStats";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[System Diagnostics :<Processor Stats {Processor Load Average:80.9, Available Processors:5}>]", status);
    }

    @Test
    public void systemDiagnosticFlowFileRepo() throws Exception {
        SystemDiagnostics systemDiagnostics = new SystemDiagnostics();
        addFlowFileRepoToSystemDiagnostics(systemDiagnostics);
        when(mockFlowController.getSystemDiagnostics()).thenReturn(systemDiagnostics);

        String statusRequest = "systemDiagnostics:flowfilerepositoryusage";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[System Diagnostics :<FlowFile Repository Usage {Free Space:30, Total Space:100, Disk Utilization:70, Used Space:70}>]", status);
    }

    @Test
    public void systemDiagnosticContentRepo() throws Exception {
        SystemDiagnostics systemDiagnostics = new SystemDiagnostics();
        addContentRepoToSystemDiagnostics(systemDiagnostics);
        when(mockFlowController.getSystemDiagnostics()).thenReturn(systemDiagnostics);

        String statusRequest = "systemDiagnostics:contentrepositoryusage";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[System Diagnostics :<Content Repository Usage {'Content repo1':[Free Space:30, Total Space:100, Disk Utilization:70, Used Space:70], 'Content repo2':[Free Space:50, " +
                "Total Space:60, Disk Utilization:17, Used Space:10]}>]", status);
    }

    @Test
    public void systemDiagnosticGarbageCollection() throws Exception {
        SystemDiagnostics systemDiagnostics = new SystemDiagnostics();
        addGarbageCollectionToSystemDiagnostics(systemDiagnostics);
        when(mockFlowController.getSystemDiagnostics()).thenReturn(systemDiagnostics);

        String statusRequest = "systemDiagnostics:garbagecollection";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[System Diagnostics :<Garbage Collection {'garbage 2':[Collection Count:2, Collection Time:20], 'garbage 1':[Collection Count:1, Collection Time:10]}>]", status);
    }


    @Test
    public void systemDiagnosticAll() throws Exception {
        SystemDiagnostics systemDiagnostics = new SystemDiagnostics();

        addGarbageCollectionToSystemDiagnostics(systemDiagnostics);
        addHeapSystemDiagnostics(systemDiagnostics);
        addContentRepoToSystemDiagnostics(systemDiagnostics);
        addFlowFileRepoToSystemDiagnostics(systemDiagnostics);
        addProcessorInfoToSystemDiagnostics(systemDiagnostics);

        when(mockFlowController.getSystemDiagnostics()).thenReturn(systemDiagnostics);

        String statusRequest = "systemDiagnostics:garbagecollection, heap, processorstats, contentrepositoryusage, flowfilerepositoryusage";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[System Diagnostics :<Garbage Collection {'garbage 2':[Collection Count:2, Collection Time:20], 'garbage 1':[Collection Count:1, Collection Time:10]}><Heap {Total Heap:3, " +
                "Max Heap:5, Free Heap:1, Used Heap:2, Heap Utilization:40, Total NonHeap:8, Max NonHeap:9, Free NonHeap:2, Used NonHeap:6, NonHeap Utilization:67}><Processor Stats {Processor Load " +
                "Average:80.9, Available Processors:5}><Content Repository Usage {'Content repo1':[Free Space:30, Total Space:100, Disk Utilization:70, Used Space:70], 'Content repo2':" +
                "[Free Space:50, Total Space:60, Disk Utilization:17, Used Space:10]}><FlowFile Repository Usage {Free Space:30, Total Space:100, Disk Utilization:70, Used Space:70}>]", status);
    }

    @Test
    public void instanceStatusHealth() throws Exception {
        when(bulletinRepo.findBulletins(anyObject())).thenReturn(Collections.emptyList());
        setRootGroupStatusVariables();

        String statusRequest = "instance:health";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Instance :<Health {Queued Count:2, Queued Content Size:1, Has Bulletin(s):false, Active Threads:3}>]", status);
    }

    @Test
    public void instanceStatusBulletins() throws Exception {
        addBulletinsToInstance();

        String statusRequest = "instance:bulletins";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Instance :<Bulletins {'Mon May 23 12:00:45 EDT 2016':'Bulletin message'}>]", status);
    }

    @Test
    public void instanceStatusAll() throws Exception {
        setRootGroupStatusVariables();
        addBulletinsToInstance();

        String statusRequest = "instance:bulletins, health";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Instance :<Bulletins {'Mon May 23 12:00:45 EDT 2016':'Bulletin message'}><Health {Queued Count:2, Queued Content Size:1, Has Bulletin(s):true, Active Threads:3}>]", status);
    }

    @Test
    public void controllerServiceStatusHealth() throws Exception {
        when(bulletinRepo.findBulletins(anyObject())).thenReturn(Collections.emptyList());

        ControllerServiceNode controllerServiceNode = mock(ControllerServiceNode.class);
        addControllerServiceHealth(controllerServiceNode);

        HashSet<ControllerServiceNode> controllerServiceNodes = new HashSet<>();
        controllerServiceNodes.add(controllerServiceNode);
        when(mockFlowController.getAllControllerServices()).thenReturn(controllerServiceNodes);

        String statusRequest = "controllerServices:health";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Controller Service 'mockControllerService':<Health {State:ENABLED, Has Bulletin(s):false}>]", status);
    }

    @Test
    public void controllerServiceStatusBulletins() throws Exception {
        when(bulletinRepo.findBulletins(anyObject())).thenReturn(Collections.emptyList());

        ControllerServiceNode controllerServiceNode = mock(ControllerServiceNode.class);
        when(controllerServiceNode.getName()).thenReturn("mockControllerService");
        when(controllerServiceNode.getIdentifier()).thenReturn("mockControllerService");

        HashSet<ControllerServiceNode> controllerServiceNodes = new HashSet<>();
        controllerServiceNodes.add(controllerServiceNode);

        when(mockFlowController.getAllControllerServices()).thenReturn(controllerServiceNodes);
        addBulletins("Bulletin message", controllerServiceNode.getIdentifier());

        String statusRequest = "controllerServices:bulletins";
        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Controller Service 'mockControllerService':<Bulletins {'Mon May 23 12:00:45 EDT 2016':'Bulletin message'}>]", status);
    }

    @Test
    public void controllerServiceStatusAll() throws Exception {
        when(bulletinRepo.findBulletins(anyObject())).thenReturn(Collections.emptyList());

        ControllerServiceNode controllerServiceNode = mock(ControllerServiceNode.class);
        addControllerServiceHealth(controllerServiceNode);

        addValidationErrors(controllerServiceNode);

        HashSet<ControllerServiceNode> controllerServiceNodes = new HashSet<>();
        controllerServiceNodes.add(controllerServiceNode);

        when(mockFlowController.getAllControllerServices()).thenReturn(controllerServiceNodes);

        addBulletins("Bulletin message", "mockControllerService");

        String statusRequest = "controllerServices:bulletins, health";

        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Controller Service 'mockControllerService':<Bulletins {'Mon May 23 12:00:45 EDT 2016':'Bulletin message'}><Health {State:ENABLED, Has Bulletin(s):true, Validation Error(s):" +
                "['subject' is invalid with input 'input' because 'is not valid', 'subject2' is invalid with input 'input2' because 'is not valid too']}>]", status);
    }

    @Test
    public void statusEverything() throws Exception {
        when(bulletinRepo.findBulletins(anyObject())).thenReturn(Collections.emptyList());

        // Controller Service
        ControllerServiceNode controllerServiceNode = mock(ControllerServiceNode.class);
        addControllerServiceHealth(controllerServiceNode);
        addValidationErrors(controllerServiceNode);
        HashSet<ControllerServiceNode> controllerServiceNodes = new HashSet<>();
        controllerServiceNodes.add(controllerServiceNode);
        when(mockFlowController.getAllControllerServices()).thenReturn(controllerServiceNodes);
        // Instance
        setRootGroupStatusVariables();
        addBulletinsToInstance();

        // System Diagnostics
        SystemDiagnostics systemDiagnostics = new SystemDiagnostics();
        addGarbageCollectionToSystemDiagnostics(systemDiagnostics);
        addHeapSystemDiagnostics(systemDiagnostics);
        addContentRepoToSystemDiagnostics(systemDiagnostics);
        addFlowFileRepoToSystemDiagnostics(systemDiagnostics);
        addProcessorInfoToSystemDiagnostics(systemDiagnostics);
        when(mockFlowController.getSystemDiagnostics()).thenReturn(systemDiagnostics);

        // Reporting Task
        ReportingTaskNode reportingTaskNode = mock(ReportingTaskNode.class);
        addReportingTaskNodeVariables(reportingTaskNode);
        HashSet<ReportingTaskNode> reportingTaskNodes = new HashSet<>();
        reportingTaskNodes.add(reportingTaskNode);
        when(mockFlowController.getAllReportingTasks()).thenReturn(reportingTaskNodes);

        // Connection
        ConnectionStatus connectionStatus = new ConnectionStatus();
        connectionStatus.setQueuedBytes(100);
        connectionStatus.setId("connectionId");
        connectionStatus.setQueuedCount(10);
        connectionStatus.setInputCount(1);
        connectionStatus.setInputBytes(2);
        connectionStatus.setOutputCount(3);
        connectionStatus.setOutputBytes(4);
        Collection<ConnectionStatus> connectionStatusCollection = new ArrayList<>();
        connectionStatusCollection.add(connectionStatus);
        when(rootGroupStatus.getConnectionStatus()).thenReturn(connectionStatusCollection);

        // Processor
        ProcessorStatus processorStatus = new ProcessorStatus();
        processorStatus.setType("org.apache.nifi.processors.attributes.UpdateAttribute");
        processorStatus.setId("UpdateAttributeProcessorId");
        processorStatus.setRunStatus(RunStatus.Stopped);
        processorStatus.setActiveThreadCount(1);
        processorStatus.setFlowFilesReceived(2);
        processorStatus.setBytesRead(3);
        processorStatus.setBytesWritten(4);
        processorStatus.setFlowFilesSent(5);
        processorStatus.setInvocations(6);
        processorStatus.setProcessingNanos(7);
        Collection<ProcessorStatus> processorStatusCollection = new ArrayList<>();
        processorStatusCollection.add(processorStatus);
        mockProcessorEmptyValidation(processorStatus.getId());
        when(rootGroupStatus.getProcessorStatus()).thenReturn(processorStatusCollection);

        String statusRequest = "controllerServices:bulletins,health; processor:all:health,stats,bulletins; instance:bulletins,health; systemDiagnostics:garbagecollection, heap, processorstats, " +
                "contentrepositoryusage, flowfilerepositoryusage; connection:all:health,stats";

        String status = StatusConfigReporter.getStatus(mockFlowController, statusRequest, LoggerFactory.getLogger(TestStatusConfigReporter.class));
        assertEquals("[Controller Service 'mockControllerService':<Bulletins {'Mon May 23 12:00:45 EDT 2016':'Bulletin message'}><Health {State:ENABLED, Has Bulletin(s):true, Validation Error(s):" +
                "['subject' is invalid with input 'input' because 'is not valid', 'subject2' is invalid with input 'input2' because 'is not valid too']}>]", status);
    }




    private void addBulletinsToInstance(){
        Bulletin bulletin = mock(Bulletin.class);
        when(bulletin.getTimestamp()).thenReturn(new Date(1464019245000L));
        when(bulletin.getMessage()).thenReturn("Bulletin message");

        List<Bulletin> bulletinList = new ArrayList<>();
        bulletinList.add(bulletin);

        when(bulletinRepo.findBulletinsForController()).thenReturn(bulletinList);
    }

    private void setRootGroupStatusVariables(){
        when(rootGroupStatus.getQueuedContentSize()).thenReturn(1L);
        when(rootGroupStatus.getQueuedCount()).thenReturn(2);
        when(rootGroupStatus.getActiveThreadCount()).thenReturn(3);
    }

    private void addGarbageCollectionToSystemDiagnostics(SystemDiagnostics systemDiagnostics){
        Map<String, GarbageCollection> garbageCollectionMap = new HashMap<>();

        GarbageCollection garbageCollection1 = new GarbageCollection();
        garbageCollection1.setCollectionCount(1);
        garbageCollection1.setCollectionTime(10);
        garbageCollection1.setName("garbage 1");
        garbageCollectionMap.put(garbageCollection1.getName(), garbageCollection1);

        GarbageCollection garbageCollection2 = new GarbageCollection();
        garbageCollection2.setCollectionCount(2);
        garbageCollection2.setCollectionTime(20);
        garbageCollection2.setName("garbage 2");
        garbageCollectionMap.put(garbageCollection2.getName(), garbageCollection2);

        systemDiagnostics.setGarbageCollection(garbageCollectionMap);
    }

    private void addContentRepoToSystemDiagnostics(SystemDiagnostics systemDiagnostics){
        Map<String, StorageUsage> stringStorageUsageMap = new HashMap<>();

        StorageUsage repoUsage1 = new StorageUsage();
        repoUsage1.setFreeSpace(30);
        repoUsage1.setTotalSpace(100);
        repoUsage1.setIdentifier("Content repo1");
        stringStorageUsageMap.put(repoUsage1.getIdentifier(), repoUsage1);

        StorageUsage repoUsage2 = new StorageUsage();
        repoUsage2.setFreeSpace(50);
        repoUsage2.setTotalSpace(60);
        repoUsage2.setIdentifier("Content repo2");
        stringStorageUsageMap.put(repoUsage2.getIdentifier(), repoUsage2);

        systemDiagnostics.setContentRepositoryStorageUsage(stringStorageUsageMap);
    }

    private void addFlowFileRepoToSystemDiagnostics(SystemDiagnostics systemDiagnostics){
        StorageUsage repoUsage = new StorageUsage();
        repoUsage.setFreeSpace(30);
        repoUsage.setTotalSpace(100);
        repoUsage.setIdentifier("FlowFile repo");
        systemDiagnostics.setFlowFileRepositoryStorageUsage(repoUsage);
    }

    private void addHeapSystemDiagnostics(SystemDiagnostics systemDiagnostics) {
        systemDiagnostics.setMaxHeap(5);
        systemDiagnostics.setTotalHeap(3);
        systemDiagnostics.setUsedHeap(2);
        systemDiagnostics.setMaxNonHeap(9);
        systemDiagnostics.setTotalNonHeap(8);
        systemDiagnostics.setUsedNonHeap(6);
    }

    private void addProcessorInfoToSystemDiagnostics(SystemDiagnostics systemDiagnostics) {
        systemDiagnostics.setProcessorLoadAverage(80.9);
        systemDiagnostics.setAvailableProcessors(5);
    }

    private void mockProcessorEmptyValidation(String id) {
        ProcessGroup processGroup = mock(ProcessGroup.class);
        when(mockFlowController.getGroup(mockFlowController.getRootGroupId())).thenReturn(processGroup);

        ProcessorNode processorNode = mock(ProcessorNode.class);
        when(processGroup.getProcessor(id)).thenReturn(processorNode);
        when(processorNode.getValidationErrors()).thenReturn(Collections.emptyList());
    }

    private void addControllerServiceHealth(ControllerServiceNode controllerServiceNode){
        when(controllerServiceNode.getName()).thenReturn("mockControllerService");
        when(controllerServiceNode.getIdentifier()).thenReturn("mockControllerService");
        when(controllerServiceNode.getState()).thenReturn(ControllerServiceState.ENABLED);
        when(controllerServiceNode.getValidationErrors()).thenReturn(Collections.emptyList());
    }

    private void addReportingTaskNodeVariables(ReportingTaskNode reportingTaskNode){
        when(reportingTaskNode.getValidationErrors()).thenReturn(Collections.emptyList());
        when(reportingTaskNode.getActiveThreadCount()).thenReturn(1);
        when(reportingTaskNode.getScheduledState()).thenReturn(ScheduledState.RUNNING);
        when(reportingTaskNode.getIdentifier()).thenReturn("ReportProvenance");
        when(reportingTaskNode.getName()).thenReturn("ReportProvenance");

    }

    private void addBulletins(String message, String sourceId){
        Bulletin bulletin = mock(Bulletin.class);
        when(bulletin.getTimestamp()).thenReturn(new Date(1464019245000L));
        when(bulletin.getMessage()).thenReturn(message);

        List<Bulletin> bulletinList = new ArrayList<>();
        bulletinList.add(bulletin);

        BulletinQueryAnswer bulletinQueryAnswer = new BulletinQueryAnswer(sourceId, bulletinList);
        when(bulletinRepo.findBulletins(anyObject())).then(bulletinQueryAnswer);
    }

    private void addValidationErrors(ConfiguredComponent connectable){
        ValidationResult validationResult = new ValidationResult.Builder()
                .input("input")
                .subject("subject")
                .explanation("is not valid")
                .build();

        ValidationResult validationResult2 = new ValidationResult.Builder()
                .input("input2")
                .subject("subject2")
                .explanation("is not valid too")
                .build();

        List<ValidationResult> validationResultList = new ArrayList<>();
        validationResultList.add(validationResult);
        validationResultList.add(validationResult2);
        when(connectable.getValidationErrors()).thenReturn(validationResultList);
    }

    private class BulletinQueryAnswer implements Answer{

        String idToMatch = "";
        List<Bulletin> bulletinList;

        private BulletinQueryAnswer(String idToMatch, List<Bulletin> bulletinList){
            this.idToMatch = idToMatch;
            this.bulletinList = bulletinList;
        }

        @Override
        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            BulletinQuery bulletinQuery = (BulletinQuery) invocationOnMock.getArguments()[0];
            if (idToMatch.equals(bulletinQuery.getSourceIdPattern().toString())){
                return bulletinList;
            }
            return Collections.emptyList();
        }
    }
}
