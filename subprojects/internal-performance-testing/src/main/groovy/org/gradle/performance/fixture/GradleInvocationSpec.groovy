/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.performance.fixture

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.gradle.initialization.ParallelismBuildOptions
import org.gradle.integtests.fixtures.executer.GradleDistribution
import org.gradle.profiler.BuildAction
import org.gradle.profiler.RunTasksAction

@CompileStatic
@EqualsAndHashCode
class GradleInvocationSpec implements InvocationSpec {
    final GradleDistribution gradleDistribution
    final File workingDirectory
    final List<String> tasksToRun
    final BuildAction buildAction
    final List<String> args
    /**
     * The GRADLE_OPTS for client VM. It's ignored by TAPI invocation.
     */
    final List<String> gradleOpts
    /**
     * The JVM arguments for daemon
     */
    final List<String> jvmArguments
    final List<String> cleanTasks
    final boolean useDaemon
    final boolean useToolingApi
    final boolean expectFailure
    final File buildLog

    GradleInvocationSpec(
        GradleDistribution gradleDistribution,
        File workingDirectory,
        List<String> tasksToRun,
        List<String> args,
        List<String> gradleOpts,
        List<String> jvmArguments,
        List<String> cleanTasks,
        boolean useDaemon,
        boolean useToolingApi,
        boolean expectFailure,
        BuildAction buildAction,
        File buildLog
    ) {
        this.gradleDistribution = gradleDistribution
        this.workingDirectory = workingDirectory
        this.tasksToRun = tasksToRun
        this.args = args
        this.gradleOpts = gradleOpts
        this.jvmArguments = jvmArguments
        this.cleanTasks = cleanTasks
        this.useDaemon = useDaemon
        this.useToolingApi = useToolingApi
        this.expectFailure = expectFailure
        this.buildAction = buildAction
        this.buildLog = buildLog
    }

    boolean getBuildWillRunInDaemon() {
        return useDaemon || useToolingApi
    }

    static InvocationBuilder builder() {
        return new InvocationBuilder()
    }

    InvocationBuilder withBuilder() {
        InvocationBuilder builder = new InvocationBuilder()
        builder.distribution(gradleDistribution)
        builder.workingDirectory(workingDirectory)
        builder.tasksToRun.addAll(this.tasksToRun)
        builder.args.addAll(args)
        builder.jvmArguments.addAll(jvmArguments)
        builder.cleanTasks.addAll(cleanTasks)
        builder.useDaemon = useDaemon
        builder.useToolingApi = useToolingApi
        builder.expectFailure = expectFailure
        builder.buildLog(buildLog)
        builder
    }

    GradleInvocationSpec withAdditionalJvmOpts(List<String> additionalJvmOpts) {
        InvocationBuilder builder = withBuilder()
        builder.jvmArguments.addAll(additionalJvmOpts)
        return builder.build()
    }

    GradleInvocationSpec withAdditionalArgs(List<String> additionalArgs) {
        InvocationBuilder builder = withBuilder()
        builder.args.addAll(additionalArgs)
        return builder.build()
    }

    static class InvocationBuilder implements Builder {
        GradleDistribution gradleDistribution
        File workingDirectory
        List<String> tasksToRun = []
        BuildAction buildAction
        List<String> args = []
        List<String> jvmArguments = []
        List<String> gradleOpts = []
        List<String> cleanTasks = []
        boolean useDaemon = true
        boolean useToolingApi
        boolean expectFailure
        File buildLog

        InvocationBuilder distribution(GradleDistribution gradleDistribution) {
            this.gradleDistribution = gradleDistribution
            this
        }

        InvocationBuilder workingDirectory(File workingDirectory) {
            this.workingDirectory = workingDirectory
            this
        }

        InvocationBuilder tasksToRun(String... taskToRun) {
            this.tasksToRun.addAll(Arrays.asList(taskToRun))
            this
        }

        InvocationBuilder tasksToRun(Iterable<String> taskToRun) {
            this.tasksToRun.addAll(taskToRun)
            this
        }

        InvocationBuilder buildAction(BuildAction buildAction) {
            this.buildAction = buildAction
            this
        }

        InvocationBuilder args(String... args) {
            this.args.addAll(Arrays.asList(args))
            this
        }

        InvocationBuilder jvmArgs(String... gradleOpts) {
            this.jvmArguments.addAll(Arrays.asList(gradleOpts))
            this
        }

        InvocationBuilder jvmArgs(Iterable<String> gradleOpts) {
            this.jvmArguments.addAll(gradleOpts)
            this
        }

        InvocationBuilder gradleOpts(String... gradleOpts) {
            this.gradleOpts.addAll(Arrays.asList(gradleOpts))
            this
        }

        InvocationBuilder gradleOpts(Iterable<String> gradleOpts) {
            this.gradleOpts.addAll(gradleOpts)
            this
        }

        InvocationBuilder cleanTasks(String... cleanTasks) {
            this.cleanTasks(Arrays.asList(cleanTasks))
        }

        InvocationBuilder cleanTasks(Iterable<String> cleanTasks) {
            this.cleanTasks.addAll(cleanTasks)
            this
        }

        InvocationBuilder useDaemon(boolean flag) {
            this.useDaemon = flag
            this
        }

        InvocationBuilder useToolingApi() {
            useToolingApi(true)
            this
        }

        InvocationBuilder useToolingApi(boolean flag) {
            this.useToolingApi = flag
            this
        }

        InvocationBuilder disableParallelWorkers() {
            jvmArgs("-D${ParallelismBuildOptions.MaxWorkersOption.GRADLE_PROPERTY}=1")
        }

        InvocationBuilder buildLog(File buildLog) {
            this.buildLog = buildLog
            this
        }

        @Override
        Builder expectFailure() {
            expectFailure = true
            this
        }

        GradleInvocationSpec build() {
            assert gradleDistribution != null
            assert workingDirectory != null

            return new GradleInvocationSpec(
                gradleDistribution,
                workingDirectory,
                tasksToRun.asImmutable(),
                args.asImmutable(),
                gradleOpts.asImmutable(),
                jvmArguments.asImmutable(),
                cleanTasks.asImmutable(),
                useDaemon,
                useToolingApi,
                expectFailure,
                buildAction ?: new RunTasksAction(tasksToRun),
                buildLog)
        }

    }
}
