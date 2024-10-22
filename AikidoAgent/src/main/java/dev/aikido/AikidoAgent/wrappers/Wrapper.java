package dev.aikido.AikidoAgent.wrappers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface Wrapper {
    String getName();
    ElementMatcher<? super MethodDescription> getMatcher();
}
