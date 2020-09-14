package com.q.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 反射工具类
 */
@Slf4j
public class ReflectionUtils {
    public static final String FILE_PROTOCOL = "file";
    /**
     * 创建对象
     * @param clazz
     * @param <T>
     * @return
     */
    public static  <T> T newInstance(Class<T> clazz){
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取某个class的public方法
     * @param clazz
     * @return
     */
    public static Method[] getPublicMethods(Class clazz){
        Method[] methods=clazz.getDeclaredMethods();
        List<Method> pmethods=new ArrayList<>();
        for(Method m:methods){
            if(Modifier.isPublic(m.getModifiers())){
                pmethods.add(m);
            }
        }
        return pmethods.toArray(new Method[0]);
    }

    /**
     * 调用指定对象的指定方法
     * @param obj
     * @param method
     * @param args
     * @return
     */
    public  static Object invoke(Object obj,Method method,Object... args){
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取Class对象
     *
     * @param className class全名=package + 类名
     * @return Class
     */
    public static Class<?> loadClass(String className){
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("load class error:", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 获得栈底类名即main所在类
     * @return
     */
    public static String getStackTrace() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        return stack[stack.length - 1].getClassName();
    }
    /**
     * 获取classLoader
     *
     * @return 当前ClassLoader
     */
    public static  ClassLoader getClassLoader(){
        return Thread.currentThread().getContextClassLoader();
    }
    /**
     * 获得所在包下所有类
     * @param basePackage 扫描包路径
     * @return
     */
    public static Set<Class<?>> getClasses(String basePackage) {
        ClassLoader classLoader=getClassLoader();

        URL url = classLoader.getResource(basePackage.replace(".", "/"));
        if(url==null){
            log.warn("unable to retrieve anything from package: " + basePackage);
            return  null;
        }
        //3.依据不同的资源类型，采用不同的方式获取资源的集合
        Set<Class<?>> classSet = null;
        //过滤出文件类型的资源
        if (url.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)){
            classSet = new HashSet<Class<?>>();
            File packageDirectory = new File(url.getPath());//目录
            extractClassFile(classSet, packageDirectory, basePackage);
        }
        //TODO 此处可以加入针对其他类型资源的处理

        return classSet;
    }
    /**
     * 递归获取目标package里面的所有class文件(包括子package里的class文件)
     *
     * @param emptyClassSet 装载目标类的集合
     * @param fileSource    文件或者目录
     * @param packageName   包名
     * @return 类集合
     */
    private static void extractClassFile(Set<Class<?>> emptyClassSet, File fileSource, String packageName) {
        if(!fileSource.isDirectory()){
            return;
        }
        //如果是一个文件夹，则调用其listFiles方法获取文件夹下的文件或文件夹
        File[] files = fileSource.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if(file.isDirectory()){
                    return true;
                } else{
                    //获取文件的绝对值路径
                    String absoluteFilePath = file.getAbsolutePath();
                    if(absoluteFilePath.endsWith(".class")){
                        //若是class文件，则直接加载
                        addToClassSet(absoluteFilePath);
                    }
                }
                return false;
            }
            //根据class文件的绝对值路径，获取并生成class对象，并放入classSet中
            private void addToClassSet(String absoluteFilePath) {
                //1.从class文件的绝对值路径里提取出包含了package的类名
                //如/Users/baidu/imooc/springframework/sampleframework/target/classes/com/imooc/entity/dto/MainPageInfoDTO.class
                //需要弄成com.imooc.entity.dto.MainPageInfoDTO
                absoluteFilePath = absoluteFilePath.replace(File.separator, ".");
                String className = absoluteFilePath.substring(absoluteFilePath.indexOf(packageName));
                className = className.substring(0, className.lastIndexOf("."));
                //2.通过反射机制获取对应的Class对象并加入到classSet里
                Class targetClass = loadClass(className);
                emptyClassSet.add(targetClass);
            }
        });
        if(files != null){
            for(File f : files){
                //递归调用
                extractClassFile(emptyClassSet, f, packageName);
            }
        }
    }
}
