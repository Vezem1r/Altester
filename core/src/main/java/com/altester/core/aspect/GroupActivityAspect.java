package com.altester.core.aspect;

import com.altester.core.exception.GroupInactiveException;
import com.altester.core.model.subject.Group;
import com.altester.core.service.subject.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupActivityAspect {

    private final GroupActivityService groupActivityService;

    private final List<String> readOperations = Arrays.asList(
            "getGroup", "getAllGroups", "findGroup", "findAllGroups",
            "findById", "getById", "fetchGroup", "retrieveGroup"
    );

    @Before("execution(* com.altester.core.service.*Service.*(..)) && args(group,..)")
    public void checkGroupActivityBeforeOperation(JoinPoint joinPoint, Group group) {
        String methodName = joinPoint.getSignature().getName();
        log.debug("Checking activity for group {} before operation {}",
                group.getName(), methodName);

        boolean isActive = groupActivityService.checkAndUpdateGroupActivity(group);

        if (!isActive && !isReadOperation(methodName)) {
            throw new GroupInactiveException(
                    "Group " + group.getName() + " is inactive. Only read operations are allowed."
            );
        }
    }

    private boolean isReadOperation(String methodName) {
        return methodName.startsWith("get") || methodName.startsWith("find") ||
                methodName.startsWith("fetch") || methodName.startsWith("retrieve") ||
                readOperations.contains(methodName);
    }
}