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

package org.gradle.internal.rule

import org.gradle.model.InvalidModelRuleDeclarationException
import org.gradle.model.Mutate
import org.gradle.model.internal.core.ModelType
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicReference

class RuleSourceBackedRuleActionTest extends Specification {
    private ModelType<List<String>> listType = new ModelType<List<String>>() {}
    private action
    private List collector = []

    def "creates rule action for rule source"() {
        when:
        action = RuleSourceBackedRuleAction.create(SimpleRuleSource, ModelType.of(List))

        then:
        action.inputTypes == [String, Integer, Set]

        when:
        action.execute(collector, ["foo", 1, ["bar", "baz"] as Set])

        then:
        collector == ["foo", 1, "bar", "baz"]
    }

    static class SimpleRuleSource {
        @Mutate
        void theRule(List subject, String input1, Integer input2, Set input3) {
            subject.add(input1)
            subject.add(input2)
            subject.addAll(input3)
        }
    }

    def "creates rule action for rule source with typed params"() {
        when:
        action = RuleSourceBackedRuleAction.create(RuleSourceWithTypedParams, listType)

        then:
        action.inputTypes == [AtomicReference, Map, Set]

        when:
        action.execute(collector, [new AtomicReference<String>("foo"), [2: "bar"], [4, 5] as Set])

        then:
        collector == ["foo", "2", "bar", "4", "5"]
    }

    static class RuleSourceWithTypedParams {
        @Mutate
        void theRule(List<String> subject, AtomicReference<String> input1, Map<Integer, String> input2, Set<Number> input3) {
            subject.add(input1.get())
            subject.addAll(input2.keySet().collect({it.toString()}))
            subject.addAll(input2.values())
            subject.addAll(input3.collect({it.toString()}))
        }
    }

    def "fails to create rule action for rule source that #issue"() {
        when:
        action = RuleSourceBackedRuleAction.create(ruleSource, listType)

        then:
        def e = thrown InvalidModelRuleDeclarationException
        e.message == "Type ${ruleSource.name} is not a valid model rule source: ${reason}"

        where:
        ruleSource                          | reason
        RuleSourceWithNoMethod              | "must have at exactly one method annotated with @Mutate"
        RuleSourceWithNoMutateMethod        | "must have at exactly one method annotated with @Mutate"
        RuleSourceWithMultipleMutateMethods | "must have at exactly one method annotated with @Mutate"
        RuleSourceWithDifferentSubjectType  | "first parameter of rule method must be of type java.util.List<java.lang.String>"
        RuleSourceWithNoSubject             | "rule method must have at least one parameter"
        RuleSourceWithReturnValue           | "rule method must return void"
    }

    static class RuleSourceWithNoMethod {}

    static class RuleSourceWithNoMutateMethod {
        void theRule(List<String> subject) {}
    }

    static class RuleSourceWithMultipleMutateMethods {
        @Mutate void theRule(List<String> subject) {}
        @Mutate void theOtherRule(List<String> subject) {}
    }

    static class RuleSourceWithDifferentSubjectType {
        @Mutate void theRule(String subject) {}
    }

    static class RuleSourceWithReturnValue {
        @Mutate String theRule(List<String> subject) {}
    }

    static class RuleSourceWithNoSubject {
        @Mutate void theRule() {}
    }
}
