package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TransactionProcessor {

    private final UserRecordRepository userRepo;
    private final TransactionRecordRepository txRepo;

    public TransactionProcessor(UserRecordRepository userRepo, TransactionRecordRepository txRepo) {
        this.userRepo = userRepo;
        this.txRepo = txRepo;
    }

    @Transactional
    public void process(String senderId, String recipientId, float amount) {
        // 1) sender valid?
        Optional<UserRecord> senderOpt = userRepo.findById(Long.parseLong(senderId));
        if (senderOpt.isEmpty()) return;

        // 2) recipient valid?
        Optional<UserRecord> recipientOpt = userRepo.findById(Long.parseLong(recipientId));
        if (recipientOpt.isEmpty()) return;

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        // 3) sender has enough balance?
        if (sender.getBalance() < amount) return;

        // apply balance updates
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        // record transaction
        txRepo.save(new TransactionRecord(sender, recipient, amount));

        // save users (often not strictly required with JPA dirty checking,
        // but it's safe + clear)
        userRepo.save(sender);
        userRepo.save(recipient);
    }
}
