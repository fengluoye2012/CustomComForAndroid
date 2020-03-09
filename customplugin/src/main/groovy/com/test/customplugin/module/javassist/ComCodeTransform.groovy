package com.test.customplugin.module.javassist

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.test.customplugin.module.Config
import javassist.*
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * Gradle Transform技术，简单来说就是能够让开发者在项目构建阶段即由class到dex转换期间修改class文件，
 * Transform阶段会扫描所有的class文件和资源文件，找到目标class文件(即IAppLike 其子类和Application 的子类)，
 * 并通过ASM 在Application的子类的方法中插入相关代码。
 *
 *
 * 1）找到IAppLike子类的集合，通过
 * 2）找到作为主工程（即主工程或者单独运行的module）的Application 子类，在onCreate()方法中初始化IAppLike子类
 *
 * 避免在每个子module 中添加该方法。
 */
class ComCodeTransform extends Transform {

    Project project

    /**
     * 当前作为主工程的Application
     */
    String applicationName

    ClassPool classPool

    ComCodeTransform(Project project) {
        this.project = project
    }

    //该Transform的名称，自定义即可，只是一个标示
    @Override
    String getName() {
        return "ComCodeTransform"
    }

    //该Transform支持扫描的文件类型，分为class文件和资源文件，我们这里只处理class文件扫描
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    //Transform的扫描范围，扫描整个工程，包括当前module及其他jar包、arr文件等所有的class
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    //是否增量扫描
    @Override
    boolean isIncremental() {
        return false
    }


    /**
     * 扫描所有的文件，找到目标class 文件
     */
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        println("start to transform ----->>>>>>")

        Collection<TransformInput> inputs = transformInvocation.inputs
        def context = transformInvocation.context
        def referencedInputs = transformInvocation.referencedInputs
        def outputProvider = transformInvocation.outputProvider
        def incremental = transformInvocation.incremental

        getRealApplication()

        classPool = new ClassPool()

        def android = project.extensions.getByType(AppExtension)
        android.bootClasspath.each {
            //添加搜索路径，可以搜索当前路径下所有的class 文件
            classPool.appendClassPath((String) it.absolutePath)
        }
        def box = ConvertUtils.toCtClass(inputs, classPool)

        //要收集的application,一般情况下只有一个
        List<CtClass> applications = new ArrayList<>()
        //要收集的IAppLike,一般情况下有几个组件就有几个IAppLike
        List<CtClass> IAppLikes = new ArrayList<>()

        println("all class size is " + box.size())
        for (CtClass ctClass : box) {
            if (isApplication(ctClass)) {
                applications.add(ctClass)
                continue
            }

            if (isIAppLike(ctClass)) {
                IAppLikes.add(ctClass)
            }
        }

        for (CtClass ctClass : applications) {
            println("application is" + ctClass.name)
        }

        for (CtClass ctClass : IAppLikes) {
            println("IAppLike is " + ctClass.name)
        }

        //inputs就是所有扫描到class文件或者是jar包，一共两种类型
        inputs.each { TransformInput input ->
            //1.遍历所有的class文件目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //是否自动注册
                boolean isRegisterCompoAuto = project.extensions.comBuild.isRegisterCompoAuto
                //自动注册，找到Application 并在onCreate()插入代码
                if (isRegisterCompoAuto) {
                    String fileName = directoryInput.file.absolutePath
                    File dir = new File(fileName)
                    dir.eachFileRecurse { File file ->
                        String filePath = file.absolutePath
                        String classNameTemp = filePath.replace(fileName, "").replace("\\", Config.DOT).replace("/", Config.DOT)
                        if (classNameTemp.endsWith(".class")) {
                            String className = classNameTemp.substring(1, classNameTemp.length() - 6)
                            if (className.equals(applicationName)) {
                                injectApplicationCode(applications.get(0), IAppLikes, fileName)
                            }
                        }
                    }
                }

                //Transform扫描的class文件是输入文件（input）,有输入必然有输出（output），处理完成后需要将输入文件拷贝到一个输出目录下去
                //后面打包将class 文件转换成dex 文件时，直接采用的就是输出目录下的class文件了，必须这样获取输出路径的目录名称
                def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            //2、遍历查找所有的jar包
            input.jarInputs.each { JarInput jarInput ->

                //jar文件一般是第三方依赖库jar 文件
                //重命名输出文件（同目录copyFile 会冲突）
                //与处理class文件一样，处理jar包也是一样，最后要将inputs转换为outputs
                def jarName = jarInput.name
                def md5 = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //获取输出路径下的jar 包名称，必须这样获取，得到的输出路径名不能重复，否则会被覆盖
                def dest = outputProvider.getContentLocation(jarName + md5, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                //将输入文件拷贝到输出目录下
                FileUtils.copyFile(jarInput.file, dest)
            }
        }

        println("transform finish -----------<<<<\n")
    }


    /**
     * 获取作为主工程的Application
     */
    void getRealApplication() {
        applicationName = project.extensions.comBuild.applicationName
        if (applicationName == null || applicationName.isEmpty()) {
            throw new RuntimeException("you should set applicationName in comBuild in current module's build.gradle")
        }
        println("applicationName is ${applicationName}")
    }

    /**
     * 找到目标Application
     * @param ctClass
     * @return
     */
    private boolean isApplication(CtClass ctClass) {
        try {
            if (applicationName != null && applicationName.equals(ctClass.getName())) {
                return true
            }
        } catch (Exception e) {
            println "class not found exception class name:  " + ctClass.getName()
        }
        return false
    }

    /**
     * 获取当前类的接口类，是否实现IAppLike 接口
     * @param ctClass
     * @return
     */
    private static boolean isIAppLike(CtClass ctClass) {
        try {
            for (CtClass ctClassInter : ctClass.getInterfaces()) {
                if (Config.IAPPLIKE_FULL_NAME.equals(ctClassInter.name)) {
                    return true
                }
            }
        } catch (Exception e) {
            println "class not found exception class name:  " + ctClass.getName()
        }
        return false
    }

    /**
     * 找到onCreate()方法（如果没有重写onCreate()方法，则添加onCreate()），在其方法中插入指定代码，
     * @param ctClassApplication
     * @param IAppLikes
     * @param patch
     */
    private static void injectApplicationCode(CtClass ctClassApplication, List<CtClass> IAppLikes, String patch) {
        println("injectApplicationCode start")

        //CtClass对象被writeFile(),toClass()或者toBytecode()转换成了类对象，Javassist将会冻结此CtClass对象，所以要先解冻；
        ctClassApplication.defrost()
        try {
            CtMethod onCreateMethod = ctClassApplication.getDeclaredMethod("onCreate", null)
            onCreateMethod.insertAfter(getAutoLoadComCode(IAppLikes))
        } catch (CannotCompileException | NotFoundException e) {
            //不存在目标onCreate()方法，添加onCreate()方法
            StringBuilder methodBody = new StringBuilder()
            methodBody.append("protected void onCreate() {")
            methodBody.append("super.onCreate();")
            methodBody.append(getAutoLoadComCode(IAppLikes))
            methodBody.append("}")
            ctClassApplication.addMethod(CtMethod.make(methodBody.toString(), ctClassApplication))
        } catch (Exception e) {
            println("在 Application中 插入code 时异常")
        }

        ctClassApplication.writeFile(patch)
        //避免内存溢出
        ctClassApplication.detach()

        println("injectApplicationCode success")

    }

    private static String getAutoLoadComCode(List<CtClass> IAppLikes) {
        StringBuilder autoLoadComCode = new StringBuilder()
        for (CtClass ctClass : IAppLikes) {
            //调用IAppLike实现类的onCreate()方法
            autoLoadComCode.append("new " + ctClass.getName() + "()" + ".onCreate();")
        }
        println("autoLoadComCode " + autoLoadComCode.toString())
        return autoLoadComCode.toString()
    }
}
