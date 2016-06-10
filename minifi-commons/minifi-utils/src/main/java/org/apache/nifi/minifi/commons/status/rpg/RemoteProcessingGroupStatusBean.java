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

package org.apache.nifi.minifi.commons.status.rpg;

import org.apache.nifi.minifi.commons.status.common.BulletinStatus;

import java.util.List;

public class RemoteProcessingGroupStatusBean implements java.io.Serializable {
    private String name;
    private RemoteProcessingGroupHealth remoteProcessingGroupHealth;
    private List<BulletinStatus> bulletinList;
    private List<String> authorizationIssues;
    private List<InputPortStatus> inputPortStatusList;
    private RemoteProcessingGroupStats remoteProcessingGroupStats;

    public RemoteProcessingGroupStatusBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RemoteProcessingGroupHealth getRemoteProcessingGroupHealth() {
        return remoteProcessingGroupHealth;
    }

    public void setRemoteProcessingGroupHealth(RemoteProcessingGroupHealth remoteProcessingGroupHealth) {
        this.remoteProcessingGroupHealth = remoteProcessingGroupHealth;
    }

    public List<BulletinStatus> getBulletinList() {
        return bulletinList;
    }

    public void setBulletinList(List<BulletinStatus> bulletinList) {
        this.bulletinList = bulletinList;
    }

    public List<String> getAuthorizationIssues() {
        return authorizationIssues;
    }

    public void setAuthorizationIssues(List<String> authorizationIssues) {
        this.authorizationIssues = authorizationIssues;
    }

    public List<InputPortStatus> getInputPortStatusList() {
        return inputPortStatusList;
    }

    public void setInputPortStatusList(List<InputPortStatus> inputPortStatusList) {
        this.inputPortStatusList = inputPortStatusList;
    }

    public RemoteProcessingGroupStats getRemoteProcessingGroupStats() {
        return remoteProcessingGroupStats;
    }

    public void setRemoteProcessingGroupStats(RemoteProcessingGroupStats remoteProcessingGroupStats) {
        this.remoteProcessingGroupStats = remoteProcessingGroupStats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteProcessingGroupStatusBean that = (RemoteProcessingGroupStatusBean) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getRemoteProcessingGroupHealth() != null ? !getRemoteProcessingGroupHealth().equals(that.getRemoteProcessingGroupHealth()) : that.getRemoteProcessingGroupHealth() != null) return false;
        if (getBulletinList() != null ? !getBulletinList().equals(that.getBulletinList()) : that.getBulletinList() != null) return false;
        if (getAuthorizationIssues() != null ? !getAuthorizationIssues().equals(that.getAuthorizationIssues()) : that.getAuthorizationIssues() != null) return false;
        if (getInputPortStatusList() != null ? !getInputPortStatusList().equals(that.getInputPortStatusList()) : that.getInputPortStatusList() != null) return false;
        return getRemoteProcessingGroupStats() != null ? getRemoteProcessingGroupStats().equals(that.getRemoteProcessingGroupStats()) : that.getRemoteProcessingGroupStats() == null;

    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getRemoteProcessingGroupHealth() != null ? getRemoteProcessingGroupHealth().hashCode() : 0);
        result = 31 * result + (getBulletinList() != null ? getBulletinList().hashCode() : 0);
        result = 31 * result + (getAuthorizationIssues() != null ? getAuthorizationIssues().hashCode() : 0);
        result = 31 * result + (getInputPortStatusList() != null ? getInputPortStatusList().hashCode() : 0);
        result = 31 * result + (getRemoteProcessingGroupStats() != null ? getRemoteProcessingGroupStats().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", remoteProcessingGroupHealth=" + remoteProcessingGroupHealth +
                ", bulletinList=" + bulletinList +
                ", authorizationIssues=" + authorizationIssues +
                ", inputPortStatusList=" + inputPortStatusList +
                ", remoteProcessingGroupStats=" + remoteProcessingGroupStats +
                '}';
    }
}
