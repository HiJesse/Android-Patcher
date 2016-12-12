import cn.jesse.patcher.build.gradle.extension.PatcherExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by jesse on 12/12/2016.
 */
public class PatcherPlugin implements Plugin<Project> {
    private final String EXTENSION_PATCHER = "patcher"

    @Override
    void apply(Project project) {
        project.extensions.create(EXTENSION_PATCHER, PatcherExtension)
        project.tasks.create("patcherTest", PatcherTask)
    }
}