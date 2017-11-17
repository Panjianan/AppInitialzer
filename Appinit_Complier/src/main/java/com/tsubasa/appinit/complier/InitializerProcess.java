package com.tsubasa.appinit.complier;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.tsubasa.appinit.annotation.Initable;
import com.tsubasa.appinit.annotation.Injectable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.tsubasa.appinit.complier.Consts.ACTIVITY;
import static com.tsubasa.appinit.complier.Consts.ANNOTATION_TYPE_INITABLE;
import static com.tsubasa.appinit.complier.Consts.ANNOTATION_TYPE_INJECTABLE;
import static com.tsubasa.appinit.complier.Consts.INITABLE;
import static com.tsubasa.appinit.complier.Consts.ITROUTE_INITIALIZER_ROOT;
import static com.tsubasa.appinit.complier.Consts.ITROUTE_ROOT;
import static com.tsubasa.appinit.complier.Consts.KEY_MODULE_NAME;
import static com.tsubasa.appinit.complier.Consts.METHOD_LOAD_INTO;
import static com.tsubasa.appinit.complier.Consts.NAME_OF_INITIALIZER_ROOT;
import static com.tsubasa.appinit.complier.Consts.NAME_OF_ROOT;
import static com.tsubasa.appinit.complier.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.tsubasa.appinit.complier.Consts.SEPARATOR;
import static com.tsubasa.appinit.complier.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;

@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ANNOTATION_TYPE_INJECTABLE, ANNOTATION_TYPE_INITABLE})
public class InitializerProcess extends AbstractProcessor {
    private Map<String, Set<Element>> groupMap = new HashMap<>(); // ModuleName and routeMeta.
    private Filer mFiler;       // File util, write class file into disk.
    private Logger logger;
    private Types types;
    private Elements elements;
    private String moduleName = null;   // Module name, maybe its 'app' or others

    /**
     * Initializes the processor with the processing environment by
     * setting the {@code processingEnv} field to the value of the
     * {@code processingEnv} argument.  An {@code
     * IllegalStateException} will be thrown if this method is called
     * more than once on the same object.
     *
     * @param processingEnv environment to access facilities the tool framework
     *                      provides to the processor
     * @throws IllegalStateException if this method is called more than once.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();                  // Generate class.
        elements = processingEnv.getElementUtils();      // Get class meta.
        types = processingEnv.getTypeUtils();            // Get type utils.

        logger = new Logger(processingEnv.getMessager());   // Package the log utils.

        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");

            logger.info("The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            logger.error("These no module name, at 'build.gradle', like :\n" +
                    "apt {\n" +
                    "    arguments {\n" +
                    "        moduleName project.getName();\n" +
                    "    }\n" +
                    "}\n");
            throw new RuntimeException("InitializerProcess::Compiler >>> No module name, for more information, look at gradle log.");
        }

        logger.info(">>> InitializerProcess init. <<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            logger.info(">>> " + ClassName.get(annotation).topLevelClassName() + " <<<");
        }
        if (CollectionUtils.isNotEmpty(annotations)) {
            Set<? extends Element> initElements = roundEnv.getElementsAnnotatedWith(Initable.class);
            try {
                if (initElements.size() > 1) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("initable cannot set over one time, 同一模块初始化代码不能超过1个\n");
                    for (Element routeElement : initElements) {
                        ClassName className = ClassName.get((TypeElement) routeElement);
                        sb.append("initable -> ").append(className.topLevelClassName()).append("\n");
                    }
                    throw new RuntimeException(sb.toString());
                }
                logger.info(">>> Found Initable, start... <<<");
                this.parseInitializer(initElements);

            } catch (Exception e) {
                logger.error(e);
            }

            Set<? extends Element> routeElements = roundEnv.getElementsAnnotatedWith(Injectable.class);
            try {
                if (routeElements != null) {
                    logger.info(">>> Found Injectable, start... <<<");
                    this.parseRoutes(routeElements);
                }
            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }

    private void parseRoutes(Set<? extends Element> routeElements) throws IOException {
        if (CollectionUtils.isNotEmpty(routeElements)) {
            // Perpare the type an so on.

            logger.info(">>> Found Injectable routes, size is " + routeElements.size() + " <<<");

            // 这个还要拿来判断Activity的
            TypeMirror typeActivity = elements.getTypeElement(ACTIVITY).asType();

            /*
               Build input type, format as :
               ```Map<String, Class<? extends IRouteGroup>>```
             */
            ParameterizedTypeName inputMapTypeOfRoot = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(Class.class),
                    ClassName.get(String.class)
            );

            /*
              Build input param name.
             */
            ParameterSpec rootParamSpec = ParameterSpec.builder(inputMapTypeOfRoot, "routes").build();

            /*
              Build method : 'loadInto'
             */
            MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(rootParamSpec);

            //  Follow a sequence, find out metas of group first, generate java file, then statistics them as root.
            for (Element element : routeElements) {
                TypeMirror tm = element.asType();
                if (types.isSubtype(tm, typeActivity)) {
                    loadIntoMethodOfRootBuilder.addStatement("routes.put($T.class, $S)", ClassName.get((TypeElement) element), moduleName);
                }
                // if (StringUtils.isEmpty(moduleName)) {   // Hasn't generate the module name.
                //     moduleName = ModuleUtils.generateModuleName(element, logger);
                // }
            }


            // Write root meta into disk.
            String rootFileName = NAME_OF_ROOT + SEPARATOR + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(rootFileName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(elements.getTypeElement(ITROUTE_ROOT)))
                            .addModifiers(PUBLIC)
                            .addMethod(loadIntoMethodOfRootBuilder.build())
                            .build()
            ).build().writeTo(mFiler);

            logger.info(">>> Generated root, name is " + rootFileName + " <<<");
        } else {
            logger.info(">>> InitializerProcess not Found Injectable. <<<");
        }
    }

    private void parseInitializer(Set<? extends Element> routeElements) throws IOException {
        if (CollectionUtils.isNotEmpty(routeElements)) {
            // Perpare the type an so on.

            logger.info(">>> Found Initable routes, size is " + routeElements.size() + " <<<");

            // 这个还要拿来判断可初始化的接口的
            TypeMirror typeInitializer = elements.getTypeElement(INITABLE).asType();

            /*
               Build input type, format as :
               ```Map<String, Class<? extends IInitializer>>```
             */
            ParameterizedTypeName inputMapTypeOfRoot = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ParameterizedTypeName.get(
                            ClassName.get(Set.class),
                            ParameterizedTypeName.get(
                                    ClassName.get(Class.class),
                                    WildcardTypeName.subtypeOf(ClassName.get(typeInitializer))
                            )
                    )
            );

            /*
              Build input param name.
             */
            ParameterSpec rootParamSpec = ParameterSpec.builder(inputMapTypeOfRoot, "routes").build();

            /*
              Build method : 'loadInto'
             */
            MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec.methodBuilder(METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(rootParamSpec);

            //  Follow a sequence, find out metas of group first, generate java file, then statistics them as root.
            for (Element element : routeElements) {
                TypeMirror tm = element.asType();
                if (types.isSubtype(tm, typeInitializer)) {
                    loadIntoMethodOfRootBuilder.addStatement("routes.get($S).add($T.class)", moduleName, ClassName.get((TypeElement) element));
                }
                // if (StringUtils.isEmpty(moduleName)) {   // Hasn't generate the module name.
                //     moduleName = ModuleUtils.generateModuleName(element, logger);
                // }
            }


            // Write root meta into disk.
            String rootFileName = NAME_OF_INITIALIZER_ROOT + SEPARATOR + moduleName;
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(rootFileName)
                            .addJavadoc(WARNING_TIPS)
                            .addSuperinterface(ClassName.get(elements.getTypeElement(ITROUTE_INITIALIZER_ROOT)))
                            .addModifiers(PUBLIC)
                            .addMethod(loadIntoMethodOfRootBuilder.build())
                            .build()
            ).build().writeTo(mFiler);

            logger.info(">>> Generated root, name is " + rootFileName + " <<<");
        } else {
            logger.info(">>> InitializerProcess not Found Initable. <<<");
        }
    }

}
