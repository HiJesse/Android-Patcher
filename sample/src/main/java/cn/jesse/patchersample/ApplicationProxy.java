package cn.jesse.patchersample;

import cn.jesse.patcher.anno.PatcherApplication;

/**
 * Created by jesse on 14/11/2016.
 */
@PatcherApplication(
        application = "com.test",
        flags = 1,
        loadVerifyFlag = false
)
public class ApplicationProxy {

}
