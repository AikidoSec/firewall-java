package dev.aikido.agent.wrappers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface Wrapper {
    String getName();

    ElementMatcher<? super MethodDescription> getMatcher();

    public ElementMatcher<? super TypeDescription> getTypeMatcher();
}
