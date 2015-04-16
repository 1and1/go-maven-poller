package com.oneandone.go.plugin.maven.message;

import com.oneandone.go.plugin.maven.util.JsonUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigurationMessageTest {

    @Test
    public void testDeserialization() throws Exception {
        final ConfigurationMessage configurationMessage = JsonUtil.fromJsonString(
                "{\n" +
                "  \"repository-configuration\": {\n" +
                "    \"REPO_URL\": {\n" +
                "      \"value\": \"https:\\/\\/repository.jboss.org\\/nexus\\/content\\/repositories\\/snapshots\\/\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"package-configuration\": {\n" +
                "    \"GROUP_ID\": {\n" +
                "      \"value\": \"org.wildfly.metrics\"\n" +
                "    },\n" +
                "    \"ARTIFACT_ID\": {\n" +
                "      \"value\": \"scheduler\"\n" +
                "    },\n" +
                "    \"PACKAGING\": {\n" +
                "      \"value\": \"jar\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"previous-revision\": {\n" +
                "    \"revision\": \"0.0.6-SNAPSHOT\",\n" +
                "    \"timestamp\": \"2015-04-09T11:08:52.209Z\",\n" +
                "    \"data\": {\n" +
                "      \"LOCATION\": \"https:\\/\\/repository.jboss.org\\/nexus\\/content\\/repositories\\/snapshots\\/org\\/wildfly\\/metrics\\/scheduler\\/0.0.6-SNAPSHOT\\/scheduler-0.0.6-20141022.115053-3.jar\",\n" +
                "      \"VERSION\": \"0.0.6-20141022.115053-3\"\n" +
                "    }\n" +
                "  }\n" +
                "}", ConfigurationMessage.class);

        assertNotNull(configurationMessage);
        assertNotNull(configurationMessage.getPreviousRevision());
        assertNotNull(configurationMessage.getPreviousRevision().getTimestamp());
    }

}