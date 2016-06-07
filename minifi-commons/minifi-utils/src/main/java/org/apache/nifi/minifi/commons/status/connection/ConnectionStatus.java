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

package org.apache.nifi.minifi.commons.status.connection;

public class ConnectionStatus {
    private String name;
    private ConnectionHealth connectionHealth;
    private ConnectionStats connectionStats;

    public ConnectionStatus() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConnectionHealth getConnectionHealth() {
        return connectionHealth;
    }

    public void setConnectionHealth(ConnectionHealth connectionHealth) {
        this.connectionHealth = connectionHealth;
    }

    public ConnectionStats getConnectionStats() {
        return connectionStats;
    }

    public void setConnectionStats(ConnectionStats connectionStats) {
        this.connectionStats = connectionStats;
    }
}
