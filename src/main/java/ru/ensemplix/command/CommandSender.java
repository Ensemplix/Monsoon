package ru.ensemplix.command;

/**
 * Пользователь который отправил команду.
 */
public interface CommandSender {

    /**
     * Отправить сообщение пользователю.
     */
    void sendMessage(String message);

}
