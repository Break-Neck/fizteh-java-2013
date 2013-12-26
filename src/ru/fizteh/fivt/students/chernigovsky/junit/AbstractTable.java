package ru.fizteh.fivt.students.chernigovsky.junit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractTable<ValueType> {
    protected final HashMap<String, ValueType> commitedEntries;
    protected final ThreadLocal<HashMap<String, ValueType>> changedEntries;
    protected final ThreadLocal<HashMap<String, ValueType>> removedEntries;
    protected final ReadWriteLock tableLock;
    protected final boolean autoCommit;
    protected final String tableName;
    private final Pattern pattern = Pattern.compile("\\s");

    public Set<Map.Entry<String, ValueType>> getEntrySet() {
        return commitedEntries.entrySet();
    }

    public AbstractTable(String name, boolean flag) {
        tableName = name;
        autoCommit = flag;
        commitedEntries = new HashMap<String, ValueType>();
        tableLock = new ReentrantReadWriteLock(true);
        changedEntries = new ThreadLocal<HashMap<String, ValueType>>() {
            protected HashMap<String, ValueType> initialValue() {
                return new HashMap<String, ValueType>();
            }
        };
        removedEntries = new ThreadLocal<HashMap<String, ValueType>>() {
            protected HashMap<String, ValueType> initialValue() {
                return new HashMap<String, ValueType>();
            }
        };
    }

    public AbstractTable(AbstractTable table) {
        tableName = table.tableName;
        autoCommit = table.autoCommit;
        commitedEntries = table.commitedEntries;
        changedEntries = table.changedEntries;
        removedEntries = table.removedEntries;
        tableLock = table.tableLock;
    }

    public String getName(){
        return tableName;
    }

    public abstract boolean valuesEqual(ValueType firstValue, ValueType secondValue);

    public int getDiffCount() {
        int diffCount = 0;

        try {
            tableLock.readLock().lock();
            for (String string : changedEntries.get().keySet()) {
                if (commitedEntries.get(string) == null || !valuesEqual(commitedEntries.get(string), changedEntries.get().get(string))) {
                    ++diffCount;
                }
            }

            for (String string : removedEntries.get().keySet()) {
                if (commitedEntries.get(string) != null) {
                    ++diffCount;
                }
            }
        } finally {
            tableLock.readLock().unlock();
        }

        return diffCount;
    }

    /**
     * Получает значение по указанному ключу.
     *
     * @param key Ключ.
     * @return Значение. Если не найдено, возвращает null.
     *
     * @throws IllegalArgumentException Если значение параметра key является null.
     */
    public ValueType get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        Matcher matcher = pattern.matcher(key);
        if (key.isEmpty() || matcher.find()) {
            throw new IllegalArgumentException("key is wrong");
        }

        if (removedEntries.get().get(key) != null) {
            return null;
        }

        if (changedEntries.get().get(key) != null) {
            return changedEntries.get().get(key);
        }

        ValueType value;
        try {
            tableLock.readLock().lock();
            value = commitedEntries.get(key);
        } finally {
            tableLock.readLock().unlock();
        }

        return value;
    }

    /**
     * Устанавливает значение по указанному ключу.
     *
     * @param key Ключ.
     * @param value Значение.
     * @return Значение, которое было записано по этому ключу ранее. Если ранее значения не было записано,
     * возвращает null.
     *
     * @throws IllegalArgumentException Если значение параметров key или value является null.
     */

    private ValueType putting(String key, ValueType value) {
        ValueType oldValue = get(key);
        try {
            tableLock.readLock().lock();
            ValueType commitedValue = commitedEntries.get(key);
        } finally {
            tableLock.readLock().unlock();
        }

        changedEntries.get().put(key, value);
        removedEntries.get().remove(key);

        return oldValue;
    }

    public ValueType put(String key, ValueType value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        Matcher matcher = pattern.matcher(key);
        if (key.isEmpty() || matcher.find()) {
            throw new IllegalArgumentException("key is wrong");
        }

        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        ValueType ans = putting(key, value);
        if (autoCommit) {
            try {
                commit();
            } catch (IOException ex) {
                throw new IllegalStateException("Write error");
            }

        }
        return ans;

    }

    /**
     * Удаляет значение по указанному ключу.
     *
     * @param key Ключ.
     * @return Значение. Если не найдено, возвращает null.
     *
     * @throws IllegalArgumentException Если значение параметра key является null.
     */

    private ValueType removing(String key) {
        ValueType oldValue = get(key);

        if (oldValue != null) {
            removedEntries.get().put(key, oldValue);
            changedEntries.get().remove(key);
        }

        return oldValue;
    }

    public ValueType remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(key);
        if (key.isEmpty() || matcher.find()) {
            throw new IllegalArgumentException("key is wrong");
        }

        ValueType ans = removing(key);
        if (autoCommit) {
            try {
                commit();
            } catch (IOException ex) {
                throw new IllegalStateException();
            }
        }
        return ans;

    }

    /**
     * Возвращает количество ключей в таблице.
     *
     * @return Количество ключей в таблице.
     */
    public int size() {
        int size;
        try {
            tableLock.readLock().lock();
            size = commitedEntries.size();

            for (String string : changedEntries.get().keySet()) {
                if (commitedEntries.get(string) == null) {
                    ++size;
                }
            }

            for (String string : removedEntries.get().keySet()) {
                if (commitedEntries.get(string) != null) {
                    --size;
                }
            }
        } finally {
            tableLock.readLock().unlock();
        }

        return size;
    }

    /**
     * Выполняет фиксацию изменений.
     *
     * @return Количество сохранённых ключей.
     */
    public int commit() throws IOException {
        int changed = getDiffCount();

        try {
            tableLock.writeLock().lock();
            for (Map.Entry<String, ValueType> entry : changedEntries.get().entrySet()) {
                commitedEntries.put(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, ValueType> entry : removedEntries.get().entrySet()) {
                commitedEntries.remove(entry.getKey());
            }
        } finally {
            tableLock.writeLock().unlock();
        }

        changedEntries.get().clear();
        removedEntries.get().clear();

        return changed;
    }

    /**
     * Выполняет откат изменений с момента последней фиксации.
     *
     * @return Количество отменённых ключей.
     */
    public int rollback() {
        int changed = getDiffCount();

        changedEntries.get().clear();
        removedEntries.get().clear();

        return changed;
    }
}
