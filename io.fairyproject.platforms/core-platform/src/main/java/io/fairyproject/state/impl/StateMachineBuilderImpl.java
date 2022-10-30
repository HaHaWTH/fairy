/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.state.impl;

import io.fairyproject.state.*;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class StateMachineBuilderImpl implements StateMachineBuilder {

    private final Map<State, StateConfigBuilderImpl> states = new HashMap<>();
    private final StateMachineTransitionBuilderImpl transitionBuilder = new StateMachineTransitionBuilderImpl();
    private Duration interval;
    private State initialState;

    @Override
    public @NotNull StateConfigBuilder state(@NotNull State state) {
        return this.states.computeIfAbsent(state, StateConfigBuilderImpl::new);
    }

    @Override
    public @NotNull StateMachineBuilder interval(@NotNull Duration interval) {
        this.interval = interval;
        return this;
    }

    @Override
    public @NotNull StateMachineBuilder initialState(@NotNull State state) {
        this.initialState = state;
        return this;
    }

    @Override
    public @NotNull StateMachineTransitionBuilderImpl transition() {
        return this.transitionBuilder;
    }

    @Override
    public @NotNull StateMachine build() {
        StateMachineImpl stateMachine = new StateMachineImpl();

        Transition transition = this.transitionBuilder.build(stateMachine);
        stateMachine.setTransition(transition);

        this.states.forEach((state, stateConfigBuilder) -> {
            StateConfig stateConfig = stateConfigBuilder.build(stateMachine);
            stateMachine.addState(state, stateConfig);
        });

        if (this.interval != null) {
            stateMachine.setInterval(this.interval);
        }

        stateMachine.start(this.initialState);
        return stateMachine;
    }
}
