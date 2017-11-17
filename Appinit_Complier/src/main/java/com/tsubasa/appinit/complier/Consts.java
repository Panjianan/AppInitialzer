package com.tsubasa.appinit.complier;

/**
 * Created by tsubasa on 2017/11/15.
 */

public class Consts {
    // Generate
    public static final String SEPARATOR = "$$";
    public static final String PROJECT = "AppInitializer";
    public static final String METHOD_LOAD_INTO = "loadInto";
    public static final String NAME_OF_ROOT = PROJECT + SEPARATOR + "Root";
    public static final String NAME_OF_INITIALIZER_ROOT = PROJECT + SEPARATOR + "InitializerRoot";
    public static final String NAME_OF_GROUP = PROJECT + SEPARATOR + "Group" + SEPARATOR;
    public static final String PACKAGE_OF_GENERATE_FILE = "com.tsubasa.appinit.routes";
    public static final String WARNING_TIPS = "DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY " + PROJECT;

    // Options of processor
    public static final String KEY_MODULE_NAME = "moduleName";


    // Custom interface
    private static final String FACADE_PACKAGE = "com.tsubasa.appinit";
    private static final String TEMPLATE_PACKAGE = ".api.template";
    public static final String ITROUTE_ROOT = FACADE_PACKAGE + TEMPLATE_PACKAGE + ".IRouteRoot";
    public static final String ITROUTE_INITIALIZER_ROOT = FACADE_PACKAGE + TEMPLATE_PACKAGE + ".IInitializerRoot";

    // Annotation type
    public static final String ANNOTATION_TYPE_INJECTABLE = FACADE_PACKAGE + ".annotation.Injectable";
    public static final String ANNOTATION_TYPE_INITABLE = FACADE_PACKAGE + ".annotation.Initable";

    // Log
    public static final String PREFIX_OF_LOGGER = PROJECT + "::Compiler ";

    // System interface
    public static final String ACTIVITY = "android.app.Activity";
    public static final String FRAGMENT = "android.app.Fragment";
    public static final String FRAGMENT_V4 = "android.support.v4.app.Fragment";
    public static final String SERVICE = "android.app.Service";
    public static final String INITABLE = FACADE_PACKAGE + ".api.template.IInitializer";
}
