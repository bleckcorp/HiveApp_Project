package com.bctech.hive.utils.event.listeners;

import com.bctech.hive.entity.Task;
import com.bctech.hive.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskCreatedEvent extends ApplicationEvent {
    private final User user;
    private final Task task;
    private final String applicationUrl;

    public TaskCreatedEvent(User user, Task task, String applicationUrl) {
        super(user);
        this.user = user;
        this.task = task;
        this.applicationUrl = applicationUrl;
    }
}
