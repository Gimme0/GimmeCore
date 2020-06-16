package me.gimme.gimmecore.command;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class PlaceholderCollection<T> {

    private Collection<T> collection;
    private Function<? super T, ? extends String> function;

    PlaceholderCollection(@NotNull Collection<T> collection, @NotNull Function<? super T, ? extends String> function) {
        this.collection = collection;
        this.function = function;
    }

    List<String> getList() {
        return collection.stream()
                .map(function)
                .collect(Collectors.toList());
    }

}
