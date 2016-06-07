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

public class InstanceHealth {

    private String queuedCount;
    private String queuedContentSize;
    private boolean hasBulletins;

    public InstanceHealth() {
    }

    public String getQueuedCount() {
        return queuedCount;
    }

    public void setQueuedCount(String queuedCount) {
        this.queuedCount = queuedCount;
    }

    public String getQueuedContentSize() {
        return queuedContentSize;
    }

    public void setQueuedContentSize(String queuedContentSize) {
        this.queuedContentSize = queuedContentSize;
    }

    public boolean isHasBulletins() {
        return hasBulletins;
    }

    public void setHasBulletins(boolean hasBulletins) {
        this.hasBulletins = hasBulletins;
    }
}
