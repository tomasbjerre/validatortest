package se.java6pp.api;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.reflect.Proxy.getProxyClass;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Predicate;

public class CallRecorder<T> {
	private static <T> Iterable<T> createProxy(final Iterable<T> actualObject) {
		final InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(final Object object, final Method method, final Object[] args) throws Throwable {
				calledMethod = of(method);
				calledArgs = ofNullable(args);
				return null;
			}
		};
		final Class<?> proxyClass = getProxyClass(actualObject.getClass().getClassLoader(),
				new Class[] { Iterable.class });
		try {
			return (Iterable<T>) proxyClass.getConstructor(new Class[] { InvocationHandler.class }) //
					.newInstance(new Object[] { handler });
		} catch (final Exception e) {
			throw propagate(e);
		}
	}

	public static <T> CallRecorder<T> from(final Iterable<T> actualObject) {
		return new CallRecorder<T>(actualObject);
	}

	public static <E> E nested(final Iterable<E> t) {
		return createProxy(t).iterator().next();
	}

	private static Optional<Method> calledMethod = empty();
	private static Optional<Object[]> calledArgs = empty();

	private final Iterable<T> actualObject;

	private CallRecorder(final Iterable<T> actualObject) {
		this.actualObject = actualObject;
	}

	@SuppressWarnings("unchecked")
	public <E> Iterable<T> thatHas(final E method, final Predicate<E> p) {
		final List<T> toReturn = newArrayList();
		for (final T element : this.actualObject) {
			try {
				final E callValue = (E) calledMethod.get().invoke(element, calledArgs.get());
				if (p.apply(callValue)) {
					toReturn.add(element);
				}
			} catch (final IllegalAccessException e) {
				propagate(e);
			} catch (final IllegalArgumentException e) {
				propagate(e);
			} catch (final InvocationTargetException e) {
				propagate(e);
			}
		}
		return toReturn;
	}
}
