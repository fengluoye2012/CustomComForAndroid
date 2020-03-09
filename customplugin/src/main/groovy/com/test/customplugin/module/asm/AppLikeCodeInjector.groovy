package com.test.customplugin.module.asm


import jdk.internal.org.objectweb.asm.*
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter
import org.apache.commons.io.IOUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * 在目标class 中动态插入代码
 * 根据Transform中找到的 目标class文件全类名，插入AppLifeCycleManager的loadAppLike()方法中
 * 如：registerAppLike("com.test.lifecycle_apt.proxy.fly$$ModuleAAppLike$$Proxy");
 */
class AppLikeCodeInjector {

    //扫描出来的所有IAppLike类
    List<String> proxyAppLikeClassList

    /**
     * 注入的目标方法
     */
    private String TARGET_METHOD_NAME = "loadAppLike"

    AppLikeCodeInjector(List<String> list) {
        this.proxyAppLikeClassList = list
    }

    void execute() {
        println("开始执行ASM方法 =======>>>>>>>>>>")
        File srcFle = CusScanUtil.FILE_CONTAINS_INIT_CLASS

        //创建一个临时jar文件，要修改注入的字节码会先写入该文件里
        File optJar = new File(srcFle.getParent(), srcFle.name + ".opt")
        if (optJar.exists()) {
            optJar.delete()
        }
        def file = new JarFile(srcFle)
        Enumeration<JarEntry> enumeration = file.entries()
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = enumeration.nextElement()
            def entryName = jarEntry.getName()
            def zipEntry = new ZipEntry(entryName)
            def inputStream = file.getInputStream(jarEntry)
            jarOutputStream.putNextEntry(zipEntry)

            //找到需要插入代码的class,通过ASM动态注入字节码
            if (CusScanUtil.REGISTER_CLASS_FILE_NAME == entryName) {
                println("insert register code to class >>" + entryName)
                ClassReader classReader = new ClassReader(inputStream)
                //构建一个ClassWriter对象，并设置让系统自动计算栈和本地变量大小
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
                def classVisitor = new AppLikeClassVisitor(classWriter)
                //开始扫描class 文件
                classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)

                def bytes = classWriter.toByteArray()
                //将注入过字节码的class,写入临时jar文件里
                jarOutputStream.write(bytes)
            } else {
                //不需要修改的class,写入临时jar文件里
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            inputStream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()

        //删除原来的jar文件
        if (srcFle.exists()) {
            srcFle.delete()
        }
        //重新命名临时jar文件，新的jar包里已经包含了我们注入的字节码了
        optJar.renameTo(srcFle)
    }


    //插入字节码的逻辑，都在这个类里面
    class AppLikeClassVisitor extends ClassVisitor {

        AppLikeClassVisitor(ClassVisitor classVisitor) {
            super(Opcodes.ASM5, classVisitor)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exception) {
            println("visit method:" + name)
            def mv = super.visitMethod(access, name, desc, signature, exception)
            //找到AppLifeCycleManager里的loadAppLike()方法，我们在这个方法里插入字节码
            if (TARGET_METHOD_NAME == name) {
                println("------目标方法==  ${TARGET_METHOD_NAME} ------- ")
                mv = new LoadAppLikeMethodAdapter(mv, access, name, desc)
            }
            return mv
        }
    }

    class LoadAppLikeMethodAdapter extends AdviceAdapter {

        protected LoadAppLikeMethodAdapter(MethodVisitor mv, int access, String name, String descriptor) {
            super(Opcodes.ASM5, mv, access, name, descriptor)
        }

        @Override
        protected void onMethodEnter() {
            super.onMethodEnter()

            println("---- onMethodEnter ----")
            //遍历插入字节码，其实就是在 loadAppLike()方法里插入类似registerAppLike("")的字节码
            proxyAppLikeClassList.forEach({ proxyClassName ->
                println("开始注入代码：${proxyClassName}")
                def fullName = CusScanUtil.PROXY_CLASS_PACKAGE_NAME.replace("/", ".") + "." + proxyClassName.substring(0, proxyClassName.length() - 6)
                println("full className = ${fullName}")
                mv.visitLdcInsn(fullName)
                mv.visitMethodInsn(INVOKESTATIC, "com/test/lifecycle_api/AppLifeCycleManager", "registerAppLike", "(Ljava/lang/String;)V", false)
            })
        }

        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode)
            println("------ onMethodExit -------")
        }
    }

}