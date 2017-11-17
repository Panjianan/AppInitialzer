package com.tsubasa.appinit.api.template;

import java.util.Map;
import java.util.Set;

/***
 * Template of Initialzer group.
 */
public interface IInitializerRoot {
    void loadInto(Map<String, Set<Class<? extends IInitializer>>> initializer);
}
