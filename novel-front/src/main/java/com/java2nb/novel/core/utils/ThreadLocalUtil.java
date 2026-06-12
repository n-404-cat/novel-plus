package com.java2nb.novel.core.utils;

/**
 * 模板操作工具类
 * @author Administrator
 */
public class ThreadLocalUtil {

    /**
     * 存储当前线程访问的模板目录
     * */
    private static final ThreadLocal<String> templateDir = new ThreadLocal<>();

    /**
     * 存储当前会话的sessionID
     * */
    private static final ThreadLocal<String> clientId = new ThreadLocal<>();

    /**
     * 存储当前线程实际使用的模板名称。
     */
    private static final ThreadLocal<String> templateName = new ThreadLocal<>();

    /**
     * 设置当前应该访问的模板目录
     * */
    public static void setTemplateDir(String dir){
        templateDir.set(dir);
    }

    /**
     * 获取当前应该访问的模板路径前缀
     * */
    public static String getTemplateDir(){
        return templateDir.get();
    }
    
    /**
     * 设置当前访问线程的客户端ID
     * */
    public static void setClientId(String id){
        clientId.set(id);
    }

    /**
     * 设置当前请求正在使用的模板名称。
     */
    public static void setTemplateName(String name){
        templateName.set(name);
    }

    /**
     * 获取当前请求正在使用的模板名称。
     */
    public static String getTemplateName(){
        return templateName.get();
    }

    /**
     * 清理当前线程中的模板上下文，避免线程复用时串请求。
     */
    public static void clear(){
        templateDir.remove();
        clientId.remove();
        templateName.remove();
    }



}
