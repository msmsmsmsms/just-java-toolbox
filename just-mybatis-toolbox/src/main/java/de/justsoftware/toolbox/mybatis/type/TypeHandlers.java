package de.justsoftware.toolbox.mybatis.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.LongFunction;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * utilities for type handlers
 */
@ParametersAreNonnullByDefault
public class TypeHandlers {

    @Nonnull
    private static <T> T construct(final Constructor<T> constructor, final Object nestedObject) {
        try {
            return constructor.newInstance(nestedObject);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    private static <T> Constructor<T> getConstructor(final Class<T> clz, final Class<?> nestedClass) {
        try {
            return clz.getConstructor(nestedClass);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    public static <N, T> Function<N, T> construct(final Class<T> targetClass, final Class<N> nestedClass) {
        final Constructor<T> constructor = getConstructor(targetClass, nestedClass);
        return o -> construct(constructor, o);
    }

    @Nonnull
    public static <T> LongFunction<T> constructLong(final Class<T> targetClass) {
        final Constructor<T> constructor = getConstructor(targetClass, long.class);
        return o -> construct(constructor, o);
    }
}
