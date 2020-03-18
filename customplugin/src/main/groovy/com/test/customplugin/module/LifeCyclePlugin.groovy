package com.test.customplugin.module

import com.android.build.gradle.AppExtension
import com.test.customplugin.module.exten.ComExtension
import com.test.customplugin.module.javassist.ComCodeTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class LifeCyclePlugin implements Plugin<Project> {

    private String TAG = LifeCyclePlugin.class.getSimpleName()

    //默认是app，直接运行assembleRelease的时候，等同于运行app:assembleRelease
    String compileModule = "app"

    @Override
    void apply(Project project) {
        println("---- LifeCycle plugin entrance ----")
        gradleConfigDeal(project)
    }


    /**
     * android 项目gradle配置处理
     * @param project
     */
    private void gradleConfigDeal(Project project) {

        //用来自定义Extensions,只需要在build.gradle中加入
        //comBuild{
        //   对应的属性
        // }
        //
        project.extensions.create(Config.EXTENSIONS_NAME, ComExtension)

        String taskNames = project.gradle.startParameter.taskNames.toString()
        println("taskNames is ${taskNames}")

        String module = project.path.replace(":", "")
        println("current module  is ${module}")

        AssembleTask assembleTask = getTaskInfo(project.gradle.startParameter.taskNames)
        if (assembleTask.isAssemble) {
            fetchMainModuleName(project, assembleTask)
            println("compileModule is ${compileModule}")
        }

        //判断在当前module目录下gradle.properties 是否存在isRunAlone 属性
        if (!project.hasProperty(Config.IS_RUN_ALONE_PARA)) {
            throw new RuntimeException("you should set ${Config.IS_RUN_ALONE_PARA} in ${module} 's gradle.properties")
        }

        //对于isRunAlone == true的情况 需要根据实际情况修改其值，但如果是false 就不用修改
        boolean isRunAlone = Boolean.parseBoolean(project.properties.get(Config.IS_RUN_ALONE_PARA))
        def mainModuleName = project.rootProject.property(Config.MAIN_MODULE_NAME_PARA)
        if (isRunAlone && assembleTask.isAssemble) {
            //这对于要编译的组件和主项目，isRunAlone 修改为true,其他组件都强制修改为false
            //这就意味着组件不能引用主项目，这在层级结构里面也是这么规定的
            if (module.equals(compileModule) || module.equals(mainModuleName)) {
                isRunAlone = true
            } else {
                isRunAlone = false
            }
        }
        project.setProperty(Config.IS_RUN_ALONE_PARA, isRunAlone)


        //根据配置添加各种组件依赖，并且自动化生成组件加载代码
        if (isRunAlone) {
            project.apply plugin: 'com.android.application'

            //不是主工程的话,配置sourceSets。
            if (!module.equals(mainModuleName)) {
                println("${module} is run alone")
                project.android.sourceSets {
                    main {
                        manifest.srcFile 'src/main/runAlone/AndroidManifest.xml'
                        java.srcDirs = ['src/main/java', 'src/main/runAlone/java']
                        res.srcDirs = ['src/main/res', 'src/main/runAlone/res']
                        assets.srcDirs = ['src/main/assets', 'src/main/runAlone/assets']
                        jniLibs.srcDirs = ['src/main/jniLibs', 'src/main/runAlone/jniLibs']
                    }
                }
            }
            println("${module} apply plugin is com.android.application")
            if (assembleTask.isAssemble && module.equals(compileModule)) {
                compileComponents(assembleTask, project)
                //可以用 project.android 代替 project.getExtensions().getByType(AppExtension)
                def android = project.getExtensions().getByType(AppExtension)
                //在插件中注册该Transform
                android.registerTransform(new ComCodeTransform(project))
            }
        } else {
            project.apply plugin: 'com.android.library'
            println("${module} apply plugin is " + 'com.android.library')
        }
    }

    /**
     * taskNames集合中的元素类似：
     * [:customplugin:assemble, :customplugin:testClasses, :lifecycle_apt:assemble, :lifecycle_apt:testClasses, :app:assembleDebug]
     * @param taskNames
     * @return
     */
    private AssembleTask getTaskInfo(List<String> taskNames) {
        AssembleTask assembleTask = new AssembleTask()
        for (String task : taskNames) {
            if (task.toUpperCase().contains("ASSEMBLE") || task.contains("aR")
                    || task.toUpperCase().contains("TINKER") || task.toUpperCase().contains("INSTALL")
                    || task.toUpperCase().contains("RESGUARD")) {

                if (task.toUpperCase().contains("DEBUG")) {
                    assembleTask.isDebug = true
                }
                assembleTask.isAssemble = true
                def strs = task.split(":")
                assembleTask.modules.add(strs.length > 1 ? strs[strs.length - 2] : "all")
                break
            }
        }

        return assembleTask
    }


    /**
     * 根据当前的task,获取要运行的组件，规则如下：
     * assembleRelease ---app
     * app:assembleRelease :app:assembleRelease ---app
     * sharecomponent:assembleRelease :sharecomponent:assembleRelease ---sharecomponent
     *
     * @param project
     * @param assembleTask
     */
    private void fetchMainModuleName(Project project, AssembleTask assembleTask) {

        //判断在跟目录下gradle.properties 中是否包含mainModuleName 属性
        if (!project.rootProject.hasProperty(Config.MAIN_MODULE_NAME_PARA)) {
            throw new RuntimeException("you should set compileModule in rootProject's gradle.properties")
        }

        if (assembleTask.modules.size() > 0 && assembleTask.modules.get(0) != null
                && assembleTask.modules.get(0).trim().length() > 0 && !assembleTask.modules.get(0).equals("all")) {
            compileModule = assembleTask.modules.get(0)
        } else {
            compileModule = project.rootProject.project(Config.MAIN_MODULE_NAME_PARA)
        }

        if (compileModule == null || compileModule.trim().length() <= 0) {
            compileModule = Config.DEFAULT_MAIN_MODULE_NAME
        }
    }


    /**
     * 自动往当前module所在的build.gradle的dependencies 中添加依赖
     *
     * 自动添加依赖，只在运行assemble 任务才会添加依赖，因此在开发期间组件之间是完全感知不到的，这是做到完全隔离的关键
     * 支持两种语法：module 或者groupId:artifactId:version(@aar)，前者之间引用module工程，后者使用maven中已经发布的aar
     * @param assembleTask
     * @param project
     */
    private void compileComponents(AssembleTask assembleTask, Project project) {
        String components
        if (assembleTask.isDebug) {
            //当前module中gradle.properties中属性的值
            components = project.properties.get(Config.DEBUG_COMPONENT)
        } else {
            components = project.properties.get(Config.RELEASE_COMPONENT)
        }

        if (components == null || components.length() == 0) {
            println("there is no add dependencies")
            return
        }

        println("需要添加的依赖：${components}")

        String[] compileComponents = components.split(",")
        if (compileComponents == null || compileComponents.length == 0) {
            println("there is no add dependencies")
            return
        }



        for (String str : compileComponents) {
            println("需要添加的依赖：comp is ${str}")
            if (str.contains(":")) {
                /**
                 * 示例语法:groupId:artifactId:version(@aar)
                 * compileComponent=com.luojilab.reader:readercomponent:1.0.0
                 * 注意，前提是已经将组件aar文件发布到maven上，并配置了相应的repositories
                 */
                project.dependencies.add("api", str)
                println("add dependencies lib ：${str}")
            } else {
                /**
                 * 示例语法:module
                 * compileComponent=readercomponent,sharecomponent
                 */
                project.dependencies.add("api", project.project(":" + str))
                println("add dependencies project ：${str}")
            }
        }
    }

    private class AssembleTask {
        boolean isAssemble = false
        boolean isDebug = false
        List<String> modules = new ArrayList<>()
    }
}
