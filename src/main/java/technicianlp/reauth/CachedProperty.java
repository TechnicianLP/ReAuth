package technicianlp.reauth;

final class CachedProperty<T> {
    private T value;
    private final T invalid;
    private boolean valid;

    private long timestamp;
    private final long validity;

    CachedProperty(long validity, T invalid) {
        this.validity = validity;
        this.invalid = invalid;
    }

    T get() {
        return valid ? value : invalid;
    }

    boolean check() {
        if (System.currentTimeMillis() - timestamp > validity)
            invalidate();
        return valid;
    }

    void invalidate() {
        valid = false;
    }

    void set(T value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
        this.valid = true;
    }
}
