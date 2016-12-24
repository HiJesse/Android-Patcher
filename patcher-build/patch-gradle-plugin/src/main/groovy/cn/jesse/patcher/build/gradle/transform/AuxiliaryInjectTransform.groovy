package cn.jesse.patcher.build.gradle.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.google.common.collect.ImmutableSet
import org.gradle.api.Project


/**
 * Created by jesse on 24/12/2016.
 */

public class AuxiliaryInjectTransform extends Transform {
    private static final String TRANSFORM_NAME = 'AuxiliaryInject'

    private final Project project

    private boolean isEnabled = false

    def applicationVariants

    public AuxiliaryInjectTransform(Project project) {
        this.project = project

        project.afterEvaluate {
            this.isEnabled = project.patcher.dex.usePreGeneratedPatchDex

            this.applicationVariants = project.android.applicationVariants
        }
    }

    /**
     * Returns the unique name of the transform.
     *
     * <p/>
     * This is associated with the type of work that the transform does. It does not have to be
     * unique per variant.
     */
    @Override
    String getName() {
        return TRANSFORM_NAME
    }

    /**
     * Returns the type(s) of data that is consumed by the Transform. This may be more than
     * one type.
     *
     * <strong>This must be of type {@link QualifiedContent.DefaultContentType}</strong>
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES)
    }

    /**
     * Returns the scope(s) of the Transform. This indicates which scopes the transform consumes.
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return ImmutableSet.of(
                QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES
        )
    }

    /**
     * Returns whether the Transform can perform incremental work.
     *
     * <p/>
     * If it does, then the TransformInput may contain a list of changed/removed/added files, unless
     * something else triggers a non incremental run.
     */
    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        project.getLogger().error("in transform " + isEnabled)
    }
}
