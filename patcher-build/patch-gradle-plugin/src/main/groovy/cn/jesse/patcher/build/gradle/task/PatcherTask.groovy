import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class PatcherTask extends DefaultTask {

    PatcherTask() {
        super()
        group = 'patcher'
    }

    @TaskAction
    public void test() {
        println(project.patcher.toString());
    }
}