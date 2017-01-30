package ru.ensemplix.command.region;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.ensemplix.command.CommandContext;
import ru.ensemplix.command.argument.Argument;
import ru.ensemplix.command.argument.ArgumentParser;

import static ru.ensemplix.command.argument.Argument.Result.SUCCESS;

public class RegionArgumentParser implements ArgumentParser<Region> {

    @Override
    public Argument<Region> parseArgument(@NotNull CommandContext context, int index, @Nullable String value) {
        if(value == null || value.isEmpty()) {
            return null;
        }

        return new Argument<>(SUCCESS, new Region(value));
    }

}
