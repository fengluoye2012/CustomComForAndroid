package com.test.lifecycle_apt;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.test.lifecycle_annotation.AutoWired;
import com.test.lifecycle_annotation.enums.Type;
import com.test.lifecycle_apt.utils.Constants;
import com.test.lifecycle_apt.utils.Logger;
import com.test.lifecycle_apt.utils.TypeUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * 通过AutoWired 注解获取所有在Activity、Fragment中使用注解的成员变量，然后getEnclosingElement()获取所在的封装元素(所在的类中)。
 * <p>
 * 核心的注解处理类，在这里我们可以扫描源代码里所有的注解，找到我们需要的注解，然后作出相应处理
 * <p>
 * AutoService本身就是一个静态注解，在build/META-INF文件夹下生成了一个service指定文件
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes(Constants.ANNOTATION_TYPE_AUTO_WIRED)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoWiredProcessor extends AbstractProcessor {

    private Elements elementUtils;

    //用于文件处理
    private Filer filer;

    /**
     * 用于打印日志，简单将getMessager()封装
     * <p>
     * 也可以使用 System.out.println(）打印日志，在Build下看到对应的日志
     */
    private Logger logger;
    private Types types;
    private TypeUtils typeUtils;
    private String TAG = AutoWiredProcessor.class.getSimpleName();

    /**
     * TypeElement 表示当前Activity、Fragment
     * List<Element> 表示当前Activity、Fragment中的所有使用AutoWired 注解的成员变量
     */
    private Map<TypeElement, List<Element>> parentAndChild = new HashMap<>();

    private static final ClassName AndroidLog = ClassName.get("android.util", "Log");

    private static final ClassName NullPointerException = ClassName.get("java.lang", "NullPointerException");

    /**
     * 初始化方法会被注解处理工具调用，并传入参数processingEnvironment，该参数提供了很多有用的工具类，
     * 如：Elements、Types、Filter等等
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        types = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        logger = new Logger(processingEnv.getMessager());

        typeUtils = new TypeUtils(types, elementUtils);

        logger.info("AutoWiredProcessor init ");
    }


    /**
     * 相当于main函数，在这个方法中处理注解与生成新的文件，所有逻辑都在这里完成
     *
     * @param set              该方法需要处理的注解类型
     * @param roundEnvironment 关于一轮遍历中提供给我们调用的信息.
     * @return 改轮注解是否处理完成 true 下轮或者其他的注解处理器将不会接收到次类型的注解.用处不大
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if (CollectionUtils.isEmpty(set)) {
            return false;
        }

        //这里返回所有使用了AutoWired 注解的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AutoWired.class);
        categories(elements);

        try {
            generateHelper();
        } catch (Exception e) {
            logger.error(e);
        }

        return true;
    }

    /**
     * 将所有添加了注解的成员变量 找到对应的类
     *
     * @param elements
     */
    private void categories(Set<? extends Element> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return;
        }

        for (Element element : elements) {
            //返回 封装元素；如果没有，则返回 null
            TypeElement enclosingElement = (TypeElement) (element).getEnclosingElement();
            Name qualifiedName = enclosingElement.getQualifiedName();
            logger.info(element.getSimpleName() + " 所在的环境 is" + qualifiedName);

            //变量的修饰符包含private
            if (element.getModifiers().contains(Modifier.PRIVATE)) {
                throw new IllegalStateException("The AutoWired fields CAN NOT BE 'private'!!! please check field ["
                        + element.getSimpleName() + "] in class [" + enclosingElement.getQualifiedName() + "]");
            }

            if (parentAndChild.containsKey(enclosingElement)) {
                parentAndChild.get(enclosingElement).add(element);
            } else {
                List<Element> children = new ArrayList<>();
                children.add(element);
                parentAndChild.put(enclosingElement, children);
            }
        }
    }


    /**
     * 利用javapeot 生成java类
     */
    private void generateHelper() throws IllegalAccessException, IOException {
        TypeElement typeISyringe = elementUtils.getTypeElement(Constants.ISYRINGE);
        TypeElement typeJsonService = elementUtils.getTypeElement(Constants.JSON_SERVICE);

        TypeMirror activityTm = elementUtils.getTypeElement(Constants.ACTIVITY).asType();
        TypeMirror fragmentTm = elementUtils.getTypeElement(Constants.FRAGMENT).asType();
        TypeMirror fragmentV4Tm = elementUtils.getTypeElement(Constants.FRAGMENT_V4).asType();

        //方法参数
        ParameterSpec objParamSpec = ParameterSpec.builder(TypeName.OBJECT, "target").build();

        if (MapUtils.isEmpty(parentAndChild)) {
            return;
        }

        Set<Map.Entry<TypeElement, List<Element>>> entries = parentAndChild.entrySet();
        for (Map.Entry<TypeElement, List<Element>> entry : entries) {

            MethodSpec.Builder injectMethod = initInjectMethod();

            TypeElement parent = entry.getKey();
            List<Element> children = entry.getValue();

            String qualifiedName = parent.getQualifiedName().toString();
            String pkgName = qualifiedName.substring(0, qualifiedName.lastIndexOf(Constants.DOT));

            String fileName = parent.getSimpleName() + Constants.SUFFIX_AUTO_WIRED;
            logger.info("start process " + children.size() + " filed in " + parent.getSimpleName());

            //成员变量
            FieldSpec jsonServiceField = FieldSpec.builder(TypeName.get(typeJsonService.asType()), "jsonService", Modifier.PRIVATE).build();

            //类的信息
            TypeSpec.Builder helper = TypeSpec.classBuilder(fileName)//类名
                    .addJavadoc("Auto generate by " + TAG)//注释
                    .addField(jsonServiceField) //添加成员变量
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get(typeISyringe));


            //方法中添加
            injectMethod.addStatement("jsonService = $T.Factory.getInstance().create();", ClassName.get(typeJsonService));
            injectMethod.addStatement("$T substitute = ($T)target;", ClassName.get(parent), ClassName.get(parent));

            for (Element element : children) {
                AutoWired fieldConfig = element.getAnnotation(AutoWired.class);
                String fieldName = element.getSimpleName().toString();

                String originalValue = "substitute." + fieldName;
                String statment = "substitute." + fieldName + " = substitute.";

                boolean isActivity = false;
                if (types.isSubtype(parent.asType(), activityTm)) {
                    isActivity = true;
                    statment += "getIntent().";
                } else if (types.isSubtype(parent.asType(), fragmentTm) || types.isSubtype(parent.asType(), fragmentV4Tm)) {
                    statment += "getArguments().";
                } else {
                    throw new IllegalAccessException("The field [" + fieldName + "] need " +
                            "AutoWired from intent, its parent must be activity or fragment!");
                }

                statment = buildStatement(originalValue, statment, typeUtils.typeExchange(element), isActivity);

                if (statment.startsWith("jsonService.")) {   // Not mortals
                    injectMethod.beginControlFlow("if (null != jsonService)");
                    injectMethod.addStatement(
                            "substitute." + fieldName + " = " + statment,
                            (StringUtils.isEmpty(fieldConfig.name()) ? fieldName : fieldConfig.name()),
                            ClassName.get(element.asType())
                    );
                    injectMethod.nextControlFlow("else");
                    injectMethod.addStatement(
                            "$T.e(\"" + TAG + "\", \"You want automatic inject the field '"
                                    + fieldName + "' in class '$T' ," +
                                    " but JsonService not found in Router\")", AndroidLog, ClassName.get(parent));

                    injectMethod.endControlFlow();
                } else {
                    injectMethod.addStatement(statment, StringUtils.isEmpty(fieldConfig.name()) ? fieldName : fieldConfig.name());
                }


                // Validator
                if (fieldConfig.required() && !element.asType().getKind().isPrimitive()) {  // Primitive wont be check.
                    injectMethod.beginControlFlow("if (null == substitute." + fieldName + ")");
                    injectMethod.addStatement(
                            "$T.e(\"" + TAG + "\", \"The field '" + fieldName + "' is null," + "field description is:" + fieldConfig.desc() +
                                    ",in class '\" + $T.class.getName() + \"!\")", AndroidLog, ClassName.get(parent));

                    if (fieldConfig.throwOnNull()) {
                        injectMethod.addStatement("throw new $T(" +
                                "\"The field '" + fieldName + "' is null," + "field description is:" + fieldConfig.desc() +
                                ",in class '\" + $T.class.getName() + \"!\")", NullPointerException, ClassName.get(parent));
                    }

                    injectMethod.endControlFlow();
                }
            }
            helper.addMethod(injectMethod.build());

            // Generate autowire helper
            JavaFile.builder(pkgName, helper.build()).build().writeTo(filer);

            logger.info(">>> " + parent.getSimpleName() + " has been processed, " + fileName + " has been generated. <<<");
        }

        logger.info(">>> Autowired processor stop. <<<");
    }


    private MethodSpec.Builder initInjectMethod() {
        MethodSpec.Builder injectMethod = MethodSpec.methodBuilder("inject")
                .addParameter(ParameterSpec.builder(TypeName.OBJECT, "target").build()) //参数
                .addAnnotation(Override.class) //添加注解
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);//添加返回值

        return injectMethod;
    }

    /**
     * Activity、Fragment 接受Intent 参数
     */
    private String buildStatement(String originalValue, String statement, int type, boolean isActivity) {
        if (type == Type.BOOLEAN.ordinal()) {
            statement += (isActivity ? ("getBooleanExtra($S, " + originalValue + ")") : ("getBoolean($S)"));
        } else if (type == Type.BYTE.ordinal()) {
            statement += (isActivity ? ("getByteExtra($S, " + originalValue + ")") : ("getByte($S)"));
        } else if (type == Type.SHORT.ordinal()) {
            statement += (isActivity ? ("getShortExtra($S, " + originalValue + ")") : ("getShort($S)"));
        } else if (type == Type.INT.ordinal()) {
            statement += (isActivity ? ("getIntExtra($S, " + originalValue + ")") : ("getInt($S)"));
        } else if (type == Type.LONG.ordinal()) {
            statement += (isActivity ? ("getLongExtra($S, " + originalValue + ")") : ("getLong($S)"));
        } else if (type == Type.CHAR.ordinal()) {
            statement += (isActivity ? ("getCharExtra($S, " + originalValue + ")") : ("getChar($S)"));
        } else if (type == Type.FLOAT.ordinal()) {
            statement += (isActivity ? ("getFloatExtra($S, " + originalValue + ")") : ("getFloat($S)"));
        } else if (type == Type.DOUBLE.ordinal()) {
            statement += (isActivity ? ("getDoubleExtra($S, " + originalValue + ")") : ("getDouble($S)"));
        } else if (type == Type.STRING.ordinal()) {
            statement += (isActivity ? ("getStringExtra($S)") : ("getString($S)"));
        } else if (type == Type.PARCELABLE.ordinal()) {
            statement += (isActivity ? ("getParcelableExtra($S)") : ("getParcelable($S)"));
        } else if (type == Type.OBJECT.ordinal()) {
            statement = "jsonService.parseObject(substitute." +
                    (isActivity ? "getIntent()." : "getArguments().") +
                    (isActivity ? "getStringExtra($S)" : "getString($S)") + ", $T.class)";
        }

        return statement;
    }
}
