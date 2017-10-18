package de.justsoftware.toolbox.mockito;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;

import com.google.common.collect.FluentIterable;

/**
 * utility class for usage with mocks
 *
 * @author Jan Burkhardt (jan.burkhardt@just.social) (initial creation)
 */
@ParametersAreNonnullByDefault
public final class Mocks<S> {

    private final S _service;
    private final Object[] _mocks;

    private Mocks(final S service, final Object[] mocks) {
        _service = service;
        _mocks = mocks;
    }

    public void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(_mocks);
    }

    @Nonnull
    public InOrder inOrder() {
        return Mockito.inOrder(_mocks);
    }

    @Nonnull
    public S getService() {
        return _service;
    }

    /**
     * return a used mock of the given class
     * the result is not fully type safe, but this is a common cast for classes with generic parameters.
     * expect {@link ClassCastException}s when you are using it wrong
     */
    @Nonnull
    public <T> T getMock(final Class<T> mockClass) {
        for (final Object m : _mocks) {
            if (mockClass.isInstance(m)) {
                @SuppressWarnings("unchecked")
                final T t = (T) m;
                return t;
            }
        }
        throw new IllegalArgumentException("Could not find mock of type " + mockClass);
    }

    /**
     * return a used mock of the given class ready to verify it
     * the result is not fully type safe, but this is a common cast for classes with generic parameters.
     * expect {@link ClassCastException}s when you are using it wrong
     */
    @Nonnull
    public <T> T verify(final Class<T> mockClass) {
        return Mockito.verify(getMock(mockClass));
    }

    /**
     * searches for a constructor which accepts mocks and creates an instance of the provided class
     *
     * @param clz
     *            the class of the service implementation
     * @param params
     *            are searched for candidates and passed to the constructor if the type of an argument matches
     * @return the created object
     */
    @Nonnull
    public static <S> S injectMocks(final Class<? extends S> clz, final Object... params) {
        return create(clz, params).getService();
    }

    /**
     * searches for a constructor which accepts mocks and creates an instance of the provided class
     *
     * @param clz
     *            the class of the service implementation
     * @param params
     *            are searched for candidates and passed to the constructor if the type of an argument matches
     * @return a wrapper around the created object with some helper functions
     */
    @Nonnull
    public static <S> Mocks<S> create(final Class<? extends S> clz, final Object... params) {
        //try to find an @Autowired annotation
        for (final Constructor<?> c : clz.getConstructors()) {
            for (final Annotation a : c.getAnnotations()) {
                if (a.annotationType().getCanonicalName().equals("org.springframework.beans.factory.annotation.Autowired")) {
                    return tryInstanciation(c, params);
                }
            }
        }

        //try other constructors
        for (final Constructor<?> c : clz.getConstructors()) {
            return tryInstanciation(c, params);
        }

        //try default constructor
        try {
            return new Mocks<>(clz.newInstance(), new Object[0]);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Nonnull
    private static <S> Mocks<S> tryInstanciation(final Constructor<?> c, final Object... params) {
        try {
            c.setAccessible(true);
            final Object[] initParams = FluentIterable
                    .from(c.getParameterTypes())
                    .transform(input -> findMock(params, input))
                    .toArray(Object.class);

            final Object[] mocks = FluentIterable
                    .from(initParams)
                    .filter(new MockUtil()::isMock)
                    .toArray(Object.class);

            @SuppressWarnings("unchecked")
            final S result = (S) c.newInstance(initParams);
            return new Mocks<>(result, mocks);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Nonnull
    private static Object findMock(final Object[] mocks, final Class<?> input) {
        for (final Object mock : mocks) {
            if (input.isInstance(mock)) {
                return mock;
            }
        }
        return Mockito.mock(input);
    }

}
