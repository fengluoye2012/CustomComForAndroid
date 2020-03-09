package com.test.customplugin.module.javassist

import com.android.SdkConstants
import com.android.build.api.transform.TransformInput
import com.test.customplugin.module.Config
import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.io.FileUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher

class ConvertUtils {

    /**
     * 获取所有的.class 文件
     * @param inputs
     * @param classPool
     * @return
     */
    static List<CtClass> toCtClass(Collection<TransformInput> inputs, ClassPool classPool) {

        println("start toCtClass")

        List<String> classNames = new ArrayList<>()
        List<CtClass> allClass = new ArrayList<>()

        //inputs就是所有扫描到class文件或者是jar包，一共两种类型
        inputs.each {
            //1.遍历所有的class文件目录
            it.directoryInputs.each {
                def dirPath = it.file.absolutePath
                classPool.insertClassPath(it.file.absolutePath)
                FileUtils.listFiles(it.file, null, true).each {
                    if (it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                        def className = it.absolutePath.substring(dirPath.length() + 1, it.absolutePath.length() - SdkConstants.DOT_CLASS.length()).replace(Matcher.quoteReplacement(File.separator), Config.DOT)
                        if (classNames.contains(className)) {
                            throw new RuntimeException("You have duplicate classes with the same name : " + className + " please remove duplicate classes ")
                        }
                        classNames.add(className)
                    }
                }
            }

            //2、遍历查找所有的jar包
            it.jarInputs.each {
                classPool.insertClassPath(it.file.absolutePath)
                def jarFile = new JarFile(it.file)
                Enumeration<JarEntry> classes = jarFile.entries()

                while (classes.hasMoreElements()) {
                    JarEntry libClass = classes.nextElement()
                    String className = libClass.getName()
                    if (className.endsWith(SdkConstants.DOT_CLASS)) {
                        className = className.substring(0, className.length() - SdkConstants.DOT_CLASS.length()).replaceAll(File.separator, Config.DOT)
                        if (classNames.contains(className)) {
                            throw new RuntimeException("You have duplicate classes with the same name : " + className + " please remove duplicate classes ")
                        }
                        classNames.add(className)
                    }
                }
            }

        }

        classNames.each {
            try {
                allClass.add(classPool.get(it))
            } catch (Exception e) {
                println("class not found exception class name:  $it ")
            }
        }


        return allClass
    }
}