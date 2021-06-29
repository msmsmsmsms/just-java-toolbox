package de.justsoftware.toolbox.hamcrest;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * This is a class with useful Hamcest matchers.
 *
 * @author wolfgang (initial creation)
 */
//CSOFF:MultipleStringLiterals
@ParametersAreNonnullByDefault
public class HamcrestMatchers {

    @Nonnull
    public static <T> Matcher<Iterable<T>> sameIterable(final Function<T, Matcher<? super T>> matcherFunction,
            final Iterable<? extends T> expected) {
        return new TypeSafeDiagnosingMatcher<>() {
            private final ImmutableList<Matcher<? super T>> _matchers =
                    FluentIterable.from(expected).transform(matcherFunction).toList();

            @Override
            public void describeTo(final Description description) {
                description.appendText("iterable same as ").appendValueList("[", ", ", "]", expected);
            }

            @Override
            protected boolean matchesSafely(final Iterable<T> itemIter, final Description mismatchDescription) {
                final ImmutableList<T> items = ImmutableList.copyOf(itemIter);

                if (_matchers.size() != items.size()) {
                    mismatchDescription.appendText("has length of " + items.size() + " (expected: " + _matchers.size() + ")");
                    return false;
                }

                for (int i = 0; i < _matchers.size(); i++) {
                    final Matcher<? super T> matcher = _matchers.get(i);
                    if (!matcher.matches(items.get(i))) {
                        mismatchDescription.appendText("item " + i + ": ");
                        matcher.describeMismatch(items.get(i), mismatchDescription);
                        return false;
                    }
                }
                return true;
            }
        };
    }

    @Nonnull
    public static <T> Matcher<Iterable<T>> sameIterable(final Iterable<? extends T> expected) {
        return sameIterable(Matchers::equalTo, expected);
    }

    @Nonnull
    public static <K, V> Matcher<Map<K, V>> sameMap(final Function<V, Matcher<? super V>> valueMatcherFunction,
            final Map<K, V> expected) {
        return new TypeSafeDiagnosingMatcher<>() {

            private final ImmutableMap<K, Matcher<? super V>> _matchers =
                    ImmutableMap.copyOf(Maps.transformValues(expected, valueMatcherFunction));

            @Override
            public void describeTo(final Description description) {
                description.appendText("map same as ").appendValue(expected);
            }

            @Override
            protected boolean matchesSafely(final Map<K, V> item, final Description mismatchDescription) {
                final Set<K> expectedKeys = _matchers.keySet();
                if (!item.keySet().equals(expectedKeys)) {
                    mismatchDescription.appendText("key sets differ. was: " + item.keySet() + " expected: " + expectedKeys);
                    return false;
                }

                for (final Entry<K, Matcher<? super V>> entry : _matchers.entrySet()) {
                    final K key = entry.getKey();
                    final Matcher<? super V> matcher = entry.getValue();
                    final V itemValue = item.get(key);
                    if (!matcher.matches(itemValue)) {
                        mismatchDescription.appendText("item " + key + ": ");
                        matcher.describeMismatch(itemValue, mismatchDescription);
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * matcher for {@link Multimap}s but only useful for list multi maps
     */
    @Nonnull
    public static <K, V> Matcher<Multimap<K, V>> sameMultimap(final Function<V, Matcher<? super V>> valueMatcherFunction,
            final Multimap<K, V> expected) {
        return new TypeSafeDiagnosingMatcher<>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("list multimap same as ").appendValue(expected);
            }

            @Override
            protected boolean matchesSafely(final Multimap<K, V> item, final Description mismatchDescription) {
                final Set<K> expectedKeys = expected.keySet();
                if (!item.keySet().equals(expectedKeys)) {
                    mismatchDescription.appendText("key sets differ. was: " + item.keySet() + " expected: " + expectedKeys);
                    return false;
                }

                for (final Entry<K, Collection<V>> entry : item.asMap().entrySet()) {
                    final K key = entry.getKey();
                    final Collection<V> itemValues = entry.getValue();
                    final Matcher<Iterable<V>> matcher = sameIterable(valueMatcherFunction, expected.get(key));
                    if (!matcher.matches(itemValues)) {
                        mismatchDescription.appendText("item " + key + ": ");
                        matcher.describeMismatch(itemValues, mismatchDescription);
                        return false;
                    }
                }
                return true;
            }

        };
    }

    @Nonnull
    public static <T extends Collection<?>> Matcher<T> isCollectionOfNElements(final int numberOfElements) {
        return new BaseMatcher<T>() {
            @Override
            public final boolean matches(final Object collection) {
                return ((Collection<?>) collection).size() == numberOfElements;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("collection has a size of ").appendValue(Integer.valueOf(numberOfElements));
            }
        };
    }

    /**
     * creates a matcher from a predicate, assuming toString is implemented for type <T>
     */
    @Nonnull
    public static <T> Matcher<T> custom(final T expectedValue, final BiPredicate<? super T, ? super T> predicate) {
        return new TypeSafeDiagnosingMatcher<T>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("custom predicate is true for ").appendValue(expectedValue);
            }

            @Override
            protected boolean matchesSafely(final T item, final Description mismatchDescription) {
                final boolean result = predicate.test(item, expectedValue);
                if (!result) {
                    mismatchDescription.appendText("predicate does not match was: " + item + " expected: " + expectedValue);
                }
                return result;
            }

        };

    }

}
