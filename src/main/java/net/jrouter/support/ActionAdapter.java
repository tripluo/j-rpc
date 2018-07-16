/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.jrouter.support;

import java.lang.annotation.Annotation;
import jrouter.annotation.Action;
import jrouter.annotation.Parameter;
import jrouter.annotation.Result;
import jrouter.annotation.Scope;
import jrouter.util.CollectionUtil;

/**
 * ActionAdapter.
 */
public abstract class ActionAdapter implements Action {

    @Override
    public String[] value() {
        return CollectionUtil.EMPTY_STRING_ARRAY;
    }

    @Override
    public String[] name() {
        return CollectionUtil.EMPTY_STRING_ARRAY;
    }

    @Override
    public String interceptorStack() {
        return "";
    }

    @Override
    public String[] interceptors() {
        return CollectionUtil.EMPTY_STRING_ARRAY;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public Result[] results() {
        return new Result[0];
    }

    @Override
    public Scope scope() {
        return Scope.SINGLETON;
    }

    @Override
    public Parameter[] parameters() {
        return new Parameter[0];
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

}
