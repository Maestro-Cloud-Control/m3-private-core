package io.maestro3.agent.terraform.util;

import io.maestro3.agent.terraform.exception.BadRequestException;
import io.maestro3.agent.terraform.exception.ConcurrentActionException;
import io.maestro3.agent.terraform.model.TerraformStack;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.tf.integration.model.TemplateStatus;
import io.maestro3.agent.tf.integration.task.TerraformTask;
import io.maestro3.sdk.internal.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class TerraformTemplateTaskValidator {

    private static final String MULTI_STACK_ERROR_MESSAGE = "Multi-stack terraform template '%s' is under %s action(s). " +
            "%s action is prohibited for multi-stack templates under any of the %s action(s)";
    private static final String SINGLE_STACK_ERROR_MESSAGE = "Single-stack terraform template '%s' is under %s action. " +
            "Please wait for the current task to complete";
    private static final String TASK_NOT_ALLOWED_FOR_STATUS_MESSAGE = "%s action is not allowed when template status is '%s'";
    private static final String QUEUED_TASK_PRESENT_ERROR = "%s is queued for execution for terraform template '%s'. " +
            "Please wait for it to finish.";
    private static final Map<TerraformTask, Set<TerraformTask>> PROHIBITED_CONCURRENT_TASKS = Map.of(
            TerraformTask.VALIDATE, Set.of(TerraformTask.VALIDATE, TerraformTask.PLAN, TerraformTask.APPLY),
            TerraformTask.PLAN, Set.of(TerraformTask.VALIDATE, TerraformTask.PLAN),
            TerraformTask.APPLY, Collections.singleton(TerraformTask.VALIDATE)
    );

    private TerraformTemplateTaskValidator() {
        throw new UnsupportedOperationException("Class is not designed for an instantiation");
    }

    public static void assertTaskAllowed(final TerraformTemplate template, final TerraformTask task) {
        assertTaskAllowed(template, task, false);
    }

    public static void assertTaskAllowed(final TerraformTemplate template, final TerraformTask task,
                                         final boolean ignoreQueuedTasksCheck) {
        checkConflictWithInProgressTasks(template, task);
        if (!ignoreQueuedTasksCheck) {
            checkAlreadyQueuedTasks(template);
        }
        validateStatus(template.getStatus(), task);
    }

    public static void assertTaskAllowed(final TerraformTemplate template, final TerraformStack stack,
                                         final TerraformTask task) {
        checkConflictWithInProgressTasks(template, task);
        checkAlreadyQueuedTasks(template);
        validateStatus(stack.getStatus(), task);
    }

    private static void checkConflictWithInProgressTasks(final TerraformTemplate template, final TerraformTask task) {
        final String templateName = template.getName();
        final Set<TerraformTask> tasksInProgress = new HashSet<>(template.getTasksInProgress().values());
        if (CollectionUtils.isEmpty(tasksInProgress)) {
            return;
        }

        if (!template.isMultiStack()) {
            // parallel actions are not allowed for single-stack templates to keep templates' statuses straight
            final String message = String.format(SINGLE_STACK_ERROR_MESSAGE, templateName, tasksInProgress);
            throw new ConcurrentActionException(message);
        }

        final Set<TerraformTask> prohibitedConcurrentTasks = PROHIBITED_CONCURRENT_TASKS.get(task);
        if (CollectionUtils.isEmpty(prohibitedConcurrentTasks)) {
            return;
        }
        final boolean actionAllowed = Collections.disjoint(tasksInProgress, prohibitedConcurrentTasks);
        if (!actionAllowed) {
            final String message = String.format(MULTI_STACK_ERROR_MESSAGE, templateName, tasksInProgress, task, prohibitedConcurrentTasks);
            throw new ConcurrentActionException(message);
        }
    }

    private static void checkAlreadyQueuedTasks(final TerraformTemplate template) {
        final Queue<String> autoTaskQueue = template.getAutoTaskQueue();
        if (!autoTaskQueue.isEmpty()) {
            final String queuedTask = autoTaskQueue.peek();
            final String message = String.format(QUEUED_TASK_PRESENT_ERROR, queuedTask, template.getName());
            throw new ConcurrentActionException(message);
        }
    }

    private static void validateStatus(final TemplateStatus status, final TerraformTask task) {
        if (!task.isStatusAllowed(status)) {
            final String message = String.format(TASK_NOT_ALLOWED_FOR_STATUS_MESSAGE, task, status);
            throw new BadRequestException(message);
        }
    }
}
