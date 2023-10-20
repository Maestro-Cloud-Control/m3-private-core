/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.amqp;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.integration.amqp.support.AmqpHeaderMapper;
import org.springframework.integration.amqp.support.DefaultAmqpHeaderMapper;
import org.springframework.messaging.MessageHeaders;

import java.util.Map;

public class RegionHeaderMapper implements AmqpHeaderMapper {

    private final AmqpHeaderMapper headerMapper = DefaultAmqpHeaderMapper.inboundMapper();

    private final String regionId;

    public RegionHeaderMapper(String regionId) {
        this.regionId = regionId;
    }

    @Override
    public void fromHeadersToRequest(MessageHeaders messageHeaders, MessageProperties messageProperties) {
        headerMapper.fromHeadersToRequest(messageHeaders, messageProperties);
    }

    @Override
    public void fromHeadersToReply(MessageHeaders messageHeaders, MessageProperties messageProperties) {
        headerMapper.fromHeadersToReply(messageHeaders, messageProperties);
    }

    @Override
    public Map<String, Object> toHeadersFromRequest(MessageProperties messageProperties) {
        Map<String, Object> headers = headerMapper.toHeadersFromRequest(messageProperties);
        headers.put(PrivateAgentAmqpConstants.REGION_ID, regionId);
        return headers;
    }

    @Override
    public Map<String, Object> toHeadersFromReply(MessageProperties messageProperties) {
        return headerMapper.toHeadersFromReply(messageProperties);
    }
}
