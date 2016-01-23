package ru.ensemplix.command;

import java.util.Collection;

/**
 * Реализация данного интерфейса позволяет создавать автоматическое
 * дополнение команды.
 */
public interface CommandCompleter {

    /**
     * Автодополнение на основе переданной строки.
     *
     * @param value Часть строки, по которой мы будем искать.
     * @return Возвращает список возможных вариантов дополнения.
     */
    Collection<String> complete(String value);

}