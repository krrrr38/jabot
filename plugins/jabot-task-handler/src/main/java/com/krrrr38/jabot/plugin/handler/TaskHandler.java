package com.krrrr38.jabot.plugin.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krrrr38.jabot.plugin.brain.JabotBrainException;
import com.krrrr38.jabot.plugin.message.SendMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskHandler extends Handler {
    private static final String HANDLER_NAME = "task";
    private static final String ALL_USERS_TASKS_MANAGEMENT_ID = "ALL_USERS_TASKS_MANAGEMENT_ID";
    private static final String ALL_USERS_TASKS_MANAGEMENT_NAME = "ALL_USERS_TASKS";
    // for brain serialization
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Override
    List<Rule> buildRules(Map<String, String> options) {
        return Arrays.asList(LIST_TASKS, DELETE_TASK, DELETE_ALL_TASKS, ADD_TASK);
    }

    private final Rule LIST_TASKS =
            new Rule(
                    Pattern.compile("\\A(list )?tasks\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Show user's task list",
                    "list tasks",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        String senderId = sender.getId().orElse(ALL_USERS_TASKS_MANAGEMENT_ID);
                        String senderName = sender.getName().orElse(ALL_USERS_TASKS_MANAGEMENT_NAME);
                        try {
                            List<String> tasks = loadTasks(senderId);
                            if (tasks.isEmpty()) {
                                send(new SendMessage("No registered tasks for " + senderName));
                            } else {
                                String header = String.format("=== %s's Tasks ===\n", senderName);
                                StringJoiner sj = new StringJoiner("\n");
                                for (int i = 0; i < tasks.size(); i++) {
                                    sj.add(String.format("[%d] %s", i, tasks.get(i)));
                                }
                                send(new SendMessage(header + sj.toString()));
                            }
                        } catch (IOException e) {
                            send(new SendMessage("Failed to deserialize tasks: " + e.getMessage()));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule DELETE_TASK =
            new Rule(
                    Pattern.compile("\\Adelete task (\\d+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Delete user's task task by index",
                    "delete task <index:\\d+>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        int index = Integer.parseInt(strings[0]);
                        String senderId = sender.getId().orElse(ALL_USERS_TASKS_MANAGEMENT_ID);
                        try {
                            List<String> tasks = loadTasks(senderId);
                            if (index < 0 || tasks.size() <= index) {
                                send(new SendMessage(
                                        "No such task: " + index + ", max index is " + (tasks.size() - 1)));
                                return Optional.empty();
                            }

                            String deletedTask = tasks.remove(index);
                            if (deletedTask != null) {
                                storeTasks(senderId, tasks);
                                send(new SendMessage("Task Deleted: " + deletedTask));
                            } else {
                                send(new SendMessage("No such task"));
                            }
                        } catch (IOException e) {
                            send(new SendMessage("Failed to deserialize tasks: " + e.getMessage()));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule DELETE_ALL_TASKS =
            new Rule(
                    Pattern.compile("\\Adelete all tasks\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Delete user's all tasks",
                    "delete all tasks",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        String senderId = sender.getId().orElse(ALL_USERS_TASKS_MANAGEMENT_ID);
                        String senderName = sender.getName().orElse(ALL_USERS_TASKS_MANAGEMENT_NAME);
                        delete(senderId);
                        send(new SendMessage(String.format("Deleted %s's all tasks", senderName)));
                        return Optional.empty();
                    })
            );

    private Rule ADD_TASK =
            new Rule(
                    Pattern.compile("\\Aadd task (.+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Register user's new task",
                    "add task <message>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        String senderId = sender.getId().orElse(ALL_USERS_TASKS_MANAGEMENT_ID);
                        String senderName = sender.getName().orElse(ALL_USERS_TASKS_MANAGEMENT_NAME);
                        String task = strings[0];
                        try {
                            int taskIndex = addTask(senderId, task);
                            send(new SendMessage(
                                    String.format("Registered new task: [%d] %s for %s", taskIndex, task,
                                                  senderName)));
                        } catch (IOException e) {
                            send(new SendMessage("Failed to deserialize tasks: " + e.getMessage()));
                        }
                        return Optional.empty();
                    })
            );

    private List<String> loadTasks(String senderId) throws IOException, JabotBrainException {
        return OBJECT_MAPPER.readValue(get(senderId).orElse("[]"), new TypeReference<List<String>>() {});
    }

    /** @return return added task index */
    private int addTask(String senderId, String task) throws IOException, JabotBrainException {
        List<String> tasks = loadTasks(senderId);
        tasks.add(task);
        storeTasks(senderId, tasks);
        return tasks.size() - 1;
    }

    private void storeTasks(String senderId, List<String> tasks)
            throws JsonProcessingException, JabotBrainException {
        store(senderId, OBJECT_MAPPER.writeValueAsString(tasks));
    }

    @Override
    public void afterSetup(Map<String, String> options) {
    }

    @Override
    public void beforeDestroy() {
    }

    @Override
    public void afterRegister(List<Handler> handlers) {
    }
}
