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

package org.apache.nifi.minifi.commons.status;

import org.apache.nifi.minifi.commons.status.connection.ConnectionStatus;
import org.apache.nifi.minifi.commons.status.controllerservice.ControllerServiceStatus;
import org.apache.nifi.minifi.commons.status.instance.InstanceHealth;
import org.apache.nifi.minifi.commons.status.processor.ProcessorStatus;
import org.apache.nifi.minifi.commons.status.system.SystemDiagnostics;

import java.util.List;

public class StatusReport {
    private List<ControllerServiceStatus> controllerServiceStatusList;
    private List<ProcessorStatus> processorStatusList;
    private List<ConnectionStatus> connectionStatusList;
    private InstanceHealth instanceHealth;
    private SystemDiagnostics systemDiagnostics;

    public StatusReport() {
    }

    public List<ControllerServiceStatus> getControllerServiceStatusList() {
        return controllerServiceStatusList;
    }

    public void setControllerServiceStatusList(List<ControllerServiceStatus> controllerServiceStatusList) {
        this.controllerServiceStatusList = controllerServiceStatusList;
    }

    public List<ProcessorStatus> getProcessorStatusList() {
        return processorStatusList;
    }

    public void setProcessorStatusList(List<ProcessorStatus> processorStatusList) {
        this.processorStatusList = processorStatusList;
    }

    public List<ConnectionStatus> getConnectionStatusList() {
        return connectionStatusList;
    }

    public void setConnectionStatusList(List<ConnectionStatus> connectionStatusList) {
        this.connectionStatusList = connectionStatusList;
    }

    public InstanceHealth getInstanceHealth() {
        return instanceHealth;
    }

    public void setInstanceHealth(InstanceHealth instanceHealth) {
        this.instanceHealth = instanceHealth;
    }

    public SystemDiagnostics getSystemDiagnostics() {
        return systemDiagnostics;
    }

    public void setSystemDiagnostics(SystemDiagnostics systemDiagnostics) {
        this.systemDiagnostics = systemDiagnostics;
    }
}
