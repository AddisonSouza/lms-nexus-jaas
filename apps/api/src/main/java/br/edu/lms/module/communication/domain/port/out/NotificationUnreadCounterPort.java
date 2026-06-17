package br.edu.lms.module.communication.domain.port.out;

public interface NotificationUnreadCounterPort {
    void increment(String userId);
    void decrement(String userId);
    void reset(String userId);
    long get(String userId);
}
