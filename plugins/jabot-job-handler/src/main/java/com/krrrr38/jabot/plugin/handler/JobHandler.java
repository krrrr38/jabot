package com.krrrr38.jabot.plugin.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.krrrr38.jabot.plugin.brain.JabotBrainException;
import com.krrrr38.jabot.plugin.message.SendMessage;

import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Scheduler;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobHandler extends Handler {
    private static final String HANDLER_NAME = "job";

    private final Scheduler scheduler = new Scheduler();
    private List<Job> jobs;

    @Override
    List<Rule> buildRules(Map<String, String> options) {
        return Arrays.asList(JOB_LIST, ADD_JOB, DELETE_ALL_JOBS, DELETE_JOB);
    }

    private final Rule JOB_LIST =
            new Rule(
                    Pattern.compile("\\Alist jobs\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Show registered jobs",
                    "list jobs",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        if (jobs.isEmpty()) {
                            send(new SendMessage("No registered jobs"));
                        } else {
                            String header = "=== Job List ===\n";
                            StringJoiner sj = new StringJoiner("\n");
                            for (int i = 0; i < jobs.size(); i++) {
                                Job job = jobs.get(i);
                                sj.add(String.format("[%d] %s %s", i, job.getCronSyntax(), job.getMessage()));
                            }
                            send(new SendMessage(header + sj.toString()));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule ADD_JOB =
            new Rule(
                    Pattern.compile("\\Aadd job \"(.+)\" (.+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Register new job",
                    "add job \"<cron syntax>\" <message>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        String cronSyntax = strings[0];
                        String message = strings[1];
                        try {
                            String scheduledId = scheduler.schedule(cronSyntax, () -> {
                                send(new SendMessage(message));
                            });
                            Job job = new Job(scheduledId, cronSyntax, message);
                            store(scheduledId, job.getStoredData());
                            jobs.add(job);
                            send(new SendMessage(String.format("Register new job [%d]", jobs.size())));
                        } catch (InvalidPatternException e) {
                            send(new SendMessage("Failed to register job: " + e.getMessage()));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule DELETE_ALL_JOBS =
            new Rule(
                    Pattern.compile("\\Adelete all jobs\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Delete all jobs",
                    "delete all jobs",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        if (clear()) {
                            jobs.forEach(job -> {
                                scheduler.deschedule(job.getScheduledId());
                            });
                            jobs = Collections.synchronizedList(new ArrayList<>());
                            send(new SendMessage("Deleted all jobs"));
                        } else {
                            send(new SendMessage("Failed to delete jobs"));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule DELETE_JOB =
            new Rule(
                    Pattern.compile("\\Adelete job (\\d+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Delete job by index",
                    "delete job <index:\\d+>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        int index = Integer.parseInt(strings[0]);
                        if (index < 0 || jobs.size() <= index) {
                            send(new SendMessage("No such job: " + index + ", max index is " + (jobs.size() - 1)));
                            return Optional.empty();
                        }
                        Job job = jobs.remove(index);
                        scheduler.deschedule(job.getScheduledId());
                        delete(job.getScheduledId());
                        send(new SendMessage(String.format("Deleted job: [%s] %s %s", index, job.getCronSyntax(), job.getMessage())));
                        return Optional.empty();
                    })
            );

    @Override
    public void afterSetup(Map<String, String> options) {
        jobs = Collections.synchronizedList(new ArrayList<>());
        try {
            List<Job> oldJobs = getAll().values().stream().map(storedData -> {
                return Job.parse("ignored-scheduled-id", storedData);
            }).collect(Collectors.toList());
            clear();
            for (Job job : oldJobs) {
                String newScheduledId = scheduler.schedule(job.getCronSyntax(), () -> {
                    send(new SendMessage(job.getMessage()));
                });
                store(newScheduledId, job.getStoredData());
                jobs.add(new Job(newScheduledId, job.getCronSyntax(), job.getMessage()));
            }
        } catch (JabotBrainException e) {
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

    @Value
    static class Job {
        private static final String JOB_STORE_SEPARATOR = "\t";
        private String scheduledId;
        private String cronSyntax;
        private String message;

        static Job parse(String scheduledId, String storedData) {
            String[] split = storedData.split(JOB_STORE_SEPARATOR, 2);
            if (split.length == 1) {
                return new Job(scheduledId, split[0], "");
            } else {
                return new Job(scheduledId, split[0], split[1]);
            }
        }

        public String getStoredData() {
            return cronSyntax + JOB_STORE_SEPARATOR + message;
        }
    }
}
