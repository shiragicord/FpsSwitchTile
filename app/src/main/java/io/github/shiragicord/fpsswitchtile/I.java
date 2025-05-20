package io.github.shiragicord.fpsswitchtile;

public interface I<T> {
    <U extends T> void some(U t);
}
