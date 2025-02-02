/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.bukkit.listener.asm;

import io.fairyproject.bukkit.listener.annotation.PlayerSearchAttribute;
import io.fairyproject.bukkit.player.PlayerEventRecognizer;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import io.fairyproject.bukkit.listener.FilteredEventList;

import java.lang.reflect.Method;

public class MethodHandleEventExecutor implements EventExecutor {

    private final boolean ignoredFilters;
    private final FilteredEventList eventList;
    private final Class<? extends Event> eventClass;
    private final Method handle;
    private final Class<? extends PlayerEventRecognizer.Attribute<?>>[] attributes;

    public MethodHandleEventExecutor(@NonNull Class<? extends Event> eventClass, @NonNull Method m, boolean ignoredFilters, FilteredEventList eventList) {
        this.eventClass = eventClass;
        this.ignoredFilters = ignoredFilters;
        this.eventList = eventList;

        m.setAccessible(true);
        this.handle = m;

        final PlayerSearchAttribute annotation = m.getAnnotation(PlayerSearchAttribute.class);
        if (annotation != null) {
            this.attributes = annotation.value();
        } else {
            this.attributes = new Class[0];
        }
    }

    @Override
    @SneakyThrows
    public void execute(@NonNull Listener listener, @NonNull Event event) {
        if (eventClass.isInstance(event) && (ignoredFilters || eventList.check(event, this.attributes))) {
            handle.invoke(listener, event);
        }
    }
}
