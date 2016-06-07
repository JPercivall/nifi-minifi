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

package org.apache.nifi.minifi.commons.status.instance;

import org.apache.nifi.minifi.commons.status.common.Bulletin;

import java.util.List;

public class InstanceStatus {

    private InstanceHealth instanceHealth;
    private List<Bulletin> bulletinList;

    public InstanceStatus() {
    }

    public InstanceHealth getInstanceHealth() {
        return instanceHealth;
    }

    public void setInstanceHealth(InstanceHealth instanceHealth) {
        this.instanceHealth = instanceHealth;
    }

    public List<Bulletin> getBulletinList() {
        return bulletinList;
    }

    public void setBulletinList(List<Bulletin> bulletinList) {
        this.bulletinList = bulletinList;
    }
}
