package com.kainv.junit.extension;

import com.kainv.service.UserService;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class UserServiceParamResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
//        Определим тип параметра и если он равен UserService.class, то это отличный вариант чтобы мы установили в качестве зависимости свой
        return parameterContext.getParameter().getType() == UserService.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
//        Для методов которые требуют UserService мы возвращаем всегда один и тот же store:
        ExtensionContext.Store store = extensionContext.getStore(Namespace.create(extensionContext.getTestMethod()));
//        Получаем по ключу значение
        return store.getOrComputeIfAbsent(UserService.class, it -> new UserService());
    }
}
