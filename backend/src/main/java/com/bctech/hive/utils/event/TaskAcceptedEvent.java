package com.bctech.hive.utils.event;

import com.bctech.hive.entity.Task;
import com.bctech.hive.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskAcceptedEvent extends ApplicationEvent {

    private final User user;
    private final Task task;

    public TaskAcceptedEvent(User user, Task task) {
        super(user);
        this.user = user;
        this.task =task;
    }

}
