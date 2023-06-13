package com.bctech.hive.repository;

import com.bctech.hive.entity.EscrowWallet;
import com.bctech.hive.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EscrowWalletRepository extends JpaRepository<EscrowWallet, String > {
    void deleteByTask(Task task);
}
