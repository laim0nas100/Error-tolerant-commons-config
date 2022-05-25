package lt.lb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.TolerantConfig.ConversionTolerantFunction;
import org.apache.commons.configuration2.ImmutableConfiguration;

/**
 *
 * @author laim0nas100
 */
public abstract class KeyProp {
    
    public static <T> Builder<T> ofDefault(String key, T defaultVal, ConversionTolerantFunction<TolerantConfig, ? extends T> func) {
        Builder<T> builder = new Builder<>(key, func);
        builder.defaultSet = true;
        builder.defaultVal = defaultVal;
        return builder;
    }
    
    public static Builder<Boolean> of(String key, Boolean defaultVal) {
        return ofDefault(key, defaultVal, t -> t.getBoolean(key));
    }
    
    public static Builder<String> of(String key, String defaultVal) {
        return ofDefault(key, defaultVal, t -> t.getString(key));
    }
    
    public static Builder<Integer> of(String key, Integer defaultVal) {
        return ofDefault(key, defaultVal, t -> t.getInt(key));
    }
    
    public static Builder<Long> of(String key, Long defaultVal) {
        return ofDefault(key, defaultVal, t -> t.getLong(key));
    }
    
    public static Builder<Float> of(String key, Float defaultVal) {
        return ofDefault(key, defaultVal, t -> t.getFloat(key));
    }
    
    public static Builder<Double> of(String key, Double defaultVal) {
        return ofDefault(key, defaultVal, t -> t.getDouble(key));
    }
    
    public static Builder<BigInteger> of(String key, BigInteger defaultVal) {
        return ofDefault(key, defaultVal, t -> t.getBigInteger(key));
    }
    
    public static Builder<BigDecimal> of(String key, BigDecimal defaultVal) {
        return ofDefault(key, defaultVal, t -> t.getBigDecimal(key));
    }
    
    public static Builder<String[]> of(String key, String[] defaultVal) {
        return ofDefault(key, defaultVal, t -> t.getStringArray(key));
    }
    
    public static <T> Builder<List<T>> of(String key, Class<T> cls, List<T> defaultVal) {
        Objects.requireNonNull(cls);
        return ofDefault(key, defaultVal, t -> t.getList(cls, key));
    }
    
    public static Builder<List> of(String key, List defaultVal) {
        return ofDefault(key, defaultVal, t -> t.getList(key));
    }
    
    public static <T, C extends Collection<T>> Builder<C> of(String key, Class<T> cls, C defaultVal, Supplier<C> constructor) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(constructor);
        return ofDefault(key, defaultVal, t -> {
            C target = constructor.get();
            t.getCollection(cls, key, target);
            return target;
        });
    }
    
    public static <T> Builder<T> of(String key, Class<T> cls, T defaultVal) {
        Objects.requireNonNull(cls);
        return ofDefault(key, defaultVal, (t) -> {
            TolerantConfig<ImmutableConfiguration> tol = t;
            return tol.get(cls, key);
        });
    }
    
    public static Builder<Boolean> ofBoolean(String key) {
        return new Builder<>(key, f -> f.getBoolean(key));
    }
    
    public static Builder<String> ofString(String key) {
        return new Builder<>(key, f -> f.getString(key));
    }
    
    public static Builder<Integer> ofInteger(String key) {
        return new Builder<>(key, f -> f.getInt(key));
    }
    
    public static Builder<Long> ofLong(String key) {
        return new Builder<>(key, f -> f.getLong(key));
    }
    
    public static Builder<Float> ofFloat(String key) {
        return new Builder<>(key, f -> f.getFloat(key));
    }
    
    public static Builder<Double> ofDouble(String key) {
        return new Builder<>(key, f -> f.getDouble(key));
    }
    
    public static Builder<BigInteger> ofBigInteger(String key) {
        return new Builder<>(key, f -> f.getBigInteger(key));
    }
    
    public static Builder<BigDecimal> ofBigDecimal(String key) {
        return new Builder<>(key, f -> f.getBigDecimal(key));
    }
    
    public static Builder<String[]> ofStringArray(String key) {
        return new Builder<>(key, f -> f.getStringArray(key));
    }
    
    public static Builder<List> ofList(String key) {
        Builder<List> builder = new Builder<>(key, f -> f.getList(key));
        return builder;
    }
    
    public static <T> Builder<List<T>> ofList(String key, Class<T> cls) {
        Objects.requireNonNull(cls, "List class is null");
        Builder<List<T>> builder = new Builder<>(key, f -> f.getList(cls, key));
        return builder;
    }
    
    public static <T> Builder<Collection<T>> ofCollection(String key, Class<T> cls, Collection<T> target) {
        Objects.requireNonNull(cls, "Collection class is null");
        Builder<Collection<T>> builder = new Builder<>(key, f -> f.getCollection(cls, key, target));
        return builder;
    }
    
    public static class Builder<T> {
        
        protected final String key;
        protected Object defaultVal;
        
        protected boolean defaultSet = false;
        protected boolean cachable = false;
        
        protected ConversionTolerantFunction<TolerantConfig, ? extends T> fun;
        
        private Builder(String key, ConversionTolerantFunction<TolerantConfig, ? extends T> fun) {
            this.key = Objects.requireNonNull(key);
            this.fun = Objects.requireNonNull(fun);
        }
        
        private Builder(Builder old) {
            this.cachable = old.cachable;
            this.defaultSet = old.defaultSet;
            this.key = old.key;
            this.fun = old.fun;
            this.defaultVal = old.defaultVal;
        }
        
        public Key<T> toKey() {
            return () -> key;
        }
        
        public KeyProperty<T> toKeyProperty() {
            if (cachable) {
                return new CachableKeyProperty<>(new ResolvableKeyProperty<>(key, fun));
            } else {
                return new ResolvableKeyProperty<>(key, fun);
            }
        }
        
        public CachableDefaultKeyProperty<T> toCachableDefaultProperty() {
            if (!defaultSet) {
                throw new IllegalArgumentException("Default value was not set for KeyProperty:" + key);
            }
            return new CachableDefaultKeyProperty<>(new ResolvableDefaultKeyProperty<>(key, (T) defaultVal, fun));
        }
        
        public KeyDefaultProperty<T> toKeyDefaultProperty() {
            if (!defaultSet) {
                throw new IllegalArgumentException("Default value was not set for KeyProperty:" + key);
            }
            if (cachable) {
                return new CachableDefaultKeyProperty<>(new ResolvableDefaultKeyProperty<>(key, (T) defaultVal, fun));
            } else {
                return new ResolvableDefaultKeyProperty<>(key, (T) defaultVal, fun);
            }
        }
        
        public Builder<T> withCachable(boolean cachable) {
            Builder<T> b = new Builder<>(this);
            b.cachable = cachable;
            return b;
        }
        
    }

    /**
     * Static String key
     *
     * @param <T>
     */
    public static interface Key<T> {

        /**
         *
         * @return key
         */
        public String getKey();
    }

    /**
     * Basically like an entry with String keys.
     *
     * @param <T>
     */
    public static interface KeyVal<T> extends Key<T> {

        /**
         *
         * @return value
         */
        public T getValue();
        
    }

    /**
     * Property that can resolve values
     *
     * @param <T>
     */
    public static interface KeyProperty<T> extends Key<T> {
        
        public T resolve(TolerantConfig... config);
        
        public default T resolveThrowIfNull(TolerantConfig... prop) {
            T resolve = resolve(prop);
            if (resolve == null) {
                throw new NoSuchElementException(getKey() + " resolves to a null");
            }
            return resolve;
        }
        
        public T explicitResolve(TolerantConfig config);
        
        public default <U> KeyProperty<U> map(Function<? super T, ? extends U> mapper) {
            Objects.requireNonNull(mapper);
            KeyProperty<T> me = this;
            return new KeyProperty<U>() {
                @Override
                public U resolve(TolerantConfig... config) {
                    return mapper.apply(me.resolve(config));
                }
                
                @Override
                public String getKey() {
                    return me.getKey();
                }
                
                @Override
                public U explicitResolve(TolerantConfig config) {
                    return mapper.apply(me.explicitResolve(config));
                }
            };
        }
    }

    /**
     * Property that can resolve values and also has a default value
     *
     * @param <T>
     */
    public static interface KeyDefaultProperty<T> extends KeyProperty<T> {
        
        public T getDefault();
        
        @Override
        public default <U> KeyDefaultProperty<U> map(Function<? super T, ? extends U> mapper) {
            Objects.requireNonNull(mapper);
            KeyDefaultProperty<T> me = this;
            return new KeyDefaultProperty<U>() {
                @Override
                public U getDefault() {
                    return mapper.apply(me.getDefault());
                }
                
                @Override
                public U resolve(TolerantConfig... config) {
                    return mapper.apply(me.resolve(config));
                }
                
                @Override
                public String getKey() {
                    return me.getKey();
                }
                
                @Override
                public U explicitResolve(TolerantConfig config) {
                    return mapper.apply(me.explicitResolve(config));
                }
            };
        }
        
    }

    /**
     * Default implementation of {@link KeyDefaultProperty}
     *
     * @param <T>
     */
    public static class KDP<T> implements KeyDefaultProperty<T> {
        
        public final String key;
        public final T defaultVal;
        
        protected final TolerantConfig.ConversionTolerantFunction<TolerantConfig, ? extends T> func;
        
        public KDP(String key, T defaultVal, TolerantConfig.ConversionTolerantFunction<TolerantConfig, ? extends T> func) {
            this.key = Objects.requireNonNull(key);
            this.defaultVal = defaultVal;
            this.func = Objects.requireNonNull(func);
        }
        
        @Override
        public String getKey() {
            return key;
        }
        
        @Override
        public T getDefault() {
            return defaultVal;
        }
        
        @Override
        public T resolve(TolerantConfig... prop) {
            if (prop == null || prop.length == 0) {
                return getDefault();
            }
            for (int i = 0; i < prop.length; i++) {
                TolerantConfig conf = prop[i];
                if (conf == null) {
                    throw new IllegalArgumentException("TolerantConfig at index " + i + " is null");
                }
                if (conf.containsKey(key)) {
                    T resolved = func.apply(conf);
                    if (resolved != null) {
                        return resolved;
                    }
                }
                
            }
            return getDefault();
        }
        
        @Override
        public T explicitResolve(TolerantConfig config) {
            Objects.requireNonNull(config);
            return func.convert(config);
        }
        
        public static <T> KDP<T> of(String key, T defaultVal, TolerantConfig.ConversionTolerantFunction<TolerantConfig, ? extends T> func) {
            return new KDP(key, defaultVal, func);
        }
        
        public static KDP<Boolean> of(String key, Boolean defaultVal) {
            return of(key, defaultVal, t -> t.getBoolean(key, defaultVal));
        }
        
        public static KDP<String> of(String key, String defaultVal) {
            return of(key, defaultVal, t -> t.getString(key, defaultVal));
        }
        
        public static KDP<Integer> of(String key, Integer defaultVal) {
            return of(key, defaultVal, t -> t.getInteger(key, defaultVal));
        }
        
        public static KDP<Long> of(String key, Long defaultVal) {
            return of(key, defaultVal, t -> t.getLong(key, defaultVal));
        }
        
        public static KDP<Float> of(String key, Float defaultVal) {
            return of(key, defaultVal, t -> t.getFloat(key, defaultVal));
        }
        
        public static KDP<Double> of(String key, Double defaultVal) {
            return of(key, defaultVal, t -> t.getDouble(key, defaultVal));
        }
        
        public static KDP<BigInteger> of(String key, BigInteger defaultVal) {
            return of(key, defaultVal, t -> t.getBigInteger(key, defaultVal));
        }
        
        public static KDP<BigDecimal> of(String key, BigDecimal defaultVal) {
            return of(key, defaultVal, t -> t.getBigDecimal(key, defaultVal));
        }
        
        public static KDP<String[]> of(String key, String[] defaultVal) {
            return of(key, defaultVal, t -> t.getStringArray(key, defaultVal));
        }
        
        public static <T> KDP<List<T>> of(String key, Class<T> cls, List<T> defaultVal) {
            Objects.requireNonNull(cls);
            return of(key, defaultVal, t -> t.getList(cls, key, defaultVal));
        }
        
        public static KDP<List> of(String key, List defaultVal) {
            return of(key, defaultVal, t -> t.getList(key, defaultVal));
        }
        
        public static <T, C extends Collection<T>> KDP<C> of(String key, Class<T> cls, C defaultVal, Supplier<C> constructor) {
            Objects.requireNonNull(cls);
            Objects.requireNonNull(constructor);
            return of(key, defaultVal, t -> {
                C target = constructor.get();
                t.getCollection(cls, key, target, defaultVal);
                return target;
            });
        }
        
        public static <T> KDP<T> of(String key, Class<T> cls, T defaultVal) {
            Objects.requireNonNull(cls);
            return of(key, defaultVal, (t) -> {
                TolerantConfig<ImmutableConfiguration> tol = t;
                return tol.get(cls, key, defaultVal);
            });
        }
        
    }
    
    public static class ResolvableKeyProperty<T> implements KeyProperty<T> {
        
        protected final String key;
        protected final TolerantConfig.ConversionTolerantFunction<TolerantConfig, ? extends T> fun;
        
        public ResolvableKeyProperty(String key, TolerantConfig.ConversionTolerantFunction<TolerantConfig, ? extends T> fun) {
            this.key = Objects.requireNonNull(key);
            this.fun = Objects.requireNonNull(fun);
        }
        
        @Override
        public T resolve(TolerantConfig... prop) {
            if (prop == null || prop.length == 0) {
                throw new IllegalArgumentException("No config was provided");
            }
            for (int i = 0; i < prop.length; i++) {
                TolerantConfig conf = prop[i];
                if (conf == null) {
                    throw new IllegalArgumentException("TolerantConfig at index " + i + " is null");
                }
                if (conf.containsKey(key)) {
                    T resolved = fun.apply(conf);
                    if (resolved != null) {
                        return resolved;
                    }
                }
                
            }
            throw new NoSuchElementException("No elements was found by key:" + key + " is configs:" + Arrays.asList(prop));
        }
        
        @Override
        public T explicitResolve(TolerantConfig config) {
            return fun.convert(Objects.requireNonNull(config));
        }
        
        @Override
        public String getKey() {
            return key;
        }
    }
    
    public static class CachableKeyProperty<T> implements KeyProperty<T> {
        
        protected KeyProperty<T> resolvable;
        
        public CachableKeyProperty(KeyProperty<T> prop) {
            this.resolvable = Objects.requireNonNull(prop);
        }
        
        public static int MAX_CACHE_SIZE = 128;
        // FIFO cache
        protected Map<TolerantConfig, T> cached = new LinkedHashMap<>();
        protected boolean isEmptyCached;
        protected T emptyCached; // only relevant if config returns default value that can be mapped

        private final Object lock = new Object();
        
        @Override
        public T resolve(TolerantConfig... prop) {
            
            if (prop.length == 0 && isEmptyCached) {
                return emptyCached;
            }
            for (TolerantConfig prop1 : prop) {
                if (cached.containsKey(prop1)) {
                    return cached.get(prop1);
                }
            }
            
            synchronized (lock) {
                T resolve = resolvable.resolve(prop);
                if (prop.length == 0) {
                    isEmptyCached = true;
                    emptyCached = resolve;
                } else {
                    if (prop.length == 1) {
                        cached.put(prop[0], resolve);
                    } else {
                        for (int i = 0; i < prop.length; i++) {
                            if (prop[i].containsKey(getKey())) {// resolve again, hopefully types match
                                if (Objects.equals(resolvable.resolve(prop[i]), resolve)) {
                                    cached.put(prop[i], resolve);
                                    break;
                                }
                                
                            }
                        }
                    }
                    while (cached.size() > MAX_CACHE_SIZE) {
                        cached.remove(cached.keySet().iterator().next());
                    }
                }
                return resolve;
            }
            
        }
        
        @Override
        public <U> CachableKeyProperty<U> map(Function<? super T, ? extends U> mapper) {
            return new CachableKeyProperty<>(resolvable.map(mapper));
        }
        
         @Override
        public T explicitResolve(TolerantConfig config) {
            return resolvable.explicitResolve(config);
        }
        
        @Override
        public String getKey() {
            return resolvable.getKey();
        }
        
    }
    
    public static class ResolvableDefaultKeyProperty<T> extends ResolvableKeyProperty<T> implements KeyDefaultProperty<T> {
        
        protected T defaultVal;
        
        public ResolvableDefaultKeyProperty(String key, T defaultVal, ConversionTolerantFunction<TolerantConfig, ? extends T> fun) {
            super(key, fun);
            this.defaultVal = defaultVal;
        }
        
        @Override
        public T resolve(TolerantConfig... prop) {
            if (prop == null || prop.length == 0) {
                return getDefault();
            }
            for (int i = 0; i < prop.length; i++) {
                TolerantConfig conf = prop[i];
                if (conf == null) {
                    throw new IllegalArgumentException("TolerantConfig at index " + i + " is null");
                }
                if (conf.containsKey(key)) {
                    T resolved = fun.apply(conf);
                    if (resolved != null) {
                        return resolved;
                    }
                }
                
            }
            return getDefault();
        }
        
        @Override
        public T getDefault() {
            return defaultVal;
        }
        
    }
    
    public static class CachableDefaultKeyProperty<T> extends CachableKeyProperty<T> implements KeyDefaultProperty<T> {
        
        protected KeyDefaultProperty<T> resolvableDefault;
        
        public CachableDefaultKeyProperty(KeyDefaultProperty<T> resolvable) {
            super(resolvable);
            this.resolvableDefault = resolvable;
        }
        
        @Override
        public <U> CachableDefaultKeyProperty<U> map(Function<? super T, ? extends U> mapper) {
            return new CachableDefaultKeyProperty<>(resolvableDefault.map(mapper));
        }
        
        @Override
        public T getDefault() {
            return resolvableDefault.getDefault();
        }
        
    }
    
    public static class KP implements KeyProp.KeyVal<Object> {
        
        private final String key;
        private final Object val;
        
        public KP(String key, Object val) {
            this.key = key;
            this.val = val;
        }
        
        @Override
        public String getKey() {
            return key;
        }
        
        @Override
        public Object getValue() {
            return val;
        }
        
    }
    
    public static class KPIterator implements Iterator<KP> {
        
        protected final Iterator<String> keys;
        protected final ImmutableConfiguration config;
        
        public KPIterator(Iterator<String> keys, ImmutableConfiguration config) {
            this.keys = Objects.requireNonNull(keys, "Keys are null");
            this.config = Objects.requireNonNull(config, "Configuration is null");
        }
        
        @Override
        public boolean hasNext() {
            return keys.hasNext();
        }
        
        @Override
        public KP next() {
            String key = keys.next();
            return new KP(key, config.getProperty(key));
        }
        
    }
    
}
