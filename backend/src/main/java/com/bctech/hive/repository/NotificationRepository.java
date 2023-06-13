package com.bctech.hive.repository;

import com.bctech.hive.entity.Notification;
import com.bctech.hive.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findAllByUserOrderByCreatedAtDesc(User user);
}

