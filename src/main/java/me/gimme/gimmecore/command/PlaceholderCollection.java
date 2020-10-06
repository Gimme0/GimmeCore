package me.gimme.gimmecore.command;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class PlaceholderCollection<T> {

    private Supplier<Collection<? extends T>> supplier;
    private Function<? super T, ? extends String> function;

    PlaceholderCollection(@NotNull Supplier<Collection<? extends T>> supplier, @NotNull Function<? super T, ? extends String> function) {
        this.supplier = supplier;
        this.function = function;
    }

    List<String> getList() {
        return supplier.get().stream()
                .map(function)
                .collect(Collectors.toList());
    }

}
