package com.test.lifecycle_apt;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.test.lifecycle_annotation.AutoWired;
import com.test.lifecycle_annotation.RouteNode;
import com.test.lifecycle_annotation.enums.NodeType;
import com.test.lifecycle_annotation.model.Node;
import com.test.lifecycle_annotation.utils.RouterUtils;
import com.test.lifecycle_apt.utils.Constants;
import com.test.lifecycle_apt.utils.FileUtils;
import com.test.lifecycle_apt.utils.Logger;
import com.test.lifecycle_apt.utils.StringUtils;
import com.test.lifecycle_apt.utils.TypeUtils;

import org.apache.commons.collections4.MapUtils;

import java.io.BufferedWriter;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

/**
 * 实现注解处理器
 * <p>
 * 核心的注解处理类，在这里我们可以扫描源代码里所有的注解，找到我们需要的注解，然后作出相应处理
 * <p>
 * AutoService本身就是一个静态注解，在build/META-INF文件夹下生成了一个service指定文件
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes(Constants.ANNOTATION_TYPE_ROUTE)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RouteNodeProcessor extends AbstractProcessor {

    private Elements elementUtils;

    //用于文件处理
    private Filer filer;

    private List<Node> routeNodes;

    /**
     * 用于打印日志，简单将getMessager()封装
     * <p>
     * 也可以使用 System.out.println(）打印日志，在Build下看到对应的日志
     */
    private Logger logger;
    private Types types;
    private TypeUtils typeUtils;
    /**
     * String 类型
     */
    private TypeMirror typeString;
    private String host;

    /**
     * 初始化方法会被注解处理工具调用，并传入参数processingEnvironment，该参数提供了很多有用的工具类，
     * 如：Elements、Types、Filter等等
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        routeNodes = new ArrayList<>();

        types = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        logger = new Logger(processingEnv.getMessager());

        typeUtils = new TypeUtils(types, elementUtils);
        typeString = elementUtils.getTypeElement("java.lang.String").asType();

        //可以在对应的module中的build.gradle中配置javaCompileOptions。
        Map<String, String> options = processingEnv.getOptions();
        if (options != null) {
            host = options.get(Constants.KEY_HOST_NAME);
            logger.info("host is " + host);
        }

        if (host == null || Constants.STRING_EMPTY.equals(host)) {
            host = Constants.DEFAULT_HOST;
        }
        logger.info("RouteNodeProcessor init ");
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

        if (set == null || set.isEmpty()) {
            return false;
        }

        //这里返回所有使用了RouteNode 注解的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(RouteNode.class);

        try {
            logger.info("Found routes start");
            parseRouteNodes(elements);
        } catch (Exception e) {
            logger.error(e);
        }

        generateRouterImpl();
        generateRouterTable();

        return true;
    }

    /**
     * 解析路由
     *
     * @param elements
     */
    private void parseRouteNodes(Set<? extends Element> elements) {

        //返回给定类型，并返回该元素的定义类型
        TypeMirror typeActivity = elementUtils.getTypeElement(Constants.ACTIVITY).asType();

        //遍历所有使用了该注解的元素
        for (Element element : elements) {
            //返回该元素的定义类型
            TypeMirror tm = element.asType();
            //获取注解，然后可以获取注解中的参数
            RouteNode routeNode = element.getAnnotation(RouteNode.class);

            //判断t1是否作为t2的子类, RouteNode 注解只能用在Activity;
            if (!types.isSubtype(tm, typeActivity)) {
                throw new IllegalStateException("only activity can be annotated by RouteNode");
            }

            logger.info("Found activity route is " + tm.toString());
            Node node = new Node();
            String path = routeNode.path();
            checkPath(path);

            node.setPath(path);
            node.setDesc(routeNode.desc());
            node.setPriority(routeNode.priority());
            node.setNodeType(NodeType.ACTIVITY);
            node.setRawType(element);

            //字段对应的类型，类型用数字来表示
            Map<String, Integer> paramsType = new HashMap<>();
            Map<String, String> paramsDesc = new HashMap<>();

            //遍历所有的成员变量
            for (Element field : element.getEnclosedElements()) {
                //判断是否为成员变量类型，并且是否添加了{@link AutoWired} 注解
                if (field.getKind().isField() && field.getAnnotation(AutoWired.class) != null) {
                    //获取成员变量的注解，可以获取参数的值
                    AutoWired paramConfig = field.getAnnotation(AutoWired.class);
                    paramsType.put(StringUtils.isEmpty(paramConfig.name())
                            ? field.getSimpleName().toString() : paramConfig.name(), typeUtils.typeExchange(field));

                    paramsDesc.put(StringUtils.isEmpty(paramConfig.name()) ? field.getSimpleName().toString() :
                            paramConfig.name(), typeUtils.typeDesc(field));
                }
            }
            node.setParamsType(paramsType);
            node.setParamsDesc(paramsDesc);

            if (!routeNodes.contains(node)) {
                routeNodes.add(node);
            }
        }
    }


    /**
     * 通过javapoet 生成Java 文件，如KotlinUiRouter.java
     */
    private void generateRouterImpl() {
        //全类名
        String className = RouterUtils.genHostUIRouterClass(host);

        String pkgName = className.substring(0, className.lastIndexOf(RouterUtils.DOT));

        String simpleName = className.substring(className.lastIndexOf(RouterUtils.DOT) + 1);

        ClassName supperClass = ClassName.get(elementUtils.getTypeElement(Constants.BASE_COMP_ROUTER));

        MethodSpec hostMethod = generateGetHostMethod();
        MethodSpec initMapMethod = generateInitMapMethod();


        TypeSpec classSpec = TypeSpec.classBuilder(simpleName)
                .addModifiers(Modifier.PUBLIC) //类的访问修饰符
                .superclass(supperClass) //超类
                .addMethod(hostMethod) //添加方法
                .addMethod(initMapMethod) //添加方法
                .build();

        //创建类
        try {
            JavaFile javaFile = JavaFile.builder(pkgName, classSpec).build();

            //logger.info(javaFile.toString());
            javaFile.writeTo(filer);

        } catch (IOException e) {
            logger.error(e);
        }
    }

    /**
     * generate HostRouterTable.txt
     */
    private void generateRouterTable() {
        String fileName = RouterUtils.genRouterTable(host);
        if (FileUtils.createFile(fileName)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("auto generated, do not change !!!! \n\n");
            stringBuilder.append("HOST : " + host + "\n\n");

            if (routeNodes != null && routeNodes.size() > 0) {
                logger.info("routeNodes size is " + routeNodes.size());
            } else {
                logger.info("routeNodes size is " + 0);
            }

            for (Node node : routeNodes) {
                stringBuilder.append(node.getDesc() + "\n");
                stringBuilder.append(node.getPath() + "\n");
                Map<String, String> paramsType = node.getParamsDesc();
                if (MapUtils.isNotEmpty(paramsType)) {
                    for (Map.Entry<String, String> types : paramsType.entrySet()) {
                        stringBuilder.append(types.getKey() + ":" + types.getValue() + "\n");
                    }
                }
                stringBuilder.append("\n");
            }
            FileUtils.writeStringToFile(fileName, stringBuilder.toString(), false);
        }
    }


    /**
     * path 校验
     *
     * @param path
     */
    private void checkPath(String path) {
        if (path == null || path.isEmpty() || !path.startsWith("/")) {
            throw new IllegalStateException("path cannot be null or empty and should start with /,this is " + path);
        }

        if (path.contains("//") || path.contains("&") || path.contains("?")) {
            throw new IllegalArgumentException("path should not contain // ,& or ?,this is:" + path);
        }

        if (path.endsWith("/"))
            throw new IllegalArgumentException("path should not endWith /,this is:" + path + ";or append a token:index");
    }


    /**
     * 生成getHost方法
     */
    private MethodSpec generateGetHostMethod() {
        TypeName returnType = TypeName.get(typeString);

        return MethodSpec.methodBuilder("getHost")//方法名称
                //.addParameter(String.class,"") //添加参数
                .addAnnotation(Override.class)//添加Override 注解
                .addModifiers(Modifier.PUBLIC)//添加访问
                .returns(returnType) //返回值
                .addStatement("return $S", host)//添加代码
                .build();
    }

    /**
     * 生成initMap 方法
     */
    private MethodSpec generateInitMapMethod() {

        TypeName returnType = TypeName.VOID;

        MethodSpec.Builder builder = MethodSpec.methodBuilder("initMap")//方法名称
                //.addParameter(String.class,"") //添加参数
                .addAnnotation(Override.class)//添加Override 注解
                .addModifiers(Modifier.PUBLIC)//添加访问
                .returns(returnType) //返回值
                .addStatement("super.initMap()");//添加代码

        for (Node node : routeNodes) {
            builder.addStatement(Constants.ROUTE_MAPPER_FIELD_NAME + ".put($S,$T.class)", node.getPath(),
                    ClassName.get((TypeElement) node.getRawType()));

            StringBuilder mapBodyBuilder = new StringBuilder();
            Map<String, Integer> paramsType = node.getParamsType();
            if (MapUtils.isNotEmpty(paramsType)) {
                Set<Map.Entry<String, Integer>> entries = paramsType.entrySet();
                for (Map.Entry<String, Integer> entry : entries) {
                    mapBodyBuilder.append("put(").append(entry.getKey()).append(", ").append(entry.getValue()).append(");");
                }
            }

            String mapBody = mapBodyBuilder.toString();
            logger.info("mapBody is " + mapBody);

            if (!StringUtils.isEmpty(mapBody)) {
                builder.addStatement(
                        Constants.PARAMS_MAPPER_FIELD_NAME + ".put($T.class,new java.util.HashMap<String,Integer>(){{" +
                                mapBody + "}})", ClassName.get((TypeElement) node.getRawType()));
            }
        }
        return builder.build();
    }
}
