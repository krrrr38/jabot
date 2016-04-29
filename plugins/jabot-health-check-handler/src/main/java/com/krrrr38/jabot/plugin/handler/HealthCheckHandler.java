package com.krrrr38.jabot.plugin.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krrrr38.jabot.plugin.brain.JabotBrainException;
import com.krrrr38.jabot.plugin.message.SendMessage;

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HealthCheckHandler extends Handler {
    private static final String HANDLER_NAME = "health-check";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final String OPTIONS_INTERVAL_CRON_SYNTAX = "intervalCronSyntax";
    private static final String DEFAULT_INTERVAL_CRON_SYNTAX = "* * * * *";
    private static final String OPTIONS_ALERT_PREFIX = "alertPrefix";
    private static final String DEFAULT_ALERT_PREFIX = "";
    private static final String OPTIONS_CONNECT_TIMEOUT = "connectTimeout";
    private static final int DEFAULT_CONNECT_TIMEOUT = 2000;
    private static final String OPTIONS_SOCKET_TIMEOUT = "socketTimeout";
    private static final int DEFAULT_SOCKET_TIMEOUT = 3000;
    private static final String OPTIONS_USER_AGENT = "userAgent";
    private static final String DEFAULT_USER_AGENT = "jabot-health-check";
    private static final String OPTIONS_CUSTOM_HEADER_NAME = "customHeaderName";
    private static final String DEFAULT_CUSTOM_HEADER_NAME = null;
    private static final String OPTIONS_CUSTOM_HEADER_VALUE = "customHeaderValue";
    private static final String DEFAULT_CUSTOM_HEADER_VALUE = null;

    private final Scheduler scheduler = new Scheduler();
    private List<HealthCheckJob> jobs;

    private String intervalCronSyntax;
    private String alertMessagePrefix;
    private int connectTimeout;
    private int socketTimeout;
    private String userAgent;
    private String customHeaderName;
    private String customHeaderValue;

    @Override
    List<Rule> buildRules(Map<String, String> options) {
        return Arrays.asList(HEALTH_CHECK_LIST, ADD_HEALTH_CHECK,
                             SUSPEND_ALL_HEALTH_CHECK, SUSPEND_HEALTH_CHECK,
                             RESUME_ALL_HEALTH_CHECK, RESUME_HEALTH_CHECK,
                             DELETE_ALL_HEALTH_CHECK, DELETE_HEALTH_CHECK);
    }

    private final Rule HEALTH_CHECK_LIST =
            new Rule(
                    Pattern.compile("\\A(list )?health-check\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Show registered health check list",
                    "list health-check",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        if (jobs.isEmpty()) {
                            send(new SendMessage("No registered health check"));
                        } else {
                            String header = "=== Health Check List ===\n";
                            StringJoiner sj = new StringJoiner("\n");
                            for (int i = 0; i < jobs.size(); i++) {
                                HealthCheckJob job = jobs.get(i);
                                sj.add(String.format("[%d](%s) %s %s %s", i, job.getActiveStatus(),
                                                     job.getHttpMethod(), job.getUrl(), job.getMemo()));
                            }
                            send(new SendMessage(header + sj.toString()));
                        }
                        return Optional.empty();
                    })
            );

    private static final String ADD_HEALTH_CHECK_PATTERN
            = "\\Aadd health-check (GET|HEAD) (https?://[-_.!~*'()a-zA-Z0-9;/?:@&=+\\$,%#]+) ?([\\s\\S]*)\\z";
    private final Rule ADD_HEALTH_CHECK =
            new Rule(
                    Pattern.compile(ADD_HEALTH_CHECK_PATTERN, Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Register new health check",
                    "add health-check (GET|HEAD) <url> <memo>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        HttpMethod httpMethod = strings[0].toUpperCase().equals("GET")
                                                ? HttpMethod.GET : HttpMethod.HEAD;
                        String url = strings[1];
                        String memo = strings[2];

                        HealthCheckJob job = new HealthCheckJob(null, httpMethod, ActiveStatus.ACTIVE, url,
                                                                memo);

                        // try health check before register job
                        if (!healthCheck(job)) {
                            send(new SendMessage("This health check job is not registered."));
                            return Optional.empty();
                        }

                        try {
                            job = registerJob(job);
                            send(new SendMessage(
                                    String.format("Register new health check [%d] %s %s", jobs.size() - 1,
                                                  job.getHttpMethod(), job.getUrl())));
                        } catch (JsonProcessingException e) {
                            send(new SendMessage("Failed to serialize health check: " + e.getMessage()));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule SUSPEND_ALL_HEALTH_CHECK =
            new Rule(
                    Pattern.compile("\\Asuspend all health-check\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Suspend all health check",
                    "suspend all health-check",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        try {
                            for (int i = 0; i < jobs.size(); i++) {
                                suspendJob(i);
                            }
                            send(new SendMessage("Suspend all health check"));
                        } catch (JsonProcessingException e) {
                            send(new SendMessage("Failed to serialize health check: " + e.getMessage()));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule SUSPEND_HEALTH_CHECK =
            new Rule(
                    Pattern.compile("\\Asuspend health-check (\\d{1,9})\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Suspend health check by index",
                    "suspend health-check <index:\\d{1,9}>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        int index = Integer.parseInt(strings[0]);
                        if (index < 0 || jobs.size() <= index) {
                            send(new SendMessage(
                                    "No such health check: " + index + ", max index is " + (jobs.size() - 1)));
                            return Optional.empty();
                        }
                        try {
                            HealthCheckJob job = suspendJob(index);
                            send(new SendMessage(
                                    String.format("Suspend health check: %s %s %s", job.getHttpMethod(),
                                                  job.getUrl(), job.getMemo())));
                        } catch (JsonProcessingException e) {
                            send(new SendMessage("Failed to serialize health check: " + e.getMessage()));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule RESUME_ALL_HEALTH_CHECK =
            new Rule(
                    Pattern.compile("\\Aresume all health-check\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Resume all health check",
                    "resume all health-check",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        try {
                            for (int i = 0; i < jobs.size(); i++) {
                                resumeJob(i);
                            }
                            send(new SendMessage("Resume all health check"));
                        } catch (JsonProcessingException e) {
                            send(new SendMessage("Failed to serialize health check: " + e.getMessage()));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule RESUME_HEALTH_CHECK =
            new Rule(
                    Pattern.compile("\\Aresume health-check (\\d{1,9})\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Resume health check by index",
                    "resume health-check <index:\\d{1,9}>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        int index = Integer.parseInt(strings[0]);
                        if (index < 0 || jobs.size() <= index) {
                            send(new SendMessage(
                                    "No such health check: " + index + ", max index is " + (jobs.size() - 1)));
                            return Optional.empty();
                        }
                        try {
                            HealthCheckJob job = resumeJob(index);
                            send(new SendMessage(
                                    String.format("Resume health check: %s %s %s", job.getHttpMethod(),
                                                  job.getUrl(), job.getMemo())));
                        } catch (JsonProcessingException e) {
                            send(new SendMessage("Failed to serialize health check: " + e.getMessage()));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule DELETE_ALL_HEALTH_CHECK =
            new Rule(
                    Pattern.compile("\\Adelete all health-check\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Delete all jobs",
                    "delete all jobs",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        for (int i = 0; i < jobs.size(); i++) {
                            unregisterJob(i);
                        }
                        clear();
                        send(new SendMessage("Deleted all health check"));
                        return Optional.empty();
                    })
            );

    private final Rule DELETE_HEALTH_CHECK =
            new Rule(
                    Pattern.compile("\\Adelete health-check (\\d+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Delete job by index",
                    "delete job <index:\\d+>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        int index = Integer.parseInt(strings[0]);
                        if (index < 0 || jobs.size() <= index) {
                            send(new SendMessage(
                                    "No such health check: " + index + ", max index is " + (jobs.size() - 1)));
                            return Optional.empty();
                        }
                        HealthCheckJob job = unregisterJob(index);
                        send(new SendMessage(
                                String.format("Deleted health check: %s %s %s", job.getHttpMethod(),
                                              job.getUrl(), job.getMemo())));
                        return Optional.empty();
                    })
            );

    private HealthCheckJob registerJob(HealthCheckJob job)
            throws JabotBrainException, JsonProcessingException {
        HealthCheckJob scheduledJob = scheduleJob(job);
        store(scheduledJob.getScheduledId(), OBJECT_MAPPER.writeValueAsString(scheduledJob));
        jobs.add(scheduledJob);
        return scheduledJob;
    }

    private HealthCheckJob scheduleJob(HealthCheckJob job) throws JabotBrainException {
        String newScheduledId = scheduler.schedule(intervalCronSyntax, () -> {
            healthCheck(job);
        });
        job.setScheduledId(newScheduledId);
        return job;
    }

    private HealthCheckJob unregisterJob(int index) throws JabotBrainException {
        HealthCheckJob job = jobs.remove(index);
        scheduler.deschedule(job.getScheduledId());
        delete(job.getScheduledId());
        return job;
    }

    private HealthCheckJob suspendJob(int index) throws JabotBrainException, JsonProcessingException {
        HealthCheckJob job = jobs.get(index);
        if (job.getActiveStatus() == ActiveStatus.SUSPENDED) {
            return job;
        }
        job.setActiveStatus(ActiveStatus.SUSPENDED);
        scheduler.deschedule(job.getScheduledId());
        store(job.getScheduledId(), OBJECT_MAPPER.writeValueAsString(job));
        return job;
    }

    private HealthCheckJob resumeJob(int index) throws JabotBrainException, JsonProcessingException {
        HealthCheckJob job = jobs.get(index);
        if (job.getActiveStatus() == ActiveStatus.ACTIVE) {
            return job;
        }
        job.setActiveStatus(ActiveStatus.ACTIVE);
        scheduleJob(job);
        return job;
    }

    protected boolean healthCheck(HealthCheckJob job) {
        try {
            log.debug("Send health check request: {} {}", job.getHttpMethod(), job.getUrl());
            Request request = job.getHttpMethod()
                                 .request(job.getUrl())
                                 .userAgent(userAgent)
                                 .connectTimeout(connectTimeout)
                                 .socketTimeout(socketTimeout);
            if (customHeaderName != null && customHeaderValue != null) {
                request.addHeader(customHeaderName, customHeaderValue);
            }
            HttpResponse res = request.execute().returnResponse();
            if (!hasSuccessResponse(res)) {
                String message = String.format(
                        "%shealth check failed: %s %s\n → [%s] %s",
                        alertMessagePrefix,
                        job.getHttpMethod(),
                        job.getUrl(),
                        res.getStatusLine(),
                        job.getMemo()
                );
                send(new SendMessage(message));
                return false;
            }
            return true;
        } catch (IOException e) {
            String message = String.format(
                    "%sFailed to send request %s %s: %s",
                    alertMessagePrefix,
                    job.getHttpMethod(),
                    job.getUrl(),
                    e.getMessage()
            );
            send(new SendMessage(message));
            return false;
        }
    }

    protected boolean hasSuccessResponse(HttpResponse httpResponse) {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        return 200 <= statusCode || statusCode < 400;
    }

    @Override
    public void afterSetup(Map<String, String> options) {
        intervalCronSyntax = optionString(options, OPTIONS_INTERVAL_CRON_SYNTAX, DEFAULT_INTERVAL_CRON_SYNTAX);
        alertMessagePrefix = optionString(options, OPTIONS_ALERT_PREFIX, DEFAULT_ALERT_PREFIX);
        connectTimeout = optionInteger(options, OPTIONS_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
        socketTimeout = optionInteger(options, OPTIONS_SOCKET_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
        userAgent = optionString(options, OPTIONS_USER_AGENT, DEFAULT_USER_AGENT);
        customHeaderName = optionString(options, OPTIONS_CUSTOM_HEADER_NAME, DEFAULT_CUSTOM_HEADER_NAME);
        customHeaderValue = optionString(options, OPTIONS_CUSTOM_HEADER_VALUE, DEFAULT_CUSTOM_HEADER_VALUE);

        new SchedulingPattern(intervalCronSyntax); // just check cron syntax is valid without exception

        jobs = Collections.synchronizedList(new ArrayList<>());
        try {
            List<HealthCheckJob> oldJobs = new ArrayList<>();
            for (String storedData : getAll().values()) {
                oldJobs.add(OBJECT_MAPPER.readValue(storedData, HealthCheckJob.class));
            }
            clear();
            for (HealthCheckJob job : oldJobs) {
                if (job.getActiveStatus() == ActiveStatus.ACTIVE) {
                    job = scheduleJob(job);
                }
                store(job.getScheduledId(), OBJECT_MAPPER.writeValueAsString(job));
                jobs.add(job);
            }
        } catch (JabotBrainException | IOException e) {
            log.error("Failed to load jobs: " + e.getMessage(), e);
        }
        scheduler.start();
    }

    @Override
    public void afterRegister(List<Handler> handlers) {
    }

    @Override
    public void beforeDestroy() {
        scheduler.stop();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class HealthCheckJob {
        private String scheduledId;
        private HttpMethod httpMethod;
        private ActiveStatus activeStatus;
        private String url;
        private String memo;
    }

    enum HttpMethod {
        HEAD {
            @Override
            Request request(String url) {
                return Request.Head(url);
            }
        },
        GET {
            @Override
            Request request(String url) {
                return Request.Get(url);
            }
        };

        abstract Request request(String url);
    }

    enum ActiveStatus {
        ACTIVE,
        SUSPENDED
    }
}
