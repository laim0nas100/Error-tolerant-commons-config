package lt.lb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.KeyProp.KPIterator;
import lt.lb.KeyProp.KeyProperty;
import lt.lb.KeyProp.KeyVal;
import org.apache.commons.configuration2.ConfigurationDecoder;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.ImmutableHierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * Error tolerant configuration based on
 * {@link org.apache.commons.configuration2.ImmutableConfiguration}
 *
 * @author laim0nas100
 * @param <Conf>
 */
public interface TolerantConfig<Conf extends ImmutableConfiguration> extends ImmutableConfiguration {

    public static class DefaultTolerantConfig<C extends ImmutableConfiguration> implements TolerantConfig<C> {

        protected final ConfigurationSupplier<C> supply;

        public DefaultTolerantConfig(ConfigurationSupplier<C> supply) {
            this.supply = Objects.requireNonNull(supply, "ConfigurationSupplier must not be null");
        }

        @Override
        public C getDelegated() {
            return supply.getConfOrNull();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getDelegated());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DefaultTolerantConfig<?> other = (DefaultTolerantConfig<?>) obj;
            return Objects.equals(this.getDelegated(), other.getDelegated());
        }

    }

    public static final TolerantConfig empty = new DefaultTolerantConfig(() -> null);

    public static TolerantConfig empty() {
        return empty;
    }

    public static TolerantConfig of(ImmutableConfiguration conf) {
        if (conf == null) {
            return empty;
        }
        return new DefaultTolerantConfig<>(() -> conf);
    }

    public static <C extends ImmutableConfiguration> TolerantConfig<C> ofSuplier(final ConfigurationSupplier<C> supply) {
        return new DefaultTolerantConfig<>(supply);
    }

    public static <C extends ImmutableConfiguration> TolerantConfig<C> ofSuplierCached(final ConfigurationSupplier<C> supply) {
        Objects.requireNonNull(supply, "Supplier must not be null");
        return ofSuplier(new CachingConfSupplier<>(supply));
    }

    public static interface ConversionTolerantFunction<C extends ImmutableConfiguration, T> extends Function<C, T> {

        @Override
        public default T apply(C t) {
            try {
                return convert(t);
            } catch (ConversionException conv) {
                return null;
            }
        }

        public T convert(C t);

    }

    public static interface ConfigurationSupplier<T extends ImmutableConfiguration> {

        public T getConfiguration() throws ConfigurationException;

        public default T getConfOrNull() {
            try {
                return getConfiguration();
            } catch (ConfigurationException ex) {
                return null;
            }
        }

        public default boolean isChanged() { // might want to hot reload
            return false;
        }

    }

    public static class CachingConfSupplier<T extends ImmutableConfiguration> implements ConfigurationSupplier<T> {

        protected final ConfigurationSupplier<T> supply;
        protected T conf;
        protected final boolean sync;

        public CachingConfSupplier(ConfigurationSupplier<T> real) {
            this(real, true);
        }

        public CachingConfSupplier(ConfigurationSupplier<T> real, boolean sync) {
            this.supply = Objects.requireNonNull(real);
            this.sync = sync;
        }

        @Override
        public T getConfiguration() throws ConfigurationException {
            if (conf != null && !supply.isChanged()) {
                return conf;
            }
            if (sync) {
                synchronized (supply) {
                    conf = supply.getConfOrNull();
                }
            } else {
                conf = supply.getConfOrNull();
            }

            return conf;
        }

        @Override
        public boolean isChanged() {
            return supply.isChanged();
        }

    }

    public Conf getDelegated();

    public default Iterator<? extends KeyVal> getEntries() {
        ImmutableConfiguration delegated = getDelegated();
        if (delegated == null) {
            return Collections.emptyIterator();
        }
        return new KPIterator(delegated.getKeys(), delegated);
    }

    public default Iterator<? extends KeyVal> getEntries(String prefix) {
        ImmutableConfiguration delegated = getDelegated();
        if (delegated == null) {
            return Collections.emptyIterator();
        }
        return new KPIterator(delegated.getKeys(prefix), delegated);
    }

    public default <T> T getByProp(KeyProperty<T> prop) {
        Objects.requireNonNull(prop, "KeyProperty must be supplied");
        return prop.resolve(this);
    }

    public default boolean getOr(String key, boolean or) {
        return getBoolean(key, or);
    }

    /**
     * Get the property converted to int or return the given default if property
     * was not present or conversion has failed
     *
     * @param key
     * @param or
     * @return
     */
    public default int getOr(String key, int or) {
        return getInt(key, or);
    }

    /**
     * Get the property converted to long or return the given default if
     * property was not present or conversion has failed
     *
     * @param key
     * @param or
     * @return
     */
    public default long getOr(String key, long or) {
        return getLong(key, or);
    }

    /**
     * Get the property converted to float or return the given default if
     * property was not present or conversion has failed
     *
     * @param key
     * @param or
     * @return
     */
    public default float getOr(String key, float or) {
        return getFloat(key, or);
    }

    /**
     * Get the property converted to double or return the given default if
     * property was not present or conversion has failed
     *
     * @param key
     * @param or
     * @return
     */
    public default double getOr(String key, double or) {
        return getDouble(key, or);
    }

    /**
     * Get the property converted to BigInteger or return the given default if
     * property was not present or conversion has failed
     *
     * @param key
     * @param or
     * @return
     */
    public default BigInteger getOr(String key, BigInteger or) {
        return getBigInteger(key, or);
    }

    /**
     * Get the property converted to BigDecimal or return the given default if
     * property was not present or conversion has failed
     *
     * @param key
     * @param or
     * @return
     */
    public default BigDecimal getOr(String key, BigDecimal or) {
        return getBigDecimal(key, or);
    }

    /**
     * Get the property as String or return the given default if property was
     * not present
     *
     * @param key
     * @param or
     * @return
     */
    public default String getOr(String key, String or) {
        return getString(key, or);
    }

    /**
     * Get the property converted to String array or return the given default if
     * property was not present or conversion has failed
     *
     * @param key
     * @param or
     * @return
     */
    public default String[] getOr(String key, String[] or) {
        return getStringArray(key, or);
    }

    /**
     * Get the property converted to specified type using the conversion
     * function or return the given default if property was not present or
     * conversion has failed
     *
     * @param func
     * @param ifNot
     * @return
     */
    public default <T> T getOr(ConversionTolerantFunction<Conf, T> func, T ifNot) {
        return Optional.ofNullable(getDelegated()).map(func).orElse(ifNot);
    }

    /**
     * Get the property converted to specified type using the conversion
     * function or return the given default from a supplier if property was not
     * present or conversion has failed
     *
     * @param func
     * @param ifNot
     * @return
     */
    public default <T> T getOrSup(ConversionTolerantFunction<Conf, T> func, Supplier<? extends T> ifNot) {
        return Optional.ofNullable(getDelegated()).map(func).orElseGet(ifNot);
    }

    /**
     * Get the property converted to specified type using the conversion
     * function or throw {@link NoSuchElementException} if property was not
     * present or conversion has failed
     *
     * @param key
     * @param func
     * @return
     */
    public default <T> T getOrThrow(String key, Function<Conf, T> func) {
        return Optional.ofNullable(getDelegated()).map(func).orElseThrow(() -> new NoSuchElementException(key));
    }

    @Override
    public default boolean isEmpty() {
        return getOr(ImmutableConfiguration::isEmpty, true);
    }

    @Override
    public default int size() {
        return getOr(ImmutableConfiguration::size, 0);
    }

    @Override
    public default boolean containsKey(String key) {
        return getOr(p -> p.containsKey(key), false);
    }

    @Override
    public default Object getProperty(String key) {
        return getOr(p -> p.getProperty(key), null);
    }

    @Override
    public default Iterator<String> getKeys(String prefix) {
        return getOr(p -> p.getKeys(prefix), Collections.EMPTY_LIST.iterator());
    }

    @Override
    public default Iterator<String> getKeys() {
        return getOr(ImmutableConfiguration::getKeys, Collections.EMPTY_LIST.iterator());
    }

    @Override
    public default Properties getProperties(String key) {
        return getOrSup(p -> p.getProperties(key), Properties::new);
    }

    @Override
    public default boolean getBoolean(String key) {
        return getOrThrow(key, p -> p.getBoolean(key));
    }

    @Override
    public default boolean getBoolean(String key, boolean defaultValue) {
        return getOr(p -> p.getBoolean(key, defaultValue), defaultValue);
    }

    @Override
    public default Boolean getBoolean(String key, Boolean defaultValue) {
        return getOr(p -> p.getBoolean(key, defaultValue), defaultValue);
    }

    @Override
    public default byte getByte(String key) {
        return getOrThrow(key, p -> p.getByte(key));
    }

    @Override
    public default byte getByte(String key, byte defaultValue) {
        return getOr(p -> p.getByte(key, defaultValue), defaultValue);
    }

    @Override
    public default Byte getByte(String key, Byte defaultValue) {
        return getOr(p -> p.getByte(key, defaultValue), defaultValue);
    }

    @Override
    public default double getDouble(String key) {
        return getOrThrow(key, p -> p.getDouble(key));
    }

    @Override
    public default double getDouble(String key, double defaultValue) {
        return getOr(p -> p.getDouble(key, defaultValue), defaultValue);
    }

    @Override
    public default Double getDouble(String key, Double defaultValue) {
        return getOr(p -> p.getDouble(key, defaultValue), defaultValue);
    }

    @Override
    public default float getFloat(String key) {
        return getOrThrow(key, p -> p.getFloat(key));
    }

    @Override
    public default float getFloat(String key, float defaultValue) {
        return getOr(p -> p.getFloat(key, defaultValue), defaultValue);
    }

    @Override
    public default Float getFloat(String key, Float defaultValue) {
        return getOr(p -> p.getFloat(key, defaultValue), defaultValue);
    }

    @Override
    public default int getInt(String key) {
        return getOrThrow(key, p -> p.getInt(key));
    }

    @Override
    public default int getInt(String key, int defaultValue) {
        return getOr(p -> p.getInt(key, defaultValue), defaultValue);
    }

    @Override
    public default Integer getInteger(String key, Integer defaultValue) {
        return getOr(p -> p.getInteger(key, defaultValue), defaultValue);
    }

    @Override
    public default long getLong(String key) {
        return getOrThrow(key, p -> p.getLong(key));
    }

    @Override
    public default long getLong(String key, long defaultValue) {
        return getOr(p -> p.getLong(key, defaultValue), defaultValue);
    }

    @Override
    public default Long getLong(String key, Long defaultValue) {
        return getOr(p -> p.getLong(key, defaultValue), defaultValue);
    }

    @Override
    public default short getShort(String key) {
        return getOrThrow(key, p -> p.getShort(key));
    }

    @Override
    public default short getShort(String key, short defaultValue) {
        return getOr(p -> p.getShort(key, defaultValue), defaultValue);
    }

    @Override
    public default Short getShort(String key, Short defaultValue) {
        return getOr(p -> p.getShort(key, defaultValue), defaultValue);
    }

    @Override
    public default BigDecimal getBigDecimal(String key) {
        return getOrThrow(key, p -> p.getBigDecimal(key));
    }

    @Override
    public default BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return getOr(p -> p.getBigDecimal(key, defaultValue), defaultValue);
    }

    @Override
    public default BigInteger getBigInteger(String key) {
        return getOrThrow(key, p -> p.getBigInteger(key));
    }

    @Override
    public default BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return getOr(p -> p.getBigInteger(key, defaultValue), defaultValue);
    }

    @Override
    public default String getString(String key) {
        return getOrThrow(key, p -> p.getString(key));
    }

    @Override
    public default String getString(String key, String defaultValue) {
        return getOr(p -> p.getString(key, defaultValue), defaultValue);
    }

    @Override
    public default String getEncodedString(String key, ConfigurationDecoder decoder) {
        return getOrThrow(key, p -> p.getEncodedString(key, decoder));
    }

    public default String getEncodedString(String key, ConfigurationDecoder decoder, String defaultValue) {
        return getOr(p -> p.getEncodedString(key, decoder), defaultValue);
    }

    @Override
    public default String getEncodedString(String key) {
        return getOrThrow(key, p -> p.getEncodedString(key));
    }

    public default String getEncodedString(String key, String defaultValue) {
        return getOr(p -> p.getEncodedString(key), defaultValue);
    }

    @Override
    public default String[] getStringArray(String key) {
        return getOrThrow(key, p -> p.getStringArray(key));
    }

    public default String[] getStringArray(String key, String[] defaultValue) {
        return getOr(p -> p.getStringArray(key), defaultValue);
    }

    @Override
    public default List getList(String key) {
        return getOrThrow(key, p -> p.getList(key));
    }

    @Override
    public default List getList(String key, List defaultValue) {
        return getOr(p -> p.getList(key, defaultValue), defaultValue);
    }

    @Override
    public default <T> T get(Class<T> cls, String key) {
        return getOrThrow(key, p -> p.get(cls, key));
    }

    @Override
    public default <T> T get(Class<T> cls, String key, T defaultValue) {
        return getOr(p -> p.get(cls, key, defaultValue), defaultValue);
    }

    @Override
    public default Object getArray(Class<?> cls, String key) {
        return getOrThrow(key, p -> p.getArray(cls, key));
    }

    @Override
    public default Object getArray(Class<?> cls, String key, Object defaultValue) {
        return getOr(p -> p.getArray(cls, key, defaultValue), defaultValue);
    }

    @Override
    public default <T> List<T> getList(Class<T> cls, String key) {
        return getOrThrow(key, p -> p.getList(cls, key));
    }

    @Override
    public default <T> List<T> getList(Class<T> cls, String key, List<T> defaultValue) {
        return getOr(p -> p.getList(cls, key, defaultValue), defaultValue);
    }

    @Override
    public default <T> Collection<T> getCollection(Class<T> cls, String key, Collection<T> target) {
        return getOrThrow(key, p -> p.getCollection(cls, key, target));
    }

    @Override
    public default <T> Collection<T> getCollection(Class<T> cls, String key, Collection<T> target, Collection<T> defaultValue) {
        return getOr(p -> p.getCollection(cls, key, target, defaultValue), defaultValue);
    }

    @Override
    public default TolerantConfig<Conf> immutableSubset(String prefix) {
        return getOr(p -> TolerantConfig.of(p.immutableSubset(prefix)), TolerantConfig.empty);
    }

    /**
     * Get all current entries and put into a Properties object with full keys
     * which is then returned.
     *
     * @param prefix
     * @return
     */
    public default Properties nonTruncatedPropertySubset(String prefix) {
        final String final_prefix = prefix + ".";
        return immutableSubset(prefix).asProperties(k -> {
            if (k == null) {
                return null;
            }
            if (StringUtils.isBlank(k)) {
                return prefix;
            }
            return final_prefix + k;
        }, Function.identity());
    }

    /**
     * Get all current entries and put into a Properties object which is then
     * returned.
     *
     * @return
     */
    public default Properties asProperties() {
        return asProperties(Function.identity(), Function.identity());
    }

    /**
     * Get all current entries and put into a Properties object with key and
     * object modification function which is then returned.
     *
     * Null keys are not inserted
     *
     * @param keyMod
     * @param objectMod
     * @return
     */
    public default Properties asProperties(Function<String, String> keyMod, Function objectMod) {
        Objects.requireNonNull(keyMod, "Key modification function is empty");
        Objects.requireNonNull(objectMod, "Object modification function is null");
        Properties props = new Properties();
        Iterator<? extends KeyVal> entries = getEntries();
        while (entries.hasNext()) {
            KeyVal kv = entries.next();
            String key = keyMod.apply(kv.getKey());
            if (key == null) {
                continue;
            }
            props.put(key, objectMod.apply(kv.getValue()));
        }
        return props;
    }

    /**
     *
     * Error tolerant configuration based on
     * {@link org.apache.commons.configuration2.ImmutableHierarchicalConfiguration}
     *
     * @author laim0nas100
     * @param <Conf>
     */
    public static interface TolerantHConfig<Conf extends ImmutableHierarchicalConfiguration> extends TolerantConfig<Conf>, ImmutableHierarchicalConfiguration {

        public static final TolerantHConfig empty_h = () -> null;

        public static <C extends ImmutableHierarchicalConfiguration> TolerantHConfig<C> empty() {
            return empty_h;
        }

        public static <C extends ImmutableHierarchicalConfiguration> TolerantHConfig<C> of(C conf) {
            if (conf == null) {
                return empty_h;
            }
            return () -> conf;
        }

        public static <C extends ImmutableHierarchicalConfiguration> TolerantConfig<C> ofSuplier(final ConfigurationSupplier<C> supply) {
            Objects.requireNonNull(supply, "Supplier must not be null");
            return supply::getConfOrNull;
        }

        public static <C extends ImmutableHierarchicalConfiguration> TolerantConfig<C> ofSuplierCached(final ConfigurationSupplier<C> supply) {
            Objects.requireNonNull(supply, "Supplier must not be null");
            return ofSuplier(new CachingConfSupplier<>(supply));
        }

        @Override
        public default ExpressionEngine getExpressionEngine() {
            return getOrThrow("ExpressionEngine is not configured", p -> p.getExpressionEngine());
        }

        @Override
        public default int getMaxIndex(String key) {
            return getOr(p -> p.getMaxIndex(key), -1);
        }

        @Override
        public default String getRootElementName() {
            return getOr(p -> p.getRootElementName(), "");
        }

        @Override
        public default TolerantHConfig<Conf> immutableConfigurationAt(String key, boolean supportUpdates) {
            return (TolerantHConfig) getOr(p -> TolerantHConfig.of(p.immutableConfigurationAt(key, supportUpdates)), TolerantHConfig.empty());
        }

        @Override
        public default TolerantHConfig<Conf> immutableConfigurationAt(String key) {
            return (TolerantHConfig) getOr(p -> TolerantHConfig.of(p.immutableConfigurationAt(key)), TolerantHConfig.empty());
        }

        @Override
        public default List<ImmutableHierarchicalConfiguration> immutableConfigurationsAt(String key) {
            return getOr(p -> p.immutableConfigurationsAt(key), Collections.emptyList());
        }

        @Override
        public default List<ImmutableHierarchicalConfiguration> immutableChildConfigurationsAt(String key) {
            return getOr(p -> p.immutableChildConfigurationsAt(key), Collections.emptyList());
        }

    }

}
