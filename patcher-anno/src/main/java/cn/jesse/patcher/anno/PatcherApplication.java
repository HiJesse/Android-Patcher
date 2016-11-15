package cn.jesse.patcher.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jesse on 14/11/2016.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface PatcherApplication {

    /**
     * 真实的Application
     */
    String application();

    /**
     * patch loader
     */
    String loaderClass() default "cn.jesse.patcher.loader.PatcherLoader";

    /**
     * 支持的文件类型
     * Constants.PATCHER_DISABLE:不支持任何类型的文件
     * Constants.PATCHER_DEXONLY:只支持dex文件
     * Constants.PATCHER_LIBRARYONLY:只支持library文件
     * Constants.PATCHER_DEXANDLIBRARY:只支持dex与res的修改
     * Constants.PATCHER_ENABLEALL:支持任何类型的文件，也是我们通常的设置的模式
     */
    int flags();

    /**
     * 是否每次都校验补丁包的MD5
     */
    boolean loadVerifyFlag() default false;


}
